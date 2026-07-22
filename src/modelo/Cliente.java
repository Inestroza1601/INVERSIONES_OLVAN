package modelo;

public class Cliente {
    private int idCliente;
    private String identidadCliente;
    private String nombreCliente;
    private String apellidoCliente;
    private String telefonoCliente;
    private String correoCliente;
    private boolean estadoCliente;

    // Constructor vacío
    public Cliente() {}

    // Getters y Setters
    public int getIdCliente() { return idCliente; }
    public void setIdCliente(int idCliente) { this.idCliente = idCliente; }

    public String getIdentidadCliente() { return identidadCliente; }
    public void setIdentidadCliente(String identidadCliente) { this.identidadCliente = identidadCliente; }

    public String getNombreCliente() { return nombreCliente; }
    public void setNombreCliente(String nombreCliente) { this.nombreCliente = nombreCliente; }

    public String getApellidoCliente() { return apellidoCliente; }
    public void setApellidoCliente(String apellidoCliente) { this.apellidoCliente = apellidoCliente; }

    public String getTelefonoCliente() { return telefonoCliente; }
    public void setTelefonoCliente(String telefonoCliente) { this.telefonoCliente = telefonoCliente; }

    public String getCorreoCliente() { return correoCliente; }
    public void setCorreoCliente(String correoCliente) { this.correoCliente = correoCliente; }

    public boolean isEstadoCliente() { return estadoCliente; }
    public void setEstadoCliente(boolean estadoCliente) { this.estadoCliente = estadoCliente; }
}