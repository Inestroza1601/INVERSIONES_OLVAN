package scratch;

import factory.ConexionFactory;
import java.sql.Connection;
import java.sql.Statement;

public class AddLogicalDeleteColumns {
    public static void main(String[] args) {
        ConexionFactory factory = new ConexionFactory();
        System.out.println("Intentando conectar a la base de datos...");
        try (Connection con = factory.getConexion(); Statement stmt = con.createStatement()) {
            System.out.println("Conectado con éxito. Ejecutando ALTER TABLE...");
            
            try {
                stmt.execute("ALTER TABLE CATEGORIAS ADD estado_categoria INT DEFAULT 1 NOT NULL;");
                System.out.println("Columna estado_categoria agregada exitosamente.");
            } catch (Exception e) {
                if (e.getMessage().contains("already has a") || e.getMessage().contains("ya existe")) {
                    System.out.println("La columna estado_categoria ya existe.");
                } else {
                    System.out.println("Error con CATEGORIAS: " + e.getMessage());
                }
            }

            try {
                stmt.execute("ALTER TABLE UBICACIONES ADD estado_ubicacion INT DEFAULT 1 NOT NULL;");
                System.out.println("Columna estado_ubicacion agregada exitosamente.");
            } catch (Exception e) {
                if (e.getMessage().contains("already has a") || e.getMessage().contains("ya existe")) {
                    System.out.println("La columna estado_ubicacion ya existe.");
                } else {
                    System.out.println("Error con UBICACIONES: " + e.getMessage());
                }
            }
            
            System.out.println("Operación completada.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
