package modelo;

public class Usuario {
    private int idUsuario;
    private int idRol;
    private String nombreRol;
    private String nombreUsuario;
    private String passwordHash;
    private boolean estadoUsuario;
    private String emailUsuario;
    private java.util.List<String> permisos = new java.util.ArrayList<>(); // <--- AGREGAR PERMISOS RBAC

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
    public String getEmailUsuario() { return emailUsuario; }
    public void setEmailUsuario(String emailUsuario) { this.emailUsuario = emailUsuario; }
    
    public java.util.List<String> getPermisos() { return permisos; }
    public void setPermisos(java.util.List<String> permisos) { this.permisos = permisos; }
    
    // Utilidad rápida para chequear permiso en la UI
    public boolean tienePermiso(String permisoBuscado) {
        // Rol 1 (Admin/Programador) o "Administrador" (si el nombre aplica) tienen todo.
        if (this.idRol == 1 || (this.nombreRol != null && this.nombreRol.equalsIgnoreCase("Administrador"))) {
            return true;
        }
        return this.permisos != null && this.permisos.contains(permisoBuscado.toUpperCase());
    }
}
