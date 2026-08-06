package scratch;
import factory.ConexionFactory;
import java.sql.*;

public class CheckSchema {
    public static void main(String[] args) {
        try (Connection con = new ConexionFactory().getConexion();
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT TOP 1 * FROM INVENTARIO")) {
            ResultSetMetaData rsmd = rs.getMetaData();
            for (int i = 1; i <= rsmd.getColumnCount(); i++) {
                System.out.println(rsmd.getColumnName(i));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
