package dao;

import factory.ConexionFactory;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class BiometriaDAO {
    private ConexionFactory conexionFactory;

    public BiometriaDAO() {
        this.conexionFactory = new ConexionFactory();
    }

    public int obtenerLabelIdPorUsuario(int idUsuario) {
        String sql = "SELECT label_id FROM USUARIO_BIOMETRIA WHERE id_usuario = ?";
        try (Connection con = conexionFactory.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idUsuario);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("label_id");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1; // No existe
    }

    public int obtenerUsuarioPorLabelId(int labelId) {
        String sql = "SELECT id_usuario FROM USUARIO_BIOMETRIA WHERE label_id = ?";
        try (Connection con = conexionFactory.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, labelId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id_usuario");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1; // No encontrado
    }
    
    public int registrarUsuarioBiometria(int idUsuario) {
        int labelExistente = obtenerLabelIdPorUsuario(idUsuario);
        if (labelExistente != -1) {
            return labelExistente;
        }
        
        String sql = "INSERT INTO USUARIO_BIOMETRIA (id_usuario) VALUES (?)";
        try (Connection con = conexionFactory.getConexion();
             PreparedStatement ps = con.prepareStatement(sql, java.sql.Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, idUsuario);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }
}
