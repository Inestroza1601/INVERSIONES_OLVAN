package gui;

import dao.ApartadoDAO;
import dao.KardexDAO;
import dao.VentasDAO;
import modelo.AbonoApartado;
import modelo.Apartado;
import modelo.DetalleApartado;
import java.util.Map;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.text.SimpleDateFormat;
import java.util.List;

public class DialogoRegistrarAbono extends JDialog {
    private ApartadoDAO dao;
    private int idApartado;
    private Apartado ap;
    private List<DetalleApartado> detalles;
    private List<AbonoApartado> abonos;
    private Map<Integer, String> metodos;

    private JTable tablaProductos;
    private JTable tablaAbonos;
    private DefaultTableModel modeloProductos;
    private DefaultTableModel modeloAbonos;

    private JLabel lblTotal;
    private JLabel lblAbonado;
    private JLabel lblSaldo;

    private JTextField txtAbono;
    private JComboBox<String> cmbMetodo;
    private JTextField txtRef;
    private JComboBox<String> cmbBanco;
    private JPanel pnlRefBanco;

    private boolean exito = false;

    public DialogoRegistrarAbono(Frame parent, int idApartado) {
        super(parent, "Registrar Nuevo Abono - Apartado #" + idApartado, true);
        this.idApartado = idApartado;
        this.dao = new ApartadoDAO();
        this.metodos = new VentasDAO().obtenerMetodosPago();

        cargarDatosGenerales();
        iniciarDiseno();
        poblarTablas();
    }

    public boolean isExito() {
        return exito;
    }

    private void cargarDatosGenerales() {
        ap = dao.obtenerPorId(idApartado);
        detalles = dao.listarDetalles(idApartado);
        abonos = dao.listarAbonos(idApartado);
    }

    private void iniciarDiseno() {
        setSize(950, 600);
        setLocationRelativeTo(getParent());
        setLayout(new BorderLayout());
<<<<<<< HEAD
        getContentPane().setBackground(new Color(245, 247, 250));

        // Cabecera superior
        JPanel pnlCabecera = new JPanel(new BorderLayout());
        pnlCabecera.setBackground(Color.WHITE);
        pnlCabecera.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        JLabel lblTitulo = new JLabel("Registro de Abono");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitulo.setForeground(new Color(30, 30, 30));

=======
        getContentPane().setBackground(utilidades.EfectosUI.COLOR_FONDO_PANEL);
        
        // Cabecera superior
        JPanel pnlCabecera = new JPanel(new BorderLayout());
        pnlCabecera.setBackground(utilidades.EfectosUI.COLOR_SIDEBAR_PASTEL);
        pnlCabecera.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, utilidades.EfectosUI.COLOR_BORDE),
            BorderFactory.createEmptyBorder(15, 20, 15, 20)
        ));
        
        JLabel lblTitulo = new JLabel("Registro de Abono");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitulo.setForeground(utilidades.EfectosUI.COLOR_TEXTO_TITULO);
        
>>>>>>> origin/parte-muoz
        String cliente = ap.getNombreCliente() + " " + (ap.getApellidoCliente() != null ? ap.getApellidoCliente() : "");
        JLabel lblCliente = new JLabel("Cliente: " + cliente + " | Fecha Límite: " +
                (ap.getFechaLimite() != null ? new SimpleDateFormat("dd/MM/yyyy").format(ap.getFechaLimite()) : "N/A"));
        lblCliente.setFont(new Font("Segoe UI", Font.PLAIN, 14));
<<<<<<< HEAD
        lblCliente.setForeground(Color.GRAY);

=======
        lblCliente.setForeground(utilidades.EfectosUI.COLOR_TEXTO_SUBTITULO);
        
