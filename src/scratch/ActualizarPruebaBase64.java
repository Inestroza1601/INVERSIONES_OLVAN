package scratch;

import java.io.File;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Base64;

public class ActualizarPruebaBase64 {
    public static void main(String[] args) {
        String url = "jdbc:sqlserver://170.80.140.2:6161;databaseName=NexarBD;encrypt=true;trustServerCertificate=true;";
        String usuario = "orionsys";
        String password = "123";
        
        File file = new File("image/logo_inversionesOlvan.png");
        if (!file.exists()) {
            System.out.println("Error: No se encontró la imagen en " + file.getAbsolutePath());
            return;
        }
        
        String base64Str = null;
        try {
            byte[] bytes = Files.readAllBytes(file.toPath());
            String b64 = Base64.getEncoder().encodeToString(bytes);
            
            String mimeType = "image/png"; 
            String name = file.getName().toLowerCase();
            if (name.endsWith(".jpg") || name.endsWith(".jpeg")) {
                mimeType = "image/jpeg";
            } else if (name.endsWith(".gif")) {
                mimeType = "image/gif";
            }
            
            base64Str = "data:" + mimeType + ";base64," + b64;
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        
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
                        
                        psUpdate.setString(1, base64Str);
                        psUpdate.setInt(2, idProducto);
                        psUpdate.executeUpdate();
                        
                        contador++;
                    }
                    System.out.println("¡" + contador + " productos actualizados con la imagen Base64!");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
