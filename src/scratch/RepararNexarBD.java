package scratch;

import java.sql.Connection;
import java.sql.Statement;

public class RepararNexarBD {
    public static void main(String[] args) {
        try {
            factory.ConexionFactory factory = new factory.ConexionFactory();
            try (Connection con = factory.getConexion();
                 Statement st = con.createStatement()) {
                
                System.out.println("1. Forzando cierre de conexiones a NexarBD...");
                try {
                    st.execute("ALTER DATABASE NexarBD SET SINGLE_USER WITH ROLLBACK IMMEDIATE");
                    System.out.println("   Conexiones externas eliminadas.");
                } catch (Exception ex) {
                    System.out.println("   (Nota: " + ex.getMessage() + ")");
                }

                System.out.println("2. Alterando columna imagen_logo a VARCHAR(MAX)...");
                st.execute("ALTER TABLE EMPRESA ALTER COLUMN imagen_logo VARCHAR(MAX)");
                System.out.println("   Columna alterada exitosamente.");

                System.out.println("3. Restaurando acceso multi-usuario a NexarBD...");
                st.execute("ALTER DATABASE NexarBD SET MULTI_USER");
                System.out.println("   Acceso restaurado exitosamente.");
                
            }
        } catch (Exception e) {
            System.err.println("Error crítico: " + e.getMessage());
        }
    }
}
