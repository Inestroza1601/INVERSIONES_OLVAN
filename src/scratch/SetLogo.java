package scratch;
import java.sql.*;
import java.io.File;
import java.nio.file.Files;
import java.util.Base64;
import factory.ConexionFactory;

public class SetLogo {
    public static void main(String[] args) {
        try {
            File f = new File("src/image/logo_inversionesOlvan.png");
            if (!f.exists()) {
                f = new File("src/image/logo.png");
            }
            if(f.exists()) {
                byte[] fileContent = Files.readAllBytes(f.toPath());
                String base64 = "data:image/png;base64," + Base64.getEncoder().encodeToString(fileContent);
                
                ConexionFactory factory = new ConexionFactory();
                Connection con = factory.getConexion();
                PreparedStatement ps = con.prepareStatement("UPDATE EMPRESA SET imagen_logo = ? WHERE id_empresa = 1");
                ps.setString(1, base64);
                ps.executeUpdate();
                System.out.println("Logo actualizado exitosamente.");
                con.close();
            } else {
                System.out.println("No se encontro ninguna imagen de logo.");
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}
