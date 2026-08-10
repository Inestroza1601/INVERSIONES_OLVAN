package gui;

import dao.ApartadoDAO;
import modelo.AbonoApartado;
import modelo.Apartado;
import modelo.DetalleApartado;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.List;

public class DialogoDetallesApartado extends JDialog {
    private ApartadoDAO dao;
    private int idApartado;
    private Apartado ap;
    private List<DetalleApartado> detalles;
    private List<AbonoApartado> abonos;
    
    private JTable tablaProductos;
    private JTable tablaAbonos;
    private DefaultTableModel modeloAbonos;

    public DialogoDetallesApartado(Frame parent, int idApartado) {
        super(parent, "Detalles del Apartado #" + idApartado, true);
        this.idApartado = idApartado;
        this.dao = new ApartadoDAO();
        
        cargarDatos();
        iniciarDiseno();
    }

    private void cargarDatos() {
        ap = dao.obtenerPorId(idApartado);
        detalles = dao.listarDetalles(idApartado);
        abonos = dao.listarAbonos(idApartado);
    }

    private void iniciarDiseno() {
        setSize(700, 550);
        setLocationRelativeTo(getParent());
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(utilidades.EfectosUI.COLOR_FONDO_PANEL);

        // Cabecera info
        JPanel pnlInfo = new JPanel(new GridLayout(3, 2, 10, 10));
        pnlInfo.setBackground(utilidades.EfectosUI.COLOR_SIDEBAR_PASTEL);
        pnlInfo.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, utilidades.EfectosUI.COLOR_BORDE),
            BorderFactory.createEmptyBorder(15, 20, 15, 20)
        ));
        
        Font fBold = new Font("Segoe UI", Font.BOLD, 14);
        Font fPlain = new Font("Segoe UI", Font.PLAIN, 14);
        
        String cli = ap.getNombreCliente() + " " + (ap.getApellidoCliente() != null ? ap.getApellidoCliente() : "");
        pnlInfo.add(crearLabelInfo("Cliente:", cli, fBold, fPlain));
        pnlInfo.add(crearLabelInfo("Estado:", ap.getEstadoApartado(), fBold, fPlain));
        pnlInfo.add(crearLabelInfo("Fecha Apartado:", new SimpleDateFormat("dd/MM/yyyy HH:mm").format(ap.getFechaApartado()), fBold, fPlain));
        pnlInfo.add(crearLabelInfo("Fecha L\u00EDmite:", (ap.getFechaLimite() != null ? new SimpleDateFormat("dd/MM/yyyy").format(ap.getFechaLimite()) : "N/A"), fBold, fPlain));
        pnlInfo.add(crearLabelInfo("Total Apartado:", String.format("L %,.2f", ap.getTotalApartado()), fBold, fPlain));
        pnlInfo.add(crearLabelInfo("Saldo Pendiente:", String.format("L %,.2f", ap.getSaldoPendiente()), fBold, fPlain));

        add(pnlInfo, BorderLayout.NORTH);

        // Lista Productos & Abonos
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tabs.setBackground(Color.WHITE);
        
        // Tab Productos
        String[] colP = {"C\u00F3digo", "Producto", "Cantidad", "Precio Unitario", "Subtotal"};
        DefaultTableModel modP = new DefaultTableModel(null, colP) { @Override public boolean isCellEditable(int r, int c) { return false; } };
        tablaProductos = new JTable(modP); 
        tablaProductos.setRowHeight(25);
        tablaProductos.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        for (DetalleApartado d : detalles) {
            double sub = d.getCantidadApartado() * d.getPrecioUnitarioApartado();
            modP.addRow(new Object[]{ 
                d.getCodigoBarras(), 
                d.getNombreProducto(), 
                d.getCantidadApartado(), 
                String.format("L %,.2f", d.getPrecioUnitarioApartado()),
                String.format("L %,.2f", sub)
            });
        }
        
        JPanel pnlProd = new JPanel(new BorderLayout());
        pnlProd.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        pnlProd.setOpaque(false);
        pnlProd.add(new JScrollPane(tablaProductos), BorderLayout.CENTER);
        tabs.addTab("Productos", pnlProd);

        // Tab Abonos
        String[] colA = {"ID Abono", "Fecha Abono", "Monto", "M\u00E9todo", "Cajero"};
        modeloAbonos = new DefaultTableModel(null, colA) { @Override public boolean isCellEditable(int r, int c) { return false; } };
        tablaAbonos = new JTable(modeloAbonos); 
        tablaAbonos.setRowHeight(25);
        tablaAbonos.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        for (AbonoApartado ab : abonos) {
            modeloAbonos.addRow(new Object[]{ 
                ab.getIdAbono(), 
                sdf.format(ab.getFechaAbono()), 
                String.format("L %,.2f", ab.getMontoAbono()), 
                ab.getNombreMetodo(), 
                ab.getNombreUsuario() 
            });
        }
        
        JPanel pnlAbonos = new JPanel(new BorderLayout());
        pnlAbonos.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        pnlAbonos.setOpaque(false);
        pnlAbonos.add(new JScrollPane(tablaAbonos), BorderLayout.CENTER);
        tabs.addTab("Abonos", pnlAbonos);

        add(tabs, BorderLayout.CENTER);

        // Botonera Inferior
        JPanel pnlBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 15));
        pnlBotones.setOpaque(false);
        
        JButton btnPrintGeneral = new JButton("Imprimir Ticket General");
        btnPrintGeneral.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnPrintGeneral.setBackground(new Color(41, 128, 185));
        btnPrintGeneral.setForeground(Color.WHITE);
        btnPrintGeneral.setFocusPainted(false);
        btnPrintGeneral.addActionListener(e -> imprimirTicketGeneral());
        
        JButton btnPrintAbono = new JButton("Reimprimir Abono Seleccionado");
        btnPrintAbono.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnPrintAbono.setBackground(new Color(39, 174, 96));
        btnPrintAbono.setForeground(Color.WHITE);
        btnPrintAbono.setFocusPainted(false);
        btnPrintAbono.setEnabled(false);
        btnPrintAbono.addActionListener(e -> imprimirAbonoSeleccionado());
        
        tablaAbonos.getSelectionModel().addListSelectionListener(e -> {
            btnPrintAbono.setEnabled(tablaAbonos.getSelectedRow() >= 0);
        });

        JButton btnCerrar = new JButton("Cerrar");
        btnCerrar.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btnCerrar.addActionListener(e -> dispose());

        pnlBotones.add(btnPrintGeneral);
        pnlBotones.add(btnPrintAbono);
        pnlBotones.add(btnCerrar);
        
        add(pnlBotones, BorderLayout.SOUTH);
    }
    
    private JPanel crearLabelInfo(String titulo, String valor, Font fTit, Font fVal) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        p.setOpaque(false);
        JLabel l1 = new JLabel(titulo); l1.setFont(fTit); l1.setForeground(Color.DARK_GRAY);
        JLabel l2 = new JLabel(valor); l2.setFont(fVal); l2.setForeground(Color.BLACK);
        p.add(l1); p.add(l2);
        return p;
    }
    
    private void imprimirTicketGeneral() {
        try {
            java.io.File dir = new java.io.File("reportes/apartados");
            if (!dir.exists()) dir.mkdirs();
            String ruta = "reportes/apartados/Ticket_Apartado_" + idApartado + ".pdf";
            utilidades.GeneradorTickets.generarTicketApartadoPDF(
                ruta,
                ap.getNombreCliente() + " " + (ap.getApellidoCliente() != null ? ap.getApellidoCliente() : ""),
                new SimpleDateFormat("dd/MM/yyyy HH:mm").format(ap.getFechaApartado()),
                ap.getTotalApartado(),
                ap.getSaldoPendiente(),
                idApartado,
                detalles,
                abonos
            );
            utilidades.GestorImpresion.procesarImpresion(new java.io.File(ruta), utilidades.GestorImpresion.TIPO_TICKET);
        } catch (Exception ex) {
            utilidades.Mensajes.showMessageDialog(this, "Error al generar ticket general: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void imprimirAbonoSeleccionado() {
        int row = tablaAbonos.getSelectedRow();
        if (row < 0) return;
        
        int idAbono = (int) tablaAbonos.getValueAt(row, 0);
        String fecha = (String) tablaAbonos.getValueAt(row, 1);
        String montoStr = (String) tablaAbonos.getValueAt(row, 2);
        double monto = Double.parseDouble(montoStr.replace("L ", "").replace(",", "").trim());
        String metodo = (String) tablaAbonos.getValueAt(row, 3);
        
        // Buscar el abono original para obtener su saldo hist\u00F3rico
        modelo.AbonoApartado abonoOrig = null;
        double saldoDinamico = ap.getTotalApartado();
        
        for (modelo.AbonoApartado ab : abonos) {
            saldoDinamico -= ab.getMontoAbono();
            if (ab.getIdAbono() == idAbono) {
                abonoOrig = ab;
                break;
            }
        }
        
        if (abonoOrig == null) {
            utilidades.Mensajes.showMessageDialog(this, "Error: No se encontr\u00F3 la informaci\u00F3n hist\u00F3rica del abono.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            java.io.File dir = new java.io.File("reportes/abonos");
            if (!dir.exists()) dir.mkdirs();
            String ruta = "reportes/abonos/Ticket_Abono_Apartado_" + idApartado + "_" + System.currentTimeMillis() + ".pdf";
            utilidades.GeneradorTickets.generarTicketAbonoPDF(
                ruta,
                ap.getNombreCliente() + " " + (ap.getApellidoCliente() != null ? ap.getApellidoCliente() : ""),
                fecha,
                monto,
                saldoDinamico, 
                metodo,
                null,
                null,
                idApartado
            );
            utilidades.GestorImpresion.procesarImpresion(new java.io.File(ruta), utilidades.GestorImpresion.TIPO_TICKET);
        } catch (Exception ex) {
            utilidades.Mensajes.showMessageDialog(this, "Error al generar ticket de abono: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}