>>>>>>> origin/parte-muoz
        pnlCabecera.add(lblTitulo, BorderLayout.NORTH);
        pnlCabecera.add(lblCliente, BorderLayout.SOUTH);
        add(pnlCabecera, BorderLayout.NORTH);

        // Contenedor principal (dos columnas)
        JPanel pnlCuerpo = new JPanel(new BorderLayout(15, 15));
        pnlCuerpo.setOpaque(false);
        pnlCuerpo.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // --- LADO IZQUIERDO: Tablas e Info ---
        JPanel pnlIzquierdo = new JPanel(new BorderLayout(10, 15));
        pnlIzquierdo.setOpaque(false);
        pnlIzquierdo.setPreferredSize(new Dimension(550, 0));

        // Panel Totales (Arriba)
        JPanel pnlTotales = new JPanel(new GridLayout(1, 3, 10, 0));
        pnlTotales.setOpaque(false);

        lblTotal = crearLabelTarjeta("Total Apartado", new Color(41, 128, 185));
        lblAbonado = crearLabelTarjeta("Total Abonado", new Color(39, 174, 96));
        lblSaldo = crearLabelTarjeta("Saldo Pendiente", new Color(227, 0, 15));

        JPanel cTotal = crearTarjetaResumen(lblTotal, new Color(41, 128, 185));
        JPanel cAbon = crearTarjetaResumen(lblAbonado, new Color(39, 174, 96));
        JPanel cSald = crearTarjetaResumen(lblSaldo, new Color(227, 0, 15));

        pnlTotales.add(cTotal);
        pnlTotales.add(cAbon);
        pnlTotales.add(cSald);

        pnlIzquierdo.add(pnlTotales, BorderLayout.NORTH);

        // Tablas
        JPanel pnlTablas = new JPanel(new GridLayout(2, 1, 0, 15));
        pnlTablas.setOpaque(false);

        // Tabla Productos
        JPanel pnlProd = new JPanel(new BorderLayout());
        pnlProd.setBackground(Color.WHITE);
        pnlProd.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(220, 222, 225)),
                "Productos del Apartado"));
        modeloProductos = new DefaultTableModel(null, new String[] { "Producto", "Cant", "Precio Unit", "Subtotal" }) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        tablaProductos = new JTable(modeloProductos);
        tablaProductos.setRowHeight(25);
        pnlProd.add(new JScrollPane(tablaProductos), BorderLayout.CENTER);

        // Tabla Abonos
        JPanel pnlHist = new JPanel(new BorderLayout());
        pnlHist.setBackground(Color.WHITE);
        pnlHist.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(220, 222, 225)),
                "Historial de Pagos"));
        modeloAbonos = new DefaultTableModel(null, new String[] { "No.", "Fecha", "Monto", "Método" }) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        tablaAbonos = new JTable(modeloAbonos);
        tablaAbonos.setRowHeight(25);
        pnlHist.add(new JScrollPane(tablaAbonos), BorderLayout.CENTER);

        pnlTablas.add(pnlProd);
        pnlTablas.add(pnlHist);

        pnlIzquierdo.add(pnlTablas, BorderLayout.CENTER);

        // --- LADO DERECHO: Formulario ---
        JPanel pnlDerecho = new JPanel(new BorderLayout());
        pnlDerecho.setBackground(Color.WHITE);
        pnlDerecho.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 222, 225)),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)));

        JPanel pnlForm = new JPanel(new GridBagLayout());
        pnlForm.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, 15, 0);
        gbc.weightx = 1.0;
        gbc.gridx = 0;

        // Monto
        gbc.gridy = 0;
        JLabel lMonto = new JLabel("Monto a Abonar (L):");
        lMonto.setFont(new Font("Segoe UI", Font.BOLD, 14));
        pnlForm.add(lMonto, gbc);

        gbc.gridy = 1;
        txtAbono = new JTextField();
        txtAbono.setFont(new Font("Segoe UI", Font.BOLD, 18));
        txtAbono.setHorizontalAlignment(JTextField.RIGHT);
        txtAbono.setPreferredSize(new Dimension(200, 40));
        txtAbono.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyTyped(java.awt.event.KeyEvent evt) {
                char c = evt.getKeyChar();
                // Permitir solo números y punto decimal
                if (!Character.isDigit(c) && c != '.') {
                    evt.consume();
                }
                // Prevenir múltiples puntos
                if (c == '.' && txtAbono.getText().contains(".")) {
                    evt.consume();
                }
            }

            @Override
            public void keyReleased(java.awt.event.KeyEvent evt) {
                String s = txtAbono.getText().replace(",", "").trim();
                if (s.isEmpty() || s.contains("."))
                    return;
                try {
                    long num = Long.parseLong(s);
                    txtAbono.setText(String.format("%,d", num));
                } catch (Exception ex) {
                }
            }
        });
        pnlForm.add(txtAbono, gbc);

        // Método
        gbc.gridy = 2;
        pnlForm.add(new JLabel("Método de Pago:"), gbc);

        gbc.gridy = 3;
        cmbMetodo = new JComboBox<>();
        cmbMetodo.setPreferredSize(new Dimension(200, 35));
        for (String m : metodos.values())
            cmbMetodo.addItem(m);
        pnlForm.add(cmbMetodo, gbc);

        // Panel Ref/Banco (Oculto por defecto)
        pnlRefBanco = new JPanel(new GridLayout(4, 1, 0, 5));
        pnlRefBanco.setOpaque(false);
        pnlRefBanco.add(new JLabel("Banco:"));
        cmbBanco = new JComboBox<>(
                new String[] { "Seleccione Banco...", "BAC", "FICOHSA", "ATLANTIDA", "BANPAIS", "OCCIDENTE" });
        cmbBanco.setPreferredSize(new Dimension(200, 35));
        pnlRefBanco.add(cmbBanco);
        pnlRefBanco.add(new JLabel("Referencia / Voucher:"));
        txtRef = new JTextField();
        txtRef.setPreferredSize(new Dimension(200, 35));
        pnlRefBanco.add(txtRef);
        pnlRefBanco.setVisible(false);

        gbc.gridy = 4;
        pnlForm.add(pnlRefBanco, gbc);

        pnlDerecho.add(pnlForm, BorderLayout.NORTH);

        // Boton Procesar
        JButton btnProcesar = utilidades.EfectosUI.crearBotonVerde("Procesar Abono");
        btnProcesar.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btnProcesar.setPreferredSize(new Dimension(0, 50));
        btnProcesar.addActionListener(e -> procesarAbono());

        pnlDerecho.add(btnProcesar, BorderLayout.SOUTH);

        pnlCuerpo.add(pnlIzquierdo, BorderLayout.CENTER);
        pnlCuerpo.add(pnlDerecho, BorderLayout.EAST);

        add(pnlCuerpo, BorderLayout.CENTER);

        // Listeners
        cmbMetodo.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                String val = cmbMetodo.getSelectedItem().toString().toLowerCase();
                boolean mostrar = val.contains("tarjeta") || val.contains("transferencia");
                pnlRefBanco.setVisible(mostrar);
                pnlDerecho.revalidate();
                pnlDerecho.repaint();
            }
        });
    }

    private void poblarTablas() {
        double tAbonado = 0;

        modeloProductos.setRowCount(0);
        for (DetalleApartado d : detalles) {
            double sub = d.getCantidadApartado() * d.getPrecioUnitarioApartado();
            modeloProductos.addRow(new Object[] {
                    d.getNombreProducto(),
                    d.getCantidadApartado(),
                    String.format("L %,.2f", d.getPrecioUnitarioApartado()),
                    String.format("L %,.2f", sub)
            });
        }

        modeloAbonos.setRowCount(0);
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yy HH:mm");
        int num = 1;
        for (AbonoApartado a : abonos) {
            modeloAbonos.addRow(new Object[] {
                    num++,
                    sdf.format(a.getFechaAbono()),
                    String.format("L %,.2f", a.getMontoAbono()),
                    a.getNombreMetodo()
            });
            tAbonado += a.getMontoAbono();
        }

        lblTotal.setText(String.format("L %,.2f", ap.getTotalApartado()));
        lblAbonado.setText(String.format("L %,.2f", tAbonado));
        lblSaldo.setText(String.format("L %,.2f", ap.getSaldoPendiente()));
    }

    private JLabel crearLabelTarjeta(String texto, Color color) {
        JLabel lbl = new JLabel("L 0.00", SwingConstants.CENTER);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lbl.setForeground(color);
        return lbl;
    }

    private JPanel crearTarjetaResumen(JLabel lblValor, Color colorBorde) {
        JPanel pnl = new JPanel(new BorderLayout());
        pnl.setBackground(Color.WHITE);
        pnl.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(3, 0, 0, 0, colorBorde),
                BorderFactory.createEmptyBorder(10, 5, 10, 5)));

        String titulo = "";
        if (colorBorde.equals(new Color(41, 128, 185)))
            titulo = "Total Apartado";
        else if (colorBorde.equals(new Color(39, 174, 96)))
            titulo = "Total Abonado";
        else
            titulo = "Saldo Pendiente";

        JLabel lblTit = new JLabel(titulo, SwingConstants.CENTER);
        lblTit.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblTit.setForeground(Color.DARK_GRAY);

        pnl.add(lblTit, BorderLayout.NORTH);
        pnl.add(lblValor, BorderLayout.CENTER);

        return pnl;
    }

    private void procesarAbono() {
        try {
            double abono = Double.parseDouble(txtAbono.getText().replace(",", "").trim());
            if (abono <= 0) {
                JOptionPane.showMessageDialog(this, "El abono debe ser mayor a 0.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (abono > ap.getSaldoPendiente() + 0.05) {
                JOptionPane.showMessageDialog(this, "El abono no puede exceder el saldo pendiente.", "Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

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
            if (!pnlRefBanco.isVisible()) {
                ref = null;
                banco = null;
            } else {
                if (ref.isEmpty()) {
                    JOptionPane.showMessageDialog(this,
                            "Debe ingresar el número de referencia para este método de pago.", "Error",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }
                if (banco == null) {
                    JOptionPane.showMessageDialog(this, "Debe seleccionar un banco para este método de pago.", "Error",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }

            JPasswordField pfPass = new JPasswordField();
            int opSign = JOptionPane.showConfirmDialog(this,
                    new Object[] { "Ingrese contraseña de cajero para autorizar el abono:", pfPass },
                    "Firma Autorización", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (opSign != JOptionPane.OK_OPTION)
                return;

            String pass = new String(pfPass.getPassword());
            int idUserFirma = new KardexDAO().validarFirmaUsuario(pass);

            if (idUserFirma <= 0) {
                JOptionPane.showMessageDialog(this, "Contraseña incorrecta o usuario inactivo.", "Acceso Denegado",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (dao.registrarAbono(idApartado, abono, idMetodo, idUserFirma, ref, banco)) {
                JOptionPane.showMessageDialog(this, "Abono registrado con éxito.", "Éxito",
                        JOptionPane.INFORMATION_MESSAGE);
                this.exito = true;

                // Imprimir comprobante
                int optImp = JOptionPane.showConfirmDialog(this, "¿Desea imprimir el comprobante del abono?",
                        "Imprimir", JOptionPane.YES_NO_OPTION);
                if (optImp == JOptionPane.YES_OPTION) {
                    try {
                        String ruta = System.getProperty("user.home") + "/Ticket_Abono_" + idApartado + "_"
                                + System.currentTimeMillis() + ".pdf";
                        utilidades.GeneradorTickets.generarTicketAbonoPDF(
                                ruta,
                                ap.getNombreCliente(),
                                new SimpleDateFormat("dd/MM/yyyy HH:mm").format(new java.util.Date()),
                                abono,
                                ap.getSaldoPendiente() - abono, // nuevo saldo pendiente
                                selectedMetodo,
                                ref,
                                banco,
                                idApartado);
                        Desktop.getDesktop().open(new java.io.File(ruta));
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(this, "Error al generar ticket: " + ex.getMessage());
                    }
                }

                this.dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Error de base de datos al guardar abono.", "Error",
                        JOptionPane.ERROR_MESSAGE);
            }

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Monto de abono inválido.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
