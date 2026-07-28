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
 * Factory para la conexión a la base de datos (SQL Server)
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
    
    static {
        // 1. Forzar a Java a cargar el driver de SQL Server
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        } catch (ClassNotFoundException e) {
            JOptionPane.showMessageDialog(null, 
                "Falta el archivo mssql-jdbc.jar en las librerías del proyecto.", 
                "Error de Driver", JOptionPane.ERROR_MESSAGE);
        }

        // 2. Cargar configuración desde el archivo externo (raíz del programa)
        Properties config = new Properties();
        
        // Apuntamos al archivo físico en la carpeta donde se está ejecutando el programa
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
            System.out.println("ATENCIÓN: No se encontró el archivo físico en: " + archivoConfig.getAbsolutePath());
            System.out.println("Usando credenciales por defecto incrustadas en el código.");
        }
    }

    /**
     * Obtiene una conexión activa analizando todos los escenarios de fallo. 
     */
    public Connection getConexion() throws SQLException {
        String url = "jdbc:sqlserver://" + host + ":" + port + 
             ";databaseName=" + database + 
             ";encrypt=true;trustServerCertificate=true;";
        
        try {
            Connection con = DriverManager.getConnection(url, usuario, password);
            
            if (huboFalloConexion) {
                JOptionPane.showMessageDialog(null, "Conexión Restablecida exitosamente con el servidor.", "Conexión Recuperada", JOptionPane.INFORMATION_MESSAGE);
                huboFalloConexion = false; 
            }
            return con;
            
        } catch (SQLException e) {
            huboFalloConexion = true;
            analizarErrorSQL(e); // Llamamos a nuestro nuevo método detector de errores
            
            if (!hiloVigilanteActivo) {
                System.out.println("Iniciando Vigilante Fantasma... Nexar buscará el servidor en segundo plano.");
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

            if (msjOriginal.contains("tcp/ip") || msjOriginal.contains("connection refused") || msjOriginal.contains("se ha denegado la conexión")) {
                tituloAlerta = "El Servidor está Apagado o Bloqueado (TCP/IP)";
                msjTraducido = "ORION SYSTEMS no puede alcanzar la base de datos.\n\n"
                             + "SOLUCIONES SUGERIDAS:\n"
                             + "1. El servicio de SQL Server no está iniciado.\n"
                             + "2. El protocolo TCP/IP está deshabilitado en el 'SQL Server Configuration Manager'.\n"
                             + "3. El puerto " + port + " está bloqueado por el Firewall de Windows.\n"
                             + "4. La IP/Host (" + host + ") es incorrecta.";
                             
            } else if (msjOriginal.contains("login failed") || msjOriginal.contains("error de inicio de sesión")) {
                tituloAlerta = "Error de Autenticación";
                msjTraducido = "Las credenciales son incorrectas.\n\n"
                             + "Verifique que el usuario '" + usuario + "' y la contraseña sean correctos en el archivo config.properties.";
                             
            } else if (msjOriginal.contains("database") && (msjOriginal.contains("not found") || msjOriginal.contains("no existe"))) {
                tituloAlerta = "Base de Datos no encontrada";
                msjTraducido = "Se logró conectar al servidor, pero la base de datos '" + database + "' no existe.\n\n"
                             + "Asegúrese de haber ejecutado el script de creación de NexarBD en SQL Server.";
                             
            } else if (msjOriginal.contains("certificate") || msjOriginal.contains("ssl")) {
                tituloAlerta = "Error de Certificado de Seguridad";
                msjTraducido = "Hubo un problema con la encriptación SSL de Java hacia SQL Server.\n"
                             + "Asegúrese de que 'trustServerCertificate=true' esté en la URL.";
                             
            } else {
                tituloAlerta = "Error Desconocido de Base de Datos";
                msjTraducido = "Código de error: " + e.getErrorCode() + "\nDetalle: " + e.getMessage();
            }

            JOptionPane.showMessageDialog(null, msjTraducido, tituloAlerta, JOptionPane.ERROR_MESSAGE);
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
<<<<<<< HEAD
}
=======
}
>>>>>>> origin/parte-muoz
