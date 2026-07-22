package modelo;

public class Usuario {
    private int idUsuario;
    private int idRol;
    private String nombreRol;
    private String nombreUsuario;
    private String passwordHash;
    private boolean estadoUsuario;

    public Usuario() {}

    // Getters y Setters
    public int getIdUsuario() { return idUsuario; }
    public void setIdUsuario(int idUsuario) { this.idUsuario = idUsuario; }
    public int getIdRol() { return idRol; }
    public void setIdRol(int idRol) { this.idRol = idRol; }
    public String getNombreRol() { return nombreRol; } // <--- AGREGAR
    public void setNombreRol(String nombreRol) { this.nombreRol = nombreRol; } // <--- AGREGAR
    public String getNombreUsuario() { return nombreUsuario; }
    public void setNombreUsuario(String nombreUsuario) { this.nombreUsuario = nombreUsuario; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public boolean isEstadoUsuario() { return estadoUsuario; }
    public void setEstadoUsuario(boolean estadoUsuario) { this.estadoUsuario = estadoUsuario; }
}