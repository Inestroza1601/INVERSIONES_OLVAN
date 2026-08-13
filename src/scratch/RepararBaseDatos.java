package scratch;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class RepararBaseDatos {
    public static void main(String[] args) {
        try {
            factory.ConexionFactory factory = new factory.ConexionFactory();
            try (Connection con = factory.getConexion();
                 Statement st = con.createStatement()) {
                
                System.out.println("1. Verificando tipo de columna actual...");
                ResultSet rs = st.executeQuery("SELECT DATA_TYPE, CHARACTER_MAXIMUM_LENGTH FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'EMPRESA' AND COLUMN_NAME = 'imagen_logo'");
                if (rs.next()) {
                    System.out.println("Tipo actual: " + rs.getString("DATA_TYPE") + "(" + rs.getInt("CHARACTER_MAXIMUM_LENGTH") + ")");
                }
                rs.close();

                System.out.println("2. Forzando actualización a VARCHAR(MAX)...");
                st.setQueryTimeout(10); // 10 segundos maximo para no colgarse
                st.execute("ALTER TABLE EMPRESA ALTER COLUMN imagen_logo VARCHAR(MAX)");
                System.out.println("3. ¡Modificación exitosa! La columna ahora soporta Base64 sin truncar.");
                
            }
        } catch (Exception e) {
            System.err.println("Error crítico al alterar la BD: " + e.getMessage());
        }
    }
}
