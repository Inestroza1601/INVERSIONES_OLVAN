package utilidades;

import dao.ErrorDAO;
import java.io.PrintWriter;
import java.io.StringWriter;

public class GestorErrores {

    private static ErrorDAO dao = new ErrorDAO();

    /**
     * Registra un error en la base de datos de forma automática.
     * @param e La excepción capturada
     * @param origen La clase o módulo donde ocurrió (ej. "PanelPuntoVenta - procesarVenta")
     */
    public static void registrarError(Exception e, String origen) {
        try {
            String resumen = e.getMessage();
            if (resumen == null || resumen.isEmpty()) {
                resumen = e.getClass().getSimpleName();
            }

            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            String stackTrace = sw.toString();

            dao.insertarError(origen, resumen, stackTrace);
            System.err.println("Error registrado en BD desde: " + origen);
        } catch (Exception ex) {
            System.err.println("No se pudo registrar el error en la BD. " + ex.getMessage());
        }
    }

    /**
     * Configura el manejador global para hilos que no atraparon excepciones.
     */
    public static void inicializarManejadorGlobal() {
        Thread.setDefaultUncaughtExceptionHandler((t, e) -> {
            if (e instanceof Exception) {
                registrarError((Exception) e, "Hilo Global: " + t.getName());
            } else {
                // Para Errores de la JVM u otros Throwables
                Exception exWrapper = new Exception(e);
                registrarError(exWrapper, "Hilo Global (Throwable): " + t.getName());
            }
        });
    }
}
