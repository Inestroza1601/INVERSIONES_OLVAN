package gui;

import dao.VentasDAO;
import utilidades.SesionGlobal;
import utilidades.GeneradorTickets;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;

public class PanelHistorialVentas extends JPanel {
    private VentasDAO dao;
    private List<Object[]> ventas;
    private JTable tablaVentas;
    private DefaultTableModel modeloTabla;
    private JTextField txtBuscar;

    public PanelHistorialVentas() {
        this.dao = new VentasDAO();
        iniciarDiseno();
        cargarVentas();
    }

    private void iniciarDiseno() {
        this.setLayout(new BorderLayout(20, 20));
        this.setBackground(new Color(240, 242, 245)); // Gris Nube
        this.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        // Cabecera
        JPanel pnlCabecera = new JPanel(new BorderLayout());
        pnlCabecera.setOpaque(false);

        JLabel lblTitulo = new JLabel("Historial de Ventas");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitulo.setForeground(new Color(45, 45, 45));
        pnlCabecera.add(lblTitulo, BorderLayout.WEST);

        JPanel pnlBuscar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        pnlBuscar.setOpaque(false);
        txtBuscar = new JTextField(15);
        txtBuscar.putClientProperty("JTextField.placeholderText", "Buscar por cliente o método...");
        txtBuscar.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtBuscar.setPreferredSize(new Dimension(220, 35));
        txtBuscar.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(220, 222, 225)), BorderFactory.createEmptyBorder(0, 8, 0, 8)));
        txtBuscar.addActionListener(e -> filtrarVentas());
        pnlBuscar.add(txtBuscar);
        pnlCabecera.add(pnlBuscar, BorderLayout.EAST);

        this.add(pnlCabecera, BorderLayout.NORTH);

        // Tabla
        String[] cols = {"ID Venta", "Fecha / Hora", "Cliente", "Método Pago", "Vendedor", "Subtotal", "ISV (15%)", "Total"};
        modeloTabla = new DefaultTableModel(null, cols) { @Override public boolean isCellEditable(int r, int c) { return false; } };
        tablaVentas = new JTable(modeloTabla);
        tablaVentas.setRowHeight(32);
        tablaVentas.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        
        JScrollPane scroll = new JScrollPane(tablaVentas);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(220, 222, 225)));
        scroll.getViewport().setBackground(Color.WHITE);
        this.add(scroll, BorderLayout.CENTER);

        // Botones de acción
        JPanel pnlBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        pnlBotones.setOpaque(false);

        JButton btnDetalles = new JButton("👁 Ver Detalles");
        btnDetalles.setBackground(Color.WHITE);
        btnDetalles.setForeground(new Color(45, 45, 45));
        btnDetalles.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnDetalles.setFocusPainted(false);
        btnDetalles.setPreferredSize(new Dimension(140, 38));
        btnDetalles.setBorder(BorderFactory.createLineBorder(new Color(220, 222, 225)));
        btnDetalles.addActionListener(e -> verDetallesVenta());

        JButton btnReimprimir = new JButton("🖨 Reimprimir Ticket");
        btnReimprimir.setBackground(new Color(41, 128, 185)); // Azul
        btnReimprimir.setForeground(Color.WHITE);
        btnReimprimir.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnReimprimir.setFocusPainted(false);
        btnReimprimir.setPreferredSize(new Dimension(160, 38));
        btnReimprimir.addActionListener(e -> reimprimirTicket());

        pnlBotones.add(btnDetalles);
        pnlBotones.add(btnReimprimir);
        this.add(pnlBotones, BorderLayout.SOUTH);
    }

    private void cargarVentas() {
        modeloTabla.setRowCount(0);
        ventas = dao.listarVentas();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        for (Object[] v : ventas) {
            modeloTabla.addRow(new Object[]{
                v[0], // id_ventas
                v[1] != null ? sdf.format((java.util.Date) v[1]) : "N/A", // fecha_venta
                v[2], // cliente
                v[3], // metodo
                v[4], // vendedor
                "L " + String.format("%.2f", (double) v[5]), // subtotal
                "L " + String.format("%.2f", (double) v[6]), // isv
                "L " + String.format("%.2f", (double) v[7])  // total
            });
        }
    }

    private void filtrarVentas() {
        String filter = txtBuscar.getText().toLowerCase().trim();
        if (filter.isEmpty()) { cargarVentas(); return; }
        
        modeloTabla.setRowCount(0);
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        for (Object[] v : ventas) {
            String client = v[2].toString().toLowerCase();
            String method = v[3].toString().toLowerCase();
            String seller = v[4].toString().toLowerCase();
            String idStr = v[0].toString();
            
            if (client.contains(filter) || method.contains(filter) || seller.contains(filter) || idStr.contains(filter)) {
                modeloTabla.addRow(new Object[]{
                    v[0],
                    v[1] != null ? sdf.format((java.util.Date) v[1]) : "N/A",
                    v[2],
                    v[3],
                    v[4],
                    "L " + String.format("%.2f", (double) v[5]),
                    "L " + String.format("%.2f", (double) v[6]),
                    "L " + String.format("%.2f", (double) v[7])
                });
            }
        }
    }

    private void verDetallesVenta() {
        int selectedRow = tablaVentas.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Seleccione una venta de la lista.", "Aviso", JOptionPane.WARNING_MESSAGE); return;
        }

        int idVenta = (int) tablaVentas.getValueAt(selectedRow, 0);
        Map<String, Object> venta = dao.obtenerReciboPorId(idVenta);
        if (venta == null || venta.isEmpty()) return;

        List<Object[]> detalles = (List<Object[]>) venta.get("detalles");

        Window parent = SwingUtilities.getWindowAncestor(this);
        JDialog dialog = new JDialog((Frame) parent, "Detalles de Venta #" + idVenta, true);
        dialog.setSize(600, 480); dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout(10, 10));

        // Info General
        JPanel pnlInfo = new JPanel(new GridLayout(3, 2, 10, 10));
        pnlInfo.setBorder(BorderFactory.createEmptyBorder(15, 15, 10, 15));
        pnlInfo.add(new JLabel("Cliente: " + venta.get("cliente")));
        pnlInfo.add(new JLabel("Fecha / Hora: " + venta.get("fecha")));
        pnlInfo.add(new JLabel("Método de Pago: " + venta.get("metodo")));
        
        String ref = (String) venta.get("ref");
        String banco = (String) venta.get("banco");
        String infoExtra = (ref != null ? ref : "N/A") + (banco != null ? " (" + banco + ")" : "");
        pnlInfo.add(new JLabel("Referencia / Banco: " + infoExtra));
        pnlInfo.add(new JLabel("Subtotal: L " + String.format("%.2f", (double) venta.get("subtotal"))));
        pnlInfo.add(new JLabel("Impuesto (15%): L " + String.format("%.2f", (double) venta.get("isv"))));

        // Tabla Productos
        String[] colP = {"Descripción / Serie", "Cantidad", "Precio Unitario", "Total Fila"};
        DefaultTableModel modP = new DefaultTableModel(null, colP) { @Override public boolean isCellEditable(int r, int c) { return false; } };
        JTable tabP = new JTable(modP); tabP.setRowHeight(25);
        for (Object[] d : detalles) {
            String desc = d[2].toString(); // descripcion_venta
            String serie = d[1] != null ? d[1].toString() : null; // identificador_serie
            String fullDesc = desc + (serie != null && !serie.isEmpty() ? " [S/N: " + serie + "]" : "");
            
            modP.addRow(new Object[]{
                fullDesc,
                d[3], // cantidad
                "L " + String.format("%.2f", (double) d[4]), // precio_unitario
                "L " + String.format("%.2f", (double) d[5])  // subtotal
            });
        }
        
        JScrollPane scrollProd = new JScrollPane(tabP);
        scrollProd.setBorder(BorderFactory.createLineBorder(new Color(220, 222, 225)));
        
        dialog.add(pnlInfo, BorderLayout.NORTH);
        dialog.add(scrollProd, BorderLayout.CENTER);

        // Pie
        JPanel pnlBot = new JPanel(new BorderLayout());
        pnlBot.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        
        JLabel lblTotal = new JLabel("TOTAL PAGADO: L " + String.format("%.2f", (double) venta.get("total")));
        lblTotal.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTotal.setForeground(new Color(39, 174, 96));
        pnlBot.add(lblTotal, BorderLayout.WEST);

        JButton btnCerrar = new JButton("Cerrar");
        btnCerrar.addActionListener(e -> dialog.dispose());
        pnlBot.add(btnCerrar, BorderLayout.EAST);

        dialog.add(pnlBot, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void reimprimirTicket() {
        int selectedRow = tablaVentas.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Seleccione una venta de la lista.", "Aviso", JOptionPane.WARNING_MESSAGE); return;
        }

        int idVenta = (int) tablaVentas.getValueAt(selectedRow, 0);
        Map<String, Object> venta = dao.obtenerReciboPorId(idVenta);
        if (venta == null || venta.isEmpty()) return;

        List<Object[]> detalles = (List<Object[]>) venta.get("detalles");

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Reimprimir Ticket de Venta #" + idVenta);
        chooser.setSelectedFile(new File("Reimpresion_Factura_" + idVenta + "_" + System.currentTimeMillis() + ".pdf"));

        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File archivoDestino = chooser.getSelectedFile();
            if (!archivoDestino.getName().toLowerCase().endsWith(".pdf")) {
                archivoDestino = new File(archivoDestino.getAbsolutePath() + ".pdf");
            }

            try {
                boolean facturacionHabilitada = dao.empresaTieneFacturacionHabilitada(1);
                
                GeneradorTickets.generarTicketVentaPDF(
                    archivoDestino.getAbsolutePath(),
                    (String) venta.get("cliente"),
                    (String) venta.get("fecha"),
                    detalles,
                    (double) venta.get("subtotal"),
                    (double) venta.get("isv"),
                    (double) venta.get("total"),
                    facturacionHabilitada,
                    (String) venta.get("metodo"),
                    (String) venta.get("ref"),
                    (String) venta.get("banco")
                );

                JOptionPane.showMessageDialog(this, "Ticket reimpreso y generado con éxito.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                if (Desktop.isDesktopSupported()) {
                    Desktop.getDesktop().open(archivoDestino);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error al generar el PDF:\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
