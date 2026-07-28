package gui;

import dao.ClienteDAO;
import dao.InventarioDAO;
import dao.VentasDAO;
import dao.ApartadoDAO;
import modelo.Cliente;
import modelo.Producto;
import modelo.Apartado;
import modelo.DetalleApartado;
import utilidades.SesionGlobal;

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
    }

    private void iniciarDiseno() {
        this.setLayout(new BorderLayout(20, 20));
        this.setBackground(new Color(240, 242, 245)); // Gris Nube
        this.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        JPanel pnlTop = new JPanel(new BorderLayout(15, 0)); pnlTop.setOpaque(false);
        JPanel pnlClientes = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0)); pnlClientes.setOpaque(false);
        JLabel lblClie = new JLabel("Cliente:"); lblClie.setForeground(new Color(140, 145, 150)); lblClie.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblClienteSeleccionado = new JLabel("CONSUMIDOR FINAL"); lblClienteSeleccionado.setForeground(new Color(45, 45, 45)); lblClienteSeleccionado.setFont(new Font("Segoe UI", Font.BOLD, 14));
        
        JButton btnBuscarCliente = new JButton("🔍 Buscar"); btnBuscarCliente.setBackground(new Color(255, 255, 255)); btnBuscarCliente.setForeground(new Color(45, 45, 45)); btnBuscarCliente.setFocusPainted(false); btnBuscarCliente.setCursor(new Cursor(Cursor.HAND_CURSOR)); btnBuscarCliente.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(220, 222, 225)), BorderFactory.createEmptyBorder(5, 10, 5, 10)));
        btnBuscarCliente.addActionListener(e -> new DialogoBuscarClientePOS((Frame) SwingUtilities.getWindowAncestor(this)).setVisible(true));
        
        JButton btnNuevoCliente = new JButton("+ Nuevo"); btnNuevoCliente.setBackground(new Color(39, 174, 96)); btnNuevoCliente.setForeground(Color.WHITE); btnNuevoCliente.setFocusPainted(false); btnNuevoCliente.setCursor(new Cursor(Cursor.HAND_CURSOR)); btnNuevoCliente.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
        btnNuevoCliente.addActionListener(e -> {
            JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Nuevo Cliente", true);
            dialog.setUndecorated(true); dialog.setBackground(new Color(0,0,0,0));
            dialog.add(new PanelFormularioCliente(dialog, new PanelGestionClientes(), null));
            dialog.pack(); dialog.setLocationRelativeTo(this); dialog.setVisible(true);
        });

        pnlClientes.add(lblClie); pnlClientes.add(lblClienteSeleccionado); pnlClientes.add(btnBuscarCliente); pnlClientes.add(btnNuevoCliente);

        JPanel pnlLector = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0)); pnlLector.setOpaque(false);
        txtCodigoBarrasBusqueda = new JTextField(15); txtCodigoBarrasBusqueda.putClientProperty("JTextField.placeholderText", "Escanear código..."); txtCodigoBarrasBusqueda.setFont(new Font("Segoe UI", Font.PLAIN, 14)); txtCodigoBarrasBusqueda.setPreferredSize(new Dimension(220, 35)); txtCodigoBarrasBusqueda.setBackground(new Color(255, 255, 255)); txtCodigoBarrasBusqueda.setForeground(new Color(45, 45, 45)); txtCodigoBarrasBusqueda.setCaretColor(new Color(45, 45, 45)); txtCodigoBarrasBusqueda.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(220, 222, 225)), BorderFactory.createEmptyBorder(0, 8, 0, 8)));
        txtCodigoBarrasBusqueda.addActionListener(e -> buscarProductoPorCodigo(txtCodigoBarrasBusqueda.getText().trim()));
        
        JButton btnBuscarProducto = new JButton("Catálogo"); btnBuscarProducto.setBackground(new Color(13, 110, 253)); btnBuscarProducto.setForeground(Color.WHITE); btnBuscarProducto.setFont(new Font("Segoe UI", Font.BOLD, 12)); btnBuscarProducto.setPreferredSize(new Dimension(100, 35)); btnBuscarProducto.setFocusPainted(false); btnBuscarProducto.setCursor(new Cursor(Cursor.HAND_CURSOR)); btnBuscarProducto.setBorder(BorderFactory.createEmptyBorder(0,0,0,0));
        btnBuscarProducto.addActionListener(e -> new DialogoBuscarProductoPOS((Frame) SwingUtilities.getWindowAncestor(this)).setVisible(true));

        pnlLector.add(txtCodigoBarrasBusqueda); pnlLector.add(btnBuscarProducto);
        pnlTop.add(pnlClientes, BorderLayout.WEST); pnlTop.add(pnlLector, BorderLayout.EAST);
        this.add(pnlTop, BorderLayout.NORTH);

        String[] columnas = {"ID", "Foto", "Nombre del Producto", "Cant.", "Precio Unit.", "Subtotal Fila", "StockMax", "RutaFoto", "IMEI", "DiasGarantia"};
        modeloTablaVentas = new DefaultTableModel(null, columnas) { @Override public boolean isCellEditable(int row, int column) { return false; } };
        tablaVentas = new JTable(modeloTablaVentas); 
        tablaVentas.setShowGrid(false); tablaVentas.setIntercellSpacing(new Dimension(0, 0)); tablaVentas.setRowHeight(60); tablaVentas.setBackground(new Color(255, 255, 255)); tablaVentas.setForeground(new Color(45, 45, 45)); tablaVentas.setFont(new Font("Segoe UI", Font.PLAIN, 14)); tablaVentas.setSelectionBackground(new Color(230, 235, 240)); tablaVentas.setSelectionForeground(new Color(45, 45, 45));
        tablaVentas.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14)); tablaVentas.getTableHeader().setBackground(new Color(240, 242, 245)); tablaVentas.getTableHeader().setForeground(new Color(100, 100, 100)); tablaVentas.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(220, 222, 225)));
        tablaVentas.getTableHeader().setPreferredSize(new Dimension(tablaVentas.getTableHeader().getPreferredSize().width, 40));
        
        tablaVentas.getColumnModel().getColumn(0).setMinWidth(0); tablaVentas.getColumnModel().getColumn(0).setMaxWidth(0); 
        tablaVentas.getColumnModel().getColumn(6).setMinWidth(0); tablaVentas.getColumnModel().getColumn(6).setMaxWidth(0); 
        tablaVentas.getColumnModel().getColumn(7).setMinWidth(0); tablaVentas.getColumnModel().getColumn(7).setMaxWidth(0); 
        tablaVentas.getColumnModel().getColumn(8).setMinWidth(0); tablaVentas.getColumnModel().getColumn(8).setMaxWidth(0);
        tablaVentas.getColumnModel().getColumn(9).setMinWidth(0); tablaVentas.getColumnModel().getColumn(9).setMaxWidth(0);
        
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
                        mostrarZoomImagen((String) modeloTablaVentas.getValueAt(tablaVentas.convertRowIndexToModel(f), 7));
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

        JPanel pnlControlVenta = new JPanel(new BorderLayout(20, 20)); pnlControlVenta.setOpaque(false); pnlControlVenta.setPreferredSize(new Dimension(300, 0));

        JPanel pnlLiquidacion = new JPanel(new GridBagLayout()); pnlLiquidacion.setBackground(new Color(255, 255, 255)); pnlLiquidacion.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(220, 222, 225), 1, true), BorderFactory.createEmptyBorder(15, 15, 15, 15)));
        GridBagConstraints gbc = new GridBagConstraints(); gbc.fill = GridBagConstraints.HORIZONTAL; gbc.gridx = 0; gbc.weightx = 1.0; gbc.insets = new Insets(5, 0, 5, 0);

        cmbMetodoPago = new JComboBox<>(); cmbMetodoPago.setFont(new Font("Segoe UI", Font.PLAIN, 14)); cmbMetodoPago.setBackground(new Color(255, 255, 255));
        
        pnlCamposExtraPago = new JPanel(new GridLayout(4, 1, 0, 5)); pnlCamposExtraPago.setOpaque(false);
        pnlCamposExtraPago.setVisible(false);
        
        JLabel lblBanco = new JLabel("Banco Emisor:"){{ setForeground(new Color(100, 100, 100)); setFont(new Font("Segoe UI", Font.BOLD, 12)); }};
        String[] bancosHonduras = {"Seleccione Banco...", "Banco Atlántida", "BAC Credomatic", "Banco Ficohsa", "Banpaís", "Banco de Occidente", "Banco Banrural", "Banco Promerica", "Banco LAFISE", "Banco FICENSA", "Banco BANHCAFE", "ACH - Transferencia Interbancaria"};
        cmbBanco = new JComboBox<>(bancosHonduras); cmbBanco.setFont(new Font("Segoe UI", Font.PLAIN, 14)); cmbBanco.setBackground(new Color(255, 255, 255));
        
        lblReferenciaPago = new JLabel("Nº Referencia:"){{ setForeground(new Color(100, 100, 100)); setFont(new Font("Segoe UI", Font.BOLD, 12)); }};
        txtReferenciaPago = new JTextField(); txtReferenciaPago.setFont(new Font("Segoe UI", Font.PLAIN, 14)); txtReferenciaPago.setBackground(new Color(255, 255, 255)); txtReferenciaPago.setForeground(new Color(45, 45, 45)); txtReferenciaPago.setCaretColor(new Color(45, 45, 45)); txtReferenciaPago.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(220, 222, 225)), BorderFactory.createEmptyBorder(0, 5, 0, 5))); txtReferenciaPago.setPreferredSize(new Dimension(0, 32));
        
        pnlCamposExtraPago.add(lblBanco); pnlCamposExtraPago.add(cmbBanco); pnlCamposExtraPago.add(lblReferenciaPago); pnlCamposExtraPago.add(txtReferenciaPago);

        cmbMetodoPago.addActionListener(e -> {
            ItemPago pagoSeleccionado = (ItemPago) cmbMetodoPago.getSelectedItem();
            if (pagoSeleccionado != null) {
                String nombrePago = pagoSeleccionado.nombre.toLowerCase();
                if (nombrePago.contains("transferencia") || nombrePago.contains("tarjeta")) {
                    lblReferenciaPago.setText(nombrePago.contains("transferencia") ? "Nº Referencia / ACH:" : "Nº Voucher / Referencia:");
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
        lblTotal = new JLabel("L 0.00"); lblTotal.setForeground(new Color(39, 174, 96)); lblTotal.setFont(new Font("Segoe UI", Font.BOLD, 36));

        JButton btnCobrar = new JButton("Cobrar Venta"); btnCobrar.setBackground(new Color(13, 110, 253)); btnCobrar.setForeground(Color.WHITE); btnCobrar.setFont(new Font("Segoe UI", Font.BOLD, 16)); btnCobrar.setPreferredSize(new Dimension(0, 50)); btnCobrar.setFocusPainted(false); btnCobrar.setCursor(new Cursor(Cursor.HAND_CURSOR)); btnCobrar.setBorder(BorderFactory.createEmptyBorder(0,0,0,0)); btnCobrar.addActionListener(e -> procesarVenta());

        cmbTipoTransaccion = new JComboBox<>(new String[]{"Venta Directa", "Apartado (Abonos)"});
        cmbTipoTransaccion.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        cmbTipoTransaccion.setBackground(Color.WHITE);
        cmbTipoTransaccion.addActionListener(e -> {
            boolean esApartado = cmbTipoTransaccion.getSelectedIndex() == 1;
            btnCobrar.setText(esApartado ? "Registrar Apartado" : "Cobrar Venta");
        });

        gbc.gridy = 0; pnlLiquidacion.add(new JLabel("Tipo Transacción:"){{setForeground(new Color(100, 100, 100));}}, gbc);
        gbc.gridy = 1; pnlLiquidacion.add(cmbTipoTransaccion, gbc);
        gbc.gridy = 2; gbc.insets = new Insets(10, 0, 5, 0); pnlLiquidacion.add(new JLabel("Método de Pago:"){{setForeground(new Color(100, 100, 100));}}, gbc);
        gbc.gridy = 3; pnlLiquidacion.add(cmbMetodoPago, gbc);
        gbc.gridy = 4; gbc.insets = new Insets(5, 0, 5, 0); pnlLiquidacion.add(pnlCamposExtraPago, gbc);
        gbc.gridy = 5; gbc.insets = new Insets(15, 0, 0, 0); pnlLiquidacion.add(lblSubtotal, gbc);
        gbc.gridy = 6; gbc.insets = new Insets(5, 0, 0, 0); pnlLiquidacion.add(lblImpuesto, gbc);
        gbc.gridy = 7; gbc.insets = new Insets(15, 0, 0, 0); pnlLiquidacion.add(new JLabel("TOTAL:"){{setForeground(new Color(45, 45, 45)); setFont(new Font("Segoe UI", Font.BOLD, 14));}}, gbc);
        gbc.gridy = 8; gbc.insets = new Insets(0, 0, 20, 0); pnlLiquidacion.add(lblTotal, gbc);
        gbc.gridy = 9; pnlLiquidacion.add(btnCobrar, gbc);
        pnlControlVenta.add(pnlLiquidacion, BorderLayout.NORTH);
        this.add(pnlControlVenta, BorderLayout.EAST);
    }

    private void cargarMetodosPago() {
        cmbMetodoPago.removeAllItems(); 
        VentasDAO dao = new VentasDAO();
        Map<Integer, String> metodos = dao.obtenerMetodosPago();
        
        ItemPago itemEfectivo = null; // Variable para atrapar la opción de Efectivo
        
        for (Map.Entry<Integer, String> entry : metodos.entrySet()) {
            ItemPago item = new ItemPago(entry.getKey(), entry.getValue());
            cmbMetodoPago.addItem(item);
            
            // Verificamos si este método es el Efectivo
            if (item.nombre.toLowerCase().contains("efectivo")) {
                itemEfectivo = item;
            }
        }
        
        // Si encontramos la opción de Efectivo, la fijamos como predeterminada
        if (itemEfectivo != null) {
            cmbMetodoPago.setSelectedItem(itemEfectivo);
        }
    }

    private void recalcularTotales() {
        granTotal = 0.0;
        for (int i = 0; i < modeloTablaVentas.getRowCount(); i++) granTotal += (double) modeloTablaVentas.getValueAt(i, 5);

        ItemPago pagoSeleccionado = (ItemPago) cmbMetodoPago.getSelectedItem();
        boolean pagoConTarjeta = pagoSeleccionado != null && pagoSeleccionado.nombre.toLowerCase().contains("tarjeta");

        if (facturacionHabilitada || pagoConTarjeta) {
            sumSubtotal = granTotal / 1.15;
            sumImpuesto = granTotal - sumSubtotal;
        } else {
            sumSubtotal = granTotal;
            sumImpuesto = 0.0;
        }

<<<<<<< HEAD
        lblSubtotal.setText(String.format("Subtotal: L %.2f", sumSubtotal));
        lblImpuesto.setText(String.format("ISV (15%%): L %.2f", sumImpuesto));
        lblTotal.setText(String.format("L %.2f", granTotal));
=======
        lblSubtotal.setText(String.format("Subtotal: L %,.2f", sumSubtotal));
        lblImpuesto.setText(String.format("ISV (15%%): L %,.2f", sumImpuesto));
        lblTotal.setText(String.format("L %,.2f", granTotal));
>>>>>>> origin/parte-muoz
    }

   // --- RESTRICCIÓN DE STOCK Y VALIDACIÓN DE SERIE ---
    public void agregarProductoAVenta(Producto p) {
        if (p.getStockProducto() < 1) { 
            JOptionPane.showMessageDialog(this, "Stock agotado. No hay unidades disponibles en vitrina.", "Stock Insuficiente", JOptionPane.WARNING_MESSAGE); 
            return; 
        }

        String imei = null;
        if (p.isRequiereSerie()) {
            imei = JOptionPane.showInputDialog(this, "Este producto requiere un identificador (IMEI/ServiceTag).\nIngrese el código para: " + p.getNombreProducto(), "Validación de Garantía", JOptionPane.QUESTION_MESSAGE);
            
            if (imei == null || imei.trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Operación cancelada. Debe ingresar el IMEI/Serie para facturar este producto.", "Aviso", JOptionPane.WARNING_MESSAGE);
                return;
            }
            imei = imei.trim();

            // --- VALIDACIÓN 1: EVITAR IDENTIFICADOR DUPLICADO EN LA VENTA ACTUAL ---
            for (int i = 0; i < modeloTablaVentas.getRowCount(); i++) {
                Object valorImeiFila = modeloTablaVentas.getValueAt(i, 8); 
                if (valorImeiFila != null && valorImeiFila.toString().equalsIgnoreCase(imei)) {
                    JOptionPane.showMessageDialog(this, "El identificador '" + imei + "' ya está en la lista de compras actual.", "Identificador Duplicado", JOptionPane.ERROR_MESSAGE);
                    return; 
                }
            }
            
            // --- VALIDACIÓN 2: EVITAR IDENTIFICADOR QUE YA FUE VENDIDO ANTERIORMENTE ---
            VentasDAO daoVentas = new VentasDAO();
            if (daoVentas.existeIdentificadorVendido(imei)) {
                JOptionPane.showMessageDialog(this, "¡ALERTA! El identificador '" + imei + "' ya se encuentra registrado como VENDIDO en la base de datos.\nVerifique el equipo físico o contacte al administrador.", "Fraude / Error Detectado", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

<<<<<<< HEAD
        // Si NO requiere serie, agrupamos las cantidades si ya está en la tabla
        if (!p.isRequiereSerie()) {
=======
        // Si NO requiere serie Y NO TIENE GARANTÍA, agrupamos las cantidades si ya está en la tabla
        if (!p.isRequiereSerie() && p.getDiasGarantia() == 0) {
>>>>>>> origin/parte-muoz
            for (int i = 0; i < modeloTablaVentas.getRowCount(); i++) {
                if ((int) modeloTablaVentas.getValueAt(i, 0) == p.getIdProducto()) {
                    int cantActual = (int) modeloTablaVentas.getValueAt(i, 3);
                    if (cantActual >= p.getStockProducto()) {
                        JOptionPane.showMessageDialog(this, "Stock insuficiente. Solo hay " + p.getStockProducto() + " unidades en vitrina.", "Aviso", JOptionPane.WARNING_MESSAGE); 
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
        modeloTablaVentas.addRow(new Object[]{ 
            p.getIdProducto(), p.getRutaImagen(), p.getNombreProducto(), 1, 
            p.getPrecioVenta(), p.getPrecioVenta(), p.getStockProducto(), p.getRutaImagen(), 
            imei, p.getDiasGarantia() 
        });
        recalcularTotales();
    }

    private void buscarProductoPorCodigo(String codigo) {
        if(codigo.isEmpty()) return;
        VentasDAO dao = new VentasDAO();
        Producto p = dao.buscarProductoPorCodigo(codigo);
        if (p != null) agregarProductoAVenta(p);
        else JOptionPane.showMessageDialog(this, "Producto no encontrado o inactivo.", "Aviso", JOptionPane.WARNING_MESSAGE);
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
                // Si la tecla NO es un número, NO es un punto y NO es la tecla de borrar -> la bloqueamos
                if (!Character.isDigit(c) && c != '.' && c != java.awt.event.KeyEvent.VK_BACK_SPACE) {
                    e.consume(); // Anula la pulsación
                }
                // Si ya hay un punto decimal y el usuario intenta poner otro -> lo bloqueamos
                if (c == '.' && txtNuevoPrecio.getText().contains(".")) {
                    e.consume(); 
                }
            }
        });

        // Mostramos el cuadro de diálogo con nuestro JTextField protegido
        int opcion = JOptionPane.showConfirmDialog(this, new Object[]{"Nuevo Precio Unitario:", txtNuevoPrecio}, "Modificar Precio", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        
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
        
<<<<<<< HEAD
        // --- BLOQUEO DE SEGURIDAD PARA GARANTÍAS ---
        if (modeloTablaVentas.getValueAt(f, 8) != null) {
            JOptionPane.showMessageDialog(this, "No puede modificar la cantidad de un equipo que requiere Identificador.\nSi el cliente lleva varios, escanee el producto nuevamente para registrar el otro identificador.", "Acción Bloqueada", JOptionPane.WARNING_MESSAGE);
=======
        // --- BLOQUEO DE SEGURIDAD PARA GARANTÍAS Y SERIES ---
        if (modeloTablaVentas.getValueAt(f, 9) != null || (int)modeloTablaVentas.getValueAt(f, 8) > 0) {
            JOptionPane.showMessageDialog(this, "No puede modificar la cantidad de un equipo que requiere Identificador o posee Garantía.\nSi el cliente lleva varios, escanee el producto nuevamente.", "Acción Bloqueada", JOptionPane.WARNING_MESSAGE);
>>>>>>> origin/parte-muoz
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

        int opcion = JOptionPane.showConfirmDialog(this, new Object[]{"Nueva Cantidad (Stock Disponible: " + stockMaximo + "):", txtNuevaCant}, "Modificar Cantidad", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        
        if (opcion == JOptionPane.OK_OPTION && !txtNuevaCant.getText().isEmpty()) {
            try {
                int nuevaCant = Integer.parseInt(txtNuevaCant.getText());
                if (nuevaCant <= 0) { quitarProducto(); return; }
                if (nuevaCant > stockMaximo) {
                    JOptionPane.showMessageDialog(this, "No puede facturar " + nuevaCant + " unidades. Solo hay " + stockMaximo + " en stock.", "Error", JOptionPane.ERROR_MESSAGE); 
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

    private void procesarVenta() {
        if(modeloTablaVentas.getRowCount() == 0) { JOptionPane.showMessageDialog(this, "La venta está vacía.", "Aviso", JOptionPane.WARNING_MESSAGE); return; }
        
        // Validar que la caja esté abierta
        modelo.ControlCaja CCActiva = new dao.ControlCajaDAO().obtenerSesionActiva();
        if (CCActiva == null) {
            JOptionPane.showMessageDialog(this, "Operación denegada: El turno de caja no está abierto. Abra la caja antes de registrar transacciones.", "Caja Cerrada", JOptionPane.ERROR_MESSAGE);
            return;
        }

        VentasDAO dao = new VentasDAO();
        ItemPago pago = (ItemPago) cmbMetodoPago.getSelectedItem();
        if(pago == null) return;

        String refPago = null;
        String bancoSeleccionado = null;
        if (pnlCamposExtraPago.isVisible()) {
            if (cmbBanco.getSelectedIndex() == 0) {
                JOptionPane.showMessageDialog(this, "Debe seleccionar el Banco Emisor para autorizar esta transacción.", "Datos Incompletos", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (txtReferenciaPago.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Ingrese el número de Referencia, Voucher o ACH del pago.", "Datos Incompletos", JOptionPane.WARNING_MESSAGE);
                return;
            }
            refPago = txtReferenciaPago.getText().trim();
            bancoSeleccionado = cmbBanco.getSelectedItem().toString();
        }

        boolean esApartado = cmbTipoTransaccion.getSelectedIndex() == 1;

        if (esApartado) {
            JTextField txtAbonoInicial = new JTextField("0.00");
<<<<<<< HEAD
            JTextField txtDiasPlazo = new JTextField("30");
            Object[] fields = {
                "Total de la Compra: L " + String.format("%.2f", granTotal),
=======
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
>>>>>>> origin/parte-muoz
                "Abono Inicial (L):", txtAbonoInicial,
                "Plazo de Pago (Días):", txtDiasPlazo
            };

            int opApartado = JOptionPane.showConfirmDialog(this, fields, "Detalles del Apartado", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (opApartado != JOptionPane.OK_OPTION) return;

            try {
                double abono = Double.parseDouble(txtAbonoInicial.getText().trim());
                int dias = Integer.parseInt(txtDiasPlazo.getText().trim());

                if (abono < 0 || abono > granTotal) {
                    JOptionPane.showMessageDialog(this, "El abono inicial debe ser menor o igual al total.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                if (dias <= 0) {
                    JOptionPane.showMessageDialog(this, "El plazo en días debe ser mayor a 0.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // Password signature
                JPasswordField pfPass = new JPasswordField();
                int opSign = JOptionPane.showConfirmDialog(this, new Object[]{"Ingrese su contraseña para firmar el apartado:", pfPass}, "Firma Requerida", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
                if (opSign != JOptionPane.OK_OPTION) return;

                String pass = new String(pfPass.getPassword());
                int idUsuarioAutorizado = dao.obtenerIdUsuarioPorPassword(pass);

                if (idUsuarioAutorizado <= 0) {
                    JOptionPane.showMessageDialog(this, "Contraseña incorrecta o usuario inactivo.", "Acceso Denegado", JOptionPane.ERROR_MESSAGE);
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
                if (apDAO.registrarApartado(a, detallesA)) {
                    // Generar PDF
                    txtReferenciaPago.setText(""); cmbBanco.setSelectedIndex(0); pnlCamposExtraPago.setVisible(false);
                    JFileChooser chooser = new JFileChooser();
                    chooser.setDialogTitle("Guardar Comprobante de Apartado");
                    chooser.setSelectedFile(new File("Apartado_Nexar_" + System.currentTimeMillis() + ".pdf"));

                    if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                        File archivoDestino = chooser.getSelectedFile();
                        if (!archivoDestino.getName().toLowerCase().endsWith(".pdf")) archivoDestino = new File(archivoDestino.getAbsolutePath() + ".pdf");

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
                            JOptionPane.showMessageDialog(this, "Apartado registrado con éxito.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                            if (Desktop.isDesktopSupported()) Desktop.getDesktop().open(archivoDestino);
                        } catch (Exception ex) {
                            JOptionPane.showMessageDialog(this, "El apartado se guardó, pero hubo un error al generar el PDF:\n" + ex.getMessage(), "Aviso", JOptionPane.WARNING_MESSAGE);
                        }
                    }

                    modeloTablaVentas.setRowCount(0); recalcularTotales();
                    lblClienteSeleccionado.setText("CONSUMIDOR FINAL"); idClienteActual = 1;
                } else {
                    JOptionPane.showMessageDialog(this, "Error crítico al guardar el apartado en la base de datos.", "Error", JOptionPane.ERROR_MESSAGE);
                }

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Verifique los valores numéricos ingresados.", "Error", JOptionPane.ERROR_MESSAGE);
            }
            return;
        }

        JPasswordField pfPass = new JPasswordField();
        int opcion = JOptionPane.showConfirmDialog(this, new Object[]{"Ingrese su contraseña de cajero para autorizar la venta:", pfPass}, "Autorizar Cobro", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (opcion != JOptionPane.OK_OPTION) return;

        String pass = new String(pfPass.getPassword());
        int idUsuarioAutorizado = dao.obtenerIdUsuarioPorPassword(pass);

        if (idUsuarioAutorizado <= 0) {
            JOptionPane.showMessageDialog(this, "Contraseña incorrecta o usuario inactivo.", "Acceso Denegado", JOptionPane.ERROR_MESSAGE);
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

        if (dao.procesarVentaCompleta(idClienteActual, idUsuarioAutorizado, pago.id, sumSubtotal, sumImpuesto, granTotal, refPago, bancoSeleccionado, detalles)) {
            
            txtReferenciaPago.setText(""); cmbBanco.setSelectedIndex(0); pnlCamposExtraPago.setVisible(false);
            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("Guardar e Imprimir Comprobante");
            chooser.setSelectedFile(new File("Factura_Nexar_" + System.currentTimeMillis() + ".pdf"));

            if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                File archivoDestino = chooser.getSelectedFile();
                if (!archivoDestino.getName().toLowerCase().endsWith(".pdf")) archivoDestino = new File(archivoDestino.getAbsolutePath() + ".pdf");

               try {
                    // Generamos la fecha exacta del momento en que se presiona el botón cobrar
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
                    JOptionPane.showMessageDialog(this, "Venta registrada y comprobante generado con éxito.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                    if (Desktop.isDesktopSupported()) Desktop.getDesktop().open(archivoDestino);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "La venta se guardó, pero hubo un error al generar el PDF:\n" + ex.getMessage(), "Aviso", JOptionPane.WARNING_MESSAGE);
                }
            }

            modeloTablaVentas.setRowCount(0); recalcularTotales();
            lblClienteSeleccionado.setText("CONSUMIDOR FINAL"); idClienteActual = 1;

        } else {
            JOptionPane.showMessageDialog(this, "Error crítico al guardar la venta en base de datos.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private class ItemPago {
        int id; String nombre;
        public ItemPago(int id, String nombre) { this.id = id; this.nombre = nombre; }
        @Override public String toString() { return nombre; }
    }

    private class ImagenMiniaturaRenderer extends DefaultTableCellRenderer {
        @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel label = new JLabel(); label.setHorizontalAlignment(SwingConstants.CENTER); label.setOpaque(true); label.setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
            String imgVal = (value != null) ? value.toString() : null;
            
            if (imgVal == null || imgVal.trim().isEmpty()) {
                if (utilidades.SesionGlobal.getEmpresaActual() != null && utilidades.SesionGlobal.getEmpresaActual().getLogoEmpresaRuta() != null) {
                    imgVal = utilidades.SesionGlobal.getEmpresaActual().getLogoEmpresaRuta();
                }
            }
            
            ImageIcon icon = utilidades.ImagenHelper.obtenerIcono(imgVal, 50, 50);
            if (icon != null) {
                label.setIcon(icon);
            } else { label.setText("No Img"); label.setForeground(new Color(140, 145, 150)); }
            return label;
        }
    }

    private class DialogoBuscarClientePOS extends JDialog {
        public DialogoBuscarClientePOS(Frame parent) {
            super(parent, "Seleccionar Cliente", true);
            setSize(700, 500); setLocationRelativeTo(parent); getContentPane().setBackground(new Color(240, 242, 245)); setLayout(new BorderLayout(10, 10));
            
            JPanel pnlTop = new JPanel(new BorderLayout()); pnlTop.setBackground(new Color(240, 242, 245)); pnlTop.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            JTextField txtB = new JTextField(); txtB.setBackground(new Color(255, 255, 255)); txtB.setForeground(new Color(45, 45, 45)); txtB.setCaretColor(new Color(45, 45, 45)); txtB.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(220, 222, 225)), BorderFactory.createEmptyBorder(0, 5, 0, 5))); txtB.setFont(new Font("Segoe UI", Font.PLAIN, 14)); txtB.putClientProperty("JTextField.placeholderText", "Buscar por Nombre o Identidad...");
            pnlTop.add(txtB, BorderLayout.CENTER); add(pnlTop, BorderLayout.NORTH);
            
            String[] cols = {"ID", "Avatar", "Nombre Completo", "Identidad", "Teléfono"};
            DefaultTableModel mod = new DefaultTableModel(null, cols) { @Override public boolean isCellEditable(int r, int c) { return false; } };
            JTable tab = new JTable(mod); tab.setBackground(new Color(255, 255, 255)); tab.setForeground(new Color(45, 45, 45)); tab.setRowHeight(45); tab.setFont(new Font("Segoe UI", Font.PLAIN, 14)); tab.setSelectionBackground(new Color(230, 235, 240)); tab.setSelectionForeground(new Color(45, 45, 45));
            tab.getTableHeader().setBackground(new Color(240, 242, 245)); tab.getTableHeader().setForeground(new Color(100, 100, 100)); tab.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(220, 222, 225))); tab.getTableHeader().setPreferredSize(new Dimension(0, 35));
            
            tab.getColumnModel().getColumn(0).setMinWidth(0); tab.getColumnModel().getColumn(0).setMaxWidth(0); 
            tab.getColumnModel().getColumn(1).setPreferredWidth(50); tab.getColumnModel().getColumn(1).setMaxWidth(50);
            
            tab.getColumnModel().getColumn(1).setCellRenderer(new DefaultTableCellRenderer() {
                @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                    JPanel panel = new JPanel() {
                        @Override protected void paintComponent(Graphics g) {
                            super.paintComponent(g); Graphics2D g2 = (Graphics2D) g; g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                            String inicial = value != null ? value.toString() : "?"; int hash = Math.abs(inicial.hashCode());
                            Color[] paleta = {new Color(220, 53, 69), new Color(13, 110, 253), new Color(39, 174, 96), new Color(253, 126, 20)};
                            g2.setColor(paleta[hash % paleta.length]);
                            int size = 30; int x = (getWidth() - size) / 2; int y = (getHeight() - size) / 2;
                            g2.fill(new java.awt.geom.Ellipse2D.Double(x, y, size, size));
                            g2.setColor(Color.WHITE); g2.setFont(new Font("Segoe UI", Font.BOLD, 14)); FontMetrics fm = g2.getFontMetrics();
                            g2.drawString(inicial, x + (size - fm.stringWidth(inicial)) / 2, y + ((size - fm.getHeight()) / 2) + fm.getAscent());
                        }
                    };
                    panel.setBackground(isSelected ? table.getSelectionBackground() : table.getBackground()); return panel;
                }
            });

            TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(mod); tab.setRowSorter(sorter);
            txtB.getDocument().addDocumentListener(new DocumentListener() {
                @Override public void insertUpdate(DocumentEvent e) { s(); } public void removeUpdate(DocumentEvent e) { s(); } @Override public void changedUpdate(DocumentEvent e) { s(); }
                private void s() { sorter.setRowFilter(RowFilter.regexFilter("(?i)" + txtB.getText(), 2, 3)); }
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
            JScrollPane sc = new JScrollPane(tab); sc.setBorder(BorderFactory.createLineBorder(new Color(220, 222, 225))); sc.getViewport().setBackground(new Color(255, 255, 255)); add(sc, BorderLayout.CENTER);
        }
    }

    // --- MINI BUSCADOR DE PRODUCTOS (CON ALERTA DE STOCK Y FILTRO DE ELIMINADOS) ---
    private class DialogoBuscarProductoPOS extends JDialog {
        public DialogoBuscarProductoPOS(Frame parent) {
            super(parent, "Catálogo Rápido", true);
            setSize(600, 500); setLocationRelativeTo(parent); getContentPane().setBackground(new Color(240, 242, 245)); setLayout(new BorderLayout(10, 10));
            
            JPanel pnlTop = new JPanel(new BorderLayout()); pnlTop.setBackground(new Color(240, 242, 245)); pnlTop.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            JTextField txtB = new JTextField(); txtB.setBackground(new Color(255, 255, 255)); txtB.setForeground(new Color(45, 45, 45)); txtB.setCaretColor(new Color(45, 45, 45)); txtB.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(220, 222, 225)), BorderFactory.createEmptyBorder(0, 5, 0, 5))); txtB.setFont(new Font("Segoe UI", Font.PLAIN, 14)); txtB.putClientProperty("JTextField.placeholderText", "Buscar por Nombre o Código...");
            pnlTop.add(txtB, BorderLayout.CENTER); add(pnlTop, BorderLayout.NORTH);
            
            String[] cols = {"ID", "Foto", "Código", "Producto", "Precio", "Stock"};
            DefaultTableModel mod = new DefaultTableModel(null, cols) { @Override public boolean isCellEditable(int r, int c) { return false; } };
            
            // --- APLICACIÓN DE COLORES EN ROJO SI EL STOCK ES 0 ---
            JTable tab = new JTable(mod) {
                @Override
                public Component prepareRenderer(javax.swing.table.TableCellRenderer renderer, int row, int column) {
                    Component c = super.prepareRenderer(renderer, row, column);
                    if (!isRowSelected(row)) {
                        Object valorStock = getValueAt(row, 5); // El stock está en la columna 5
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
            
            tab.setBackground(new Color(255, 255, 255)); tab.setForeground(new Color(45, 45, 45)); tab.setRowHeight(60); tab.setFont(new Font("Segoe UI", Font.PLAIN, 14)); tab.setSelectionBackground(new Color(230, 235, 240)); tab.setSelectionForeground(new Color(45, 45, 45));
            tab.getTableHeader().setBackground(new Color(240, 242, 245)); tab.getTableHeader().setForeground(new Color(100, 100, 100)); tab.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(220, 222, 225))); tab.getTableHeader().setPreferredSize(new Dimension(0, 35));
            
            tab.getColumnModel().getColumn(0).setMinWidth(0); tab.getColumnModel().getColumn(0).setMaxWidth(0); 
            tab.getColumnModel().getColumn(1).setPreferredWidth(70); tab.getColumnModel().getColumn(1).setMaxWidth(70); 
            tab.getColumnModel().getColumn(1).setCellRenderer(new ImagenMiniaturaRenderer()); 
            
            TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(mod); tab.setRowSorter(sorter);
            txtB.getDocument().addDocumentListener(new DocumentListener() {
                @Override public void insertUpdate(DocumentEvent e) { s(); } @Override public void removeUpdate(DocumentEvent e) { s(); } @Override public void changedUpdate(DocumentEvent e) { s(); }
                private void s() { sorter.setRowFilter(RowFilter.regexFilter("(?i)" + txtB.getText(), 2, 3)); }
            });
            
            List<Producto> lista = new InventarioDAO().listarProductosActivos();
            for (Producto p : lista) { 
                // FILTRO ESTRICTO: Si está eliminado lógicamente (eliminado_producto == 1), NO se añade a la lista
                // Nota: Si tu método en el modelo se llama diferente (ej: isEliminadoProducto()), adáptalo aquí
<<<<<<< HEAD
                mod.addRow(new Object[]{p.getIdProducto(), p.getRutaImagen(), p.getCodigoBarras(), p.getNombreProducto(), String.format("L %.2f", p.getPrecioVenta()), p.getStockProducto()}); 
=======
                mod.addRow(new Object[]{p.getIdProducto(), p.getRutaImagen(), p.getCodigoBarras(), p.getNombreProducto(), String.format("L %,.2f", p.getPrecioVenta()), p.getStockProducto()}); 
>>>>>>> origin/parte-muoz
            }
            
            tab.addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseClicked(java.awt.event.MouseEvent e) {
                    if (e.getClickCount() == 2) {
                        int filaModelo = tab.convertRowIndexToModel(tab.getSelectedRow());
                        int stockActual = (int) mod.getValueAt(filaModelo, 5);
                        
                        // --- BLOQUEO DE SELECCIÓN SI EL STOCK ES 0 ---
                        if (stockActual <= 0) {
                            JOptionPane.showMessageDialog(DialogoBuscarProductoPOS.this, "No puede seleccionar este artículo porque no cuenta con existencias en el inventario.", "Falta de Stock", JOptionPane.WARNING_MESSAGE);
                            return;
                        }
                        
                        int idSelec = (int) mod.getValueAt(filaModelo, 0);
                        Producto pSelec = lista.stream().filter(p -> p.getIdProducto() == idSelec).findFirst().orElse(null);
                        if(pSelec != null) agregarProductoAVenta(pSelec);
                        dispose();
                    }
                }
            });
            JScrollPane sc = new JScrollPane(tab); sc.setBorder(BorderFactory.createLineBorder(new Color(220, 222, 225))); sc.getViewport().setBackground(new Color(255, 255, 255)); add(sc, BorderLayout.CENTER);
        }
    }
    
    private void mostrarZoomImagen(String ruta) {
        if (ruta == null || !new File(ruta).exists()) return;
        JDialog zoomDialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Previsualización", true);
        zoomDialog.setLayout(new BorderLayout()); zoomDialog.getContentPane().setBackground(new Color(240, 242, 245)); 
        int tamano = 600; zoomDialog.setSize(tamano, tamano);
        Image imgOriginal = new ImageIcon(ruta).getImage();
        int anchoOriginal = imgOriginal.getWidth(null); int altoOriginal = imgOriginal.getHeight(null);
        if (anchoOriginal <= 0 || altoOriginal <= 0) return;
        int nuevoAncho = tamano - 40; int nuevoAlto = tamano - 40;
        if (anchoOriginal > altoOriginal) nuevoAlto = (altoOriginal * nuevoAncho) / anchoOriginal;
        else nuevoAncho = (anchoOriginal * nuevoAlto) / altoOriginal;
        
        java.awt.image.BufferedImage scratch = new java.awt.image.BufferedImage(anchoOriginal, altoOriginal, java.awt.image.BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = scratch.createGraphics(); g2.drawImage(imgOriginal, 0, 0, null); g2.dispose();
        
        int w = anchoOriginal, h = altoOriginal;
        while (w > nuevoAncho * 2 || h > nuevoAlto * 2) {
            w = (w > nuevoAncho * 2) ? w / 2 : nuevoAncho; h = (h > nuevoAlto * 2) ? h / 2 : nuevoAlto;
            java.awt.image.BufferedImage temp = new java.awt.image.BufferedImage(w, h, java.awt.image.BufferedImage.TYPE_INT_ARGB);
            g2 = temp.createGraphics(); g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR); g2.drawImage(scratch, 0, 0, w, h, null); g2.dispose();
            scratch = temp;
        }
        java.awt.image.BufferedImage imgFinal = new java.awt.image.BufferedImage(nuevoAncho, nuevoAlto, java.awt.image.BufferedImage.TYPE_INT_ARGB);
        g2 = imgFinal.createGraphics(); g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC); g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); g2.drawImage(scratch, 0, 0, nuevoAncho, nuevoAlto, null); g2.dispose();
        
        JLabel lblZoom = new JLabel(new ImageIcon(imgFinal)); lblZoom.setHorizontalAlignment(SwingConstants.CENTER);
        zoomDialog.add(lblZoom, BorderLayout.CENTER); zoomDialog.setLocationRelativeTo(this); zoomDialog.setVisible(true);
    }
    
    // =========================================================
    // MENÚ CONTEXTUAL DE OPCIONES
    // =========================================================
    private void mostrarMenuOpciones(Component componente, int x, int y) {
        JPopupMenu menu = new JPopupMenu();
        menu.setBackground(new Color(255, 255, 255)); // Blanco puro
        menu.setBorder(BorderFactory.createLineBorder(new Color(220, 222, 225), 1));

        // Inyectamos los nuevos íconos específicos para cada acción
        JMenuItem itemModPrecio = crearMenuItem("Modificar Precio", new Color(13, 110, 253), new IconoPrecio());
        JMenuItem itemModCant = crearMenuItem("Modificar Cantidad", new Color(39, 174, 96), new IconoCantidad());
        JMenuItem itemQuitar = crearMenuItem("Quitar Producto", new Color(227, 0, 15), new IconoBasurero());

        itemModPrecio.addActionListener(e -> modificarPrecio());
        itemModCant.addActionListener(e -> modificarCantidad());
        itemQuitar.addActionListener(e -> quitarProducto());

        menu.add(itemModPrecio);
        menu.add(itemModCant);
        menu.addSeparator(); 
        menu.add(itemQuitar);
        
        menu.show(componente, x, y);
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
    // ÍCONOS VECTORIALES ESPECÍFICOS (JAVA 2D)
    // =========================================================
    private class IconoPrecio implements Icon {
        @Override public int getIconWidth() { return 20; }
        @Override public int getIconHeight() { return 20; }
        @Override public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(c.getForeground()); // Color dinámico
            g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

            // Dibujo de una Etiqueta de Precio detallada
            g2.drawRoundRect(x + 2, y + 4, 16, 12, 3, 3); // Cuerpo de la etiqueta
            g2.drawOval(x + 5, y + 8, 4, 4); // Orificio para el cordón
            
            // Líneas horizontales simétricas que simulan el valor/código de barras
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
            g2.setColor(c.getForeground()); // Color dinámico
            g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

            // Dibujo de dos Cajas Apiladas (Representa inventario físico/unidades)
            // 1. Caja de atrás (arriba a la derecha)
            g2.drawRect(x + 7, y + 3, 10, 9);
            
            // Mascara de fondo claro intermedio para evitar que las líneas se crucen feo
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
            g2.setColor(c.getForeground()); // Color dinámico (cambiará de texto oscuro a texto blanco sobre fondo rojo)
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
