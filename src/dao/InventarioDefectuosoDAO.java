package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InventarioDefectuosoDAO {

    private factory.ConexionFactory factory;

    public InventarioDefectuosoDAO() {
        factory = new factory.ConexionFactory();
    }

    public List<Map<String, Object>> obtenerInventarioDefectuosoAgrupado() {
        List<Map<String, Object>> lista = new ArrayList<>();
        String sql = "SELECT MIN(d.id_inventarioDefectuoso) as id_inventarioDefectuoso, d.id_producto, i.nombre_producto, i.codigo_barras_producto, d.estado_defecto, SUM(d.cantidad) as cantidad_total, "
                   + "c.nombre_cliente, c.identidad_cliente, MAX(ISNULL(dv.foto_garantia, d.foto)) as foto "
                   + "FROM INVENTARIO_DEFECTUOSO d "
                   + "INNER JOIN INVENTARIO i ON d.id_producto = i.id_producto "
                   + "LEFT JOIN DETALLES_VENTA dv ON d.id_detalle_venta = dv.id_detalle_venta "
                   + "LEFT JOIN VENTAS ve ON dv.id_ventas = ve.id_ventas "
                   + "LEFT JOIN CLIENTES c ON ve.id_cliente_venta = c.id_cliente "
                   + "GROUP BY d.id_producto, i.nombre_producto, i.codigo_barras_producto, d.estado_defecto, c.nombre_cliente, c.identidad_cliente, CASE WHEN c.nombre_cliente IS NULL THEN 0 ELSE d.id_inventarioDefectuoso END "
                   + "ORDER BY i.nombre_producto, d.estado_defecto";
        
        try (Connection con = factory.getConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
             
            while (rs.next()) {
                Map<String, Object> fila = new HashMap<>();
                int idDefectuoso = rs.getInt("id_inventarioDefectuoso");
                String nombreCliente = rs.getString("nombre_cliente");
                
                fila.put("id_inventarioDefectuoso", idDefectuoso);
                fila.put("id_producto", rs.getInt("id_producto"));
                
                if (nombreCliente == null) {
                    fila.put("codigo_barras", "INVD-LOTE");
                } else {
                    fila.put("codigo_barras", "INVD-" + String.format("%04d", idDefectuoso));
                }
                
                fila.put("nombre_producto", rs.getString("nombre_producto"));
                fila.put("estado_defecto", rs.getString("estado_defecto"));
                fila.put("cantidad", rs.getInt("cantidad_total"));
                fila.put("foto", rs.getString("foto"));
                
                fila.put("cliente", nombreCliente);
                fila.put("identidad", rs.getString("identidad_cliente") != null ? rs.getString("identidad_cliente") : "");
                
                lista.add(fila);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return lista;
    }

    public List<Map<String, Object>> obtenerDetallesPorProductoYEstado(int idProducto, String estadoDefecto, String nombreCliente, int idDefectuoso) {
        List<Map<String, Object>> lista = new ArrayList<>();
        String sql;
        if ("INVERSIONES OLVAN (Empresa)".equals(nombreCliente) || nombreCliente == null || "Desconocido".equals(nombreCliente)) {
            sql = "SELECT d.id_inventarioDefectuoso, d.fecha_ingreso, d.motivo_danio, v.observacion_garantia, v.resolucion_garantia, ISNULL(v.foto_garantia, d.foto) as foto_garantia, "
                + "d.fecha_envio_proveedor, d.fecha_recibido_proveedor, d.fecha_entregado_cliente, c.nombre_cliente "
                + "FROM INVENTARIO_DEFECTUOSO d "
                + "LEFT JOIN DETALLES_VENTA v ON d.id_detalle_venta = v.id_detalle_venta "
                + "LEFT JOIN VENTAS ve ON v.id_ventas = ve.id_ventas "
                + "LEFT JOIN CLIENTES c ON ve.id_cliente_venta = c.id_cliente "
                + "WHERE d.id_producto = ? AND d.estado_defecto = ? AND d.id_detalle_venta IS NULL "
                + "ORDER BY d.fecha_ingreso DESC";
        } else {
            sql = "SELECT d.id_inventarioDefectuoso, d.fecha_ingreso, d.motivo_danio, v.observacion_garantia, v.resolucion_garantia, ISNULL(v.foto_garantia, d.foto) as foto_garantia, "
                + "d.fecha_envio_proveedor, d.fecha_recibido_proveedor, d.fecha_entregado_cliente, c.nombre_cliente "
                + "FROM INVENTARIO_DEFECTUOSO d "
                + "LEFT JOIN DETALLES_VENTA v ON d.id_detalle_venta = v.id_detalle_venta "
                + "LEFT JOIN VENTAS ve ON v.id_ventas = ve.id_ventas "
                + "LEFT JOIN CLIENTES c ON ve.id_cliente_venta = c.id_cliente "
                + "WHERE d.id_inventarioDefectuoso = ?";
        }
        
        try (Connection con = factory.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
             
            if ("INVERSIONES OLVAN (Empresa)".equals(nombreCliente) || nombreCliente == null || "Desconocido".equals(nombreCliente)) {
                ps.setInt(1, idProducto);
                ps.setString(2, estadoDefecto);
            } else {
                ps.setInt(1, idDefectuoso);
            }
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> fila = new HashMap<>();
                    fila.put("id", rs.getInt("id_inventarioDefectuoso"));
                    fila.put("fecha", rs.getTimestamp("fecha_ingreso"));
                    
                    String motivo = rs.getString("motivo_danio");
                    if (motivo == null || motivo.trim().isEmpty()) {
                        motivo = rs.getString("observacion_garantia");
                    }
                    fila.put("motivo", motivo != null ? motivo : "Sin observaci\u00F3n");
                    fila.put("resolucion", rs.getString("resolucion_garantia"));
                    fila.put("foto", rs.getString("foto_garantia"));
                    fila.put("fecha_envio", rs.getTimestamp("fecha_envio_proveedor"));
                    fila.put("fecha_recibido", rs.getTimestamp("fecha_recibido_proveedor"));
                    fila.put("fecha_entregado", rs.getTimestamp("fecha_entregado_cliente"));
                    fila.put("cliente", rs.getString("nombre_cliente"));
                    lista.add(fila);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return lista;
    }

    public boolean cambiarEstadoDefectuoso(int idProducto, String estadoActual, String nuevoEstado, int idUsuario, String kardexRef, String nombreCliente, int idDefectuoso) {
        String sqlUpdate;
        String sqlCount;
        boolean isEmpresa = ("INVERSIONES OLVAN (Empresa)".equals(nombreCliente) || nombreCliente == null || "Desconocido".equals(nombreCliente));
        
        if (isEmpresa) {
            sqlUpdate = "UPDATE INVENTARIO_DEFECTUOSO SET estado_defecto = ?";
            if (nuevoEstado.startsWith("Enviado a Proveedor")) sqlUpdate += ", fecha_envio_proveedor = GETDATE()";
            else if (nuevoEstado.startsWith("Recibido de Proveedor")) sqlUpdate += ", fecha_recibido_proveedor = GETDATE()";
            sqlUpdate += " WHERE id_producto = ? AND estado_defecto = ? AND id_detalle_venta IS NULL";
            
            sqlCount = "SELECT SUM(cantidad) FROM INVENTARIO_DEFECTUOSO WHERE id_producto = ? AND estado_defecto = ? AND id_detalle_venta IS NULL";
        } else {
            sqlUpdate = "UPDATE INVENTARIO_DEFECTUOSO SET estado_defecto = ?";
            if (nuevoEstado.startsWith("Enviado a Proveedor")) sqlUpdate += ", fecha_envio_proveedor = GETDATE()";
            else if (nuevoEstado.startsWith("Recibido de Proveedor")) sqlUpdate += ", fecha_recibido_proveedor = GETDATE()";
            sqlUpdate += " WHERE id_inventarioDefectuoso = ?";
            
            sqlCount = "SELECT cantidad FROM INVENTARIO_DEFECTUOSO WHERE id_inventarioDefectuoso = ?";
        }
        
        String sqlKardex = "INSERT INTO KARDEX (id_producto, id_usuario, fecha_movimiento_producto, tipo_movimiento_producto, cantidad_producto, stock_restante_producto, referencia_producto) VALUES (?, ?, GETDATE(), 'Salida', ?, ?, ?)";
        
        Connection con = null;
        try {
            con = factory.getConexion();
            con.setAutoCommit(false);
            
            int cantidadAfectada = 0;
            try(PreparedStatement psC = con.prepareStatement(sqlCount)){
                if (isEmpresa) {
                    psC.setInt(1, idProducto);
                    psC.setString(2, estadoActual);
                } else {
                    psC.setInt(1, idDefectuoso);
                }
                try(ResultSet rs = psC.executeQuery()){ if(rs.next()) cantidadAfectada = rs.getInt(1); }
            }
            
            if (cantidadAfectada > 0) {
                try(PreparedStatement psU = con.prepareStatement(sqlUpdate)){
                    psU.setString(1, nuevoEstado);
                    if (isEmpresa) {
                        psU.setInt(2, idProducto);
                        psU.setString(3, estadoActual);
                    } else {
                        psU.setInt(2, idDefectuoso);
                    }
                    psU.executeUpdate();
                }
                
                int stockReal = 0;
                try(PreparedStatement psS = con.prepareStatement("SELECT stock_producto FROM INVENTARIO WHERE id_producto = ?")){
                    psS.setInt(1, idProducto);
                    try(ResultSet rs = psS.executeQuery()){ if(rs.next()) stockReal = rs.getInt(1); }
                }
                
                try(PreparedStatement psK = con.prepareStatement(sqlKardex)){
                    psK.setInt(1, idProducto);
                    psK.setInt(2, idUsuario);
                    psK.setInt(3, cantidadAfectada);
                    psK.setInt(4, stockReal);
                    psK.setString(5, kardexRef);
                    psK.executeUpdate();
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

    public boolean reingresarInventario(int idProducto, String estadoActual, int idUsuario, String observacion, String nombreCliente, int idDefectuoso) throws java.sql.SQLException {
        String sqlDelete;
        String sqlCount;
        boolean isEmpresa = ("INVERSIONES OLVAN (Empresa)".equals(nombreCliente) || nombreCliente == null || "Desconocido".equals(nombreCliente));
        
        if (isEmpresa) {
            sqlDelete = "DELETE FROM INVENTARIO_DEFECTUOSO WHERE id_producto = ? AND estado_defecto = ? AND id_detalle_venta IS NULL";
            sqlCount = "SELECT SUM(cantidad) FROM INVENTARIO_DEFECTUOSO WHERE id_producto = ? AND estado_defecto = ? AND id_detalle_venta IS NULL";
        } else {
            sqlDelete = "DELETE FROM INVENTARIO_DEFECTUOSO WHERE id_inventarioDefectuoso = ?";
            sqlCount = "SELECT cantidad FROM INVENTARIO_DEFECTUOSO WHERE id_inventarioDefectuoso = ?";
        }
        
        String sqlUpdateInv = "UPDATE INVENTARIO SET stock_producto = stock_producto + ? WHERE id_producto = ?";
        String sqlKardex = "INSERT INTO KARDEX (id_producto, id_usuario, fecha_movimiento_producto, tipo_movimiento_producto, cantidad_producto, stock_restante_producto, referencia_producto) VALUES (?, ?, GETDATE(), 'Entrada', ?, ?, ?)";
        
        Connection con = null;
        try {
            con = factory.getConexion();
            con.setAutoCommit(false);
            
            int cantidadAfectada = 0;
            try(PreparedStatement psC = con.prepareStatement(sqlCount)){
                if (isEmpresa) {
                    psC.setInt(1, idProducto);
                    psC.setString(2, estadoActual);
                } else {
                    psC.setInt(1, idDefectuoso);
                }
                try(ResultSet rs = psC.executeQuery()){ if(rs.next()) cantidadAfectada = rs.getInt(1); }
            }
            
            if (cantidadAfectada > 0) {
                try(PreparedStatement psU = con.prepareStatement(sqlUpdateInv)){
                    psU.setInt(1, cantidadAfectada);
                    psU.setInt(2, idProducto);
                    psU.executeUpdate();
                }
                
                try(PreparedStatement psD = con.prepareStatement(sqlDelete)){
                    if (isEmpresa) {
                        psD.setInt(1, idProducto);
                        psD.setString(2, estadoActual);
                    } else {
                        psD.setInt(1, idDefectuoso);
                    }
                    psD.executeUpdate();
                }
                
                int stockReal = 0;
                try(PreparedStatement psS = con.prepareStatement("SELECT stock_producto FROM INVENTARIO WHERE id_producto = ?")){
                    psS.setInt(1, idProducto);
                    try(ResultSet rs = psS.executeQuery()){ if(rs.next()) stockReal = rs.getInt(1); }
                }
                
                try(PreparedStatement psK = con.prepareStatement(sqlKardex)){
                    psK.setInt(1, idProducto);
                    psK.setInt(2, idUsuario);
                    psK.setInt(3, cantidadAfectada);
                    psK.setInt(4, stockReal);
                    psK.setString(5, "RETORNO DE DEFECTUOSO: " + observacion);
                    psK.executeUpdate();
                }
            }
            
            con.commit();
            return true;
        } catch(java.sql.SQLException e) {
            if(con != null) try{con.rollback();}catch(Exception ex){}
            throw e;
        } finally {
            if(con != null) try{con.setAutoCommit(true); con.close();}catch(Exception ex){}
        }
    }

    public boolean entregarCliente(int idProducto, String estadoActual, String nombreCliente, int idDefectuoso) {
        String sqlDelete;
        boolean isEmpresa = ("INVERSIONES OLVAN (Empresa)".equals(nombreCliente) || nombreCliente == null || "Desconocido".equals(nombreCliente));
        
        if (isEmpresa) {
            sqlDelete = "DELETE FROM INVENTARIO_DEFECTUOSO WHERE id_producto = ? AND estado_defecto = ? AND id_detalle_venta IS NULL";
        } else {
            sqlDelete = "DELETE FROM INVENTARIO_DEFECTUOSO WHERE id_inventarioDefectuoso = ?";
        }
        
        Connection con = null;
        try {
            con = factory.getConexion();
            con.setAutoCommit(false);
            
            try(PreparedStatement psD = con.prepareStatement(sqlDelete)){
                if (isEmpresa) {
                    psD.setInt(1, idProducto);
                    psD.setString(2, estadoActual);
                } else {
                    psD.setInt(1, idDefectuoso);
                }
                psD.executeUpdate();
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

    public int reportarDefectuosoAlmacen(int idProducto, int cantidad, String motivo, int idUsuario, String fotoBase64) throws java.sql.SQLException {
        String sqlCheckStock = "SELECT stock_producto FROM INVENTARIO WHERE id_producto = ?";
        String sqlUpdateInv = "UPDATE INVENTARIO SET stock_producto = stock_producto - ? WHERE id_producto = ?";
        String sqlInsertKardex = "INSERT INTO KARDEX (id_producto, id_usuario, fecha_movimiento_producto, tipo_movimiento_producto, cantidad_producto, stock_restante_producto, referencia_producto) VALUES (?, ?, GETDATE(), 'Salida', ?, ?, ?)";
        String sqlInsertDefectuoso = "INSERT INTO INVENTARIO_DEFECTUOSO (id_producto, fecha_ingreso, cantidad, motivo_danio, estado_defecto, foto) VALUES (?, GETDATE(), ?, ?, 'En Bodega (Falla de F\u00E1brica)', ?)";
        
        Connection con = null;
        try {
            con = factory.getConexion();
            con.setAutoCommit(false);
            
            int stockActual = 0;
            try(PreparedStatement psStock = con.prepareStatement(sqlCheckStock)){
                psStock.setInt(1, idProducto);
                try(ResultSet rs = psStock.executeQuery()){
                    if(rs.next()) {
                        stockActual = rs.getInt(1);
                    } else {
                        throw new java.sql.SQLException("Producto no encontrado en inventario.");
                    }
                }
            }
            
            if (stockActual < cantidad) {
                throw new java.sql.SQLException("No hay suficiente stock para reportar esta cantidad como defectuosa.");
            }
            
            int nuevoStock = stockActual - cantidad;
            
            try(PreparedStatement psU = con.prepareStatement(sqlUpdateInv)){
                psU.setInt(1, cantidad);
                psU.setInt(2, idProducto);
                psU.executeUpdate();
            }
            
            try(PreparedStatement psK = con.prepareStatement(sqlInsertKardex)){
                psK.setInt(1, idProducto);
                psK.setInt(2, idUsuario);
                psK.setInt(3, cantidad);
                psK.setInt(4, nuevoStock);
                psK.setString(5, "TRASLADO A DEFECTUOSO: " + motivo);
                psK.executeUpdate();
            }
            
            int idGenerado = -1;
            try(PreparedStatement psD = con.prepareStatement(sqlInsertDefectuoso, java.sql.Statement.RETURN_GENERATED_KEYS)){
                psD.setInt(1, idProducto);
                psD.setInt(2, cantidad);
                psD.setString(3, motivo);
                psD.setString(4, fotoBase64);
                psD.executeUpdate();
                try (ResultSet rsKeys = psD.getGeneratedKeys()) {
                    if (rsKeys.next()) {
                        idGenerado = rsKeys.getInt(1);
                    }
                }
            }
            
            con.commit();
            return idGenerado;
        } catch(java.sql.SQLException e) {
            if(con != null) try{con.rollback();}catch(Exception ex){}
            throw e;
        } finally {
            if(con != null) try{con.setAutoCommit(true); con.close();}catch(Exception ex){}
        }
    }
}
