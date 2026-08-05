package factory; // Poniéndolo en factory para que pueda acceder a ConexionFactory fácilmente si es package private

import java.sql.Connection;
import java.sql.Statement;

public class ActualizarBD {
    public static void main(String[] args) {
        try (Connection con = new ConexionFactory().getConexion();
             Statement stmt = con.createStatement()) {
             
             try {
                 stmt.executeUpdate("ALTER TABLE EMPRESA ADD politicas_garantia VARCHAR(MAX) NULL");
                 System.out.println("politicas_garantia agregada.");
             } catch(Exception e) {
                 System.out.println("politicas_garantia ya existe o error: " + e.getMessage());
             }

             try {
                 stmt.executeUpdate("ALTER TABLE EMPRESA ADD mensaje_ticket_cambio VARCHAR(MAX) NULL");
                 System.out.println("mensaje_ticket_cambio agregado.");
             } catch(Exception e) {
                 System.out.println("mensaje_ticket_cambio ya existe o error: " + e.getMessage());
             }

             try {
                 stmt.executeUpdate("ALTER TABLE EMPRESA ADD mensaje_ticket_reclamo VARCHAR(MAX) NULL");
                 System.out.println("mensaje_ticket_reclamo agregado.");
             } catch(Exception e) {
                 System.out.println("mensaje_ticket_reclamo ya existe o error: " + e.getMessage());
             }

        } catch(Exception e) {
             e.printStackTrace();
        }
    }
}
