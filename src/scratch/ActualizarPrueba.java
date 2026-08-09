package scratch;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class ActualizarPrueba {
    public static void main(String[] args) {
        String url = "jdbc:sqlserver://170.80.140.2:6161;databaseName=NexarBD;encrypt=true;trustServerCertificate=true;";
        String usuario = "orionsys";
        String password = "123";
        
        String rutaLocal = "src/image/logo_inversionesOlvan.png";
        
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            try (Connection con = DriverManager.getConnection(url, usuario, password)) {
                
                String sqlSelect = "SELECT id_producto FROM INVENTARIO WHERE codigo_barras_producto LIKE 'PROD-TEST-%'";
                String sqlUpdate = "UPDATE INVENTARIO SET ruta_imagen_producto = ? WHERE id_producto = ?";
                
                try (PreparedStatement psSelect = con.prepareStatement(sqlSelect);
                     ResultSet rs = psSelect.executeQuery();
                     PreparedStatement psUpdate = con.prepareStatement(sqlUpdate)) {
                     
                    int contador = 0;
                    while (rs.next()) {
                        int idProducto = rs.getInt("id_producto");
                        
                        psUpdate.setString(1, rutaLocal);
                        psUpdate.setInt(2, idProducto);
                        psUpdate.executeUpdate();
                        
                        contador++;
                    }
                    System.out.println("¡" + contador + " productos actualizados con la ruta local: " + rutaLocal + "!");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
