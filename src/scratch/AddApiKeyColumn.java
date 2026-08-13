package scratch;

import java.sql.Connection;
import java.sql.Statement;
import factory.ConexionFactory;

public class AddApiKeyColumn {
    public static void main(String[] args) {
        try {
            ConexionFactory factory = new ConexionFactory();
            Connection con = factory.getConexion();
            Statement stmt = con.createStatement();
            
            // Verificamos primero si la columna existe
            String sql = "IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID(N'[dbo].[EMPRESA]') AND name = 'api_key_gemini') "
                       + "BEGIN "
                       + "    ALTER TABLE EMPRESA ADD api_key_gemini VARCHAR(255) NULL; "
                       + "END";
            
            stmt.execute(sql);
            System.out.println("Columna 'api_key_gemini' agregada con éxito a la tabla EMPRESA (o ya existía).");
            
            stmt.close();
            con.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
