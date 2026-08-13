package scratch;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import factory.ConexionFactory;

public class CheckApiKeyColumn {
    public static void main(String[] args) {
        System.out.println("--- DIAGNÓSTICO DE LA TABLA EMPRESA ---");
        try (Connection con = new ConexionFactory().getConexion()) {
            String sql = "SELECT * FROM EMPRESA";
            try (PreparedStatement ps = con.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                
                ResultSetMetaData meta = rs.getMetaData();
                int numCols = meta.getColumnCount();
                boolean colExists = false;
                for (int i = 1; i <= numCols; i++) {
                    if (meta.getColumnName(i).equalsIgnoreCase("api_key_gemini")) {
                        colExists = true;
                        break;
                    }
                }
                
                System.out.println("¿La columna 'api_key_gemini' existe?: " + (colExists ? "SÍ" : "NO"));
                
                if (colExists) {
                    System.out.println("\nValores encontrados:");
                    while (rs.next()) {
                        int id = rs.getInt("id_empresa");
                        String val = rs.getString("api_key_gemini");
                        System.out.println("Fila ID " + id + " -> valor de api_key_gemini = " + (val == null ? "NULL" : "\"" + val + "\""));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
