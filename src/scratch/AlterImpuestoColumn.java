package scratch;

import factory.ConexionFactory;
import java.sql.Connection;
import java.sql.Statement;

public class AlterImpuestoColumn {

    public static void main(String[] args) {
        System.out.println("Iniciando actualización de esquema de la tabla 'inventario'...");

        String query = "ALTER TABLE inventario MODIFY COLUMN incluye_impuesto TINYINT(1) DEFAULT 0;";

        try (Connection con = new ConexionFactory().getConexion();
             Statement stmt = con.createStatement()) {

            stmt.executeUpdate(query);
            System.out.println("¡Éxito! La columna 'incluye_impuesto' ha sido actualizada correctamente en la tabla 'inventario'.");
            System.out.println("Ahora soporta los valores 0 (No incluido), 1 (Incluido), y 2 (Exento).");

        } catch (Exception e) {
            System.err.println("Error al actualizar la base de datos:");
            e.printStackTrace();
        }
    }
}
