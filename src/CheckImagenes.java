import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class CheckImagenes {
    public static void main(String[] args) {
        try (Connection con = new factory.ConexionFactory().getConexion();
             PreparedStatement ps = con.prepareStatement("SELECT COUNT(*) AS total, SUM(CASE WHEN imagen_producto IS NULL THEN 1 ELSE 0 END) AS nulos FROM INVENTARIO");
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                System.out.println("Total productos: " + rs.getInt("total"));
                System.out.println("Productos sin imagen: " + rs.getInt("nulos"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
