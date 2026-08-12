package dao;

import factory.ConexionFactory;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ErrorDAO {

    private ConexionFactory factory;

    public ErrorDAO() {
        this.factory = new ConexionFactory();
        crearTablaSiNoExiste();
    }

    private void crearTablaSiNoExiste() {
        String sql = "IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='ERRORES_SISTEMA' and xtype='U') " +
                     "CREATE TABLE ERRORES_SISTEMA (" +
                     "id_error INT IDENTITY(1,1) PRIMARY KEY, " +
                     "fecha_suceso DATETIME DEFAULT GETDATE(), " +
                     "origen VARCHAR(255), " +
                     "resumen VARCHAR(500), " +
                     "stacktrace TEXT" +
                     ")";
        try (Connection con = factory.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.execute();
        } catch (SQLException e) {
            System.err.println("Error verificando/creando tabla ERRORES_SISTEMA: " + e.getMessage());
        }
    }

    public boolean insertarError(String origen, String resumen, String stacktrace) {
        String sql = "INSERT INTO ERRORES_SISTEMA (origen, resumen, stacktrace) VALUES (?, ?, ?)";
        try (Connection con = factory.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, origen);
            ps.setString(2, resumen);
            ps.setString(3, stacktrace);
            int filas = ps.executeUpdate();
            return filas > 0;
        } catch (SQLException e) {
            System.err.println("Fallo crítico al guardar log de error: " + e.getMessage());
            return false;
        }
    }

    public List<Object[]> listarErrores() {
        List<Object[]> lista = new ArrayList<>();
        String sql = "SELECT id_error, fecha_suceso, origen, resumen, stacktrace FROM ERRORES_SISTEMA ORDER BY fecha_suceso DESC";
        try (Connection con = factory.getConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(new Object[]{
                    rs.getInt("id_error"),
                    rs.getString("fecha_suceso"),
                    rs.getString("origen"),
                    rs.getString("resumen"),
                    rs.getString("stacktrace")
                });
            }
        } catch (SQLException e) {
            System.err.println("Error al listar los errores: " + e.getMessage());
        }
        return lista;
    }

    public boolean limpiarErrores() {
        String sql = "DELETE FROM ERRORES_SISTEMA";
        try (Connection con = factory.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            return ps.executeUpdate() >= 0;
        } catch (SQLException e) {
            System.err.println("Error al vaciar los errores: " + e.getMessage());
            return false;
        }
    }
}
