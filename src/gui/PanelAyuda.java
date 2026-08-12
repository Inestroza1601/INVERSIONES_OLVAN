package gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.border.EmptyBorder;

public class PanelAyuda extends JPanel {

    private final Color COLOR_FONDO_MENU = new Color(232, 243, 236); // Verde clarito Orion
    private final Color COLOR_TEXTO_MENU = new Color(45, 106, 79); // Verde oscuro
    private final Color COLOR_MENU_HOVER = new Color(210, 230, 215);
    private final Color COLOR_MENU_ACTIVO = Color.WHITE;
    private final Color COLOR_FONDO_CONTENIDO = Color.WHITE;

    private JPanel panelContenido;
    private CardLayout cardLayout;
    private JPanel panelBotones;
    
    private JButton botonActivo = null;

    public PanelAyuda() {
        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout());

        // Panel Lateral (Menú)
        JPanel panelLateral = new JPanel(new BorderLayout());
        panelLateral.setBackground(COLOR_FONDO_MENU);
        panelLateral.setPreferredSize(new Dimension(260, 0));
        panelLateral.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(200, 220, 205)));

        // Título del Menú
        JLabel lblMenu = new JLabel("Centro de Ayuda");
        lblMenu.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblMenu.setForeground(COLOR_TEXTO_MENU);
        lblMenu.setBorder(new EmptyBorder(25, 20, 20, 20));
        panelLateral.add(lblMenu, BorderLayout.NORTH);

        // Lista de Botones
        panelBotones = new JPanel();
        panelBotones.setLayout(new BoxLayout(panelBotones, BoxLayout.Y_AXIS));
        panelBotones.setBackground(COLOR_FONDO_MENU);
        
        // Área Central (CardLayout)
        cardLayout = new CardLayout();
        panelContenido = new JPanel(cardLayout);
        panelContenido.setBackground(COLOR_FONDO_CONTENIDO);

        // Crear Módulos con Iconos Personalizados
        agregarModulo("Introducción", "intro", getHtmlIntroduccion(), new IconoAyuda(1));
        agregarModulo("Administración", "admin", getHtmlAdmin(), new IconoAyuda(2));
        agregarModulo("Clientes e Inventario", "inventario", getHtmlInventario(), new IconoAyuda(3));
        agregarModulo("Caja y Ventas (POS)", "ventas", getHtmlVentas(), new IconoAyuda(4));
        agregarModulo("Apartados y Garantías", "apartados", getHtmlApartados(), new IconoAyuda(5));
        agregarModulo("Reportes y Sesión", "reportes", getHtmlReportes(), new IconoAyuda(6));

        panelLateral.add(panelBotones, BorderLayout.CENTER);
        
        add(panelLateral, BorderLayout.WEST);
        add(panelContenido, BorderLayout.CENTER);

        // Mostrar la primera por defecto
        if (panelBotones.getComponentCount() > 0) {
            JButton primerBoton = (JButton) panelBotones.getComponent(0);
            seleccionarBoton(primerBoton);
            cardLayout.show(panelContenido, "intro");
        }
    }

    private void agregarModulo(String titulo, String id, String contenidoHtml, Icon icono) {
        // Botón del Menú
        JButton btn = new JButton("  " + titulo);
        btn.setIcon(icono);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setForeground(COLOR_TEXTO_MENU);
        btn.setBackground(COLOR_FONDO_MENU);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setBorder(new EmptyBorder(10, 15, 10, 15));
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (botonActivo != btn) btn.setBackground(COLOR_MENU_HOVER);
            }
            @Override
            public void mouseExited(MouseEvent e) {
                if (botonActivo != btn) btn.setBackground(COLOR_FONDO_MENU);
            }
            @Override
            public void mousePressed(MouseEvent e) {
                seleccionarBoton(btn);
                cardLayout.show(panelContenido, id);
            }
        });
        panelBotones.add(btn);

        // Contenido Web
        JEditorPane editorPane = new JEditorPane();
        editorPane.setEditable(false);
        editorPane.setContentType("text/html");
        editorPane.setText("<html><body style='font-family: Segoe UI, sans-serif; margin: 30px; color: #333333; line-height: 1.6;'>"
                + "<h1 style='color: #2c3e50; border-bottom: 2px solid #27ae60; padding-bottom: 10px;'>" + titulo + "</h1>"
                + contenidoHtml
                + "</body></html>");
        editorPane.setCaretPosition(0);
        
        JScrollPane scroll = new JScrollPane(editorPane);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        
        panelContenido.add(scroll, id);
    }

    private void seleccionarBoton(JButton btn) {
        if (botonActivo != null) {
            botonActivo.setBackground(COLOR_FONDO_MENU);
        }
        botonActivo = btn;
        botonActivo.setBackground(COLOR_MENU_ACTIVO);
    }

    // --- CLASE INTERNA PARA DIBUJAR ICONOS SIMPLES ---
    private class IconoAyuda implements Icon {
        private int tipo;
        public IconoAyuda(int tipo) { this.tipo = tipo; }
        
        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(COLOR_TEXTO_MENU);
            g2.setStroke(new BasicStroke(1.5f));
            
            int cx = x + 4;
            int cy = y + 4;

            switch(tipo) {
                case 1: // Intro (Libro)
                    g2.drawRect(cx, cy, 14, 16);
                    g2.drawLine(cx + 7, cy, cx + 7, cy + 16);
                    break;
                case 2: // Admin (Engranaje)
                    g2.drawOval(cx + 3, cy + 3, 10, 10);
                    g2.drawLine(cx + 8, cy, cx + 8, cy + 16);
                    g2.drawLine(cx, cy + 8, cx + 16, cy + 8);
                    break;
                case 3: // Clientes (Personas)
                    g2.drawOval(cx + 4, cy, 8, 8);
                    g2.drawArc(cx, cy + 8, 16, 16, 0, 180);
                    break;
                case 4: // Ventas (Carrito/Caja)
                    g2.drawRect(cx, cy + 2, 16, 10);
                    g2.drawOval(cx + 2, cy + 13, 4, 4);
                    g2.drawOval(cx + 10, cy + 13, 4, 4);
                    break;
                case 5: // Garantias (Escudo)
                    g2.drawPolygon(new int[]{cx+8, cx+16, cx+16, cx+8, cx}, new int[]{cy, cy+3, cy+10, cy+16, cy+10}, 5);
                    break;
                case 6: // Reportes (Gráfico)
                    g2.drawLine(cx, cy, cx, cy + 16);
                    g2.drawLine(cx, cy + 16, cx + 16, cy + 16);
                    g2.fillRect(cx + 3, cy + 8, 3, 8);
                    g2.fillRect(cx + 7, cy + 4, 3, 12);
                    g2.fillRect(cx + 11, cy, 3, 16);
                    break;
            }
            g2.dispose();
        }

        @Override
        public int getIconWidth() { return 24; }
        @Override
        public int getIconHeight() { return 24; }
    }

    // --- CONTENIDOS HTML DE LOS MÓDULOS ---

    private String getHtmlIntroduccion() {
        return "<h2 style='color: #27ae60;'>1. Objetivo del Sistema</h2>"
             + "<p>Bienvenido al manual de usuario oficial de <b>ORION SYS</b>. Este software centraliza, automatiza y optimiza todos los procesos operativos de <b>INVERSIONES OLVAN</b>.</p>"
             + "<table width='100%' cellpadding='15' cellspacing='0' style='background-color: #e8f8f5; border-left: 6px solid #1abc9c; margin-top: 10px;'>"
             + "<tr><td><b>💡 TIP DE PRODUCTIVIDAD</b><br>Utilice el teclado numérico y atajos como <b>F12</b> para agilizar su trabajo diario.</td></tr>"
             + "</table><br>"
             + "<h2 style='color: #27ae60;'>2. Ingreso y Seguridad</h2>"
             + "<p>El sistema cuenta con un control de acceso estricto. Cada empleado debe tener su propio usuario.</p>"
             + "<table width='100%' cellpadding='15' cellspacing='0' style='background-color: #fdf2e9; border-left: 6px solid #e67e22; margin-top: 10px;'>"
             + "<tr><td><b>⚠️ SEGURIDAD</b><br>Nunca comparta su contraseña. Su usuario es su firma digital y todas las acciones quedarán registradas a su nombre.</td></tr>"
             + "</table><br>"
             + "<h2 style='color: #27ae60;'>3. Entorno de Trabajo</h2>"
             + "<table width='100%' cellpadding='10' cellspacing='0' style='border: 1px solid #d5dbdb;'>"
             + "<tr><td style='background-color: #2c3e50; color: white;'><b>COMPONENTE</b></td><td style='background-color: #2c3e50; color: white;'><b>FUNCIÓN</b></td></tr>"
             + "<tr><td style='background-color: #ecf0f1;'><b>Menú Lateral</b></td><td>Panel permanente interactivo para navegar entre módulos.</td></tr>"
             + "<tr><td style='background-color: white;'><b>Área Central</b></td><td>Zona dinámica donde se ejecutan los formularios y tablas de datos.</td></tr>"
             + "</table>";
    }

    private String getHtmlAdmin() {
        return "<h2 style='color: #27ae60;'>1. Panel de Administración</h2>"
             + "<p>Este módulo es de acceso exclusivo. Contiene el cerebro del sistema.</p>"
             + "<table width='100%' cellpadding='12' cellspacing='5' style='background-color: #f8f9fa;'>"
             + "<tr>"
             + "<td style='border: 1px solid #bdc3c7; background-color: white;'><b>🏢 Datos de Empresa</b><br><span style='color:#7f8c8d; font-size: 10px;'>Configura RTN, Logo y Políticas de los tickets.</span></td>"
             + "<td style='border: 1px solid #bdc3c7; background-color: white;'><b>👥 Usuarios y Roles</b><br><span style='color:#7f8c8d; font-size: 10px;'>Creación de cuentas, reseteo de claves y asignación Vendedor/Admin.</span></td>"
             + "</tr>"
             + "<tr>"
             + "<td style='border: 1px solid #bdc3c7; background-color: white;'><b>🔒 Permisos</b><br><span style='color:#7f8c8d; font-size: 10px;'>Restringe el acceso a módulos específicos por empleado.</span></td>"
             + "<td style='border: 1px solid #bdc3c7; background-color: white;'><b>🤖 Gestor de Errores</b><br><span style='color:#7f8c8d; font-size: 10px;'>Auditoría técnica impulsada por Gemini AI.</span></td>"
             + "</tr>"
             + "</table>";
    }

    private String getHtmlInventario() {
        return "<h2 style='color: #27ae60;'>1. Gestión de Clientes</h2>"
             + "<p>Mantenga una base de datos actualizada para fidelizar clientes y generar apartados.</p>"
             + "<table width='100%' cellpadding='10' cellspacing='0' style='background-color: #ebf5fb; border: 1px solid #a9cce3; margin-bottom: 20px;'>"
             + "<tr><td><b>Paso rápido:</b> Clientes &gt; <b>+ Nuevo Cliente</b> &gt; Llenar formulario &gt; Guardar.</td></tr>"
             + "</table>"
             + "<h2 style='color: #27ae60;'>2. Catálogo e Inventario</h2>"
             + "<p>Visualice todos los productos. Para un control exacto, cada producto debe tener su código de barras escaneado en el sistema.</p>"
             + "<h2 style='color: #27ae60;'>3. Kardex (Auditoría de Stock)</h2>"
             + "<p>El Kardex es el registro histórico e inalterable de cada artículo.</p>"
             + "<table width='100%' cellpadding='10' cellspacing='0'>"
             + "<tr><td width='50%' style='background-color: #eaeded; border-top: 3px solid #3498db;'><b>ENTRADAS</b><br>Compras a proveedores, devoluciones. Suman al stock.</td>"
             + "<td width='50%' style='background-color: #eaeded; border-top: 3px solid #e74c3c;'><b>SALIDAS</b><br>Ventas, mermas o garantías. Restan al stock.</td></tr>"
             + "</table>";
    }

    private String getHtmlVentas() {
        return "<h2 style='color: #27ae60;'>1. Control de Caja (Fundamental)</h2>"
             + "<table width='100%' cellpadding='15' cellspacing='0' style='background-color: #fce4e4; border-left: 6px solid #c0392b; margin-bottom: 20px;'>"
             + "<tr><td><b>¡ALTO! REGLA DE CAJA</b><br>El sistema de ventas está bloqueado por defecto. Todo cajero debe realizar la <b>Apertura de Caja</b> con su fondo inicial. Al finalizar el turno, es obligatorio hacer el <b>Cierre de Caja</b> para imprimir el cuadre (Arqueo).</td></tr>"
             + "</table>"
             + "<h2 style='color: #27ae60;'>2. Flujo del Punto de Venta (POS)</h2>"
             + "<table width='100%' cellpadding='10' cellspacing='0' style='border: 1px solid #d5dbdb;'>"
             + "<tr><td width='30' align='center' style='background-color: #2c3e50; color: white;'><b>1</b></td><td style='background-color: #f8f9fa;'>Escanee los productos con el lector.</td></tr>"
             + "<tr><td width='30' align='center' style='background-color: #2c3e50; color: white;'><b>2</b></td><td style='background-color: white;'>Seleccione un cliente frecuente (Opcional).</td></tr>"
             + "<tr><td width='30' align='center' style='background-color: #27ae60; color: white;'><b>3</b></td><td style='background-color: #f8f9fa;'><b>Presione F12 (Botón Cobrar).</b></td></tr>"
             + "<tr><td width='30' align='center' style='background-color: #2c3e50; color: white;'><b>4</b></td><td style='background-color: white;'>Ingrese el billete recibido para calcular el cambio.</td></tr>"
             + "<tr><td width='30' align='center' style='background-color: #2c3e50; color: white;'><b>5</b></td><td style='background-color: #f8f9fa;'>Confirme para guardar la venta e imprimir el Ticket.</td></tr>"
             + "</table>";
    }

    private String getHtmlApartados() {
        return "<h2 style='color: #27ae60;'>1. Módulo de Apartados (Layaway)</h2>"
             + "<p>Asegura mercancía con pagos fraccionados en el tiempo.</p>"
             + "<table width='100%' cellpadding='15' cellspacing='0' style='background-color: #f5eef8; border-left: 6px solid #8e44ad; margin-bottom: 20px;'>"
             + "<tr><td><b>PROCESO DE APARTADO:</b><br>1. Seleccione artículos y cliente (Obligatorio).<br>2. Registre el Abono Inicial.<br>3. El sistema reserva el stock físicamente y genera el recibo con saldo pendiente.</td></tr>"
             + "</table>"
             + "<h2 style='color: #27ae60;'>2. Garantías y Mermas</h2>"
             + "<p>¿Un producto salió malo? Valide si está dentro del tiempo de garantía estipulado en las políticas de la empresa.</p>"
             + "<p>Al aplicar la garantía, el artículo se transfiere automáticamente al <b>Inventario Defectuoso</b>, una bodega virtual para que la administración decida si mandarlo al proveedor o registrarlo como pérdida total, manteniendo sus finanzas exactas.</p>";
    }

    private String getHtmlReportes() {
        return "<h2 style='color: #27ae60;'>1. Historial de Ventas</h2>"
             + "<p>Motor de búsqueda de todas las transacciones pasadas.</p>"
             + "<table width='100%' cellpadding='10' cellspacing='0' style='background-color: #e8f8f5; border: 1px solid #1abc9c; margin-bottom: 20px;'>"
             + "<tr><td>✅ <b>Reimpresión:</b> Puede volver a imprimir cualquier ticket antiguo a solicitud del cliente.<br>✅ <b>Auditoría:</b> Revise qué cajero hizo qué factura.</td></tr>"
             + "</table>"
             + "<h2 style='color: #27ae60;'>2. Estadísticas Estratégicas</h2>"
             + "<p>Un Dashboard completo para los tomadores de decisiones:</p>"
             + "<ul>"
             + "<li><b>Gráficos en Tiempo Real:</b> Flujo de ingresos diarios y mensuales.</li>"
             + "<li><b>Top Ventas:</b> Conozca sus productos estrella.</li>"
             + "<li><b>Ticket Promedio:</b> Cuánto gasta cada cliente en su tienda.</li>"
             + "</ul>"
             + "<h2 style='color: #27ae60;'>3. Seguridad Diaria</h2>"
             + "<table width='100%' cellpadding='15' cellspacing='0' style='background-color: #fdf2e9; border-left: 6px solid #d35400;'>"
             + "<tr><td><b>CERRAR SESIÓN:</b><br>Utilice el botón de Cerrar Sesión cada vez que abandone la computadora temporalmente para evitar que otros facturen a su nombre.<br><i>(Nota: Esto bloquea la pantalla, pero NO cierra su caja).</i></td></tr>"
             + "</table>";
    }
}
