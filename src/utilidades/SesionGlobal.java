package utilidades;

import modelo.Empresa;
// import modelo.Usuario; <-- Aquí también podrás guardar quién inició sesión luego

public class SesionGlobal {
    
    private static Empresa empresaActual;
    // private static Usuario usuarioActual;
    
    // Método para guardar los datos al iniciar el sistema
    public static void setEmpresaActual(Empresa emp) {
        empresaActual = emp;
    }
    
    // Método para obtener los datos desde cualquier parte del sistema
    public static Empresa getEmpresaActual() {
        return empresaActual;
    }
}