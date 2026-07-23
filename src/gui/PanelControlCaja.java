package gui;

import dao.ControlCajaDAO;
import modelo.ControlCaja;
import utilidades.SesionGlobal;
import utilidades.Seguridad;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;

public class PanelControlCaja extends JPanel {
    private ControlCajaDAO dao;
    private ControlCaja activa;

    // Components active session
    private JPanel pnlActivo;
    private JLabel lblCajero;
    private JLabel lblFechaApertura;
    private JLabel lblMontoApertura;
    private JLabel lblEsperadoLabel;
    private JLabel lblEsperadoValor;
    private JLabel lblVentasValor;
    private JLabel lblAbonosValor;
    private JPanel pnlCalculosAdmin;
    private JTextField txtMontoReal;
    private JTextArea txtObservaciones;

    // Components no active session
    private JPanel pnlInactivo;
    private JButton btnAbrirCaja;

    // Components history
    private JPanel pnlHistorial;
    private JTable tablaHistorial;
    private DefaultTableModel modeloHistorial;

    public PanelControlCaja() {
        this.dao = new ControlCajaDAO();
        iniciarDiseno();
        verificarSesion();
    }

    private void iniciarDiseno() {
        this.setLayout(new BorderLayout(20, 20));
        this.setBackground(new Color(240, 242, 245)); // Gris Nube
        this.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        JLabel lblTitulo = new JLabel("Control de Caja y Arqueos");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitulo.setForeground(new Color(45, 45, 45));
        this.add(lblTitulo, BorderLayout.NORTH);

        // Contenedor Central
        JPanel pnlCentro = new JPanel(new CardLayout());
        pnlCentro.setOpaque(false);

        // 1. PANEL ACTIVO
        pnlActivo = new JPanel(new BorderLayout(20, 20));
        pnlActivo.setOpaque(false);

        JPanel pnlDetalleActivo = new JPanel(new GridBagLayout());
        pnlDetalleActivo.setBackground(Color.WHITE);
        pnlDetalleActivo.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 222, 225), 1, true),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        lblCajero = new JLabel("");
        lblFechaApertura = new JLabel("");
        lblMontoApertura = new JLabel("");

        // Column Left: General Data
        JPanel pnlDataIzq = new JPanel(new GridLayout(3, 1, 10, 10));
        pnlDataIzq.setOpaque(false);
        pnlDataIzq.add(lblCajero);
        pnlDataIzq.add(lblFechaApertura);
        pnlDataIzq.add(lblMontoApertura);

        // Column Right: Expected Calculations (Admin only)
        pnlCalculosAdmin = new JPanel(new GridLayout(3, 1, 10, 10));
        pnlCalculosAdmin.setOpaque(false);
        lblVentasValor = new JLabel("Ventas del Turno: L 0.00");
        lblAbonosValor = new JLabel("Abonos del Turno: L 0.00");
        lblEsperadoValor = new JLabel("Efectivo Esperado: L 0.00");
        lblEsperadoValor.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblEsperadoValor.setForeground(new Color(39, 174, 96)); // Verde Menta
        pnlCalculosAdmin.add(lblVentasValor);
        pnlCalculosAdmin.add(lblAbonosValor);
        pnlCalculosAdmin.add(lblEsperadoValor);

        // Column Right: Blind notification (Cashier only)
        lblEsperadoLabel = new JLabel("<html><body style='width: 250px;'><i style='color:#e67e22;'>Arqueo a ciegas habilitado.</i><br>Por políticas de auditoría, los montos de ventas y el saldo esperado no son visibles para el cajero durante el turno.</body></html>");
        lblEsperadoLabel.setFont(new Font("Segoe UI", Font.ITALIC, 13));
        lblEsperadoLabel.setForeground(new Color(127, 135, 143));

        gbc.gridx = 0; gbc.gridy = 0; pnlDetalleActivo.add(pnlDataIzq, gbc);
        gbc.gridx = 1; gbc.gridy = 0; pnlDetalleActivo.add(pnlCalculosAdmin, gbc);
        gbc.gridy = 1; pnlDetalleActivo.add(lblEsperadoLabel, gbc);

        // Cierre Form (Efectivo real)
        JPanel pnlFormCierre = new JPanel(new GridBagLayout());
        pnlFormCierre.setBackground(Color.WHITE);
        pnlFormCierre.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 222, 225), 1, true),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));
        GridBagConstraints gbcC = new GridBagConstraints();
        gbcC.insets = new Insets(8, 0, 8, 0); gbcC.fill = GridBagConstraints.HORIZONTAL; gbcC.gridx = 0; gbcC.weightx = 1.0;

        JLabel lblMontoReal = new JLabel("Efectivo Contado Real en Gaveta (L):");
        lblMontoReal.setFont(new Font("Segoe UI", Font.BOLD, 12));
        txtMontoReal = new JTextField();
        txtMontoReal.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtMontoReal.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(220, 222, 225)), BorderFactory.createEmptyBorder(5, 8, 5, 8)));

        JLabel lblObs = new JLabel("Observaciones / Comentarios:");
        lblObs.setFont(new Font("Segoe UI", Font.BOLD, 12));
        txtObservaciones = new JTextArea(3, 20);
        txtObservaciones.setLineWrap(true);
        txtObservaciones.setWrapStyleWord(true);
        txtObservaciones.setBorder(BorderFactory.createLineBorder(new Color(220, 222, 225)));

        JButton btnCerrarCaja = new JButton("Realizar Arqueo y Cierre");
        btnCerrarCaja.setBackground(new Color(227, 0, 15)); // Rojo Logo
        btnCerrarCaja.setForeground(Color.WHITE);
        btnCerrarCaja.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnCerrarCaja.setFocusPainted(false);
        btnCerrarCaja.setPreferredSize(new Dimension(0, 42));
        btnCerrarCaja.addActionListener(e -> realizarCierreCaja());

        pnlFormCierre.add(lblMontoReal, gbcC); gbcC.gridy = 1;
        pnlFormCierre.add(txtMontoReal, gbcC); gbcC.gridy = 2;
        pnlFormCierre.add(lblObs, gbcC); gbcC.gridy = 3;
        pnlFormCierre.add(new JScrollPane(txtObservaciones), gbcC); gbcC.gridy = 4;
        pnlFormCierre.add(btnCerrarCaja, gbcC);

        JPanel pnlActCentral = new JPanel(new GridLayout(1, 2, 20, 0));
        pnlActCentral.setOpaque(false);
        pnlActCentral.add(pnlDetalleActivo);
        pnlActCentral.add(pnlFormCierre);
        pnlActivo.add(pnlActCentral, BorderLayout.CENTER);

        // 2. PANEL INACTIVO (Warning / Abrir Caja)
        pnlInactivo = new JPanel(new GridBagLayout());
        pnlInactivo.setBackground(Color.WHITE);
        pnlInactivo.setBorder(BorderFactory.createLineBorder(new Color(220, 222, 225), 1, true));
        GridBagConstraints gbcI = new GridBagConstraints();
        gbcI.insets = new Insets(15, 15, 15, 15);
        gbcI.gridx = 0;

        JLabel lblIconWarn = new JLabel("⚠️");
        lblIconWarn.setFont(new Font("Segoe UI", Font.PLAIN, 48));
        
        JLabel lblInactMsg = new JLabel("No hay ningún turno de caja abierto actualmente.");
        lblInactMsg.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblInactMsg.setForeground(new Color(45, 45, 45));

        JLabel lblInactDesc = new JLabel("Debe realizar la apertura de caja para registrar el dinero en efectivo inicial e iniciar operaciones.");
        lblInactDesc.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblInactDesc.setForeground(new Color(140, 145, 150));

        btnAbrirCaja = new JButton("Abrir Turno de Caja");
        btnAbrirCaja.setBackground(new Color(39, 174, 96)); // Verde Menta
        btnAbrirCaja.setForeground(Color.WHITE);
        btnAbrirCaja.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnAbrirCaja.setFocusPainted(false);
        btnAbrirCaja.setPreferredSize(new Dimension(180, 40));
        btnAbrirCaja.addActionListener(e -> mostrarAperturaModal());

        pnlInactivo.add(lblIconWarn, gbcI); gbcI.gridy = 1;
        pnlInactivo.add(lblInactMsg, gbcI); gbcI.gridy = 2;
        pnlInactivo.add(lblInactDesc, gbcI); gbcI.gridy = 3;
        pnlInactivo.add(Box.createVerticalStrut(15), gbcI); gbcI.gridy = 4;
        pnlInactivo.add(btnAbrirCaja, gbcI);

        pnlCentro.add(pnlActivo, "ACTIVO");
        pnlCentro.add(pnlInactivo, "INACTIVO");
        this.add(pnlCentro, BorderLayout.CENTER);

        // 3. HISTORIAL DE ARQUEOS (Bottom)
        pnlHistorial = new JPanel(new BorderLayout(10, 10));
        pnlHistorial.setOpaque(false);
        pnlHistorial.setPreferredSize(new Dimension(0, 240));

        JPanel pnlHistHeader = new JPanel(new BorderLayout());
        pnlHistHeader.setOpaque(false);

        JLabel lblHist = new JLabel("Historial de Arqueos Cerrados");
        lblHist.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblHist.setForeground(new Color(45, 45, 45));
        pnlHistHeader.add(lblHist, BorderLayout.WEST);

        JButton btnReimprimirCierre = new JButton("🖨 Reimprimir Cierre");
        btnReimprimirCierre.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnReimprimirCierre.setBackground(new Color(41, 128, 185));
        btnReimprimirCierre.setForeground(Color.WHITE);
        btnReimprimirCierre.setFocusPainted(false);
        btnReimprimirCierre.addActionListener(e -> {
            int row = tablaHistorial.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "Seleccione un turno de caja del historial.", "Aviso", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int idCaja = (int) tablaHistorial.getValueAt(row, 0);
            Map<String, Object> calcs = dao.obtenerCalculosTurno(idCaja);
            if (calcs != null) {
                JFileChooser chooser = new JFileChooser();
                chooser.setDialogTitle("Reimprimir Ticket de Cierre #" + idCaja);
                chooser.setSelectedFile(new java.io.File("Reimpresion_Cierre_Caja_" + idCaja + "_" + System.currentTimeMillis() + ".pdf"));

                if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                    java.io.File archivoDestino = chooser.getSelectedFile();
                    if (!archivoDestino.getName().toLowerCase().endsWith(".pdf")) {
                        archivoDestino = new java.io.File(archivoDestino.getAbsolutePath() + ".pdf");
                    }
                    try {
                        utilidades.GeneradorTickets.generarTicketCierreCajaPDF(archivoDestino.getAbsolutePath(), calcs);
                        if (java.awt.Desktop.isDesktopSupported()) {
                            java.awt.Desktop.getDesktop().open(archivoDestino);
                        }
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(this, "Error al generar el PDF del cierre:\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });
        pnlHistHeader.add(btnReimprimirCierre, BorderLayout.EAST);
        pnlHistorial.add(pnlHistHeader, BorderLayout.NORTH);

        String[] cols = {"ID Turno", "Apertura", "Cierre", "Cajero", "Ef. Inicial", "Ef. Esperado", "Ef. Real", "Diferencia", "Estado"};
        modeloHistorial = new DefaultTableModel(null, cols) { @Override public boolean isCellEditable(int r, int c) { return false; } };
        tablaHistorial = new JTable(modeloHistorial);
        tablaHistorial.setRowHeight(30);
        tablaHistorial.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        JScrollPane scrollH = new JScrollPane(tablaHistorial);
        scrollH.setBorder(BorderFactory.createLineBorder(new Color(220, 222, 225)));
        scrollH.getViewport().setBackground(Color.WHITE);
        pnlHistorial.add(scrollH, BorderLayout.CENTER);

        this.add(pnlHistorial, BorderLayout.SOUTH);
    }

    private void verificarSesion() {
        activa = dao.obtenerSesionActiva();
        CardLayout cl = (CardLayout) pnlActivo.getParent().getLayout();
        
        int rolId = SesionGlobal.getUsuarioActual() != null ? SesionGlobal.getUsuarioActual().getIdRol() : 3;

        if (activa != null) {
            cl.show(pnlActivo.getParent(), "ACTIVO");
            lblCajero.setText("<html><strong>Cajero de Turno:</strong> " + (activa.getCajeroTurno() != null && !activa.getCajeroTurno().isEmpty() ? activa.getCajeroTurno() : activa.getNombreUsuarioApertura()) + "</html>");
            
            String fechaA = new SimpleDateFormat("dd/MM/yyyy HH:mm").format(activa.getFechaApertura());
            lblFechaApertura.setText("<html><strong>Fecha Apertura:</strong> " + fechaA + "</html>");
            lblMontoApertura.setText("<html><strong>Efectivo Inicial:</strong> L " + String.format("%.2f", activa.getMontoApertura()) + "</html>");

            // Configurar visibilidad según el rol
            if (rolId == 3) {
                pnlCalculosAdmin.setVisible(false);
                lblEsperadoLabel.setVisible(true);
                pnlHistorial.setVisible(false);
            } else {
                pnlCalculosAdmin.setVisible(true);
                lblEsperadoLabel.setVisible(false);
                pnlHistorial.setVisible(true);
                cargarCalculosEnVivo();
                cargarHistorial();
            }
        } else {
            cl.show(pnlActivo.getParent(), "ININ"); // Show inactive
            cl.show(pnlActivo.getParent(), "INACTIVO");
            pnlHistorial.setVisible(rolId != 3);
            if (rolId != 3) {
                cargarHistorial();
            }
        }
        this.revalidate();
        this.repaint();
    }

    private void cargarCalculosEnVivo() {
        if (activa == null) return;
        Map<String, Object> c = dao.obtenerCalculosTurno(activa.getIdCaja());
        if (c != null) {
            double vTot = (double) c.get("total_ventas_general");
            double aTot = (double) c.get("total_abonos_general");
            double esp = (double) c.get("efectivo_esperado");

            lblVentasValor.setText("Ventas del Turno: L " + String.format("%.2f", vTot));
            lblAbonosValor.setText("Abonos del Turno: L " + String.format("%.2f", aTot));
            lblEsperadoValor.setText("Efectivo Esperado: L " + String.format("%.2f", esp));
        }
    }

    private void cargarHistorial() {
        modeloHistorial.setRowCount(0);
        List<ControlCaja> lista = dao.listarHistoricos();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        for (ControlCaja c : lista) {
            String fApe = sdf.format(c.getFechaApertura());
            String fCie = c.getFechaCierre() != null ? sdf.format(c.getFechaCierre()) : "-";
            modeloHistorial.addRow(new Object[]{
                c.getIdCaja(),
                fApe,
                fCie,
                c.getCajeroTurno() != null && !c.getCajeroTurno().isEmpty() ? c.getCajeroTurno() : c.getNombreUsuarioApertura(),
                "L " + String.format("%.2f", c.getMontoApertura()),
                c.getFechaCierre() != null ? ("L " + String.format("%.2f", c.getMontoCierreEsperado())) : "-",
                c.getFechaCierre() != null ? ("L " + String.format("%.2f", c.getMontoCierreReal())) : "-",
                c.getFechaCierre() != null ? ("L " + String.format("%.2f", c.getDiferenciaCierre())) : "-",
                c.getEstadoCaja() == 1 ? "ABIERTA" : "CERRADA"
            });
        }
    }

    private void mostrarAperturaModal() {
        int rolId = SesionGlobal.getUsuarioActual() != null ? SesionGlobal.getUsuarioActual().getIdRol() : 3;
        String userActual = SesionGlobal.getUsuarioActual() != null ? SesionGlobal.getUsuarioActual().getNombreUsuario() : "";
        int idUserActual = SesionGlobal.getUsuarioActual() != null ? SesionGlobal.getUsuarioActual().getIdUsuario() : 1;

        JTextField txtMonto = new JTextField("0.00");
        JTextField txtCajero = new JTextField(userActual);
        
        Object[] msgElements;
        if (rolId == 1) {
            msgElements = new Object[]{
                "Monto de Apertura (Efectivo Inicial L):", txtMonto,
                "Nombre del Cajero de Turno:", txtCajero
            };
        } else {
            msgElements = new Object[]{
                "Monto de Apertura (Efectivo Inicial L):", txtMonto
            };
        }

        int opt = JOptionPane.showConfirmDialog(this, msgElements, "Apertura de Caja", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (opt == JOptionPane.OK_OPTION) {
            try {
                double monto = Double.parseDouble(txtMonto.getText().trim());
                if (monto < 0) { JOptionPane.showMessageDialog(this, "El monto no puede ser negativo.", "Error", JOptionPane.ERROR_MESSAGE); return; }
                
                String cajero = txtCajero.getText().trim();
                if (cajero.isEmpty()) { cajero = userActual; }

                if (dao.abrirCaja(idUserActual, monto, cajero)) {
                    JOptionPane.showMessageDialog(this, "Turno de caja abierto exitosamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                    verificarSesion();
                } else {
                    JOptionPane.showMessageDialog(this, "Error al abrir la sesión en la base de datos.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Monto inválido.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void realizarCierreCaja() {
        if (activa == null) return;
        try {
            double montoReal = Double.parseDouble(txtMontoReal.getText().trim());
            if (montoReal < 0) { JOptionPane.showMessageDialog(this, "El monto no puede ser negativo.", "Error", JOptionPane.ERROR_MESSAGE); return; }

            String obs = txtObservaciones.getText().trim();

            // Solicitar firma electrónica del cajero
            JPasswordField pfPass = new JPasswordField();
            int op = JOptionPane.showConfirmDialog(this, new Object[]{"Ingrese su contraseña para firmar y cerrar el turno:", pfPass}, "Firma de Cierre", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (op != JOptionPane.OK_OPTION) return;

            String pass = new String(pfPass.getPassword());
            int idUsuarioFirma = new dao.KardexDAO().validarFirmaUsuario(pass);

            if (idUsuarioFirma <= 0) {
                JOptionPane.showMessageDialog(this, "Contraseña incorrecta o usuario inactivo.", "Acceso Denegado", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (dao.cerrarCaja(activa.getIdCaja(), montoReal, obs, idUsuarioFirma)) {
                JOptionPane.showMessageDialog(this, "Caja cerrada y arqueo finalizado correctamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                
                // Generar e Imprimir Ticket de Cierre
                Map<String, Object> calcs = dao.obtenerCalculosTurno(activa.getIdCaja());
                if (calcs != null) {
                    JFileChooser chooser = new JFileChooser();
                    chooser.setDialogTitle("Guardar Ticket de Cierre de Caja");
                    chooser.setSelectedFile(new java.io.File("Cierre_Caja_" + activa.getIdCaja() + "_" + System.currentTimeMillis() + ".pdf"));

                    if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                        java.io.File archivoDestino = chooser.getSelectedFile();
                        if (!archivoDestino.getName().toLowerCase().endsWith(".pdf")) {
                            archivoDestino = new java.io.File(archivoDestino.getAbsolutePath() + ".pdf");
                        }
                        try {
                            utilidades.GeneradorTickets.generarTicketCierreCajaPDF(archivoDestino.getAbsolutePath(), calcs);
                            if (java.awt.Desktop.isDesktopSupported()) {
                                java.awt.Desktop.getDesktop().open(archivoDestino);
                            }
                        } catch (Exception ex) {
                            JOptionPane.showMessageDialog(this, "Error al generar el PDF del cierre:\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                    
                    mostrarReporteResumen(calcs);
                }

                txtMontoReal.setText("");
                txtObservaciones.setText("");
                verificarSesion();
            } else {
                JOptionPane.showMessageDialog(this, "Error al guardar el cierre de caja.", "Error", JOptionPane.ERROR_MESSAGE);
            }

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Ingrese un monto real válido.", "Aviso", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void mostrarReporteResumen(Map<String, Object> c) {
        StringBuilder sb = new StringBuilder();
        sb.append("=========================================\n");
        sb.append("         REPORTE DE ARQUEO DE CAJA       \n");
        sb.append("=========================================\n");
        sb.append("Cajero Asignado: ").append(c.get("cajero_turno")).append("\n");
        
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        sb.append("Apertura: ").append(sdf.format((Timestamp) c.get("fecha_apertura"))).append("\n");
        if (c.get("fecha_cierre") != null) {
            sb.append("Cierre: ").append(sdf.format((Timestamp) c.get("fecha_cierre"))).append("\n");
        }
        sb.append("-----------------------------------------\n");
        sb.append("Efectivo Inicial: L ").append(String.format("%.2f", c.get("monto_apertura"))).append("\n");
        sb.append("Ventas Totales: L ").append(String.format("%.2f", c.get("total_ventas_general"))).append("\n");
        sb.append("Abonos Recibidos: L ").append(String.format("%.2f", c.get("total_abonos_general"))).append("\n");
        sb.append("Efectivo Esperado: L ").append(String.format("%.2f", c.get("efectivo_esperado"))).append("\n");
        
        double real = 0;
        if (activa != null) {
            try { real = Double.parseDouble(txtMontoReal.getText().trim()); } catch(Exception e) {}
        }
        sb.append("Efectivo Real Contado: L ").append(String.format("%.2f", real)).append("\n");
        double esperado = (double) c.get("efectivo_esperado");
        sb.append("Diferencia (Sobrante/Faltante): L ").append(String.format("%.2f", real - esperado)).append("\n");
        sb.append("=========================================\n");
        
        List<Map<String, Object>> prods = (List<Map<String, Object>>) c.get("productos_vendidos");
        if (prods != null && !prods.isEmpty()) {
            sb.append("\nPRODUCTOS VENDIDOS EN EL TURNO:\n");
            for (Map<String, Object> p : prods) {
                sb.append(p.get("cantidad")).append("x ").append(p.get("descripcion")).append(" (L ").append(String.format("%.2f", p.get("total_valor"))).append(")\n");
            }
        }
        
        JTextArea area = new JTextArea(sb.toString());
        area.setFont(new Font("Monospaced", Font.PLAIN, 12));
        area.setEditable(false);
        JOptionPane.showMessageDialog(this, new JScrollPane(area), "Resumen Cierre de Caja", JOptionPane.INFORMATION_MESSAGE);
    }
}
