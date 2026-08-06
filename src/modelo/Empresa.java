package modelo;

public class Empresa {
    private int idEmpresa;
    private String nombreEmpresa;
    private String rtnEmpresa;
    private String duenoEmpresa;
    private String direccionEmpresa;
    private boolean estadoEmpresa;
    private boolean habilitarFacturacion;
    private String numeroTelefono;
    private String telefonoSecundario;
    private String whatsapp;
    private String email;
    private String web;
    private String facebook;

    // campos para los tickets y logo
    private String mensajeTicketPieFactura;
    private String mensajeTicketPieRecibo;
    private String mensajeTicketEntrega;
    private String mensajeTicketPieCotizacion;
    private String mensajeTicketCambio;
    private String mensajeTicketReclamo;
    private String logoEmpresaRuta;
    private String politicasGarantia;

    // Constructores vacíos
    public Empresa() {
    }

    // Getters y Setters
    public int getIdEmpresa() {
        return idEmpresa;
    }

    public void setIdEmpresa(int idEmpresa) {
        this.idEmpresa = idEmpresa;
    }

    public String getNombreEmpresa() {
        return nombreEmpresa;
    }

    public void setNombreEmpresa(String nombreEmpresa) {
        this.nombreEmpresa = nombreEmpresa;
    }

    public String getRtnEmpresa() {
        return rtnEmpresa;
    }

    public void setRtnEmpresa(String rtnEmpresa) {
        this.rtnEmpresa = rtnEmpresa;
    }

    public String getDuenoEmpresa() {
        return duenoEmpresa;
    }

    public void setDuenoEmpresa(String duenoEmpresa) {
        this.duenoEmpresa = duenoEmpresa;
    }

    public String getDireccionEmpresa() {
        return direccionEmpresa;
    }

    public void setDireccionEmpresa(String direccionEmpresa) {
        this.direccionEmpresa = direccionEmpresa;
    }

    public boolean isEstadoEmpresa() {
        return estadoEmpresa;
    }

    public void setEstadoEmpresa(boolean estadoEmpresa) {
        this.estadoEmpresa = estadoEmpresa;
    }

    public boolean isHabilitarFacturacion() {
        return habilitarFacturacion;
    }

    public void setHabilitarFacturacion(boolean habilitarFacturacion) {
        this.habilitarFacturacion = habilitarFacturacion;
    }

    public String getNumeroTelefono() {
        return numeroTelefono;
    }

    public void setNumeroTelefono(String numeroTelefono) {
        this.numeroTelefono = numeroTelefono;
    }

    public String getLogoEmpresaRuta() {
        return logoEmpresaRuta;
    }

    public void setLogoEmpresaRuta(String logoEmpresaRuta) {
        this.logoEmpresaRuta = logoEmpresaRuta;
    }

    public String getPoliticasGarantia() {
        return politicasGarantia;
    }

    public void setPoliticasGarantia(String politicasGarantia) {
        this.politicasGarantia = politicasGarantia;
    }

    public String getMensajeTicketCambio() {
        return mensajeTicketCambio;
    }

    public void setMensajeTicketCambio(String mensajeTicketCambio) {
        this.mensajeTicketCambio = mensajeTicketCambio;
    }

    public String getMensajeTicketReclamo() {
        return mensajeTicketReclamo;
    }

    public void setMensajeTicketReclamo(String mensajeTicketReclamo) {
        this.mensajeTicketReclamo = mensajeTicketReclamo;
    }
}
