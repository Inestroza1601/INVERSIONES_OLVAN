package scratch;

import factory.ConexionFactory;
import java.io.File;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Base64;

public class MigradorBase64 {
    public static void main(String[] args) {
        ConexionFactory factory = new ConexionFactory();
        try (Connection con = factory.getConexion()) {
            if (con == null) {
                System.out.println("Error: No se pudo conectar a la base de datos.");
                return;
            }
            
            System.out.println("--- INICIANDO MIGRACI\u00D3N DE IM\u00C1GENES A BASE64 ---");
            
            try (Statement st = con.createStatement()) {
                System.out.println("A\u00F1adiendo nuevas columnas...");
                try {
                    st.execute("ALTER TABLE INVENTARIO ADD imagen_producto VARCHAR(MAX)");
                    System.out.println("Columna imagen_producto a\u00F1adida a INVENTARIO.");
                } catch(Exception e) { System.out.println("La columna imagen_producto ya existe o hubo error: " + e.getMessage()); }
                
                try {
                    st.execute("ALTER TABLE EMPRESA ADD imagen_logo VARCHAR(MAX)");
                    System.out.println("Columna imagen_logo a\u00F1adida a EMPRESA.");
                } catch(Exception e) { System.out.println("La columna imagen_logo ya existe o hubo error: " + e.getMessage()); }
            }

            System.out.println("\nProcesando INVENTARIO...");
            String selInv = "SELECT id_producto, ruta_imagen_producto FROM INVENTARIO WHERE ruta_imagen_producto IS NOT NULL AND ruta_imagen_producto != ''";
            String updInv = "UPDATE INVENTARIO SET imagen_producto = ? WHERE id_producto = ?";
            
            try (PreparedStatement psSelInv = con.prepareStatement(selInv);
                 ResultSet rsInv = psSelInv.executeQuery();
                 PreparedStatement psUpdInv = con.prepareStatement(updInv)) {
                 
                int countInv = 0;
                while (rsInv.next()) {
                    int id = rsInv.getInt("id_producto");
                    String ruta = rsInv.getString("ruta_imagen_producto");
                    File f = new File(ruta);
                    if (f.exists() && f.isFile()) {
                        byte[] bytes = Files.readAllBytes(f.toPath());
                        String base64 = Base64.getEncoder().encodeToString(bytes);
                        psUpdInv.setString(1, base64);
                        psUpdInv.setInt(2, id);
                        psUpdInv.executeUpdate();
                        countInv++;
                        System.out.println("Migrado producto ID: " + id);
                    } else {
                        System.out.println("Archivo no encontrado para producto ID: " + id + " -> " + ruta);
                    }
                }
                System.out.println("Total productos migrados: " + countInv);
            }

            System.out.println("\nProcesando EMPRESA...");
            String selEmp = "SELECT id_empresa, logo_empresa_ruta FROM EMPRESA WHERE logo_empresa_ruta IS NOT NULL AND logo_empresa_ruta != ''";
            String updEmp = "UPDATE EMPRESA SET imagen_logo = ? WHERE id_empresa = ?";
            
            try (PreparedStatement psSelEmp = con.prepareStatement(selEmp);
                 ResultSet rsEmp = psSelEmp.executeQuery();
                 PreparedStatement psUpdEmp = con.prepareStatement(updEmp)) {
                 
                int countEmp = 0;
                while (rsEmp.next()) {
                    int id = rsEmp.getInt("id_empresa");
                    String ruta = rsEmp.getString("logo_empresa_ruta");
                    File f = new File(ruta);
                    if (f.exists() && f.isFile()) {
                        byte[] bytes = Files.readAllBytes(f.toPath());
                        String base64 = Base64.getEncoder().encodeToString(bytes);
                        psUpdEmp.setString(1, base64);
                        psUpdEmp.setInt(2, id);
                        psUpdEmp.executeUpdate();
                        countEmp++;
                        System.out.println("Migrada empresa ID: " + id);
                    } else {
                        System.out.println("Archivo no encontrado para empresa ID: " + id + " -> " + ruta);
                    }
                }
                System.out.println("Total empresas migradas: " + countEmp);
            }
            
            System.out.println("\nEliminando columnas antiguas...");
            try (Statement st = con.createStatement()) {
                st.execute("ALTER TABLE INVENTARIO DROP COLUMN ruta_imagen_producto");
                System.out.println("Columna ruta_imagen_producto eliminada.");
            } catch(Exception e) { System.out.println("Error eliminando ruta_imagen_producto: " + e.getMessage()); }
            
            try (Statement st = con.createStatement()) {
                st.execute("ALTER TABLE EMPRESA DROP COLUMN logo_empresa_ruta");
                System.out.println("Columna logo_empresa_ruta eliminada.");
            } catch(Exception e) { System.out.println("Error eliminando logo_empresa_ruta: " + e.getMessage()); }
            
            System.out.println("--- MIGRACI\u00D3N COMPLETADA ---");
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
