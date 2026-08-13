package gui;

import java.awt.*;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;
import dao.ErrorDAO;

public class PanelGestionErrores extends JPanel {
    private JTable tabla;
    private DefaultTableModel modelo;
    private JEditorPane txtAnalisis;
    private JTextArea txtStackTrace;
    private JTextField txtBuscar;
    private ErrorDAO daoError = new ErrorDAO();
    private List<Object[]> logs;
    private JTextField txtChat;
    private JButton btnEnviarChat;
    private List<String[]> historialChat = new java.util.ArrayList<>();
    private JPanel pnlPromptsContainer;
    private JButton btnPrompt1, btnPrompt2, btnPrompt3;
    private JComboBox<String> cmbModelos;

    public PanelGestionErrores() {
        setLayout(new BorderLayout(20, 20));
        setBackground(utilidades.EfectosUI.COLOR_FONDO_PANEL);
        setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        // --- ENCABEZADO Y BUSCADOR ---
        JPanel pnlTop = new JPanel(new BorderLayout(15, 10));
        pnlTop.setOpaque(false);

        JLabel lblTit = new JLabel("Centro de Diagnóstico y Errores del Sistema");
        lblTit.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTit.setForeground(utilidades.EfectosUI.COLOR_TEXTO_TITULO);
        pnlTop.add(lblTit, BorderLayout.WEST);

        txtBuscar = new JTextField();
        txtBuscar.setPreferredSize(new Dimension(300, 40));
        txtBuscar.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtBuscar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(180, 208, 192)),
            BorderFactory.createEmptyBorder(0, 10, 0, 10)
        ));
        txtBuscar.putClientProperty("JTextField.placeholderText", "🔍 Filtrar incidencias...");
        pnlTop.add(txtBuscar, BorderLayout.EAST);
        add(pnlTop, BorderLayout.NORTH);

        // --- TABLA DE LOGS ---
        modelo = new DefaultTableModel(new Object[]{"ID", "Fecha del Suceso", "Módulo / Origen", "Resumen del Error"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tabla = new JTable(modelo);
        tabla.setRowHeight(40);
        tabla.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tabla.setSelectionBackground(new Color(213, 233, 222));
        tabla.setSelectionForeground(new Color(19, 58, 42));
        tabla.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        tabla.getTableHeader().setBackground(new Color(230, 242, 235));
        tabla.getTableHeader().setPreferredSize(new Dimension(0, 40));
        tabla.getSelectionModel().addListSelectionListener(e -> actualizarDetalle());
        
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(modelo);
        tabla.setRowSorter(sorter);
        txtBuscar.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent e) {
                sorter.setRowFilter(RowFilter.regexFilter("(?i)" + txtBuscar.getText()));
            }
        });

        JScrollPane scrollTabla = new JScrollPane(tabla);
        scrollTabla.setBorder(BorderFactory.createLineBorder(new Color(220, 222, 225)));
        
        // --- LADO IZQUIERDO (Tabla + StackTrace) ---
        JPanel pnlIzquierdo = new JPanel(new BorderLayout(0, 20));
        pnlIzquierdo.setOpaque(false);
        pnlIzquierdo.add(scrollTabla, BorderLayout.CENTER);

        JPanel pnlSt = new JPanel(new BorderLayout(0, 5)); pnlSt.setOpaque(false);
        JLabel lblSt = new JLabel(" Detalle Técnico (Código Java)", new IconoCodigo(), SwingConstants.LEFT);
        lblSt.setFont(new Font("Segoe UI", Font.BOLD, 13)); lblSt.setForeground(Color.GRAY);
        txtStackTrace = new JTextArea();
        txtStackTrace.setEditable(false);
        txtStackTrace.setFont(new Font("Consolas", Font.PLAIN, 12));
        txtStackTrace.setBackground(new Color(40, 44, 52));
        txtStackTrace.setForeground(new Color(171, 178, 191));
        JScrollPane scrollSt = new JScrollPane(txtStackTrace);
        scrollSt.setPreferredSize(new Dimension(0, 250));
        pnlSt.add(lblSt, BorderLayout.NORTH); pnlSt.add(scrollSt, BorderLayout.CENTER);
        
        pnlIzquierdo.add(pnlSt, BorderLayout.SOUTH);

        // --- LADO DERECHO (Chat de ORION IA) ---
        JPanel pnlDerecho = new JPanel(new BorderLayout(0, 5));
        pnlDerecho.setOpaque(false);
        pnlDerecho.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));

        JPanel pnlChatTop = new JPanel(new BorderLayout());
        pnlChatTop.setOpaque(false);
        JLabel lblAn = new JLabel(" ORION AI - Asistente Técnico", new IconoBombillo(), SwingConstants.LEFT);
        lblAn.setFont(new Font("Segoe UI", Font.BOLD, 14)); lblAn.setForeground(utilidades.EfectosUI.COLOR_VERDE_PRIMARIO);
        pnlChatTop.add(lblAn, BorderLayout.WEST);
        
        cmbModelos = new JComboBox<>(new String[]{"Orion Flash", "Orion Pro"});
        cmbModelos.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        cmbModelos.setBackground(Color.WHITE);
        pnlChatTop.add(cmbModelos, BorderLayout.EAST);
        
        txtAnalisis = new JEditorPane();
        txtAnalisis.setContentType("text/html");
        txtAnalisis.setEditable(false);
        txtAnalisis.setBackground(Color.WHITE);
        
        HTMLEditorKit kit = new HTMLEditorKit();
        StyleSheet styleSheet = kit.getStyleSheet();
        styleSheet.addRule("body { font-family: Segoe UI, sans-serif; font-size: 13px; color: #333333; padding: 5px; }");
        styleSheet.addRule("b { color: #2c3e50; }");
        styleSheet.addRule("ul { margin-left: 15px; margin-top: 5px; margin-bottom: 5px; }");
        styleSheet.addRule("li { margin-bottom: 4px; }");
        txtAnalisis.setEditorKit(kit);
        
        JScrollPane scrollAn = new JScrollPane(txtAnalisis);
        scrollAn.setBorder(BorderFactory.createLineBorder(new Color(220, 222, 225)));
        
        pnlDerecho.add(pnlChatTop, BorderLayout.NORTH); 
        pnlDerecho.add(scrollAn, BorderLayout.CENTER);

        // --- ZONA INFERIOR DEL CHAT (Prompts + Entrada) ---
        JPanel pnlChatBottom = new JPanel(new BorderLayout(5, 5));
        pnlChatBottom.setOpaque(false);
        pnlChatBottom.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));

        pnlPromptsContainer = new JPanel(new GridLayout(3, 1, 0, 5));
        pnlPromptsContainer.setOpaque(false);
        
        btnPrompt1 = utilidades.EfectosUI.crearBotonBlanco("¿Por qué ocurrió este error?");
        btnPrompt2 = utilidades.EfectosUI.crearBotonBlanco("¿Cómo soluciono esto paso a paso?");
        btnPrompt3 = utilidades.EfectosUI.crearBotonBlanco("Explícame qué significa esto");
        
        btnPrompt1.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnPrompt2.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnPrompt3.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        
        btnPrompt1.addActionListener(e -> enviarPromptRapido(btnPrompt1.getText()));
        btnPrompt2.addActionListener(e -> enviarPromptRapido(btnPrompt2.getText()));
        btnPrompt3.addActionListener(e -> enviarPromptRapido(btnPrompt3.getText()));

        pnlPromptsContainer.add(btnPrompt1);
        pnlPromptsContainer.add(btnPrompt2);
        pnlPromptsContainer.add(btnPrompt3);
        pnlPromptsContainer.setVisible(false);

        JPanel pnlInput = new JPanel(new BorderLayout(5, 0));
        pnlInput.setOpaque(false);
        
        txtChat = new JTextField();
        txtChat.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtChat.putClientProperty("JTextField.placeholderText", "Escribe aquí...");
        txtChat.setEnabled(true);
        
        btnEnviarChat = utilidades.EfectosUI.crearBotonVerde("Enviar");
        btnEnviarChat.setPreferredSize(new Dimension(80, 30));
        btnEnviarChat.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnEnviarChat.setEnabled(true);
        
        btnEnviarChat.addActionListener(e -> enviarMensajeUsuario());
        txtChat.addActionListener(e -> {
            if (btnEnviarChat.isEnabled()) enviarMensajeUsuario();
        });

        pnlInput.add(txtChat, BorderLayout.CENTER);
        pnlInput.add(btnEnviarChat, BorderLayout.EAST);
        
        pnlChatBottom.add(pnlPromptsContainer, BorderLayout.NORTH);
        pnlChatBottom.add(pnlInput, BorderLayout.SOUTH);
        pnlDerecho.add(pnlChatBottom, BorderLayout.SOUTH);

        // --- DIVISOR PRINCIPAL ---
        JSplitPane splitPrincipal = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, pnlIzquierdo, pnlDerecho);
        splitPrincipal.setDividerLocation(0.65); // 65% para tabla/codigo, 35% para chat
        splitPrincipal.setResizeWeight(0.65);
        splitPrincipal.setOpaque(false);
        splitPrincipal.setBorder(null);
        splitPrincipal.setContinuousLayout(true);

        add(splitPrincipal, BorderLayout.CENTER);

        // --- BOTONES DE ACCIÓN ---
        JPanel pnlBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        pnlBotones.setOpaque(false);

        JButton btnPrueba = utilidades.EfectosUI.crearBotonBlanco("Realizar prueba de funcionamiento");
        btnPrueba.setPreferredSize(new Dimension(280, 45));

        JButton btnVaciar = utilidades.EfectosUI.crearBotonPeligro("Limpiar Base de Datos");
        btnVaciar.setPreferredSize(new Dimension(200, 45));

        JButton btnExportarPDF = utilidades.EfectosUI.crearBotonBlanco("Exportar a PDF");
        btnExportarPDF.setPreferredSize(new Dimension(170, 45));
        btnExportarPDF.setFont(new Font("Segoe UI", Font.BOLD, 14));

        btnVaciar.addActionListener(e -> {
            if(utilidades.Mensajes.showConfirmDialog(this, "¿Borrar todos los errores del registro?", "Confirmar", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                if (daoError.limpiarErrores()) {
                    cargarDatos();
                    utilidades.Mensajes.showMessageDialog(this, "Registro de errores limpiado con éxito.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                }
            }
        });
        btnPrueba.addActionListener(e -> {
            try {
                int errorIntencional = 10 / 0; // Genera ArithmeticException
                System.out.println(errorIntencional);
            } catch (Exception ex) {
                utilidades.GestorErrores.registrarError(ex, "Botón de Prueba");
                cargarDatos();
                utilidades.Mensajes.showMessageDialog(this, "Se generó un error de división por cero capturado exitosamente.", "Prueba Exitosa", JOptionPane.INFORMATION_MESSAGE);
            }
        });
        
        btnExportarPDF.addActionListener(e -> exportarErrorPDF());

        pnlBotones.add(btnPrueba);
        pnlBotones.add(btnVaciar);
        pnlBotones.add(btnExportarPDF);
        add(pnlBotones, BorderLayout.SOUTH);

        cargarDatos();
    }

    private void cargarDatos() {
        modelo.setRowCount(0);
        logs = daoError.listarErrores();
        for (Object[] row : logs) {
            modelo.addRow(new Object[]{row[0], row[1], row[2], row[3]});
        }
        txtAnalisis.setText("<html><body>Seleccione un error de la tabla superior para ver su detalle.<br><br>Para obtener una solución paso a paso guiada por Inteligencia Artificial, elija un prompt o escriba abajo.</body></html>");
        txtStackTrace.setText("");
        historialChat.clear();
        txtChat.setEnabled(true);
        btnEnviarChat.setEnabled(true);
        if (pnlPromptsContainer != null) pnlPromptsContainer.setVisible(true);
    }

    private void actualizarDetalle() {
        int f = tabla.getSelectedRow();
        if (f >= 0) {
            Object[] log = logs.get(tabla.convertRowIndexToModel(f));
            txtStackTrace.setText(log[4] != null ? log[4].toString() : "No hay detalles.");
            txtStackTrace.setCaretPosition(0);
            
            txtAnalisis.setText("<html><body><b>Error detectado en:</b> " + log[2] + "<br><b>Resumen:</b> " + log[3] + "<br><br><b>¿En qué te puedo ayudar con este error?</b> Puedes elegir una sugerencia de abajo o escribirme lo que necesites.</body></html>");
            historialChat.clear();
            txtChat.setEnabled(true);
            btnEnviarChat.setEnabled(true);
            if (pnlPromptsContainer != null) pnlPromptsContainer.setVisible(true);
        }
    }

    private void exportarErrorPDF() {
        int f = tabla.getSelectedRow();
        if (f < 0) {
            utilidades.Mensajes.showMessageDialog(this, "Debe seleccionar un error de la tabla primero.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Object[] log = logs.get(tabla.convertRowIndexToModel(f));
        String id = log[0].toString();
        String fecha = log[1].toString();
        String modulo = log[2].toString();
        String resumen = log[3].toString();
        String stackTrace = log[4] != null ? log[4].toString() : "No hay detalles de StackTrace.";
        
        // Obtener el texto del JEditorPane usando Document para evitar etiquetas HTML y Entidades
        String respuestaIA = "";
        try {
            javax.swing.text.Document doc = txtAnalisis.getDocument();
            respuestaIA = doc.getText(0, doc.getLength());
        } catch (Exception ex) {
            respuestaIA = txtAnalisis.getText();
        }

        dao.EmpresaDAO empDao = new dao.EmpresaDAO();
        modelo.Empresa empresa = empDao.obtenerDatos();

        utilidades.GeneradorTickets.generarReporteErrorPDF(id, fecha, modulo, resumen, stackTrace, respuestaIA, empresa);
    }

    private void enviarPromptRapido(String textoPr) {
        txtChat.setText(textoPr);
        enviarMensajeUsuario();
    }

    private void renderizarChatHTML() {
        StringBuilder html = new StringBuilder();
        html.append("<html><body>");
        for (String[] mensaje : historialChat) {
            String role = mensaje[0];
            String text = mensaje[1];
            if (role.equals("user")) {
                String displayTexto = text;
                if (text.contains("Pregunta del usuario:\n")) {
                    displayTexto = text.substring(text.indexOf("Pregunta del usuario:\n") + 22);
                }
                
                if (text.startsWith("Soy un sistema") && !text.contains("Pregunta del usuario:\n")) {
                    html.append("<div style='margin-bottom:10px; color:#2c3e50;'><i>Iniciando análisis del error seleccionado...</i></div><hr style='border: 1px solid #ddd;'>");
                } else {
                    html.append("<div style='margin-bottom:10px; padding:10px; background-color:#e2f0e6; border-radius:10px; border-left: 4px solid #27ae60;'><b>Tú:</b><br>").append(displayTexto).append("</div>");
                }
            } else if (role.equals("model")) {
                text = text.replace("```html", "").replace("```", "").trim();
                html.append("<div style='margin-bottom:15px; padding:10px; background-color:#ffffff; border-radius:10px; border-left: 4px solid #3498db;'><b>IA de Orion:</b><br>").append(text).append("</div>");
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

    private void enviarMensajeUsuario() {
        String texto = txtChat.getText().trim();
        if (texto.isEmpty()) return;

        txtChat.setText("");
        txtChat.setEnabled(false);
        btnEnviarChat.setEnabled(false);
        if (pnlPromptsContainer != null) pnlPromptsContainer.setVisible(false);

        // Si el historialChat está vacío, inyectamos el contexto y el stack trace ocultamente antes del primer mensaje
        if (historialChat.isEmpty()) {
            int f = tabla.getSelectedRow();
            String stackTrace = "";
            String prefix = "";
            if (f >= 0) {
                Object[] log = logs.get(tabla.convertRowIndexToModel(f));
                stackTrace = log[4] != null ? log[4].toString() : "Error desconocido sin traza";
                prefix = "Encontré este error en consola y necesito que me ayudes.\n\n";
            } else {
                stackTrace = "Ningún error seleccionado en la tabla.";
                prefix = "El administrador está realizando una consulta técnica libre o reportando un problema visual del sistema que no generó un registro automático.\n\n";
            }
            
            String systemContext = "Soy un sistema de facturacion y tengo un sistema de gestion de errores. "
                + prefix
                + "REGLA MUY IMPORTANTE: Si ves que el tema es complejo, requiere tocar código fuente de Java, modificar clases principales, o hacer configuraciones avanzadas de entorno, DEBES INDÍCARLE EXPLÍCITAMENTE AL USUARIO QUE DEBE CONTACTARSE CON EL DEPARTAMENTO DE TI (Soporte Técnico) para que lo resuelvan.\n\n"
                + "REGLAS DE FORMATO:\n"
                + "1. Genera tu respuesta EXCLUSIVAMENTE en formato HTML básico, compatible con JEditorPane de Java.\n"
                + "2. Usa etiquetas como <b>, <i>, <br>, <ul>, <li> para dar formato elegante.\n"
                + "3. NO USES Markdown bajo ninguna circunstancia (ni asteriscos **, ni numerales #).\n"
                + "4. NO uses emojis.\n"
                + "5. No incluyas las etiquetas <html>, <head> ni <body>.\n"
                + "6. IMPORTANTE: NO uses entidades HTML para los caracteres (no uses &#225; ni &aacute;, usa directamente á, é, í, ó, ú, ñ). Usa UTF-8 nativo.\n\n"
                + "CONTEXTO DEL ERROR ACTUAL:\n" + stackTrace + "\n\nPregunta del usuario:\n";
            
            historialChat.add(new String[]{"user", systemContext + texto});
        } else {
            historialChat.add(new String[]{"user", texto});
        }
        
        historialChat.add(new String[]{"model_thinking", "⏳ <i>Orion AI está analizando el error...</i>"});
        
        // Renderizar chat temporal con indicador de escritura
        StringBuilder html = new StringBuilder();
        html.append("<html><body>");
        for (String[] mensaje : historialChat) {
            String role = mensaje[0];
            String text = mensaje[1];
            if (role.equals("user")) {
                String displayTexto = text;
                if (text.contains("Pregunta del usuario:\n")) {
                    displayTexto = text.substring(text.indexOf("Pregunta del usuario:\n") + 22);
                }
                
                if (text.startsWith("Soy un sistema") && !text.contains("Pregunta del usuario:\n")) {
                    html.append("<div style='margin-bottom:10px; color:#2c3e50;'><i>Iniciando análisis del error seleccionado...</i></div><hr style='border: 1px solid #ddd;'>");
                } else {
                    html.append("<div style='margin-bottom:10px; padding:10px; background-color:#e2f0e6; border-radius:10px; border-left: 4px solid #27ae60;'><b>Tú:</b><br>").append(displayTexto).append("</div>");
                }
            } else if (role.equals("model")) {
                text = text.replace("```html", "").replace("```", "").trim();
                html.append("<div style='margin-bottom:15px; padding:10px; background-color:#ffffff; border-radius:10px; border-left: 4px solid #3498db;'><b>IA de Orion:</b><br>").append(text).append("</div>");
            }
        }
        html.append("<div style='margin-bottom:10px; color:#7f8c8d; font-style:italic;'>La IA está escribiendo...</div>");
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

        String seleccion = (String) cmbModelos.getSelectedItem();
        String modeloReal = "gemini-3.6-flash"; // Por defecto
        if (seleccion != null) {
            if (seleccion.equals("Orion Pro")) modeloReal = "gemini-3.6-pro";
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
                    historialChat.add(new String[]{"model", "<b>Error en la red:</b> " + ex.getMessage()});
                    renderizarChatHTML();
                }
            }
        };
        worker.execute();
    }

    // --- ICONOS PERSONALIZADOS DIBUJADOS A MANO ---
    private class IconoBombillo implements Icon {
        @Override public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(241, 196, 15));
            g2.fillOval(x + 2, y + 2, 12, 12);
            g2.fillRect(x + 5, y + 12, 6, 4);
            g2.setColor(new Color(149, 165, 166));
            g2.fillRoundRect(x + 6, y + 16, 4, 3, 2, 2);
            g2.dispose();
        }
        @Override public int getIconWidth() { return 16; }
        @Override public int getIconHeight() { return 18; }
    }

    private class IconoCodigo implements Icon {
        @Override public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(Color.GRAY);
            g2.setStroke(new BasicStroke(2));
            g2.drawLine(x + 5, y + 3, x + 2, y + 8);
            g2.drawLine(x + 2, y + 8, x + 5, y + 13);
            g2.drawLine(x + 11, y + 3, x + 14, y + 8);
            g2.drawLine(x + 14, y + 8, x + 11, y + 13);
            g2.dispose();
        }
        @Override public int getIconWidth() { return 16; }
        @Override public int getIconHeight() { return 16; }
    }
}
