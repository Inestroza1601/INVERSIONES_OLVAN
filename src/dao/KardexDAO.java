package dao;

import factory.ConexionFactory;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class KardexDAO {
    private ConexionFactory factory;

    public KardexDAO() {
        this.factory = new ConexionFactory();
    }

    // ==========================================
    // 1. LÓGICA DE FIRMA Y SEGURIDAD (SHA-256)
    // ==========================================
    
    private String encriptarSHA256(String password) {
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException("Error al encriptar contraseña.", e);
        }
    }

    /**
     * Valida la firma SOLO con la contraseña. 
     * Encripta lo que el usuario escribe y busca ese Hash en la BD.
     */
    public int validarFirmaUsuario(String passwordPlana) {
        String hashIngresado = encriptarSHA256(passwordPlana);
        String sql = "SELECT id_usuario FROM USUARIOS WHERE password_hash = ? AND estado_usuario = 1";
        
        try (Connection con = factory.getConexion(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, hashIngresado);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id_usuario");
                }
            }
        } catch (SQLException e) { System.err.println("Error validando firma: " + e.getMessage()); }
        return -1;
    }

    // ==========================================
    // 2. MOVIMIENTOS Y CONSULTAS
    // ==========================================

    public boolean registrarMovimiento(int idProducto, String tipoMovimiento, int cantidad, String observacion, int idUsuario) {
        // Consultas ajustadas estrictamente a tu imagen
        String sqlStockActual = "SELECT stock_producto FROM INVENTARIO WHERE id_producto = ?";
        String sqlKardex = "INSERT INTO KARDEX (id_producto, fecha_movimiento_producto, tipo_movimiento_producto, cantidad_producto, stock_restante_producto, referencia_producto, id_usuario) VALUES (?, GETDATE(), ?, ?, ?, ?, ?)";
        String sqlInventario = "UPDATE INVENTARIO SET stock_producto = ? WHERE id_producto = ?";

        Connection con = null;
        try {
            con = factory.getConexion();
            con.setAutoCommit(false); 

            // 1. Obtener el stock actual para calcular el restante
            int stockActual = 0;
            try (PreparedStatement psStock = con.prepareStatement(sqlStockActual)) {
                psStock.setInt(1, idProducto);
                try (ResultSet rs = psStock.executeQuery()) {
                    if (rs.next()) {
                        stockActual = rs.getInt("stock_producto");
                    }
                }
            }

            int nuevoStock = tipoMovimiento.equals("Salida") ? (stockActual - cantidad) : (stockActual + cantidad);

            // 2. Guardar en KARDEX
            try (PreparedStatement psK = con.prepareStatement(sqlKardex)) {
                psK.setInt(1, idProducto);
                psK.setString(2, tipoMovimiento);
                psK.setInt(3, cantidad);
                psK.setInt(4, nuevoStock); // Insertamos el stock restante calculado
                psK.setString(5, observacion); // Que equivale a tu referencia_producto
                psK.setInt(6, idUsuario);
                psK.executeUpdate();
            }

            // 3. Actualizar INVENTARIO con el nuevo stock
            try (PreparedStatement psI = con.prepareStatement(sqlInventario)) {
                psI.setInt(1, nuevoStock);
                psI.setInt(2, idProducto);
                psI.executeUpdate();
            }

            con.commit(); 
            return true;

        } catch (SQLException e) {
            if (con != null) try { con.rollback(); } catch (SQLException ex) { }
            System.err.println("Error en transacción de Kardex: " + e.getMessage());
            return false;
        } finally {
            if (con != null) try { con.setAutoCommit(true); con.close(); } catch (SQLException e) { }
        }
    }

    public java.util.List<Object[]> obtenerHistorialKardex(int idProducto) {
        java.util.List<Object[]> historial = new java.util.ArrayList<>();
        
        String sql = "SELECT k.fecha_movimiento_producto, k.tipo_movimiento_producto, k.cantidad_producto, k.stock_restante_producto, k.referencia_producto, u.nombre_usuario " +
                     "FROM KARDEX k INNER JOIN USUARIOS u ON k.id_usuario = u.id_usuario " +
                     "WHERE k.id_producto = ? ORDER BY k.fecha_movimiento_producto DESC";
                     
        try (Connection con = factory.getConexion(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idProducto);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    // Ahora el arreglo manda exactamente los 6 datos en el orden correcto
                    historial.add(new Object[]{
                        rs.getTimestamp("fecha_movimiento_producto"),
                        rs.getString("tipo_movimiento_producto"),
                        rs.getInt("cantidad_producto"),
                        rs.getInt("stock_restante_producto"), // <-- EL DATO NUEVO
                        rs.getString("referencia_producto"), 
                        rs.getString("nombre_usuario")
                    });
                }
            }
        } catch (SQLException e) { System.err.println("Error obteniendo historial: " + e.getMessage()); }
        return historial;
    }
}