package dao;

import factory.ConexionFactory;
import modelo.Producto;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Set;

public class InventarioDAO {

    private ConexionFactory factory;

    public InventarioDAO() {
        this.factory = new ConexionFactory();
    }

    /**
     * Carga todos los códigos de barras activos a la RAM para la validación instantánea del panel.
     */
    public Set<String> obtenerCodigosEnRam() {
        Set<String> codigos = new HashSet<>();
        String sql = "SELECT codigo_barras_producto FROM INVENTARIO WHERE eliminado_producto = 0 AND codigo_barras_producto IS NOT NULL";
        
        try (Connection con = factory.getConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
             
            while (rs.next()) {
                codigos.add(rs.getString("codigo_barras_producto"));
            }
            
        } catch (SQLException e) {
            System.err.println("Error al cargar códigos de barras a RAM: " + e.getMessage());
        }
        return codigos;
    }

    /**
     * Registra un nuevo producto. Si el código de barras viene vacío, utiliza el ID autogenerado.
     */
    public boolean registrarProducto(Producto p) {
        String sql = "INSERT INTO INVENTARIO (codigo_barras_producto, nombre_producto, id_categoria, id_proveedor, "
                   + "id_ubicacion, precio_compra_producto, precio_venta_producto, precio_mayorista_producto, "
                   + "stock_minimo_producto, stock_producto, ruta_imagen_producto, dias_garantia, requiere_serie, eliminado_producto) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 0)";

        try (Connection con = factory.getConexion();
             // Le decimos a Java que recupere el ID que SQL Server generará automáticamente
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            // 1. Validar si trae código de barras o viene nulo
            if (p.getCodigoBarras() == null || p.getCodigoBarras().trim().isEmpty()) {
                ps.setNull(1, java.sql.Types.VARCHAR);
            } else {
                ps.setString(1, p.getCodigoBarras().trim());
            }

            ps.setString(2, p.getNombreProducto().trim());
            ps.setInt(3, p.getIdCategoria());
            ps.setInt(4, p.getIdProveedor());
            ps.setInt(5, p.getIdUbicacion());
            ps.setDouble(6, p.getPrecioCompra());
            ps.setDouble(7, p.getPrecioVenta());

            // 8. Precio mayorista (Si es mayor a 0 lo guarda, si no, lo deja como nulo en la BD)
            if (p.getPrecioMayorista() > 0) {
                ps.setDouble(8, p.getPrecioMayorista());
            } else {
                ps.setNull(8, java.sql.Types.DECIMAL);
            }

            ps.setInt(9, p.getStockMinimo());
            ps.setInt(10, p.getStockProducto());

            // 11. Ruta de imagen
            if (p.getRutaImagen() == null || p.getRutaImagen().trim().isEmpty()) {
                ps.setNull(11, java.sql.Types.VARCHAR);
            } else {
                ps.setString(11, p.getRutaImagen());
            }
            
            ps.setInt(12, p.getDiasGarantia());
            ps.setBoolean(13, p.isRequiereSerie());

            int filasAfectadas = ps.executeUpdate();

            // Si se guardó correctamente, revisamos si necesitamos actualizar el código de barras
            if (filasAfectadas > 0) {
                if (p.getCodigoBarras() == null || p.getCodigoBarras().trim().isEmpty()) {
                    
                    // Recuperamos el ID autogenerado
                    try (ResultSet rs = ps.getGeneratedKeys()) {
                        if (rs.next()) {
                            int idGenerado = rs.getInt(1); // Este es el id_producto
                            
                            // Actualizamos el producto recién creado para que su código de barras sea su ID
                            String sqlUpdate = "UPDATE INVENTARIO SET codigo_barras_producto = ? WHERE id_producto = ?";
                            try (PreparedStatement psUpdate = con.prepareStatement(sqlUpdate)) {
                                psUpdate.setString(1, String.valueOf(idGenerado));
                                psUpdate.setInt(2, idGenerado);
                                psUpdate.executeUpdate();
                            }
                        }
                    }
                }
                return true;
            }
            return false;

        } catch (SQLException e) {
            System.err.println("Error al registrar producto en BD: " + e.getMessage());
            return false;
        }
    }
    
    // ==========================================
    // MÉTODOS DE BÚSQUEDA Y ELIMINACIÓN
    // ==========================================

