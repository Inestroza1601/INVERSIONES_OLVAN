package gui;

import javax.swing.*;
import java.awt.*;
import java.net.URL;

public class PanelAyuda extends JPanel {

    private final Color COLOR_FONDO = new Color(232, 243, 236);
    private final Color COLOR_VERDE_EMERALD = new Color(45, 106, 79);

    public PanelAyuda() {
        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        setBackground(COLOR_FONDO);

        // T\u00EDtulo del Panel
        JLabel lblTitulo = new JLabel("Manual de Usuario General - ORION SYS");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitulo.setForeground(COLOR_VERDE_EMERALD);
        lblTitulo.setHorizontalAlignment(SwingConstants.CENTER);
        lblTitulo.setBorder(BorderFactory.createEmptyBorder(20, 10, 20, 10));
        add(lblTitulo, BorderLayout.NORTH);

        // Contenido en HTML
        JEditorPane editorPane = new JEditorPane();
        editorPane.setEditable(false);
        editorPane.setContentType("text/html");
        
        String htmlContent = "<html><body style='font-family: Segoe UI, sans-serif; margin: 20px; color: #1c3b2d;'>"
                + "<h2 style='color: #2d6a4f;'>1. Introducci\u00f3n y Objetivo del Sistema</h2>"
                + "<p>Bienvenido al manual de usuario oficial del sistema inform\u00e1tico <b>ORION SYS</b>. Este software ha sido desarrollado a medida para centralizar, automatizar y optimizar todos los procesos operativos y comerciales de la empresa <b>INVERSIONES OLVAN</b>.</p>"
                + "<p>El objetivo principal de esta herramienta es brindarle un control absoluto sobre su <b>inventario, ventas diarias, apartados de mercanc\u00eda, manejo de garant\u00edas y el flujo de dinero en caja</b>.</p>"
                + "<h2 style='color: #2d6a4f;'>2. Ingreso al Sistema y Seguridad</h2>"
                + "<p>El sistema cuenta con un control de acceso estricto. Cada empleado debe tener su propio usuario para que todas las acciones queden registradas a su nombre.</p>"
                + "<h3>2.1. Pantalla de Inicio de Sesi\u00f3n (Login)</h3>"
                + "<ol>"
                + "<li>En el campo <b>Usuario</b>, escriba el nombre de usuario que le fue asignado.</li>"
                + "<li>En el campo <b>Contrase\u00f1a</b>, introduzca su clave secreta.</li>"
                + "<li>Haga clic en el bot\u00f3n <b>Ingresar</b> o presione la tecla <code>Enter</code>.</li>"
                + "</ol>"
                + "<p><i>Consejo: Nunca comparta su contrase\u00f1a con otros compa\u00f1eros. Su usuario es su firma digital dentro del sistema.</i></p>"
                + "<h2 style='color: #2d6a4f;'>3. Entorno de Trabajo y Navegaci\u00f3n</h2>"
                + "<p>Identificar\u00e1 dos zonas de trabajo perfectamente delimitadas:</p>"
                + "<ul>"
                + "<li><b>El Men\u00fa Lateral Izquierdo</b>: Panel de control permanente interactivo.</li>"
                + "<li><b>El \u00c1rea de Trabajo Central</b>: Zona din\u00e1mica donde se abrir\u00e1n los formularios.</li>"
                + "</ul>"
                + "<h2 style='color: #2d6a4f;'>4. M\u00f3dulo de Administraci\u00f3n del Sistema</h2>"
                + "<p>Contiene configuraciones base, como Datos de la Empresa (nombre, RTN, impresoras t\u00e9rmicas) y Gesti\u00f3n de Roles y Usuarios.</p>"
                + "<h2 style='color: #2d6a4f;'>5. M\u00f3dulo de Gesti\u00f3n de Clientes</h2>"
                + "<p>Para agregar un cliente: Vaya a Clientes > + Nuevo Cliente > Llene el DNI/RTN y dem\u00e1s datos > Guardar.</p>"
                + "<h2 style='color: #2d6a4f;'>6. M\u00f3dulo de Inventario y Kardex</h2>"
                + "<p>Vea el cat\u00e1logo completo de productos y agregue nuevos leyendo el C\u00f3digo de Barras. Utilice el <b>Kardex</b> para auditar entradas y salidas.</p>"
                + "<h2 style='color: #2d6a4f;'>7. M\u00f3dulo de Control de Caja (Fundamental)</h2>"
                + "<p>Todo cajero debe iniciar su jornada aqu\u00ed. <b>\u00a1ALTO! El sistema bloquea las ventas si la caja no est\u00e1 abierta.</b> Al finalizar el d\u00eda, haga el Cierre de Caja.</p>"
                + "<h2 style='color: #2d6a4f;'>8. M\u00f3dulo de Punto de Venta (POS)</h2>"
                + "<p>1. Verifique cantidades. 2. Asocie a un cliente (opcional). 3. Clic en Cobrar. 4. Seleccione m\u00e9todo de pago. 5. Confirmar Venta.</p>"
                + "<h2 style='color: #2d6a4f;'>9. M\u00f3dulo de Apartados (Layaway)</h2>"
                + "<p>Ideal para asegurar mercanc\u00eda con pagos parciales. Recuerde seleccionar el cliente y registrar el abono inicial.</p>"
                + "<h2 style='color: #2d6a4f;'>10. M\u00f3dulo de Historial de Ventas</h2>"
                + "<p>Permite buscar, analizar y <b>reimprimir recibos</b> de ventas pasadas.</p>"
                + "<h2 style='color: #2d6a4f;'>11. Garant\u00edas e Inventario Defectuoso</h2>"
                + "<p>Permite aplicar garant\u00edas si el producto est\u00e1 dentro del tiempo, y enviar productos al inventario defectuoso (bodega virtual).</p>"
                + "<h2 style='color: #2d6a4f;'>12. Estad\u00edsticas y Reportes</h2>"
                + "<p>Dashboard gr\u00e1fico para conocer m\u00e9tricas como Ticket Promedio y el producto m\u00e1s vendido. Generaci\u00f3n de reportes PDF y env\u00edo por correo.</p>"
                + "<h2 style='color: #2d6a4f;'>13. Cierre Seguro de Sesi\u00f3n</h2>"
                + "<p>Use el bot\u00f3n 'Cerrar Sesi\u00f3n' si se retira de la estaci\u00f3n temporalmente. <i>Recuerde: Cerrar sesi\u00f3n NO es lo mismo que cerrar la Caja.</i></p>"
                + "</body></html>";
        
        editorPane.setText(htmlContent);
        editorPane.setCaretPosition(0);

        JScrollPane scrollPane = new JScrollPane(editorPane);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        add(scrollPane, BorderLayout.CENTER);
    }
}
