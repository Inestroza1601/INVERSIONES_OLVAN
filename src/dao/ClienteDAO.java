package dao;

import factory.ConexionFactory;
import modelo.Cliente;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ClienteDAO {

    private ConexionFactory factory;

    public ClienteDAO() {
        this.factory = new ConexionFactory();
    }

    // 1. LISTAR CLIENTES (Solo los activos)
    public List<Cliente> listarClientesActivos() {
        List<Cliente> lista = new ArrayList<>();
        String sql = "SELECT * FROM CLIENTES WHERE estado_cliente = 1 ORDER BY nombre_cliente ASC";

        try (Connection con = factory.getConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Cliente c = new Cliente();
                c.setIdCliente(rs.getInt("id_cliente"));
                c.setIdentidadCliente(rs.getString("identidad_cliente"));
                c.setNombreCliente(rs.getString("nombre_cliente"));
                c.setApellidoCliente(rs.getString("apellido_cliente"));
                c.setTelefonoCliente(rs.getString("telefono_cliente"));
                c.setCorreoCliente(rs.getString("correo_cliente"));
                c.setEstadoCliente(rs.getBoolean("estado_cliente"));
                lista.add(c);
            }
        } catch (SQLException e) {
            System.err.println("Error al listar clientes: " + e.getMessage());
        }
        return lista;
    }

    // 2. REGISTRAR CLIENTE NUEVO
    public boolean registrarCliente(Cliente c) {
        String sql = "INSERT INTO CLIENTES (identidad_cliente, nombre_cliente, apellido_cliente, "
                   + "telefono_cliente, correo_cliente, estado_cliente) VALUES (?, ?, ?, ?, ?, 1)";

        try (Connection con = factory.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, c.getIdentidadCliente());
            ps.setString(2, c.getNombreCliente());
            ps.setString(3, c.getApellidoCliente());
            ps.setString(4, c.getTelefonoCliente());
            ps.setString(5, c.getCorreoCliente());
            // El estado por defecto será 1 (Activo) en la inserción

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error al registrar cliente: " + e.getMessage());
            return false;
        }
    }

    // 3. ACTUALIZAR CLIENTE EXISTENTE
    public boolean actualizarCliente(Cliente c) {
        String sql = "UPDATE CLIENTES SET identidad_cliente = ?, nombre_cliente = ?, apellido_cliente = ?, "
                   + "telefono_cliente = ?, correo_cliente = ? WHERE id_cliente = ?";

        try (Connection con = factory.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, c.getIdentidadCliente());
            ps.setString(2, c.getNombreCliente());
            ps.setString(3, c.getApellidoCliente());
            ps.setString(4, c.getTelefonoCliente());
            ps.setString(5, c.getCorreoCliente());
            ps.setInt(6, c.getIdCliente());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error al actualizar cliente: " + e.getMessage());
            return false;
        }
    }

    // 4. ELIMINACIÓN LÓGICA (SOFT DELETE)
    public boolean desactivarCliente(int idCliente) {
        String sql = "UPDATE CLIENTES SET estado_cliente = 0 WHERE id_cliente = ?";

        try (Connection con = factory.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idCliente);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error al desactivar cliente: " + e.getMessage());
            return false;
        }
    }
    
    // 5. VALIDAR SI EXISTE LA IDENTIDAD (Para evitar duplicados)
    public boolean existeIdentidad(String identidad, int idExcluir) {
        // Buscamos si hay alguien activo con esa identidad, ignorando al cliente que estamos editando
        String sql = "SELECT id_cliente FROM CLIENTES WHERE identidad_cliente = ? AND id_cliente != ? AND estado_cliente = 1";
        
        try (Connection con = factory.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, identidad);
            ps.setInt(2, idExcluir);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next(); // Retorna true si encontró a alguien
            }

        } catch (SQLException e) {
            System.err.println("Error al validar identidad: " + e.getMessage());
            return false;
        }
    }
    
    // 6. CARGAR IDENTIDADES EN RAM PARA VALIDACIÓN INSTANTÁNEA
    public java.util.Set<String> obtenerIdentidadesEnRam() {
        java.util.Set<String> identidades = new java.util.HashSet<>();
        String sql = "SELECT identidad_cliente FROM CLIENTES WHERE estado_cliente = 1";
        
        try (Connection con = factory.getConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
             
            while (rs.next()) {
                // Guardamos las identidades en la memoria RAM
                identidades.add(rs.getString("identidad_cliente"));
            }
            
        } catch (SQLException e) {
            System.err.println("Error al cargar identidades a RAM: " + e.getMessage());
        }
        return identidades;
    }
}