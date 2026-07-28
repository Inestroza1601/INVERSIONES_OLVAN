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
        String sql = "SELECT d.id_producto, i.nombre_producto, i.codigo_barras_producto, d.estado_defecto, SUM(d.cantidad) as cantidad_total, "
                   + "(SELECT TOP 1 v.foto_garantia FROM INVENTARIO_DEFECTUOSO d2 INNER JOIN DETALLES_VENTA v ON d2.id_detalle_venta = v.id_detalle_venta WHERE d2.id_producto = d.id_producto AND d2.estado_defecto = d.estado_defecto ORDER BY d2.fecha_ingreso DESC) as foto "
                   + "FROM INVENTARIO_DEFECTUOSO d "
                   + "INNER JOIN INVENTARIO i ON d.id_producto = i.id_producto "
                   + "GROUP BY d.id_producto, i.nombre_producto, i.codigo_barras_producto, d.estado_defecto "
                   + "ORDER BY i.nombre_producto, d.estado_defecto";
        
        try (Connection con = factory.getConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
             
            while (rs.next()) {
                Map<String, Object> fila = new HashMap<>();
                fila.put("id_producto", rs.getInt("id_producto"));
                fila.put("codigo_barras", rs.getString("codigo_barras_producto"));
                fila.put("nombre_producto", rs.getString("nombre_producto"));
                fila.put("estado_defecto", rs.getString("estado_defecto"));
                fila.put("cantidad", rs.getInt("cantidad_total"));
                fila.put("foto", rs.getString("foto"));
                lista.add(fila);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return lista;
    }

    public List<Map<String, Object>> obtenerDetallesPorProductoYEstado(int idProducto, String estadoDefecto) {
        List<Map<String, Object>> lista = new ArrayList<>();
        String sql = "SELECT d.id_inventarioDefectuoso, d.fecha_ingreso, d.motivo_danio, v.observacion_garantia, v.resolucion_garantia, v.foto_garantia, "
                   + "d.fecha_envio_proveedor, d.fecha_recibido_proveedor, d.fecha_entregado_cliente, c.nombre_cliente "
                   + "FROM INVENTARIO_DEFECTUOSO d "
                   + "LEFT JOIN DETALLES_VENTA v ON d.id_detalle_venta = v.id_detalle_venta "
                   + "LEFT JOIN VENTAS ve ON v.id_ventas = ve.id_ventas "
                   + "LEFT JOIN CLIENTES c ON ve.id_cliente_venta = c.id_cliente "
                   + "WHERE d.id_producto = ? AND d.estado_defecto = ? "
                   + "ORDER BY d.fecha_ingreso DESC";
        
        try (Connection con = factory.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
             
            ps.setInt(1, idProducto);
            ps.setString(2, estadoDefecto);
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> fila = new HashMap<>();
                    fila.put("id", rs.getInt("id_inventarioDefectuoso"));
                    fila.put("fecha", rs.getTimestamp("fecha_ingreso"));
                    
                    String motivo = rs.getString("motivo_danio");
                    if (motivo == null || motivo.trim().isEmpty()) {
                        motivo = rs.getString("observacion_garantia");
                    }
                    fila.put("motivo", motivo != null ? motivo : "Sin observación");
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

    public boolean cambiarEstadoMermas(int idProducto, String estadoActual, String nuevoEstado, int idUsuario, String kardexRef) {
        String sqlUpdate = "UPDATE INVENTARIO_DEFECTUOSO SET estado_defecto = ?";
        if (nuevoEstado.startsWith("Enviado a Proveedor")) {
            sqlUpdate += ", fecha_envio_proveedor = GETDATE()";
        } else if (nuevoEstado.startsWith("Recibido de Proveedor")) {
            sqlUpdate += ", fecha_recibido_proveedor = GETDATE()";
        }
        sqlUpdate += " WHERE id_producto = ? AND estado_defecto = ?";
        String sqlKardex = "INSERT INTO KARDEX (id_producto, id_usuario, fecha_movimiento_producto, tipo_movimiento_producto, cantidad_producto, stock_restante_producto, referencia_producto) VALUES (?, ?, GETDATE(), 'Salida', ?, ?, ?)";
        
        Connection con = null;
        try {
            con = factory.getConexion();
            con.setAutoCommit(false);
            
            // Cuantos se van a cambiar
            int cantidadAfectada = 0;
            try(PreparedStatement psC = con.prepareStatement("SELECT SUM(cantidad) FROM INVENTARIO_DEFECTUOSO WHERE id_producto = ? AND estado_defecto = ?")){
                psC.setInt(1, idProducto);
                psC.setString(2, estadoActual);
                try(ResultSet rs = psC.executeQuery()){ if(rs.next()) cantidadAfectada = rs.getInt(1); }
            }
            
            if (cantidadAfectada > 0) {
                // Actualizar Estado
                try(PreparedStatement psU = con.prepareStatement(sqlUpdate)){
                    psU.setString(1, nuevoEstado);
                    psU.setInt(2, idProducto);
                    psU.setString(3, estadoActual);
                    psU.executeUpdate();
                }
                
                // Obtener stock normal para el Kardex
                int stockReal = 0;
                try(PreparedStatement psS = con.prepareStatement("SELECT stock_producto FROM INVENTARIO WHERE id_producto = ?")){
                    psS.setInt(1, idProducto);
                    try(ResultSet rs = psS.executeQuery()){ if(rs.next()) stockReal = rs.getInt(1); }
                }
                
                // Kardex Log
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

    public boolean reingresarInventario(int idProducto, String estadoActual, int idUsuario) {
        String sqlDelete = "DELETE FROM INVENTARIO_DEFECTUOSO WHERE id_producto = ? AND estado_defecto = ?";
        String sqlUpdateInv = "UPDATE INVENTARIO SET stock_producto = stock_producto + ? WHERE id_producto = ?";
        String sqlKardex = "INSERT INTO KARDEX (id_producto, id_usuario, fecha_movimiento_producto, tipo_movimiento_producto, cantidad_producto, stock_restante_producto, referencia_producto) VALUES (?, ?, GETDATE(), 'Entrada', ?, ?, ?)";
        
        Connection con = null;
        try {
            con = factory.getConexion();
            con.setAutoCommit(false);
            
            // Cuantos se van a reingresar
            int cantidadAfectada = 0;
            try(PreparedStatement psC = con.prepareStatement("SELECT SUM(cantidad) FROM INVENTARIO_DEFECTUOSO WHERE id_producto = ? AND estado_defecto = ?")){
                psC.setInt(1, idProducto);
                psC.setString(2, estadoActual);
                try(ResultSet rs = psC.executeQuery()){ if(rs.next()) cantidadAfectada = rs.getInt(1); }
            }
            
            if (cantidadAfectada > 0) {
                // Sumar al inventario normal
                try(PreparedStatement psU = con.prepareStatement(sqlUpdateInv)){
                    psU.setInt(1, cantidadAfectada);
                    psU.setInt(2, idProducto);
                    psU.executeUpdate();
                }
                
                // Borrar de defectuosos
                try(PreparedStatement psD = con.prepareStatement(sqlDelete)){
                    psD.setInt(1, idProducto);
                    psD.setString(2, estadoActual);
                    psD.executeUpdate();
                }
                
                // Obtener stock normal para el Kardex
                int stockReal = 0;
                try(PreparedStatement psS = con.prepareStatement("SELECT stock_producto FROM INVENTARIO WHERE id_producto = ?")){
                    psS.setInt(1, idProducto);
                    try(ResultSet rs = psS.executeQuery()){ if(rs.next()) stockReal = rs.getInt(1); }
                }
                
                // Kardex Log
                try(PreparedStatement psK = con.prepareStatement(sqlKardex)){
                    psK.setInt(1, idProducto);
                    psK.setInt(2, idUsuario);
                    psK.setInt(3, cantidadAfectada);
                    psK.setInt(4, stockReal);
                    psK.setString(5, "RETORNO DE PROVEEDOR (MERMA)");
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

    public boolean entregarCliente(int idProducto, String estadoActual) {
        String sqlDelete = "DELETE FROM INVENTARIO_DEFECTUOSO WHERE id_producto = ? AND estado_defecto = ?";
        
        Connection con = null;
        try {
            con = factory.getConexion();
            con.setAutoCommit(false);
            
            try(PreparedStatement psD = con.prepareStatement(sqlDelete)){
                psD.setInt(1, idProducto);
                psD.setString(2, estadoActual);
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
}
