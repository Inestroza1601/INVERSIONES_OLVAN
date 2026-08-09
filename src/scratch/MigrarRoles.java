package scratch;

import factory.ConexionFactory;
import java.sql.Connection;
import java.sql.Statement;

public class MigrarRoles {
    public static void main(String[] args) {
        ConexionFactory factory = new ConexionFactory();
        try (Connection con = factory.getConexion();
             Statement stmt = con.createStatement()) {
            
            System.out.println("Conectando a la base de datos...");
            
            String sql1 = "IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='PERMISOS' and xtype='U') " +
                          "CREATE TABLE PERMISOS (" +
                          "id_permiso INT IDENTITY(1,1) PRIMARY KEY, " +
                          "nombre_permiso VARCHAR(50) NOT NULL UNIQUE, " +
                          "descripcion VARCHAR(150))";
            stmt.execute(sql1);
            System.out.println("Tabla PERMISOS lista.");
            
            String sql2 = "IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='ROL_PERMISOS' and xtype='U') " +
                          "CREATE TABLE ROL_PERMISOS (" +
                          "id_rol INT NOT NULL, " +
                          "id_permiso INT NOT NULL, " +
                          "PRIMARY KEY (id_rol, id_permiso), " +
                          "FOREIGN KEY (id_rol) REFERENCES ROLES_USUARIO(id_rol), " +
                          "FOREIGN KEY (id_permiso) REFERENCES PERMISOS(id_permiso))";
            stmt.execute(sql2);
            System.out.println("Tabla ROL_PERMISOS lista.");
            
            // Insertar permisos básicos si no existen
            String[] permisos = {
                "ACCESO_ADMINISTRACION", 
                "ACCESO_INVENTARIO", "ELIMINAR_PRODUCTOS",
                "ACCESO_CLIENTES", "ELIMINAR_CLIENTES",
                "ACCESO_POS", "CANCELAR_VENTAS", "APLICAR_DESCUENTOS",
                "ACCESO_CAJA", 
                "ACCESO_APARTADOS", 
                "ACCESO_VENTAS", "ANULAR_FACTURAS",
                "ACCESO_GARANTIAS", 
                "ACCESO_ESTADISTICAS"
            };
            
            for (String p : permisos) {
                String ins = "IF NOT EXISTS (SELECT * FROM PERMISOS WHERE nombre_permiso = '" + p + "') " +
                             "INSERT INTO PERMISOS (nombre_permiso, descripcion) VALUES ('" + p + "', 'Acceso a " + p + "')";
                stmt.execute(ins);
            }
            System.out.println("Permisos insertados.");
            
            System.out.println("Migración completada exitosamente.");
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
