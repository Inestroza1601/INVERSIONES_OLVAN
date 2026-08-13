package factory;

import java.io.FileInputStream;
import java.io.File;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import javax.swing.JOptionPane;

/**
 * Factory para la conexi\u00F3n a la base de datos (SQL Server)
 * Proyecto: Multiservicios WYS - Sistema de Control de Motos / NexarBD
 */
public class ConexionFactory {
    
    private static String host = "170.80.140.2";
    private static String port = "6161"; 
    private static String database = "NexarBD";
    private static String usuario = "orionsys";
    private static String password = "123";
    
    private static long tiempoUltimoError = 0;
    
    // --- VARIABLES DE ESTADO ---
    private static boolean huboFalloConexion = false;
    private static boolean hiloVigilanteActivo = false;
    private static boolean monitorRedIniciado = false;
    private static boolean conexionPerdidaNotificada = false;
    
    static {
        // 1. Forzar a Java a cargar el driver de SQL Server
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        } catch (ClassNotFoundException e) {
            utilidades.Mensajes.showMessageDialog(null, 
                "Falta el archivo mssql-jdbc.jar en las librer\u00EDas del proyecto.", 
                "Error de Driver", JOptionPane.ERROR_MESSAGE);
        }

        // 2. Cargar configuraci\u00F3n desde el archivo externo (ra\u00EDz del programa)
        Properties config = new Properties();
        
        // Apuntamos al archivo f\u00EDsico en la carpeta donde se est\u00E1 ejecutando el programa
        File archivoConfig = new File("config.properties");
        
        if (archivoConfig.exists()) {
            try (InputStream input = new FileInputStream(archivoConfig)) {
                config.load(input);
                host = config.getProperty("IP_SERVIDOR", host);
                port = config.getProperty("PUERTO", port);
                database = config.getProperty("BASE_DATOS", database);
                usuario = config.getProperty("USUARIO", usuario);
                password = config.getProperty("PASSWORD", password);
            } catch (Exception e) { 
                System.out.println("Error al leer el archivo de propiedades: " + e.getMessage());
            }
        } else {
            System.out.println("ATENCI\u00D3N: No se encontr\u00F3 el archivo f\u00EDsico en: " + archivoConfig.getAbsolutePath());
            System.out.println("Usando credenciales por defecto incrustadas en el c\u00F3digo.");
        }
        
