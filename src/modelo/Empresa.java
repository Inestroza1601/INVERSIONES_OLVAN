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
    private String logoEmpresaRuta;
    private String politicasGarantia;

    // Constructores vacíos
    public Empresa() {}

    // Getters y Setters
    public int getIdEmpresa() { return idEmpresa; }
    public void setIdEmpresa(int idEmpresa) { this.idEmpresa = idEmpresa; }

    public String getNombreEmpresa() { return nombreEmpresa; }
    public void setNombreEmpresa(String nombreEmpresa) { this.nombreEmpresa = nombreEmpresa; }

    public String getRtnEmpresa() { return rtnEmpresa; }
    public void setRtnEmpresa(String rtnEmpresa) { this.rtnEmpresa = rtnEmpresa; }

    public String getDuenoEmpresa() { return duenoEmpresa; }
    public void setDuenoEmpresa(String duenoEmpresa) { this.duenoEmpresa = duenoEmpresa; }

    public String getDireccionEmpresa() { return direccionEmpresa; }
    public void setDireccionEmpresa(String direccionEmpresa) { this.direccionEmpresa = direccionEmpresa; }

    public boolean isEstadoEmpresa() { return estadoEmpresa; }
    public void setEstadoEmpresa(boolean estadoEmpresa) { this.estadoEmpresa = estadoEmpresa; }

    public boolean isHabilitarFacturacion() { return habilitarFacturacion; }
    public void setHabilitarFacturacion(boolean habilitarFacturacion) { this.habilitarFacturacion = habilitarFacturacion; }

    public String getNumeroTelefono() { return numeroTelefono; }
    public void setNumeroTelefono(String numeroTelefono) { this.numeroTelefono = numeroTelefono; }

    public String getTelefonoSecundario() { return telefonoSecundario; }
    public void setTelefonoSecundario(String telefonoSecundario) { this.telefonoSecundario = telefonoSecundario; }

    public String getWhatsapp() { return whatsapp; }
    public void setWhatsapp(String whatsapp) { this.whatsapp = whatsapp; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getWeb() { return web; }
    public void setWeb(String web) { this.web = web; }

    public String getFacebook() { return facebook; }
    public void setFacebook(String facebook) { this.facebook = facebook; }
    
    public String getMensajeTicketPieFactura() { return mensajeTicketPieFactura; }
    public void setMensajeTicketPieFactura(String mensajeTicketPieFactura) { this.mensajeTicketPieFactura = mensajeTicketPieFactura; }

    public String getMensajeTicketPieRecibo() { return mensajeTicketPieRecibo; }
    public void setMensajeTicketPieRecibo(String mensajeTicketPieRecibo) { this.mensajeTicketPieRecibo = mensajeTicketPieRecibo; }

    public String getMensajeTicketEntrega() { return mensajeTicketEntrega; }
    public void setMensajeTicketEntrega(String mensajeTicketEntrega) { this.mensajeTicketEntrega = mensajeTicketEntrega; }

    public String getMensajeTicketPieCotizacion() { return mensajeTicketPieCotizacion; }
    public void setMensajeTicketPieCotizacion(String mensajeTicketPieCotizacion) { this.mensajeTicketPieCotizacion = mensajeTicketPieCotizacion; }

    public String getLogoEmpresaRuta() { return logoEmpresaRuta; }
    public void setLogoEmpresaRuta(String logoEmpresaRuta) { this.logoEmpresaRuta = logoEmpresaRuta; }
    
    public String getPoliticasGarantia() { return politicasGarantia; }
    public void setPoliticasGarantia(String politicasGarantia) { this.politicasGarantia = politicasGarantia; }
}