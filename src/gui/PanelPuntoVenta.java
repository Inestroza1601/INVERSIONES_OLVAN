package gui;

import dao.ClienteDAO;
import dao.InventarioDAO;
import dao.VentasDAO;
import dao.ApartadoDAO;
import modelo.Cliente;
import modelo.Producto;
import modelo.Apartado;
import modelo.DetalleApartado;


import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.io.File;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class PanelPuntoVenta extends JPanel {

    private JLabel lblClienteSeleccionado;
    private int idClienteActual = 1; 

    // --- MEMORIA ESTÁTICA ---
    public static List<Object[]> memoriaFilasVenta = new ArrayList<>();
    public static int memoriaIdCliente = 1;
    public static String memoriaNombreCliente = "CONSUMIDOR FINAL";
    public static int memoriaTipoTransaccion = 0; 
    public static boolean memoriaMayoristaActivo = false;
    public static boolean memoriaMayoristaGlobal = true;
    // ------------------------
    
    // Cach\u00E9 y estado para Lazy Loading con Shimmer
    private java.util.Map<Integer, ImageIcon> cacheImagenes = new java.util.concurrent.ConcurrentHashMap<>();
    private java.util.Set<Integer> imagenesEnProceso = java.util.Collections.synchronizedSet(new java.util.HashSet<>());
    private float animacionShimmerPhase = 0f;
    private Timer timerShimmer;
    
    private JTextField txtCodigoBarrasBusqueda;
    private JTable tablaVentas;
    private DefaultTableModel modeloTablaVentas;
    
    private JComboBox<ItemPago> cmbMetodoPago;
    private JComboBox<String> cmbTipoTransaccion;
    private JLabel lblSubtotal;
    private JLabel lblImpuesto;
    private JLabel lblTotal;
    
    private double sumSubtotal = 0.0;
    private double sumImpuesto = 0.0;
    private double granTotal = 0.0;
    private boolean facturacionHabilitada = false;
    
    private JPanel pnlCamposExtraPago;
    private JLabel lblReferenciaPago;
    private JTextField txtReferenciaPago;
    private JComboBox<String> cmbBanco;

    public PanelPuntoVenta() {
        if (utilidades.SesionGlobal.getEmpresaActual() != null) {
            facturacionHabilitada = new dao.VentasDAO().empresaTieneFacturacionHabilitada(utilidades.SesionGlobal.getEmpresaActual().getIdEmpresa());
        }
        iniciarDiseno();
        cargarMetodosPago();
        configurarAtajosTeclado();
        cargarMemoria();
    }

    private void cargarMemoria() {
        for (Object[] row : memoriaFilasVenta) {
            modeloTablaVentas.addRow(row);
        }
        cmbTipoTransaccion.setSelectedIndex(memoriaTipoTransaccion);
        recalcularTotales();
    }

    private void sincronizarMemoria() {
        memoriaFilasVenta.clear();
        for (int i = 0; i < modeloTablaVentas.getRowCount(); i++) {
            Object[] row = new Object[modeloTablaVentas.getColumnCount()];
            for (int c = 0; c < row.length; c++) row[c] = modeloTablaVentas.getValueAt(i, c);
            memoriaFilasVenta.add(row);
        }
        memoriaIdCliente = idClienteActual;
        memoriaNombreCliente = lblClienteSeleccionado.getText();
        if (cmbTipoTransaccion != null) {
            memoriaTipoTransaccion = cmbTipoTransaccion.getSelectedIndex();
        }
    }

    private void aplicarLogicaMayorista() {
        if (!memoriaMayoristaActivo) {
            // Revert all to normal price
            for (int i = 0; i < modeloTablaVentas.getRowCount(); i++) {
                double precioNormal = (double) modeloTablaVentas.getValueAt(i, 11);
                int cant = (int) modeloTablaVentas.getValueAt(i, 3);
                modeloTablaVentas.setValueAt(precioNormal, i, 4);
                modeloTablaVentas.setValueAt(precioNormal * cant, i, 5);
            }
        } else {
            if (memoriaMayoristaGlobal) {
                // Apply wholesale to all
                for (int i = 0; i < modeloTablaVentas.getRowCount(); i++) {
                    double precioMayorista = (double) modeloTablaVentas.getValueAt(i, 12);
                    if (precioMayorista > 0) {
                        int cant = (int) modeloTablaVentas.getValueAt(i, 3);
                        modeloTablaVentas.setValueAt(precioMayorista, i, 4);
                        modeloTablaVentas.setValueAt(precioMayorista * cant, i, 5);
                    }
                }
            } else {
                // Unitario: revert to normal, user will select manually
                for (int i = 0; i < modeloTablaVentas.getRowCount(); i++) {
                    double precioNormal = (double) modeloTablaVentas.getValueAt(i, 11);
                    int cant = (int) modeloTablaVentas.getValueAt(i, 3);
                    modeloTablaVentas.setValueAt(precioNormal, i, 4);
                    modeloTablaVentas.setValueAt(precioNormal * cant, i, 5);
                }
            }
        }
        recalcularTotales();
    }

    private void iniciarDiseno() {
        this.setLayout(new BorderLayout(20, 20));
        this.setBackground(utilidades.EfectosUI.COLOR_FONDO_PANEL); // Verde Vintage
        this.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        JPanel pnlTop = new JPanel(new BorderLayout(15, 0)); pnlTop.setOpaque(false);
        JPanel pnlTopIzquierdo = new JPanel(new GridLayout(2, 1, 0, 5)); pnlTopIzquierdo.setOpaque(false);
        JPanel pnlClientes = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0)); pnlClientes.setOpaque(false);
        JLabel lblClie = new JLabel("Cliente:"); lblClie.setForeground(utilidades.EfectosUI.COLOR_TEXTO_SUBTITULO); lblClie.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblClienteSeleccionado = new JLabel(memoriaNombreCliente); lblClienteSeleccionado.setForeground(utilidades.EfectosUI.COLOR_TEXTO_TITULO); lblClienteSeleccionado.setFont(new Font("Segoe UI", Font.BOLD, 14));
        idClienteActual = memoriaIdCliente;
        
        JButton btnBuscarCliente = new JButton("Buscar"); btnBuscarCliente.setBackground(new Color(255, 255, 255)); btnBuscarCliente.setForeground(new Color(30, 41, 59)); btnBuscarCliente.setFocusPainted(false); btnBuscarCliente.setCursor(new Cursor(Cursor.HAND_CURSOR)); btnBuscarCliente.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(220, 222, 225)), BorderFactory.createEmptyBorder(6, 14, 6, 14)));
        utilidades.EfectosUI.aplicarEfectoHover(btnBuscarCliente, Color.WHITE, utilidades.EfectosUI.COLOR_VERDE_CLARO, new Color(30, 41, 59), Color.BLACK);
        btnBuscarCliente.addActionListener(e -> new DialogoBuscarClientePOS((Frame) SwingUtilities.getWindowAncestor(this)).setVisible(true));
        
        JButton btnNuevoCliente = new JButton("+ Nuevo"); btnNuevoCliente.setBackground(utilidades.EfectosUI.COLOR_VERDE_PRIMARIO); btnNuevoCliente.setForeground(Color.WHITE); btnNuevoCliente.setFont(new Font("Segoe UI", Font.BOLD, 13)); btnNuevoCliente.setFocusPainted(false); btnNuevoCliente.setCursor(new Cursor(Cursor.HAND_CURSOR)); btnNuevoCliente.setBorder(BorderFactory.createEmptyBorder(6, 14, 6, 14));
        utilidades.EfectosUI.aplicarEfectoHover(btnNuevoCliente, utilidades.EfectosUI.COLOR_VERDE_PRIMARIO, utilidades.EfectosUI.COLOR_VERDE_HOVER, Color.WHITE, Color.WHITE);
        btnNuevoCliente.addActionListener(e -> {
            JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Nuevo Cliente", true);
            dialog.setUndecorated(true); dialog.setBackground(new Color(0,0,0,0));
            dialog.add(new PanelFormularioCliente(dialog, new PanelGestionClientes(), null));
            dialog.pack(); dialog.setLocationRelativeTo(this); dialog.setVisible(true);
        });

        pnlClientes.add(lblClie); pnlClientes.add(lblClienteSeleccionado); pnlClientes.add(btnBuscarCliente); pnlClientes.add(btnNuevoCliente);

        JPanel pnlLector = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0)); pnlLector.setOpaque(false);
        txtCodigoBarrasBusqueda = new JTextField(); 
        txtCodigoBarrasBusqueda.putClientProperty("JTextField.placeholderText", "Escanear c\u00F3digo de barras o escribir c\u00F3digo..."); 
        txtCodigoBarrasBusqueda.putClientProperty("JTextField.showClearButton", true);
        txtCodigoBarrasBusqueda.setFont(new Font("Segoe UI", Font.PLAIN, 14)); 
        txtCodigoBarrasBusqueda.setPreferredSize(new Dimension(350, 38)); 
        txtCodigoBarrasBusqueda.setBackground(new Color(255, 255, 255)); 
        txtCodigoBarrasBusqueda.setForeground(new Color(45, 45, 45)); 
        txtCodigoBarrasBusqueda.setCaretColor(new Color(45, 45, 45)); 
        txtCodigoBarrasBusqueda.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 220, 210), 1), 
            BorderFactory.createEmptyBorder(0, 12, 0, 12)
        ));
        txtCodigoBarrasBusqueda.addActionListener(e -> buscarProductoPorCodigo(txtCodigoBarrasBusqueda.getText().trim()));
        
        JButton btnBuscarProducto = new JButton("Catálogo");
        btnBuscarProducto.setBackground(new Color(39, 174, 96));
        btnBuscarProducto.setForeground(Color.WHITE);
        btnBuscarProducto.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnBuscarProducto.setPreferredSize(new Dimension(110, 38));
        btnBuscarProducto.setFocusPainted(false);
        btnBuscarProducto.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnBuscarProducto.setBorder(BorderFactory.createEmptyBorder());
        btnBuscarProducto.putClientProperty("JButton.buttonType", "roundRect");
        utilidades.EfectosUI.aplicarEfectoHover(btnBuscarProducto, new Color(39, 174, 96), new Color(34, 153, 84), Color.WHITE, Color.WHITE);
        
        btnBuscarProducto.addActionListener(e -> {
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            btnBuscarProducto.setEnabled(false);
            SwingWorker<DialogoBuscarProductoPOS, Void> worker = new SwingWorker<DialogoBuscarProductoPOS, Void>() {
                @Override
                protected DialogoBuscarProductoPOS doInBackground() throws Exception {
                    return new DialogoBuscarProductoPOS((Frame) SwingUtilities.getWindowAncestor(PanelPuntoVenta.this));
                }
                @Override
                protected void done() {
                    setCursor(Cursor.getDefaultCursor());
                    btnBuscarProducto.setEnabled(true);
                    try {
                        get().setVisible(true);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        utilidades.Mensajes.showMessageDialog(PanelPuntoVenta.this, "Error al cargar el catálogo.");
                    }
                }
            };
            worker.execute();
        });

        pnlLector.add(txtCodigoBarrasBusqueda);
        pnlLector.add(Box.createHorizontalStrut(10));
        pnlLector.add(btnBuscarProducto);
        
        JPanel pnlMayorista = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0)); pnlMayorista.setOpaque(false);
        JToggleButton btnToggleMayorista = new JToggleButton("Aplicar Precio Mayorista");
        btnToggleMayorista.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnToggleMayorista.setBackground(Color.WHITE);
        btnToggleMayorista.setFocusPainted(false);
        
        JRadioButton rbTodos = new JRadioButton("Todos"); rbTodos.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        JRadioButton rbUnitario = new JRadioButton("Unitario"); rbUnitario.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        rbTodos.setOpaque(false); rbUnitario.setOpaque(false);
        ButtonGroup bgMayorista = new ButtonGroup();
        bgMayorista.add(rbTodos); bgMayorista.add(rbUnitario);
        
        rbTodos.setSelected(memoriaMayoristaGlobal);
        rbUnitario.setSelected(!memoriaMayoristaGlobal);
        btnToggleMayorista.setSelected(memoriaMayoristaActivo);
        rbTodos.setVisible(memoriaMayoristaActivo);
        rbUnitario.setVisible(memoriaMayoristaActivo);
        
        btnToggleMayorista.addActionListener(e -> {
            boolean activo = btnToggleMayorista.isSelected();
            rbTodos.setVisible(activo);
            rbUnitario.setVisible(activo);
            memoriaMayoristaActivo = activo;
            aplicarLogicaMayorista();
        });
        
        rbTodos.addActionListener(e -> { memoriaMayoristaGlobal = true; aplicarLogicaMayorista(); });
        rbUnitario.addActionListener(e -> { memoriaMayoristaGlobal = false; aplicarLogicaMayorista(); });

        pnlMayorista.add(btnToggleMayorista);
        pnlMayorista.add(rbTodos);
        pnlMayorista.add(rbUnitario);
        
        pnlTopIzquierdo.add(pnlClientes);
        pnlTopIzquierdo.add(pnlMayorista);
        
        pnlTop.add(pnlTopIzquierdo, BorderLayout.WEST); pnlTop.add(pnlLector, BorderLayout.EAST);
        this.add(pnlTop, BorderLayout.NORTH);

        String[] columnas = {"ID", "Foto", "Nombre del Producto", "Cant.", "Precio Unit.", "Subtotal Fila", "StockMax", "RutaFoto", "IMEI", "DiasGarantia", "IncluyeImpuesto", "PrecioNormal", "PrecioMayorista"};
        modeloTablaVentas = new DefaultTableModel(null, columnas) { @Override public boolean isCellEditable(int row, int column) { return false; } };
        tablaVentas = new JTable(modeloTablaVentas); 
        tablaVentas.setShowGrid(false); tablaVentas.setIntercellSpacing(new Dimension(0, 0)); tablaVentas.setRowHeight(60); tablaVentas.setBackground(new Color(255, 255, 255)); tablaVentas.setForeground(new Color(30, 41, 59)); tablaVentas.setFont(new Font("Segoe UI", Font.PLAIN, 14)); tablaVentas.setSelectionBackground(new Color(213, 233, 222)); tablaVentas.setSelectionForeground(new Color(19, 58, 42));
        tablaVentas.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14)); tablaVentas.getTableHeader().setBackground(new Color(230, 242, 235)); tablaVentas.getTableHeader().setForeground(new Color(19, 58, 42)); tablaVentas.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, utilidades.EfectosUI.COLOR_VERDE_PRIMARIO));
        tablaVentas.getTableHeader().setPreferredSize(new Dimension(tablaVentas.getTableHeader().getPreferredSize().width, 42));
        
        tablaVentas.getColumnModel().getColumn(0).setMinWidth(0); tablaVentas.getColumnModel().getColumn(0).setMaxWidth(0); 
        tablaVentas.getColumnModel().getColumn(6).setMinWidth(0); tablaVentas.getColumnModel().getColumn(6).setMaxWidth(0); 
        tablaVentas.getColumnModel().getColumn(7).setMinWidth(0); tablaVentas.getColumnModel().getColumn(7).setMaxWidth(0); 
        tablaVentas.getColumnModel().getColumn(8).setMinWidth(0); tablaVentas.getColumnModel().getColumn(8).setMaxWidth(0);
        tablaVentas.getColumnModel().getColumn(9).setMinWidth(0); tablaVentas.getColumnModel().getColumn(9).setMaxWidth(0);
        tablaVentas.getColumnModel().getColumn(10).setMinWidth(0); tablaVentas.getColumnModel().getColumn(10).setMaxWidth(0);
        tablaVentas.getColumnModel().getColumn(11).setMinWidth(0); tablaVentas.getColumnModel().getColumn(11).setMaxWidth(0);
        tablaVentas.getColumnModel().getColumn(12).setMinWidth(0); tablaVentas.getColumnModel().getColumn(12).setMaxWidth(0);
        
        tablaVentas.getColumnModel().getColumn(1).setPreferredWidth(70); tablaVentas.getColumnModel().getColumn(1).setMaxWidth(70);
        tablaVentas.getColumnModel().getColumn(1).setCellRenderer(new ImagenMiniaturaRenderer());
        
        tablaVentas.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            @Override public void mouseMoved(java.awt.event.MouseEvent e) {
                if (tablaVentas.columnAtPoint(e.getPoint()) == 1) tablaVentas.setCursor(new Cursor(Cursor.HAND_CURSOR));
                else tablaVentas.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            }
        });
        
        tablaVentas.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                int f = tablaVentas.rowAtPoint(e.getPoint());
                int c = tablaVentas.columnAtPoint(e.getPoint());
                
                if (f >= 0) {
                    tablaVentas.setRowSelectionInterval(f, f);

                    if (c == 1 && SwingUtilities.isLeftMouseButton(e)) {
                        int idSelec = (int) modeloTablaVentas.getValueAt(tablaVentas.convertRowIndexToModel(f), 0);
                        mostrarZoomImagen(idSelec);
                    }
                    if ((SwingUtilities.isLeftMouseButton(e) && e.getClickCount() == 2 && c != 1) || SwingUtilities.isRightMouseButton(e)) {
                        mostrarMenuOpciones(e.getComponent(), e.getX(), e.getY());
                    }
                }
            }
        });

        JScrollPane scrollTabla = new JScrollPane(tablaVentas); 
        scrollTabla.setBorder(BorderFactory.createLineBorder(new Color(220, 222, 225), 1, true)); 
        scrollTabla.getViewport().setBackground(new Color(255, 255, 255));
        this.add(scrollTabla, BorderLayout.CENTER);
        
        timerShimmer = new Timer(30, e -> {
            if (!imagenesEnProceso.isEmpty()) {
                animacionShimmerPhase += 0.05f;
                if (animacionShimmerPhase > 1f) animacionShimmerPhase = 0f;
                tablaVentas.repaint();
            }
        });
        timerShimmer.start();

        JPanel pnlControlVenta = new JPanel(new BorderLayout(20, 20)); pnlControlVenta.setOpaque(false); pnlControlVenta.setPreferredSize(new Dimension(300, 0));

        JPanel pnlLiquidacion = new JPanel(new GridBagLayout()); pnlLiquidacion.setBackground(new Color(255, 255, 255)); pnlLiquidacion.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(220, 222, 225), 1, true), BorderFactory.createEmptyBorder(15, 15, 15, 15)));
        GridBagConstraints gbc = new GridBagConstraints(); gbc.fill = GridBagConstraints.HORIZONTAL; gbc.gridx = 0; gbc.weightx = 1.0; gbc.insets = new Insets(5, 0, 5, 0);

        cmbMetodoPago = new JComboBox<>(); cmbMetodoPago.setFont(new Font("Segoe UI", Font.PLAIN, 14)); cmbMetodoPago.setBackground(new Color(255, 255, 255));
        
        pnlCamposExtraPago = new JPanel(new GridLayout(4, 1, 0, 5)); pnlCamposExtraPago.setOpaque(false);
        pnlCamposExtraPago.setVisible(false);
        
        JLabel lblBanco = new JLabel("Banco Emisor:"){{ setForeground(new Color(100, 100, 100)); setFont(new Font("Segoe UI", Font.BOLD, 12)); }};
        String[] bancosHonduras = {"Seleccione Banco...", "Banco Atl\u00E1ntida", "BAC Credomatic", "Banco Ficohsa", "Banpa\u00EDs", "Banco de Occidente", "Banco Banrural", "Banco Promerica", "Banco LAFISE", "Banco FICENSA", "Banco BANHCAFE", "ACH - Transferencia Interbancaria"};
        cmbBanco = new JComboBox<>(bancosHonduras); cmbBanco.setFont(new Font("Segoe UI", Font.PLAIN, 14)); cmbBanco.setBackground(new Color(255, 255, 255));
        
        lblReferenciaPago = new JLabel("N\u00BA Referencia:"){{ setForeground(new Color(100, 100, 100)); setFont(new Font("Segoe UI", Font.BOLD, 12)); }};
        txtReferenciaPago = new JTextField(); txtReferenciaPago.setFont(new Font("Segoe UI", Font.PLAIN, 14)); txtReferenciaPago.setBackground(new Color(255, 255, 255)); txtReferenciaPago.setForeground(new Color(45, 45, 45)); txtReferenciaPago.setCaretColor(new Color(45, 45, 45)); txtReferenciaPago.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(220, 222, 225)), BorderFactory.createEmptyBorder(0, 5, 0, 5))); txtReferenciaPago.setPreferredSize(new Dimension(0, 32));
        
        pnlCamposExtraPago.add(lblBanco); pnlCamposExtraPago.add(cmbBanco); pnlCamposExtraPago.add(lblReferenciaPago); pnlCamposExtraPago.add(txtReferenciaPago);

        cmbMetodoPago.addActionListener(e -> {
            ItemPago pagoSeleccionado = (ItemPago) cmbMetodoPago.getSelectedItem();
            if (pagoSeleccionado != null) {
                String nombrePago = pagoSeleccionado.nombre.toLowerCase();
                if (nombrePago.contains("transferencia") || nombrePago.contains("tarjeta")) {
                    lblReferenciaPago.setText(nombrePago.contains("transferencia") ? "N\u00BA Referencia / ACH:" : "N\u00BA Voucher / Referencia:");
                    pnlCamposExtraPago.setVisible(true);
                } else {
                    pnlCamposExtraPago.setVisible(false);
                    txtReferenciaPago.setText("");
                    cmbBanco.setSelectedIndex(0);
                }
                pnlLiquidacion.revalidate(); pnlLiquidacion.repaint();
            }
            recalcularTotales();
        });
        
        lblSubtotal = new JLabel("Subtotal: L 0.00"); lblSubtotal.setForeground(new Color(140, 145, 150)); lblSubtotal.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblImpuesto = new JLabel("ISV (15%): L 0.00"); lblImpuesto.setForeground(new Color(140, 145, 150)); lblImpuesto.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblTotal = new JLabel("L 0.00"); lblTotal.setForeground(utilidades.EfectosUI.COLOR_VERDE_PRIMARIO); lblTotal.setFont(new Font("Segoe UI", Font.BOLD, 36));

        JButton btnCobrar = new JButton("Cobrar Venta"); btnCobrar.setBackground(utilidades.EfectosUI.COLOR_VERDE_PRIMARIO); btnCobrar.setForeground(Color.WHITE); btnCobrar.setFont(new Font("Segoe UI", Font.BOLD, 16)); btnCobrar.setPreferredSize(new Dimension(0, 50)); btnCobrar.setFocusPainted(false); btnCobrar.setCursor(new Cursor(Cursor.HAND_CURSOR)); btnCobrar.setBorder(BorderFactory.createEmptyBorder(0,0,0,0)); btnCobrar.putClientProperty("JButton.buttonType", "roundRect");
        utilidades.EfectosUI.aplicarEfectoHover(btnCobrar, utilidades.EfectosUI.COLOR_VERDE_PRIMARIO, utilidades.EfectosUI.COLOR_VERDE_HOVER, Color.WHITE, Color.BLACK);
        btnCobrar.addActionListener(e -> procesarVenta());
        
        modelo.Usuario uAct = utilidades.SesionGlobal.getUsuarioActual();
        if (uAct != null && !uAct.tienePermiso("CREAR_POS")) {
            btnCobrar.setEnabled(false);
            btnCobrar.setToolTipText("No tienes permiso para registrar ventas o apartados.");
        }

        cmbTipoTransaccion = new JComboBox<>(new String[]{"Venta Directa", "Apartado (Abonos)"});
        cmbTipoTransaccion.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        cmbTipoTransaccion.setBackground(Color.WHITE);
        cmbTipoTransaccion.addActionListener(e -> {
            boolean esApartado = cmbTipoTransaccion.getSelectedIndex() == 1;
            btnCobrar.setText(esApartado ? "Registrar Apartado" : "Cobrar Venta");
        });

        gbc.gridy = 0; gbc.insets = new Insets(0, 0, 5, 0); pnlLiquidacion.add(new JLabel("Tipo Transacci\u00F3n:"){{setForeground(new Color(100, 100, 100)); setFont(new Font("Segoe UI", Font.BOLD, 12));}}, gbc);
        gbc.gridy = 1; gbc.insets = new Insets(0, 0, 8, 0); pnlLiquidacion.add(cmbTipoTransaccion, gbc);
        gbc.gridy = 2; gbc.insets = new Insets(5, 0, 5, 0); pnlLiquidacion.add(new JLabel("M\u00E9todo de Pago:"){{setForeground(new Color(100, 100, 100)); setFont(new Font("Segoe UI", Font.BOLD, 12));}}, gbc);
        gbc.gridy = 3; gbc.insets = new Insets(0, 0, 5, 0); pnlLiquidacion.add(cmbMetodoPago, gbc);
        gbc.gridy = 4; gbc.insets = new Insets(5, 0, 5, 0); pnlLiquidacion.add(pnlCamposExtraPago, gbc);
        gbc.gridy = 5; gbc.insets = new Insets(15, 0, 0, 0); pnlLiquidacion.add(lblSubtotal, gbc);
        gbc.gridy = 6; gbc.insets = new Insets(5, 0, 0, 0); pnlLiquidacion.add(lblImpuesto, gbc);
        gbc.gridy = 7; gbc.insets = new Insets(15, 0, 0, 0); pnlLiquidacion.add(new JLabel("TOTAL:"){{setForeground(new Color(45, 45, 45)); setFont(new Font("Segoe UI", Font.BOLD, 14));}}, gbc);
        gbc.gridy = 8; gbc.insets = new Insets(0, 0, 20, 0); pnlLiquidacion.add(lblTotal, gbc);
        gbc.gridy = 9; gbc.insets = new Insets(0, 0, 0, 0); pnlLiquidacion.add(btnCobrar, gbc);
        
        pnlControlVenta.add(pnlLiquidacion, BorderLayout.NORTH);
        this.add(pnlControlVenta, BorderLayout.EAST);
    }

    private void cargarMetodosPago() {
        cmbMetodoPago.removeAllItems(); 
        VentasDAO dao = new VentasDAO();
        Map<Integer, String> metodos = dao.obtenerMetodosPago();
        
        ItemPago itemEfectivo = null; // Variable para atrapar la opci\u00F3n de Efectivo
        
        for (Map.Entry<Integer, String> entry : metodos.entrySet()) {
            ItemPago item = new ItemPago(entry.getKey(), entry.getValue());
            cmbMetodoPago.addItem(item);
            
            // Verificamos si este m\u00E9todo es el Efectivo
            if (item.nombre.toLowerCase().contains("efectivo")) {
                itemEfectivo = item;
            }
        }
        
        // Si encontramos la opci\u00F3n de Efectivo, la fijamos como predeterminada
        if (itemEfectivo != null) {
            cmbMetodoPago.setSelectedItem(itemEfectivo);
        }
    }

    private void recalcularTotales() {
        granTotal = 0.0;
        sumSubtotal = 0.0;
        sumImpuesto = 0.0;
        
        for (int i = 0; i < modeloTablaVentas.getRowCount(); i++) {
            double subFila = (double) modeloTablaVentas.getValueAt(i, 5); // Cant * Precio
            boolean incluyeImp = (boolean) modeloTablaVentas.getValueAt(i, 10);
            
            if (incluyeImp) {
                sumSubtotal += subFila / 1.15;
            } else {
                sumSubtotal += subFila;
            }
        }

        // ItemPago pagoSeleccionado = (ItemPago) cmbMetodoPago.getSelectedItem();
        // boolean pagoConTarjeta = pagoSeleccionado != null && pagoSeleccionado.nombre.toLowerCase().contains("tarjeta");

        /*
        if (facturacionHabilitada || pagoConTarjeta) {
            sumImpuesto = sumSubtotal * 0.15;
            granTotal = sumSubtotal + sumImpuesto;
        } else {
            // Si no hay facturaci\u00F3n, el cliente no paga impuesto. (O puedes ajustarlo seg\u00FAn necesidad).
            // Seg\u00FAn el requisito, se muestra todo el total:
            granTotal = sumSubtotal; // Mantiene el subtotal como el precio final, o suma impuesto = 0.0
            sumImpuesto = 0.0;
        }
        */

        // Siempre cobrar impuesto, incluso si es en efectivo
        sumImpuesto = sumSubtotal * 0.15;
        granTotal = sumSubtotal + sumImpuesto;

        lblSubtotal.setText(String.format("Subtotal: L %,.2f", sumSubtotal));
        lblImpuesto.setText(String.format("ISV (15%%): L %,.2f", sumImpuesto));
        lblTotal.setText(String.format("L %,.2f", granTotal));
        
        sincronizarMemoria();
    }

   // --- RESTRICCI\u00D3N DE STOCK Y VALIDACI\u00D3N DE SERIE ---
    public void agregarProductoAVenta(Producto p) {
        if (p.getStockProducto() < 1) { 
            utilidades.Mensajes.showMessageDialog(this, "Stock agotado. No hay unidades disponibles en vitrina.", "Stock Insuficiente", JOptionPane.WARNING_MESSAGE); 
            return; 
        }

        String imei = null;
        if (p.isRequiereSerie()) {
            imei = JOptionPane.showInputDialog(this, "Este producto requiere un identificador (IMEI/ServiceTag).\nIngrese el c\u00F3digo para: " + p.getNombreProducto(), "Validaci\u00F3n de Garant\u00EDa", JOptionPane.QUESTION_MESSAGE);
            
            if (imei == null || imei.trim().isEmpty()) {
                utilidades.Mensajes.showMessageDialog(this, "Operaci\u00F3n cancelada. Debe ingresar el IMEI/Serie para facturar este producto.", "Aviso", JOptionPane.WARNING_MESSAGE);
                return;
            }
            imei = imei.trim();

            // --- VALIDACI\u00D3N 1: EVITAR IDENTIFICADOR DUPLICADO EN LA VENTA ACTUAL ---
            for (int i = 0; i < modeloTablaVentas.getRowCount(); i++) {
                Object valorImeiFila = modeloTablaVentas.getValueAt(i, 8); 
                if (valorImeiFila != null && valorImeiFila.toString().equalsIgnoreCase(imei)) {
                    utilidades.Mensajes.showMessageDialog(this, "El identificador '" + imei + "' ya est\u00E1 en la lista de compras actual.", "Identificador Duplicado", JOptionPane.ERROR_MESSAGE);
                    return; 
                }
            }
            
            // --- VALIDACI\u00D3N 2: EVITAR IDENTIFICADOR QUE YA FUE VENDIDO ANTERIORMENTE ---
            VentasDAO daoVentas = new VentasDAO();
            if (daoVentas.existeIdentificadorVendido(imei)) {
                utilidades.Mensajes.showMessageDialog(this, "\u00A1ALERTA! El identificador '" + imei + "' ya se encuentra registrado como VENDIDO en la base de datos.\nVerifique el equipo f\u00EDsico o contacte al administrador.", "Fraude / Error Detectado", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        // Si NO requiere serie Y NO TIENE GARANT\u00CDA, agrupamos las cantidades si ya est\u00E1 en la tabla
        if (!p.isRequiereSerie() && p.getDiasGarantia() == 0) {
            for (int i = 0; i < modeloTablaVentas.getRowCount(); i++) {
                if ((int) modeloTablaVentas.getValueAt(i, 0) == p.getIdProducto()) {
                    int cantActual = (int) modeloTablaVentas.getValueAt(i, 3);
                    if (cantActual >= p.getStockProducto()) {
                        utilidades.Mensajes.showMessageDialog(this, "Stock insuficiente. Solo hay " + p.getStockProducto() + " unidades en vitrina.", "Aviso", JOptionPane.WARNING_MESSAGE); 
                        return;
                    }
                    double precioUnit = (double) modeloTablaVentas.getValueAt(i, 4);
                    modeloTablaVentas.setValueAt(cantActual + 1, i, 3);
                    modeloTablaVentas.setValueAt((cantActual + 1) * precioUnit, i, 5);
                    recalcularTotales(); return;
                }
            }
        }

        // Si requiere serie, o es un repuesto nuevo, lo agregamos como fila independiente
        double precioUsar = p.getPrecioVenta();
        if (memoriaMayoristaActivo && memoriaMayoristaGlobal && p.getPrecioMayorista() > 0) {
            precioUsar = p.getPrecioMayorista();
        }

        modeloTablaVentas.addRow(new Object[]{ 
            p.getIdProducto(), p.getImagen_producto(), p.getNombreProducto(), 1, 
            precioUsar, precioUsar, p.getStockProducto(), p.getImagen_producto(), 
            imei, p.getDiasGarantia(), p.isIncluyeImpuesto(), p.getPrecioVenta(), p.getPrecioMayorista()
        });
        recalcularTotales();
    }

    private void buscarProductoPorCodigo(String codigo) {
        if(codigo.isEmpty()) return;
        VentasDAO dao = new VentasDAO();
        Producto p = dao.buscarProductoPorCodigo(codigo);
        if (p != null) agregarProductoAVenta(p);
        else utilidades.Mensajes.showMessageDialog(this, "Producto no encontrado o inactivo.", "Aviso", JOptionPane.WARNING_MESSAGE);
        txtCodigoBarrasBusqueda.setText(""); txtCodigoBarrasBusqueda.requestFocus();
    }

    private void modificarPrecio() {
        int f = tablaVentas.getSelectedRow(); if(f < 0) return;
        
        // Creamos un cuadro de texto personalizado
        JTextField txtNuevoPrecio = new JTextField(String.valueOf(modeloTablaVentas.getValueAt(f, 4)));
        txtNuevoPrecio.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        
        // Le agregamos un filtro que se dispara cada vez que el usuario presiona una tecla
        txtNuevoPrecio.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent e) {
                char c = e.getKeyChar();
                // Si la tecla NO es un n\u00FAmero, NO es un punto y NO es la tecla de borrar -> la bloqueamos
                if (!Character.isDigit(c) && c != '.' && c != java.awt.event.KeyEvent.VK_BACK_SPACE) {
                    e.consume(); // Anula la pulsaci\u00F3n
                }
                // Si ya hay un punto decimal y el usuario intenta poner otro -> lo bloqueamos
                if (c == '.' && txtNuevoPrecio.getText().contains(".")) {
                    e.consume(); 
                }
            }
        });

        // Mostramos el cuadro de di\u00E1logo con nuestro JTextField protegido
        int opcion = utilidades.Mensajes.showConfirmDialog(this, new Object[]{"Nuevo Precio Unitario:", txtNuevoPrecio}, "Modificar Precio", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        
        if (opcion == JOptionPane.OK_OPTION && !txtNuevoPrecio.getText().isEmpty()) {
            try {
                double nuevoPrecio = Double.parseDouble(txtNuevoPrecio.getText());
                int cant = (int) modeloTablaVentas.getValueAt(f, 3);
                modeloTablaVentas.setValueAt(nuevoPrecio, f, 4); 
                modeloTablaVentas.setValueAt(cant * nuevoPrecio, f, 5);
                recalcularTotales();
            } catch(NumberFormatException ex) {}
        }
    }

    private void modificarCantidad() {
        int f = tablaVentas.getSelectedRow(); if(f < 0) return;
        
        // --- BLOQUEO DE SEGURIDAD PARA GARANT\u00CDAS Y SERIES ---
        String imei = (String) modeloTablaVentas.getValueAt(f, 8);
        int garant = (int) modeloTablaVentas.getValueAt(f, 9);
        if ((imei != null && !imei.isEmpty()) || garant > 0) {
            utilidades.Mensajes.showMessageDialog(this, "No puede modificar la cantidad de un equipo que requiere Identificador o posee Garant\u00EDa.\nSi el cliente lleva varios, escanee el producto nuevamente.", "Acci\u00F3n Bloqueada", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int stockMaximo = (int) modeloTablaVentas.getValueAt(f, 6);
        
        JTextField txtNuevaCant = new JTextField(String.valueOf(modeloTablaVentas.getValueAt(f, 3)));
        txtNuevaCant.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        
        txtNuevaCant.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent e) {
                char c = e.getKeyChar();
                if (!Character.isDigit(c) && c != java.awt.event.KeyEvent.VK_BACK_SPACE) {
                    e.consume(); 
                }
            }
        });

        int opcion = utilidades.Mensajes.showConfirmDialog(this, new Object[]{"Nueva Cantidad (Stock Disponible: " + stockMaximo + "):", txtNuevaCant}, "Modificar Cantidad", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        
        if (opcion == JOptionPane.OK_OPTION && !txtNuevaCant.getText().isEmpty()) {
            try {
                int nuevaCant = Integer.parseInt(txtNuevaCant.getText());
                if (nuevaCant <= 0) { quitarProducto(); return; }
                if (nuevaCant > stockMaximo) {
                    utilidades.Mensajes.showMessageDialog(this, "No puede facturar " + nuevaCant + " unidades. Solo hay " + stockMaximo + " en stock.", "Error", JOptionPane.ERROR_MESSAGE); 
                    return;
                }
                double precio = (double) modeloTablaVentas.getValueAt(f, 4);
                modeloTablaVentas.setValueAt(nuevaCant, f, 3); 
                modeloTablaVentas.setValueAt(nuevaCant * precio, f, 5);
                recalcularTotales();
            } catch(NumberFormatException ex) {}
        }
    }

    private void quitarProducto() {
        int f = tablaVentas.getSelectedRow(); if(f < 0) return;
        modeloTablaVentas.removeRow(f); recalcularTotales();
    }

    private void configurarAtajosTeclado() {
        javax.swing.InputMap inputMapGlobal = this.getInputMap(javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW);
        javax.swing.ActionMap actionMapGlobal = this.getActionMap();

        inputMapGlobal.put(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F12, 0), "cobrarVenta");
        actionMapGlobal.put("cobrarVenta", new javax.swing.AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                modelo.Usuario uAct = utilidades.SesionGlobal.getUsuarioActual();
                if (uAct != null && !uAct.tienePermiso("CREAR_POS")) return;
                procesarVenta();
            }
        });

        javax.swing.InputMap inputMapTabla = tablaVentas.getInputMap(javax.swing.JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        javax.swing.ActionMap actionMapTabla = tablaVentas.getActionMap();

        javax.swing.Action accionAumentar = new javax.swing.AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                aumentarCantidadLocal();
            }
        };
        inputMapTabla.put(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ADD, 0), "aumentarCant");
        inputMapTabla.put(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_PLUS, 0), "aumentarCant");
        inputMapTabla.put(javax.swing.KeyStroke.getKeyStroke('+', java.awt.event.InputEvent.SHIFT_DOWN_MASK), "aumentarCant");
        actionMapTabla.put("aumentarCant", accionAumentar);

        javax.swing.Action accionDisminuir = new javax.swing.AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                disminuirCantidadLocal();
            }
        };
        inputMapTabla.put(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_SUBTRACT, 0), "disminuirCant");
        inputMapTabla.put(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_MINUS, 0), "disminuirCant");
        actionMapTabla.put("disminuirCant", accionDisminuir);
    }

    private void aumentarCantidadLocal() {
        int f = tablaVentas.getSelectedRow();
        if (f < 0) return;
        String imei = (String) modeloTablaVentas.getValueAt(f, 8);
        int garant = (int) modeloTablaVentas.getValueAt(f, 9);
        if ((imei != null && !imei.isEmpty()) || garant > 0) return; 

        int cantActual = (int) modeloTablaVentas.getValueAt(f, 3);
        int stockMax = (int) modeloTablaVentas.getValueAt(f, 6);
        if (cantActual < stockMax) {
            double precioUnit = (double) modeloTablaVentas.getValueAt(f, 4);
            modeloTablaVentas.setValueAt(cantActual + 1, f, 3);
            modeloTablaVentas.setValueAt((cantActual + 1) * precioUnit, f, 5);
            recalcularTotales();
        } else {
            utilidades.Mensajes.showMessageDialog(this, "Stock insuficiente.", "Aviso", javax.swing.JOptionPane.WARNING_MESSAGE);
        }
    }

    private void disminuirCantidadLocal() {
        int f = tablaVentas.getSelectedRow();
        if (f < 0) return;
        String imei = (String) modeloTablaVentas.getValueAt(f, 8);
        int garant = (int) modeloTablaVentas.getValueAt(f, 9);
        if ((imei != null && !imei.isEmpty()) || garant > 0) return; 

        int cantActual = (int) modeloTablaVentas.getValueAt(f, 3);
        if (cantActual > 1) {
            double precioUnit = (double) modeloTablaVentas.getValueAt(f, 4);
            modeloTablaVentas.setValueAt(cantActual - 1, f, 3);
            modeloTablaVentas.setValueAt((cantActual - 1) * precioUnit, f, 5);
            recalcularTotales();
        } else if (cantActual == 1) {
            quitarProducto();
        }
    }

    private void procesarVenta() {
        if(modeloTablaVentas.getRowCount() == 0) { utilidades.Mensajes.showMessageDialog(this, "La venta est\u00E1 vac\u00EDa.", "Aviso", javax.swing.JOptionPane.WARNING_MESSAGE); return; }
        
        // Validar que la caja est\u00E9 abierta
        modelo.ControlCaja CCActiva = new dao.ControlCajaDAO().obtenerSesionActiva();
        if (CCActiva == null) {
            utilidades.Mensajes.showMessageDialog(this, "Operaci\u00F3n denegada: El turno de caja no est\u00E1 abierto. Abra la caja antes de registrar transacciones.", "Caja Cerrada", JOptionPane.ERROR_MESSAGE);
            return;
        }

        VentasDAO dao = new VentasDAO();
        ItemPago pago = (ItemPago) cmbMetodoPago.getSelectedItem();
        if(pago == null) return;

        String refPago = null;
        String bancoSeleccionado = null;
        if (pnlCamposExtraPago.isVisible()) {
            if (cmbBanco.getSelectedIndex() == 0) {
                utilidades.Mensajes.showMessageDialog(this, "Debe seleccionar el Banco Emisor para autorizar esta transacci\u00F3n.", "Datos Incompletos", JOptionPane.WARNING_MESSAGE);
                return;
            }
            String refStr = txtReferenciaPago.getText().trim();
            if (!refStr.matches("^[a-zA-Z0-9]{4,}$")) {
                utilidades.Mensajes.showMessageDialog(this, "El n\u00FAmero de Referencia, Voucher o ACH debe contener al menos 4 caracteres alfanum\u00E9ricos.", "Datos Inv\u00E1lidos", JOptionPane.WARNING_MESSAGE);
                return;
            }
            refPago = refStr;
            bancoSeleccionado = cmbBanco.getSelectedItem().toString();
        }

        boolean esApartado = cmbTipoTransaccion.getSelectedIndex() == 1;

        if (idClienteActual == 1) {
            if (esApartado) {
                utilidades.Mensajes.showMessageDialog(this, "Para registrar un Apartado (Abonos) es obligatorio seleccionar un cliente registrado. No se permite Consumidor Final.", "Cliente Requerido", JOptionPane.WARNING_MESSAGE);
                DialogoBuscarClientePOS dlgBuscar = new DialogoBuscarClientePOS((Frame) SwingUtilities.getWindowAncestor(this));
                dlgBuscar.setVisible(true);
                
                if (idClienteActual == 1) {
                    utilidades.Mensajes.showMessageDialog(this, "Operación cancelada. Se requiere asociar un cliente para registrar el apartado.", "Aviso", JOptionPane.WARNING_MESSAGE);
                    return;
                }
            }
        }

        if (esApartado) {
            JTextField txtAbonoInicial = new JTextField("0.00");
            txtAbonoInicial.addKeyListener(new java.awt.event.KeyAdapter() {
                public void keyTyped(java.awt.event.KeyEvent evt) {
                    char c = evt.getKeyChar();
                    if (!Character.isDigit(c) && c != '.') evt.consume();
                    if (c == '.' && txtAbonoInicial.getText().contains(".")) evt.consume();
                }
            });
            JTextField txtDiasPlazo = new JTextField("30");
            txtDiasPlazo.addKeyListener(new java.awt.event.KeyAdapter() {
                public void keyTyped(java.awt.event.KeyEvent evt) {
                    if (!Character.isDigit(evt.getKeyChar())) evt.consume();
                }
            });
            Object[] fields = {
                "Total de la Compra: L " + String.format("%,.2f", granTotal),
                "Abono Inicial (L):", txtAbonoInicial,
                "Plazo de Pago (D\u00EDas):", txtDiasPlazo
            };

            int opApartado = utilidades.Mensajes.showConfirmDialog(this, fields, "Detalles del Apartado", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (opApartado != JOptionPane.OK_OPTION) return;

            double abono = 0;
            int dias = 0;
            try {
                abono = Double.parseDouble(txtAbonoInicial.getText().trim());
                dias = Integer.parseInt(txtDiasPlazo.getText().trim());
            } catch (NumberFormatException ex) {
                utilidades.Mensajes.showMessageDialog(this, "Verifique los valores num\u00E9ricos ingresados.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (abono < 0 || abono > granTotal) {
                utilidades.Mensajes.showMessageDialog(this, "El abono inicial debe ser menor o igual al total.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (dias <= 0) {
                    utilidades.Mensajes.showMessageDialog(this, "El plazo en d\u00EDas debe ser mayor a 0.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // Password signature
                JPasswordField pfPass = new JPasswordField();
                int opSign = utilidades.Mensajes.showConfirmDialog(this, new Object[]{"Ingrese su contrase\u00F1a para firmar el apartado:", pfPass}, "Firma Requerida", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
                if (opSign != JOptionPane.OK_OPTION) return;

                String pass = new String(pfPass.getPassword());
                int idUsuarioAutorizado = dao.obtenerIdUsuarioPorPassword(pass);

                if (idUsuarioAutorizado <= 0) {
                    utilidades.Mensajes.showMessageDialog(this, "Contrase\u00F1a incorrecta o usuario inactivo.", "Acceso Denegado", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // Create Apartado
                Apartado a = new Apartado();
                a.setTotalApartado(granTotal);
                a.setAbonoInicial(abono);
                a.setSaldoPendiente(granTotal - abono);
                a.setIdClienteApartado(idClienteActual);
                a.setIdUsuario(idUsuarioAutorizado);
                a.setIdMetodoPago(pago.id);
                a.setReferenciaPago(refPago);
                a.setBancoPago(bancoSeleccionado);
                
                // Fecha limite
                java.util.Calendar cal = java.util.Calendar.getInstance();
                cal.add(java.util.Calendar.DAY_OF_YEAR, dias);
                a.setFechaLimite(new Timestamp(cal.getTimeInMillis()));

                // Detalles
                List<DetalleApartado> detallesA = new ArrayList<>();
                List<Object[]> detallesPDF = new ArrayList<>();
                for (int i = 0; i < modeloTablaVentas.getRowCount(); i++) {
                    DetalleApartado d = new DetalleApartado();
                    d.setIdProducto((int) modeloTablaVentas.getValueAt(i, 0));
                    d.setDescripcionApartado((String) modeloTablaVentas.getValueAt(i, 2));
                    d.setCantidadApartado((int) modeloTablaVentas.getValueAt(i, 3));
                    d.setPrecioUnitarioApartado((double) modeloTablaVentas.getValueAt(i, 4));
                    d.setSubtotalApartado((double) modeloTablaVentas.getValueAt(i, 5));
                    d.setIdentificadorSerie(modeloTablaVentas.getValueAt(i, 9) != null ? modeloTablaVentas.getValueAt(i, 9).toString() : null);
                    detallesA.add(d);

                    detallesPDF.add(new Object[]{
                        modeloTablaVentas.getValueAt(i, 0), modeloTablaVentas.getValueAt(i, 8), modeloTablaVentas.getValueAt(i, 2),
                        modeloTablaVentas.getValueAt(i, 3), modeloTablaVentas.getValueAt(i, 4), modeloTablaVentas.getValueAt(i, 5),
                        modeloTablaVentas.getValueAt(i, 9)
                    });
                }

                ApartadoDAO apDAO = new ApartadoDAO();
                int idApartadoGenerado = apDAO.registrarApartado(a, detallesA);
                if (idApartadoGenerado > 0) {
                    // Generar PDF
                    txtReferenciaPago.setText(""); cmbBanco.setSelectedIndex(0); pnlCamposExtraPago.setVisible(false);
                    
                    File dir = new File("reportes/apartados");
                    if (!dir.exists()) dir.mkdirs();
                    File archivoDestino = new File("reportes/apartados/Ticket_Apartado_" + idApartadoGenerado + ".pdf");

                    try {
                        String fechaActual = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm").format(new java.util.Date());
                        String fechaLim = new java.text.SimpleDateFormat("dd/MM/yyyy").format(a.getFechaLimite());

                        utilidades.GeneradorTickets.generarTicketApartadoPDF(
                            archivoDestino.getAbsolutePath(),
                            lblClienteSeleccionado.getText(),
                            fechaActual,
                            fechaLim,
                            detallesPDF,
                            granTotal,
                            abono,
                            a.getSaldoPendiente(),
                            pago.nombre
                        );
                        utilidades.Mensajes.showMessageDialog(this, "Apartado registrado con \u00E9xito.", "\u00C9xito", JOptionPane.INFORMATION_MESSAGE);
                        if (Desktop.isDesktopSupported()) utilidades.GestorImpresion.procesarImpresion(archivoDestino, utilidades.GestorImpresion.TIPO_TICKET);
                    } catch (Exception ex) {
                        utilidades.Mensajes.showMessageDialog(this, "El apartado se guard\u00F3, pero hubo un error al generar el PDF:\n" + ex.getMessage(), "Aviso", JOptionPane.WARNING_MESSAGE);
                    }

                    modeloTablaVentas.setRowCount(0); recalcularTotales();
                    lblClienteSeleccionado.setText("CONSUMIDOR FINAL"); idClienteActual = 1;
                    txtAbonoInicial.setText("");
                } else {
                    utilidades.Mensajes.showMessageDialog(this, "Error cr\u00EDtico al guardar el apartado en la base de datos.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            return;
        }

        JPasswordField pfPass = new JPasswordField();
        int opcion = utilidades.Mensajes.showConfirmDialog(this, new Object[]{"Ingrese su contrase\u00F1a de cajero para autorizar la venta:", pfPass}, "Autorizar Cobro", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (opcion != JOptionPane.OK_OPTION) return;

        String pass = new String(pfPass.getPassword());
        int idUsuarioAutorizado = dao.obtenerIdUsuarioPorPassword(pass);

        if (idUsuarioAutorizado <= 0) {
            utilidades.Mensajes.showMessageDialog(this, "Contrase\u00F1a incorrecta o usuario inactivo.", "Acceso Denegado", JOptionPane.ERROR_MESSAGE);
            return;
        }

        List<Object[]> detalles = new ArrayList<>();
        for(int i = 0; i < modeloTablaVentas.getRowCount(); i++) {
            detalles.add(new Object[]{
                modeloTablaVentas.getValueAt(i, 0), modeloTablaVentas.getValueAt(i, 8), modeloTablaVentas.getValueAt(i, 2),
                modeloTablaVentas.getValueAt(i, 3), modeloTablaVentas.getValueAt(i, 4), modeloTablaVentas.getValueAt(i, 5),
                modeloTablaVentas.getValueAt(i, 9)
            });
        }

        try {
            int idVentaGenerada = dao.procesarVentaCompleta(idClienteActual, idUsuarioAutorizado, pago.id, sumSubtotal, sumImpuesto, granTotal, refPago, bancoSeleccionado, detalles);
            if (idVentaGenerada > 0) {
                
                txtReferenciaPago.setText(""); cmbBanco.setSelectedIndex(0); pnlCamposExtraPago.setVisible(false);
                
                File dir = new File("reportes/ventas");
                if (!dir.exists()) dir.mkdirs();
                File archivoDestino = new File("reportes/ventas/Ticket_Venta_" + idVentaGenerada + ".pdf");

               try {
                    // Generamos la fecha exacta del momento en que se presiona el bot\u00F3n cobrar
                    String fechaActual = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm").format(new java.util.Date());

                    utilidades.GeneradorTickets.generarTicketVentaPDF(
                        archivoDestino.getAbsolutePath(), 
                        lblClienteSeleccionado.getText(), 
                        fechaActual, // <--- PASAMOS LA FECHA ACTUAL EN VIVO
                        detalles, sumSubtotal, sumImpuesto, granTotal,
                        facturacionHabilitada,
                        pago.nombre, 
                        refPago, bancoSeleccionado
                    );
                    utilidades.Mensajes.showMessageDialog(this, "Venta registrada y comprobante generado con \u00E9xito.", "\u00C9xito", JOptionPane.INFORMATION_MESSAGE);
                    if (Desktop.isDesktopSupported()) utilidades.GestorImpresion.procesarImpresion(archivoDestino, utilidades.GestorImpresion.TIPO_TICKET);
                } catch (Exception ex) {
                    utilidades.Mensajes.showMessageDialog(this, "La venta se guard\u00F3, pero hubo un error al generar el PDF:\n" + ex.getMessage(), "Aviso", JOptionPane.WARNING_MESSAGE);
                }

                modeloTablaVentas.setRowCount(0); recalcularTotales();
                lblClienteSeleccionado.setText("CONSUMIDOR FINAL"); idClienteActual = 1;
            } else {
                utilidades.Mensajes.showMessageDialog(this, "Error al registrar la venta en la Base de Datos.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (java.sql.SQLException ex) {
            utilidades.Mensajes.showMessageDialog(this, "No se pudo procesar la venta: \n" + ex.getMessage(), "Error de Base de Datos", JOptionPane.ERROR_MESSAGE);
        }
    }

    private class ItemPago {
        int id; String nombre;
        public ItemPago(int id, String nombre) { this.id = id; this.nombre = nombre; }
        @Override public String toString() { return nombre; }
    }

    private class ImagenMiniaturaRenderer extends DefaultTableCellRenderer {
        @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            
            int filaModelo = table.convertRowIndexToModel(row);
            int idProducto = (int) table.getModel().getValueAt(filaModelo, 0);
            
            if (cacheImagenes.containsKey(idProducto)) {
                JLabel label = new JLabel(); 
                label.setHorizontalAlignment(SwingConstants.CENTER); 
                label.setOpaque(true); 
                label.setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
                
                ImageIcon icon = cacheImagenes.get(idProducto);
                if (icon != null && icon.getIconWidth() > 0) {
                    label.setIcon(icon);
                } else { 
                    label.setText("No Img"); 
                    label.setForeground(new Color(140, 145, 150)); 
                }
                return label;
            }
            
            if (!imagenesEnProceso.contains(idProducto)) {
                imagenesEnProceso.add(idProducto);
                
                SwingWorker<ImageIcon, Void> worker = new SwingWorker<>() {
                    @Override
                    protected ImageIcon doInBackground() throws Exception {
                        String imgVal = new InventarioDAO().obtenerRutaImagenBase64(idProducto);
                        if (imgVal == null || imgVal.trim().isEmpty()) {
                            if (utilidades.SesionGlobal.getEmpresaActual() != null && utilidades.SesionGlobal.getEmpresaActual().getImagen_logo() != null) {
                                imgVal = utilidades.SesionGlobal.getEmpresaActual().getImagen_logo();
                            }
                        }
                          if (imgVal == null || imgVal.trim().isEmpty()) {
                              java.net.URL defaultLogo = getClass().getResource("/image/logo.png");
                              if (defaultLogo != null) {
                                  return new ImageIcon(new ImageIcon(defaultLogo).getImage().getScaledInstance(60, 60, Image.SCALE_SMOOTH));
                              }
                              return null;
                          }
                        
                        String valProcesar = imgVal;
                        if (valProcesar.contains("|")) {
                            valProcesar = valProcesar.split("\\|")[0];
                        }
                        return utilidades.ImagenHelper.obtenerIcono(valProcesar, 50, 50);
                    }
                    @Override
                    protected void done() {
                        try {
                            ImageIcon icon = get();
                            cacheImagenes.put(idProducto, icon != null ? icon : new ImageIcon()); 
                        } catch (Exception ex) {
                            cacheImagenes.put(idProducto, new ImageIcon());
                        } finally {
                            imagenesEnProceso.remove(idProducto);
                            table.repaint();
                        }
                    }
                };
                worker.execute();
            }

            JPanel panelShimmer = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    
                    g2.setColor(new Color(235, 237, 240));
                    g2.fillRoundRect(getWidth()/2 - 25, getHeight()/2 - 25, 50, 50, 10, 10);
                    
                    int gradientWidth = 50;
                    int startX = (getWidth()/2 - 25) - gradientWidth + (int)(animacionShimmerPhase * (50 + gradientWidth * 2));
                    
                    Color c1 = new Color(255, 255, 255, 0);
                    Color c2 = new Color(255, 255, 255, 200);
                    
                    LinearGradientPaint paint = new LinearGradientPaint(
                            startX, 0, startX + gradientWidth, 0,
                            new float[]{0.0f, 0.5f, 1.0f},
                            new Color[]{c1, c2, c1}
                    );
                    
                    g2.setPaint(paint);
                    g2.fillRoundRect(getWidth()/2 - 25, getHeight()/2 - 25, 50, 50, 10, 10);
                    g2.dispose();
                }
            };
            panelShimmer.setOpaque(true);
            panelShimmer.setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
            return panelShimmer;
        }
    }

    private class DialogoConfirmacionCliente extends JDialog {
        private int resultado = -1; // 0 = Buscar Cliente, 1 = Consumidor Final, 2 = Cancelar

        public DialogoConfirmacionCliente(Frame parent, String mensajeInfo) {
            super(parent, "Confirmaci\u00F3n de Cliente", true);
            setSize(450, 290);
            setLocationRelativeTo(parent);
            setUndecorated(true); // Sin marcos para un dise\u00F1o limpio y moderno
            
            JPanel pnlPrincipal = new JPanel(new BorderLayout(15, 15));
            pnlPrincipal.setBackground(Color.WHITE); // Fondo blanco limpio
            pnlPrincipal.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(180, 208, 192), 1, true), // Borde suave redondeado
                BorderFactory.createEmptyBorder(25, 25, 25, 25)
            ));

            // Cabecera / T\u00EDtulo con Icono
            JPanel pnlHeader = new JPanel(new BorderLayout(10, 5));
            pnlHeader.setOpaque(false);

            JLabel lblIcon = new JLabel("\uD83D\uDC65");
            lblIcon.setFont(new Font("Segoe UI", Font.PLAIN, 46));
            lblIcon.setHorizontalAlignment(SwingConstants.CENTER);
            lblIcon.setForeground(new Color(45, 106, 79));
            pnlHeader.add(lblIcon, BorderLayout.NORTH);

            JLabel lblTitulo = new JLabel("Asociar Cliente a la Transacci\u00F3n");
            lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 18));
            lblTitulo.setForeground(new Color(30, 41, 59)); // Gris oscuro moderno
            lblTitulo.setHorizontalAlignment(SwingConstants.CENTER);
            pnlHeader.add(lblTitulo, BorderLayout.SOUTH);
            pnlPrincipal.add(pnlHeader, BorderLayout.NORTH);

            // Mensaje Central
            JLabel lblMensaje = new JLabel("<html><center>" + mensajeInfo + "</center></html>");
            lblMensaje.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            lblMensaje.setForeground(new Color(71, 85, 105)); // Gris suave
            lblMensaje.setHorizontalAlignment(SwingConstants.CENTER);
            pnlPrincipal.add(lblMensaje, BorderLayout.CENTER);

            // Botonera
            JPanel pnlBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
            pnlBotones.setOpaque(false);

            JButton btnBuscar = utilidades.EfectosUI.crearBotonVerde("Buscar Cliente");
            btnBuscar.setFont(new Font("Segoe UI", Font.BOLD, 13));
            btnBuscar.setPreferredSize(new Dimension(135, 42));
            btnBuscar.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btnBuscar.addActionListener(e -> {
                resultado = 0;
                dispose();
            });

            JButton btnCF = utilidades.EfectosUI.crearBotonBlanco("Consumidor Final");
            btnCF.setFont(new Font("Segoe UI", Font.BOLD, 13));
            btnCF.setPreferredSize(new Dimension(145, 42));
            btnCF.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btnCF.addActionListener(e -> {
                resultado = 1;
                dispose();
            });

            JButton btnCancelar = utilidades.EfectosUI.crearBotonPeligro("Cancelar");
            btnCancelar.setFont(new Font("Segoe UI", Font.BOLD, 13));
            btnCancelar.setPreferredSize(new Dimension(95, 42));
            btnCancelar.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btnCancelar.addActionListener(e -> {
                resultado = 2;
                dispose();
            });

            pnlBotones.add(btnBuscar);
            pnlBotones.add(btnCF);
            pnlBotones.add(btnCancelar);

            pnlPrincipal.add(pnlBotones, BorderLayout.SOUTH);
            
            add(pnlPrincipal);
        }

        public int getResultado() {
            return resultado;
        }
    }

    private class DialogoBuscarClientePOS extends JDialog {
        public DialogoBuscarClientePOS(Frame parent) {
            super(parent, "Seleccionar Cliente", true);
            setSize(800, 550); setLocationRelativeTo(parent); 
            getContentPane().setBackground(Color.WHITE); 
            setLayout(new BorderLayout(10, 10));
            
            JPanel pnlTopWrapper = new JPanel(new BorderLayout());
            pnlTopWrapper.setOpaque(false);
            
            JPanel pnlHeader = new JPanel(new BorderLayout(15, 5));
            pnlHeader.setBackground(Color.WHITE);
            pnlHeader.setBorder(BorderFactory.createEmptyBorder(15, 20, 10, 20));

            JPanel pnlTitleText = new JPanel(new GridLayout(2, 1, 0, 2));
            pnlTitleText.setOpaque(false);
            JLabel lblTitle = new JLabel("Buscar y Seleccionar Cliente");
            lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
            lblTitle.setForeground(new Color(30, 41, 59));
            JLabel lblSubtitle = new JLabel("Busque por nombre o identidad, o registre un nuevo cliente.");
            lblSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            lblSubtitle.setForeground(new Color(100, 116, 139));
            pnlTitleText.add(lblTitle);
            pnlTitleText.add(lblSubtitle);
            pnlHeader.add(pnlTitleText, BorderLayout.WEST);

            String[] cols = {"ID", "Avatar", "Nombre Completo", "Identidad", "Tel\u00E9fono"};
            DefaultTableModel mod = new DefaultTableModel(null, cols) { @Override public boolean isCellEditable(int r, int c) { return false; } };

            JButton btnNuevo = utilidades.EfectosUI.crearBotonVerde("+ Nuevo Cliente");
            btnNuevo.setFont(new Font("Segoe UI", Font.BOLD, 13));
            btnNuevo.setPreferredSize(new Dimension(150, 36));
            btnNuevo.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btnNuevo.addActionListener(e -> {
                setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                btnNuevo.setEnabled(false);

                SwingWorker<PanelFormularioCliente, Void> worker = new SwingWorker<PanelFormularioCliente, Void>() {
                    @Override
                    protected PanelFormularioCliente doInBackground() throws Exception {
                        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(DialogoBuscarClientePOS.this), "Nuevo Cliente", true);
                        dialog.setUndecorated(true); dialog.setBackground(new Color(0,0,0,0));
                        return new PanelFormularioCliente(dialog, new PanelGestionClientes(), null);
                    }
                    @Override
                    protected void done() {
                        setCursor(Cursor.getDefaultCursor());
                        btnNuevo.setEnabled(true);
                        try {
                            PanelFormularioCliente form = get();
                            JDialog dialog = form.getDialogoPadre();
                            dialog.add(form);
                            dialog.pack(); dialog.setLocationRelativeTo(DialogoBuscarClientePOS.this); 
                            dialog.setVisible(true);
                            
                            // Recargar tabla de clientes (se hace cuando se cierra el di\u00E1logo)
                            mod.setRowCount(0);
                            mod.addRow(new Object[]{1, "C", "CONSUMIDOR FINAL", "0000-0000-00000", "N/A"});
                            int maxId = 1;
                            String maxNombre = "CONSUMIDOR FINAL";
                            for (Cliente c : new ClienteDAO().listarClientesActivos()) {
                                String nombreComp = c.getNombreCliente() + " " + (c.getApellidoCliente()!=null?c.getApellidoCliente():"");
                                String inicial = nombreComp.isEmpty() ? "?" : nombreComp.substring(0, 1).toUpperCase();
                                mod.addRow(new Object[]{c.getIdCliente(), inicial, nombreComp, c.getIdentidadCliente(), c.getTelefonoCliente()});
                                if (c.getIdCliente() > maxId) {
                                    maxId = c.getIdCliente();
                                    maxNombre = nombreComp;
                                }
                            }
                            
                            // Auto-seleccionar nuevo cliente creado
                            if (maxId != 1) {
                                idClienteActual = maxId;
                                lblClienteSeleccionado.setText(maxNombre);
                                dispose();
                            }
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                };
                worker.execute();
            });
            pnlHeader.add(btnNuevo, BorderLayout.EAST);
            pnlTopWrapper.add(pnlHeader, BorderLayout.NORTH);

            JPanel pnlSearch = new JPanel(new BorderLayout());
            pnlSearch.setBackground(Color.WHITE);
            pnlSearch.setBorder(BorderFactory.createEmptyBorder(0, 20, 15, 20));
            
            JTextField txtB = new JTextField();
            txtB.setPreferredSize(new Dimension(0, 42));
            txtB.setBackground(new Color(248, 250, 252));
            txtB.setForeground(new Color(51, 65, 85));
            txtB.setCaretColor(new Color(51, 65, 85));
            txtB.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240), 1, true),
                BorderFactory.createEmptyBorder(0, 12, 0, 12)
            ));
            txtB.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            txtB.putClientProperty("JTextField.placeholderText", "\uD83D\uDD0D Escriba el nombre, identidad o DNI del cliente...");
            pnlSearch.add(txtB, BorderLayout.CENTER);
            pnlTopWrapper.add(pnlSearch, BorderLayout.SOUTH);
            add(pnlTopWrapper, BorderLayout.NORTH);
            
            JTable tab = new JTable(mod); tab.setBackground(Color.WHITE); tab.setForeground(new Color(51, 65, 85)); tab.setRowHeight(45); tab.setFont(new Font("Segoe UI", Font.PLAIN, 14)); tab.setSelectionBackground(new Color(205, 235, 218)); tab.setSelectionForeground(Color.BLACK);
            tab.getTableHeader().setBackground(new Color(248, 250, 252)); tab.getTableHeader().setForeground(new Color(71, 85, 105)); tab.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(226, 232, 240))); tab.getTableHeader().setPreferredSize(new Dimension(0, 38)); tab.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
            
            tab.getColumnModel().getColumn(0).setMinWidth(0); tab.getColumnModel().getColumn(0).setMaxWidth(0); 
            tab.getColumnModel().getColumn(1).setPreferredWidth(50); tab.getColumnModel().getColumn(1).setMaxWidth(50);
            
            tab.getColumnModel().getColumn(1).setCellRenderer(new DefaultTableCellRenderer() {
                @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                    JPanel panel = new JPanel() {
                        @Override protected void paintComponent(Graphics g) {
                            super.paintComponent(g); Graphics2D g2 = (Graphics2D) g; g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                            String inicial = value != null ? value.toString() : "?"; int hash = Math.abs(inicial.hashCode());
                            Color[] paleta = {new Color(79, 70, 229), new Color(13, 148, 136), new Color(225, 29, 72), new Color(217, 119, 6)};
                            g2.setColor(paleta[hash % paleta.length]);
                            int size = 32; int x = (getWidth() - size) / 2; int y = (getHeight() - size) / 2;
                            g2.fill(new java.awt.geom.Ellipse2D.Double(x, y, size, size));
                            g2.setColor(Color.WHITE); g2.setFont(new Font("Segoe UI", Font.BOLD, 13)); FontMetrics fm = g2.getFontMetrics();
                            g2.drawString(inicial, x + (size - fm.stringWidth(inicial)) / 2, y + ((size - fm.getHeight()) / 2) + fm.getAscent());
                        }
                    };
                    panel.setBackground(isSelected ? table.getSelectionBackground() : table.getBackground()); return panel;
                }
            });

            TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(mod); tab.setRowSorter(sorter);
            txtB.getDocument().addDocumentListener(new DocumentListener() {
                @Override public void insertUpdate(DocumentEvent e) { s(); } public void removeUpdate(DocumentEvent e) { s(); } @Override public void changedUpdate(DocumentEvent e) { s(); }
                private void s() { sorter.setRowFilter(RowFilter.regexFilter("(?i)" + txtB.getText().trim(), 2, 3)); }
            });
            tab.addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseClicked(java.awt.event.MouseEvent e) {
                    if (e.getClickCount() == 2) {
                        int r = tab.convertRowIndexToModel(tab.getSelectedRow());
                        idClienteActual = (int) mod.getValueAt(r, 0); lblClienteSeleccionado.setText((String) mod.getValueAt(r, 2)); dispose();
                    }
                }
            });

            mod.addRow(new Object[]{1, "C", "CONSUMIDOR FINAL", "0000-0000-00000", "N/A"});
            for (Cliente c : new ClienteDAO().listarClientesActivos()) {
                String nombreComp = c.getNombreCliente() + " " + (c.getApellidoCliente()!=null?c.getApellidoCliente():"");
                String inicial = nombreComp.isEmpty() ? "?" : nombreComp.substring(0, 1).toUpperCase();
                mod.addRow(new Object[]{c.getIdCliente(), inicial, nombreComp, c.getIdentidadCliente(), c.getTelefonoCliente()});
            }
            JScrollPane sc = new JScrollPane(tab); sc.setBorder(BorderFactory.createLineBorder(new Color(241, 245, 249))); sc.getViewport().setBackground(Color.WHITE); add(sc, BorderLayout.CENTER);
        }
    }

    // --- MINI BUSCADOR DE PRODUCTOS (CON ALERTA DE STOCK Y FILTRO DE ELIMINADOS) ---
    private class DialogoBuscarProductoPOS extends JDialog {
        public DialogoBuscarProductoPOS(Frame parent) {
            super(parent, "Cat\u00E1logo R\u00E1pido", true);
            setSize(600, 500); setLocationRelativeTo(parent); getContentPane().setBackground(utilidades.EfectosUI.COLOR_FONDO_PANEL); setLayout(new BorderLayout(10, 10));
            
            JPanel pnlTop = new JPanel(new BorderLayout()); pnlTop.setBackground(utilidades.EfectosUI.COLOR_FONDO_PANEL); pnlTop.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            JTextField txtB = new JTextField(); txtB.setBackground(new Color(255, 255, 255)); txtB.setForeground(new Color(45, 45, 45)); txtB.setCaretColor(new Color(45, 45, 45)); txtB.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(180, 208, 192)), BorderFactory.createEmptyBorder(0, 8, 0, 8))); txtB.setFont(new Font("Segoe UI", Font.PLAIN, 14)); txtB.putClientProperty("JTextField.placeholderText", "Buscar por Nombre o C\u00F3digo...");
            pnlTop.add(txtB, BorderLayout.CENTER); add(pnlTop, BorderLayout.NORTH);
            
            String[] cols = {"ID", "Foto", "C\u00F3digo", "Producto", "Precio", "Stock"};
            DefaultTableModel mod = new DefaultTableModel(null, cols) { @Override public boolean isCellEditable(int r, int c) { return false; } };
            
            // --- APLICACI\u00D3N DE COLORES EN ROJO SI EL STOCK ES 0 ---
            JTable tab = new JTable(mod) {
                @Override
                public Component prepareRenderer(javax.swing.table.TableCellRenderer renderer, int row, int column) {
                    Component c = super.prepareRenderer(renderer, row, column);
                    if (!isRowSelected(row)) {
                        Object valorStock = getValueAt(row, 5); // El stock est\u00E1 en la columna 5
                        int stock = 1;
                        if (valorStock != null) {
                            try { stock = Integer.parseInt(valorStock.toString()); } catch (NumberFormatException e) {}
                        }
                        if (stock <= 0) {
                            c.setForeground(new Color(227, 0, 15)); // Rojo Logo
                        } else {
                            c.setForeground(new Color(45, 45, 45)); // Gris Oscuro normal
                        }
                        c.setBackground(new Color(255, 255, 255));
                    }
                    return c;
                }
            };
            
            tab.setBackground(new Color(255, 255, 255)); tab.setForeground(new Color(45, 45, 45)); tab.setRowHeight(60); tab.setFont(new Font("Segoe UI", Font.PLAIN, 14)); tab.setSelectionBackground(new Color(205, 235, 218)); tab.setSelectionForeground(Color.BLACK);
            tab.getTableHeader().setBackground(utilidades.EfectosUI.COLOR_SIDEBAR_PASTEL); tab.getTableHeader().setForeground(utilidades.EfectosUI.COLOR_TEXTO_TITULO); tab.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, utilidades.EfectosUI.COLOR_BORDE)); tab.getTableHeader().setPreferredSize(new Dimension(0, 35));
            
            tab.getColumnModel().getColumn(0).setMinWidth(0); tab.getColumnModel().getColumn(0).setMaxWidth(0); 
            tab.getColumnModel().getColumn(1).setPreferredWidth(70); tab.getColumnModel().getColumn(1).setMaxWidth(70); 
            tab.getColumnModel().getColumn(1).setCellRenderer(new ImagenMiniaturaRenderer()); 
            
            TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(mod); tab.setRowSorter(sorter);
            txtB.getDocument().addDocumentListener(new DocumentListener() {
                @Override public void insertUpdate(DocumentEvent e) { s(); } public void removeUpdate(DocumentEvent e) { s(); } @Override public void changedUpdate(DocumentEvent e) { s(); }
                private void s() { sorter.setRowFilter(RowFilter.regexFilter("(?i)" + txtB.getText(), 2, 3)); }
            });
            
            List<Producto> lista = new InventarioDAO().listarProductosActivos();
            for (Producto p : lista) { 
                // FILTRO ESTRICTO: Si est\u00E1 eliminado l\u00F3gicamente (eliminado_producto == 1), NO se a\u00F1ade a la lista
                // Nota: Si tu m\u00E9todo en el modelo se llama diferente (ej: isEliminadoProducto()), ad\u00E1ptalo aqu\u00ED
                mod.addRow(new Object[]{p.getIdProducto(), p.getImagen_producto(), p.getCodigoBarras(), p.getNombreProducto(), String.format("L %,.2f", p.getPrecioVenta()), p.getStockProducto()}); 
            }
            
            tab.addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseClicked(java.awt.event.MouseEvent e) {
                    if (e.getClickCount() == 2) {
                        int filaModelo = tab.convertRowIndexToModel(tab.getSelectedRow());
                        int stockActual = (int) mod.getValueAt(filaModelo, 5);
                        
                        // --- BLOQUEO DE SELECCI\u00D3N SI EL STOCK ES 0 ---
                        if (stockActual <= 0) {
                            utilidades.Mensajes.showMessageDialog(DialogoBuscarProductoPOS.this, "No puede seleccionar este art\u00EDculo porque no cuenta con existencias en el inventario.", "Falta de Stock", JOptionPane.WARNING_MESSAGE);
                            return;
                        }
                        
                        int idSelec = (int) mod.getValueAt(filaModelo, 0);
                        Producto pSelec = lista.stream().filter(p -> p.getIdProducto() == idSelec).findFirst().orElse(null);
                        if(pSelec != null) agregarProductoAVenta(pSelec);
                        dispose();
                    }
                }
            });
            
            Timer localTimer = new Timer(30, e -> {
                if (!imagenesEnProceso.isEmpty() && tab.isShowing()) {
                    animacionShimmerPhase += 0.05f;
                    if (animacionShimmerPhase > 1f) animacionShimmerPhase = 0f;
                    tab.repaint();
                }
            });
            localTimer.start();
            
            addWindowListener(new java.awt.event.WindowAdapter() {
                @Override
                public void windowClosed(java.awt.event.WindowEvent e) {
                    localTimer.stop();
                }
            });

            JScrollPane sc = new JScrollPane(tab); sc.setBorder(BorderFactory.createLineBorder(new Color(220, 222, 225))); sc.getViewport().setBackground(new Color(255, 255, 255)); add(sc, BorderLayout.CENTER);
        }
    }
    
    private void mostrarZoomImagen(int idProducto) {
        String imgVal = new InventarioDAO().obtenerRutaImagenBase64(idProducto);
        if (imgVal == null || imgVal.trim().isEmpty()) {
            if (utilidades.SesionGlobal.getEmpresaActual() != null && utilidades.SesionGlobal.getEmpresaActual().getImagen_logo() != null) {
                imgVal = utilidades.SesionGlobal.getEmpresaActual().getImagen_logo();
            }
        }
        if (imgVal == null || imgVal.trim().isEmpty()) return;

        JDialog zoomDialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Previsualizaci\u00F3n", true);
        zoomDialog.setLayout(new BorderLayout()); zoomDialog.getContentPane().setBackground(utilidades.EfectosUI.COLOR_FONDO_PANEL); 
        int tamano = 600; zoomDialog.setSize(tamano, tamano);
        
        String valProcesar = imgVal;
        if (valProcesar.contains("|")) {
            valProcesar = valProcesar.split("\\|")[0];
        }
        
        ImageIcon iconoZoom = utilidades.ImagenHelper.obtenerIcono(valProcesar, tamano - 40, tamano - 40);
        if (iconoZoom != null) {
            JLabel lblZoom = new JLabel(iconoZoom); lblZoom.setHorizontalAlignment(SwingConstants.CENTER);
            zoomDialog.add(lblZoom, BorderLayout.CENTER); zoomDialog.setLocationRelativeTo(this); zoomDialog.setVisible(true);
        }
    }
    
    // =========================================================
    // MEN\u00DA CONTEXTUAL DE OPCIONES
    // =========================================================
    private void mostrarMenuOpciones(Component componente, int x, int y) {
        JPopupMenu menu = new JPopupMenu();
        menu.setBackground(new Color(255, 255, 255)); // Blanco puro
        menu.setBorder(BorderFactory.createLineBorder(new Color(220, 222, 225), 1));

        // Inyectamos los nuevos \u00EDconos espec\u00EDficos para cada acci\u00F3n
        JMenuItem itemModPrecio = crearMenuItem("Modificar Precio", new Color(13, 110, 253), new IconoPrecio());
        JMenuItem itemModCant = crearMenuItem("Modificar Cantidad", new Color(39, 174, 96), new IconoCantidad());
        JMenuItem itemQuitar = crearMenuItem("Quitar Producto", new Color(227, 0, 15), new IconoBasurero());

        itemModPrecio.addActionListener(e -> modificarPrecio());
        itemModCant.addActionListener(e -> modificarCantidad());
        itemQuitar.addActionListener(e -> quitarProducto());

        if (memoriaMayoristaActivo && !memoriaMayoristaGlobal) {
            JMenuItem itemAlternarPrecio = crearMenuItem("Alternar Mayorista", new Color(255, 140, 0), new IconoPrecio());
            itemAlternarPrecio.addActionListener(e -> alternarPrecioMayoristaPorItem());
            menu.add(itemAlternarPrecio);
            menu.addSeparator();
        }

        menu.add(itemModPrecio);
        menu.add(itemModCant);
        menu.addSeparator(); 
        menu.add(itemQuitar);
        
        menu.show(componente, x, y);
    }

    private void alternarPrecioMayoristaPorItem() {
        int f = tablaVentas.getSelectedRow();
        if (f < 0) return;
        
        double precioNormal = (double) modeloTablaVentas.getValueAt(f, 11);
        double precioMayorista = (double) modeloTablaVentas.getValueAt(f, 12);
        double precioActual = (double) modeloTablaVentas.getValueAt(f, 4);
        
        if (precioMayorista <= 0) {
            utilidades.Mensajes.showMessageDialog(this, "Este producto no tiene precio mayorista configurado.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        double nuevoPrecio = (precioActual == precioNormal) ? precioMayorista : precioNormal;
        int cant = (int) modeloTablaVentas.getValueAt(f, 3);
        modeloTablaVentas.setValueAt(nuevoPrecio, f, 4);
        modeloTablaVentas.setValueAt(nuevoPrecio * cant, f, 5);
        recalcularTotales();
    }
    
    private JMenuItem crearMenuItem(String texto, Color colorHover, Icon icono) {
        JMenuItem item = new JMenuItem(texto);
        item.setIcon(icono);
        item.setIconTextGap(12);
        item.setFont(new Font("Segoe UI", Font.BOLD, 14));
        item.setForeground(new Color(45, 45, 45));
        item.setBackground(new Color(255, 255, 255));
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

    // =========================================================
    // \u00CDCONOS VECTORIALES ESPEC\u00CDFICOS (JAVA 2D)
    // =========================================================
    private class IconoPrecio implements Icon {
        @Override public int getIconWidth() { return 20; }
        @Override public int getIconHeight() { return 20; }
        @Override public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(c.getForeground()); // Color din\u00E1mico
            g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

            // Dibujo de una Etiqueta de Precio detallada
            g2.drawRoundRect(x + 2, y + 4, 16, 12, 3, 3); // Cuerpo de la etiqueta
            g2.drawOval(x + 5, y + 8, 4, 4); // Orificio para el cord\u00F3n
            
            // L\u00EDneas horizontales sim\u00E9tricas que simulan el valor/c\u00F3digo de barras
            g2.setStroke(new BasicStroke(1.5f));
            g2.drawLine(x + 12, y + 7, x + 15, y + 7);
            g2.drawLine(x + 11, y + 11, x + 15, y + 11);
            g2.dispose();
        }
    }

    private class IconoCantidad implements Icon {
        @Override public int getIconWidth() { return 20; }
        @Override public int getIconHeight() { return 20; }
        @Override public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(c.getForeground()); // Color din\u00E1mico
            g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

            // Dibujo de dos Cajas Apiladas (Representa inventario f\u00EDsico/unidades)
            // 1. Caja de atr\u00E1s (arriba a la derecha)
            g2.drawRect(x + 7, y + 3, 10, 9);
            
            // Mascara de fondo claro intermedio para evitar que las l\u00EDneas se crucen feo
            g2.setColor(c.getBackground());
            g2.fillRect(x + 3, y + 8, 10, 9);
            
            // 2. Caja de adelante (abajo a la izquierda)
            g2.setColor(c.getForeground());
            g2.drawRect(x + 3, y + 8, 10, 9);
            
            // Detalle: Cinta de empaque de la caja frontal
            g2.setStroke(new BasicStroke(1.2f));
            g2.drawLine(x + 3, y + 12, x + 13, y + 12);
            g2.dispose();
        }
    }

    private class IconoBasurero implements Icon {
        @Override public int getIconWidth() { return 20; }
        @Override public int getIconHeight() { return 20; }
        @Override public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(c.getForeground()); // Color din\u00E1mico (cambiar\u00E1 de texto oscuro a texto blanco sobre fondo rojo)
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
    @SuppressWarnings("unused")
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

