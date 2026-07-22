package gui;

import dao.KardexDAO;
import modelo.Producto;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.util.List;

public class DialogoKardex extends JDialog {

    // Aquí están las variables que te daban error
    private Producto productoActual;
    private JTable tablaHistorial;
    private DefaultTableModel modeloTabla;
    private JLabel lblStockDinamico;

    public DialogoKardex(Window parent, Producto producto) {
        super(parent, "Kardex del Producto", ModalityType.APPLICATION_MODAL);
        this.productoActual = producto;
        iniciarDiseno();
        cargarHistorial();
    }

    private void iniciarDiseno() {
        this.setSize(750, 500);
        this.setLocationRelativeTo(getOwner());
        this.setLayout(new BorderLayout());
        this.getContentPane().setBackground(new Color(240, 242, 245)); // Gris Nube

        // --- PANEL SUPERIOR ---
        JPanel pnlTop = new JPanel(new BorderLayout());
        pnlTop.setBackground(new Color(255, 255, 255)); // Blanco puro
        pnlTop.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(220, 222, 225)), // Borde gris claro inferior
            BorderFactory.createEmptyBorder(15, 20, 15, 20)
        ));

        JPanel pnlInfo = new JPanel(new GridLayout(2, 1));
        pnlInfo.setOpaque(false);
        JLabel lblNombre = new JLabel("Producto: " + productoActual.getNombreProducto());
        lblNombre.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblNombre.setForeground(new Color(45, 45, 45)); // Gris oscuro
        
        lblStockDinamico = new JLabel("Stock Actual: " + productoActual.getStockProducto() + " unidades");
        lblStockDinamico.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblStockDinamico.setForeground(new Color(140, 145, 150)); // Gris suave

        pnlInfo.add(lblNombre);
        pnlInfo.add(lblStockDinamico);
        
        JButton btnNuevoMov = new JButton("+ Nuevo Movimiento");
        btnNuevoMov.setBackground(new Color(39, 174, 96)); // Verde Menta
        btnNuevoMov.setForeground(Color.WHITE);
        btnNuevoMov.setFocusPainted(false);
        btnNuevoMov.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnNuevoMov.addActionListener(e -> abrirFormularioMovimiento());

        pnlTop.add(pnlInfo, BorderLayout.CENTER);
        pnlTop.add(btnNuevoMov, BorderLayout.EAST);
        this.add(pnlTop, BorderLayout.NORTH);

        // --- TABLA DE HISTORIAL 
        String[] columnas = {"Fecha", "Tipo", "Cant.", "Stock Rest.", "Observación", "Usuario/Firma"};
        modeloTabla = new DefaultTableModel(null, columnas) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        tablaHistorial = new JTable(modeloTabla);
        tablaHistorial.setBackground(new Color(255, 255, 255)); // Blanco puro
        tablaHistorial.setForeground(new Color(45, 45, 45)); // Gris oscuro
        tablaHistorial.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tablaHistorial.setRowHeight(35);
        tablaHistorial.getTableHeader().setBackground(new Color(240, 242, 245)); // Gris Nube
        tablaHistorial.getTableHeader().setForeground(new Color(100, 100, 100)); // Gris intermedio
        tablaHistorial.setSelectionBackground(new Color(230, 235, 240)); // Selección gris/azul suave
        tablaHistorial.setSelectionForeground(new Color(45, 45, 45));
        
        // --- LÓGICA DE CURSORES (MANITA) SOBRE REFERENCIA DE VENTA ---
        tablaHistorial.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            @Override
            public void mouseMoved(java.awt.event.MouseEvent e) {
                int col = tablaHistorial.columnAtPoint(e.getPoint());
                if (col == 4) { // Columna "Observación"
                    int row = tablaHistorial.rowAtPoint(e.getPoint());
                    if (row >= 0) {
                        Object obs = tablaHistorial.getValueAt(row, col);
                        if (obs != null && obs.toString().startsWith("Venta #")) {
                            tablaHistorial.setCursor(new Cursor(Cursor.HAND_CURSOR));
                            return;
                        }
                    }
                }
                tablaHistorial.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            }
        });

        // --- LÓGICA DE CLIC PARA ABRIR PREVISUALIZACIÓN ---
        tablaHistorial.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                int col = tablaHistorial.columnAtPoint(e.getPoint());
                if (col == 4 && e.getClickCount() == 1) { // Un clic sobre observación
                    int row = tablaHistorial.rowAtPoint(e.getPoint());
                    Object obs = tablaHistorial.getValueAt(row, col);
                    if (obs != null && obs.toString().startsWith("Venta #")) {
                        try {
                            String textoObs = obs.toString();
                            int idVenta = Integer.parseInt(textoObs.substring(textoObs.indexOf("#") + 1));
                            mostrarVistaPreviaRecibo(idVenta); // Nuevo método que agregaremos abajo
                        } catch (Exception ex) {}
                    }
                }
            }
        });
        
        JScrollPane scroll = new JScrollPane(tablaHistorial);
        scroll.getViewport().setBackground(new Color(255, 255, 255)); // Fondo blanco
        scroll.setBorder(BorderFactory.createLineBorder(new Color(220, 222, 225))); // Borde gris suave
        this.add(scroll, BorderLayout.CENTER);
        
    }

    // --- MÉTODOS QUE FALTABAN ---
    public void cargarHistorial() {
        modeloTabla.setRowCount(0);
        KardexDAO dao = new KardexDAO();
        List<Object[]> historial = dao.obtenerHistorialKardex(productoActual.getIdProducto());
        for (Object[] fila : historial) {
            modeloTabla.addRow(fila);
        }
    }
    
    public void actualizarStockVisual(int nuevoStock) {
        productoActual.setStockProducto(nuevoStock);
        lblStockDinamico.setText("Stock Actual: " + nuevoStock + " unidades");
    }

    private void abrirFormularioMovimiento() {
        DialogoMovimientoKardex dialog = new DialogoMovimientoKardex(this, productoActual);
        dialog.setVisible(true);
    }
    
    // =========================================================
    // VENTANA DE PREVISUALIZACIÓN DE RECIBO (MODO AUDITORÍA)
    // =========================================================
    private void mostrarVistaPreviaRecibo(int idVenta) {
        dao.VentasDAO vDao = new dao.VentasDAO();
        java.util.Map<String, Object> datos = vDao.obtenerReciboPorId(idVenta);
        
        if (datos.isEmpty() || !datos.containsKey("detalles")) {
            JOptionPane.showMessageDialog(this, "No se encontraron los datos de la Venta #" + idVenta, "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String cliente = (String) datos.get("cliente");
        String fechaHistorica = (String) datos.get("fecha"); // <--- EXTRAEMOS LA FECHA ORIGINAL
        double subtotal = (double) datos.get("subtotal");
        double isv = (double) datos.get("isv");
        double total = (double) datos.get("total");
        String metodo = (String) datos.get("metodo");
        String ref = (String) datos.get("ref");
        String banco = (String) datos.get("banco");
        @SuppressWarnings("unchecked")
        List<Object[]> detalles = (List<Object[]>) datos.get("detalles");

        JDialog previewDialog = new JDialog(this, "Previsualización Venta #" + idVenta, true);
        previewDialog.setSize(350, 650);
        previewDialog.setLocationRelativeTo(this);
        previewDialog.setLayout(new BorderLayout(10, 10));
        previewDialog.getContentPane().setBackground(new Color(240, 242, 245)); // Gris Nube

        // Pasamos la fecha histórica exacta a la previsualización del ticket en pantalla
        JPanel pnlTicket = utilidades.GeneradorTickets.crearTicketVistaPrevia("Venta #" + idVenta, cliente, fechaHistorica, detalles, subtotal, isv, total, metodo, ref, banco);
        
        JScrollPane scrollPreview = new JScrollPane(pnlTicket);
        scrollPreview.setBorder(null);
        previewDialog.add(scrollPreview, BorderLayout.CENTER);

        JPanel pnlBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        pnlBotones.setOpaque(false);

        JButton btnCerrar = new JButton("Cerrar");
        btnCerrar.setBackground(new Color(140, 145, 150)); // Gris secundario
        btnCerrar.setForeground(Color.WHITE);
        btnCerrar.addActionListener(ex -> previewDialog.dispose());

        JButton btnImprimir = new JButton("🖨 Imprimir Copia");
        btnImprimir.setBackground(new Color(39, 174, 96)); // Verde Menta
        btnImprimir.setForeground(Color.WHITE);
        btnImprimir.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnImprimir.addActionListener(ex -> {
            try {
                String rutaTemp = System.getProperty("java.io.tmpdir") + "Copia_Venta_" + idVenta + ".pdf";
                
                // Pasamos la fecha histórica exacta al regenerador de PDF físico
                utilidades.GeneradorTickets.generarTicketVentaPDF(rutaTemp, cliente, fechaHistorica, detalles, subtotal, isv, total, true, metodo, ref, banco);
                
                JOptionPane.showMessageDialog(previewDialog, "Copia de recibo generada exitosamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                if (Desktop.isDesktopSupported()) Desktop.getDesktop().open(new File(rutaTemp));
                
            } catch (Exception err) {
                JOptionPane.showMessageDialog(previewDialog, "Error al generar PDF: " + err.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        pnlBotones.add(btnImprimir); pnlBotones.add(btnCerrar);
        previewDialog.add(pnlBotones, BorderLayout.SOUTH);

        previewDialog.setVisible(true);
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
