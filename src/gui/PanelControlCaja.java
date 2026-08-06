package gui;

import dao.ControlCajaDAO;
import modelo.ControlCaja;
import utilidades.SesionGlobal;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;

public class PanelControlCaja extends JPanel {
    private ControlCajaDAO dao;
    private ControlCaja activa;

    // Components active session panel (right side)
    private JLabel lblCajero;
    private JLabel lblFechaApertura;
    private JLabel lblMontoApertura;
    private JLabel lblVentasValor;
    private JLabel lblAbonosValor;
    private JLabel lblEsperadoValor;
    private JPanel pnlCalculosAdmin;
    private JLabel lblEsperadoLabel;
    private JTextField txtMontoReal;
    private JTextArea txtObservaciones;

    // Panel switching (right side)
    private JPanel pnlDerechaCards;
    private JPanel pnlActivo;
    private JPanel pnlInactivo;
    private JButton btnAbrirCaja;

    // History (left side)
    private JTable tablaHistorial;
    private DefaultTableModel modeloHistorial;
    private JPanel pnlHistorialWrapper;
    private TableRowSorter<DefaultTableModel> sorterHistorial;

    public PanelControlCaja() {
        this.dao = new ControlCajaDAO();
        iniciarDiseno();
        verificarSesion();
    }

    private void iniciarDiseno() {
        this.setLayout(new BorderLayout(0, 0));
        this.setBackground(utilidades.EfectosUI.COLOR_FONDO_PANEL);
        this.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        // ====== TÍTULO ======
        JPanel pnlTituloWrapper = new JPanel(new BorderLayout());
        pnlTituloWrapper.setOpaque(false);
        pnlTituloWrapper.setBorder(BorderFactory.createEmptyBorder(0, 0, 16, 0));

        JLabel lblTitulo = new JLabel("Control de Caja y Arqueos");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitulo.setForeground(utilidades.EfectosUI.COLOR_TEXTO_TITULO);
        pnlTituloWrapper.add(lblTitulo, BorderLayout.WEST);
        this.add(pnlTituloWrapper, BorderLayout.NORTH);

        // ====== CONTENIDO PRINCIPAL: GridBagLayout para igualar alturas ======
        JPanel pnlMain = new JPanel(new GridBagLayout());
        pnlMain.setOpaque(false);
        GridBagConstraints gbcMain = new GridBagConstraints();
        gbcMain.insets = new Insets(0, 0, 0, 0);

<<<<<<< HEAD
        // 1. PANEL ACTIVO
        pnlActivo = new JPanel(new BorderLayout(20, 20));
        pnlActivo.setOpaque(false);

        JPanel pnlDetalleActivo = new JPanel(new GridBagLayout());
        pnlDetalleActivo.setBackground(Color.WHITE);
        pnlDetalleActivo.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 222, 225), 1, true),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)));
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
        lblEsperadoLabel = new JLabel(
                "<html><body style='width: 250px;'><i style='color:#e67e22;'>Arqueo a ciegas habilitado.</i><br>Por políticas de auditoría, los montos de ventas y el saldo esperado no son visibles para el cajero durante el turno.</body></html>");
        lblEsperadoLabel.setFont(new Font("Segoe UI", Font.ITALIC, 13));
        lblEsperadoLabel.setForeground(new Color(127, 135, 143));

        gbc.gridx = 0;
        gbc.gridy = 0;
        pnlDetalleActivo.add(pnlDataIzq, gbc);
        gbc.gridx = 1;
        gbc.gridy = 0;
        pnlDetalleActivo.add(pnlCalculosAdmin, gbc);
        gbc.gridy = 1;
        pnlDetalleActivo.add(lblEsperadoLabel, gbc);

        // Cierre Form (Efectivo real)
        JPanel pnlFormCierre = new JPanel(new GridBagLayout());
        pnlFormCierre.setBackground(Color.WHITE);
        pnlFormCierre.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 222, 225), 1, true),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)));
        GridBagConstraints gbcC = new GridBagConstraints();
        gbcC.insets = new Insets(8, 0, 8, 0);
        gbcC.fill = GridBagConstraints.HORIZONTAL;
        gbcC.gridx = 0;
        gbcC.weightx = 1.0;

        JLabel lblMontoReal = new JLabel("Efectivo Contado Real en Gaveta (L):");
        lblMontoReal.setFont(new Font("Segoe UI", Font.BOLD, 12));
        txtMontoReal = new JTextField();
        txtMontoReal.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtMontoReal.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 222, 225)), BorderFactory.createEmptyBorder(5, 8, 5, 8)));

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

        pnlFormCierre.add(lblMontoReal, gbcC);
        gbcC.gridy = 1;
        pnlFormCierre.add(txtMontoReal, gbcC);
        gbcC.gridy = 2;
        pnlFormCierre.add(lblObs, gbcC);
        gbcC.gridy = 3;
        pnlFormCierre.add(new JScrollPane(txtObservaciones), gbcC);
        gbcC.gridy = 4;
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

        JLabel lblInactDesc = new JLabel(
                "Debe realizar la apertura de caja para registrar el dinero en efectivo inicial e iniciar operaciones.");
        lblInactDesc.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblInactDesc.setForeground(new Color(140, 145, 150));

        btnAbrirCaja = new JButton("Abrir Turno de Caja");
        btnAbrirCaja.setBackground(new Color(39, 174, 96)); // Verde Menta
        btnAbrirCaja.setForeground(Color.WHITE);
        btnAbrirCaja.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnAbrirCaja.setFocusPainted(false);
        btnAbrirCaja.setPreferredSize(new Dimension(180, 40));
        btnAbrirCaja.addActionListener(e -> mostrarAperturaModal());

        pnlInactivo.add(lblIconWarn, gbcI);
        gbcI.gridy = 1;
        pnlInactivo.add(lblInactMsg, gbcI);
        gbcI.gridy = 2;
        pnlInactivo.add(lblInactDesc, gbcI);
        gbcI.gridy = 3;
        pnlInactivo.add(Box.createVerticalStrut(15), gbcI);
        gbcI.gridy = 4;
        pnlInactivo.add(btnAbrirCaja, gbcI);

        pnlCentro.add(pnlActivo, "ACTIVO");
        pnlCentro.add(pnlInactivo, "INACTIVO");
        this.add(pnlCentro, BorderLayout.CENTER);

        // 3. HISTORIAL DE ARQUEOS (Bottom)
        pnlHistorial = new JPanel(new BorderLayout(10, 10));
        pnlHistorial.setOpaque(false);
        pnlHistorial.setPreferredSize(new Dimension(0, 240));
