package scratch;
import java.sql.*;
import factory.ConexionFactory;

public class CheckEmpresa {
    public static void main(String[] args) {
        try {
            ConexionFactory factory = new ConexionFactory();
            Connection con = factory.getConexion();
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT imagen_logo FROM EMPRESA WHERE id_empresa = 1");
            if (rs.next()) {
                String logo = rs.getString("imagen_logo");
                if (logo == null) {
                    System.out.println("Logo is NULL");
                } else {
                    System.out.println("Logo length: " + logo.length());
                    System.out.println("Prefix: " + (logo.length() > 50 ? logo.substring(0, 50) : logo));
                }
            } else {
                System.out.println("No row found");
            }
            con.close();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}
