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
    private JButton btnAnalizarIA; // Promoted to class field to disable it during request

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
        
        // --- PANEL DE DETALLE INFERIOR ---
        JPanel pnlDetalle = new JPanel(new GridLayout(1, 2, 20, 0));
        pnlDetalle.setOpaque(false);
        pnlDetalle.setPreferredSize(new Dimension(0, 250));

        // Lado Izquierdo: Instrucciones / Análisis IA
        JPanel pnlAn = new JPanel(new BorderLayout(0, 5)); pnlAn.setOpaque(false);
        JLabel lblAn = new JLabel(" Pasos para Solucionar con IA", new IconoBombillo(), SwingConstants.LEFT);
        lblAn.setFont(new Font("Segoe UI", Font.BOLD, 13)); lblAn.setForeground(utilidades.EfectosUI.COLOR_VERDE_PRIMARIO);
        
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
        pnlAn.add(lblAn, BorderLayout.NORTH); pnlAn.add(scrollAn, BorderLayout.CENTER);

        // Lado Derecho: StackTrace Técnico
        JPanel pnlSt = new JPanel(new BorderLayout(0, 5)); pnlSt.setOpaque(false);
        JLabel lblSt = new JLabel(" Detalle Técnico (Código Java)", new IconoCodigo(), SwingConstants.LEFT);
        lblSt.setFont(new Font("Segoe UI", Font.BOLD, 13)); lblSt.setForeground(Color.GRAY);
        txtStackTrace = new JTextArea();
        txtStackTrace.setEditable(false);
        txtStackTrace.setFont(new Font("Consolas", Font.PLAIN, 12));
        txtStackTrace.setBackground(new Color(40, 44, 52));
        txtStackTrace.setForeground(new Color(171, 178, 191));
        JScrollPane scrollSt = new JScrollPane(txtStackTrace);
        pnlSt.add(lblSt, BorderLayout.NORTH); pnlSt.add(scrollSt, BorderLayout.CENTER);

        pnlDetalle.add(pnlAn);
        pnlDetalle.add(pnlSt);

        JPanel pnlCentro = new JPanel(new BorderLayout(0, 20));
        pnlCentro.setOpaque(false);
        pnlCentro.add(scrollTabla, BorderLayout.CENTER);
        pnlCentro.add(pnlDetalle, BorderLayout.SOUTH);
        add(pnlCentro, BorderLayout.CENTER);

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
        
        btnAnalizarIA = utilidades.EfectosUI.crearBotonVerde("Analizar con IA");
        btnAnalizarIA.setIcon(new IconoIA());
        btnAnalizarIA.setPreferredSize(new Dimension(200, 45));
        btnAnalizarIA.setFont(new Font("Segoe UI", Font.BOLD, 14));

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
        
        btnAnalizarIA.addActionListener(e -> ejecutarPromptIA());
        btnExportarPDF.addActionListener(e -> exportarErrorPDF());

        pnlBotones.add(btnPrueba);
        pnlBotones.add(btnVaciar);
        pnlBotones.add(btnExportarPDF);
        pnlBotones.add(btnAnalizarIA);
        add(pnlBotones, BorderLayout.SOUTH);

        cargarDatos();
    }

    private void cargarDatos() {
        modelo.setRowCount(0);
        logs = daoError.listarErrores();
        for (Object[] row : logs) {
            modelo.addRow(new Object[]{row[0], row[1], row[2], row[3]});
        }
        txtAnalisis.setText("<html><body>Seleccione un error de la tabla superior para ver su detalle.<br><br>Para obtener una solución paso a paso guiada por Inteligencia Artificial, presione el botón <b>'Analizar con IA'</b>.</body></html>");
        txtStackTrace.setText("");
    }

    private void actualizarDetalle() {
        int f = tabla.getSelectedRow();
        if (f >= 0) {
            Object[] log = logs.get(tabla.convertRowIndexToModel(f));
            txtStackTrace.setText(log[4] != null ? log[4].toString() : "No hay detalles.");
            txtStackTrace.setCaretPosition(0);
            
            txtAnalisis.setText("<html><body><b>Error detectado en:</b> " + log[2] + "<br><b>Resumen:</b> " + log[3] + "<br><br>Para analizar este problema y generar un manual de solución rápida, haz clic en <b>'Analizar con IA'</b>.</body></html>");
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

    private void ejecutarPromptIA() {
        int f = tabla.getSelectedRow();
        if (f < 0) {
            utilidades.Mensajes.showMessageDialog(this, "Debe seleccionar un error de la tabla primero.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Object[] log = logs.get(tabla.convertRowIndexToModel(f));
        String stackTrace = log[4] != null ? log[4].toString() : "Error desconocido sin traza";

        btnAnalizarIA.setEnabled(false);
        btnAnalizarIA.setText("Analizando...");

        Timer timer = new Timer(500, new java.awt.event.ActionListener() {
            int puntos = 0;
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                puntos = (puntos + 1) % 4;
                String dots = "";
                for (int i = 0; i < puntos; i++) dots += ".";
                txtAnalisis.setText("<html><body><div style='text-align: center; margin-top: 20px; color: #7f8c8d;'>"
                        + "<b>Consultando a la Inteligencia Artificial (Gemini)</b><br><br>"
                        + "Por favor espere" + dots + "</div></body></html>");
            }
        });
        timer.start();

        SwingWorker<String, Void> worker = new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() throws Exception {
                return utilidades.ClienteGemini.analizarError(stackTrace);
            }

            @Override
            protected void done() {
                timer.stop();
                btnAnalizarIA.setEnabled(true);
                btnAnalizarIA.setText("Analizar con IA");
                try {
                    String respuesta = get();
                    // Limpiar posibles etiquetas markdown residuales que la IA terquea en mandar
                    respuesta = respuesta.replace("```html", "").replace("```", "").trim();
                    txtAnalisis.setText("<html><body>" + respuesta + "</body></html>");
                } catch (Exception ex) {
                    txtAnalisis.setText("<html><body><b>Ocurrió un error al obtener la respuesta de la IA.</b><br><br>" + ex.getMessage() + "</body></html>");
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

    private class IconoIA implements Icon {
        @Override public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(Color.WHITE);
            g2.drawOval(x, y, 16, 16);
            g2.fillOval(x + 4, y + 4, 3, 3);
            g2.fillOval(x + 9, y + 4, 3, 3);
            g2.drawArc(x + 4, y + 8, 8, 4, 0, -180);
            g2.dispose();
        }
        @Override public int getIconWidth() { return 18; }
        @Override public int getIconHeight() { return 18; }
    }
}
