package scratch;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class TestDB {
    public static void main(String[] args) {
        String url = "jdbc:sqlserver://170.80.140.2:6161;databaseName=NexarBD;encrypt=true;trustServerCertificate=true;";
        try (Connection conn = DriverManager.getConnection(url, "orionsys", "123");
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT @@VERSION")) {
            if (rs.next()) {
                System.out.println("Conectado: " + rs.getString(1));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
