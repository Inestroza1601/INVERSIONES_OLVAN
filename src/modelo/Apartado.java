package modelo;

import java.sql.Timestamp;

public class Apartado {
    private int idApartado;
    private Timestamp fechaApartado;
    private Timestamp fechaLimite; // Calculated dynamically in memory
    private double totalApartado;
    private double abonoInicial;
    private double saldoPendiente;
    private int idClienteApartado;
    private int idUsuario;
    private String estadoApartado; // 'VIGENTE', 'PAGADO', 'ENTREGADO', 'CANCELADO'
    private Timestamp fechaEntrega;
    private int idMetodoPago;     // For abono inicial
    private String referenciaPago; // For abono inicial
    private String bancoPago;      // For abono inicial
    private String nombreCliente;  // For joins
    private String apellidoCliente;
    private String nombreUsuario;

    public Apartado() {}

    // Getters and Setters
    public int getIdApartado() { return idApartado; }
    public void setIdApartado(int idApartado) { this.idApartado = idApartado; }

    public Timestamp getFechaApartado() { return fechaApartado; }
    public void setFechaApartado(Timestamp fechaApartado) { this.fechaApartado = fechaApartado; }

    public Timestamp getFechaLimite() { return fechaLimite; }
    public void setFechaLimite(Timestamp fechaLimite) { this.fechaLimite = fechaLimite; }

    public double getTotalApartado() { return totalApartado; }
    public void setTotalApartado(double totalApartado) { this.totalApartado = totalApartado; }

    public double getAbonoInicial() { return abonoInicial; }
    public void setAbonoInicial(double abonoInicial) { this.abonoInicial = abonoInicial; }

    public double getSaldoPendiente() { return saldoPendiente; }
    public void setSaldoPendiente(double saldoPendiente) { this.saldoPendiente = saldoPendiente; }

    public int getIdClienteApartado() { return idClienteApartado; }
    public void setIdClienteApartado(int idClienteApartado) { this.idClienteApartado = idClienteApartado; }

    public int getIdUsuario() { return idUsuario; }
    public void setIdUsuario(int idUsuario) { this.idUsuario = idUsuario; }

    public String getEstadoApartado() { return estadoApartado; }
    public void setEstadoApartado(String estadoApartado) { this.estadoApartado = estadoApartado; }

    public Timestamp getFechaEntrega() { return fechaEntrega; }
    public void setFechaEntrega(Timestamp fechaEntrega) { this.fechaEntrega = fechaEntrega; }

    public int getIdMetodoPago() { return idMetodoPago; }
    public void setIdMetodoPago(int idMetodoPago) { this.idMetodoPago = idMetodoPago; }

    public String getReferenciaPago() { return referenciaPago; }
    public void setReferenciaPago(String referenciaPago) { this.referenciaPago = referenciaPago; }

    public String getBancoPago() { return bancoPago; }
    public void setBancoPago(String bancoPago) { this.bancoPago = bancoPago; }

    public String getNombreCliente() { return nombreCliente; }
    public void setNombreCliente(String nombreCliente) { this.nombreCliente = nombreCliente; }

    public String getApellidoCliente() { return apellidoCliente; }
    public void setApellidoCliente(String apellidoCliente) { this.apellidoCliente = apellidoCliente; }

    public String getNombreUsuario() { return nombreUsuario; }
    public void setNombreUsuario(String nombreUsuario) { this.nombreUsuario = nombreUsuario; }
}
