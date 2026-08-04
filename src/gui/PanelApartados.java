package gui;

import dao.ApartadoDAO;
import dao.KardexDAO;
import dao.VentasDAO;
import modelo.Apartado;
import modelo.DetalleApartado;
import modelo.AbonoApartado;
import utilidades.SesionGlobal;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;

public class PanelApartados extends JPanel {
    private ApartadoDAO dao;
    private List<Apartado> apartados;
    private JTable tablaApartados;
    private DefaultTableModel modeloTabla;
    private JTextField txtBuscar;
    private JCheckBox chkMostrarEntregados;

    public PanelApartados() {
        this.dao = new ApartadoDAO();
        iniciarDiseno();
        cargarApartados();
    }

    private void iniciarDiseno() {
        this.setLayout(new BorderLayout(20, 20));
        this.setBackground(utilidades.EfectosUI.COLOR_FONDO_PANEL); // Verde Vintage
        this.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        // Cabecera
        JPanel pnlCabecera = new JPanel(new BorderLayout());
        pnlCabecera.setOpaque(false);

        JLabel lblTitulo = new JLabel("Módulo de Apartados y Abonos");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitulo.setForeground(utilidades.EfectosUI.COLOR_TEXTO_TITULO);
        pnlCabecera.add(lblTitulo, BorderLayout.WEST);

        JPanel pnlBuscar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        pnlBuscar.setOpaque(false);
        txtBuscar = new JTextField(15);
        txtBuscar.putClientProperty("JTextField.placeholderText", "Buscar por cliente, ID o DNI...");
        txtBuscar.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtBuscar.setPreferredSize(new Dimension(200, 35));
        txtBuscar.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(220, 222, 225)), BorderFactory.createEmptyBorder(0, 8, 0, 8)));
        txtBuscar.addActionListener(e -> filtrarApartados());
        pnlBuscar.add(txtBuscar);

        chkMostrarEntregados = new JCheckBox("Mostrar Entregados/Cancelados");
        chkMostrarEntregados.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        chkMostrarEntregados.setOpaque(false);
        chkMostrarEntregados.setForeground(utilidades.EfectosUI.COLOR_TEXTO_OSCURO);
        chkMostrarEntregados.setSelected(false);
        chkMostrarEntregados.addActionListener(e -> cargarApartados());
        pnlBuscar.add(chkMostrarEntregados);

        pnlCabecera.add(pnlBuscar, BorderLayout.EAST);

        this.add(pnlCabecera, BorderLayout.NORTH);

        // Tabla
        String[] cols = {"ID", "Fecha", "Fecha Límite", "Cliente", "Total", "Abonado", "Saldo Pendiente", "Estado"};
        modeloTabla = new DefaultTableModel(null, cols) { @Override public boolean isCellEditable(int r, int c) { return false; } };
        tablaApartados = new JTable(modeloTabla);
        tablaApartados.setRowHeight(32);
        tablaApartados.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        
        tablaApartados.getColumnModel().getColumn(7).setCellRenderer(new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (value != null) {
                    String estado = value.toString().toUpperCase();
                    if (estado.equals("ENTREGADO")) {
                        c.setForeground(isSelected ? Color.WHITE : new Color(39, 174, 96));
                        c.setFont(c.getFont().deriveFont(Font.BOLD));
                    } else if (estado.equals("CANCELADO")) {
                        c.setForeground(isSelected ? Color.WHITE : new Color(231, 76, 60));
                        c.setFont(c.getFont().deriveFont(Font.BOLD));
                    } else if (estado.equals("VIGENTE")) {
                        c.setForeground(isSelected ? Color.WHITE : new Color(41, 128, 185));
                        c.setFont(c.getFont().deriveFont(Font.BOLD));
                    } else if (estado.equals("PAGADO")) {
                        c.setForeground(isSelected ? Color.WHITE : new Color(243, 156, 18));
                        c.setFont(c.getFont().deriveFont(Font.BOLD));
                    } else {
                        c.setForeground(isSelected ? table.getSelectionForeground() : table.getForeground());
                        c.setFont(c.getFont().deriveFont(Font.PLAIN));
                    }
                }
                return c;
            }
        });

        JScrollPane scroll = new JScrollPane(tablaApartados);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(220, 222, 225)));
        scroll.getViewport().setBackground(Color.WHITE);
        this.add(scroll, BorderLayout.CENTER);

        // Botonera Lateral/Inferior
        JPanel pnlBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        pnlBotones.setOpaque(false);

        JButton btnDetalles = new JButton("Ver Detalles", new IconoBoton(1));
        btnDetalles.setBackground(Color.WHITE);
        btnDetalles.setForeground(new Color(45, 45, 45));
        btnDetalles.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnDetalles.setFocusPainted(false);
        btnDetalles.setIconTextGap(8);
        btnDetalles.setPreferredSize(new Dimension(140, 38));
        btnDetalles.setBorder(BorderFactory.createLineBorder(new Color(220, 222, 225)));
        btnDetalles.addActionListener(e -> verDetallesApartado());

        JButton btnAbonar = new JButton("Registrar Abono", new IconoBoton(2));
        btnAbonar.setBackground(new Color(39, 174, 96)); // Verde Menta
        btnAbonar.setForeground(Color.WHITE);
        btnAbonar.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnAbonar.setFocusPainted(false);
        btnAbonar.setIconTextGap(8);
        btnAbonar.setPreferredSize(new Dimension(160, 38));
        btnAbonar.addActionListener(e -> registrarAbonoRapido());

        JButton btnEntregar = new JButton("Entregar Artículos", new IconoBoton(3));
        btnEntregar.setBackground(new Color(41, 128, 185)); // Azul
        btnEntregar.setForeground(Color.WHITE);
        btnEntregar.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnEntregar.setFocusPainted(false);
        btnEntregar.setIconTextGap(8);
        btnEntregar.setPreferredSize(new Dimension(175, 38));
        btnEntregar.addActionListener(e -> entregarArticulos());

        JButton btnCancelar = new JButton("Cancelar Apartado", new IconoBoton(4));
        btnCancelar.setBackground(new Color(227, 0, 15)); // Rojo Logo
        btnCancelar.setForeground(Color.WHITE);
        btnCancelar.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnCancelar.setFocusPainted(false);
        btnCancelar.setIconTextGap(8);
        btnCancelar.setPreferredSize(new Dimension(175, 38));
        btnCancelar.addActionListener(e -> cancelarApartadoCompleto());

        // Restringir botón de cancelar si es cajero
        int rolId = SesionGlobal.getUsuarioActual() != null ? SesionGlobal.getUsuarioActual().getIdRol() : 3;
        if (rolId == 3) {
            btnCancelar.setVisible(false);
        }

        pnlBotones.add(btnDetalles);
        pnlBotones.add(btnAbonar);
        pnlBotones.add(btnEntregar);
        pnlBotones.add(btnCancelar);
        this.add(pnlBotones, BorderLayout.SOUTH);
    }

    private void cargarApartados() {
        modeloTabla.setRowCount(0);
        apartados = dao.listarApartados();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        boolean mostrarEntregados = chkMostrarEntregados.isSelected();

        for (Apartado a : apartados) {
            String estado = a.getEstadoApartado();
            if (!mostrarEntregados && (estado.equalsIgnoreCase("Entregado") || estado.equalsIgnoreCase("Cancelado"))) {
                continue; // Skip delivered or canceled ones
            }

            String fApe = sdf.format(a.getFechaApartado());
            String fLim = a.getFechaLimite() != null ? sdf.format(a.getFechaLimite()) : "N/A";
            String clie = a.getNombreCliente() + " " + (a.getApellidoCliente() != null ? a.getApellidoCliente() : "");
            
            modeloTabla.addRow(new Object[]{
                a.getIdApartado(),
                fApe,
                fLim,
                clie,
                "L " + String.format("%,.2f", a.getTotalApartado()),
                "L " + String.format("%,.2f", a.getTotalApartado() - a.getSaldoPendiente()),
                "L " + String.format("%,.2f", a.getSaldoPendiente()),
                estado
            });
        }
    }

    private void filtrarApartados() {
        String filter = txtBuscar.getText().toLowerCase().trim();
        if (filter.isEmpty()) { cargarApartados(); return; }
        
        modeloTabla.setRowCount(0);
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        boolean mostrarEntregados = chkMostrarEntregados.isSelected();

        for (Apartado a : apartados) {
            String estado = a.getEstadoApartado();
            if (!mostrarEntregados && (estado.equalsIgnoreCase("Entregado") || estado.equalsIgnoreCase("Cancelado"))) {
                continue;
            }

            String clie = (a.getNombreCliente() + " " + (a.getApellidoCliente() != null ? a.getApellidoCliente() : "")).toLowerCase();
            String dni = a.getIdentidadCliente() != null ? a.getIdentidadCliente().toLowerCase().replace("-", "").replace(" ", "") : "";
            String idStr = String.valueOf(a.getIdApartado());
            String filterClean = filter.replace("-", "").replace(" ", "");

            if (clie.contains(filter) || idStr.contains(filter) || dni.contains(filterClean) || dni.contains(filter)) {
                String fApe = sdf.format(a.getFechaApartado());
                String fLim = a.getFechaLimite() != null ? sdf.format(a.getFechaLimite()) : "N/A";
                modeloTabla.addRow(new Object[]{
                    a.getIdApartado(),
                    fApe,
                    fLim,
                    a.getNombreCliente() + " " + (a.getApellidoCliente() != null ? a.getApellidoCliente() : ""),
                    "L " + String.format("%,.2f", a.getTotalApartado()),
                    "L " + String.format("%,.2f", a.getTotalApartado() - a.getSaldoPendiente()),
                    "L " + String.format("%,.2f", a.getSaldoPendiente()),
                    estado
                });
            }
        }
    }

    private void registrarAbonoRapido() {
        int selectedRow = tablaApartados.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Seleccione un apartado de la lista.", "Aviso", JOptionPane.WARNING_MESSAGE); return;
        }

        int id = (int) tablaApartados.getValueAt(selectedRow, 0);
        Apartado ap = dao.obtenerPorId(id);
        
        if (ap == null) return;
        if (ap.getEstadoApartado().equalsIgnoreCase("Cancelado") || ap.getEstadoApartado().equalsIgnoreCase("Entregado")) {
            JOptionPane.showMessageDialog(this, "No se pueden aplicar abonos a apartados en estado: " + ap.getEstadoApartado(), "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Check active cash session
        modelo.ControlCaja CCActiva = new dao.ControlCajaDAO().obtenerSesionActiva();
        if (CCActiva == null) {
            JOptionPane.showMessageDialog(this, "Operación denegada: El turno de caja no está abierto. Abra la caja antes de registrar cobros.", "Caja Cerrada", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Window parent = SwingUtilities.getWindowAncestor(this);
        DialogoRegistrarAbono dialog = new DialogoRegistrarAbono((Frame) parent, id);
        dialog.setVisible(true);

        if (dialog.isExito()) {
            cargarApartados();
        }
    }

    private void entregarArticulos() {
        int selectedRow = tablaApartados.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Seleccione un apartado de la lista.", "Aviso", JOptionPane.WARNING_MESSAGE); return;
        }

        int id = (int) tablaApartados.getValueAt(selectedRow, 0);
        Apartado ap = dao.obtenerPorId(id);

        if (ap == null) return;
        if (ap.getSaldoPendiente() > 0.05) {
            JOptionPane.showMessageDialog(this, "No se puede entregar la mercancía de un apartado con saldo pendiente (L " + String.format("%,.2f", ap.getSaldoPendiente()) + ").", "Acceso Denegado", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (ap.getEstadoApartado().equalsIgnoreCase("Entregado")) {
            JOptionPane.showMessageDialog(this, "El apartado ya fue entregado.", "Aviso", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        if (ap.getEstadoApartado().equalsIgnoreCase("Cancelado")) {
            JOptionPane.showMessageDialog(this, "El apartado se encuentra cancelado.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JCheckBox chkISV = new JCheckBox("Calcular y aplicar 15% de Impuesto (ISV) a esta venta", true);
        Object[] msj = {
            "¿Confirmar que la mercancía ha sido entregada físicamente al cliente?",
            "Esta acción registrará oficialmente los ingresos en el Historial de Ventas.",
            " ",
            chkISV
        };
        int opt = JOptionPane.showConfirmDialog(this, msj, "Entregar Apartado y Registrar Venta", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);
        
        if (opt == JOptionPane.YES_OPTION) {
            int idUser = utilidades.SesionGlobal.getUsuarioActual() != null ? utilidades.SesionGlobal.getUsuarioActual().getIdUsuario() : 1;
            int idVentaGenerada = dao.entregarApartadoYGenerarVenta(id, idUser, chkISV.isSelected());
            if (idVentaGenerada > 0) {
                try {
                    java.io.File pdf = utilidades.GeneradorTickets.generarFactura(idVentaGenerada);
                    if (java.awt.Desktop.isDesktopSupported()) {
                        java.awt.Desktop.getDesktop().open(pdf);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                JOptionPane.showMessageDialog(this, "Apartado completado, entregado y Comprobante registrado exitosamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                cargarApartados();
            } else {
                JOptionPane.showMessageDialog(this, "Error de base de datos al entregar apartado y generar venta.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void cancelarApartadoCompleto() {
        int selectedRow = tablaApartados.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Seleccione un apartado de la lista.", "Aviso", JOptionPane.WARNING_MESSAGE); return;
        }

        int id = (int) tablaApartados.getValueAt(selectedRow, 0);
        Apartado ap = dao.obtenerPorId(id);

        if (ap == null) return;
        if (ap.getEstadoApartado().equalsIgnoreCase("Cancelado") || ap.getEstadoApartado().equalsIgnoreCase("Entregado")) {
            JOptionPane.showMessageDialog(this, "No se puede cancelar un apartado con estado: " + ap.getEstadoApartado(), "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int opt = JOptionPane.showConfirmDialog(this, "Al cancelar, los productos se devolverán al inventario activo. ¿Desea continuar?", "Cancelar Apartado", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (opt == JOptionPane.YES_OPTION) {
            // Password signature
            JPasswordField pfPass = new JPasswordField();
            int opSign = JOptionPane.showConfirmDialog(this, new Object[]{"Ingrese su contraseña para firmar la cancelación:", pfPass}, "Firma Requerida", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (opSign != JOptionPane.OK_OPTION) return;

            String pass = new String(pfPass.getPassword());
            int idUserFirma = new KardexDAO().validarFirmaUsuario(pass);

            if (idUserFirma <= 0) {
                JOptionPane.showMessageDialog(this, "Contraseña incorrecta o usuario inactivo.", "Firma Rechazada", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (dao.cancelarApartado(id, idUserFirma)) {
                JOptionPane.showMessageDialog(this, "Apartado cancelado exitosamente y stock retornado al inventario.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                cargarApartados();
            } else {
                JOptionPane.showMessageDialog(this, "Error al registrar cancelación.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void verDetallesApartado() {
        int selectedRow = tablaApartados.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Seleccione un apartado de la lista.", "Aviso", JOptionPane.WARNING_MESSAGE); return;
        }

        int id = (int) tablaApartados.getValueAt(selectedRow, 0);
        
        Window parent = SwingUtilities.getWindowAncestor(this);
        DialogoDetallesApartado dialog = new DialogoDetallesApartado((Frame) parent, id);
        dialog.setVisible(true);
    }
    
    // Clase interna para dibujar iconos vectoriales de los botones sin usar emojis
    private class IconoBoton implements Icon {
        private int tipo;
        private int size = 18;
        
        public IconoBoton(int tipo) { this.tipo = tipo; }

        @Override public int getIconWidth() { return size; }
        @Override public int getIconHeight() { return size; }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(c.getForeground());
            g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

            switch (tipo) {
                case 1: // Ojo (Detalles)
                    g2.drawArc(x + 2, y + 5, 14, 8, 0, 180);
                    g2.drawArc(x + 2, y + 5, 14, 8, 180, 180);
                    g2.drawOval(x + 7, y + 7, 4, 4);
                    break;
                case 2: // Billete (Abonar)
                    g2.drawRect(x + 2, y + 4, 14, 10);
                    g2.drawOval(x + 7, y + 7, 4, 4);
                    g2.drawLine(x + 4, y + 6, x + 4, y + 6);
                    g2.drawLine(x + 14, y + 12, x + 14, y + 12);
                    break;
                case 3: // Caja (Entregar)
                    g2.drawPolygon(new int[]{x + 9, x + 16, x + 16, x + 9, x + 2, x + 2}, 
                                   new int[]{y + 2, y + 6, y + 14, y + 17, y + 14, y + 6}, 6);
                    g2.drawLine(x + 2, y + 6, x + 9, y + 10);
                    g2.drawLine(x + 16, y + 6, x + 9, y + 10);
                    g2.drawLine(x + 9, y + 10, x + 9, y + 17);
                    break;
                case 4: // Cruz (Cancelar)
                    g2.drawLine(x + 5, y + 5, x + 13, y + 13);
                    g2.drawLine(x + 13, y + 5, x + 5, y + 13);
                    break;
            }
            g2.dispose();
        }
    }
}
