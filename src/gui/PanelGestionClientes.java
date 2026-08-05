package gui;

import modelo.Cliente;
import dao.ClienteDAO;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.geom.Ellipse2D;

public class PanelGestionClientes extends JPanel {

    private JTable tablaClientes;
    private DefaultTableModel modeloTabla;
    private JButton btnNuevoCliente;
    private JTextField txtBusqueda;
    private TableRowSorter<DefaultTableModel> sorter; // Para el buscador

    public PanelGestionClientes() {
        iniciarDiseno();
    }

    private void iniciarDiseno() {
        this.removeAll();
        this.setLayout(new BorderLayout(20, 20));
        this.setBackground(utilidades.EfectosUI.COLOR_FONDO_PANEL); // Verde Vintage
        this.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        // --- 1. PANEL SUPERIOR (Título, Búsqueda y Botón Nuevo) ---
        JPanel panelSuperior = new JPanel(new BorderLayout(20, 0)); // 20px de separación horizontal
        panelSuperior.setOpaque(false);

        JLabel lblTitulo = new JLabel("Directorio de Clientes");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 28));
        lblTitulo.setForeground(utilidades.EfectosUI.COLOR_TEXTO_TITULO);

        // El buscador lo metemos en un panel central para que no se estire a lo loco
        JPanel panelCentro = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        panelCentro.setOpaque(false);
        txtBusqueda = new JTextField(20);
        txtBusqueda.putClientProperty("JTextField.placeholderText", "Buscar Cliente...");
        txtBusqueda.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtBusqueda.setPreferredSize(new Dimension(250, 35));
        panelCentro.add(txtBusqueda);

        btnNuevoCliente = new JButton("+ Nuevo Cliente");
        btnNuevoCliente.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnNuevoCliente.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        btnNuevoCliente.putClientProperty("JButton.buttonType", "roundRect");
        utilidades.EfectosUI.aplicarEfectoHover(btnNuevoCliente, utilidades.EfectosUI.COLOR_VERDE_PRIMARIO, utilidades.EfectosUI.COLOR_VERDE_HOVER, Color.WHITE, Color.WHITE);
        
        btnNuevoCliente.addActionListener(e -> abrirFormularioCliente(null));

        // Asignamos cada cosa a su esquina para que nunca choquen
        panelSuperior.add(lblTitulo, BorderLayout.WEST);
        panelSuperior.add(panelCentro, BorderLayout.CENTER);
        panelSuperior.add(btnNuevoCliente, BorderLayout.EAST);

        this.add(panelSuperior, BorderLayout.NORTH);
        // --- 2. CONFIGURACIÓN DE LA TABLA ESTILO WEB ---
        // Eliminamos la columna "Acciones" y recorremos los ocultos
        String[] columnas = {"ID", "", "Nombre Completo", "Identidad/RTN", "Teléfono", "Correo Electrónico", "NombreRaw", "ApellidoRaw"};
        modeloTabla = new DefaultTableModel(null, columnas) {
            @Override public boolean isCellEditable(int row, int column) { return false; } // Nada es editable directamente
        };

        tablaClientes = new JTable(modeloTabla);
        
        tablaClientes.setShowGrid(false);
        tablaClientes.setIntercellSpacing(new Dimension(0, 0));
        tablaClientes.setRowHeight(55);
        tablaClientes.setBackground(new Color(255, 255, 255)); // Blanco Puro
        tablaClientes.setForeground(new Color(30, 41, 59));
        tablaClientes.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tablaClientes.setSelectionBackground(new Color(209, 250, 229)); // Menta luminosa
        tablaClientes.setSelectionForeground(new Color(6, 95, 70));

        tablaClientes.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        tablaClientes.getTableHeader().setBackground(new Color(236, 253, 245)); // Verde Menta fresca
        tablaClientes.getTableHeader().setForeground(new Color(6, 95, 70));     // Verde Esmeralda Oscuro
        tablaClientes.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(16, 185, 129)));
        tablaClientes.getTableHeader().setPreferredSize(new Dimension(0, 42));

        // Ocultar ID (0), NombreRaw (6) y ApellidoRaw (7)
        ocultarColumna(0);
        ocultarColumna(6);
        ocultarColumna(7);

        // Renderizador de Avatar
        tablaClientes.getColumnModel().getColumn(1).setPreferredWidth(60);
        tablaClientes.getColumnModel().getColumn(1).setMaxWidth(60);
        tablaClientes.getColumnModel().getColumn(1).setCellRenderer(new AvatarRenderer());

        // --- NUEVA LÓGICA DE MENÚ CONTEXTUAL ---
        tablaClientes.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                int fila = tablaClientes.rowAtPoint(e.getPoint());
                if (fila >= 0) {
                    tablaClientes.setRowSelectionInterval(fila, fila); // Selecciona visualmente la fila
                    // Mostrar menú con doble clic o clic derecho
                    if ((SwingUtilities.isLeftMouseButton(e) && e.getClickCount() == 2) || SwingUtilities.isRightMouseButton(e)) {
                        mostrarMenuOpciones(e.getComponent(), e.getX(), e.getY(), fila);
                    }
                }
            }
        });

        // LÓGICA DEL BUSCADOR EN TIEMPO REAL
        sorter = new TableRowSorter<>(modeloTabla);
        tablaClientes.setRowSorter(sorter);
        txtBusqueda.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { filtrar(); }
            @Override public void removeUpdate(DocumentEvent e) { filtrar(); }
            @Override public void changedUpdate(DocumentEvent e) { filtrar(); }
            private void filtrar() {
                String texto = txtBusqueda.getText();
                if (texto.trim().length() == 0) sorter.setRowFilter(null);
                else sorter.setRowFilter(RowFilter.regexFilter("(?i)" + texto, 2, 3));
            }
        });

        JScrollPane scrollPane = new JScrollPane(tablaClientes);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(220, 222, 225), 1, true));
        scrollPane.getViewport().setBackground(new Color(255, 255, 255)); // Fondo del área sin filas

        this.add(scrollPane, BorderLayout.CENTER);

        cargarDatosDesdeBD();
    }

    private void ocultarColumna(int index) {
        tablaClientes.getColumnModel().getColumn(index).setMinWidth(0);
        tablaClientes.getColumnModel().getColumn(index).setMaxWidth(0);
        tablaClientes.getColumnModel().getColumn(index).setWidth(0);
    }

    public void cargarDatosDesdeBD() {
        modeloTabla.setRowCount(0);
        ClienteDAO dao = new ClienteDAO();
        for (Cliente c : dao.listarClientesActivos()) {
            String apellido = c.getApellidoCliente() != null ? c.getApellidoCliente() : "";
            String nombreCompleto = c.getNombreCliente() + " " + apellido;
            
            modeloTabla.addRow(new Object[]{
                c.getIdCliente(),           // 0: ID
                nombreCompleto,             // 1: Avatar
                nombreCompleto,             // 2: Nombre Completo
                c.getIdentidadCliente(),    // 3: Identidad
                c.getTelefonoCliente(),     // 4: Teléfono
                c.getCorreoCliente(),       // 5: Correo
                c.getNombreCliente(),       // 6: Nombre Real Oculto
                apellido                    // 7: Apellido Real Oculto
            });
        }
    }

    private void abrirFormularioCliente(Cliente cliente) {
        Window ventanaPadre = SwingUtilities.getWindowAncestor(this);
        JDialog dialog = new JDialog((Frame) ventanaPadre, "Cliente", true);
        dialog.setUndecorated(true);
        dialog.setBackground(new Color(0, 0, 0, 0)); 
        
        // Estado visual de carga (Cursor)
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        if (btnNuevoCliente != null) btnNuevoCliente.setEnabled(false);

        SwingWorker<PanelFormularioCliente, Void> worker = new SwingWorker<PanelFormularioCliente, Void>() {
            @Override
            protected PanelFormularioCliente doInBackground() throws Exception {
                // Instancia el panel en segundo plano (las DB queries corren aquí)
                return new PanelFormularioCliente(dialog, PanelGestionClientes.this, cliente);
            }

            @Override
            protected void done() {
                setCursor(Cursor.getDefaultCursor());
                if (btnNuevoCliente != null) btnNuevoCliente.setEnabled(true);
                try {
                    PanelFormularioCliente panelFormulario = get();
                    dialog.add(panelFormulario);
                    dialog.pack();
                    dialog.setLocationRelativeTo(ventanaPadre);
                    dialog.setVisible(true);
                } catch (Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(PanelGestionClientes.this, "Error al abrir el formulario: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    // =========================================================================
    // CLASES INTERNAS (Renderizadores y Botones)
    // =========================================================================

    private class AvatarRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JPanel panel = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2 = (Graphics2D) g;
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                    String nombre = value != null ? value.toString() : "?";
                    String inicial = nombre.isEmpty() ? "?" : nombre.substring(0, 1).toUpperCase();

                    int hash = Math.abs(inicial.hashCode());
                    Color[] paleta = {new Color(227, 0, 15), new Color(39, 174, 96), new Color(13, 110, 253), new Color(253, 126, 20), new Color(111, 66, 193)};
                    g2.setColor(paleta[hash % paleta.length]);
                    
                    int size = 36;
                    int x = (getWidth() - size) / 2;
                    int y = (getHeight() - size) / 2;
                    g2.fill(new Ellipse2D.Double(x, y, size, size));

                    g2.setColor(Color.WHITE);
                    g2.setFont(new Font("Segoe UI", Font.BOLD, 18));
                    FontMetrics fm = g2.getFontMetrics();
                    int textX = x + (size - fm.stringWidth(inicial)) / 2;
                    int textY = y + ((size - fm.getHeight()) / 2) + fm.getAscent();
                    g2.drawString(inicial, textX, textY);
                }
            };
            panel.setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
            return panel;
        }
    }

    // =========================================================
    // LÓGICA Y MENÚ CONTEXTUAL DE OPCIONES
    // =========================================================
    private void mostrarMenuOpciones(Component componente, int x, int y, int filaVista) {
        JPopupMenu menu = new JPopupMenu();
        menu.setBackground(new Color(255, 255, 255)); // Blanco puro
        menu.setBorder(BorderFactory.createLineBorder(new Color(220, 222, 225), 1));

        JMenuItem itemEditar = crearMenuItem("Editar Cliente", new Color(39, 174, 96), new IconoLapiz()); // Verde Menta
        JMenuItem itemEliminar = crearMenuItem("Eliminar Cliente", new Color(227, 0, 15), new IconoBasurero()); // Rojo Logo

        itemEditar.addActionListener(e -> editarClienteSeleccionado(filaVista));
        itemEliminar.addActionListener(e -> eliminarClienteSeleccionado(filaVista));

        menu.add(itemEditar);
        menu.addSeparator(); 
        menu.add(itemEliminar);
        
        menu.show(componente, x, y);
    }

    private void editarClienteSeleccionado(int filaVista) {
        int filaModelo = tablaClientes.convertRowIndexToModel(filaVista);
        Cliente c = new Cliente();
        c.setIdCliente((int) modeloTabla.getValueAt(filaModelo, 0));
        c.setIdentidadCliente(modeloTabla.getValueAt(filaModelo, 3).toString());
        c.setTelefonoCliente(modeloTabla.getValueAt(filaModelo, 4) != null ? modeloTabla.getValueAt(filaModelo, 4).toString() : "");
        c.setCorreoCliente(modeloTabla.getValueAt(filaModelo, 5) != null ? modeloTabla.getValueAt(filaModelo, 5).toString() : "");
        c.setNombreCliente(modeloTabla.getValueAt(filaModelo, 6).toString());
        c.setApellidoCliente(modeloTabla.getValueAt(filaModelo, 7).toString());
        
        abrirFormularioCliente(c);
    }

    private void eliminarClienteSeleccionado(int filaVista) {
        int filaModelo = tablaClientes.convertRowIndexToModel(filaVista);
        int idCliente = (int) modeloTabla.getValueAt(filaModelo, 0);
        String nombre = modeloTabla.getValueAt(filaModelo, 2).toString();
        
        int confirmacion = JOptionPane.showConfirmDialog(this, 
            "¿Está seguro de desactivar al cliente: " + nombre + "?", 
            "Confirmar", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            
        if (confirmacion == JOptionPane.YES_OPTION) {
            ClienteDAO dao = new ClienteDAO();
            if (dao.desactivarCliente(idCliente)) {
                JOptionPane.showMessageDialog(this, "Cliente eliminado exitosamente.");
                cargarDatosDesdeBD();
            }
        }
    }

    private JMenuItem crearMenuItem(String texto, Color colorHover, Icon icono) {
        JMenuItem item = new JMenuItem(texto);
        item.setIcon(icono);
        item.setIconTextGap(12);
        item.setFont(new Font("Segoe UI", Font.BOLD, 14));
        item.setForeground(new Color(45, 45, 45)); // Gris Oscuro
        item.setBackground(new Color(255, 255, 255)); // Blanco puro
        item.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        item.setCursor(new Cursor(Cursor.HAND_CURSOR));
        item.setOpaque(true);
        item.addChangeListener(e -> {
            if (item.isArmed()) {
                item.setBackground(colorHover);
                item.setForeground(Color.WHITE); // Texto blanco al pasar el mouse
            } else {
                item.setBackground(new Color(255, 255, 255));
                item.setForeground(new Color(45, 45, 45)); // Texto oscuro al quitar el mouse
            }
        });
        return item;
    }

    // =========================================================
    // ÍCONOS VECTORIALES (JAVA 2D)
    // =========================================================
    private class IconoLapiz implements Icon {
        @Override public int getIconWidth() { return 20; }
        @Override public int getIconHeight() { return 20; }
        @Override public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            // Hereda el color del texto del item (gris oscuro o blanco si tiene hover)
            g2.setColor(c.getForeground()); 
            g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            // Lápiz de edición diagonal
            g2.drawPolygon(new int[]{x+14, x+17, x+6, x+3, x+3}, new int[]{y+3, y+6, y+17, y+17, y+14}, 5);
            g2.drawLine(x+11, y+6, x+14, y+9); 
            g2.drawLine(x+3, y+17, x+6, y+14); 
            g2.dispose();
        }
    }

    private class IconoBasurero implements Icon {
        @Override public int getIconWidth() { return 20; }
        @Override public int getIconHeight() { return 20; }
        @Override public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            // Hereda el color del texto del item para integrarse bien con el hover
            g2.setColor(c.getForeground()); 
            g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.drawRoundRect(x + 8, y + 2, 4, 3, 2, 2);
            g2.drawLine(x + 4, y + 5, x + 16, y + 5);
            g2.drawRoundRect(x + 5, y + 5, 10, 12, 3, 3);
            g2.setStroke(new BasicStroke(1.5f));
            g2.drawLine(x + 8, y + 8, x + 8, y + 14);
            g2.drawLine(x + 12, y + 8, x + 12, y + 14);
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
