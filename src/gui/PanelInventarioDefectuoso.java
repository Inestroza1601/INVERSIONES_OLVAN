package gui;

import dao.InventarioDefectuosoDAO;
import utilidades.SesionGlobal;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Map;

public class PanelInventarioDefectuoso extends JPanel {

    private JTable tablaDefectuosos;
    private DefaultTableModel modeloTabla;
    private TableRowSorter<DefaultTableModel> sorter;
    private InventarioDefectuosoDAO dao;

    public PanelInventarioDefectuoso() {
        dao = new InventarioDefectuosoDAO();
        iniciarDiseno();
        cargarDatos();
    }

    private void iniciarDiseno() {
        this.setLayout(new BorderLayout());
        this.setBackground(utilidades.EfectosUI.COLOR_FONDO_PANEL);

        // Cabecera
        JPanel panelCabecera = new JPanel(new BorderLayout());
        panelCabecera.setBackground(utilidades.EfectosUI.COLOR_FONDO_PANEL);
        panelCabecera.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));

        JLabel lblTitulo = new JLabel("Control de Inventario Defectuoso");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitulo.setForeground(utilidades.EfectosUI.COLOR_TEXTO_TITULO);
        panelCabecera.add(lblTitulo, BorderLayout.WEST);

        // Barra de búsqueda
        JPanel pnlBusqueda = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        pnlBusqueda.setOpaque(false);
        JLabel lblBuscar = new JLabel("Buscar:");
        lblBuscar.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblBuscar.setForeground(utilidades.EfectosUI.COLOR_TEXTO_SUBTITULO);
        JTextField txtBusqueda = new JTextField(20);
        txtBusqueda.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtBusqueda.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        pnlBusqueda.add(lblBuscar);
        pnlBusqueda.add(txtBusqueda);
        panelCabecera.add(pnlBusqueda, BorderLayout.EAST);

        // Tabla
        String[] columnas = { "ID Producto", "Imagen", "Código de Barras", "Nombre del Producto", "Cliente", "Estado",
                "Cantidad Defectuosa", "Identidad" };
        modeloTabla = new DefaultTableModel(null, columnas) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 1)
                    return ImageIcon.class;
                return super.getColumnClass(columnIndex);
            }
        };

        tablaDefectuosos = new JTable(modeloTabla);
        tablaDefectuosos.setRowHeight(60);
        tablaDefectuosos.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tablaDefectuosos.setSelectionBackground(new Color(209, 250, 229));
        tablaDefectuosos.setSelectionForeground(new Color(6, 95, 70));
        tablaDefectuosos.setShowVerticalLines(false);
        tablaDefectuosos.setGridColor(new Color(241, 245, 249));

        sorter = new TableRowSorter<>(modeloTabla);
        tablaDefectuosos.setRowSorter(sorter);

        txtBusqueda.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { filtrar(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { filtrar(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filtrar(); }
            private void filtrar() {
                String text = txtBusqueda.getText();
                if (text.trim().length() == 0) {
                    sorter.setRowFilter(null);
                } else {
                    sorter.setRowFilter(RowFilter.regexFilter("(?i)" + text));
                }
            }
        });

        JTableHeader header = tablaDefectuosos.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 14));
        header.setBackground(new Color(236, 253, 245)); // Verde Menta fresca
        header.setForeground(new Color(6, 95, 70));     // Verde Esmeralda Oscuro
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(16, 185, 129)));
        header.setPreferredSize(new Dimension(0, 42));
        ((DefaultTableCellRenderer) header.getDefaultRenderer()).setHorizontalAlignment(JLabel.LEFT);

        // Renderizado del Estado
        tablaDefectuosos.getColumnModel().getColumn(5).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                String estado = (value != null) ? value.toString() : "";
                if (estado.contains("En Bodega")) {
                    c.setForeground(new Color(200, 50, 50));
                } else if (estado.contains("Enviado a Proveedor")) {
                    c.setForeground(new Color(50, 100, 200));
                } else if (estado.contains("Recibido")) {
                    c.setForeground(new Color(39, 174, 96));
                } else if (estado.equals("Desechado")) {
                    c.setForeground(new Color(100, 100, 100));
                } else {
                    c.setForeground(Color.BLACK);
                }
                return c;
            }
        });

        // Ocultar ID Producto e Identidad visualmente
        tablaDefectuosos.getColumnModel().getColumn(0).setMinWidth(0);
        tablaDefectuosos.getColumnModel().getColumn(0).setMaxWidth(0);
        tablaDefectuosos.getColumnModel().getColumn(0).setWidth(0);

        tablaDefectuosos.getColumnModel().getColumn(7).setMinWidth(0);
        tablaDefectuosos.getColumnModel().getColumn(7).setMaxWidth(0);
        tablaDefectuosos.getColumnModel().getColumn(7).setWidth(0);

        // Ajustar ancho de columnas
        tablaDefectuosos.getColumnModel().getColumn(1).setPreferredWidth(80);
        tablaDefectuosos.getColumnModel().getColumn(2).setPreferredWidth(120);
        tablaDefectuosos.getColumnModel().getColumn(3).setPreferredWidth(200);
        tablaDefectuosos.getColumnModel().getColumn(4).setPreferredWidth(150);
        tablaDefectuosos.getColumnModel().getColumn(5).setPreferredWidth(150);

        JScrollPane scrollPane = new JScrollPane(tablaDefectuosos);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(Color.WHITE);

        JPanel panelCentral = new JPanel(new BorderLayout());
        panelCentral.setBorder(BorderFactory.createEmptyBorder(0, 20, 20, 20));
        panelCentral.setBackground(new Color(245, 247, 250));
        panelCentral.add(scrollPane, BorderLayout.CENTER);

        this.add(panelCabecera, BorderLayout.NORTH);
        this.add(panelCentral, BorderLayout.CENTER);

        configurarMenuContextual();
    }

    private ImageIcon obtenerImagenDesdeBase64(String base64, int ancho, int alto) {
        if (base64 == null || base64.isEmpty())
            return null;
        try {
            if (base64.contains(",")) {
                base64 = base64.split(",")[1];
            }
            byte[] bytes = java.util.Base64.getDecoder().decode(base64);
            java.io.ByteArrayInputStream bais = new java.io.ByteArrayInputStream(bytes);
            Image img = javax.imageio.ImageIO.read(bais);
            if (img != null) {
                return new ImageIcon(img.getScaledInstance(ancho, alto, Image.SCALE_SMOOTH));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private void cargarDatos() {
        modeloTabla.setRowCount(0);
        List<Map<String, Object>> lista = dao.obtenerInventarioDefectuosoAgrupado();
        for (Map<String, Object> fila : lista) {
            ImageIcon iconPreview = obtenerImagenDesdeBase64((String) fila.get("foto"), 50, 50);

            modeloTabla.addRow(new Object[] {
                    fila.get("id_producto"),
                    iconPreview,
                    fila.get("codigo_barras"),
                    fila.get("nombre_producto"),
                    fila.get("cliente"),
                    fila.get("estado_defecto"),
                    fila.get("cantidad"),
                    fila.get("identidad")
            });
        }
    }

    private void configurarMenuContextual() {
        JPopupMenu menu = new JPopupMenu();
        menu.setBackground(new Color(255, 255, 255));
        menu.setBorder(BorderFactory.createLineBorder(new Color(220, 222, 225), 1));

        JMenuItem itemDetalles = crearMenuItem("Ver Detalles de Daños", new Color(13, 110, 253), new IconoOjo());
        JMenuItem itemEnviar = crearMenuItem("Marcar como 'Enviado a Proveedor'", new Color(243, 156, 18),
                new IconoEnvio());
        JMenuItem itemReingresar = crearMenuItem("Recibir de Proveedor (Reingresar a Inv. Normal)",
                new Color(39, 174, 96), new IconoRecibir());
        JMenuItem itemEntregarCliente = crearMenuItem("Entregar producto reparado al Cliente", new Color(39, 174, 96),
                new IconoRecibir());
        JMenuItem itemDesechar = crearMenuItem("Desechar Producto (Eliminar de Inv. Defectuoso)", new Color(227, 0, 15),
                new IconoBasura());

        itemDetalles.addActionListener(e -> accionVerDetalles());
        itemEnviar.addActionListener(e -> accionCambiarEstado("Enviado a Proveedor", "ENVIO MERMA A PROVEEDOR"));
        itemDesechar.addActionListener(e -> accionCambiarEstado("Desechado", "DESECHO DE MERMA"));
        itemReingresar.addActionListener(e -> accionReingresar());
        itemEntregarCliente.addActionListener(e -> accionEntregarCliente());

        tablaDefectuosos.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    int r = tablaDefectuosos.rowAtPoint(e.getPoint());
                    if (r >= 0 && r < tablaDefectuosos.getRowCount()) {
                        tablaDefectuosos.setRowSelectionInterval(r, r);
                    } else {
                        tablaDefectuosos.clearSelection();
                    }

                    int rowindex = tablaDefectuosos.getSelectedRow();
                    if (rowindex < 0)
                        return;
                        
                    // Traducir indice en caso de filtro
                    rowindex = tablaDefectuosos.convertRowIndexToModel(rowindex);

                    String estado = modeloTabla.getValueAt(rowindex, 5).toString();

                    menu.removeAll();
                    menu.add(itemDetalles);
                    menu.addSeparator();

                    if (estado.equals("En Bodega") || estado.equals("En Bodega (Rep. Cliente)")) {
                        menu.add(itemEnviar);
                    }

                    if (estado.equals("Enviado a Proveedor")) {
                        menu.add(itemReingresar);
                    }
                    if (estado.equals("Enviado a Proveedor (Rep. Cliente)")) {
                        JMenuItem itemRecibirRep = crearMenuItem("Recibir de Proveedor", new Color(243, 156, 18),
                                new IconoEnvio());
                        itemRecibirRep.addActionListener(
                                ev -> accionCambiarEstado("Recibido de Proveedor (Rep. Cliente)", ""));
                        menu.add(itemRecibirRep);
                    }

                    if (estado.equals("Recibido de Proveedor (Rep. Cliente)")) {
                        menu.add(itemEntregarCliente);
                    }

                    if (!estado.equals("Desechado") && !estado.contains("Rep. Cliente")) {
                        menu.addSeparator();
                        menu.add(itemDesechar);
                    }

                    menu.show(e.getComponent(), e.getX(), e.getY());
                }
            }
        });
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
                item.setForeground(Color.WHITE);
            } else {
                item.setBackground(new Color(255, 255, 255));
                item.setForeground(new Color(45, 45, 45));
            }
        });
        return item;
    }

    private void accionVerDetalles() {
        int filaView = tablaDefectuosos.getSelectedRow();
        if (filaView == -1)
            return;
            
        int fila = tablaDefectuosos.convertRowIndexToModel(filaView);

        int idProducto = (int) modeloTabla.getValueAt(fila, 0);
        String producto = modeloTabla.getValueAt(fila, 3).toString();
        String estado = modeloTabla.getValueAt(fila, 5).toString();

        List<Map<String, Object>> detalles = dao.obtenerDetallesPorProductoYEstado(idProducto, estado);

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Detalles de Producto Defectuoso",
                true);
        dialog.setLayout(new BorderLayout());
        dialog.getContentPane().setBackground(Color.WHITE);

        JPanel pnlHead = new JPanel(new FlowLayout(FlowLayout.LEFT));
        pnlHead.setBackground(new Color(245, 247, 250));
        pnlHead.setBorder(BorderFactory.createEmptyBorder(15, 15, 10, 15));
        JLabel lblProd = new JLabel(
                "<html><b style='color:#333; font-size:14px'>Producto:</b> <span style='font-size:13px'>" + producto
                        + "</span><br><b style='color:#333; font-size:14px'>Estado Actual:</b> <span style='font-size:13px'>"
                        + estado + "</span></html>");
        pnlHead.add(lblProd);

        dialog.add(pnlHead, BorderLayout.NORTH);

        if (detalles.size() == 1) {
            Map<String, Object> d = detalles.get(0);
            String b64 = (String) d.get("foto");
            ImageIcon icon = obtenerImagenDesdeBase64(b64, 180, 180);

            JPanel pnlCard = new JPanel(new BorderLayout(25, 25));
            pnlCard.setBackground(Color.WHITE);
            pnlCard.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));

            JLabel lblImg = new JLabel(icon);
            lblImg.setBorder(BorderFactory.createLineBorder(new Color(220, 222, 225), 1));
            lblImg.setCursor(new Cursor(Cursor.HAND_CURSOR));
            lblImg.setToolTipText("Clic para ver imagen en grande");
            lblImg.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseClicked(java.awt.event.MouseEvent e) {
                    if (b64 != null && !b64.isEmpty()) {
                        new DialogoVisorImagen(dialog, "Visor de Fotografía", b64).setVisible(true);
                    }
                }
            });

            JPanel pnlInfo = new JPanel();
            pnlInfo.setLayout(new BoxLayout(pnlInfo, BoxLayout.Y_AXIS));
            pnlInfo.setBackground(Color.WHITE);

            String cliente = (String) d.get("cliente");
            if (estado.contains("Rep. Cliente") && cliente != null && !cliente.isEmpty()) {
                pnlInfo.add(crearEtiquetaDetalle("Cliente Propietario", cliente));
                pnlInfo.add(Box.createVerticalStrut(15));
            } else {
                pnlInfo.add(crearEtiquetaDetalle("Propietario", "Inversiones Olvan (Empresa)"));
                pnlInfo.add(Box.createVerticalStrut(15));
            }

            pnlInfo.add(crearEtiquetaDetalle("Motivo del Daño", d.get("motivo").toString()));
            pnlInfo.add(Box.createVerticalStrut(15));
            pnlInfo.add(crearEtiquetaDetalle("Resolución de Garantía", d.get("resolucion").toString()));
            pnlInfo.add(Box.createVerticalStrut(15));

            StringBuilder timeline = new StringBuilder();
            timeline.append("• ").append(d.get("fecha")).append(" (Ingreso)<br>");
            if (d.get("fecha_envio") != null) {
                timeline.append("• ").append(d.get("fecha_envio")).append(" (Enviado al Proveedor)<br>");
            }
            if (d.get("fecha_recibido") != null) {
                timeline.append("• ").append(d.get("fecha_recibido")).append(" (Recibido del Proveedor)<br>");
            }
            pnlInfo.add(crearEtiquetaDetalle("Historial de Movimientos", timeline.toString()));

            pnlCard.add(lblImg, BorderLayout.WEST);
            pnlCard.add(pnlInfo, BorderLayout.CENTER);

            JScrollPane scrollCard = new JScrollPane(pnlCard);
            scrollCard.setBorder(BorderFactory.createEmptyBorder());
            scrollCard.getVerticalScrollBar().setUnitIncrement(16);

            dialog.add(scrollCard, BorderLayout.CENTER);
            dialog.add(scrollCard, BorderLayout.CENTER);
        } else {
            String[] colD = { "Imagen", "Fecha de Ingreso", "Motivo del Daño", "Resolución Garantía", "Base64" };
            DefaultTableModel modD = new DefaultTableModel(null, colD) {
                @Override
                public boolean isCellEditable(int r, int c) {
                    return false;
                }

                @Override
                public Class<?> getColumnClass(int columnIndex) {
                    if (columnIndex == 0)
                        return ImageIcon.class;
                    return super.getColumnClass(columnIndex);
                }
            };
            for (Map<String, Object> d : detalles) {
                String b64 = (String) d.get("foto");
                ImageIcon icon = obtenerImagenDesdeBase64(b64, 60, 60);
                modD.addRow(new Object[] {
                        icon,
                        d.get("fecha"),
                        d.get("motivo"),
                        d.get("resolucion"),
                        b64 // hidden column for the base64 string
                });
            }

            JTable tabD = new JTable(modD);
            tabD.setRowHeight(70);
            tabD.setShowVerticalLines(false);
            tabD.setGridColor(new Color(235, 235, 235));

            // Hide the base64 column
            tabD.getColumnModel().getColumn(4).setMinWidth(0);
            tabD.getColumnModel().getColumn(4).setMaxWidth(0);
            tabD.getColumnModel().getColumn(4).setWidth(0);

            tabD.getColumnModel().getColumn(0).setPreferredWidth(70);
            tabD.getColumnModel().getColumn(1).setPreferredWidth(120);
            tabD.getColumnModel().getColumn(2).setPreferredWidth(250);
            tabD.getColumnModel().getColumn(3).setPreferredWidth(150);

            tabD.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseClicked(java.awt.event.MouseEvent e) {
                    int row = tabD.rowAtPoint(e.getPoint());
                    int col = tabD.columnAtPoint(e.getPoint());
                    if (row >= 0 && col == 0) {
                        String b64 = (String) tabD.getModel().getValueAt(row, 4);
                        if (b64 != null && !b64.isEmpty()) {
                            new DialogoVisorImagen(dialog, "Visor de Fotografía", b64).setVisible(true);
                        }
                    }
                }
            });

            JScrollPane scrollTab = new JScrollPane(tabD);
            scrollTab.setPreferredSize(new Dimension(650, 300));
            dialog.add(scrollTab, BorderLayout.CENTER);
        }

        JPanel pnlBot = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        pnlBot.setBackground(Color.WHITE);
        JButton btnOk = new JButton("Cerrar");
        btnOk.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnOk.setBackground(new Color(230, 230, 230));
        btnOk.addActionListener(e -> dialog.dispose());
        pnlBot.add(btnOk);
        dialog.add(pnlBot, BorderLayout.SOUTH);

        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private JLabel crearEtiquetaDetalle(String titulo, String valor) {
        JLabel lbl = new JLabel("<html><div style='width:320px;'><b style='color:#7f8c8d; font-size:12px'>"
                + titulo.toUpperCase() + "</b><br><span style='font-size:14px; color:#2c3e50; margin-top:4px'>" + valor
                + "</span></div></html>");
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        return lbl;
    }

    private void accionCambiarEstado(String nuevoEstado, String kardexRef) {
        int filaView = tablaDefectuosos.getSelectedRow();
        if (filaView == -1)
            return;
            
        int fila = tablaDefectuosos.convertRowIndexToModel(filaView);

        String estadoActual = modeloTabla.getValueAt(fila, 5).toString();

        if (estadoActual.contains("Rep. Cliente")) {
            if (nuevoEstado.equals("Enviado a Proveedor")) {
                nuevoEstado = "Enviado a Proveedor (Rep. Cliente)";
            }
        }

        if (estadoActual.equals(nuevoEstado)) {
            JOptionPane.showMessageDialog(this, "El producto ya está en ese estado.", "Información",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int idProducto = (int) modeloTabla.getValueAt(fila, 0);
        String producto = modeloTabla.getValueAt(fila, 3).toString();

        int confirm = JOptionPane.showConfirmDialog(this,
                "¿Estás seguro de marcar las mermas de '" + producto + "' como '" + nuevoEstado + "'?",
                "Confirmar Acción", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            int idUsuario = SesionGlobal.getUsuarioActual() != null ? SesionGlobal.getUsuarioActual().getIdUsuario()
                    : 1;
            if (dao.cambiarEstadoMermas(idProducto, estadoActual, nuevoEstado, idUsuario, kardexRef)) {
                JOptionPane.showMessageDialog(this, "Estado actualizado con éxito.", "Éxito",
                        JOptionPane.INFORMATION_MESSAGE);
                cargarDatos();
            } else {
                JOptionPane.showMessageDialog(this, "Error al actualizar el estado.", "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void accionReingresar() {
        int filaView = tablaDefectuosos.getSelectedRow();
        if (filaView == -1)
            return;
            
        int fila = tablaDefectuosos.convertRowIndexToModel(filaView);

        int idProducto = (int) modeloTabla.getValueAt(fila, 0);
        String producto = modeloTabla.getValueAt(fila, 3).toString();
        String estadoActual = modeloTabla.getValueAt(fila, 5).toString();
        int cant = (int) modeloTabla.getValueAt(fila, 6);

        int confirm = JOptionPane.showConfirmDialog(this,
                "Esta acción sacará " + cant + " unidad(es) de '" + producto
                        + "' de Mermas y las agregará al Inventario Normal para la venta.\n\n¿Deseas continuar?",
                "Reingresar al Inventario", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            int idUsuario = SesionGlobal.getUsuarioActual() != null ? SesionGlobal.getUsuarioActual().getIdUsuario()
                    : 1;
            if (dao.reingresarInventario(idProducto, estadoActual, idUsuario)) {
                JOptionPane.showMessageDialog(this, "Productos reingresados exitosamente.", "Éxito",
                        JOptionPane.INFORMATION_MESSAGE);
                cargarDatos();
            } else {
                JOptionPane.showMessageDialog(this, "Error al reingresar los productos.", "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void accionEntregarCliente() {
        int filaView = tablaDefectuosos.getSelectedRow();
        if (filaView == -1)
            return;
            
        int fila = tablaDefectuosos.convertRowIndexToModel(filaView);

        int idProducto = (int) modeloTabla.getValueAt(fila, 0);
        String producto = modeloTabla.getValueAt(fila, 3).toString();
        String cliente = modeloTabla.getValueAt(fila, 4).toString();
        String estadoActual = modeloTabla.getValueAt(fila, 5).toString();

        Window owner = SwingUtilities.getWindowAncestor(this);
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        
        SwingWorker<DialogoEntregarDefectuoso, Void> worker = new SwingWorker<DialogoEntregarDefectuoso, Void>() {
            @Override
            protected DialogoEntregarDefectuoso doInBackground() throws Exception {
                return new DialogoEntregarDefectuoso(owner, idProducto, producto, estadoActual, cliente);
            }
            @Override
            protected void done() {
                setCursor(Cursor.getDefaultCursor());
                try {
                    DialogoEntregarDefectuoso dialog = get();
                    dialog.setVisible(true);
                    if (dialog.isExito()) {
                        JOptionPane.showMessageDialog(PanelInventarioDefectuoso.this, "Garantía finalizada. Producto entregado.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                        cargarDatos();
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(PanelInventarioDefectuoso.this, "Error al cargar la entrega: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    // --- ICONOS CREADOS A MANO ---
    private class IconoOjo implements Icon {
        @Override
        public int getIconWidth() {
            return 20;
        }

        @Override
        public int getIconHeight() {
            return 20;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(c.getForeground());
            g2.setStroke(new BasicStroke(1.5f));
            g2.drawArc(x + 2, y + 6, 16, 8, 0, 180);
            g2.drawArc(x + 2, y + 6, 16, 8, 180, 180);
            g2.drawOval(x + 7, y + 7, 6, 6);
            g2.fillOval(x + 9, y + 9, 2, 2);
            g2.dispose();
        }
    }

    private class IconoEnvio implements Icon {
        @Override
        public int getIconWidth() {
            return 20;
        }

        @Override
        public int getIconHeight() {
            return 20;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(c.getForeground());
            g2.setStroke(new BasicStroke(1.5f));
            g2.drawLine(x + 2, y + 10, x + 14, y + 10); // Flecha
            g2.drawLine(x + 10, y + 6, x + 15, y + 10);
            g2.drawLine(x + 10, y + 14, x + 15, y + 10);
            g2.drawRect(x + 14, y + 5, 4, 10); // Caja
            g2.dispose();
        }
    }

    private class IconoRecibir implements Icon {
        @Override
        public int getIconWidth() {
            return 20;
        }

        @Override
        public int getIconHeight() {
            return 20;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(c.getForeground());
            g2.setStroke(new BasicStroke(1.5f));
            g2.drawLine(x + 16, y + 10, x + 4, y + 10); // Flecha
            g2.drawLine(x + 8, y + 6, x + 3, y + 10);
            g2.drawLine(x + 8, y + 14, x + 3, y + 10);
            g2.drawRect(x + 1, y + 5, 4, 10); // Caja
            g2.dispose();
        }
    }

    private class IconoBasura implements Icon {
        @Override
        public int getIconWidth() {
            return 20;
        }

        @Override
        public int getIconHeight() {
            return 20;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(c.getForeground());
            g2.setStroke(new BasicStroke(1.5f));
            g2.drawLine(x + 4, y + 4, x + 16, y + 4); // Tapa
            g2.drawLine(x + 8, y + 4, x + 8, y + 2);
            g2.drawLine(x + 12, y + 4, x + 12, y + 2);
            g2.drawLine(x + 8, y + 2, x + 12, y + 2);
            g2.drawRect(x + 5, y + 4, 10, 12); // Bote
            g2.drawLine(x + 8, y + 6, x + 8, y + 14);
            g2.drawLine(x + 12, y + 6, x + 12, y + 14);
            g2.dispose();
        }
    }
}
