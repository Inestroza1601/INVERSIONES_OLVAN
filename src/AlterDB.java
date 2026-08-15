import java.sql.*;
public class AlterDB {
    public static void main(String[] args) {
        try (Connection c = new factory.ConexionFactory().getConexion();
             Statement s = c.createStatement()) {
            try {
                s.execute("ALTER TABLE VENTAS ADD descuento_venta decimal(10,2) DEFAULT 0.0");
                System.out.println("Columna descuento_venta anadida a VENTAS");
            } catch (Exception e) {
                System.out.println("Puede que la columna ya exista en VENTAS: " + e.getMessage());
            }
            try {
                s.execute("ALTER TABLE DETALLES_VENTA ADD descuento_unitario decimal(10,2) DEFAULT 0.0");
                System.out.println("Columna descuento_unitario anadida a DETALLES_VENTA");
            } catch (Exception e) {
                System.out.println("Puede que la columna ya exista en DETALLES_VENTA: " + e.getMessage());
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}
