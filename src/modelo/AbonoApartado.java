package modelo;

import java.sql.Timestamp;

public class AbonoApartado {
    private int idAbono;
    private int idApartado;
    private Timestamp fechaAbono;
    private double montoAbono;
    private int idMetodoPago;
    private int idUsuario;
    private String referenciaPago;
    private String bancoPago;
    private String nombreMetodo; // For joins
    private String nombreUsuario;

    public AbonoApartado() {}

    // Getters and Setters
    public int getIdAbono() { return idAbono; }
    public void setIdAbono(int idAbono) { this.idAbono = idAbono; }

    public int getIdApartado() { return idApartado; }
    public void setIdApartado(int idApartado) { this.idApartado = idApartado; }

    public Timestamp getFechaAbono() { return fechaAbono; }
    public void setFechaAbono(Timestamp fechaAbono) { this.fechaAbono = fechaAbono; }

    public double getMontoAbono() { return montoAbono; }
    public void setMontoAbono(double montoAbono) { this.montoAbono = montoAbono; }

    public int getIdMetodoPago() { return idMetodoPago; }
    public void setIdMetodoPago(int idMetodoPago) { this.idMetodoPago = idMetodoPago; }

    public int getIdUsuario() { return idUsuario; }
    public void setIdUsuario(int idUsuario) { this.idUsuario = idUsuario; }

    public String getReferenciaPago() { return referenciaPago; }
    public void setReferenciaPago(String referenciaPago) { this.referenciaPago = referenciaPago; }

    public String getBancoPago() { return bancoPago; }
    public void setBancoPago(String bancoPago) { this.bancoPago = bancoPago; }

    public String getNombreMetodo() { return nombreMetodo; }
    public void setNombreMetodo(String nombreMetodo) { this.nombreMetodo = nombreMetodo; }

    public String getNombreUsuario() { return nombreUsuario; }
    public void setNombreUsuario(String nombreUsuario) { this.nombreUsuario = nombreUsuario; }
}
