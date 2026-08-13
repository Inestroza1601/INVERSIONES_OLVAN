package scratch;

import java.sql.Connection;
import java.sql.Statement;

public class AlterLogoColumn {
    public static void main(String[] args) {
        try {
            factory.ConexionFactory factory = new factory.ConexionFactory();
            try (Connection con = factory.getConexion();
                 Statement st = con.createStatement()) {
                
                System.out.println("Intentando modificar la columna imagen_logo a VARCHAR(MAX)...");
                st.execute("ALTER TABLE EMPRESA ALTER COLUMN imagen_logo VARCHAR(MAX)");
                System.out.println("Modificación exitosa. La columna ahora soporta Base64.");
            }
        } catch (Exception e) {
            System.err.println("Error al alterar la columna: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