=======
        // ====== IZQUIERDA: HISTORIAL (mismo alto que panel derecho) ======
        pnlHistorialWrapper = new JPanel(new BorderLayout(0, 10));
        pnlHistorialWrapper.setOpaque(false);
>>>>>>> origin/parte-muoz

        JPanel pnlHistHeader = new JPanel(new BorderLayout());
        pnlHistHeader.setOpaque(false);
        pnlHistHeader.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));

        JLabel lblHist = new JLabel("Historial de Arqueos");
        lblHist.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblHist.setForeground(utilidades.EfectosUI.COLOR_TEXTO_TITULO);
        pnlHistHeader.add(lblHist, BorderLayout.WEST);
        pnlHistorialWrapper.add(pnlHistHeader, BorderLayout.NORTH);

        // --- Tabla ---
        String[] cols = {"ID", "Apertura", "Cierre", "Cajero", "Ef. Inicial", "Ef. Esperado", "Ef. Real", "Diferencia", "Estado"};
        modeloHistorial = new DefaultTableModel(null, cols) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tablaHistorial = new JTable(modeloHistorial);
        tablaHistorial.setRowHeight(30);
        tablaHistorial.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tablaHistorial.setBackground(Color.WHITE);
        tablaHistorial.setForeground(new Color(45, 45, 45));
        tablaHistorial.setSelectionBackground(new Color(205, 235, 218));
        tablaHistorial.setSelectionForeground(Color.BLACK);
        tablaHistorial.getTableHeader().setBackground(utilidades.EfectosUI.COLOR_SIDEBAR_PASTEL);
        tablaHistorial.getTableHeader().setForeground(utilidades.EfectosUI.COLOR_TEXTO_TITULO);
        tablaHistorial.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        tablaHistorial.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, utilidades.EfectosUI.COLOR_BORDE));
        tablaHistorial.getTableHeader().setPreferredSize(new Dimension(0, 34));
        tablaHistorial.setShowGrid(false);
        tablaHistorial.setIntercellSpacing(new Dimension(0, 2));

        sorterHistorial = new TableRowSorter<>(modeloHistorial);
        tablaHistorial.setRowSorter(sorterHistorial);

        JScrollPane scrollHist = new JScrollPane(tablaHistorial);
        scrollHist.setBorder(BorderFactory.createLineBorder(new Color(180, 208, 192)));
        scrollHist.getViewport().setBackground(Color.WHITE);
        pnlHistorialWrapper.add(scrollHist, BorderLayout.CENTER);

        // Historial: ocupa todo el espacio horizontal disponible, mismo alto que el derecho
        gbcMain.gridx = 0; gbcMain.gridy = 0;
        gbcMain.weightx = 1.0; gbcMain.weighty = 1.0;
        gbcMain.fill = GridBagConstraints.BOTH;
        gbcMain.insets = new Insets(0, 0, 0, 20);
        pnlMain.add(pnlHistorialWrapper, gbcMain);

        // ====== DERECHA: ESTADO DE CAJA (Card Layout: ACTIVO / INACTIVO) ======
        pnlDerechaCards = new JPanel(new CardLayout());
        pnlDerechaCards.setOpaque(false);
        pnlDerechaCards.setPreferredSize(new Dimension(340, 0));

        // --- Card ACTIVO: datos de la caja abierta ---
        pnlActivo = construirPanelActivo();
        pnlDerechaCards.add(pnlActivo, "ACTIVO");

        // --- Card INACTIVO: sin sesión ---
        pnlInactivo = construirPanelInactivo();
        pnlDerechaCards.add(pnlInactivo, "INACTIVO");

        // Derecha: ancho fijo, mismo alto que el izquierdo
        gbcMain.gridx = 1; gbcMain.gridy = 0;
        gbcMain.weightx = 0; gbcMain.weighty = 1.0;
        gbcMain.fill = GridBagConstraints.VERTICAL;
        gbcMain.insets = new Insets(0, 0, 0, 0);
        pnlMain.add(pnlDerechaCards, gbcMain);

        this.add(pnlMain, BorderLayout.CENTER);
    }

    private JPanel construirPanelActivo() {
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setOpaque(false);
        // Padding = título "Historial de Arqueos" (Font BOLD 16 ~28px) + borde inferior (8px) = ~36px
        // Alinea el inicio de la tarjeta con el inicio de la tabla
        panel.setBorder(BorderFactory.createEmptyBorder(36, 0, 0, 0));

        // ---- Tarjeta de datos ----
        JPanel tarjeta = new JPanel();
        tarjeta.setLayout(new BoxLayout(tarjeta, BoxLayout.Y_AXIS));
        tarjeta.setBackground(Color.WHITE);
        tarjeta.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(180, 208, 192), 1, true),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));

        JLabel lblTitTarjeta = new JLabel("Turno de Caja Activo");
        lblTitTarjeta.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTitTarjeta.setForeground(utilidades.EfectosUI.COLOR_TEXTO_TITULO);
        lblTitTarjeta.setAlignmentX(Component.LEFT_ALIGNMENT);

        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(180, 208, 192));
        sep.setAlignmentX(Component.LEFT_ALIGNMENT);
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));

        lblCajero = new JLabel("");
        lblCajero.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblCajero.setForeground(new Color(45, 45, 45));
        lblCajero.setAlignmentX(Component.LEFT_ALIGNMENT);

        lblFechaApertura = new JLabel("");
        lblFechaApertura.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblFechaApertura.setForeground(new Color(45, 45, 45));
        lblFechaApertura.setAlignmentX(Component.LEFT_ALIGNMENT);

        lblMontoApertura = new JLabel("");
        lblMontoApertura.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblMontoApertura.setForeground(new Color(45, 45, 45));
        lblMontoApertura.setAlignmentX(Component.LEFT_ALIGNMENT);

        JSeparator sep2 = new JSeparator();
        sep2.setForeground(new Color(180, 208, 192));
        sep2.setAlignmentX(Component.LEFT_ALIGNMENT);
        sep2.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));

        // Cálculos (Admin)
        pnlCalculosAdmin = new JPanel();
        pnlCalculosAdmin.setLayout(new BoxLayout(pnlCalculosAdmin, BoxLayout.Y_AXIS));
        pnlCalculosAdmin.setOpaque(false);
        pnlCalculosAdmin.setAlignmentX(Component.LEFT_ALIGNMENT);

        lblVentasValor = new JLabel("Ventas del Turno: L 0.00");
        lblVentasValor.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblVentasValor.setForeground(new Color(45, 45, 45));
        lblVentasValor.setAlignmentX(Component.LEFT_ALIGNMENT);

        lblAbonosValor = new JLabel("Abonos del Turno: L 0.00");
        lblAbonosValor.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblAbonosValor.setForeground(new Color(45, 45, 45));
        lblAbonosValor.setAlignmentX(Component.LEFT_ALIGNMENT);

        lblEsperadoValor = new JLabel("Efectivo Esperado: L 0.00");
        lblEsperadoValor.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblEsperadoValor.setForeground(utilidades.EfectosUI.COLOR_VERDE_PRIMARIO);
        lblEsperadoValor.setAlignmentX(Component.LEFT_ALIGNMENT);

        pnlCalculosAdmin.add(lblVentasValor);
        pnlCalculosAdmin.add(Box.createVerticalStrut(8));
        pnlCalculosAdmin.add(lblAbonosValor);
        pnlCalculosAdmin.add(Box.createVerticalStrut(8));
        pnlCalculosAdmin.add(lblEsperadoValor);

        // Mensaje cajero (blind)
        lblEsperadoLabel = new JLabel("<html><body style='width:260px'><i style='color:#e67e22'>Arqueo a ciegas habilitado.</i><br><span style='color:#7f8c8d; font-size:11'>Los montos no son visibles para el cajero durante el turno.</span></body></html>");
        lblEsperadoLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        tarjeta.add(lblTitTarjeta);
        tarjeta.add(Box.createVerticalStrut(10));
        tarjeta.add(sep);
        tarjeta.add(Box.createVerticalStrut(12));
        tarjeta.add(lblCajero);
        tarjeta.add(Box.createVerticalStrut(8));
        tarjeta.add(lblFechaApertura);
        tarjeta.add(Box.createVerticalStrut(8));
        tarjeta.add(lblMontoApertura);
        tarjeta.add(Box.createVerticalStrut(12));
        tarjeta.add(sep2);
        tarjeta.add(Box.createVerticalStrut(12));
        tarjeta.add(pnlCalculosAdmin);
        tarjeta.add(lblEsperadoLabel);

        panel.add(tarjeta, BorderLayout.CENTER);

        // ---- Botones: Reimprimir Cierre + Cerrar Caja (abajo del panel derecho) ----
        JPanel pnlBotones = new JPanel(new GridLayout(2, 1, 0, 8));
        pnlBotones.setOpaque(false);
        pnlBotones.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        JButton btnReimprimirDer = utilidades.EfectosUI.crearBotonVerde("Reimprimir Cierre");
        btnReimprimirDer.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btnReimprimirDer.setPreferredSize(new Dimension(0, 50));
        btnReimprimirDer.addActionListener(e -> {
            int row = tablaHistorial.getSelectedRow();
            if (row < 0) {
<<<<<<< HEAD
                JOptionPane.showMessageDialog(this, "Seleccione un turno de caja del historial.", "Aviso",
                        JOptionPane.WARNING_MESSAGE);
=======
                JOptionPane.showMessageDialog(panel, "Seleccione un turno del historial.", "Aviso", JOptionPane.WARNING_MESSAGE);
>>>>>>> origin/parte-muoz
                return;
            }
            int idCaja = (int) tablaHistorial.getValueAt(row, 0);
            Map<String, Object> calcs = dao.obtenerCalculosTurno(idCaja);
            if (calcs != null) {
                JFileChooser chooser = new JFileChooser();
<<<<<<< HEAD
                chooser.setDialogTitle("Reimprimir Ticket de Cierre #" + idCaja);
                chooser.setSelectedFile(new java.io.File(
                        "Reimpresion_Cierre_Caja_" + idCaja + "_" + System.currentTimeMillis() + ".pdf"));

                if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                    java.io.File archivoDestino = chooser.getSelectedFile();
                    if (!archivoDestino.getName().toLowerCase().endsWith(".pdf")) {
                        archivoDestino = new java.io.File(archivoDestino.getAbsolutePath() + ".pdf");
                    }
=======
                chooser.setDialogTitle("Reimprimir Cierre #" + idCaja);
                chooser.setSelectedFile(new java.io.File("Reimpresion_Cierre_Caja_" + idCaja + ".pdf"));
                if (chooser.showSaveDialog(panel) == JFileChooser.APPROVE_OPTION) {
                    java.io.File dest = chooser.getSelectedFile();
                    if (!dest.getName().toLowerCase().endsWith(".pdf"))
                        dest = new java.io.File(dest.getAbsolutePath() + ".pdf");
>>>>>>> origin/parte-muoz
                    try {
                        utilidades.GeneradorTickets.generarTicketCierreCajaPDF(dest.getAbsolutePath(), calcs);
                        if (java.awt.Desktop.isDesktopSupported()) java.awt.Desktop.getDesktop().open(dest);
                    } catch (Exception ex) {
<<<<<<< HEAD
                        JOptionPane.showMessageDialog(this, "Error al generar el PDF del cierre:\n" + ex.getMessage(),
                                "Error", JOptionPane.ERROR_MESSAGE);
=======
                        JOptionPane.showMessageDialog(panel, "Error al generar PDF: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
>>>>>>> origin/parte-muoz
                    }
                }
            }
        });

<<<<<<< HEAD
        String[] cols = { "ID Turno", "Apertura", "Cierre", "Cajero", "Ef. Inicial", "Ef. Esperado", "Ef. Real",
                "Diferencia", "Estado" };
        modeloHistorial = new DefaultTableModel(null, cols) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        tablaHistorial = new JTable(modeloHistorial);
        tablaHistorial.setRowHeight(30);
        tablaHistorial.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        JScrollPane scrollH = new JScrollPane(tablaHistorial);
        scrollH.setBorder(BorderFactory.createLineBorder(new Color(220, 222, 225)));
        scrollH.getViewport().setBackground(Color.WHITE);
        pnlHistorial.add(scrollH, BorderLayout.CENTER);
=======
        JButton btnCerrarCaja = utilidades.EfectosUI.crearBotonVerde("Cerrar Caja");
        btnCerrarCaja.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btnCerrarCaja.setPreferredSize(new Dimension(0, 50));
        btnCerrarCaja.addActionListener(e -> mostrarVentanaCierreCaja());
>>>>>>> origin/parte-muoz

        pnlBotones.add(btnReimprimirDer);
        pnlBotones.add(btnCerrarCaja);

        panel.add(pnlBotones, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel construirPanelInactivo() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createLineBorder(new Color(180, 208, 192), 1, true));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(12, 15, 12, 15);
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel lblIconWarn = new JLabel("!");
        lblIconWarn.setFont(new Font("Segoe UI", Font.BOLD, 48));
        lblIconWarn.setForeground(new Color(230, 126, 34));
        lblIconWarn.setHorizontalAlignment(SwingConstants.CENTER);

        JLabel lblMsg = new JLabel("<html><center>No hay ningún turno<br>de caja abierto.</center></html>");
        lblMsg.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblMsg.setForeground(new Color(45, 45, 45));
        lblMsg.setHorizontalAlignment(SwingConstants.CENTER);

        JLabel lblDesc = new JLabel("<html><center><span style='color:#8c9'>Abra un turno para comenzar a<br>registrar operaciones.</span></center></html>");
        lblDesc.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblDesc.setForeground(new Color(140, 145, 150));
        lblDesc.setHorizontalAlignment(SwingConstants.CENTER);

        btnAbrirCaja = utilidades.EfectosUI.crearBotonVerde("Abrir Turno de Caja");
        btnAbrirCaja.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btnAbrirCaja.setPreferredSize(new Dimension(220, 50));
        btnAbrirCaja.addActionListener(e -> mostrarAperturaModal());

        gbc.gridy = 0; panel.add(lblIconWarn, gbc);
        gbc.gridy = 1; panel.add(lblMsg, gbc);
        gbc.gridy = 2; panel.add(lblDesc, gbc);
        gbc.gridy = 3; panel.add(Box.createVerticalStrut(10), gbc);
        gbc.gridy = 4; panel.add(btnAbrirCaja, gbc);

        return panel;
    }

    private void mostrarVentanaCierreCaja() {
        if (activa == null) return;

        // Actualizar cálculos antes de abrir
        Map<String, Object> calcs = dao.obtenerCalculosTurno(activa.getIdCaja());

        Frame parentFrame = (Frame) SwingUtilities.getWindowAncestor(this);
        JDialog dlgCierre = new JDialog(parentFrame, "Arqueo y Cierre de Caja", true);
        dlgCierre.setSize(520, 540);
        dlgCierre.setLocationRelativeTo(parentFrame);
        dlgCierre.setLayout(new BorderLayout());
        dlgCierre.getContentPane().setBackground(utilidades.EfectosUI.COLOR_FONDO_PANEL);

        // ---- Cabecera ----
        JPanel pnlHead = new JPanel(new BorderLayout());
        pnlHead.setBackground(utilidades.EfectosUI.COLOR_SIDEBAR_PASTEL);
        pnlHead.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, utilidades.EfectosUI.COLOR_BORDE),
            BorderFactory.createEmptyBorder(15, 20, 15, 20)
        ));
        JLabel dlgTitulo = new JLabel("Realizar Arqueo y Cierre");
        dlgTitulo.setFont(new Font("Segoe UI", Font.BOLD, 20));
        dlgTitulo.setForeground(utilidades.EfectosUI.COLOR_TEXTO_TITULO);
        JLabel dlgSub = new JLabel("Complete los datos para cerrar el turno de caja.");
        dlgSub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        dlgSub.setForeground(utilidades.EfectosUI.COLOR_TEXTO_SUBTITULO);
        pnlHead.add(dlgTitulo, BorderLayout.NORTH);
        pnlHead.add(dlgSub, BorderLayout.SOUTH);
        dlgCierre.add(pnlHead, BorderLayout.NORTH);

        // ---- Cuerpo ----
        JPanel pnlCuerpo = new JPanel(new GridBagLayout());
        pnlCuerpo.setOpaque(false);
        pnlCuerpo.setBorder(BorderFactory.createEmptyBorder(20, 25, 10, 25));
        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.weightx = 1.0;
        gc.insets = new Insets(5, 0, 5, 0);
        gc.gridx = 0;

        // Resumen calculado
        if (calcs != null) {
            JPanel pnlResumen = new JPanel(new GridLayout(3, 1, 4, 4));
            pnlResumen.setOpaque(false);
            pnlResumen.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(180, 208, 192)),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)
            ));
            double vTot = (double) calcs.get("total_ventas_general");
            double aTot = (double) calcs.get("total_abonos_general");
            double esp = (double) calcs.get("efectivo_esperado");

            JLabel rVentas = new JLabel("Ventas del Turno: L " + String.format("%,.2f", vTot));
            rVentas.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            JLabel rAbonos = new JLabel("Abonos del Turno: L " + String.format("%,.2f", aTot));
            rAbonos.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            JLabel rEsperado = new JLabel("Efectivo Esperado: L " + String.format("%,.2f", esp));
            rEsperado.setFont(new Font("Segoe UI", Font.BOLD, 14));
            rEsperado.setForeground(utilidades.EfectosUI.COLOR_VERDE_PRIMARIO);

            pnlResumen.add(rVentas);
            pnlResumen.add(rAbonos);
            pnlResumen.add(rEsperado);

            gc.gridy = 0;
            pnlCuerpo.add(pnlResumen, gc);
        }

        // Campo monto real
        gc.gridy = 1;
        gc.insets = new Insets(14, 0, 4, 0);
        JLabel lblMontoReal = new JLabel("Efectivo Real Contado en Gaveta (L):");
        lblMontoReal.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblMontoReal.setForeground(utilidades.EfectosUI.COLOR_TEXTO_TITULO);
        pnlCuerpo.add(lblMontoReal, gc);

        gc.gridy = 2;
        gc.insets = new Insets(0, 0, 10, 0);
        txtMontoReal = new JTextField();
        txtMontoReal.setFont(new Font("Segoe UI", Font.BOLD, 18));
        txtMontoReal.setHorizontalAlignment(JTextField.RIGHT);
        txtMontoReal.setPreferredSize(new Dimension(0, 44));
        txtMontoReal.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(180, 208, 192)),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));
        pnlCuerpo.add(txtMontoReal, gc);

        // Observaciones
        gc.gridy = 3;
        gc.insets = new Insets(6, 0, 4, 0);
        JLabel lblObs = new JLabel("Observaciones / Comentarios:");
        lblObs.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblObs.setForeground(utilidades.EfectosUI.COLOR_TEXTO_TITULO);
        pnlCuerpo.add(lblObs, gc);

        gc.gridy = 4;
        gc.insets = new Insets(0, 0, 0, 0);
        gc.weighty = 1.0;
        gc.fill = GridBagConstraints.BOTH;
        txtObservaciones = new JTextArea(4, 20);
        txtObservaciones.setLineWrap(true);
        txtObservaciones.setWrapStyleWord(true);
        txtObservaciones.setBorder(BorderFactory.createLineBorder(new Color(180, 208, 192)));
        JScrollPane scrollObs = new JScrollPane(txtObservaciones);
        scrollObs.setBorder(null);
        pnlCuerpo.add(scrollObs, gc);

        dlgCierre.add(pnlCuerpo, BorderLayout.CENTER);

        // ---- Footer botones ----
        JPanel pnlFoot = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 12));
        pnlFoot.setOpaque(false);
        pnlFoot.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(180, 208, 192)));

        JButton btnCancelar = new JButton("Cancelar");
        btnCancelar.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btnCancelar.setBackground(new Color(140, 145, 150));
        btnCancelar.setForeground(Color.WHITE);
        btnCancelar.setFocusPainted(false);
        btnCancelar.setPreferredSize(new Dimension(130, 50));
        btnCancelar.addActionListener(e -> dlgCierre.dispose());

        JButton btnEjecutar = new JButton("Realizar Arqueo y Cierre");
        btnEjecutar.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btnEjecutar.setBackground(new Color(227, 0, 15));
        btnEjecutar.setForeground(Color.WHITE);
        btnEjecutar.setFocusPainted(false);
        btnEjecutar.setPreferredSize(new Dimension(240, 50));
        btnEjecutar.addActionListener(e -> {
            try {
                double montoReal = Double.parseDouble(txtMontoReal.getText().trim().replace(",", ""));
                if (montoReal < 0) {
                    JOptionPane.showMessageDialog(dlgCierre, "El monto no puede ser negativo.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                String obs = txtObservaciones.getText().trim();

                JPasswordField pfPass = new JPasswordField();
                int op = JOptionPane.showConfirmDialog(dlgCierre,
                    new Object[]{"Ingrese su contraseña para firmar y cerrar el turno:", pfPass},
                    "Firma de Cierre", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
                if (op != JOptionPane.OK_OPTION) return;

                String pass = new String(pfPass.getPassword());
                int idFirma = new dao.KardexDAO().validarFirmaUsuario(pass);
                if (idFirma <= 0) {
                    JOptionPane.showMessageDialog(dlgCierre, "Contraseña incorrecta o usuario inactivo.", "Acceso Denegado", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                if (dao.cerrarCaja(activa.getIdCaja(), montoReal, obs, idFirma)) {
                    JOptionPane.showMessageDialog(dlgCierre, "Caja cerrada y arqueo finalizado correctamente.", "Exito", JOptionPane.INFORMATION_MESSAGE);
                    Map<String, Object> calcsFinales = dao.obtenerCalculosTurno(activa.getIdCaja());
                    if (calcsFinales != null) {
                        JFileChooser chooser = new JFileChooser();
                        chooser.setDialogTitle("Guardar Ticket de Cierre");
                        chooser.setSelectedFile(new java.io.File("Cierre_Caja_" + activa.getIdCaja() + ".pdf"));
                        if (chooser.showSaveDialog(dlgCierre) == JFileChooser.APPROVE_OPTION) {
                            java.io.File dest = chooser.getSelectedFile();
                            if (!dest.getName().toLowerCase().endsWith(".pdf"))
                                dest = new java.io.File(dest.getAbsolutePath() + ".pdf");
                            try {
                                utilidades.GeneradorTickets.generarTicketCierreCajaPDF(dest.getAbsolutePath(), calcsFinales);
                                if (java.awt.Desktop.isDesktopSupported()) java.awt.Desktop.getDesktop().open(dest);
                            } catch (Exception ex2) {
                                JOptionPane.showMessageDialog(dlgCierre, "Error al generar PDF: " + ex2.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                            }
                        }
                        mostrarReporteResumen(calcsFinales);
                    }
                    dlgCierre.dispose();
                    verificarSesion();
                } else {
                    JOptionPane.showMessageDialog(dlgCierre, "Error al guardar el cierre.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dlgCierre, "Ingrese un monto real valido.", "Aviso", JOptionPane.WARNING_MESSAGE);
            }
        });

        pnlFoot.add(btnCancelar);
        pnlFoot.add(btnEjecutar);
        dlgCierre.add(pnlFoot, BorderLayout.SOUTH);

        dlgCierre.setVisible(true);
    }

    private void verificarSesion() {
        activa = dao.obtenerSesionActiva();
<<<<<<< HEAD
        CardLayout cl = (CardLayout) pnlActivo.getParent().getLayout();
=======
        CardLayout cl = (CardLayout) pnlDerechaCards.getLayout();
>>>>>>> origin/parte-muoz

        int rolId = SesionGlobal.getUsuarioActual() != null ? SesionGlobal.getUsuarioActual().getIdRol() : 3;

        if (activa != null) {
<<<<<<< HEAD
            cl.show(pnlActivo.getParent(), "ACTIVO");
            lblCajero.setText("<html><strong>Cajero de Turno:</strong> "
                    + (activa.getCajeroTurno() != null && !activa.getCajeroTurno().isEmpty() ? activa.getCajeroTurno()
                            : activa.getNombreUsuarioApertura())
                    + "</html>");

            String fechaA = new SimpleDateFormat("dd/MM/yyyy HH:mm").format(activa.getFechaApertura());
            lblFechaApertura.setText("<html><strong>Fecha Apertura:</strong> " + fechaA + "</html>");
            lblMontoApertura.setText("<html><strong>Efectivo Inicial:</strong> L "
                    + String.format("%,.2f", activa.getMontoApertura()) + "</html>");
=======
            cl.show(pnlDerechaCards, "ACTIVO");

            lblCajero.setText("<html><strong>Cajero:</strong> " +
                (activa.getCajeroTurno() != null && !activa.getCajeroTurno().isEmpty()
                    ? activa.getCajeroTurno() : activa.getNombreUsuarioApertura()) + "</html>");
            String fechaA = new SimpleDateFormat("dd/MM/yyyy HH:mm").format(activa.getFechaApertura());
            lblFechaApertura.setText("<html><strong>Apertura:</strong> " + fechaA + "</html>");
            lblMontoApertura.setText("<html><strong>Efectivo Inicial:</strong> L " + String.format("%,.2f", activa.getMontoApertura()) + "</html>");
>>>>>>> origin/parte-muoz

            if (rolId == 3) { // Cajero
                pnlCalculosAdmin.setVisible(false);
                lblEsperadoLabel.setVisible(true);
                pnlHistorialWrapper.setVisible(false);
            } else { // Admin
                pnlCalculosAdmin.setVisible(true);
                lblEsperadoLabel.setVisible(false);
                pnlHistorialWrapper.setVisible(true);
                cargarCalculosEnVivo();
                cargarHistorial();
            }
        } else {
            cl.show(pnlDerechaCards, "INACTIVO");
            pnlHistorialWrapper.setVisible(rolId != 3);
            if (rolId != 3) cargarHistorial();
        }

        this.revalidate();
        this.repaint();
    }

    private void cargarCalculosEnVivo() {
        if (activa == null)
            return;
        Map<String, Object> c = dao.obtenerCalculosTurno(activa.getIdCaja());
        if (c != null) {
            lblVentasValor.setText("Ventas del Turno: L " + String.format("%,.2f", (double) c.get("total_ventas_general")));
            lblAbonosValor.setText("Abonos del Turno: L " + String.format("%,.2f", (double) c.get("total_abonos_general")));
            lblEsperadoValor.setText("Efectivo Esperado: L " + String.format("%,.2f", (double) c.get("efectivo_esperado")));
        }
    }

    private void cargarHistorial() {
        modeloHistorial.setRowCount(0);
        List<ControlCaja> lista = dao.listarHistoricos();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        for (ControlCaja c : lista) {
<<<<<<< HEAD
            String fApe = sdf.format(c.getFechaApertura());
            String fCie = c.getFechaCierre() != null ? sdf.format(c.getFechaCierre()) : "-";
            modeloHistorial.addRow(new Object[] {
                    c.getIdCaja(),
                    fApe,
                    fCie,
                    c.getCajeroTurno() != null && !c.getCajeroTurno().isEmpty() ? c.getCajeroTurno()
                            : c.getNombreUsuarioApertura(),
                    "L " + String.format("%,.2f", c.getMontoApertura()),
                    c.getFechaCierre() != null ? ("L " + String.format("%,.2f", c.getMontoCierreEsperado())) : "-",
                    c.getFechaCierre() != null ? ("L " + String.format("%,.2f", c.getMontoCierreReal())) : "-",
                    c.getFechaCierre() != null ? ("L " + String.format("%,.2f", c.getDiferenciaCierre())) : "-",
                    c.getEstadoCaja() == 1 ? "ABIERTA" : "CERRADA"
=======
            modeloHistorial.addRow(new Object[]{
                c.getIdCaja(),
                sdf.format(c.getFechaApertura()),
                c.getFechaCierre() != null ? sdf.format(c.getFechaCierre()) : "-",
                c.getCajeroTurno() != null && !c.getCajeroTurno().isEmpty() ? c.getCajeroTurno() : c.getNombreUsuarioApertura(),
                "L " + String.format("%,.2f", c.getMontoApertura()),
                c.getFechaCierre() != null ? ("L " + String.format("%,.2f", c.getMontoCierreEsperado())) : "-",
                c.getFechaCierre() != null ? ("L " + String.format("%,.2f", c.getMontoCierreReal())) : "-",
                c.getFechaCierre() != null ? ("L " + String.format("%,.2f", c.getDiferenciaCierre())) : "-",
                c.getEstadoCaja() == 1 ? "ABIERTA" : "CERRADA"
>>>>>>> origin/parte-muoz
            });
        }
    }

    private void mostrarAperturaModal() {
        int rolId = SesionGlobal.getUsuarioActual() != null ? SesionGlobal.getUsuarioActual().getIdRol() : 3;
        String userActual = SesionGlobal.getUsuarioActual() != null ? SesionGlobal.getUsuarioActual().getNombreUsuario()
                : "";
        int idUserActual = SesionGlobal.getUsuarioActual() != null ? SesionGlobal.getUsuarioActual().getIdUsuario() : 1;

        JTextField txtMonto = new JTextField("0.00");
        JTextField txtCajero = new JTextField(userActual);

<<<<<<< HEAD
        Object[] msgElements;
        if (rolId == 1) {
            msgElements = new Object[] {
                    "Monto de Apertura (Efectivo Inicial L):", txtMonto,
                    "Nombre del Cajero de Turno:", txtCajero
            };
        } else {
            msgElements = new Object[] {
                    "Monto de Apertura (Efectivo Inicial L):", txtMonto
            };
        }
=======
        Object[] msgElements = rolId == 1
            ? new Object[]{"Monto de Apertura (L):", txtMonto, "Cajero de Turno:", txtCajero}
            : new Object[]{"Monto de Apertura (L):", txtMonto};
>>>>>>> origin/parte-muoz

        int opt = JOptionPane.showConfirmDialog(this, msgElements, "Apertura de Caja", JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);
        if (opt == JOptionPane.OK_OPTION) {
            try {
                double monto = Double.parseDouble(txtMonto.getText().trim());
<<<<<<< HEAD
                if (monto < 0) {
                    JOptionPane.showMessageDialog(this, "El monto no puede ser negativo.", "Error",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }

                String cajero = txtCajero.getText().trim();
                if (cajero.isEmpty()) {
                    cajero = userActual;
                }

                if (dao.abrirCaja(idUserActual, monto, cajero)) {
                    JOptionPane.showMessageDialog(this, "Turno de caja abierto exitosamente.", "Éxito",
                            JOptionPane.INFORMATION_MESSAGE);
                    verificarSesion();
                } else {
                    JOptionPane.showMessageDialog(this, "Error al abrir la sesión en la base de datos.", "Error",
                            JOptionPane.ERROR_MESSAGE);
=======
                if (monto < 0) { JOptionPane.showMessageDialog(this, "El monto no puede ser negativo.", "Error", JOptionPane.ERROR_MESSAGE); return; }
                String cajero = txtCajero.getText().trim();
                if (cajero.isEmpty()) cajero = userActual;
                if (dao.abrirCaja(idUserActual, monto, cajero)) {
                    JOptionPane.showMessageDialog(this, "Turno de caja abierto exitosamente.", "Exito", JOptionPane.INFORMATION_MESSAGE);
                    verificarSesion();
                } else {
                    JOptionPane.showMessageDialog(this, "Error al abrir la sesion.", "Error", JOptionPane.ERROR_MESSAGE);
>>>>>>> origin/parte-muoz
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Monto invalido.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

<<<<<<< HEAD
    private void realizarCierreCaja() {
        if (activa == null)
            return;
        try {
            double montoReal = Double.parseDouble(txtMontoReal.getText().trim());
            if (montoReal < 0) {
                JOptionPane.showMessageDialog(this, "El monto no puede ser negativo.", "Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            String obs = txtObservaciones.getText().trim();

            // Solicitar firma electrónica del cajero
            JPasswordField pfPass = new JPasswordField();
            int op = JOptionPane.showConfirmDialog(this,
                    new Object[] { "Ingrese su contraseña para firmar y cerrar el turno:", pfPass }, "Firma de Cierre",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (op != JOptionPane.OK_OPTION)
                return;

            String pass = new String(pfPass.getPassword());
            int idUsuarioFirma = new dao.KardexDAO().validarFirmaUsuario(pass);

            if (idUsuarioFirma <= 0) {
                JOptionPane.showMessageDialog(this, "Contraseña incorrecta o usuario inactivo.", "Acceso Denegado",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (dao.cerrarCaja(activa.getIdCaja(), montoReal, obs, idUsuarioFirma)) {
                JOptionPane.showMessageDialog(this, "Caja cerrada y arqueo finalizado correctamente.", "Éxito",
                        JOptionPane.INFORMATION_MESSAGE);

                // Generar e Imprimir Ticket de Cierre
                Map<String, Object> calcs = dao.obtenerCalculosTurno(activa.getIdCaja());
                if (calcs != null) {
                    JFileChooser chooser = new JFileChooser();
                    chooser.setDialogTitle("Guardar Ticket de Cierre de Caja");
                    chooser.setSelectedFile(new java.io.File(
                            "Cierre_Caja_" + activa.getIdCaja() + "_" + System.currentTimeMillis() + ".pdf"));

                    if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                        java.io.File archivoDestino = chooser.getSelectedFile();
                        if (!archivoDestino.getName().toLowerCase().endsWith(".pdf")) {
                            archivoDestino = new java.io.File(archivoDestino.getAbsolutePath() + ".pdf");
                        }
                        try {
                            utilidades.GeneradorTickets.generarTicketCierreCajaPDF(archivoDestino.getAbsolutePath(),
                                    calcs);
                            if (java.awt.Desktop.isDesktopSupported()) {
                                java.awt.Desktop.getDesktop().open(archivoDestino);
                            }
                        } catch (Exception ex) {
                            JOptionPane.showMessageDialog(this,
                                    "Error al generar el PDF del cierre:\n" + ex.getMessage(), "Error",
                                    JOptionPane.ERROR_MESSAGE);
                        }
                    }

                    mostrarReporteResumen(calcs);
                }

                txtMontoReal.setText("");
                txtObservaciones.setText("");
                verificarSesion();
            } else {
                JOptionPane.showMessageDialog(this, "Error al guardar el cierre de caja.", "Error",
                        JOptionPane.ERROR_MESSAGE);
            }

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Ingrese un monto real válido.", "Aviso", JOptionPane.WARNING_MESSAGE);
        }
    }

=======
>>>>>>> origin/parte-muoz
    private void mostrarReporteResumen(Map<String, Object> c) {
        StringBuilder sb = new StringBuilder();
        sb.append("=========================================\n");
        sb.append("         REPORTE DE ARQUEO DE CAJA       \n");
        sb.append("=========================================\n");
<<<<<<< HEAD
        sb.append("Cajero Asignado: ").append(c.get("cajero_turno")).append("\n");

=======
        sb.append("Cajero: ").append(c.get("cajero_turno")).append("\n");
>>>>>>> origin/parte-muoz
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        sb.append("Apertura: ").append(sdf.format((Timestamp) c.get("fecha_apertura"))).append("\n");
        if (c.get("fecha_cierre") != null)
            sb.append("Cierre: ").append(sdf.format((Timestamp) c.get("fecha_cierre"))).append("\n");
        sb.append("-----------------------------------------\n");
        sb.append("Efectivo Inicial: L ").append(String.format("%,.2f", c.get("monto_apertura"))).append("\n");
        sb.append("Ventas Totales:   L ").append(String.format("%,.2f", c.get("total_ventas_general"))).append("\n");
        sb.append("Abonos Recibidos: L ").append(String.format("%,.2f", c.get("total_abonos_general"))).append("\n");
        sb.append("Efectivo Esperado: L ").append(String.format("%,.2f", c.get("efectivo_esperado"))).append("\n");
<<<<<<< HEAD

        double real = 0;
        if (activa != null) {
            try {
                real = Double.parseDouble(txtMontoReal.getText().trim());
            } catch (Exception e) {
            }
        }
=======
        double esp = (double) c.get("efectivo_esperado");
        double real = 0;
        try { real = Double.parseDouble(txtMontoReal.getText().trim().replace(",", "")); } catch (Exception ignored) {}
>>>>>>> origin/parte-muoz
        sb.append("Efectivo Real Contado: L ").append(String.format("%,.2f", real)).append("\n");
        sb.append("Diferencia: L ").append(String.format("%,.2f", real - esp)).append("\n");
        sb.append("=========================================\n");
<<<<<<< HEAD

        List<Map<String, Object>> prods = (List<Map<String, Object>>) c.get("productos_vendidos");
        if (prods != null && !prods.isEmpty()) {
            sb.append("\nPRODUCTOS VENDIDOS EN EL TURNO:\n");
            for (Map<String, Object> p : prods) {
                sb.append(p.get("cantidad")).append("x ").append(p.get("descripcion")).append(" (L ")
                        .append(String.format("%,.2f", p.get("total_valor"))).append(")\n");
            }
        }

=======
        List<Map<String, Object>> prods = (List<Map<String, Object>>) c.get("productos_vendidos");
        if (prods != null && !prods.isEmpty()) {
            sb.append("\nPRODUCTOS VENDIDOS EN EL TURNO:\n");
            for (Map<String, Object> p : prods)
                sb.append(p.get("cantidad")).append("x ").append(p.get("descripcion")).append(" (L ").append(String.format("%,.2f", p.get("total_valor"))).append(")\n");
        }
>>>>>>> origin/parte-muoz
        JTextArea area = new JTextArea(sb.toString());
        area.setFont(new Font("Monospaced", Font.PLAIN, 12));
        area.setEditable(false);
        JOptionPane.showMessageDialog(this, new JScrollPane(area), "Resumen Cierre de Caja",
                JOptionPane.INFORMATION_MESSAGE);
    }
}
