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

    public PanelApartados() {
        this.dao = new ApartadoDAO();
        iniciarDiseno();
        cargarApartados();
    }

    private void iniciarDiseno() {
        this.setLayout(new BorderLayout(20, 20));
        this.setBackground(new Color(240, 242, 245)); // Gris Nube
        this.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        // Cabecera
        JPanel pnlCabecera = new JPanel(new BorderLayout());
        pnlCabecera.setOpaque(false);

        JLabel lblTitulo = new JLabel("Módulo de Apartados y Abonos");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitulo.setForeground(new Color(45, 45, 45));
        pnlCabecera.add(lblTitulo, BorderLayout.WEST);

        JPanel pnlBuscar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        pnlBuscar.setOpaque(false);
        txtBuscar = new JTextField(15);
        txtBuscar.putClientProperty("JTextField.placeholderText", "Buscar por cliente...");
        txtBuscar.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtBuscar.setPreferredSize(new Dimension(200, 35));
        txtBuscar.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(220, 222, 225)), BorderFactory.createEmptyBorder(0, 8, 0, 8)));
        txtBuscar.addActionListener(e -> filtrarApartados());
        pnlBuscar.add(txtBuscar);
        pnlCabecera.add(pnlBuscar, BorderLayout.EAST);

        this.add(pnlCabecera, BorderLayout.NORTH);

        // Tabla
        String[] cols = {"ID", "Fecha", "Fecha Límite", "Cliente", "Total", "Abonado", "Saldo Pendiente", "Estado"};
        modeloTabla = new DefaultTableModel(null, cols) { @Override public boolean isCellEditable(int r, int c) { return false; } };
        tablaApartados = new JTable(modeloTabla);
        tablaApartados.setRowHeight(32);
        tablaApartados.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        
        JScrollPane scroll = new JScrollPane(tablaApartados);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(220, 222, 225)));
        scroll.getViewport().setBackground(Color.WHITE);
        this.add(scroll, BorderLayout.CENTER);

        // Botonera Lateral/Inferior
        JPanel pnlBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        pnlBotones.setOpaque(false);

        JButton btnDetalles = new JButton("👁 Ver Detalles");
        btnDetalles.setBackground(Color.WHITE);
        btnDetalles.setForeground(new Color(45, 45, 45));
        btnDetalles.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnDetalles.setFocusPainted(false);
        btnDetalles.setPreferredSize(new Dimension(140, 38));
        btnDetalles.setBorder(BorderFactory.createLineBorder(new Color(220, 222, 225)));
        btnDetalles.addActionListener(e -> verDetallesApartado());

        JButton btnAbonar = new JButton("💵 Registrar Abono");
        btnAbonar.setBackground(new Color(39, 174, 96)); // Verde Menta
        btnAbonar.setForeground(Color.WHITE);
        btnAbonar.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnAbonar.setFocusPainted(false);
        btnAbonar.setPreferredSize(new Dimension(150, 38));
        btnAbonar.addActionListener(e -> registrarAbonoRapido());

        JButton btnEntregar = new JButton("📦 Entregar Artículos");
        btnEntregar.setBackground(new Color(41, 128, 185)); // Azul
        btnEntregar.setForeground(Color.WHITE);
        btnEntregar.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnEntregar.setFocusPainted(false);
        btnEntregar.setPreferredSize(new Dimension(160, 38));
        btnEntregar.addActionListener(e -> entregarArticulos());

        JButton btnCancelar = new JButton("❌ Cancelar Apartado");
        btnCancelar.setBackground(new Color(227, 0, 15)); // Rojo Logo
        btnCancelar.setForeground(Color.WHITE);
        btnCancelar.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnCancelar.setFocusPainted(false);
        btnCancelar.setPreferredSize(new Dimension(165, 38));
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
        for (Apartado a : apartados) {
            String fApe = sdf.format(a.getFechaApartado());
            String fLim = a.getFechaLimite() != null ? sdf.format(a.getFechaLimite()) : "N/A";
            String clie = a.getNombreCliente() + " " + (a.getApellidoCliente() != null ? a.getApellidoCliente() : "");
            
            modeloTabla.addRow(new Object[]{
                a.getIdApartado(),
                fApe,
                fLim,
                clie,
                "L " + String.format("%.2f", a.getTotalApartado()),
                "L " + String.format("%.2f", a.getTotalApartado() - a.getSaldoPendiente()),
                "L " + String.format("%.2f", a.getSaldoPendiente()),
                a.getEstadoApartado()
            });
        }
    }

    private void filtrarApartados() {
        String filter = txtBuscar.getText().toLowerCase().trim();
        if (filter.isEmpty()) { cargarApartados(); return; }
        
        modeloTabla.setRowCount(0);
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        for (Apartado a : apartados) {
            String clie = (a.getNombreCliente() + " " + (a.getApellidoCliente() != null ? a.getApellidoCliente() : "")).toLowerCase();
            if (clie.contains(filter)) {
                String fApe = sdf.format(a.getFechaApartado());
                String fLim = a.getFechaLimite() != null ? sdf.format(a.getFechaLimite()) : "N/A";
                modeloTabla.addRow(new Object[]{
                    a.getIdApartado(),
                    fApe,
                    fLim,
                    a.getNombreCliente() + " " + (a.getApellidoCliente() != null ? a.getApellidoCliente() : ""),
                    "L " + String.format("%.2f", a.getTotalApartado()),
                    "L " + String.format("%.2f", a.getTotalApartado() - a.getSaldoPendiente()),
                    "L " + String.format("%.2f", a.getSaldoPendiente()),
                    a.getEstadoApartado()
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

        JTextField txtAbono = new JTextField();
        Map<Integer, String> metodos = new VentasDAO().obtenerMetodosPago();
        JComboBox<String> cmbMetodo = new JComboBox<>();
        for (String m : metodos.values()) cmbMetodo.addItem(m);
        JTextField txtRef = new JTextField();
        JComboBox<String> cmbBanco = new JComboBox<>(new String[]{"Seleccione Banco...", "BAC", "FICOHSA", "ATLANTIDA", "BANPAIS", "OCCIDENTE"});

        Object[] fields = {
            "Saldo Pendiente: L " + String.format("%.2f", ap.getSaldoPendiente()),
            "Monto a Abonar (L):", txtAbono,
            "Método de Pago:", cmbMetodo,
            "Referencia/Voucher:", txtRef,
            "Banco:", cmbBanco
        };

        int opt = JOptionPane.showConfirmDialog(this, fields, "Registrar Abono", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (opt == JOptionPane.OK_OPTION) {
            try {
                double abono = Double.parseDouble(txtAbono.getText().trim());
                if (abono <= 0) { JOptionPane.showMessageDialog(this, "El abono debe ser mayor a 0.", "Error", JOptionPane.ERROR_MESSAGE); return; }
                if (abono > ap.getSaldoPendiente() + 0.05) { JOptionPane.showMessageDialog(this, "El abono no puede exceder el saldo pendiente.", "Error", JOptionPane.ERROR_MESSAGE); return; }

                // Get method id
                int idMetodo = 1;
                String selectedMetodo = cmbMetodo.getSelectedItem().toString();
                for (Map.Entry<Integer, String> entry : metodos.entrySet()) {
                    if (entry.getValue().equals(selectedMetodo)) {
                        idMetodo = entry.getKey();
                        break;
                    }
                }

                String ref = txtRef.getText().trim();
                String banco = cmbBanco.getSelectedIndex() > 0 ? cmbBanco.getSelectedItem().toString() : null;
                if (ref.isEmpty()) ref = null;

                // Password signature
                JPasswordField pfPass = new JPasswordField();
                int opSign = JOptionPane.showConfirmDialog(this, new Object[]{"Ingrese contraseña de cajero para autorizar el abono:", pfPass}, "Firma Autorización", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
                if (opSign != JOptionPane.OK_OPTION) return;

                String pass = new String(pfPass.getPassword());
                int idUserFirma = new KardexDAO().validarFirmaUsuario(pass);

                if (idUserFirma <= 0) {
                    JOptionPane.showMessageDialog(this, "Contraseña incorrecta o usuario inactivo.", "Acceso Denegado", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                if (dao.registrarAbono(id, abono, idMetodo, idUserFirma, ref, banco)) {
                    JOptionPane.showMessageDialog(this, "Abono registrado con éxito.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                    cargarApartados();
                } else {
                    JOptionPane.showMessageDialog(this, "Error de base de datos al guardar abono.", "Error", JOptionPane.ERROR_MESSAGE);
                }

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Monto de abono inválido.", "Error", JOptionPane.ERROR_MESSAGE);
            }
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
            JOptionPane.showMessageDialog(this, "No se puede entregar la mercancía de un apartado con saldo pendiente (L " + String.format("%.2f", ap.getSaldoPendiente()) + ").", "Acceso Denegado", JOptionPane.ERROR_MESSAGE);
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

        int opt = JOptionPane.showConfirmDialog(this, "¿Confirmar que la mercancía ha sido entregada físicamente al cliente?", "Entregar Apartado", JOptionPane.YES_NO_OPTION);
        if (opt == JOptionPane.YES_OPTION) {
            if (dao.entregarApartado(id)) {
                JOptionPane.showMessageDialog(this, "Apartado completado y marcado como entregado.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                cargarApartados();
            } else {
                JOptionPane.showMessageDialog(this, "Error al actualizar estado en la base de datos.", "Error", JOptionPane.ERROR_MESSAGE);
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
        Apartado ap = dao.obtenerPorId(id);
        if (ap == null) return;

        List<DetalleApartado> detalles = dao.listarDetalles(id);
        List<AbonoApartado> abonos = dao.listarAbonos(id);

        Window parent = SwingUtilities.getWindowAncestor(this);
        JDialog dialog = new JDialog((Frame) parent, "Detalles del Apartado #" + id, true);
        dialog.setSize(600, 500); dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout(10, 10));

        // Cabecera info
        JPanel pnlInfo = new JPanel(new GridLayout(3, 2, 10, 10));
        pnlInfo.setBorder(BorderFactory.createEmptyBorder(15, 15, 10, 15));
        pnlInfo.add(new JLabel("Cliente: " + ap.getNombreCliente() + " " + (ap.getApellidoCliente() != null ? ap.getApellidoCliente() : "")));
        pnlInfo.add(new JLabel("Estado: " + ap.getEstadoApartado()));
        pnlInfo.add(new JLabel("Fecha Apartado: " + new SimpleDateFormat("dd/MM/yyyy HH:mm").format(ap.getFechaApartado())));
        pnlInfo.add(new JLabel("Fecha Límite: " + (ap.getFechaLimite() != null ? new SimpleDateFormat("dd/MM/yyyy").format(ap.getFechaLimite()) : "N/A")));
        pnlInfo.add(new JLabel("Total Apartado: L " + String.format("%.2f", ap.getTotalApartado())));
        pnlInfo.add(new JLabel("Saldo Pendiente: L " + String.format("%.2f", ap.getSaldoPendiente())));

        // Lista Productos & Abonos
        JTabbedPane tabs = new JTabbedPane();
        
        // Tab Productos
        String[] colP = {"Código", "Producto", "Cantidad", "Precio Unitario"};
        DefaultTableModel modP = new DefaultTableModel(null, colP) { @Override public boolean isCellEditable(int r, int c) { return false; } };
        JTable tabP = new JTable(modP); tabP.setRowHeight(25);
        for (DetalleApartado d : detalles) {
            modP.addRow(new Object[]{ d.getCodigoBarras(), d.getNombreProducto(), d.getCantidadApartado(), "L " + String.format("%.2f", d.getPrecioUnitarioApartado()) });
        }
        tabs.addTab("Productos Apartados", new JScrollPane(tabP));

        // Tab Abonos
        String[] colA = {"ID Abono", "Fecha Abono", "Monto", "Método", "Cajero"};
        DefaultTableModel modA = new DefaultTableModel(null, colA) { @Override public boolean isCellEditable(int r, int c) { return false; } };
        JTable tabA = new JTable(modA); tabA.setRowHeight(25);
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        for (AbonoApartado ab : abonos) {
            modA.addRow(new Object[]{ ab.getIdAbono(), sdf.format(ab.getFechaAbono()), "L " + String.format("%.2f", ab.getMontoAbono()), ab.getNombreMetodo(), ab.getNombreUsuario() });
        }
        tabs.addTab("Historial de Abonos", new JScrollPane(tabA));

        dialog.add(pnlInfo, BorderLayout.NORTH);
        dialog.add(tabs, BorderLayout.CENTER);

        JPanel pnlBot = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        JButton btnCerrar = new JButton("Cerrar"); btnCerrar.addActionListener(e -> dialog.dispose());
        pnlBot.add(btnCerrar);
        dialog.add(pnlBot, BorderLayout.SOUTH);

        dialog.setVisible(true);
    }
}
