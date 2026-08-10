package utilidades;

import modelo.Empresa;
import modelo.Usuario;

public class SesionGlobal {
    
    private static Empresa empresaActual;
    private static Usuario usuarioActual;
    
    // M\u00E9todo para guardar los datos al iniciar el sistema
    public static void setEmpresaActual(Empresa emp) {
        empresaActual = emp;
    }
    
    // M\u00E9todo para obtener los datos desde cualquier parte del sistema
    public static Empresa getEmpresaActual() {
        return empresaActual;
    }

    public static void setUsuarioActual(Usuario usr) {
        usuarioActual = usr;
    }

    public static Usuario getUsuarioActual() {
        return usuarioActual;
    }
}
