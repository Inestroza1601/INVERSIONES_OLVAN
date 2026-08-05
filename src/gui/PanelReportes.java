package gui;

import dao.ReportesDAO;
import utilidades.GeneradorReportesPDF;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class PanelReportes extends JPanel {

    private JComboBox<String> cmbTipoReporte;
    private JPanel panelFiltros;
    private JButton btnGenerarPDF;
    private JTable tablaVistaPrevia;
    private DefaultTableModel modeloTabla;

    // Filtros específicos
    private JTextField txtFechaDiaria;
    private JTextField txtFechaDesde;
    private JTextField txtFechaHasta;

    private ReportesDAO reportesDAO;
    private List<Object[]> ultimosDatos;
    private String[] ultimasColumnas;
    private String ultimoTitulo;

    public PanelReportes() {
        reportesDAO = new ReportesDAO();
        iniciarDiseno();
    }

    private void iniciarDiseno() {
        this.setLayout(new BorderLayout());
        this.setBackground(utilidades.EfectosUI.COLOR_FONDO_PANEL);

        // CABECERA
        JPanel panelCabecera = new JPanel(new BorderLayout());
        panelCabecera.setOpaque(false);
        panelCabecera.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));

        JLabel lblTitulo = new JLabel("Generación de Reportes PDF");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitulo.setForeground(utilidades.EfectosUI.COLOR_TEXTO_TITULO);
        panelCabecera.add(lblTitulo, BorderLayout.NORTH);

        // CONTROLES DE REPORTE
        JPanel panelControles = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        panelControles.setOpaque(false);

        JLabel lblTipo = new JLabel("Tipo de Reporte:");
        lblTipo.setFont(new Font("Segoe UI", Font.BOLD, 14));
        
        String[] tipos = {
            "1. Reporte de Caja Diario", 
            "2. Reporte Detallado de Ventas", 
            "3. Reporte Dinámico de Inventario", 
            "4. Alertas (Stock Bajo / Estancados > 15 días)"
        };
        cmbTipoReporte = new JComboBox<>(tipos);
        cmbTipoReporte.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        cmbTipoReporte.addActionListener(e -> cambiarFiltros());

        panelFiltros = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        panelFiltros.setOpaque(false);

        JButton btnPrevisualizar = utilidades.EfectosUI.crearBotonVerde("Previsualizar Datos");
        btnPrevisualizar.addActionListener(e -> previsualizarReporte());

        btnGenerarPDF = new JButton("Exportar a PDF");
        btnGenerarPDF.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnGenerarPDF.setBackground(new Color(227, 0, 15)); // Rojo exportar
        btnGenerarPDF.setForeground(Color.WHITE);
        btnGenerarPDF.setEnabled(false); // Se habilita tras previsualizar
        btnGenerarPDF.addActionListener(e -> exportarPDF());

        panelControles.add(lblTipo);
        panelControles.add(cmbTipoReporte);
        panelControles.add(panelFiltros);
        panelControles.add(btnPrevisualizar);
        panelControles.add(btnGenerarPDF);

        panelCabecera.add(panelControles, BorderLayout.CENTER);
        this.add(panelCabecera, BorderLayout.NORTH);

        // VISTA PREVIA (TABLA)
        modeloTabla = new DefaultTableModel();
        tablaVistaPrevia = new JTable(modeloTabla);
        tablaVistaPrevia.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tablaVistaPrevia.setRowHeight(25);
        
        JScrollPane scrollTabla = new JScrollPane(tablaVistaPrevia);
        scrollTabla.setBorder(BorderFactory.createEmptyBorder(10, 20, 20, 20));
        this.add(scrollTabla, BorderLayout.CENTER);

        // Iniciar con filtros correctos
        cambiarFiltros();
    }

    private void cambiarFiltros() {
        panelFiltros.removeAll();
        int index = cmbTipoReporte.getSelectedIndex();
        
        if (index == 0) { // Caja Diario
            panelFiltros.add(new JLabel("Fecha (YYYY-MM-DD):"));
            txtFechaDiaria = new JTextField(new SimpleDateFormat("yyyy-MM-dd").format(new Date()), 10);
            panelFiltros.add(txtFechaDiaria);
        } 
        else if (index == 1) { // Detallado Ventas
            panelFiltros.add(new JLabel("Desde (YYYY-MM-DD):"));
            txtFechaDesde = new JTextField(new SimpleDateFormat("yyyy-MM-01").format(new Date()), 8);
            panelFiltros.add(txtFechaDesde);
            
            panelFiltros.add(new JLabel("Hasta:"));
            txtFechaHasta = new JTextField(new SimpleDateFormat("yyyy-MM-dd").format(new Date()), 8);
            panelFiltros.add(txtFechaHasta);
        }
        // Inventario y Alertas no necesitan filtros extras por ahora

        panelFiltros.revalidate();
        panelFiltros.repaint();
        btnGenerarPDF.setEnabled(false);
        modeloTabla.setRowCount(0);
        modeloTabla.setColumnCount(0);
    }

    private void previsualizarReporte() {
        int index = cmbTipoReporte.getSelectedIndex();
        
        if (index == 0) {
            String fecha = txtFechaDiaria.getText().trim();
            ultimasColumnas = new String[]{"ID Caja", "Fecha", "Saldo Inicial", "Ingresos", "Egresos", "Saldo Final", "Estado"};
            ultimosDatos = reportesDAO.obtenerReporteCajaDiario(fecha);
            ultimoTitulo = "Reporte Diario de Caja - " + fecha;
        } 
        else if (index == 1) {
            String fDesde = txtFechaDesde.getText().trim();
            String fHasta = txtFechaHasta.getText().trim();
            ultimasColumnas = new String[]{"Fecha", "Ticket", "Tipo Venta", "Producto", "Cantidad", "Subtotal (L.)"};
            ultimosDatos = reportesDAO.obtenerReporteDetalladoVentas(fDesde, fHasta);
            ultimoTitulo = "Reporte Detallado de Ventas (" + fDesde + " al " + fHasta + ")";
        }
        else if (index == 2) {
            ultimasColumnas = new String[]{"Código", "Producto", "Stock Actual", "Precio Venta (L.)"};
            ultimosDatos = reportesDAO.obtenerReporteInventario();
            ultimoTitulo = "Reporte Dinámico de Inventario";
        }
        else if (index == 3) {
            ultimasColumnas = new String[]{"Código", "Producto", "Stock Actual", "Última Venta", "Motivo de Alerta"};
            ultimosDatos = reportesDAO.obtenerAlertasStockYEstancados();
            ultimoTitulo = "Alertas de Stock y Productos Estancados (> 15 días)";
        }

        // Cargar en tabla
        modeloTabla.setColumnIdentifiers(ultimasColumnas);
        modeloTabla.setRowCount(0);
        for (Object[] fila : ultimosDatos) {
            Object[] filaFormateada = new Object[fila.length];
            for (int i = 0; i < fila.length; i++) {
                if (fila[i] instanceof Double || fila[i] instanceof Float) {
                    filaFormateada[i] = String.format("%,.2f", fila[i]);
                } else {
                    filaFormateada[i] = fila[i];
                }
            }
            modeloTabla.addRow(filaFormateada);
        }

        if (ultimosDatos.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No se encontraron datos para los filtros seleccionados.", "Sin Resultados", JOptionPane.INFORMATION_MESSAGE);
            btnGenerarPDF.setEnabled(false);
        } else {
            btnGenerarPDF.setEnabled(true);
        }
    }

    private void exportarPDF() {
        try {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Guardar Reporte PDF");
            
            String tituloLimpio = ultimoTitulo.replaceAll("[\\\\/:*?\"<>|]", "-").replaceAll(" ", "_");
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String nombreSugerido = "Reporte_" + tituloLimpio + "_" + timestamp + ".pdf";
            
            fileChooser.setSelectedFile(new File(nombreSugerido));
            
            int userSelection = fileChooser.showSaveDialog(this);
            if (userSelection == JFileChooser.APPROVE_OPTION) {
                File archivoDestino = fileChooser.getSelectedFile();
                if (!archivoDestino.getName().toLowerCase().endsWith(".pdf")) {
                    archivoDestino = new File(archivoDestino.getParentFile(), archivoDestino.getName() + ".pdf");
                }
                
                File pdf = GeneradorReportesPDF.generarReporte(ultimoTitulo, ultimasColumnas, ultimosDatos, archivoDestino);
                if (pdf != null && pdf.exists()) {
                    if (Desktop.isDesktopSupported()) {
                        Desktop.getDesktop().open(pdf);
                    }
                    JOptionPane.showMessageDialog(this, "Reporte exportado exitosamente a PDF.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error al generar el PDF: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
