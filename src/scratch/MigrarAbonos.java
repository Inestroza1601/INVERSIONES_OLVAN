package scratch;

import factory.ConexionFactory;
import java.sql.Connection;
import java.sql.Statement;

public class MigrarAbonos {
    public static void main(String[] args) {
        ConexionFactory factory = new ConexionFactory();
        try (Connection con = factory.getConexion(); Statement stmt = con.createStatement()) {
            System.out.println("Conectando a la base de datos...");
            
            // 1. Añadir columnas
            try {
                stmt.executeUpdate("ALTER TABLE ABONOS_APARTADO ADD total_historico DECIMAL(10,2) DEFAULT 0.0 NOT NULL");
                System.out.println("Columna total_historico añadida.");
            } catch (Exception e) {
                System.out.println("Columna total_historico ya existe o error: " + e.getMessage());
            }
            
            try {
                stmt.executeUpdate("ALTER TABLE ABONOS_APARTADO ADD saldo_historico DECIMAL(10,2) DEFAULT 0.0 NOT NULL");
                System.out.println("Columna saldo_historico añadida.");
            } catch (Exception e) {
                System.out.println("Columna saldo_historico ya existe o error: " + e.getMessage());
            }
            
            // 2. Actualizar fallback
            String updateSql = "UPDATE ab SET ab.total_historico = ap.total_apartado, ab.saldo_historico = ap.saldo_pendiente " +
                               "FROM ABONOS_APARTADO ab INNER JOIN APARTADOS ap ON ab.id_apartado = ap.id_apartado " +
                               "WHERE ab.total_historico = 0";
            int rows = stmt.executeUpdate(updateSql);
            System.out.println("Saldos históricos actualizados (Fallback) en " + rows + " filas.");
            
            System.out.println("Migración completada exitosamente.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
