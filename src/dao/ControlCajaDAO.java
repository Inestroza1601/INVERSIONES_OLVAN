package dao;

import factory.ConexionFactory;
import modelo.ControlCaja;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ControlCajaDAO {
    private ConexionFactory factory;

    public ControlCajaDAO() {
        this.factory = new ConexionFactory();
    }

    public ControlCaja obtenerSesionActiva() {
        String sql = "SELECT c.*, u.nombre_usuario as nombre_usuario_apertura "
                   + "FROM CONTROL_CAJA c "
                   + "LEFT JOIN USUARIOS u ON c.id_usuario_apertura = u.id_usuario "
                   + "WHERE c.estado_caja = 'ABIERTA'";
        try (Connection con = factory.getConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
             
            if (rs.next()) {
                ControlCaja c = new ControlCaja();
                c.setIdCaja(rs.getInt("id_caja"));
                c.setFechaApertura(rs.getTimestamp("fecha_apertura"));
                c.setMontoApertura(rs.getDouble("monto_apertura"));
                c.setEstadoCaja(1); // Abierta
                c.setNombreUsuarioApertura(rs.getString("nombre_usuario_apertura"));
                c.setCajeroTurno(rs.getString("cajero_turno"));
                return c;
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener sesión activa de caja: " + e.getMessage());
        }
        return null;
    }

    public boolean existeCajaAbiertaAnterior() throws SQLException {
        String sql = "SELECT COUNT(*) FROM CONTROL_CAJA WHERE estado_caja = 'ABIERTA' AND CAST(fecha_apertura AS DATE) < CAST(GETDATE() AS DATE)";
        try (Connection con = factory.getConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        }
        return false;
    }

    public boolean abrirCaja(int idUsuario, double montoApertura, String cajeroTurno) {
        String sql = "INSERT INTO CONTROL_CAJA (id_usuario_apertura, fecha_apertura, monto_apertura, estado_caja, cajero_turno) "
                   + "VALUES (?, GETDATE(), ?, 'ABIERTA', ?)";
        try (Connection con = factory.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idUsuario);
            ps.setDouble(2, montoApertura);
            ps.setString(3, cajeroTurno.trim());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al abrir caja: " + e.getMessage());
            return false;
        }
    }

    public List<ControlCaja> listarHistoricos() {
        List<ControlCaja> lista = new ArrayList<>();
        String sql = "SELECT c.*, u.nombre_usuario as nombre_usuario_apertura "
                   + "FROM CONTROL_CAJA c "
                   + "LEFT JOIN USUARIOS u ON c.id_usuario_apertura = u.id_usuario "
                   + "ORDER BY c.fecha_apertura DESC";
        try (Connection con = factory.getConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
             
            while (rs.next()) {
                ControlCaja c = new ControlCaja();
                c.setIdCaja(rs.getInt("id_caja"));
                c.setFechaApertura(rs.getTimestamp("fecha_apertura"));
                c.setMontoApertura(rs.getDouble("monto_apertura"));
                c.setFechaCierre(rs.getTimestamp("fecha_cierre"));
                c.setMontoCierreEsperado(rs.getDouble("monto_cierre_esperado"));
                c.setMontoCierreReal(rs.getDouble("monto_cierre_real"));
                c.setDiferenciaCierre(rs.getDouble("diferencia_caja"));
                c.setObservaciones(rs.getString("observaciones"));
                c.setCajeroTurno(rs.getString("cajero_turno"));
                c.setIdUsuarioApertura(rs.getInt("id_usuario_apertura"));
                c.setEstadoCaja(rs.getString("estado_caja").equals("ABIERTA") ? 1 : 0);
                c.setNombreUsuarioApertura(rs.getString("nombre_usuario_apertura"));
                lista.add(c);
            }
        } catch (SQLException e) {
            System.err.println("Error al listar cajas históricas: " + e.getMessage());
        }
        return lista;
    }

    public Map<String, Object> obtenerCalculosTurno(int idCaja) {
        Map<String, Object> resultado = new HashMap<>();
        String sqlCaja = "SELECT c.*, u.nombre_usuario as nombre_usuario_apertura "
                       + "FROM CONTROL_CAJA c "
                       + "LEFT JOIN USUARIOS u ON c.id_usuario_apertura = u.id_usuario "
                       + "WHERE c.id_caja = ?";
                       
        try (Connection con = factory.getConexion();
             PreparedStatement psCaja = con.prepareStatement(sqlCaja)) {
             
            psCaja.setInt(1, idCaja);
            try (ResultSet rsCaja = psCaja.executeQuery()) {
                if (!rsCaja.next()) return null;
                
                Timestamp fechaInicio = rsCaja.getTimestamp("fecha_apertura");
                Timestamp fechaFin = rsCaja.getTimestamp("fecha_cierre") != null 
                                     ? rsCaja.getTimestamp("fecha_cierre") 
                                     : new Timestamp(System.currentTimeMillis());
                double montoApertura = rsCaja.getDouble("monto_apertura");

                // Buscar el ID del método de pago de efectivo
                int cashMethodId = 1;
                String sqlCashId = "SELECT id_metodo_pago FROM METODOS_PAGO WHERE nombre_metodo LIKE '%efectivo%'";
                try (PreparedStatement psCash = con.prepareStatement(sqlCashId);
                     ResultSet rsCash = psCash.executeQuery()) {
                    if (rsCash.next()) {
                        cashMethodId = rsCash.getInt("id_metodo_pago");
                    }
                }

                // 1. Ventas por método
                String sqlSales = "SELECT id_metodo_pago, SUM(total_venta) as total_metodo, COUNT(id_ventas) as transacciones "
                                + "FROM VENTAS "
                                + "WHERE fecha_venta >= ? AND fecha_venta <= ? "
                                + "AND (referencia_pago NOT LIKE 'Pago de Apartado%' OR referencia_pago IS NULL) "
                                + "GROUP BY id_metodo_pago";
                Map<Integer, Double> ventasTotales = new HashMap<>();
                Map<Integer, Integer> ventasCount = new HashMap<>();
                try (PreparedStatement psSales = con.prepareStatement(sqlSales)) {
                    psSales.setTimestamp(1, fechaInicio);
                    psSales.setTimestamp(2, fechaFin);
                    try (ResultSet rsSales = psSales.executeQuery()) {
                        while (rsSales.next()) {
                            ventasTotales.put(rsSales.getInt("id_metodo_pago"), rsSales.getDouble("total_metodo"));
                            ventasCount.put(rsSales.getInt("id_metodo_pago"), rsSales.getInt("transacciones"));
                        }
                    }
                }

                // 2. Abonos por método
                String sqlAbonos = "SELECT id_metodo_pago, SUM(monto_abono) as total_metodo, COUNT(id_abono) as transacciones "
                                 + "FROM ABONOS_APARTADO "
                                 + "WHERE fecha_abono >= ? AND fecha_abono <= ? "
                                 + "GROUP BY id_metodo_pago";
                Map<Integer, Double> abonosTotales = new HashMap<>();
                Map<Integer, Integer> abonosCount = new HashMap<>();
                try (PreparedStatement psAbonos = con.prepareStatement(sqlAbonos)) {
                    psAbonos.setTimestamp(1, fechaInicio);
                    psAbonos.setTimestamp(2, fechaFin);
                    try (ResultSet rsAbonos = psAbonos.executeQuery()) {
                        while (rsAbonos.next()) {
                            abonosTotales.put(rsAbonos.getInt("id_metodo_pago"), rsAbonos.getDouble("total_metodo"));
                            abonosCount.put(rsAbonos.getInt("id_metodo_pago"), rsAbonos.getInt("transacciones"));
                        }
                    }
                }

                // Cruzar con métodos de pago disponibles
                String sqlMethods = "SELECT id_metodo_pago, nombre_metodo FROM METODOS_PAGO";
                List<Map<String, Object>> reportMethods = new ArrayList<>();
                double totalSalesCash = 0.0;
                double totalAbonosCash = 0.0;
                double totalSalesAll = 0.0;
                double totalAbonosAll = 0.0;

                try (PreparedStatement psMethods = con.prepareStatement(sqlMethods);
                     ResultSet rsMethods = psMethods.executeQuery()) {
                    while (rsMethods.next()) {
                        int mId = rsMethods.getInt("id_metodo_pago");
                        String mNombre = rsMethods.getString("nombre_metodo");

                        double sTotal = ventasTotales.getOrDefault(mId, 0.0);
                        double aTotal = abonosTotales.getOrDefault(mId, 0.0);
                        int sCount = ventasCount.getOrDefault(mId, 0);
                        int aCount = abonosCount.getOrDefault(mId, 0);

                        totalSalesAll += sTotal;
                        totalAbonosAll += aTotal;

                        if (mId == cashMethodId) {
                            totalSalesCash = sTotal;
                            totalAbonosCash = aTotal;
                        }

                        Map<String, Object> item = new HashMap<>();
                        item.put("id_metodo", mId);
                        item.put("nombre_metodo", mNombre);
                        item.put("total_ventas", sTotal);
                        item.put("ventas_count", sCount);
                        item.put("total_abonos", aTotal);
                        item.put("abonos_count", aCount);
                        item.put("total_general", sTotal + aTotal);
                        reportMethods.add(item);
                    }
                }

                double efectivoEsperado = montoApertura + totalSalesCash + totalAbonosCash;

                // 3. Productos vendidos
                String sqlProducts = "SELECT p.codigo_barras_producto, d.descripcion_venta, SUM(d.cantidad_venta) as cantidad_vendida, SUM(d.subtotal_venta) as total_valor "
                                   + "FROM DETALLES_VENTA d "
                                   + "INNER JOIN VENTAS v ON d.id_ventas = v.id_ventas "
                                   + "LEFT JOIN INVENTARIO p ON d.id_producto = p.id_producto "
                                   + "WHERE v.fecha_venta >= ? AND v.fecha_venta <= ? "
                                   + "AND (v.referencia_pago NOT LIKE 'Pago de Apartado%' OR v.referencia_pago IS NULL) "
                                   + "GROUP BY p.codigo_barras_producto, d.descripcion_venta "
                                   + "ORDER BY cantidad_vendida DESC";
                List<Map<String, Object>> productosVendidos = new ArrayList<>();
                try (PreparedStatement psProducts = con.prepareStatement(sqlProducts)) {
                    psProducts.setTimestamp(1, fechaInicio);
                    psProducts.setTimestamp(2, fechaFin);
                    try (ResultSet rsProducts = psProducts.executeQuery()) {
                        while (rsProducts.next()) {
                            Map<String, Object> p = new HashMap<>();
                            p.put("codigo_barras", rsProducts.getString("codigo_barras_producto"));
                            p.put("descripcion", rsProducts.getString("descripcion_venta"));
                            p.put("cantidad", rsProducts.getInt("cantidad_vendida"));
                            p.put("total_valor", rsProducts.getDouble("total_valor"));
                            productosVendidos.add(p);
                        }
                    }
                }

                resultado.put("fecha_apertura", fechaInicio);
                resultado.put("fecha_cierre", rsCaja.getTimestamp("fecha_cierre"));
                resultado.put("monto_apertura", montoApertura);
                resultado.put("cajero_turno", rsCaja.getString("cajero_turno"));
                resultado.put("nombre_usuario_apertura", rsCaja.getString("nombre_usuario_apertura"));
                if (rsCaja.getObject("monto_cierre_real") != null) {
                    resultado.put("monto_cierre_real", rsCaja.getDouble("monto_cierre_real"));
                }
                resultado.put("observaciones", rsCaja.getString("observaciones"));
                resultado.put("metodos", reportMethods);
                resultado.put("efectivo_esperado", efectivoEsperado);
                resultado.put("total_ventas_general", totalSalesAll);
                resultado.put("total_abonos_general", totalAbonosAll);
                resultado.put("productos_vendidos", productosVendidos);
            }
        } catch (SQLException e) {
            System.err.println("Error al calcular arqueo de caja: " + e.getMessage());
            return null;
        }
        return resultado;
    }

    public boolean cerrarCaja(int idCaja, double montoReal, String observaciones, int idUsuarioCierre) {
        Map<String, Object> calcs = obtenerCalculosTurno(idCaja);
        if (calcs == null) return false;

        double esperado = (double) calcs.get("efectivo_esperado");
        double diferencia = montoReal - esperado;

        String sql = "UPDATE CONTROL_CAJA "
                   + "SET fecha_cierre = GETDATE(), "
                   + "    monto_cierre_esperado = ?, "
                   + "    monto_cierre_real = ?, "
                   + "    diferencia_caja = ?, "
                   + "    estado_caja = 'CERRADA', "
                   + "    observaciones = ? "
                   + "WHERE id_caja = ? AND estado_caja = 'ABIERTA'";
                   
        try (Connection con = factory.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setDouble(1, esperado);
            ps.setDouble(2, montoReal);
            ps.setDouble(3, diferencia);
            ps.setString(4, observaciones.trim());
            ps.setInt(5, idCaja);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al cerrar caja: " + e.getMessage());
            return false;
        }
    }
}
