package gui;

import dao.ReportesDAO;
import utilidades.GeneradorReportesPDF;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import com.toedter.calendar.JDateChooser;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class PanelReportes extends JPanel {

    private JComboBox<String> cmbTipoReporte;
    private JPanel panelFiltros;
    private JButton btnGenerarPDF;
    private JButton btnEnviarCorreo;
    private JButton btnEnviarWhatsapp;
    private JTable tablaVistaPrevia;
    private DefaultTableModel modeloTabla;

    // Filtros específicos
    private JDateChooser dpFechaDiaria;
    private JDateChooser dpFechaDesde;
    private JDateChooser dpFechaHasta;

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

        btnEnviarCorreo = utilidades.EfectosUI.crearBotonBlanco("Enviar Correo");
        btnEnviarCorreo.setEnabled(false);
        btnEnviarCorreo.addActionListener(e -> enviarPorCorreo());

        btnEnviarWhatsapp = utilidades.EfectosUI.crearBotonVerde("Enviar WhatsApp");
        btnEnviarWhatsapp.setEnabled(false);
        btnEnviarWhatsapp.addActionListener(e -> enviarPorWhatsapp());

        panelControles.add(lblTipo);
        panelControles.add(cmbTipoReporte);
        panelControles.add(panelFiltros);
        panelControles.add(btnPrevisualizar);
        panelControles.add(btnGenerarPDF);
        panelControles.add(btnEnviarCorreo);
        panelControles.add(btnEnviarWhatsapp);

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
            panelFiltros.add(new JLabel("Fecha:"));
            dpFechaDiaria = new JDateChooser(new Date());
            dpFechaDiaria.setDateFormatString("yyyy-MM-dd");
            aplicarEstiloDateChooser(dpFechaDiaria);
            panelFiltros.add(dpFechaDiaria);
        } 
        else if (index == 1) { // Detallado Ventas
            panelFiltros.add(new JLabel("Desde:"));
            dpFechaDesde = new JDateChooser(new Date());
            dpFechaDesde.setDateFormatString("yyyy-MM-dd");
            aplicarEstiloDateChooser(dpFechaDesde);
            panelFiltros.add(dpFechaDesde);
            
            panelFiltros.add(new JLabel("Hasta:"));
            dpFechaHasta = new JDateChooser(new Date());
            dpFechaHasta.setDateFormatString("yyyy-MM-dd");
            aplicarEstiloDateChooser(dpFechaHasta);
            panelFiltros.add(dpFechaHasta);

            configurarRestriccionFechas();
        }
        // Inventario y Alertas no necesitan filtros extras por ahora

        panelFiltros.revalidate();
        panelFiltros.repaint();
        btnGenerarPDF.setEnabled(false);
        btnEnviarCorreo.setEnabled(false);
        btnEnviarWhatsapp.setEnabled(false);
        modeloTabla.setRowCount(0);
        modeloTabla.setColumnCount(0);
    }

    private void configurarRestriccionFechas() {
        if (dpFechaDesde == null || dpFechaHasta == null) return;

        // Establecer fecha mínima seleccionable en fecha hasta
        Date fechaInicial = dpFechaDesde.getDate();
        if (fechaInicial != null) {
            dpFechaHasta.setMinSelectableDate(truncarInicioDia(fechaInicial));
        }

        // Listener en dpFechaDesde para actualizar el límite mínimo de dpFechaHasta
        dpFechaDesde.getDateEditor().addPropertyChangeListener("date", evt -> {
            Date nuevaDesde = dpFechaDesde.getDate();
            if (nuevaDesde != null) {
                Date minDate = truncarInicioDia(nuevaDesde);
                dpFechaHasta.setMinSelectableDate(minDate);
                Date actualHasta = dpFechaHasta.getDate();
                if (actualHasta != null && truncarInicioDia(actualHasta).before(minDate)) {
                    dpFechaHasta.setDate(nuevaDesde);
                }
            }
        });

        // Listener en dpFechaHasta para evitar fechas anteriores a dpFechaDesde
        dpFechaHasta.getDateEditor().addPropertyChangeListener("date", evt -> {
            Date desde = dpFechaDesde.getDate();
            Date hasta = dpFechaHasta.getDate();
            if (desde != null && hasta != null) {
                Date minDate = truncarInicioDia(desde);
                if (truncarInicioDia(hasta).before(minDate)) {
                    utilidades.Mensajes.showMessageDialog(PanelReportes.this,
                            "La fecha final ('Hasta') no puede ser menor que la primera fecha ('Desde').",
                            "Fecha Inválida",
                            JOptionPane.WARNING_MESSAGE);
                    dpFechaHasta.setDate(desde);
                }
            }
        });

        // Validación al perder el foco al escribir manualmente en dpFechaHasta
        if (dpFechaHasta.getDateEditor() instanceof com.toedter.calendar.JTextFieldDateEditor) {
            com.toedter.calendar.JTextFieldDateEditor editorHasta = (com.toedter.calendar.JTextFieldDateEditor) dpFechaHasta.getDateEditor();
            editorHasta.addFocusListener(new java.awt.event.FocusAdapter() {
                @Override
                public void focusLost(java.awt.event.FocusEvent e) {
                    Date desde = dpFechaDesde.getDate();
                    Date hasta = dpFechaHasta.getDate();
                    if (desde != null) {
                        Date minDate = truncarInicioDia(desde);
                        if (hasta != null && truncarInicioDia(hasta).before(minDate)) {
                            utilidades.Mensajes.showMessageDialog(PanelReportes.this,
                                    "La fecha final ('Hasta') no puede ser menor que la primera fecha ('Desde').\nSe ajustará automáticamente a la fecha inicial.",
                                    "Fecha Inválida",
                                    JOptionPane.WARNING_MESSAGE);
                            dpFechaHasta.setDate(desde);
                        } else if (hasta == null && !editorHasta.getText().trim().isEmpty()) {
                            utilidades.Mensajes.showMessageDialog(PanelReportes.this,
                                    "La fecha ingresada en 'Hasta' es inválida o menor a la fecha inicial ('Desde').\nSe restablecerá a la fecha inicial.",
                                    "Fecha Inválida",
                                    JOptionPane.WARNING_MESSAGE);
                            dpFechaHasta.setDate(desde);
                        }
                    }
                }
            });
        }

        // Validación al perder el foco al escribir manualmente en dpFechaDesde
        if (dpFechaDesde.getDateEditor() instanceof com.toedter.calendar.JTextFieldDateEditor) {
            com.toedter.calendar.JTextFieldDateEditor editorDesde = (com.toedter.calendar.JTextFieldDateEditor) dpFechaDesde.getDateEditor();
            editorDesde.addFocusListener(new java.awt.event.FocusAdapter() {
                @Override
                public void focusLost(java.awt.event.FocusEvent e) {
                    Date desde = dpFechaDesde.getDate();
                    if (desde != null) {
                        Date minDate = truncarInicioDia(desde);
                        dpFechaHasta.setMinSelectableDate(minDate);
                        Date hasta = dpFechaHasta.getDate();
                        if (hasta != null && truncarInicioDia(hasta).before(minDate)) {
                            dpFechaHasta.setDate(desde);
                        }
                    } else if (!editorDesde.getText().trim().isEmpty()) {
                        utilidades.Mensajes.showMessageDialog(PanelReportes.this,
                                "La fecha ingresada en 'Desde' no es válida.\nSe restablecerá a la fecha actual.",
                                "Fecha Inválida",
                                JOptionPane.WARNING_MESSAGE);
                        dpFechaDesde.setDate(new Date());
                        dpFechaHasta.setMinSelectableDate(truncarInicioDia(dpFechaDesde.getDate()));
                    }
                }
            });
        }
    }

    private Date truncarInicioDia(Date fecha) {
        if (fecha == null) return null;
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.setTime(fecha);
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0);
        cal.set(java.util.Calendar.MINUTE, 0);
        cal.set(java.util.Calendar.SECOND, 0);
        cal.set(java.util.Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    private void aplicarEstiloDateChooser(JDateChooser dateChooser) {
        // Estilo general
        dateChooser.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        dateChooser.setBackground(Color.WHITE);

        // Estilo del campo de texto
        com.toedter.calendar.JTextFieldDateEditor editor = (com.toedter.calendar.JTextFieldDateEditor) dateChooser.getDateEditor();
        editor.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        editor.setBackground(Color.WHITE);
        editor.setForeground(utilidades.EfectosUI.COLOR_TEXTO_OSCURO);
        editor.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(utilidades.EfectosUI.COLOR_BORDE, 1),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));

        // Estilo del botón del calendario
        JButton boton = dateChooser.getCalendarButton();
        boton.setBackground(Color.WHITE);
        boton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(utilidades.EfectosUI.COLOR_BORDE, 1),
                BorderFactory.createEmptyBorder(2, 5, 2, 5)
        ));
        boton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        utilidades.EfectosUI.aplicarEfectoHover(boton, Color.WHITE, utilidades.EfectosUI.COLOR_VERDE_CLARO, utilidades.EfectosUI.COLOR_TEXTO_OSCURO, Color.BLACK);
    }

    private void previsualizarReporte() {
        int index = cmbTipoReporte.getSelectedIndex();
        
        if (index == 0) {
            if (dpFechaDiaria == null || dpFechaDiaria.getDate() == null) {
                utilidades.Mensajes.showMessageDialog(this, "Por favor, seleccione una fecha diaria válida.", "Fecha Inválida", JOptionPane.WARNING_MESSAGE);
                return;
            }
            String fecha = new SimpleDateFormat("yyyy-MM-dd").format(dpFechaDiaria.getDate());
            ultimasColumnas = new String[]{"ID Caja", "Fecha", "Saldo Inicial", "Ingresos", "Egresos", "Saldo Final", "Estado"};
            ultimosDatos = reportesDAO.obtenerReporteCajaDiario(fecha);
            ultimoTitulo = "Reporte Diario de Caja - " + fecha;
        } 
        else if (index == 1) {
            if (dpFechaDesde == null || dpFechaDesde.getDate() == null || dpFechaHasta == null || dpFechaHasta.getDate() == null) {
                utilidades.Mensajes.showMessageDialog(this, "Por favor, seleccione un rango de fechas válido.", "Fecha Inválida", JOptionPane.WARNING_MESSAGE);
                return;
            }
            Date fDesdeDate = truncarInicioDia(dpFechaDesde.getDate());
            Date fHastaDate = truncarInicioDia(dpFechaHasta.getDate());
            if (fHastaDate.before(fDesdeDate)) {
                utilidades.Mensajes.showMessageDialog(this, "La fecha final ('Hasta') no puede ser menor que la primera fecha ('Desde').", "Rango de Fechas Inválido", JOptionPane.WARNING_MESSAGE);
                dpFechaHasta.setDate(dpFechaDesde.getDate());
                return;
            }
            String fDesde = new SimpleDateFormat("yyyy-MM-dd").format(dpFechaDesde.getDate());
            String fHasta = new SimpleDateFormat("yyyy-MM-dd").format(dpFechaHasta.getDate());
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
            utilidades.Mensajes.showMessageDialog(this, "No se encontraron datos para los filtros seleccionados.", "Sin Resultados", JOptionPane.INFORMATION_MESSAGE);
            btnGenerarPDF.setEnabled(false);
            btnEnviarCorreo.setEnabled(false);
            btnEnviarWhatsapp.setEnabled(false);
        } else {
            btnGenerarPDF.setEnabled(true);
            btnEnviarCorreo.setEnabled(true);
            btnEnviarWhatsapp.setEnabled(true);
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
                        utilidades.GestorImpresion.procesarImpresion(pdf, utilidades.GestorImpresion.TIPO_A4);
                    }
                    utilidades.Mensajes.showMessageDialog(this, "Reporte exportado exitosamente a PDF.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            utilidades.Mensajes.showMessageDialog(this, "Error al generar el PDF: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private File generarPDFTemporal() throws Exception {
        File dir = new File("reportes/temp");
        if (!dir.exists()) dir.mkdirs();
        
        String tituloLimpio = ultimoTitulo.replaceAll("[\\\\/:*?\"<>|]", "-").replaceAll(" ", "_");
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File archivoTemp = new File(dir, "Reporte_" + tituloLimpio + "_" + timestamp + ".pdf");
        
        return GeneradorReportesPDF.generarReporte(ultimoTitulo, ultimasColumnas, ultimosDatos, archivoTemp);
    }

    private void enviarPorCorreo() {
        JTextField txtEmail = new JTextField(25);
        Object[] message = {
            "Ingrese el correo electrónico del destinatario:", txtEmail
        };
        
        int option = utilidades.Mensajes.showConfirmDialog(this, message, "Enviar Reporte por Correo", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (option == JOptionPane.OK_OPTION) {
            String email = txtEmail.getText().trim();
            if (email.isEmpty() || !email.contains("@")) {
                utilidades.Mensajes.showMessageDialog(this, "Debe ingresar un correo electrónico válido.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            JDialog dlgCarga = new JDialog((java.awt.Frame) SwingUtilities.getWindowAncestor(this), "Enviando Correo", true);
            dlgCarga.setUndecorated(true);
            JPanel pnlCarga = new JPanel(new BorderLayout());
            pnlCarga.setBackground(utilidades.EfectosUI.COLOR_VERDE_PRIMARIO);
            pnlCarga.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.WHITE, 2),
                BorderFactory.createEmptyBorder(20, 30, 20, 30)
            ));
            JLabel lblCarga = new JLabel("Enviando reporte, por favor espere...");
            lblCarga.setFont(new Font("Segoe UI", Font.BOLD, 16));
            lblCarga.setForeground(Color.WHITE);
            pnlCarga.add(lblCarga, BorderLayout.CENTER);
            dlgCarga.add(pnlCarga);
            dlgCarga.pack();
            dlgCarga.setLocationRelativeTo(this);
            
            SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
                @Override
                protected Boolean doInBackground() throws Exception {
                    File pdfTemp = generarPDFTemporal();
                    if (pdfTemp == null || !pdfTemp.exists()) return false;
                    return utilidades.EmailSender.enviarReporteConAdjunto(email, pdfTemp.getAbsolutePath(), ultimoTitulo);
                }

                @Override
                protected void done() {
                    dlgCarga.dispose();
                    try {
                        if (get()) {
                            utilidades.Mensajes.showMessageDialog(PanelReportes.this, "¡Excelente! El reporte fue enviado exitosamente a:\n" + email, "Envío Confirmado", JOptionPane.INFORMATION_MESSAGE);
                        } else {
                            utilidades.Mensajes.showMessageDialog(PanelReportes.this, "Hubo un problema al enviar el correo. Revise su conexión a internet o la dirección proporcionada.", "Error de Envío", JOptionPane.ERROR_MESSAGE);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        utilidades.Mensajes.showMessageDialog(PanelReportes.this, "Error interno al enviar: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            };
            worker.execute();
            dlgCarga.setVisible(true); // Bloquea la UI hasta que worker.done() cierre el diálogo
        }
    }

    private void enviarPorWhatsapp() {
        try {
            File pdfTemp = generarPDFTemporal();
            if (pdfTemp == null || !pdfTemp.exists()) {
                utilidades.Mensajes.showMessageDialog(this, "Error al generar el PDF temporal.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Copiar archivo al portapapeles
            java.awt.datatransfer.Clipboard clipboard = java.awt.Toolkit.getDefaultToolkit().getSystemClipboard();
            java.awt.datatransfer.Transferable transferable = new java.awt.datatransfer.Transferable() {
                @Override
                public java.awt.datatransfer.DataFlavor[] getTransferDataFlavors() {
                    return new java.awt.datatransfer.DataFlavor[]{java.awt.datatransfer.DataFlavor.javaFileListFlavor};
                }
                @Override
                public boolean isDataFlavorSupported(java.awt.datatransfer.DataFlavor flavor) {
                    return java.awt.datatransfer.DataFlavor.javaFileListFlavor.equals(flavor);
                }
                @Override
                public Object getTransferData(java.awt.datatransfer.DataFlavor flavor) throws java.awt.datatransfer.UnsupportedFlavorException {
                    if (java.awt.datatransfer.DataFlavor.javaFileListFlavor.equals(flavor)) {
                        java.util.List<File> list = new java.util.ArrayList<>();
                        list.add(pdfTemp);
                        return list;
                    }
                    throw new java.awt.datatransfer.UnsupportedFlavorException(flavor);
                }
            };
            clipboard.setContents(transferable, null);

            // Mostrar instrucción
            int opt = utilidades.Mensajes.showConfirmDialog(this, 
                "El PDF ha sido copiado al portapapeles.\n\nAl presionar Continuar se abrirá WhatsApp Web.\nSolo tienes que elegir a quién enviarlo y presionar Ctrl+V (Pegar).", 
                "Instrucciones de WhatsApp", JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE);
                
            if (opt == JOptionPane.OK_OPTION) {
                if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                    Desktop.getDesktop().browse(new java.net.URI("https://web.whatsapp.com/"));
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            utilidades.Mensajes.showMessageDialog(this, "Error al preparar el envío por WhatsApp: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}

