package scratch;

import java.io.File;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Base64;
import factory.ConexionFactory;

public class ActualizarLogoEmpresa {
    public static void main(String[] args) {
        File file = new File("src/image/logo_inversionesOlvan.png");
        if (!file.exists()) {
            System.out.println("Error: No se encontró la imagen en " + file.getAbsolutePath());
            return;
        }
        
        String base64Str = null;
        try {
            byte[] bytes = Files.readAllBytes(file.toPath());
            String b64 = Base64.getEncoder().encodeToString(bytes);
            base64Str = "data:image/png;base64," + b64;
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        
        try {
            ConexionFactory factory = new ConexionFactory();
            try (Connection con = factory.getConexion()) {
                String sqlUpdate = "UPDATE EMPRESA SET imagen_logo = ? WHERE id_empresa = 1";
                try (PreparedStatement psUpdate = con.prepareStatement(sqlUpdate)) {
                    psUpdate.setString(1, base64Str);
                    int filas = psUpdate.executeUpdate();
                    System.out.println("Filas actualizadas: " + filas);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
