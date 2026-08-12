package scratch;
import java.sql.*;
import factory.ConexionFactory;

public class AddColumn {
    public static void main(String[] args) {
        try {
            ConexionFactory factory = new ConexionFactory();
            Connection con = factory.getConexion();
            Statement stmt = con.createStatement();
            stmt.executeUpdate("ALTER TABLE INVENTARIO_DEFECTUOSO ADD foto VARCHAR(MAX)");
            System.out.println("Columna 'foto' anadida a INVENTARIO_DEFECTUOSO.");
            con.close();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}