    public java.util.List<Producto> listarProductosActivos() {
        java.util.List<Producto> lista = new java.util.ArrayList<>();
        String sql = "SELECT * FROM INVENTARIO WHERE eliminado_producto = 0 ORDER BY nombre_producto ASC";

        try (Connection con = factory.getConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Producto p = new Producto();
                p.setIdProducto(rs.getInt("id_producto"));
                p.setCodigoBarras(rs.getString("codigo_barras_producto"));
                p.setNombreProducto(rs.getString("nombre_producto"));
                p.setPrecioCompra(rs.getDouble("precio_compra_producto"));
                p.setPrecioVenta(rs.getDouble("precio_venta_producto"));
                p.setPrecioMayorista(rs.getDouble("precio_mayorista_producto"));
                p.setStockProducto(rs.getInt("stock_producto"));
                p.setRutaImagen(rs.getString("ruta_imagen_producto"));
                
                // --- LAS DOS LÍNEAS QUE FALTABAN AQUÍ ---
                p.setDiasGarantia(rs.getInt("dias_garantia"));
                p.setRequiereSerie(rs.getBoolean("requiere_serie"));
                // ---------------------------------------
                
                lista.add(p);
            }
        } catch (SQLException e) {
            System.err.println("Error al listar inventario: " + e.getMessage());
        }
        return lista;
    }

    public boolean eliminarProductoLogico(int idProducto) {
        String sql = "UPDATE INVENTARIO SET eliminado_producto = 1 WHERE id_producto = ?";
        try (Connection con = factory.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idProducto);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al eliminar producto: " + e.getMessage());
            return false;
        }
    }
    
    // ==========================================
    // MÉTODOS PARA EDICIÓN DE PRODUCTO
    // ==========================================
    public Producto obtenerProductoPorId(int id) {
        String sql = "SELECT * FROM INVENTARIO WHERE id_producto = ?";
        try (Connection con = factory.getConexion(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Producto p = new Producto();
                    p.setIdProducto(rs.getInt("id_producto"));
                    p.setCodigoBarras(rs.getString("codigo_barras_producto"));
                    p.setNombreProducto(rs.getString("nombre_producto"));
                    p.setIdCategoria(rs.getInt("id_categoria"));
                    p.setIdProveedor(rs.getInt("id_proveedor"));
                    p.setIdUbicacion(rs.getInt("id_ubicacion"));
                    p.setPrecioCompra(rs.getDouble("precio_compra_producto"));
                    p.setPrecioVenta(rs.getDouble("precio_venta_producto"));
                    p.setPrecioMayorista(rs.getDouble("precio_mayorista_producto"));
                    p.setStockMinimo(rs.getInt("stock_minimo_producto"));
                    p.setStockProducto(rs.getInt("stock_producto"));
                    p.setRutaImagen(rs.getString("ruta_imagen_producto"));
                    p.setDiasGarantia(rs.getInt("dias_garantia"));
                    p.setRequiereSerie(rs.getBoolean("requiere_serie"));
                    return p;
                }
            }
        } catch (SQLException e) { System.err.println("Error al obtener producto: " + e.getMessage()); }
        return null;
    }

    public boolean actualizarProducto(Producto p) {
        String sql = "UPDATE INVENTARIO SET codigo_barras_producto = ?, nombre_producto = ?, id_categoria = ?, id_proveedor = ?, id_ubicacion = ?, precio_compra_producto = ?, precio_venta_producto = ?, precio_mayorista_producto = ?, stock_minimo_producto = ?, ruta_imagen_producto = ?, dias_garantia = ?, requiere_serie = ? WHERE id_producto = ?";
        try (Connection con = factory.getConexion(); PreparedStatement ps = con.prepareStatement(sql)) {
            if (p.getCodigoBarras() == null || p.getCodigoBarras().trim().isEmpty()) ps.setNull(1, java.sql.Types.VARCHAR);
            else ps.setString(1, p.getCodigoBarras().trim());
            
            ps.setString(2, p.getNombreProducto().trim());
            ps.setInt(3, p.getIdCategoria()); ps.setInt(4, p.getIdProveedor()); ps.setInt(5, p.getIdUbicacion());
            ps.setDouble(6, p.getPrecioCompra()); ps.setDouble(7, p.getPrecioVenta());
            
            if (p.getPrecioMayorista() > 0) ps.setDouble(8, p.getPrecioMayorista());
            else ps.setNull(8, java.sql.Types.DECIMAL);
            
            ps.setInt(9, p.getStockMinimo());
            
            if (p.getRutaImagen() == null || p.getRutaImagen().trim().isEmpty()) ps.setNull(10, java.sql.Types.VARCHAR);
            else ps.setString(10, p.getRutaImagen());
            
            ps.setInt(11, p.getDiasGarantia());
            ps.setBoolean(12, p.isRequiereSerie());
            ps.setInt(13, p.getIdProducto());
            
            ps.setInt(11, p.getIdProducto());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { System.err.println("Error al actualizar producto: " + e.getMessage()); return false; }
    }
}