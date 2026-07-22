package dao;

import factory.ConexionFactory;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class CatalogosDAO {
    private ConexionFactory factory;

    public CatalogosDAO() {
        this.factory = new ConexionFactory();
    }

    // ==========================================
    // MÉTODOS PARA CATEGORÍAS
    // ==========================================
    public Map<Integer, String> listarCategorias() {
        Map<Integer, String> mapa = new HashMap<>();
        String sql = "SELECT id_categoria, nombre_categoria FROM CATEGORIAS";
        try (Connection con = factory.getConexion(); PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) mapa.put(rs.getInt("id_categoria"), rs.getString("nombre_categoria"));
        } catch (SQLException e) { System.err.println("Error Categorias: " + e.getMessage()); }
        return mapa;
    }

    public boolean registrarCategoria(String nombre, String desc, int garantias) {
        String sql = "INSERT INTO CATEGORIAS (nombre_categoria, descripcion_categoria, dias_garantias) VALUES (?, ?, ?)";
        try (Connection con = factory.getConexion(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, nombre); ps.setString(2, desc); ps.setInt(3, garantias);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { return false; }
    }

    public String[] obtenerDatosCategoria(int id) {
        String sql = "SELECT descripcion_categoria, dias_garantias FROM CATEGORIAS WHERE id_categoria = ?";
        try (Connection con = factory.getConexion(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id); ResultSet rs = ps.executeQuery();
            if (rs.next()) return new String[]{rs.getString("descripcion_categoria"), rs.getString("dias_garantias")};
        } catch (SQLException e) { } return new String[]{"", "0"};
    }

    public boolean actualizarCategoria(int id, String nombre, String desc, int garantias) {
        String sql = "UPDATE CATEGORIAS SET nombre_categoria = ?, descripcion_categoria = ?, dias_garantias = ? WHERE id_categoria = ?";
        try (Connection con = factory.getConexion(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, nombre); ps.setString(2, desc); ps.setInt(3, garantias); ps.setInt(4, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { return false; }
    }

    // ==========================================
    // MÉTODOS PARA PROVEEDORES
    // ==========================================
    public Map<Integer, String> listarProveedores() {
        Map<Integer, String> mapa = new HashMap<>();
        String sql = "SELECT id_proveedor, nombre_proveedor FROM PROVEEDORES WHERE estado_proveedor = 1";
        try (Connection con = factory.getConexion(); PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) mapa.put(rs.getInt("id_proveedor"), rs.getString("nombre_proveedor"));
        } catch (SQLException e) { System.err.println("Error Proveedores: " + e.getMessage()); }
        return mapa;
    }

    public boolean registrarProveedor(String nombre, String encargado, String tel, String dir, String repuestos) {
        String sql = "INSERT INTO PROVEEDORES (nombre_proveedor, nombre_encargado_proveedor, telefono_proveedor, direccion_proveedor, tipo_repuestos_proveedor, estado_proveedor) VALUES (?, ?, ?, ?, ?, 1)";
        try (Connection con = factory.getConexion(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, nombre); ps.setString(2, encargado); ps.setString(3, tel); ps.setString(4, dir); ps.setString(5, repuestos);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { return false; }
    }

    public String[] obtenerDatosProveedor(int id) {
        String sql = "SELECT nombre_encargado_proveedor, telefono_proveedor, direccion_proveedor, tipo_repuestos_proveedor FROM PROVEEDORES WHERE id_proveedor = ?";
        try (Connection con = factory.getConexion(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id); ResultSet rs = ps.executeQuery();
            if (rs.next()) return new String[]{rs.getString("nombre_encargado_proveedor"), rs.getString("telefono_proveedor"), rs.getString("direccion_proveedor"), rs.getString("tipo_repuestos_proveedor")};
        } catch (SQLException e) { } return new String[]{"", "", "", ""};
    }

    public boolean actualizarProveedor(int id, String nombre, String encargado, String tel, String dir, String repuestos) {
        String sql = "UPDATE PROVEEDORES SET nombre_proveedor = ?, nombre_encargado_proveedor = ?, telefono_proveedor = ?, direccion_proveedor = ?, tipo_repuestos_proveedor = ? WHERE id_proveedor = ?";
        try (Connection con = factory.getConexion(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, nombre); ps.setString(2, encargado); ps.setString(3, tel); ps.setString(4, dir); ps.setString(5, repuestos); ps.setInt(6, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { return false; }
    }

    // ==========================================
    // MÉTODOS PARA UBICACIONES
    // ==========================================
    public Map<Integer, String> listarUbicaciones() {
        Map<Integer, String> mapa = new HashMap<>();
        String sql = "SELECT id_ubicacion, nombre_ubicacion FROM UBICACIONES";
        try (Connection con = factory.getConexion(); PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) mapa.put(rs.getInt("id_ubicacion"), rs.getString("nombre_ubicacion"));
        } catch (SQLException e) { System.err.println("Error Ubicaciones: " + e.getMessage()); }
        return mapa;
    }

    public boolean registrarUbicacion(String nombre) {
        String sql = "INSERT INTO UBICACIONES (nombre_ubicacion) VALUES (?)";
        try (Connection con = factory.getConexion(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, nombre);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { return false; }
    }

    public boolean actualizarUbicacion(int id, String nombre) {
        String sql = "UPDATE UBICACIONES SET nombre_ubicacion = ? WHERE id_ubicacion = ?";
        try (Connection con = factory.getConexion(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, nombre); ps.setInt(2, id); return ps.executeUpdate() > 0;
        } catch (SQLException e) { return false; }
    }
}