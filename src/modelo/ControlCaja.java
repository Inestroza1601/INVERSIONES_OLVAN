package modelo;

import java.sql.Timestamp;

public class ControlCaja {
    private int idCaja;
    private Timestamp fechaApertura;
    private double montoApertura;
    private Timestamp fechaCierre;
    private double montoCierreEsperado;
    private double montoCierreReal;
    private double diferenciaCierre;
    private String observaciones;
    private String cajeroTurno;
    private int idUsuarioApertura;
    private int idUsuarioCierre;
    private int estadoCaja;
    private String nombreUsuarioApertura; // Para joins visuales

    public ControlCaja() {
    }

    // Getters and Setters
    public int getIdCaja() {
        return idCaja;
    }

    public void setIdCaja(int idCaja) {
        this.idCaja = idCaja;
    }

    public Timestamp getFechaApertura() {
        return fechaApertura;
    }

    public void setFechaApertura(Timestamp fechaApertura) {
        this.fechaApertura = fechaApertura;
    }

    public double getMontoApertura() {
        return montoApertura;
    }

    public void setMontoApertura(double montoApertura) {
        this.montoApertura = montoApertura;
    }

    public Timestamp getFechaCierre() {
        return fechaCierre;
    }

    public void setFechaCierre(Timestamp fechaCierre) {
        this.fechaCierre = fechaCierre;
    }

    public double getMontoCierreEsperado() {
        return montoCierreEsperado;
    }

    public void setMontoCierreEsperado(double montoCierreEsperado) {
        this.montoCierreEsperado = montoCierreEsperado;
    }

    public double getMontoCierreReal() {
        return montoCierreReal;
    }

    public void setMontoCierreReal(double montoCierreReal) {
        this.montoCierreReal = montoCierreReal;
    }

    public double getDiferenciaCierre() {
        return diferenciaCierre;
    }

    public void setDiferenciaCierre(double diferenciaCierre) {
        this.diferenciaCierre = diferenciaCierre;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public String getCajeroTurno() {
        return cajeroTurno;
    }

    public void setCajeroTurno(String cajeroTurno) {
        this.cajeroTurno = cajeroTurno;
    }

    public int getIdUsuarioApertura() {
        return idUsuarioApertura;
    }

    public void setIdUsuarioApertura(int idUsuarioApertura) {
        this.idUsuarioApertura = idUsuarioApertura;
    }

    public int getIdUsuarioCierre() {
        return idUsuarioCierre;
    }

    public void setIdUsuarioCierre(int idUsuarioCierre) {
        this.idUsuarioCierre = idUsuarioCierre;
    }

    public int getEstadoCaja() {
        return estadoCaja;
    }

    public void setEstadoCaja(int estadoCaja) {
        this.estadoCaja = estadoCaja;
    }

    public String getNombreUsuarioApertura() {
        return nombreUsuarioApertura;
    }

    public void setNombreUsuarioApertura(String nombreUsuarioApertura) {
        this.nombreUsuarioApertura = nombreUsuarioApertura;
    }
}
