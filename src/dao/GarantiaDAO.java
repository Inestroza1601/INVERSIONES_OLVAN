package dao;

import factory.ConexionFactory;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.text.SimpleDateFormat;

public class GarantiaDAO {
    private ConexionFactory factory;

    public GarantiaDAO() {
        this.factory = new ConexionFactory();
    }

    public List<Object[]> listarGarantias() {
        List<Object[]> lista = new ArrayList<>();
        
        String sql = "SELECT v.id_ventas, v.id_cliente_venta, c.nombre_cliente, c.apellido_cliente, " +
                     "d.id_detalle_venta, d.descripcion_venta, d.identificador_serie, v.fecha_venta, d.dias_garantia, " + 
                     "d.estado_garantia " +
                     "FROM DETALLES_VENTA d " +
                     "INNER JOIN VENTAS v ON d.id_ventas = v.id_ventas " +
                     "LEFT JOIN CLIENTES c ON v.id_cliente_venta = c.id_cliente " +
                     "WHERE d.dias_garantia > 0 " +
                     "ORDER BY v.fecha_venta DESC";

        try (Connection con = factory.getConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            Date hoy = new Date();

            while (rs.next()) {
                int idVenta = rs.getInt("id_ventas");
                int idCliente = rs.getInt("id_cliente_venta");
                
                // Formatear el nombre del cliente o Consumidor Final
                String nombreClie = rs.getString("nombre_cliente");
                if (rs.getString("apellido_cliente") != null) {
                    nombreClie += " " + rs.getString("apellido_cliente");
                }
                if (idCliente == 1 || nombreClie == null || nombreClie.trim().isEmpty()) {
                    nombreClie = "CONSUMIDOR FINAL";
                }

                String producto = rs.getString("descripcion_venta");
                String serie = rs.getString("identificador_serie");
                if (serie == null || serie.trim().isEmpty()) serie = "N/A";

                java.sql.Timestamp fechaVenta = rs.getTimestamp("fecha_venta");
                int diasGarantia = rs.getInt("dias_garantia");

                // Cálculo matemático de la fecha de vencimiento
                Calendar cal = Calendar.getInstance();
                cal.setTime(fechaVenta);
                cal.add(Calendar.DAY_OF_YEAR, diasGarantia);
                Date fechaVencimiento = cal.getTime();

                String estadoBD = rs.getString("estado_garantia"); // Leemos la BD
                String estado = "VENCIDA";
                
                if (estadoBD != null && estadoBD.equals("RECLAMADA")) {
                    estado = "RECLAMADA"; // Prioridad 1: Si ya se reclamó, se queda gris
                } else if (fechaVencimiento.after(hoy)) {
                    estado = "VIGENTE";   // Prioridad 2: Si no se ha reclamado y tiene tiempo, es verde
                }

                // Empaquetar la fila exacta como la espera el modelo de la tabla
                lista.add(new Object[]{
                    "Venta #" + idVenta,
                    nombreClie.trim(),
                    producto,
                    serie,
                    sdf.format(fechaVenta),
                    sdf.format(fechaVencimiento),
                    estado,
                    idVenta, // ID Venta Oculto
                    rs.getInt("id_detalle_venta") // <-- Ahora capturamos el ID real
                });
            }
        } catch (SQLException e) {
            System.err.println("Error listando garantías en Orion Systems: " + e.getMessage());
        }
        return lista;
    }
    
    public boolean aplicarReclamo(int idDetalleVenta) {
        // Ahora sí usamos la columna nueva y apuntamos a id_detalle_venta
        String sql = "UPDATE DETALLES_VENTA SET estado_garantia = 'RECLAMADA' WHERE id_detalle_venta = ?";
        
        try (Connection con = factory.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
             
            ps.setInt(1, idDetalleVenta);
            int filasAfectadas = ps.executeUpdate();
            
            return filasAfectadas > 0;
            
        } catch (SQLException e) {
            System.err.println("Error al aplicar reclamo de garantía: " + e.getMessage());
            return false;
        }
    }
    
}