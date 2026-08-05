package dao;

import factory.ConexionFactory;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class ReportesDAO {

    private ConexionFactory factory;

    public ReportesDAO() {
        this.factory = new ConexionFactory();
    }

    // 1. Reporte de Caja Diario
    public List<Object[]> obtenerReporteCajaDiario(String fecha) {
        List<Object[]> lista = new ArrayList<>();
        String sql = "SELECT id_caja, fecha_apertura, monto_apertura, monto_cierre_esperado, monto_cierre_real, diferencia_caja, estado_caja " +
                     "FROM CONTROL_CAJA WHERE CAST(fecha_apertura AS DATE) = ?";
        try (Connection con = factory.getConexion(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, fecha);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(new Object[]{
                        rs.getInt("id_caja"),
                        rs.getString("fecha_apertura"),
                        rs.getDouble("monto_apertura"),
                        rs.getDouble("monto_cierre_esperado"),
                        rs.getDouble("monto_cierre_real"),
                        rs.getDouble("diferencia_caja"),
                        rs.getString("estado_caja")
                    });
                }
            }
        } catch (Exception e) {
            System.err.println("Error en obtenerReporteCajaDiario: " + e.getMessage());
        }
        return lista;
    }

    // 2. Reporte Detallado de Ventas (Rango de Fechas)
    public List<Object[]> obtenerReporteDetalladoVentas(String fechaInicio, String fechaFin) {
        List<Object[]> lista = new ArrayList<>();
        String sql = "SELECT CAST(v.fecha_venta AS DATE) as fecha, v.id_ventas, " +
                     "CASE WHEN v.referencia_pago LIKE 'Pago de Apartado%' THEN 'Apartado' ELSE 'Venta Directa' END as tipo_venta, " +
                     "p.nombre_producto, d.cantidad_venta, (d.cantidad_venta * d.precio_unitario_venta) as subtotal " +
                     "FROM VENTAS v " +
                     "INNER JOIN DETALLES_VENTA d ON v.id_ventas = d.id_ventas " +
                     "LEFT JOIN INVENTARIO p ON d.id_producto = p.id_producto " +
                     "WHERE CAST(v.fecha_venta AS DATE) >= ? AND CAST(v.fecha_venta AS DATE) <= ? " +
                     "ORDER BY v.fecha_venta DESC";
        try (Connection con = factory.getConexion(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, fechaInicio);
            ps.setString(2, fechaFin);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(new Object[]{
                        rs.getString("fecha"),
                        rs.getInt("id_ventas"),
                        rs.getString("tipo_venta"),
                        rs.getString("nombre_producto") != null ? rs.getString("nombre_producto") : "Producto Desconocido",
                        rs.getInt("cantidad_venta"),
                        rs.getDouble("subtotal")
                    });
                }
            }
        } catch (Exception e) {
            System.err.println("Error en obtenerReporteDetalladoVentas: " + e.getMessage());
        }
        return lista;
    }

    // 3. Reporte Dinámico de Inventario
    public List<Object[]> obtenerReporteInventario() {
        List<Object[]> lista = new ArrayList<>();
        String sql = "SELECT codigo_barras_producto, nombre_producto, stock_producto, precio_venta_producto " +
                     "FROM INVENTARIO WHERE eliminado_producto = 0 ORDER BY nombre_producto";
        try (Connection con = factory.getConexion(); PreparedStatement ps = con.prepareStatement(sql)) {
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(new Object[]{
                        rs.getString("codigo_barras_producto"),
                        rs.getString("nombre_producto"),
                        rs.getInt("stock_producto"),
                        rs.getDouble("precio_venta_producto")
                    });
                }
            }
        } catch (Exception e) {
            System.err.println("Error en obtenerReporteInventario: " + e.getMessage());
        }
        return lista;
    }

    // 4. Alertas (Stock Bajo o Estancado > 15 días)
    public List<Object[]> obtenerAlertasStockYEstancados() {
        List<Object[]> lista = new ArrayList<>();
        // Se buscan productos con stock menor o igual al mínimo, o que su última fecha en el kardex (venta/salida) fue hace más de 15 días
        String sql = "SELECT i.codigo_barras_producto, i.nombre_producto, i.stock_producto, i.stock_minimo_producto, " +
                     "(SELECT MAX(fecha_movimiento_producto) FROM KARDEX k WHERE k.id_producto = i.id_producto AND tipo_movimiento_producto = 'Salida') as ultima_venta " +
                     "FROM INVENTARIO i " +
                     "WHERE i.eliminado_producto = 0 " +
                     "AND (i.stock_producto <= i.stock_minimo_producto OR " +
                     "     DATEDIFF(day, (SELECT MAX(fecha_movimiento_producto) FROM KARDEX k WHERE k.id_producto = i.id_producto AND tipo_movimiento_producto = 'Salida'), GETDATE()) >= 15 " +
                     "     OR (SELECT COUNT(*) FROM KARDEX k WHERE k.id_producto = i.id_producto AND tipo_movimiento_producto = 'Salida') = 0)";
                     
        try (Connection con = factory.getConexion(); PreparedStatement ps = con.prepareStatement(sql)) {
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int stock = rs.getInt("stock_producto");
                    int minimo = rs.getInt("stock_minimo_producto");
                    String ultimaVenta = rs.getString("ultima_venta");
                    String motivo = "";
                    
                    if (stock <= minimo) {
                        motivo = "Stock Bajo (" + stock + "/" + minimo + ")";
                    } else {
                        motivo = "Sin movimiento > 15 días";
                    }
                    
                    lista.add(new Object[]{
                        rs.getString("codigo_barras_producto"),
                        rs.getString("nombre_producto"),
                        stock,
                        ultimaVenta == null ? "Nunca vendido" : ultimaVenta,
                        motivo
                    });
                }
            }
        } catch (Exception e) {
            System.err.println("Error en obtenerAlertasStockYEstancados: " + e.getMessage());
        }
        return lista;
    }
}
