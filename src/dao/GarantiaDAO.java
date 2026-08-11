package dao;

import factory.ConexionFactory;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.text.SimpleDateFormat;

public class GarantiaDAO {
    private ConexionFactory factory;

    public GarantiaDAO() {
        this.factory = new ConexionFactory();
    }

    public List<Object[]> listarGarantias() {
        List<Object[]> lista = new ArrayList<>();
        
        String sql = "SELECT v.id_ventas, v.id_cliente_venta, c.nombre_cliente, c.apellido_cliente, " +
                     "d.id_detalle_venta, d.descripcion_venta, d.identificador_serie, v.fecha_venta, d.dias_garantia, " + 
                     "d.estado_garantia " +
                     "FROM DETALLES_VENTA d " +
                     "INNER JOIN VENTAS v ON d.id_ventas = v.id_ventas " +
                     "LEFT JOIN CLIENTES c ON v.id_cliente_venta = c.id_cliente " +
                     "WHERE d.dias_garantia > 0 " +
                     "ORDER BY v.fecha_venta DESC";

        try (Connection con = factory.getConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            Date hoy = new Date();

            while (rs.next()) {
                int idVenta = rs.getInt("id_ventas");
                int idCliente = rs.getInt("id_cliente_venta");
                
                // Formatear el nombre del cliente o Consumidor Final
                String nombreClie = rs.getString("nombre_cliente");
                if (rs.getString("apellido_cliente") != null) {
                    nombreClie += " " + rs.getString("apellido_cliente");
                }
                if (idCliente == 1 || nombreClie == null || nombreClie.trim().isEmpty()) {
                    nombreClie = "CONSUMIDOR FINAL";
                }

                String producto = rs.getString("descripcion_venta");
                String serie = rs.getString("identificador_serie");
                if (serie == null || serie.trim().isEmpty()) serie = "N/A";

                java.sql.Timestamp fechaVenta = rs.getTimestamp("fecha_venta");
                int diasGarantia = rs.getInt("dias_garantia");

                // C\u00E1lculo matem\u00E1tico de la fecha de vencimiento
                Calendar cal = Calendar.getInstance();
                cal.setTime(fechaVenta);
                cal.add(Calendar.DAY_OF_YEAR, diasGarantia);
                Date fechaVencimiento = cal.getTime();

                String estadoBD = rs.getString("estado_garantia"); // Leemos la BD
                String estado = "VENCIDA";
                
                if (estadoBD != null && estadoBD.equals("RECLAMADA")) {
                    estado = "RECLAMADA"; // Prioridad 1: Si ya se reclam\u00F3, se queda gris
                } else if (fechaVencimiento.after(hoy)) {
                    estado = "VIGENTE";   // Prioridad 2: Si no se ha reclamado y tiene tiempo, es verde
                }

                // Empaquetar la fila exacta como la espera el modelo de la tabla
                lista.add(new Object[]{
                    "Venta #" + idVenta,
                    nombreClie.trim(),
                    producto,
                    serie,
                    sdf.format(fechaVenta),
                    sdf.format(fechaVencimiento),
                    estado,
                    idVenta, // ID Venta Oculto
                    rs.getInt("id_detalle_venta") // <-- Ahora capturamos el ID real
                });
            }
        } catch (SQLException e) {
            System.err.println("Error listando garant\u00EDas en Orion Systems: " + e.getMessage());
        }
        return lista;
    }
     public boolean aplicarReclamo(int idDetalleVenta, String observacion, String fotoBase64, String resolucion, boolean reintegro, int idUsuario) {
        String sql = "UPDATE DETALLES_VENTA SET estado_garantia = 'RECLAMADA', observacion_garantia = ?, foto_garantia = ?, resolucion_garantia = ? WHERE id_detalle_venta = ?";
        
        Connection con = null;
        try {
            con = factory.getConexion();
            con.setAutoCommit(false);
            
            try(PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, observacion);
                ps.setString(2, fotoBase64);
                ps.setString(3, resolucion);
                ps.setInt(4, idDetalleVenta);
                int filasAfectadas = ps.executeUpdate();
                
                if (filasAfectadas == 0) {
                    con.rollback();
                    return false;
                }
            }
            
            // Obtener producto original
            int idProdOriginal = -1;
            try(PreparedStatement psInfo = con.prepareStatement("SELECT id_producto FROM DETALLES_VENTA WHERE id_detalle_venta = ?")){
                psInfo.setInt(1, idDetalleVenta);
                try(java.sql.ResultSet rs = psInfo.executeQuery()){
                    if(rs.next()){ idProdOriginal = rs.getInt("id_producto"); }
                }
            }
            
            if (idProdOriginal != -1) {
                if (resolucion.startsWith("Reparaci\u00F3n T\u00E9cnica")) {
                    // Propiedad del cliente: entra a inventario defectuoso con estado especial, no afecta KARDEX ni stock normal
                    String sqlDefectuoso = "INSERT INTO INVENTARIO_DEFECTUOSO (id_producto, id_detalle_venta, fecha_ingreso, cantidad, motivo_danio, estado_defecto) VALUES (?, ?, GETDATE(), 1, ?, 'En Bodega (Rep. Cliente)')";
                    try (PreparedStatement psDef = con.prepareStatement(sqlDefectuoso)) {
                        psDef.setInt(1, idProdOriginal);
                        psDef.setInt(2, idDetalleVenta);
                        psDef.setString(3, observacion);
                        psDef.executeUpdate();
                    }
                } else if (resolucion.equals("Cambio por Producto Nuevo")) {
                    // Propiedad de la empresa: entra a inventario defectuoso, resta stock del producto nuevo entregado
                    if (reintegro) {
                        String sqlDefectuoso = "INSERT INTO INVENTARIO_DEFECTUOSO (id_producto, id_detalle_venta, fecha_ingreso, cantidad, motivo_danio, estado_defecto) VALUES (?, ?, GETDATE(), 1, ?, 'En Bodega')";
                        try (PreparedStatement psDef = con.prepareStatement(sqlDefectuoso)) {
                            psDef.setInt(1, idProdOriginal);
                            psDef.setInt(2, idDetalleVenta);
                            psDef.setString(3, observacion);
                            psDef.executeUpdate();
                        }
                    }
                    
                    try(PreparedStatement psUpStock = con.prepareStatement("UPDATE INVENTARIO SET stock_producto = stock_producto - 1 WHERE id_producto = ? AND stock_producto >= 1")){
                        psUpStock.setInt(1, idProdOriginal);
                        if (psUpStock.executeUpdate() == 0) {
                            con.rollback();
                            return false;
                        }
                    }
                    
                    // Obtener stock actual para Kardex
                    int stockReal = 0;
                    try(PreparedStatement psStock = con.prepareStatement("SELECT stock_producto FROM INVENTARIO WHERE id_producto = ?")){
                        psStock.setInt(1, idProdOriginal);
                        try(java.sql.ResultSet rs = psStock.executeQuery()){ if(rs.next()) stockReal = rs.getInt(1); }
                    }
                    
                    // Registrar la Salida en el Kardex
                    String sqlKardex = "INSERT INTO KARDEX (id_producto, id_usuario, fecha_movimiento_producto, tipo_movimiento_producto, cantidad_producto, stock_restante_producto, referencia_producto) VALUES (?, ?, GETDATE(), 'Salida', 1, ?, 'GARANTIA: CAMBIO NUEVO')";
                    try(PreparedStatement psK = con.prepareStatement(sqlKardex)){
                        psK.setInt(1, idProdOriginal);
                        psK.setInt(2, idUsuario);
                        psK.setInt(3, stockReal);
                        psK.executeUpdate();
                    }
                } else {
                    // Otros casos (ej. Sin Soluci\u00F3n)
                    if (reintegro) {
                        String sqlDefectuoso = "INSERT INTO INVENTARIO_DEFECTUOSO (id_producto, id_detalle_venta, fecha_ingreso, cantidad, motivo_danio, estado_defecto) VALUES (?, ?, GETDATE(), 1, ?, 'En Bodega')";
                        try (PreparedStatement psDef = con.prepareStatement(sqlDefectuoso)) {
                            psDef.setInt(1, idProdOriginal);
                            psDef.setInt(2, idDetalleVenta);
                            psDef.setString(3, observacion);
                            psDef.executeUpdate();
                        }
                    }
                }
            }
            
            con.commit();
            return true;
            
        } catch (Exception e) {
            if(con != null) try{con.rollback();}catch(Exception ex){}
            e.printStackTrace();
            return false;
        } finally {
            if(con != null) try{con.setAutoCommit(true); con.close();}catch(Exception ex){}
        }
    }

    public double obtenerPrecioOriginal(int idDetalleVenta) {
        String sql = "SELECT precio_unitario_venta FROM DETALLES_VENTA WHERE id_detalle_venta = ?";
        try (Connection con = factory.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idDetalleVenta);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("precio_unitario_venta");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0.0;
    }

    public boolean procesarCambioGarantia(int idDetalleDefectuoso, int idProductoSustituto, double precioSustituto, int idUsuario, String obs, String fotoBase64, String resolucion, boolean reintegro) {
        Connection con = null;
        try {
            con = factory.getConexion();
            con.setAutoCommit(false);
            
            // 1. Obtener datos originales
            String sqlOrig = "SELECT d.id_producto, d.precio_unitario_venta, v.id_cliente_venta, v.id_metodo_pago, d.descripcion_venta " +
                             "FROM DETALLES_VENTA d INNER JOIN VENTAS v ON d.id_ventas = v.id_ventas " +
                             "WHERE d.id_detalle_venta = ?";
            int idProdOriginal = 0, idCliente = 1, idMetodo = 1;
            double precioOriginal = 0;
            String descOriginal = "";
            
            try (PreparedStatement psOrig = con.prepareStatement(sqlOrig)) {
                psOrig.setInt(1, idDetalleDefectuoso);
                try(ResultSet rs = psOrig.executeQuery()){
                    if(rs.next()){
                        idProdOriginal = rs.getInt("id_producto");
                        precioOriginal = rs.getDouble("precio_unitario_venta");
                        idCliente = rs.getInt("id_cliente_venta");
                        idMetodo = rs.getInt("id_metodo_pago");
                        descOriginal = rs.getString("descripcion_venta");
                    }
                }
            }
            
            // 2. Venta Negativa (Devoluci\u00F3n)
            String sqlVentaNeg = "INSERT INTO VENTAS (fecha_venta, id_cliente_venta, id_usuario, id_metodo_pago, subtotal_venta, impuesto_venta, total_venta, referencia_pago) VALUES (GETDATE(), ?, ?, ?, ?, 0, ?, 'DEV. GARANTIA')";
            int idVentaNeg = 0;
            try (PreparedStatement psVN = con.prepareStatement(sqlVentaNeg, Statement.RETURN_GENERATED_KEYS)) {
                psVN.setInt(1, idCliente); psVN.setInt(2, idUsuario); psVN.setInt(3, idMetodo);
                psVN.setDouble(4, -precioOriginal); psVN.setDouble(5, -precioOriginal);
                psVN.executeUpdate();
                try(ResultSet rs = psVN.getGeneratedKeys()){ if(rs.next()) idVentaNeg = rs.getInt(1); }
            }
            
            // Detalle Negativo
            String sqlDetNeg = "INSERT INTO DETALLES_VENTA (id_ventas, id_producto, descripcion_venta, cantidad_venta, precio_unitario_venta, subtotal_venta, dias_garantia, estado_garantia) VALUES (?, ?, ?, -1, ?, ?, 0, 'ANULADA')";
            try(PreparedStatement psDN = con.prepareStatement(sqlDetNeg)){
                psDN.setInt(1, idVentaNeg); psDN.setInt(2, idProdOriginal); psDN.setString(3, "DEV: " + descOriginal);
                psDN.setDouble(4, precioOriginal); psDN.setDouble(5, -precioOriginal);
                psDN.executeUpdate();
            }
            
            // 3. Venta Positiva (Sustituto)
            String sqlVentaPos = "INSERT INTO VENTAS (fecha_venta, id_cliente_venta, id_usuario, id_metodo_pago, subtotal_venta, impuesto_venta, total_venta, referencia_pago) VALUES (GETDATE(), ?, ?, ?, ?, 0, ?, 'CAMBIO GARANTIA')";
            int idVentaPos = 0;
            try (PreparedStatement psVP = con.prepareStatement(sqlVentaPos, Statement.RETURN_GENERATED_KEYS)) {
                psVP.setInt(1, idCliente); psVP.setInt(2, idUsuario); psVP.setInt(3, idMetodo);
                psVP.setDouble(4, precioSustituto); psVP.setDouble(5, precioSustituto);
                psVP.executeUpdate();
                try(ResultSet rs = psVP.getGeneratedKeys()){ if(rs.next()) idVentaPos = rs.getInt(1); }
            }
            
            // Obtener desc sustituto
            String sqlSust = "SELECT nombre_producto, dias_garantia, requiere_serie FROM INVENTARIO WHERE id_producto = ?";
            String descSust = "Producto Sustituto";
            int diasSust = 0;
            try(PreparedStatement psS = con.prepareStatement(sqlSust)){
                psS.setInt(1, idProductoSustituto);
                try(ResultSet rs = psS.executeQuery()){
                    if(rs.next()){ descSust = rs.getString("nombre_producto"); diasSust = rs.getInt("dias_garantia"); }
                }
            }
            
            // Detalle Positivo
            String sqlDetPos = "INSERT INTO DETALLES_VENTA (id_ventas, id_producto, descripcion_venta, cantidad_venta, precio_unitario_venta, subtotal_venta, dias_garantia, estado_garantia) VALUES (?, ?, ?, 1, ?, ?, ?, 'VIGENTE')";
            try(PreparedStatement psDP = con.prepareStatement(sqlDetPos)){
                psDP.setInt(1, idVentaPos); psDP.setInt(2, idProductoSustituto); psDP.setString(3, descSust);
                psDP.setDouble(4, precioSustituto); psDP.setDouble(5, precioSustituto); psDP.setInt(6, diasSust);
                psDP.executeUpdate();
            }
            
            // 4. Kardex
            String sqlKardex = "INSERT INTO KARDEX (id_producto, id_usuario, fecha_movimiento_producto, tipo_movimiento_producto, cantidad_producto, stock_restante_producto, referencia_producto) VALUES (?, ?, GETDATE(), ?, 1, ?, ?)";
            
            // Obtener stock real del producto defectuoso (que se queda igual porque entra a bodega, no al piso de venta)
            int stockRealOrig = 0;
            try(PreparedStatement psGetStockOrig = con.prepareStatement("SELECT stock_producto FROM INVENTARIO WHERE id_producto = ?")){
                psGetStockOrig.setInt(1, idProdOriginal);
                try(ResultSet rs = psGetStockOrig.executeQuery()){ if(rs.next()) stockRealOrig = rs.getInt(1); }
            }
            
            try(PreparedStatement psK1 = con.prepareStatement(sqlKardex)){
                psK1.setInt(1, idProdOriginal); psK1.setInt(2, idUsuario); psK1.setString(3, "Entrada");
                psK1.setInt(4, stockRealOrig); psK1.setString(5, "DEFECTUOSO VTA #" + idVentaNeg);
                psK1.executeUpdate();
            }
            
            String sqlStockSust = "UPDATE INVENTARIO SET stock_producto = stock_producto - 1 WHERE id_producto = ? AND stock_producto >= 1";
            try(PreparedStatement psUpStock = con.prepareStatement(sqlStockSust)){
                psUpStock.setInt(1, idProductoSustituto); 
                if (psUpStock.executeUpdate() == 0) {
                    con.rollback();
                    return false;
                }
            }
            
            // Obtener stock actualizado del sustituto
            int stockRealSust = 0;
            try(PreparedStatement psGetStockSust = con.prepareStatement("SELECT stock_producto FROM INVENTARIO WHERE id_producto = ?")){
                psGetStockSust.setInt(1, idProductoSustituto);
                try(ResultSet rs = psGetStockSust.executeQuery()){ if(rs.next()) stockRealSust = rs.getInt(1); }
            }
            
            try(PreparedStatement psK2 = con.prepareStatement(sqlKardex)){
                psK2.setInt(1, idProductoSustituto); psK2.setInt(2, idUsuario); psK2.setString(3, "Salida");
                psK2.setInt(4, stockRealSust); psK2.setString(5, "ENTREGADO VTA #" + idVentaPos);
                psK2.executeUpdate();
            }
            
            // 5. Marcar Detalle Original como RECLAMADA y agregar evidencia
            String sqlReclamo = "UPDATE DETALLES_VENTA SET estado_garantia = 'RECLAMADA', observacion_garantia = ?, foto_garantia = ?, resolucion_garantia = ? WHERE id_detalle_venta = ?";
            try(PreparedStatement psR = con.prepareStatement(sqlReclamo)){
                psR.setString(1, obs);
                psR.setString(2, fotoBase64);
                psR.setString(3, resolucion);
                psR.setInt(4, idDetalleDefectuoso);
                psR.executeUpdate();
            }
            
            if (reintegro) {
                String sqlDefectuoso = "INSERT INTO INVENTARIO_DEFECTUOSO (id_producto, id_detalle_venta, fecha_ingreso, cantidad, motivo_danio) VALUES (?, ?, GETDATE(), 1, ?)";
                try (PreparedStatement psDef = con.prepareStatement(sqlDefectuoso)) {
                    psDef.setInt(1, idProdOriginal);
                    psDef.setInt(2, idDetalleDefectuoso);
                    psDef.setString(3, obs);
                    psDef.executeUpdate();
                }
            }
            
            con.commit();
            return true;
            
        } catch(Exception e) {
            if(con != null) try{con.rollback();}catch(Exception ex){}
            e.printStackTrace();
            return false;
        } finally {
            if(con != null) try{con.setAutoCommit(true); con.close();}catch(Exception ex){}
        }
    }
}
