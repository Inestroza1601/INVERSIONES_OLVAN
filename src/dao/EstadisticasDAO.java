package dao;

import factory.ConexionFactory;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class EstadisticasDAO {
    private ConexionFactory factory;

    public EstadisticasDAO() {
        this.factory = new ConexionFactory();
    }

    public Object[] obtenerDatos(String filtro) {
        double totalActual = 0;
        double totalAnterior = 0;
        double ticketProm = 0;
        String topProducto = "Sin registros";

        String condicionActual = "";
        String condicionAnterior = "";

        // Generamos las condiciones SQL din\u00E1micamente seg\u00FAn el filtro seleccionado
        switch (filtro) {
            case "D\u00EDa":
                condicionActual = "CAST(fecha_venta AS DATE) = CAST(GETDATE() AS DATE)";
                condicionAnterior = "CAST(fecha_venta AS DATE) = CAST(DATEADD(day, -1, GETDATE()) AS DATE)";
                break;
            case "Semana":
                condicionActual = "DATEPART(week, fecha_venta) = DATEPART(week, GETDATE()) AND YEAR(fecha_venta) = YEAR(GETDATE())";
                condicionAnterior = "DATEPART(week, fecha_venta) = DATEPART(week, DATEADD(week, -1, GETDATE())) AND YEAR(fecha_venta) = YEAR(DATEADD(week, -1, GETDATE()))";
                break;
            case "Mes":
                condicionActual = "MONTH(fecha_venta) = MONTH(GETDATE()) AND YEAR(fecha_venta) = YEAR(GETDATE())";
                condicionAnterior = "MONTH(fecha_venta) = MONTH(DATEADD(month, -1, GETDATE())) AND YEAR(fecha_venta) = YEAR(DATEADD(month, -1, GETDATE()))";
                break;
            case "A\u00F1o":
                condicionActual = "YEAR(fecha_venta) = YEAR(GETDATE())";
                condicionAnterior = "YEAR(fecha_venta) = YEAR(GETDATE()) - 1";
                break;
        }

        try (Connection con = factory.getConexion()) {
            // 1. Extraer Total Actual y Ticket Promedio (Misma tabla)
            String sqlActual = "SELECT SUM(total_venta) as total, AVG(total_venta) as promedio FROM VENTAS WHERE " + condicionActual;
            try (PreparedStatement ps = con.prepareStatement(sqlActual); ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    totalActual = rs.getDouble("total");
                    ticketProm = rs.getDouble("promedio");
                }
            }

            // 2. Extraer Total del Per\u00EDodo Anterior
            String sqlAnterior = "SELECT SUM(total_venta) as total FROM VENTAS WHERE " + condicionAnterior;
            try (PreparedStatement ps = con.prepareStatement(sqlAnterior); ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    totalAnterior = rs.getDouble("total");
                }
            }

            // 3. Extraer Top Producto cruzando Detalles y Ventas
            String sqlTop = "SELECT TOP 1 d.descripcion_venta FROM DETALLES_VENTA d " +
                            "INNER JOIN VENTAS v ON d.id_ventas = v.id_ventas " +
                            "WHERE " + condicionActual + " " +
                            "GROUP BY d.descripcion_venta " +
                            "ORDER BY COUNT(*) DESC";
            try (PreparedStatement ps = con.prepareStatement(sqlTop); ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    topProducto = rs.getString("descripcion_venta");
                }
            }

        } catch (Exception e) {
            System.err.println("Error conectando m\u00E9tricas de Orion Systems: " + e.getMessage());
        }

        // Empaquetamos todo en un arreglo para mandarlo al panel visual
        return new Object[]{totalActual, totalAnterior, ticketProm, topProducto};
    }
}
