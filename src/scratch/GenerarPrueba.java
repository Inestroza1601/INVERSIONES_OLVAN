package scratch;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

public class GenerarPrueba {
    public static void main(String[] args) {
        String url = "jdbc:sqlserver://170.80.140.2:6161;databaseName=NexarBD;encrypt=true;trustServerCertificate=true;";
        String usuario = "orionsys";
        String password = "123";
        
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            try (Connection con = DriverManager.getConnection(url, usuario, password)) {
                // First, find an existing category, provider and location
                int idCategoria = 1;
                int idProveedor = 1;
                int idUbicacion = 1;
                
                try (Statement st = con.createStatement()) {
                    ResultSet rs = st.executeQuery("SELECT TOP 1 id_categoria FROM CATEGORIAS");
                    if (rs.next()) idCategoria = rs.getInt(1);
                    
                    rs = st.executeQuery("SELECT TOP 1 id_proveedor FROM PROVEEDORES");
                    if (rs.next()) idProveedor = rs.getInt(1);
                    
                    rs = st.executeQuery("SELECT TOP 1 id_ubicacion FROM UBICACIONES");
                    if (rs.next()) idUbicacion = rs.getInt(1);
                }
                
                String sql = "INSERT INTO INVENTARIO (codigo_barras_producto, nombre_producto, id_categoria, id_proveedor, "
                       + "id_ubicacion, precio_compra_producto, precio_venta_producto, precio_mayorista_producto, "
                       + "stock_minimo_producto, stock_producto, ruta_imagen_producto, dias_garantia, requiere_serie, incluye_impuesto, eliminado_producto) "
                       + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 0)";
                       
                try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                    for (int i = 1; i <= 50; i++) {
                        String codigoBarras = "PROD-TEST-" + System.currentTimeMillis() + "-" + i;
                        ps.setString(1, codigoBarras);
                        ps.setString(2, "Producto de Prueba " + i);
                        ps.setInt(3, idCategoria);
                        ps.setInt(4, idProveedor);
                        ps.setInt(5, idUbicacion);
                        ps.setDouble(6, 10.0 + i);
                        ps.setDouble(7, 20.0 + i * 2);
                        ps.setNull(8, java.sql.Types.DECIMAL); // precio_mayorista
                        ps.setInt(9, 5); // stock minimo
                        ps.setInt(10, 50); // stock
                        
                        // URL aleatoria de imagen
                        String randomImageUrl = "https://picsum.photos/seed/" + (System.currentTimeMillis() + i) + "/400/400";
                        ps.setString(11, randomImageUrl);
                        
                        ps.setInt(12, 30); // dias_garantia
                        ps.setBoolean(13, false); // requiere_serie
                        ps.setBoolean(14, true); // incluye_impuesto
                        
                        ps.executeUpdate();
                        
                        try (ResultSet rs = ps.getGeneratedKeys()) {
                            if (rs.next()) {
                                int idGenerado = rs.getInt(1);
                                System.out.println("Insertado producto ID: " + idGenerado + " con URL: " + randomImageUrl);
                                
                                // Update barcode if it needs to match ID (like the DAO sometimes does), but here we set it explicitly
                            }
                        }
                    }
                }
                System.out.println("¡50 productos insertados correctamente!");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
