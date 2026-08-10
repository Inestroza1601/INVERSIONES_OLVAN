package gui;

import dao.VentasDAO;
import utilidades.SesionGlobal;
import utilidades.GeneradorTickets;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;

public class PanelHistorialVentas extends JPanel {
    private VentasDAO dao;
    private List<Object[]> ventas;
    private JTable tablaVentas;
    private DefaultTableModel modeloTabla;
    private JTextField txtBuscar;
    private JComboBox<String> cmbOrdenar;

    public PanelHistorialVentas() {
        this.dao = new VentasDAO();
        iniciarDiseno();
        cargarVentas();
    }

    private void iniciarDiseno() {
        this.setLayout(new BorderLayout(20, 20));
        this.setBackground(utilidades.EfectosUI.COLOR_FONDO_PANEL); // Verde Vintage
        this.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        // Cabecera
        JPanel pnlCabecera = new JPanel(new BorderLayout());
        pnlCabecera.setOpaque(false);

        JLabel lblTitulo = new JLabel("Historial de Ventas");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitulo.setForeground(utilidades.EfectosUI.COLOR_TEXTO_TITULO);
        pnlCabecera.add(lblTitulo, BorderLayout.WEST);

        JPanel pnlBuscar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        pnlBuscar.setOpaque(false);
        
        String[] opcionesOrden = { "M\u00E1s Recientes", "M\u00E1s Antiguos", "Mayor a Menor (Total)", "Menor a Mayor (Total)", "Cliente (A-Z)", "Cliente (Z-A)" };
        cmbOrdenar = new JComboBox<>(opcionesOrden);
        cmbOrdenar.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        cmbOrdenar.setPreferredSize(new Dimension(200, 35));
        cmbOrdenar.setBackground(Color.WHITE);
        cmbOrdenar.addActionListener(e -> {
            if (ventas != null) {
                ordenarVentasLista();
                filtrarVentas();
            }
        });
        pnlBuscar.add(cmbOrdenar);
        
        txtBuscar = new JTextField(15);
        txtBuscar.putClientProperty("JTextField.placeholderText", "Buscar por cliente, DNI, fecha o m\u00E9todo...");
        txtBuscar.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtBuscar.setPreferredSize(new Dimension(350, 38));
        txtBuscar.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(220, 222, 225)),
                BorderFactory.createEmptyBorder(0, 8, 0, 8)));
        txtBuscar.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { filtrarVentas(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { filtrarVentas(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filtrarVentas(); }
        });
        txtBuscar.addActionListener(e -> filtrarVentas()); // Keep enter key support just in case
        pnlBuscar.add(txtBuscar);
        pnlCabecera.add(pnlBuscar, BorderLayout.EAST);

        this.add(pnlCabecera, BorderLayout.NORTH);

        // Tabla
        String[] cols = { "ID Venta", "Tipo", "Nombre", "Fecha / Hora", "Cliente", "M\u00E9todo Pago", "Vendedor", "Total" };
        modeloTabla = new DefaultTableModel(null, cols) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        tablaVentas = new JTable(modeloTabla);
        tablaVentas.setRowHeight(32);
        tablaVentas.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        
        // Ajustar anchos
        tablaVentas.getColumnModel().getColumn(0).setPreferredWidth(60); // ID
        tablaVentas.getColumnModel().getColumn(1).setPreferredWidth(90); // Tipo
        tablaVentas.getColumnModel().getColumn(2).setPreferredWidth(250); // Nombre

        JScrollPane scroll = new JScrollPane(tablaVentas);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(220, 222, 225)));
        scroll.getViewport().setBackground(Color.WHITE);
        this.add(scroll, BorderLayout.CENTER);

        // Botones de accion (abajo-derecha con padding)
        JPanel pnlBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 15));
        pnlBotones.setOpaque(false);
        pnlBotones.setBackground(new Color(213, 233, 222));
        pnlBotones.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(190, 215, 200)));

        JButton btnDetalles = utilidades.EfectosUI.crearBotonVerde("Ver Detalles");
        btnDetalles.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btnDetalles.setPreferredSize(new Dimension(180, 50));
        btnDetalles.addActionListener(e -> verDetallesVenta());

        JButton btnReimprimir = utilidades.EfectosUI.crearBotonVerde("Reimprimir Ticket");
        btnReimprimir.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btnReimprimir.setPreferredSize(new Dimension(180, 50));
        btnReimprimir.addActionListener(e -> reimprimirTicket());

        pnlBotones.add(btnDetalles);
        pnlBotones.add(btnReimprimir);
        this.add(pnlBotones, BorderLayout.SOUTH);
    }

    private void cargarVentas() {
        modeloTabla.setRowCount(0);
        ventas = dao.listarVentas();
        ordenarVentasLista();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        for (Object[] v : ventas) {
            String ref = v[8] != null ? v[8].toString() : "";
            String tipoVenta = ref.startsWith("Pago de Apartado #") ? "Apartado" : "Normal";
            if (tipoVenta.equals("Apartado")) {
                tipoVenta = "Apartado (" + ref.replace("Pago de Apartado #", "").trim() + ")";
            }

            modeloTabla.addRow(new Object[] {
                    v[0], // id_ventas
                    tipoVenta, // Tipo
                    v[11] != null ? v[11].toString() : "", // Nombre
                    v[1] != null ? sdf.format((java.util.Date) v[1]) : "N/A", // fecha_venta
                    v[2], // cliente
                    v[3], // metodo
                    v[4], // vendedor
                    "L " + String.format("%,.2f", (double) v[7]) // total
            });
        }
    }

    private void filtrarVentas() {
        String filter = txtBuscar.getText().toLowerCase().trim();
        if (filter.isEmpty()) {
            cargarVentas();
            return;
        }

        modeloTabla.setRowCount(0);
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        for (Object[] v : ventas) {
            String client = v[2].toString().toLowerCase();
            String method = v[3].toString().toLowerCase();
            String seller = v[4].toString().toLowerCase();
            String idStr = v[0].toString();
            String dni = v[9] != null ? v[9].toString().toLowerCase().replace("-", "").replace(" ", "") : "";
            String fecha = v[1] != null ? sdf.format((java.util.Date) v[1]).toLowerCase() : "";
            String product = v[11] != null ? v[11].toString().toLowerCase() : "";
            
            String filterClean = filter.replace("-", "").replace(" ", "");

            if (client.contains(filter) || method.contains(filter) || seller.contains(filter)
                    || idStr.contains(filter) || dni.contains(filterClean) || dni.contains(filter) || fecha.contains(filter)
                    || product.contains(filter)) {
                
                String ref = v[8] != null ? v[8].toString() : "";
                String tipoVenta = ref.startsWith("Pago de Apartado #") ? "Apartado" : "Normal";
                if (tipoVenta.equals("Apartado")) {
                    tipoVenta = "Apartado (" + ref.replace("Pago de Apartado #", "").trim() + ")";
                }

                modeloTabla.addRow(new Object[] {
                        v[0],
                        tipoVenta,
                        v[11] != null ? v[11].toString() : "",
                        v[1] != null ? sdf.format((java.util.Date) v[1]) : "N/A",
                        v[2],
                        v[3],
                        v[4],
                        "L " + String.format("%,.2f", (double) v[7])
                });
            }
        }
    }

    private void ordenarVentasLista() {
        if (ventas == null || ventas.isEmpty() || cmbOrdenar == null) return;
        String seleccion = (String) cmbOrdenar.getSelectedItem();
        if (seleccion == null) return;
        
        ventas.sort((v1, v2) -> {
            try {
                switch (seleccion) {
                    case "M\u00E1s Antiguos":
                        java.util.Date d1 = (java.util.Date) v1[1];
                        java.util.Date d2 = (java.util.Date) v2[1];
                        if (d1 == null || d2 == null) return 0;
                        return d1.compareTo(d2);
                    case "M\u00E1s Recientes":
                        java.util.Date d1_r = (java.util.Date) v1[1];
                        java.util.Date d2_r = (java.util.Date) v2[1];
                        if (d1_r == null || d2_r == null) return 0;
                        return d2_r.compareTo(d1_r);
                    case "Mayor a Menor (Total)":
                        Double t1 = (Double) v1[7];
                        Double t2 = (Double) v2[7];
                        return t2.compareTo(t1);
                    case "Menor a Mayor (Total)":
                        Double t1_m = (Double) v1[7];
                        Double t2_m = (Double) v2[7];
                        return t1_m.compareTo(t2_m);
                    case "Cliente (A-Z)":
                        String c1 = v1[2] != null ? v1[2].toString() : "";
                        String c2 = v2[2] != null ? v2[2].toString() : "";
                        return c1.compareToIgnoreCase(c2);
                    case "Cliente (Z-A)":
                        String c1_z = v1[2] != null ? v1[2].toString() : "";
                        String c2_z = v2[2] != null ? v2[2].toString() : "";
                        return c2_z.compareToIgnoreCase(c1_z);
                    default:
                        return 0;
                }
            } catch (Exception e) {
                return 0;
            }
        });
    }

    private void verDetallesVenta() {
        int selectedRow = tablaVentas.getSelectedRow();
        if (selectedRow < 0) {
            utilidades.Mensajes.showMessageDialog(this, "Seleccione una venta de la lista.", "Aviso",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int idVenta = (int) tablaVentas.getValueAt(selectedRow, 0);
        Map<String, Object> venta = dao.obtenerReciboPorId(idVenta);
        if (venta == null || venta.isEmpty())
            return;

        List<Object[]> detalles = (List<Object[]>) venta.get("detalles");

        Window parent = SwingUtilities.getWindowAncestor(this);
        JDialog dialog = new JDialog((Frame) parent, "Detalles de Venta #" + idVenta, true);
        dialog.setSize(650, 520);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());
        dialog.getContentPane().setBackground(Color.WHITE);
        
        // Cabecera estilizada
        JPanel pnlCabecera = new JPanel(new BorderLayout());
        pnlCabecera.setBackground(new Color(39, 174, 96)); // Verde
        pnlCabecera.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        JLabel lblTitulo = new JLabel("Detalles de Venta #" + idVenta);
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitulo.setForeground(Color.WHITE);
        pnlCabecera.add(lblTitulo, BorderLayout.WEST);
        dialog.add(pnlCabecera, BorderLayout.NORTH);
        
        // Contenedor Central
        JPanel pnlCentro = new JPanel(new BorderLayout(10, 15));
        pnlCentro.setBackground(Color.WHITE);
        pnlCentro.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));

        String ref = (String) venta.get("ref");
        String banco = (String) venta.get("banco");
        boolean esApartado = ref != null && ref.startsWith("Pago de Apartado #");
        boolean esGarantia = ref != null && (ref.contains("DEV. GARANTIA") || ref.contains("CAMBIO GARANTIA"));
        
        String numApartado = "";
        if (esApartado) {
            numApartado = ref.replace("Pago de Apartado #", "").trim();
        }

        // Info General
        JPanel pnlInfo = new JPanel(new GridLayout(4, 2, 15, 12));
        pnlInfo.setBackground(Color.WHITE);
        
        pnlInfo.add(new JLabel("<html><span style='color:#7f8c8d; font-family:Segoe UI'>Cliente:</span><br><b style='font-size:14px; color:#2c3e50'>" + venta.get("cliente") + "</b></html>"));
        pnlInfo.add(new JLabel("<html><span style='color:#7f8c8d; font-family:Segoe UI'>Fecha / Hora:</span><br><b style='font-size:14px; color:#2c3e50'>" + venta.get("fecha") + "</b></html>"));
        pnlInfo.add(new JLabel("<html><span style='color:#7f8c8d; font-family:Segoe UI'>M\u00E9todo de Pago:</span><br><b style='font-size:14px; color:#2c3e50'>" + venta.get("metodo") + "</b></html>"));

        if (esApartado) {
            pnlInfo.add(new JLabel("<html><span style='color:#7f8c8d; font-family:Segoe UI'>Transacci\u00F3n:</span><br><b style='font-size:14px; color:#2c3e50'>Pago de Apartado #" + numApartado + "</b></html>"));
            pnlInfo.add(new JLabel("<html><span style='color:#7f8c8d; font-family:Segoe UI'>Referencia / Banco:</span><br><b style='font-size:14px; color:#2c3e50'>N/A</b></html>"));
        } else if (esGarantia) {
            pnlInfo.add(new JLabel("<html><span style='color:#7f8c8d; font-family:Segoe UI'>Transacci\u00F3n:</span><br><b style='font-size:14px; color:#e74c3c'>" + ref + "</b></html>"));
            pnlInfo.add(new JLabel("<html><span style='color:#7f8c8d; font-family:Segoe UI'>Referencia / Banco:</span><br><b style='font-size:14px; color:#2c3e50'>N/A</b></html>"));
        } else {
            pnlInfo.add(new JLabel("<html><span style='color:#7f8c8d; font-family:Segoe UI'>Transacci\u00F3n:</span><br><b style='font-size:14px; color:#2c3e50'>Venta Regular</b></html>"));
            String infoExtra = (ref != null && !ref.isEmpty() ? ref : "N/A") + (banco != null && !banco.isEmpty() ? " (" + banco + ")" : "");
            pnlInfo.add(new JLabel("<html><span style='color:#7f8c8d; font-family:Segoe UI'>Referencia / Banco:</span><br><b style='font-size:14px; color:#2c3e50'>" + infoExtra + "</b></html>"));
        }

        pnlInfo.add(new JLabel("<html><span style='color:#7f8c8d; font-family:Segoe UI'>Subtotal:</span><br><b style='font-size:14px; color:#2c3e50'>L " + String.format("%,.2f", (double) venta.get("subtotal")) + "</b></html>"));
        pnlInfo.add(new JLabel("<html><span style='color:#7f8c8d; font-family:Segoe UI'>Impuesto (15%):</span><br><b style='font-size:14px; color:#2c3e50'>L " + String.format("%,.2f", (double) venta.get("isv")) + "</b></html>"));

        // Tabla Productos
        String[] colP = { "Descripci\u00F3n / Serie", "Cantidad", "Precio Unitario", "Total Fila" };
        DefaultTableModel modP = new DefaultTableModel(null, colP) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        JTable tabP = new JTable(modP);
        tabP.setRowHeight(25);
        for (Object[] d : detalles) {
            String desc = d[2].toString(); // descripcion_venta
            String serie = d[1] != null ? d[1].toString() : null; // identificador_serie
            String fullDesc = desc + (serie != null && !serie.isEmpty() ? " [S/N: " + serie + "]" : "");

            modP.addRow(new Object[] {
                    fullDesc,
                    d[3], // cantidad
                    "L " + String.format("%,.2f", (double) d[4]), // precio_unitario
                    "L " + String.format("%,.2f", (double) d[5]) // subtotal
            });
        }

        JScrollPane scrollProd = new JScrollPane(tabP);
        scrollProd.setBorder(BorderFactory.createLineBorder(new Color(220, 222, 225)));
        scrollProd.getViewport().setBackground(Color.WHITE);

        pnlCentro.add(pnlInfo, BorderLayout.NORTH);
        pnlCentro.add(scrollProd, BorderLayout.CENTER);
        dialog.add(pnlCentro, BorderLayout.CENTER);

        // Pie
        JPanel pnlBot = new JPanel(new BorderLayout());
        pnlBot.setBackground(new Color(245, 247, 250)); // Fondo ligeramente gris
        pnlBot.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(220, 222, 225)),
            BorderFactory.createEmptyBorder(15, 20, 15, 20)
        ));

        JLabel lblTotal = new JLabel("TOTAL PAGADO: L " + String.format("%,.2f", (double) venta.get("total")));
        lblTotal.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTotal.setForeground(new Color(39, 174, 96));
        pnlBot.add(lblTotal, BorderLayout.WEST);

        JPanel pnlBotonesDer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        pnlBotonesDer.setOpaque(false);

        if (esApartado) {
            JButton btnAbonos = new JButton("Ver Abonos");
            btnAbonos.setFont(new Font("Segoe UI", Font.BOLD, 13));
            btnAbonos.setForeground(Color.WHITE);
            btnAbonos.setBackground(new Color(39, 174, 96)); // Verde
            btnAbonos.setFocusPainted(false);

            final String idAp = numApartado;
            btnAbonos.addActionListener(e -> {
                try {
                    int idApInt = Integer.parseInt(idAp);
                    setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                    btnAbonos.setEnabled(false);

                    SwingWorker<DialogoHistorialAbonosVenta, Void> worker = new SwingWorker<DialogoHistorialAbonosVenta, Void>() {
                        @Override
                        protected DialogoHistorialAbonosVenta doInBackground() throws Exception {
                            return new DialogoHistorialAbonosVenta((Frame) parent, idApInt);
                        }
                        @Override
                        protected void done() {
                            setCursor(Cursor.getDefaultCursor());
                            btnAbonos.setEnabled(true);
                            try {
                                get().setVisible(true);
                            } catch (Exception ex) {
                                ex.printStackTrace();
                                utilidades.Mensajes.showMessageDialog(parent, "Error al cargar el historial de abonos.");
                            }
                        }
                    };
                    worker.execute();
                } catch (Exception ex) {
                }
            });
            pnlBotonesDer.add(btnAbonos);
        }

        JButton btnCerrar = new JButton("Cerrar");
        btnCerrar.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnCerrar.setBackground(Color.WHITE);
        btnCerrar.setForeground(new Color(45, 45, 45));
        btnCerrar.setFocusPainted(false);
        btnCerrar.setPreferredSize(new Dimension(100, 35));
        btnCerrar.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
        btnCerrar.addActionListener(e -> dialog.dispose());
        pnlBotonesDer.add(btnCerrar);

        pnlBot.add(pnlBotonesDer, BorderLayout.EAST);

        dialog.add(pnlBot, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void reimprimirTicket() {
        int selectedRow = tablaVentas.getSelectedRow();
        if (selectedRow < 0) {
            utilidades.Mensajes.showMessageDialog(this, "Seleccione una venta de la lista.", "Aviso",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int idVenta = (int) tablaVentas.getValueAt(selectedRow, 0);
        Map<String, Object> venta = dao.obtenerReciboPorId(idVenta);
        if (venta == null || venta.isEmpty())
            return;

        List<Object[]> detalles = (List<Object[]>) venta.get("detalles");

        File dir = new File("reportes/ventas");
        if (!dir.exists()) dir.mkdirs();
        File archivoDestino = new File("reportes/ventas/Ticket_Venta_" + idVenta + ".pdf");

        try {
            boolean facturacionHabilitada = dao.empresaTieneFacturacionHabilitada(1);

            GeneradorTickets.generarTicketVentaPDF(
                    archivoDestino.getAbsolutePath(),
                    (String) venta.get("cliente"),
                    (String) venta.get("fecha"),
                    detalles,
                    (double) venta.get("subtotal"),
                    (double) venta.get("isv"),
                    (double) venta.get("total"),
                    facturacionHabilitada,
                    (String) venta.get("metodo"),
                    (String) venta.get("ref"),
                    (String) venta.get("banco"));

            utilidades.Mensajes.showMessageDialog(this, "Reimpresi\u00F3n generada exitosamente.", "\u00C9xito", JOptionPane.INFORMATION_MESSAGE);
            if (Desktop.isDesktopSupported()) {
                utilidades.GestorImpresion.procesarImpresion(archivoDestino, utilidades.GestorImpresion.TIPO_TICKET);
            }
        } catch (Exception ex) {
            utilidades.Mensajes.showMessageDialog(this, "Hubo un error al generar el PDF:\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private class IconoOjo implements Icon {
        @Override public int getIconWidth() { return 18; }
        @Override public int getIconHeight() { return 18; }
        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(c.getForeground());
            g2.setStroke(new BasicStroke(1.5f));
            g2.drawArc(x, y + 4, 18, 10, 0, 180);
            g2.drawArc(x, y + 4, 18, 10, 0, -180);
            g2.fillOval(x + 6, y + 6, 6, 6);
            g2.dispose();
        }
    }

    private class IconoImpresora implements Icon {
        @Override public int getIconWidth() { return 18; }
        @Override public int getIconHeight() { return 18; }
        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(c.getForeground());
            g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            // Papel superior
            g2.drawRect(x + 5, y + 1, 8, 4);
            // Cuerpo de la impresora
            g2.drawRoundRect(x + 2, y + 5, 14, 7, 3, 3);
            g2.drawLine(x + 4, y + 8, x + 6, y + 8); // Lucesitas
            // Bandeja/Papel inferior
            g2.fillRect(x + 4, y + 9, 10, 7);
            g2.setColor(c.getBackground());
            g2.drawLine(x + 6, y + 11, x + 12, y + 11);
            g2.drawLine(x + 6, y + 13, x + 12, y + 13);
            g2.dispose();
        }
    }
}

