package dao;

import factory.ConexionFactory;
import modelo.Apartado;
import modelo.DetalleApartado;
import modelo.AbonoApartado;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ApartadoDAO {
    private ConexionFactory factory;

    public ApartadoDAO() {
        this.factory = new ConexionFactory();
    }

    public boolean registrarApartado(Apartado a, List<DetalleApartado> detalles) {
        String sqlApartado = "INSERT INTO APARTADOS (id_cliente_apartado, id_usuario, fecha_apartado, total_apartado, saldo_pendiente, estado_apartado, fecha_entrega) "
                + "VALUES (?, ?, GETDATE(), ?, ?, ?, NULL)";

        String sqlDetalle = "INSERT INTO DETALLES_APARTADO (id_apartado, id_producto, descripcion_apartado, cantidad_apartado, precio_unitario_apartado, subtotal_apartado, identificador_serie) VALUES (?, ?, ?, ?, ?, ?, ?)";

        String sqlAbono = "INSERT INTO ABONOS_APARTADO (id_apartado, id_usuario, id_metodo_pago, fecha_abono, monto_abono, referencia_pago, banco_pago) "
                + "VALUES (?, ?, ?, GETDATE(), ?, ?, ?)";

        String sqlStockActual = "SELECT stock_producto FROM INVENTARIO WHERE id_producto = ?";
        String sqlStockUpdate = "UPDATE INVENTARIO SET stock_producto = ? WHERE id_producto = ?";
        String sqlKardex = "INSERT INTO KARDEX (id_producto, id_usuario, fecha_movimiento_producto, tipo_movimiento_producto, cantidad_producto, stock_restante_producto, referencia_producto) "
                + "VALUES (?, ?, GETDATE(), 'Salida', ?, ?, ?)";

        Connection con = null;
        try {
            con = factory.getConexion();
            con.setAutoCommit(false);

            int idApartado = 0;
            double saldoPendiente = a.getTotalApartado() - a.getAbonoInicial();
            if (saldoPendiente < 0)
                saldoPendiente = 0;
            String estado = (saldoPendiente <= 0) ? "PAGADO" : "VIGENTE";

            try (PreparedStatement psApartado = con.prepareStatement(sqlApartado, Statement.RETURN_GENERATED_KEYS)) {
                psApartado.setInt(1, a.getIdClienteApartado());
                psApartado.setInt(2, a.getIdUsuario());
                psApartado.setDouble(3, a.getTotalApartado());
                psApartado.setDouble(4, saldoPendiente);
                psApartado.setString(5, estado);
                psApartado.executeUpdate();

                try (ResultSet rsKeys = psApartado.getGeneratedKeys()) {
                    if (rsKeys.next()) {
                        idApartado = rsKeys.getInt(1);
                    }
                }
            }

            if (idApartado == 0) {
                con.rollback();
                return false;
            }

            // Registrar el abono inicial en la tabla ABONOS_APARTADO si es mayor a 0
            if (a.getAbonoInicial() > 0) {
                try (PreparedStatement psAbono = con.prepareStatement(sqlAbono)) {
                    psAbono.setInt(1, idApartado);
                    psAbono.setInt(2, a.getIdUsuario());
                    psAbono.setInt(3, a.getIdMetodoPago() > 0 ? a.getIdMetodoPago() : 1);
                    psAbono.setDouble(4, a.getAbonoInicial());
                    psAbono.setString(5, a.getReferenciaPago());
                    psAbono.setString(6, a.getBancoPago());
                    psAbono.executeUpdate();
                }
            }

            try (PreparedStatement psDetalle = con.prepareStatement(sqlDetalle);
                    PreparedStatement psStockActual = con.prepareStatement(sqlStockActual);
                    PreparedStatement psStockUpdate = con.prepareStatement(sqlStockUpdate);
                    PreparedStatement psKardex = con.prepareStatement(sqlKardex)) {

                for (DetalleApartado d : detalles) {
                    psDetalle.setInt(1, idApartado);
                    psDetalle.setInt(2, d.getIdProducto());
                    psDetalle.setString(3, d.getDescripcionApartado());
                    psDetalle.setInt(4, d.getCantidadApartado());
                    psDetalle.setDouble(5, d.getPrecioUnitarioApartado());
                    psDetalle.setDouble(6, d.getSubtotalApartado());
                    psDetalle.setString(7, d.getIdentificadorSerie());
                    psDetalle.executeUpdate();

                    // Restar del Stock y registrar en Kardex
                    psStockActual.setInt(1, d.getIdProducto());
                    int stock = 0;
                    try (ResultSet rsStock = psStockActual.executeQuery()) {
                        if (rsStock.next()) {
                            stock = rsStock.getInt("stock_producto");
                        }
                    }

                    int stockRestante = stock - d.getCantidadApartado();
                    if (stockRestante < 0) {
                        con.rollback();
                        throw new SQLException("Stock negativo no permitido para: " + d.getNombreProducto());
                    }
                    psStockUpdate.setInt(1, stockRestante);
                    psStockUpdate.setInt(2, d.getIdProducto());
                    psStockUpdate.executeUpdate();

                    psKardex.setInt(1, d.getIdProducto());
                    psKardex.setInt(2, a.getIdUsuario());
                    psKardex.setInt(3, d.getCantidadApartado());
                    psKardex.setInt(4, stockRestante);
                    psKardex.setString(5, "Apartado #" + idApartado);
                    psKardex.executeUpdate();
                }
            }

            con.commit();
            return true;
        } catch (SQLException e) {
            if (con != null)
                try {
                    con.rollback();
                } catch (SQLException ex) {
                }
            System.err.println("Error al registrar apartado: " + e.getMessage());
            return false;
        } finally {
            if (con != null)
                try {
                    con.setAutoCommit(true);
                    con.close();
                } catch (SQLException e) {
                }
        }
    }

    public List<Apartado> listarApartados() {
        List<Apartado> lista = new ArrayList<>();
        String sql = "SELECT a.*, c.nombre_cliente, c.apellido_cliente, c.identidad_cliente, u.nombre_usuario "
                + "FROM APARTADOS a "
                + "LEFT JOIN CLIENTES c ON a.id_cliente_apartado = c.id_cliente "
                + "LEFT JOIN USUARIOS u ON a.id_usuario = u.id_usuario "
                + "ORDER BY a.fecha_apartado DESC";
        try (Connection con = factory.getConexion();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Apartado a = new Apartado();
                a.setIdApartado(rs.getInt("id_apartado"));
                a.setFechaApartado(rs.getTimestamp("fecha_apartado"));
                if (a.getFechaApartado() != null) {
                    long treintaDias = 30L * 24L * 60L * 60L * 1000L;
                    a.setFechaLimite(new java.sql.Timestamp(a.getFechaApartado().getTime() + treintaDias));
                }
                a.setTotalApartado(rs.getDouble("total_apartado"));
                a.setSaldoPendiente(rs.getDouble("saldo_pendiente"));
                a.setIdClienteApartado(rs.getInt("id_cliente_apartado"));
                a.setIdUsuario(rs.getInt("id_usuario"));
                a.setEstadoApartado(rs.getString("estado_apartado"));
                a.setFechaEntrega(rs.getTimestamp("fecha_entrega"));
                a.setNombreCliente(rs.getString("nombre_cliente"));
                a.setApellidoCliente(rs.getString("apellido_cliente"));
                a.setIdentidadCliente(rs.getString("identidad_cliente"));
                a.setNombreUsuario(rs.getString("nombre_usuario"));
                lista.add(a);
            }
        } catch (SQLException e) {
            System.err.println("Error al listar apartados: " + e.getMessage());
        }
        return lista;
    }

    public Apartado obtenerPorId(int idApartado) {
        String sql = "SELECT a.*, c.nombre_cliente, c.apellido_cliente, u.nombre_usuario "
                + "FROM APARTADOS a "
                + "LEFT JOIN CLIENTES c ON a.id_cliente_apartado = c.id_cliente "
                + "LEFT JOIN USUARIOS u ON a.id_usuario = u.id_usuario "
                + "WHERE a.id_apartado = ?";
        try (Connection con = factory.getConexion();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idApartado);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Apartado a = new Apartado();
                    a.setIdApartado(rs.getInt("id_apartado"));
                    a.setFechaApartado(rs.getTimestamp("fecha_apartado"));
                    if (a.getFechaApartado() != null) {
                        long treintaDias = 30L * 24L * 60L * 60L * 1000L;
                        a.setFechaLimite(new java.sql.Timestamp(a.getFechaApartado().getTime() + treintaDias));
                    }
                    a.setTotalApartado(rs.getDouble("total_apartado"));
                    a.setSaldoPendiente(rs.getDouble("saldo_pendiente"));
                    a.setIdClienteApartado(rs.getInt("id_cliente_apartado"));
                    a.setIdUsuario(rs.getInt("id_usuario"));
                    a.setEstadoApartado(rs.getString("estado_apartado"));
                    a.setFechaEntrega(rs.getTimestamp("fecha_entrega"));
                    a.setNombreCliente(rs.getString("nombre_cliente"));
                    a.setApellidoCliente(rs.getString("apellido_cliente"));
                    a.setNombreUsuario(rs.getString("nombre_usuario"));
                    return a;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener apartado por ID: " + e.getMessage());
        }
        return null;
    }

    public List<DetalleApartado> listarDetalles(int idApartado) {
        List<DetalleApartado> lista = new ArrayList<>();
        String sql = "SELECT d.*, p.codigo_barras_producto, p.nombre_producto "
                + "FROM DETALLES_APARTADO d "
                + "LEFT JOIN INVENTARIO p ON d.id_producto = p.id_producto "
                + "WHERE d.id_apartado = ?";
        try (Connection con = factory.getConexion();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idApartado);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    DetalleApartado d = new DetalleApartado();
                    d.setIdDetalleApartado(rs.getInt("id_detalle_apartado"));
                    d.setIdApartado(rs.getInt("id_apartado"));
                    d.setIdProducto(rs.getInt("id_producto"));
                    d.setDescripcionApartado(rs.getString("descripcion_apartado"));
                    d.setCantidadApartado(rs.getInt("cantidad_apartado"));
                    d.setPrecioUnitarioApartado(rs.getDouble("precio_unitario_apartado"));
                    d.setSubtotalApartado(rs.getDouble("subtotal_apartado"));
                    d.setIdentificadorSerie(rs.getString("identificador_serie"));
                    d.setCodigoBarras(rs.getString("codigo_barras_producto"));
                    d.setNombreProducto(rs.getString("nombre_producto"));
                    lista.add(d);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al listar detalles del apartado: " + e.getMessage());
        }
        return lista;
    }

    public List<AbonoApartado> listarAbonos(int idApartado) {
        List<AbonoApartado> lista = new ArrayList<>();
        String sql = "SELECT ab.*, m.nombre_metodo, u.nombre_usuario "
                + "FROM ABONOS_APARTADO ab "
                + "LEFT JOIN METODOS_PAGO m ON ab.id_metodo_pago = m.id_metodo_pago "
                + "LEFT JOIN USUARIOS u ON ab.id_usuario = u.id_usuario "
                + "WHERE ab.id_apartado = ? "
                + "ORDER BY ab.fecha_abono ASC";
        try (Connection con = factory.getConexion();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idApartado);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    AbonoApartado a = new AbonoApartado();
                    a.setIdAbono(rs.getInt("id_abono"));
                    a.setIdApartado(rs.getInt("id_apartado"));
                    a.setFechaAbono(rs.getTimestamp("fecha_abono"));
                    a.setMontoAbono(rs.getDouble("monto_abono"));
                    a.setIdMetodoPago(rs.getInt("id_metodo_pago"));
                    a.setIdUsuario(rs.getInt("id_usuario"));
                    a.setReferenciaPago(rs.getString("referencia_pago"));
                    a.setBancoPago(rs.getString("banco_pago"));
                    a.setNombreMetodo(rs.getString("nombre_metodo"));
                    a.setNombreUsuario(rs.getString("nombre_usuario"));
                    lista.add(a);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al listar abonos del apartado: " + e.getMessage());
        }
        return lista;
    }

    public boolean registrarAbono(int idApartado, double monto, int idMetodo, int idUsuario, String referencia,
            String banco) {
        String sqlAbono = "INSERT INTO ABONOS_APARTADO (id_apartado, id_usuario, id_metodo_pago, fecha_abono, monto_abono, referencia_pago, banco_pago) "
                + "VALUES (?, ?, ?, GETDATE(), ?, ?, ?)";
        String sqlApartado = "SELECT saldo_pendiente, total_apartado FROM APARTADOS WHERE id_apartado = ?";
        String sqlUpdate = "UPDATE APARTADOS SET saldo_pendiente = ?, estado_apartado = ? WHERE id_apartado = ?";

        Connection con = null;
        try {
            con = factory.getConexion();
            con.setAutoCommit(false);

            double saldo = 0;
            try (PreparedStatement psAp = con.prepareStatement(sqlApartado)) {
                psAp.setInt(1, idApartado);
                try (ResultSet rs = psAp.executeQuery()) {
                    if (rs.next()) {
                        saldo = rs.getDouble("saldo_pendiente");
                    }
                }
            }

            double nuevoSaldo = saldo - monto;
            if (nuevoSaldo < 0)
                nuevoSaldo = 0;
            String nuevoEstado = (nuevoSaldo <= 0.05) ? "PAGADO" : "VIGENTE";

            // Insertar abono
            try (PreparedStatement psAb = con.prepareStatement(sqlAbono)) {
                psAb.setInt(1, idApartado);
                psAb.setInt(2, idUsuario);
                psAb.setInt(3, idMetodo);
                psAb.setDouble(4, monto);
                psAb.setString(5, referencia);
                psAb.setString(6, banco);
                psAb.executeUpdate();
            }

            // Actualizar saldo y estado
            try (PreparedStatement psUp = con.prepareStatement(sqlUpdate)) {
                psUp.setDouble(1, nuevoSaldo);
                psUp.setString(2, nuevoEstado);
                psUp.setInt(3, idApartado);
                psUp.executeUpdate();
            }

            con.commit();
            return true;
        } catch (SQLException e) {
            if (con != null)
                try {
                    con.rollback();
                } catch (SQLException ex) {
                }
            System.err.println("Error al registrar abono: " + e.getMessage());
            return false;
        } finally {
            if (con != null)
                try {
                    con.setAutoCommit(true);
                    con.close();
                } catch (SQLException e) {
                }
        }
    }

    public boolean cancelarApartado(int idApartado, int idUsuarioFirma) {
        String sqlEstado = "SELECT estado_apartado FROM APARTADOS WHERE id_apartado = ?";
        String sqlUpdate = "UPDATE APARTADOS SET estado_apartado = 'CANCELADO', saldo_pendiente = 0 WHERE id_apartado = ?";
        String sqlItems = "SELECT id_producto, cantidad_apartado FROM DETALLES_APARTADO WHERE id_apartado = ?";
        String sqlStock = "SELECT stock_producto FROM INVENTARIO WHERE id_producto = ?";
        String sqlStockUpdate = "UPDATE INVENTARIO SET stock_producto = ? WHERE id_producto = ?";
        String sqlKardex = "INSERT INTO KARDEX (id_producto, id_usuario, fecha_movimiento_producto, tipo_movimiento_producto, cantidad_producto, stock_restante_producto, referencia_producto) "
                + "VALUES (?, ?, GETDATE(), 'Entrada', ?, ?, ?)";

        Connection con = null;
        try {
            con = factory.getConexion();
            con.setAutoCommit(false);

            String estado = "";
            try (PreparedStatement psEs = con.prepareStatement(sqlEstado)) {
                psEs.setInt(1, idApartado);
                try (ResultSet rs = psEs.executeQuery()) {
                    if (rs.next())
                        estado = rs.getString("estado_apartado");
                }
            }

            if ("CANCELADO".equals(estado) || "ENTREGADO".equals(estado)) {
                con.rollback();
                return false;
            }

            // Cancelar apartado
            try (PreparedStatement psUp = con.prepareStatement(sqlUpdate)) {
                psUp.setInt(1, idApartado);
                psUp.executeUpdate();
            }

            // Devolver mercancías a inventario
            try (PreparedStatement psItems = con.prepareStatement(sqlItems);
                    PreparedStatement psStock = con.prepareStatement(sqlStock);
                    PreparedStatement psStockUp = con.prepareStatement(sqlStockUpdate);
                    PreparedStatement psKardex = con.prepareStatement(sqlKardex)) {

                psItems.setInt(1, idApartado);
                try (ResultSet rsItems = psItems.executeQuery()) {
                    while (rsItems.next()) {
                        int idProd = rsItems.getInt("id_producto");
                        int qty = rsItems.getInt("cantidad_apartado");

                        psStock.setInt(1, idProd);
                        int stock = 0;
                        try (ResultSet rsStock = psStock.executeQuery()) {
                            if (rsStock.next())
                                stock = rsStock.getInt("stock_producto");
                        }

                        int nuevoStock = stock + qty;
                        psStockUp.setInt(1, nuevoStock);
                        psStockUp.setInt(2, idProd);
                        psStockUp.executeUpdate();

                        psKardex.setInt(1, idProd);
                        psKardex.setInt(2, idUsuarioFirma);
                        psKardex.setInt(3, qty);
                        psKardex.setInt(4, nuevoStock);
                        psKardex.setString(5, "Cancelación Apartado #" + idApartado);
                        psKardex.executeUpdate();
                    }
                }
            }

            con.commit();
            return true;
        } catch (SQLException e) {
            if (con != null)
                try {
                    con.rollback();
                } catch (SQLException ex) {
                }
            System.err.println("Error al cancelar apartado: " + e.getMessage());
            return false;
        } finally {
            if (con != null)
                try {
                    con.setAutoCommit(true);
                    con.close();
                } catch (SQLException e) {
                }
        }
    }

    public boolean entregarApartadoYGenerarVenta(int idApartado, int idUsuarioCaja, boolean aplicarISV) {
        String sqlApartadoUpdate = "UPDATE APARTADOS SET estado_apartado = 'ENTREGADO', fecha_entrega = GETDATE() WHERE id_apartado = ? AND estado_apartado = 'PAGADO'";
        String sqlVenta = "INSERT INTO VENTAS (fecha_venta, id_cliente_venta, id_usuario, id_metodo_pago, subtotal_venta, impuesto_venta, total_venta, referencia_pago, banco_pago) VALUES (GETDATE(), ?, ?, ?, ?, ?, ?, ?, ?)";
        String sqlDetalleVenta = "INSERT INTO DETALLES_VENTA (id_ventas, id_producto, descripcion_venta, cantidad_venta, precio_unitario_venta, subtotal_venta, identificador_serie, dias_garantia) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        Connection con = null;
        try {
            con = factory.getConexion();
            con.setAutoCommit(false);

            // 1. Get Apartado details
            Apartado ap = obtenerPorId(idApartado);
            if (ap == null || !ap.getEstadoApartado().equalsIgnoreCase("PAGADO")) {
                con.rollback();
                return false;
            }

            // Get last payment method used for this layaway, default to 1 (Cash)
            int idMetodoPago = 1;
            String banco = null;
            String sqlMetodo = "SELECT TOP 1 id_metodo_pago, banco_pago FROM ABONOS_APARTADO WHERE id_apartado = ? ORDER BY id_abono DESC";
            try (PreparedStatement psMetodo = con.prepareStatement(sqlMetodo)) {
                psMetodo.setInt(1, idApartado);
                try (ResultSet rs = psMetodo.executeQuery()) {
                    if (rs.next()) {
                        idMetodoPago = rs.getInt("id_metodo_pago");
                        banco = rs.getString("banco_pago");
                    }
                }
            }

            // Calculate totals
            double total = ap.getTotalApartado();
            double subtotal = aplicarISV ? (total / 1.15) : total;
            double impuesto = aplicarISV ? (total - subtotal) : 0.0;

            // 2. Insert into VENTAS
            int idVentaGenerado = 0;
            try (PreparedStatement psVenta = con.prepareStatement(sqlVenta, Statement.RETURN_GENERATED_KEYS)) {
                psVenta.setInt(1, ap.getIdClienteApartado());
                psVenta.setInt(2, idUsuarioCaja);
                psVenta.setInt(3, idMetodoPago);
                psVenta.setDouble(4, subtotal);
                psVenta.setDouble(5, impuesto);
                psVenta.setDouble(6, total);
                psVenta.setString(7, "Pago de Apartado #" + idApartado);
                if (banco == null)
                    psVenta.setNull(8, Types.VARCHAR);
                else
                    psVenta.setString(8, banco);

                psVenta.executeUpdate();
                try (ResultSet rsKeys = psVenta.getGeneratedKeys()) {
                    if (rsKeys.next())
                        idVentaGenerado = rsKeys.getInt(1);
                }
            }
            if (idVentaGenerado == 0) {
                con.rollback();
                return false;
            }

            // 3. Insert into DETALLES_VENTA
            List<DetalleApartado> detalles = listarDetalles(idApartado);
            try (PreparedStatement psDetalle = con.prepareStatement(sqlDetalleVenta)) {
                for (DetalleApartado d : detalles) {
                    double precioUnitario = d.getPrecioUnitarioApartado();
                    if (aplicarISV)
                        precioUnitario = precioUnitario / 1.15;
                    double subtotalFila = precioUnitario * d.getCantidadApartado();

                    psDetalle.setInt(1, idVentaGenerado);
                    psDetalle.setInt(2, d.getIdProducto());
                    psDetalle.setString(3, d.getNombreProducto());
                    psDetalle.setInt(4, d.getCantidadApartado());
                    psDetalle.setDouble(5, precioUnitario);
                    psDetalle.setDouble(6, subtotalFila);
                    if (d.getIdentificadorSerie() == null || d.getIdentificadorSerie().isEmpty()) {
                        psDetalle.setNull(7, Types.VARCHAR);
                    } else {
                        psDetalle.setString(7, d.getIdentificadorSerie());
                    }

                    int diasGarantia = 0;
                    try (PreparedStatement psGar = con
                            .prepareStatement("SELECT dias_garantia FROM INVENTARIO WHERE id_producto = ?")) {
                        psGar.setInt(1, d.getIdProducto());
                        try (ResultSet rsGar = psGar.executeQuery()) {
                            if (rsGar.next())
                                diasGarantia = rsGar.getInt("dias_garantia");
                        }
                    }

                    psDetalle.setInt(8, diasGarantia);
                    psDetalle.executeUpdate();
                }
            }

            // 4. Update APARTADOS status
            try (PreparedStatement psUpdate = con.prepareStatement(sqlApartadoUpdate)) {
                psUpdate.setInt(1, idApartado);
                psUpdate.executeUpdate();
            }

            con.commit();
            return true;

        } catch (SQLException e) {
            if (con != null)
                try {
                    con.rollback();
                } catch (SQLException ex) {
                }
            System.err.println("Error al entregar apartado y generar venta: " + e.getMessage());
            return false;
        } finally {
            if (con != null)
                try {
                    con.setAutoCommit(true);
                    con.close();
                } catch (SQLException e) {
                }
        }
    }
}