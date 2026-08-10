package scratch;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class MigrarCRUD {
    public static void main(String[] args) {
        String url = "jdbc:sqlserver://170.80.140.2:6161;databaseName=NexarBD;encrypt=true;trustServerCertificate=true;";
        
        String[] modulosCRUD = {
            "ADMINISTRACION", "POS", "INVENTARIO", "CAJA", 
            "CLIENTES", "APARTADOS", "VENTAS", "GARANTIAS"
        };
        
        try (Connection con = DriverManager.getConnection(url, "orionsys", "123");
             Statement stmt = con.createStatement()) {
            
            // 1. Limpiar permisos antiguos
            stmt.executeUpdate("DELETE FROM ROL_PERMISOS");
            stmt.executeUpdate("DELETE FROM PERMISOS");
            
            try {
                stmt.executeUpdate("DBCC CHECKIDENT ('PERMISOS', RESEED, 0)");
            } catch (Exception e) {} // Ignorar si falla
            
            // 2. Insertar nuevos permisos CRUD
            for (String modulo : modulosCRUD) {
                stmt.executeUpdate("INSERT INTO PERMISOS (nombre_permiso) VALUES ('VER_" + modulo + "')");
                stmt.executeUpdate("INSERT INTO PERMISOS (nombre_permiso) VALUES ('CREAR_" + modulo + "')");
                stmt.executeUpdate("INSERT INTO PERMISOS (nombre_permiso) VALUES ('EDITAR_" + modulo + "')");
                stmt.executeUpdate("INSERT INTO PERMISOS (nombre_permiso) VALUES ('ELIMINAR_" + modulo + "')");
            }
            
            // Modulo Especial: Estadisticas
            stmt.executeUpdate("INSERT INTO PERMISOS (nombre_permiso) VALUES ('VER_ESTADISTICAS')");
            
            // 3. Asignar todos los permisos al Rol Administrador (id_rol = 1)
            stmt.executeUpdate("INSERT INTO ROL_PERMISOS (id_rol, id_permiso) SELECT 1, id_permiso FROM PERMISOS");
            
            System.out.println("Migracion CRUD completada con exito.");
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