        // Iniciar el monitor de red permanentemente desde que arranca la App
        iniciarMonitorRed();
    }
    
    /**
     * Inicia un hilo en segundo plano que vigila constantemente la conectividad
     * con el servidor para alertar de inmediato cuando se pierde la conexión.
     */
    public static void iniciarMonitorRed() {
        if (monitorRedIniciado) return;
        monitorRedIniciado = true;

        new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(5000); // Check every 5 seconds
                    boolean disponible = false;
                    try (java.net.Socket s = new java.net.Socket()) {
                        s.connect(new java.net.InetSocketAddress(host, Integer.parseInt(port)), 2000);
                        disponible = true;
                    } catch (Exception e) {}
                    
                    if (!disponible && !conexionPerdidaNotificada) {
                        conexionPerdidaNotificada = true;
                        huboFalloConexion = true;
                        javax.swing.SwingUtilities.invokeLater(() -> {
                            utilidades.Mensajes.showMessageDialog(null, 
                                "¡ATENCIÓN! Se ha perdido la conexión a internet o al servidor de base de datos.\n" +
                                "El sistema requiere conexión para guardar y consultar información.\n\n" +
                                "Esperando conexión a red...", 
                                "Sin Conexión", JOptionPane.WARNING_MESSAGE);
                        });
                    } else if (disponible && conexionPerdidaNotificada) {
                        conexionPerdidaNotificada = false;
                        huboFalloConexion = false;
                        javax.swing.SwingUtilities.invokeLater(() -> {
                            utilidades.Mensajes.showMessageDialog(null, 
                                "¡Conexión Restablecida! El sistema ha vuelto a conectarse exitosamente.", 
                                "Conexión Recuperada", JOptionPane.INFORMATION_MESSAGE);
                        });
                    }
                } catch (InterruptedException ex) {
                    break;
                }
            }
        }).start();
    }

    /**
     * Obtiene una conexi\u00F3n activa analizando todos los escenarios de fallo. 
     */
    public Connection getConexion() throws SQLException {
        String url = "jdbc:sqlserver://" + host + ":" + port + 
             ";databaseName=" + database + 
             ";encrypt=true;trustServerCertificate=true;";
        
        try {
            Connection con = DriverManager.getConnection(url, usuario, password);
            
            if (huboFalloConexion) {
                utilidades.Mensajes.showMessageDialog(null, "Conexi\u00F3n Restablecida exitosamente con el servidor.", "Conexi\u00F3n Recuperada", JOptionPane.INFORMATION_MESSAGE);
                huboFalloConexion = false; 
            }
            return con;
            
        } catch (SQLException e) {
            huboFalloConexion = true;
            analizarErrorSQL(e); // Llamamos a nuestro nuevo m\u00E9todo detector de errores
            
            if (!hiloVigilanteActivo) {
                System.out.println("Iniciando Vigilante Fantasma... Nexar buscar\u00E1 el servidor en segundo plano.");
                iniciarVigilanteFantasma(url); 
            }
            throw e; 
        }
    }

    /**
     * TRADUCTOR DE ERRORES: Analiza el mensaje de SQL Server y muestra una alerta amigable.
     */
    private void analizarErrorSQL(SQLException e) {
        long tiempoActual = System.currentTimeMillis();
        // Evitamos que salgan 50 ventanas de error de golpe (Cooldown de 10 segundos)
        if (tiempoActual - tiempoUltimoError > 10000) { 
            
            String msjOriginal = e.getMessage().toLowerCase();
            String msjTraducido;
            String tituloAlerta;

            if (msjOriginal.contains("tcp/ip") || msjOriginal.contains("connection refused") || msjOriginal.contains("se ha denegado la conexi\u00F3n")) {
                tituloAlerta = "El Servidor est\u00E1 Apagado o Bloqueado (TCP/IP)";
                msjTraducido = "ORION SYSTEMS no puede alcanzar la base de datos.\n\n"
                             + "SOLUCIONES SUGERIDAS:\n"
                             + "1. El servicio de SQL Server no est\u00E1 iniciado.\n"
                             + "2. El protocolo TCP/IP est\u00E1 deshabilitado en el 'SQL Server Configuration Manager'.\n"
                             + "3. El puerto " + port + " est\u00E1 bloqueado por el Firewall de Windows.\n"
                             + "4. La IP/Host (" + host + ") es incorrecta.";
                             
            } else if (msjOriginal.contains("login failed") || msjOriginal.contains("error de inicio de sesi\u00F3n")) {
                tituloAlerta = "Error de Autenticaci\u00F3n";
                msjTraducido = "Las credenciales son incorrectas.\n\n"
                             + "Verifique que el usuario '" + usuario + "' y la contrase\u00F1a sean correctos en el archivo config.properties.";
                             
            } else if (msjOriginal.contains("database") && (msjOriginal.contains("not found") || msjOriginal.contains("no existe"))) {
                tituloAlerta = "Base de Datos no encontrada";
                msjTraducido = "Se logr\u00F3 conectar al servidor, pero la base de datos '" + database + "' no existe.\n\n"
                             + "Aseg\u00FArese de haber ejecutado el script de creaci\u00F3n de NexarBD en SQL Server.";
                             
            } else if (msjOriginal.contains("certificate") || msjOriginal.contains("ssl")) {
                tituloAlerta = "Error de Certificado de Seguridad";
                msjTraducido = "Hubo un problema con la encriptaci\u00F3n SSL de Java hacia SQL Server.\n"
                             + "Aseg\u00FArese de que 'trustServerCertificate=true' est\u00E9 en la URL.";
                             
            } else {
                tituloAlerta = "Error Desconocido de Base de Datos";
                msjTraducido = "C\u00F3digo de error: " + e.getErrorCode() + "\nDetalle: " + e.getMessage();
            }

            utilidades.Mensajes.showMessageDialog(null, msjTraducido, tituloAlerta, JOptionPane.ERROR_MESSAGE);
            tiempoUltimoError = tiempoActual;
        }
    }
    
    private void iniciarVigilanteFantasma(String url) {
        if (hiloVigilanteActivo) return; 
        hiloVigilanteActivo = true;

        new Thread(() -> {
            while (huboFalloConexion) {
                try {
                    Thread.sleep(3000); 
                    Connection testCon = DriverManager.getConnection(url, usuario, password);
                    
                    if (testCon != null) {
                        testCon.close();
                        hiloVigilanteActivo = false;
                    }
                } catch (Exception ex) {
                    // Sigue intentando conectar silenciosamente en segundo plano
                }
            }
        }).start();
    }
}

