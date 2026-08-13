package gui;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.List;
import javax.swing.*;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;

public class PanelOrionAI extends JPanel {

    private JEditorPane txtAnalisis;
    private JTextField txtChat;
    private JButton btnEnviarChat;
    private List<String[]> historialChat = new java.util.ArrayList<>();
    private JPanel pnlPromptsContainer;
    private JButton btnPrompt1, btnPrompt2, btnPrompt3;
    private JComboBox<String> cmbModelos;

    public PanelOrionAI() {
        setLayout(new BorderLayout(20, 20));
        setBackground(utilidades.EfectosUI.COLOR_FONDO_PANEL);
        setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        initComponents();
        inyectarContextoDelSistema();
    }

    private void initComponents() {
        // --- CABECERA ---
        JPanel pnlChatTop = new JPanel(new BorderLayout());
        pnlChatTop.setOpaque(false);
        pnlChatTop.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        
        JLabel lblAn = new JLabel(" ORION AI - Asistente de Operaciones", new IconoRobot(), SwingConstants.LEFT);
        lblAn.setFont(new Font("Segoe UI", Font.BOLD, 18)); 
        lblAn.setForeground(utilidades.EfectosUI.COLOR_VERDE_PRIMARIO);
        pnlChatTop.add(lblAn, BorderLayout.WEST);
        
        cmbModelos = new JComboBox<>(new String[]{"Orion Flash", "Orion Pro"});
        cmbModelos.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        cmbModelos.setBackground(Color.WHITE);
        pnlChatTop.add(cmbModelos, BorderLayout.EAST);
        
        add(pnlChatTop, BorderLayout.NORTH);

        // --- ZONA DE CHAT (Centro) ---
        txtAnalisis = new JEditorPane();
        txtAnalisis.setContentType("text/html");
        txtAnalisis.setEditable(false);
        txtAnalisis.setBackground(Color.WHITE);
        
        HTMLEditorKit kit = new HTMLEditorKit();
        StyleSheet styleSheet = kit.getStyleSheet();
        styleSheet.addRule("body { font-family: Segoe UI, sans-serif; font-size: 14px; color: #333333; padding: 10px; }");
        styleSheet.addRule("b { color: #2c3e50; }");
        styleSheet.addRule("ul { margin-left: 20px; margin-top: 5px; margin-bottom: 5px; }");
        styleSheet.addRule("li { margin-bottom: 4px; }");
        txtAnalisis.setEditorKit(kit);
        
        JScrollPane scrollAn = new JScrollPane(txtAnalisis);
        scrollAn.setBorder(BorderFactory.createLineBorder(new Color(220, 222, 225)));
        
        add(scrollAn, BorderLayout.CENTER);

        // --- ZONA INFERIOR (Prompts y Entrada) ---
        JPanel pnlChatBottom = new JPanel(new BorderLayout(10, 10));
        pnlChatBottom.setOpaque(false);
        pnlChatBottom.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        pnlPromptsContainer = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        pnlPromptsContainer.setOpaque(false);
        
        btnPrompt1 = crearBotonPrompt("¿Qué es Orion Sys?");
        btnPrompt2 = crearBotonPrompt("¿Cuál es el objetivo principal?");
        btnPrompt3 = crearBotonPrompt("¿Qué funciones tienes disponibles?");
        
        pnlPromptsContainer.add(btnPrompt1);
        pnlPromptsContainer.add(btnPrompt2);
        pnlPromptsContainer.add(btnPrompt3);
        
        pnlChatBottom.add(pnlPromptsContainer, BorderLayout.NORTH);

        JPanel pnlInput = new JPanel(new BorderLayout(10, 0));
        pnlInput.setOpaque(false);
        
        txtChat = new JTextField();
        txtChat.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtChat.setPreferredSize(new Dimension(0, 35));
        txtChat.putClientProperty("JTextField.placeholderText", "Haz una pregunta sobre el sistema...");
        
        btnEnviarChat = new JButton("Enviar");
        btnEnviarChat.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnEnviarChat.setBackground(new Color(39, 110, 70));
        btnEnviarChat.setForeground(Color.WHITE);
        btnEnviarChat.setFocusPainted(false);
        btnEnviarChat.setBorderPainted(false);
        btnEnviarChat.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        pnlInput.add(txtChat, BorderLayout.CENTER);
        pnlInput.add(btnEnviarChat, BorderLayout.EAST);
        
        pnlChatBottom.add(pnlInput, BorderLayout.SOUTH);
        add(pnlChatBottom, BorderLayout.SOUTH);

        // --- EVENTOS ---
        btnEnviarChat.addActionListener(e -> enviarMensajeUsuario());
        txtChat.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    enviarMensajeUsuario();
                }
            }
        });
        
        btnPrompt1.addActionListener(e -> { txtChat.setText("¿Qué es Orion Sys?"); enviarMensajeUsuario(); });
        btnPrompt2.addActionListener(e -> { txtChat.setText("¿Cuál es el objetivo principal del sistema?"); enviarMensajeUsuario(); });
        btnPrompt3.addActionListener(e -> { txtChat.setText("¿Qué funciones principales y módulos tienes?"); enviarMensajeUsuario(); });
    }

    private JButton crearBotonPrompt(String texto) {
        JButton btn = new JButton(texto);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btn.setBackground(new Color(240, 242, 245));
        btn.setForeground(new Color(44, 62, 80));
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 205, 210)),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btn.setBackground(new Color(225, 230, 235)); }
            public void mouseExited(MouseEvent e) { btn.setBackground(new Color(240, 242, 245)); }
        });
        return btn;
    }

    private void inyectarContextoDelSistema() {
        String contextoSistema = "Eres Orion AI, el asistente técnico y manual de usuario virtual de ORION SYS. " +
            "ORION SYS es el Sistema de Organización de Recursos, Inventarios, Operaciones y Negocios de la empresa INVERSIONES OLVAN. " +
            "Tu objetivo es ayudar a los empleados respondiendo dudas sobre cómo funciona el sistema, basado exclusivamente en el siguiente contexto:\n\n" +
            "1. OBJETIVO DEL SISTEMA: ORION SYS centraliza, automatiza y optimiza todos los procesos operativos de Inversiones Olvan.\n" +
            "2. INGRESO Y SEGURIDAD: Control de acceso estricto por usuario. Nunca se debe compartir la contraseña.\n" +
            "3. ADMINISTRACIÓN: Contiene configuración de Datos de Empresa (RTN, Logo), Usuarios y Roles (Vendedor/Admin), Permisos, y Gestor de Errores impulsado por Gemini AI.\n" +
            "4. CLIENTES E INVENTARIO: Gestión de Clientes para apartados. Catálogo e Inventario para ver productos. Kardex para auditoría inalterable (Entradas y Salidas).\n" +
            "5. CAJA Y VENTAS (POS): Regla de oro: Hacer Apertura de Caja al iniciar y Cierre de Caja (Arqueo) al finalizar. El flujo POS es: escanear productos, presionar F12 (Botón Cobrar), ingresar billete, confirmar e imprimir.\n" +
            "6. APARTADOS (LAYAWAY): Permite reservar stock con pagos fraccionados.\n" +
            "7. GARANTÍAS Y MERMAS: Artículos defectuosos van al Inventario Defectuoso, donde administración decide si mandarlo al proveedor o reportar pérdida.\n" +
            "8. REPORTES Y ESTADÍSTICAS: Historial de Ventas (reimpresión de tickets), Dashboard con gráficos en tiempo real, Top Ventas y Ticket Promedio.\n\n" +
            "Si un empleado te hace una pregunta general como '¿Qué funciones tienes?', enumera los módulos disponibles de forma amigable. Habla siempre en español, con un tono profesional, amable y conciso.";
            
        historialChat.add(new String[]{"user", contextoSistema});
        historialChat.add(new String[]{"model", "¡Entendido! Soy Orion AI y estoy listo para ayudar a los empleados de Inversiones Olvan."});
        
        renderizarChatHTML();
    }

    private void enviarMensajeUsuario() {
        String msg = txtChat.getText().trim();
        if (msg.isEmpty()) return;

        txtChat.setText("");
        txtChat.setEnabled(false);
        btnEnviarChat.setEnabled(false);
        
        historialChat.add(new String[]{"user", msg});
        historialChat.add(new String[]{"model_thinking", "⏳ <i>Orion AI está pensando su respuesta...</i>"});
        renderizarChatHTML();

        String seleccion = (String) cmbModelos.getSelectedItem();
        String modeloReal = "gemini-3.6-flash"; // Por defecto
        if (seleccion != null && seleccion.equals("Orion Pro")) {
            modeloReal = "gemini-3.6-pro";
        }
        
        final String modeloFinal = modeloReal;

        SwingWorker<String, Void> worker = new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() throws Exception {
                return utilidades.ClienteGemini.enviarMensajeChat(historialChat, modeloFinal);
            }
            @Override
            protected void done() {
                txtChat.setEnabled(true);
                btnEnviarChat.setEnabled(true);
                txtChat.requestFocus();
                try {
                    String respuesta = get();
                    if (!historialChat.isEmpty() && historialChat.get(historialChat.size() - 1)[0].equals("model_thinking")) {
                        historialChat.remove(historialChat.size() - 1);
                    }
                    historialChat.add(new String[]{"model", respuesta});
                    renderizarChatHTML();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        };
        worker.execute();
    }

    private void renderizarChatHTML() {
        StringBuilder html = new StringBuilder();
        html.append("<html><body>");
        
        // Empezamos desde el índice 2 para saltarnos el contexto del sistema inyectado
        for (int i = 2; i < historialChat.size(); i++) {
            String[] m = historialChat.get(i);
            String rol = m[0];
            String contenido = m[1];

            if (rol.equals("user")) {
                html.append("<div style='background-color: #e8f5e9; padding: 10px; margin-bottom: 10px; border-left: 4px solid #4caf50;'>")
                    .append("<b>Tú:</b><br>")
                    .append(contenido.replace("\n", "<br>"))
                    .append("</div>");
            } else if (rol.equals("model_thinking")) {
                html.append("<div style='padding: 10px; margin-bottom: 15px; color: #7f8c8d;'>")
                    .append(contenido)
                    .append("</div>");
            } else {
                // Formatear markdown básico de Gemini a HTML
                contenido = contenido.replace("**", "<b>").replace("*", "<li>"); // Simplificación rápida
                contenido = contenido.replace("\n", "<br>");
                html.append("<div style='background-color: #f8f9fa; padding: 10px; margin-bottom: 15px; border-left: 4px solid #3498db;'>")
                    .append("<b>IA de Orion:</b><br>")
                    .append(contenido)
                    .append("</div>");
            }
        }
        html.append("</body></html>");
        txtAnalisis.setText(html.toString());
        
        SwingUtilities.invokeLater(() -> {
            Component parent = txtAnalisis.getParent();
            if (parent instanceof JViewport) {
                JScrollPane scroll = (JScrollPane) parent.getParent();
                JScrollBar vertical = scroll.getVerticalScrollBar();
                vertical.setValue(vertical.getMaximum());
            }
        });
    }

    // --- ICONO ROBOT SIMPLE ---
    private class IconoRobot implements Icon {
        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(utilidades.EfectosUI.COLOR_VERDE_PRIMARIO);
            g2.setStroke(new BasicStroke(1.5f));
            
            g2.drawRoundRect(x + 4, y + 4, 16, 14, 4, 4); // Cabeza
            g2.drawRect(x + 2, y + 9, 2, 4); // Oreja izq
            g2.drawRect(x + 20, y + 9, 2, 4); // Oreja der
            g2.drawLine(x + 12, y + 1, x + 12, y + 4); // Antena
            g2.drawOval(x + 11, y - 1, 2, 2); // Punta antena
            g2.fillRect(x + 7, y + 8, 3, 3); // Ojo izq
            g2.fillRect(x + 14, y + 8, 3, 3); // Ojo der
            g2.drawLine(x + 8, y + 14, x + 16, y + 14); // Boca
            
            g2.dispose();
        }

        @Override
        public int getIconWidth() { return 24; }
        @Override
        public int getIconHeight() { return 24; }
    }
}
