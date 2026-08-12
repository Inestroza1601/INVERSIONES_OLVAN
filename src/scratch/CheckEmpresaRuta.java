package scratch;
import java.sql.*;
import factory.ConexionFactory;

public class CheckEmpresaRuta {
    public static void main(String[] args) {
        try {
            ConexionFactory factory = new ConexionFactory();
            Connection con = factory.getConexion();
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT logo_empresa_ruta FROM EMPRESA WHERE id_empresa = 1");
            if (rs.next()) {
                System.out.println("Ruta: " + rs.getString("logo_empresa_ruta"));
            } else {
                System.out.println("No row found");
            }
            con.close();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}
