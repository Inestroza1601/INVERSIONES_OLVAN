import factory.ConexionFactory;
import java.sql.Connection;
import java.sql.Statement;

public class MigracionDB {
    public static void main(String[] args) {
        try {
            ConexionFactory cf = new ConexionFactory();
            try (Connection con = cf.getConexion();
                 Statement stmt = con.createStatement()) {
                String sql = "ALTER TABLE INVENTARIO ADD incluye_impuesto BIT DEFAULT 1 NOT NULL";
                stmt.executeUpdate(sql);
                System.out.println("Migracion exitosa. Columna incluye_impuesto agregada.");
            }
        } catch (Exception e) {
            System.err.println("Error en la migracion: " + e.getMessage());
        }
    }
}
