package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import factory.ConexionFactory;
import modelo.Empresa;
import utilidades.SesionGlobal;

public class EmpresaDAO {

    private ConexionFactory factory;

    public EmpresaDAO() {
        this.factory = new ConexionFactory();
    }

    /**
     * Trae los datos de una empresa ESPEC\u00CDFICA por su ID.
     */
    public Empresa obtenerDatos(int idEmpresa) {
        Empresa emp = null;
        String sql = "SELECT * FROM EMPRESA WHERE id_empresa = ?";

        try (Connection con = factory.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            
            ps.setInt(1, idEmpresa);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    emp = mapearEmpresa(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener datos de empresa por ID: " + e.getMessage());
        }
        return emp; 
    }

    /**
     * M\u00E9todo inteligente: Prioriza la sesi\u00F3n actual, si no hay, busca el TOP 1 como respaldo.
     */
    public Empresa obtenerDatos() {
        // 1. Si hay una empresa cargada en memoria, buscamos esa misma para refrescar datos
        if (SesionGlobal.getEmpresaActual() != null && SesionGlobal.getEmpresaActual().getIdEmpresa() > 0) {
            return obtenerDatos(SesionGlobal.getEmpresaActual().getIdEmpresa());
        }

        // 2. Fallback de seguridad (por ejemplo, al abrir el Login por primera vez)
        Empresa emp = null;
        String sql = "SELECT TOP 1 * FROM EMPRESA ORDER BY id_empresa ASC";

        try (Connection con = factory.getConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                emp = mapearEmpresa(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener datos de empresa (Fallback): " + e.getMessage());
        }
        return emp; 
    }

    /**
     * Extrae la l\u00F3gica de mapeo para mantener el c\u00F3digo limpio y no repetirlo.
     */
    private Empresa mapearEmpresa(ResultSet rs) throws SQLException {
        Empresa emp = new Empresa();
        emp.setIdEmpresa(rs.getInt("id_empresa"));
        emp.setNombreEmpresa(rs.getString("nombre_empresa"));
        emp.setRtnEmpresa(rs.getString("rtn_empresa"));
        emp.setDuenoEmpresa(rs.getString("due\u00F1o_empresa")); 
        emp.setDireccionEmpresa(rs.getString("direccion_empresa"));
        emp.setEstadoEmpresa(rs.getBoolean("estado_empresa"));
        emp.setHabilitarFacturacion(rs.getBoolean("habilitar_facturacion_empresa"));
        emp.setNumeroTelefono(rs.getString("numero_telefono"));
        emp.setTelefonoSecundario(rs.getString("telefono_secundario"));
        emp.setWhatsapp(rs.getString("whatsapp_empresa"));
        emp.setEmail(rs.getString("email_empresa"));
        emp.setWeb(rs.getString("web_empresa"));
        emp.setFacebook(rs.getString("facebook_empresa"));
        
        emp.setMensajeTicketPieFactura(rs.getString("mensaje_ticket_pie_factura"));
        emp.setMensajeTicketPieRecibo(rs.getString("mensaje_ticket_pie_recibo"));
        emp.setMensajeTicketEntrega(rs.getString("mensaje_ticket_entrega"));
        emp.setMensajeTicketPieCotizacion(rs.getString("mensaje_ticket_pie_cotizacion"));
        emp.setImagen_logo(rs.getString("imagen_logo"));
        
        // Nuevos campos para garant\u00EDas y cambios
        emp.setPoliticasGarantia(rs.getString("politicas_garantia"));
        emp.setMensajeTicketCambio(rs.getString("mensaje_ticket_cambio"));
        emp.setMensajeTicketReclamo(rs.getString("mensaje_ticket_reclamo"));
        
        return emp;
    }

    /**
     * Guarda o actualiza respetando el ID de la empresa enviada en el objeto.
     */
    public boolean guardarOActualizar(Empresa emp) {
        String sql;
        // Si el ID es mayor a 0, significa que la empresa ya existe en la BD
        boolean esUpdate = emp.getIdEmpresa() > 0;

        if (!esUpdate) {
            // INSERT (Para una empresa totalmente nueva - 20 par\u00E1metros)
            sql = "INSERT INTO EMPRESA (nombre_empresa, rtn_empresa, due\u00F1o_empresa, direccion_empresa, estado_empresa, "
                + "habilitar_facturacion_empresa, numero_telefono, telefono_secundario, whatsapp_empresa, "
                + "email_empresa, web_empresa, facebook_empresa, mensaje_ticket_pie_factura, "
                + "mensaje_ticket_pie_recibo, mensaje_ticket_entrega, mensaje_ticket_pie_cotizacion, imagen_logo, "
                + "politicas_garantia, mensaje_ticket_cambio, mensaje_ticket_reclamo) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        } else {
            // UPDATE (Para la empresa actual - 20 par\u00E1metros + 1 para el WHERE)
            sql = "UPDATE EMPRESA SET nombre_empresa=?, rtn_empresa=?, due\u00F1o_empresa=?, direccion_empresa=?, "
                + "estado_empresa=?, habilitar_facturacion_empresa=?, numero_telefono=?, telefono_secundario=?, "
                + "whatsapp_empresa=?, email_empresa=?, web_empresa=?, facebook_empresa=?, "
                + "mensaje_ticket_pie_factura=?, mensaje_ticket_pie_recibo=?, mensaje_ticket_entrega=?, "
                + "mensaje_ticket_pie_cotizacion=?, imagen_logo=?, politicas_garantia=?, mensaje_ticket_cambio=?, mensaje_ticket_reclamo=? WHERE id_empresa=?";
        }

        try (Connection con = factory.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, emp.getNombreEmpresa());
            ps.setString(2, emp.getRtnEmpresa());
            ps.setString(3, emp.getDuenoEmpresa());
            ps.setString(4, emp.getDireccionEmpresa());
            ps.setBoolean(5, emp.isEstadoEmpresa());
            ps.setBoolean(6, emp.isHabilitarFacturacion());
            ps.setString(7, emp.getNumeroTelefono());
            ps.setString(8, emp.getTelefonoSecundario());
            ps.setString(9, emp.getWhatsapp());
            ps.setString(10, emp.getEmail());
            ps.setString(11, emp.getWeb());
            ps.setString(12, emp.getFacebook());
            ps.setString(13, emp.getMensajeTicketPieFactura());
            ps.setString(14, emp.getMensajeTicketPieRecibo());
            ps.setString(15, emp.getMensajeTicketEntrega());
            ps.setString(16, emp.getMensajeTicketPieCotizacion());
            ps.setString(17, emp.getImagen_logo());
            ps.setString(18, emp.getPoliticasGarantia());
            ps.setString(19, emp.getMensajeTicketCambio());
            ps.setString(20, emp.getMensajeTicketReclamo());

            if (esUpdate) {
                ps.setInt(21, emp.getIdEmpresa());
            }

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error al guardar empresa: " + e.getMessage());
            return false;
        }
    }
}
