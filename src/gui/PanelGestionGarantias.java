package gui;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;

public class PanelGestionGarantias extends JPanel {

    private JTable tablaGarantias;
    private DefaultTableModel modeloTabla;
    private JTextField txtBusqueda;
    private JCheckBox chkMostrarVencidas;
    private JCheckBox chkMostrarReclamadas;
    private TableRowSorter<DefaultTableModel> sorter;

    public PanelGestionGarantias() {
        iniciarDiseno();
    }

    private void iniciarDiseno() {
        this.removeAll();
        this.setLayout(new BorderLayout(20, 20));
        this.setBackground(utilidades.EfectosUI.COLOR_FONDO_PANEL); // Verde Vintage
        this.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        // --- 1. PANEL SUPERIOR ---
        JPanel panelSuperior = new JPanel(new BorderLayout(20, 0));
        panelSuperior.setOpaque(false);

        JLabel lblTitulo = new JLabel("Control de Garantías");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 28));
        lblTitulo.setForeground(utilidades.EfectosUI.COLOR_TEXTO_TITULO);

        JPanel panelCentro = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        panelCentro.setOpaque(false);
        txtBusqueda = new JTextField(25);
        txtBusqueda.putClientProperty("JTextField.placeholderText", "Buscar por Cliente, Producto o Serie...");
        txtBusqueda.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtBusqueda.setPreferredSize(new Dimension(300, 35));
        panelCentro.add(txtBusqueda);

        // Etiqueta de la empresa
        JLabel lblEmpresa = new JLabel("INVERSIONES OLVAN");
        lblEmpresa.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblEmpresa.setForeground(utilidades.EfectosUI.COLOR_VERDE_PRIMARIO);

        JPanel panelFiltros = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        panelFiltros.setOpaque(false);
        chkMostrarVencidas = new JCheckBox("Mostrar Vencidas");
        chkMostrarVencidas.setOpaque(false);
        chkMostrarVencidas.setForeground(utilidades.EfectosUI.COLOR_TEXTO_OSCURO);
        chkMostrarVencidas.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        chkMostrarReclamadas = new JCheckBox("Mostrar Reclamadas");
        chkMostrarReclamadas.setOpaque(false);
        chkMostrarReclamadas.setForeground(utilidades.EfectosUI.COLOR_TEXTO_OSCURO);
        chkMostrarReclamadas.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        panelFiltros.add(chkMostrarVencidas);
        panelFiltros.add(chkMostrarReclamadas);

        JPanel panelTopRight = new JPanel(new BorderLayout());
        panelTopRight.setOpaque(false);
        panelTopRight.add(lblEmpresa, BorderLayout.NORTH);
        panelTopRight.add(panelFiltros, BorderLayout.SOUTH);

        panelSuperior.add(lblTitulo, BorderLayout.WEST);
        panelSuperior.add(panelCentro, BorderLayout.CENTER);
        panelSuperior.add(panelTopRight, BorderLayout.EAST);

        this.add(panelSuperior, BorderLayout.NORTH);

        // --- 2. CONFIGURACIÓN DE LA TABLA ---
        String[] columnas = {"Venta #", "Cliente", "Producto", "Serie / IMEI", "Fecha Compra", "Vencimiento", "Estado", "ID Venta Oculto", "ID Detalle Oculto"};
        modeloTabla = new DefaultTableModel(null, columnas) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };

        tablaGarantias = new JTable(modeloTabla);
        tablaGarantias.setShowGrid(false);
        tablaGarantias.setIntercellSpacing(new Dimension(0, 0));
        tablaGarantias.setRowHeight(45);
        tablaGarantias.setBackground(new Color(255, 255, 255)); // Blanco puro
        tablaGarantias.setForeground(new Color(30, 41, 59));
        tablaGarantias.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tablaGarantias.setSelectionBackground(new Color(209, 250, 229)); // Menta luminosa 
        tablaGarantias.setSelectionForeground(new Color(6, 95, 70));

        tablaGarantias.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        tablaGarantias.getTableHeader().setBackground(new Color(236, 253, 245)); // Verde Menta fresca
        tablaGarantias.getTableHeader().setForeground(new Color(6, 95, 70));     // Verde Esmeralda Oscuro
        tablaGarantias.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(16, 185, 129)));
        tablaGarantias.getTableHeader().setPreferredSize(new Dimension(0, 42));

        // Ocultar IDs
        ocultarColumna(7); // ID Venta
        ocultarColumna(8); // ID Detalle

        // Renderizador para la columna Estado (VIGENTE / VENCIDA)
        tablaGarantias.getColumnModel().getColumn(6).setCellRenderer(new EstadoGarantiaRenderer());

        // --- LÓGICA DE MENÚ CONTEXTUAL ---
        tablaGarantias.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                int fila = tablaGarantias.rowAtPoint(e.getPoint());
                if (fila >= 0) {
                    tablaGarantias.setRowSelectionInterval(fila, fila);
                    if ((SwingUtilities.isLeftMouseButton(e) && e.getClickCount() == 2) || SwingUtilities.isRightMouseButton(e)) {
                        mostrarMenuOpciones(e.getComponent(), e.getX(), e.getY(), fila);
                    }
                }
            }
        });

        // --- BUSCADOR EN TIEMPO REAL ---
        sorter = new TableRowSorter<>(modeloTabla);
        tablaGarantias.setRowSorter(sorter);
        
        java.awt.event.ActionListener filtroListener = e -> filtrar();
        chkMostrarVencidas.addActionListener(filtroListener);
        chkMostrarReclamadas.addActionListener(filtroListener);
        
        txtBusqueda.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { filtrar(); }
            @Override public void removeUpdate(DocumentEvent e) { filtrar(); }
            @Override public void changedUpdate(DocumentEvent e) { filtrar(); }
        });
        JScrollPane scrollPane = new JScrollPane(tablaGarantias);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(220, 222, 225), 1, true));
        scrollPane.getViewport().setBackground(new Color(255, 255, 255)); // Fondo blanco

        this.add(scrollPane, BorderLayout.CENTER);

        cargarDatosDesdeBD();
    }

    private void filtrar() {
        String texto = txtBusqueda.getText().trim().toLowerCase();
        boolean mostrarVencidas = chkMostrarVencidas.isSelected();
        boolean mostrarReclamadas = chkMostrarReclamadas.isSelected();

        sorter.setRowFilter(new RowFilter<DefaultTableModel, Integer>() {
            @Override
            public boolean include(Entry<? extends DefaultTableModel, ? extends Integer> entry) {
                String estado = entry.getStringValue(6);
                if (!mostrarVencidas && estado.equals("VENCIDA")) return false;
                if (!mostrarReclamadas && estado.equals("RECLAMADA")) return false;

                if (texto.isEmpty()) return true;

                String cliente = entry.getStringValue(1).toLowerCase();
                String producto = entry.getStringValue(2).toLowerCase();
                String serie = entry.getStringValue(3).toLowerCase();

                return cliente.contains(texto) || producto.contains(texto) || serie.contains(texto);
            }
        });
    }

    private void ocultarColumna(int index) {
        tablaGarantias.getColumnModel().getColumn(index).setMinWidth(0);
        tablaGarantias.getColumnModel().getColumn(index).setMaxWidth(0);
        tablaGarantias.getColumnModel().getColumn(index).setWidth(0);
    }

    // =========================================================
    // MENÚ CONTEXTUAL Y ACCIONES
    // =========================================================
    
    public void cargarDatosDesdeBD() {
        modeloTabla.setRowCount(0);
        dao.GarantiaDAO dao = new dao.GarantiaDAO();
        for (Object[] fila : dao.listarGarantias()) {
            modeloTabla.addRow(fila);
        }
        filtrar(); // Aplicar filtros por defecto (ocultar vencidas/reclamadas)
    }
    private void mostrarMenuOpciones(Component componente, int x, int y, int filaVista) {
        JPopupMenu menu = new JPopupMenu();
        menu.setBackground(new Color(255, 255, 255)); // Blanco puro
        menu.setBorder(BorderFactory.createLineBorder(new Color(220, 222, 225), 1));

        int filaModelo = tablaGarantias.convertRowIndexToModel(filaVista);
        String estado = modeloTabla.getValueAt(filaModelo, 6).toString();

        JMenuItem itemRecibo = crearMenuItem("Ver Recibo Original", new Color(13, 110, 253), new IconoRecibo());
        JMenuItem itemCertificado = crearMenuItem("Imprimir Certificado", new Color(39, 174, 96), new IconoCertificado()); // Verde Menta
        JMenuItem itemReclamar = crearMenuItem("Aplicar / Reclamar", new Color(227, 0, 15), new IconoHerramienta()); // Rojo Logo

        itemRecibo.addActionListener(e -> verReciboOriginal(filaModelo));
        itemCertificado.addActionListener(e -> imprimirCertificado(filaModelo));
        
        // Solo habilitamos el botón de reclamar si la garantía está VIGENTE y tiene permiso
        modelo.Usuario uAct = utilidades.SesionGlobal.getUsuarioActual();
        if (estado.equals("VIGENTE")) {
            itemReclamar.addActionListener(e -> reclamarGarantia(filaModelo));
            if (uAct != null && !uAct.tienePermiso("EDITAR_GARANTIAS")) {
                itemReclamar.setEnabled(false);
                itemReclamar.setToolTipText("No tienes permiso para registrar o editar garantías.");
            }
            menu.add(itemReclamar);
            menu.addSeparator();
        }

        menu.add(itemRecibo);
        menu.add(itemCertificado);
        
        menu.show(componente, x, y);
    }

    private void verReciboOriginal(int filaModelo) {
        int idVenta = Integer.parseInt(modeloTabla.getValueAt(filaModelo, 7).toString());
        try {
            // Llamamos directamente al GeneradorTickets
            java.io.File archivoPDF = utilidades.GeneradorTickets.generarFactura(idVenta);
            if (archivoPDF != null && archivoPDF.exists()) {
                longitudAbrirArchivo(archivoPDF);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al generar recibo: " + e.getMessage());
        }
    }

    private void imprimirCertificado(int filaModelo) {
        int idDetalleVenta = Integer.parseInt(modeloTabla.getValueAt(filaModelo, 8).toString());
        try {
            // Generamos el certificado individual
            java.io.File archivoPDF = utilidades.GeneradorTickets.generarCertificadoGarantia(idDetalleVenta);
            if (archivoPDF != null && archivoPDF.exists()) {
                longitudAbrirArchivo(archivoPDF);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al generar certificado: " + e.getMessage());
        }
    }

    // Método que abre el PDF en el visor de Windows
    private void longitudAbrirArchivo(java.io.File archivo) {
        try {
            if (Desktop.isDesktopSupported()) {
                utilidades.GestorImpresion.procesarImpresion(archivo, utilidades.GestorImpresion.TIPO_TICKET);
            }
        } catch (Exception ex) {
            System.err.println("No se pudo abrir el PDF: " + ex.getMessage());
        }
    }

    private void reclamarGarantia(int filaModelo) {
        String cliente = modeloTabla.getValueAt(filaModelo, 1).toString();
        String producto = modeloTabla.getValueAt(filaModelo, 2).toString();
        String serie = modeloTabla.getValueAt(filaModelo, 3).toString();
        String fechaCompra = modeloTabla.getValueAt(filaModelo, 4).toString();
        int idDetalle = Integer.parseInt(modeloTabla.getValueAt(filaModelo, 8).toString()); 

        Window parentWindow = SwingUtilities.getWindowAncestor(this);
        DialogoReclamarGarantia dialogo = new DialogoReclamarGarantia(parentWindow, cliente, producto, serie, fechaCompra, idDetalle);
        dialogo.setVisible(true);

        if (dialogo.isExito()) {
            JOptionPane.showMessageDialog(this, "Garantía reclamada exitosamente.", "Orion Systems", JOptionPane.INFORMATION_MESSAGE);
            cargarDatosDesdeBD(); // Recargamos la tabla para ver el cambio
        }
    }

    private JMenuItem crearMenuItem(String texto, Color colorHover, Icon icono) {
        JMenuItem item = new JMenuItem(texto);
        item.setIcon(icono);
        item.setIconTextGap(12);
        item.setFont(new Font("Segoe UI", Font.BOLD, 14));
        item.setForeground(new Color(45, 45, 45)); // Gris Oscuro
        item.setBackground(new Color(255, 255, 255)); // Blanco Puro
        item.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        item.setCursor(new Cursor(Cursor.HAND_CURSOR));
        item.setOpaque(true);
        item.addChangeListener(e -> {
            if (item.isArmed()) {
                item.setBackground(colorHover);
                item.setForeground(Color.WHITE); // Texto blanco al hacer hover
            } else {
                item.setBackground(new Color(255, 255, 255));
                item.setForeground(new Color(45, 45, 45)); // Texto oscuro normal
            }
        });
        return item;
    }

    // =========================================================
    // RENDERIZADOR DE ESTADO Y COLORES
    // =========================================================
    private class EstadoGarantiaRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            label.setFont(new Font("Segoe UI", Font.BOLD, 13));
            label.setHorizontalAlignment(SwingConstants.CENTER);
            
            if (value != null) {
                String estado = value.toString();
                if (estado.equals("VIGENTE")) {
                    label.setForeground(new Color(39, 174, 96)); // Verde Menta
                } else if (estado.equals("VENCIDA")) {
                    label.setForeground(new Color(227, 0, 15)); // Rojo Logo
                } else {
                    label.setForeground(new Color(140, 145, 150)); // Gris suave para Reclamada / Anulada
                }
            }
            return label;
        }
    }

    // =========================================================
    // ÍCONOS VECTORIALES (JAVA 2D)
    // =========================================================
    private class IconoRecibo implements Icon {
        @Override public int getIconWidth() { return 20; }
        @Override public int getIconHeight() { return 20; }
        @Override public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(c.getForeground()); // Dinámico
            g2.setStroke(new BasicStroke(1.5f));
            g2.drawRect(x + 4, y + 2, 12, 16);
            g2.drawLine(x + 7, y + 6, x + 13, y + 6);
            g2.drawLine(x + 7, y + 10, x + 13, y + 10);
            g2.dispose();
        }
    }

    private class IconoCertificado implements Icon {
        @Override public int getIconWidth() { return 20; }
        @Override public int getIconHeight() { return 20; }
        @Override public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(c.getForeground()); // Dinámico
            g2.setStroke(new BasicStroke(1.5f));
            g2.drawRoundRect(x + 2, y + 4, 16, 12, 2, 2);
            g2.drawOval(x + 12, y + 10, 4, 4); // Sello
            g2.drawLine(x + 5, y + 8, x + 10, y + 8);
            g2.dispose();
        }
    }

    private class IconoHerramienta implements Icon {
        @Override public int getIconWidth() { return 20; }
        @Override public int getIconHeight() { return 20; }
        @Override public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(c.getForeground()); // Dinámico
            g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.drawLine(x + 4, y + 16, x + 12, y + 8); // Mango
            g2.drawOval(x + 12, y + 4, 4, 4); // Cabeza llave
            g2.dispose();
        }
    }
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 300, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
