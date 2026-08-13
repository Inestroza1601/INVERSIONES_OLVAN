package scratch;

import java.sql.Connection;
import java.sql.Statement;

public class RepararBaseDatosForzado {
    public static void main(String[] args) {
        try {
            factory.ConexionFactory factory = new factory.ConexionFactory();
            try (Connection con = factory.getConexion();
                 Statement st = con.createStatement()) {
                
                System.out.println("1. Forzando cierre de todas las conexiones fantasma a ORION_SYS...");
                try {
                    st.execute("ALTER DATABASE ORION_SYS SET SINGLE_USER WITH ROLLBACK IMMEDIATE");
                    System.out.println("   Conexiones cerradas exitosamente.");
                } catch (Exception ex) {
                    System.out.println("   (Ignorando error de single_user: " + ex.getMessage() + ")");
                }

                System.out.println("2. Alterando columna imagen_logo a VARCHAR(MAX)...");
                st.execute("ALTER TABLE EMPRESA ALTER COLUMN imagen_logo VARCHAR(MAX)");
                System.out.println("   Columna alterada exitosamente.");

                System.out.println("3. Restaurando acceso multi-usuario a ORION_SYS...");
                st.execute("ALTER DATABASE ORION_SYS SET MULTI_USER");
                System.out.println("   Acceso restaurado exitosamente.");
                
            }
        } catch (Exception e) {
            System.err.println("Error crítico: " + e.getMessage());
        }
    }
}
