package utilidades;

import modelo.Empresa;
import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

// Importaciones de iTextPDF para generar y dibujar el documento
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfTemplate;
import com.itextpdf.text.Image;
import com.itextpdf.text.BaseColor;
import java.io.FileOutputStream;
import java.io.File;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.Chunk;

public class GeneradorTickets {

    public static void generarReporteErrorPDF(String id, String fecha, String modulo, String resumen, String stackTrace, String respuestaIA, Empresa empresa) {
        Document documento = new Document();
        try {
            File dir = new File("reportes/errores");
            if (!dir.exists()) dir.mkdirs();
            
            String ruta = "reportes/errores/Reporte_Error_" + id + ".pdf";
            PdfWriter.getInstance(documento, new FileOutputStream(ruta));
            documento.open();

            // Configuración de fuentes premium
            com.itextpdf.text.Font fuenteOrion = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, new BaseColor(34, 153, 84)); // Verde Orion
            com.itextpdf.text.Font fuenteTitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, new BaseColor(44, 62, 80)); // Azul oscuro
            com.itextpdf.text.Font fuenteSubTitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, new BaseColor(44, 62, 80));
            com.itextpdf.text.Font fuenteSub = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, BaseColor.DARK_GRAY);
            com.itextpdf.text.Font fuenteNormal = FontFactory.getFont(FontFactory.HELVETICA, 10, BaseColor.BLACK);
            com.itextpdf.text.Font fuenteConsole = FontFactory.getFont(FontFactory.COURIER, 9, new BaseColor(171, 178, 185)); // Gris claro para consola

            // 1. Encabezado Orion Systems y Logo de Empresa
            PdfPTable headerTable = new PdfPTable(2);
            headerTable.setWidthPercentage(100);
            headerTable.setWidths(new float[]{3f, 1f});

            // Logo de Orion Systems (izquierda)
            PdfPCell cellOrion = new PdfPCell();
            cellOrion.setBorder(Rectangle.NO_BORDER);
            cellOrion.setVerticalAlignment(Element.ALIGN_MIDDLE);
            try {
                Image logoOrion = Image.getInstance("src/image/logo.png");
                logoOrion.scaleToFit(140, 50);
                cellOrion.addElement(logoOrion);
            } catch (Exception e) {
                cellOrion.addElement(new Paragraph("ORION SYSTEMS\nSistema de Organización de Recursos", fuenteOrion));
            }
            headerTable.addCell(cellOrion);

            if (empresa != null && empresa.getImagen_logo() != null && !empresa.getImagen_logo().isEmpty()) {
                try {
                    Image logo;
                    String logoStr = empresa.getImagen_logo();
                    if (logoStr.startsWith("data:image/") || logoStr.length() > 500) {
                        String base64Data = logoStr;
                        if (logoStr.contains(",")) {
                            base64Data = logoStr.split(",")[1];
                        }
                        byte[] imageBytes = java.util.Base64.getDecoder().decode(base64Data);
                        logo = Image.getInstance(imageBytes);
                    } else {
                        logo = Image.getInstance(logoStr);
                    }
                    
                    logo.scaleToFit(80, 80);
                    PdfPCell cellLogo = new PdfPCell(logo);
                    cellLogo.setBorder(Rectangle.NO_BORDER);
                    cellLogo.setHorizontalAlignment(Element.ALIGN_RIGHT);
                    headerTable.addCell(cellLogo);
                } catch (Exception e) {
                    try {
                        Image logoAlt = Image.getInstance("src/image/logo_inversionesOlvan_sinFondo.png");
                        logoAlt.scaleToFit(80, 80);
                        PdfPCell cellLogoAlt = new PdfPCell(logoAlt);
                        cellLogoAlt.setBorder(Rectangle.NO_BORDER);
                        cellLogoAlt.setHorizontalAlignment(Element.ALIGN_RIGHT);
                        headerTable.addCell(cellLogoAlt);
                    } catch (Exception ex) {
                        PdfPCell cellEmpty = new PdfPCell(new Paragraph(""));
                        cellEmpty.setBorder(Rectangle.NO_BORDER);
                        headerTable.addCell(cellEmpty);
                    }
                }
            } else {
                try {
                    Image logoAlt = Image.getInstance("src/image/logo_inversionesOlvan_sinFondo.png");
                    logoAlt.scaleToFit(80, 80);
                    PdfPCell cellLogoAlt = new PdfPCell(logoAlt);
                    cellLogoAlt.setBorder(Rectangle.NO_BORDER);
                    cellLogoAlt.setHorizontalAlignment(Element.ALIGN_RIGHT);
                    headerTable.addCell(cellLogoAlt);
                } catch (Exception ex) {
                    PdfPCell cellEmpty = new PdfPCell(new Paragraph(""));
                    cellEmpty.setBorder(Rectangle.NO_BORDER);
                    headerTable.addCell(cellEmpty);
                }
            }
            documento.add(headerTable);
            documento.add(new Paragraph("\n"));

            // 2. Título Principal y Datos del Cliente
            String nombreEmpresa = empresa != null ? empresa.getNombreEmpresa() : "Empresa No Especificada";
            Paragraph titulo = new Paragraph("REPORTE TÉCNICO DE INCIDENCIA", fuenteTitulo);
            titulo.setAlignment(Element.ALIGN_CENTER);
            documento.add(titulo);
            
            Paragraph subTitulo = new Paragraph("Cliente: " + nombreEmpresa, fuenteSubTitulo);
            subTitulo.setAlignment(Element.ALIGN_CENTER);
            documento.add(subTitulo);
            documento.add(new Paragraph("\n"));

            // 3. Detalles del Error en Caja Gris
            PdfPTable tablaDetalles = new PdfPTable(1);
            tablaDetalles.setWidthPercentage(100);
            PdfPCell celdaDetalles = new PdfPCell();
            celdaDetalles.setBackgroundColor(new BaseColor(242, 243, 244)); // Gris muy claro
            celdaDetalles.setBorderColor(new BaseColor(189, 195, 199));
            celdaDetalles.setPadding(10);
            celdaDetalles.addElement(new Paragraph("ID de Incidencia: " + id, fuenteNormal));
            celdaDetalles.addElement(new Paragraph("Fecha y Hora: " + fecha, fuenteNormal));
            celdaDetalles.addElement(new Paragraph("Módulo / Origen: " + modulo, fuenteNormal));
            celdaDetalles.addElement(new Paragraph("Resumen del Error: " + resumen, fuenteNormal));
            tablaDetalles.addCell(celdaDetalles);
            documento.add(tablaDetalles);
            documento.add(new Paragraph("\n"));

            // 4. Solución IA en Caja Verde Claro
            // La IA envía el texto puro ahora extraído directamente del Document de JEditorPane,
            // pero limpiamos cualquier salto doble exagerado.
            String textoIA = respuestaIA.replace("\r", "").replaceAll("\n{3,}", "\n\n").trim();

            PdfPTable tablaContenedorIA = new PdfPTable(1);
            tablaContenedorIA.setWidthPercentage(100);
            tablaContenedorIA.setKeepTogether(true); // Mantener título y tabla en la misma página

            PdfPCell celdaTituloIA = new PdfPCell(new Paragraph("SOLUCIÓN PROPUESTA POR INTELIGENCIA ARTIFICIAL (GEMINI):\n\n", fuenteSub));
            celdaTituloIA.setBorder(Rectangle.NO_BORDER);
            tablaContenedorIA.addCell(celdaTituloIA);
            
            PdfPCell celdaIA = new PdfPCell(new Paragraph(textoIA, fuenteNormal));
            celdaIA.setBackgroundColor(new BaseColor(234, 250, 234)); // Verde ultra claro
            celdaIA.setBorderColor(new BaseColor(169, 223, 191));
            celdaIA.setPadding(15);
            tablaContenedorIA.addCell(celdaIA);
            
            documento.add(tablaContenedorIA);
            documento.add(new Paragraph("\n"));

            // 5. StackTrace estilo Consola
            documento.add(new Paragraph("STACKTRACE (DETALLE TÉCNICO):", fuenteSub));
            documento.add(new Paragraph("\n"));
            
            PdfPTable tablaConsola = new PdfPTable(1);
            tablaConsola.setWidthPercentage(100);
            PdfPCell celdaConsola = new PdfPCell(new Paragraph(stackTrace, fuenteConsole));
            celdaConsola.setBackgroundColor(new BaseColor(40, 44, 52)); // Gris oscuro estilo VSCode
            celdaConsola.setBorderColor(BaseColor.BLACK);
            celdaConsola.setPadding(15);
            tablaConsola.addCell(celdaConsola);
            documento.add(tablaConsola);

            documento.close();
            
            java.awt.Desktop.getDesktop().open(new File(ruta));
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error al generar reporte PDF: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Construye la vista previa del ticket para la interfaz gr\u00E1fica.
     * Ahora estructurado exactamente igual que el PDF generado.
     */
    /**
     * VISTA PREVIA EDITABLE: Usado exclusivamente por PanelConfiguracionImpresion
     * para mostrar datos ficticios y permitir editar el pie de p\u00E1gina.
     */
    /**
     * VISTA PREVIA EDITABLE: Usado exclusivamente por PanelConfiguracionImpresion
     * para mostrar datos ficticios y permitir editar el pie de p\u00E1gina.
     */
    /**
     * VISTA PREVIA EDITABLE: Usado exclusivamente por PanelConfiguracionImpresion
     * para mostrar datos ficticios y permitir editar el pie de p\u00E1gina.
     */
    public static JPanel crearTicketVistaPrevia(String tituloDocumento, JTextArea txtAreaEditable) {
        return crearTicketVistaPrevia(tituloDocumento, txtAreaEditable, null);
    }

    public static JPanel crearTicketVistaPrevia(String tituloDocumento, JTextArea txtAreaEditable, Empresa empresaPersonalizada) {
        JPanel panelCentrador = new JPanel(new GridBagLayout());
        panelCentrador.setBackground(new Color(30, 30, 30));

        JPanel pnlTicket = new JPanel();
        pnlTicket.setLayout(new BoxLayout(pnlTicket, BoxLayout.Y_AXIS));
        pnlTicket.setBackground(Color.WHITE);
        pnlTicket.setPreferredSize(new Dimension(300, 600));
        pnlTicket.setMinimumSize(new Dimension(300, 600));
        pnlTicket.setMaximumSize(new Dimension(300, 600));

        pnlTicket.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
                BorderFactory.createEmptyBorder(20, 15, 20, 15)));

        Font fBold = new Font("Courier New", Font.BOLD, 13);
        Font fNormal = new Font("Courier New", Font.PLAIN, 12);
        Font fTitulo = new Font("Courier New", Font.BOLD, 16);

        modelo.Empresa emp = empresaPersonalizada != null ? empresaPersonalizada : utilidades.SesionGlobal.getEmpresaActual();

        String empNombre = emp != null && emp.getNombreEmpresa() != null ? emp.getNombreEmpresa().toUpperCase()
                : "INVERSIONES OLVAN";
        String empDueño = emp != null && emp.getDuenoEmpresa() != null ? "Prop: " + emp.getDuenoEmpresa() : "";
        String empRtn = emp != null && emp.getRtnEmpresa() != null ? "RTN: " + emp.getRtnEmpresa() : "RTN: PENDIENTE";

        pnlTicket.add(crearLabelCentrado(empNombre, fTitulo, Color.BLACK));
        pnlTicket.add(Box.createVerticalStrut(5));

        if (!empDue\u00F1o.isEmpty())
            pnlTicket.add(crearLabelCentrado(empDue\u00F1o, fBold, Color.DARK_GRAY));
        pnlTicket.add(crearLabelCentrado(empRtn, fBold, Color.DARK_GRAY));
        pnlTicket.add(crearLabelCentrado("===============================", fNormal, Color.BLACK));

        if (emp != null) {
            if (emp.getDireccionEmpresa() != null && !emp.getDireccionEmpresa().isEmpty())
                pnlTicket.add(crearLabelConIcono("dir", emp.getDireccionEmpresa(), fNormal, Color.BLACK));
            if (emp.getNumeroTelefono() != null && !emp.getNumeroTelefono().isEmpty())
                pnlTicket.add(crearLabelConIcono("tel", emp.getNumeroTelefono(), fNormal, Color.BLACK));
            if (emp.getTelefonoSecundario() != null && !emp.getTelefonoSecundario().isEmpty())
                pnlTicket.add(crearLabelConIcono("tel", emp.getTelefonoSecundario(), fNormal, Color.BLACK));
            if (emp.getWhatsapp() != null && !emp.getWhatsapp().isEmpty())
                pnlTicket.add(crearLabelConIcono("wa", emp.getWhatsapp(), fNormal, Color.BLACK));
            if (emp.getFacebook() != null && !emp.getFacebook().isEmpty())
                pnlTicket.add(crearLabelConIcono("fb", emp.getFacebook(), fNormal, Color.BLACK));
            if (emp.getEmail() != null && !emp.getEmail().isEmpty())
                pnlTicket.add(crearLabelConIcono("mail", emp.getEmail(), fNormal, Color.BLACK));
            if (emp.getWeb() != null && !emp.getWeb().isEmpty())
                pnlTicket.add(crearLabelConIcono("web", emp.getWeb(), fNormal, Color.BLACK));
        }

        pnlTicket.add(Box.createVerticalStrut(10));
        pnlTicket.add(crearLabelCentrado(tituloDocumento, fBold, Color.BLACK));
        pnlTicket.add(Box.createVerticalStrut(10));

        pnlTicket.add(crearLabelCentrado("Cliente: CONSUMIDOR FINAL", fNormal, Color.BLACK));
        pnlTicket.add(crearLabelCentrado("Fecha: 01/01/2026 12:00", fNormal, Color.BLACK));
        pnlTicket.add(crearLabelCentrado("Pago via: Efectivo", fNormal, Color.BLACK));
        pnlTicket.add(crearLabelCentrado("-------------------------------", fNormal, Color.BLACK));
        pnlTicket.add(crearLabelCentrado("C. DESCRIPCION        TOTAL", fNormal, Color.BLACK));
        pnlTicket.add(crearLabelCentrado("-------------------------------", fNormal, Color.BLACK));
        pnlTicket.add(crearLabelCentrado("1  Reemplazo LCD     1500.00", fNormal, Color.BLACK));
        pnlTicket.add(crearLabelCentrado("   S/N: 35848291039384", fNormal, Color.DARK_GRAY));
        pnlTicket.add(crearLabelCentrado("   Gtia: 30 dias (Vence 31/01)", fNormal, Color.DARK_GRAY));
        pnlTicket.add(crearLabelCentrado("1  Mantenimiento      500.00", fNormal, Color.BLACK));
        pnlTicket.add(crearLabelCentrado("-------------------------------", fNormal, Color.BLACK));

        JPanel pnlTotales = new JPanel(new GridLayout(3, 1));
        pnlTotales.setOpaque(false);
        pnlTotales.add(crearLabelAlineadoDer("SUBTOTAL: L  1739.13", fBold));
        pnlTotales.add(crearLabelAlineadoDer("I.S.V (15%): L   260.87", fBold));
        pnlTotales.add(crearLabelAlineadoDer("TOTAL A PAGAR: L  2000.00", fBold));

        pnlTotales.setMaximumSize(new Dimension(250, 60));
        pnlTicket.add(pnlTotales);

        pnlTicket.add(crearLabelCentrado("===============================", fNormal, Color.BLACK));
        pnlTicket.add(Box.createVerticalStrut(10));

        // El TextArea editable se inyecta al final
        txtAreaEditable.setAlignmentX(Component.CENTER_ALIGNMENT);
        pnlTicket.add(txtAreaEditable);

        panelCentrador.add(pnlTicket);
        return panelCentrador;
    }

    public static JPanel crearTicketVistaPrevia(String tituloDocumento, String cliente, String fecha,
            java.util.List<Object[]> detalles, double subtotal, double isv, double total, String metodoPago,
            String refPago, String bancoPago) {
        JPanel panelCentrador = new JPanel(new GridBagLayout());
        panelCentrador.setBackground(new Color(30, 30, 30));

        JPanel pnlTicket = new JPanel();
        pnlTicket.setLayout(new BoxLayout(pnlTicket, BoxLayout.Y_AXIS));
        pnlTicket.setBackground(Color.WHITE);
        // Aumentamos el tama\u00F1o para que quepa toda la nueva cabecera
        pnlTicket.setPreferredSize(new Dimension(300, 600));
        pnlTicket.setMinimumSize(new Dimension(300, 600));
        pnlTicket.setMaximumSize(new Dimension(300, 600));

        pnlTicket.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
                BorderFactory.createEmptyBorder(20, 15, 20, 15)));

        Font fBold = new Font("Courier New", Font.BOLD, 13);
        Font fNormal = new Font("Courier New", Font.PLAIN, 12);
        Font fTitulo = new Font("Courier New", Font.BOLD, 16);

        modelo.Empresa emp = SesionGlobal.getEmpresaActual();

        // --- 1. EXTRACCI\u00D3N EXACTA COMO EN EL PDF ---
        String empNombre = emp != null && emp.getNombreEmpresa() != null ? emp.getNombreEmpresa().toUpperCase()
                : "INVERSIONES OLVAN";
        String empDue\u00F1o = emp != null && emp.getDuenoEmpresa() != null ? "Prop: " + emp.getDuenoEmpresa() : "";
        String empRtn = emp != null && emp.getRtnEmpresa() != null ? "RTN: " + emp.getRtnEmpresa() : "RTN: PENDIENTE";

        pnlTicket.add(crearLabelCentrado(empNombre, fTitulo, Color.BLACK));
        pnlTicket.add(Box.createVerticalStrut(5));

        if (!empDue\u00F1o.isEmpty())
            pnlTicket.add(crearLabelCentrado(empDue\u00F1o, fBold, Color.DARK_GRAY));
        pnlTicket.add(crearLabelCentrado(empRtn, fBold, Color.DARK_GRAY));
        pnlTicket.add(crearLabelCentrado("===============================", fNormal, Color.BLACK));

        // --- 2. DIBUJO DE \u00CDCONOS VECTORIALES CON JAVA 2D ---
        if (emp != null) {
            if (emp.getDireccionEmpresa() != null && !emp.getDireccionEmpresa().isEmpty())
                pnlTicket.add(crearLabelConIcono("dir", emp.getDireccionEmpresa(), fNormal, Color.BLACK));
            if (emp.getNumeroTelefono() != null && !emp.getNumeroTelefono().isEmpty())
                pnlTicket.add(crearLabelConIcono("tel", emp.getNumeroTelefono(), fNormal, Color.BLACK));
            if (emp.getTelefonoSecundario() != null && !emp.getTelefonoSecundario().isEmpty())
                pnlTicket.add(crearLabelConIcono("tel", emp.getTelefonoSecundario(), fNormal, Color.BLACK));
            if (emp.getWhatsapp() != null && !emp.getWhatsapp().isEmpty())
                pnlTicket.add(crearLabelConIcono("wa", emp.getWhatsapp(), fNormal, Color.BLACK));
            if (emp.getFacebook() != null && !emp.getFacebook().isEmpty())
                pnlTicket.add(crearLabelConIcono("fb", emp.getFacebook(), fNormal, Color.BLACK));
            if (emp.getEmail() != null && !emp.getEmail().isEmpty())
                pnlTicket.add(crearLabelConIcono("mail", emp.getEmail(), fNormal, Color.BLACK));
            if (emp.getWeb() != null && !emp.getWeb().isEmpty())
                pnlTicket.add(crearLabelConIcono("web", emp.getWeb(), fNormal, Color.BLACK));
        }

        // --- 3. DATOS REALES ASIGNADOS HIST\u00D3RICAMENTE ---
        pnlTicket.add(crearLabelCentrado("Cliente: " + cliente, fNormal, Color.BLACK));
        pnlTicket.add(crearLabelCentrado("Fecha: " + fecha, fNormal, Color.BLACK));

        if (metodoPago != null)
            pnlTicket.add(crearLabelCentrado("Pago via: " + metodoPago, fNormal, Color.BLACK));
        if (bancoPago != null && !bancoPago.isEmpty())
            pnlTicket.add(crearLabelCentrado("Banco: " + bancoPago, fNormal, Color.BLACK));
        if (refPago != null && !refPago.isEmpty())
            pnlTicket.add(crearLabelCentrado("Ref/Voucher: " + refPago, fNormal, Color.BLACK));

        // Bucle para pintar los productos reales
        for (Object[] fila : detalles) {
            String imei = (fila[1] != null) ? fila[1].toString() : "";
            String nom = fila[2].toString();
            int cant = (int) fila[3];
            double totalFila = (double) fila[5];
            int diasGarantia = (fila.length > 6 && fila[6] != null) ? (int) fila[6] : 0;

            // Nombre abreviado a 15 caracteres para que cuadre en la vista
            String nomCorto = nom.length() > 15 ? nom.substring(0, 15) : String.format("%-15s", nom);
            pnlTicket.add(crearLabelCentrado(String.format("%-2s %s %,6.2f", cant, nomCorto, totalFila), fNormal,
                    Color.BLACK));

            if (!imei.isEmpty())
                pnlTicket.add(crearLabelCentrado("   S/N: " + imei, fNormal, Color.DARK_GRAY));
            if (diasGarantia > 0) {
                java.util.Calendar cal = java.util.Calendar.getInstance();
                cal.add(java.util.Calendar.DAY_OF_YEAR, diasGarantia);
                pnlTicket.add(crearLabelCentrado("   Gtia: " + diasGarantia + " dias", fNormal, Color.DARK_GRAY));
            }
        }
        pnlTicket.add(crearLabelCentrado("-------------------------------", fNormal, Color.BLACK));

        JPanel pnlTotales = new JPanel(new GridLayout(3, 1));
        pnlTotales.setOpaque(false);
        pnlTotales.add(crearLabelAlineadoDer(String.format("SUBTOTAL: L %,8.2f", subtotal), fBold));
        pnlTotales.add(crearLabelAlineadoDer(String.format("I.S.V (15%%): L %,8.2f", isv), fBold));
        pnlTotales.add(crearLabelAlineadoDer(String.format("TOTAL: L %,8.2f", total), fBold));

        pnlTotales.setMaximumSize(new Dimension(250, 60));
        pnlTicket.add(pnlTotales);

        pnlTicket.add(crearLabelCentrado("===============================", fNormal, Color.BLACK));
        pnlTicket.add(Box.createVerticalStrut(10));

        panelCentrador.add(pnlTicket);
        return panelCentrador;
    }

    /**
     * Helper para alinear los totales a la derecha simulando el PDF
     */
    private static JLabel crearLabelAlineadoDer(String texto, Font fuente) {
        JLabel lbl = new JLabel(texto, SwingConstants.RIGHT);
        lbl.setFont(fuente);
        lbl.setForeground(Color.BLACK);
        lbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        return lbl;
    }

    public static JLabel crearLabelCentrado(String texto, Font fuente, Color color) {
        JLabel lbl = new JLabel(texto);
        lbl.setFont(fuente);
        lbl.setForeground(color);
        lbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        return lbl;
    }

    /**
     * Genera el archivo PDF f\u00EDsico del ticket de venta utilizando iTextPDF.
     * Incorpora dibujos vectoriales para las redes sociales y salto de l\u00EDnea
     * en descripciones largas.
     */
    public static void generarTicketVentaPDF(String rutaDestino, String nombreCliente, String fecha,
            java.util.List<Object[]> detalles, double subtotal, double isv, double total, boolean esFactura,
            String metodoPago, String referenciaPago, String bancoPago) throws Exception {
        generarTicketVentaPDF(rutaDestino, nombreCliente, fecha, detalles, subtotal, isv, total, esFactura, metodoPago, referenciaPago, bancoPago, null);
    }

    public static void generarTicketVentaPDF(String rutaDestino, String nombreCliente, String fecha,
            java.util.List<Object[]> detalles, double subtotal, double isv, double total, boolean esFactura,
            String metodoPago, String referenciaPago, String bancoPago, java.util.List<modelo.AbonoApartado> abonosHistoricos) throws Exception {
        Rectangle tamanoTicket = new Rectangle(226, 800); // Formato de ticket t\u00E9rmico 80mm
        Document documento = new Document(tamanoTicket, 10, 10, 10, 10);
        PdfWriter writer = PdfWriter.getInstance(documento, new FileOutputStream(rutaDestino));
        documento.open();

        com.itextpdf.text.Font fNormal = FontFactory.getFont(FontFactory.COURIER, 9);
        com.itextpdf.text.Font fBold = FontFactory.getFont(FontFactory.COURIER_BOLD, 9);
        com.itextpdf.text.Font fTitulo = FontFactory.getFont(FontFactory.COURIER_BOLD, 12);

        Empresa emp = SesionGlobal.getEmpresaActual();

        String empNombre = emp != null && emp.getNombreEmpresa() != null ? emp.getNombreEmpresa().toUpperCase()
                : "INVERSIONES OLVAN";
        String empDue\u00F1o = emp != null && emp.getDuenoEmpresa() != null ? "Prop: " + emp.getDuenoEmpresa() : "";
        String empRtn = emp != null && emp.getRtnEmpresa() != null ? "RTN: " + emp.getRtnEmpresa() : "RTN: PENDIENTE";
        // T\u00EDtulo Principal
        Paragraph parrafoNombre = new Paragraph(empNombre + "\n", fTitulo);
        parrafoNombre.setAlignment(Element.ALIGN_CENTER);
        documento.add(parrafoNombre);

        // Cabecera Texto Plano (Due\u00F1o y RTN)
        Paragraph cabeceraTexto = new Paragraph();
        cabeceraTexto.setAlignment(Element.ALIGN_CENTER);
        cabeceraTexto.setFont(fBold);
        if (!empDue\u00F1o.isEmpty())
            cabeceraTexto.add(new Chunk(empDue\u00F1o + "\n"));
        cabeceraTexto.add(new Chunk(empRtn + "\n===============================\n"));
        documento.add(cabeceraTexto);

        // --- SECCI\u00D3N DE DATOS CON \u00CDCONOS DIBUJADOS ---
        Paragraph cabeceraIconos = new Paragraph();
        cabeceraIconos.setAlignment(Element.ALIGN_CENTER);
        cabeceraIconos.setFont(fNormal);

        if (emp != null) {
            agregarLineaIcono(cabeceraIconos, writer, "dir", emp.getDireccionEmpresa());
            agregarLineaIcono(cabeceraIconos, writer, "tel", emp.getNumeroTelefono());
            agregarLineaIcono(cabeceraIconos, writer, "tel", emp.getTelefonoSecundario());
            agregarLineaIcono(cabeceraIconos, writer, "wa", emp.getWhatsapp());
            agregarLineaIcono(cabeceraIconos, writer, "fb", emp.getFacebook());
            agregarLineaIcono(cabeceraIconos, writer, "mail", emp.getEmail());
            agregarLineaIcono(cabeceraIconos, writer, "web", emp.getWeb());
        }
        documento.add(cabeceraIconos);

        // T\u00EDtulo del Documento
        String tituloDocumento = esFactura ? "FACTURA" : "COMPROBANTE DE VENTA";
        Paragraph tituloDoc = new Paragraph(
                "===============================\n" + tituloDocumento + "\n===============================\n", fBold);
        tituloDoc.setAlignment(Element.ALIGN_CENTER);
        documento.add(tituloDoc);

        // Informaci\u00F3n de Venta y Control de Pago
        StringBuilder sbInfo = new StringBuilder();
        sbInfo.append("Cliente: ").append(nombreCliente).append("\n");

        // CORRECCI\u00D3N: Fijamos la fecha hist\u00F3rica provista por el
        // par\u00E1metro
        sbInfo.append("Fecha: ").append(fecha).append("\n");

        sbInfo.append("Pago via: ").append(metodoPago).append("\n");
        if (bancoPago != null && !bancoPago.trim().isEmpty()) {
            sbInfo.append("Banco: ").append(bancoPago.trim()).append("\n");
        }
        if (referenciaPago != null && !referenciaPago.trim().isEmpty()) {
            sbInfo.append("Ref/Voucher: ").append(referenciaPago.trim()).append("\n");
        }
        sbInfo.append("-------------------------------\n");

        Paragraph info = new Paragraph(sbInfo.toString(), fNormal);
        info.setAlignment(Element.ALIGN_LEFT);
        documento.add(info);

        // --- DETALLES DE PRODUCTOS CON SALTO DE L\u00CDNEA E IDENTIFICADORES ---
        StringBuilder sbDetalles = new StringBuilder();
        sbDetalles.append(String.format("%-2s %-15s %6s\n", "C.", "DESCRIPCION", "TOTAL"));
        sbDetalles.append("-------------------------------\n");

        boolean ticketTieneGarantias = false; // Bandera para saber si imprimimos pol\u00EDticas al final

        for (Object[] fila : detalles) {
            String imei = (fila[1] != null) ? fila[1].toString() : "";
            String nom = fila[2].toString();
            int cant = (int) fila[3];
            double totalFila = (double) fila[5];
            int diasGarantia = (fila.length > 6 && fila[6] != null) ? (int) fila[6] : 0;

            int maxLen = 15;

            // 1. Imprimir Nombre, Cantidad y Precio
            if (nom.length() <= maxLen) {
                sbDetalles.append(String.format("%-2s %-15s %,6.2f\n", cant, nom, totalFila));
            } else {
                sbDetalles.append(String.format("%-2s %-15s %,6.2f\n", cant, nom.substring(0, maxLen), totalFila));
                int index = maxLen;
                while (index < nom.length()) {
                    int end = Math.min(index + maxLen, nom.length());
                    sbDetalles.append(String.format("   %-15s\n", nom.substring(index, end)));
                    index += maxLen;
                }
            }

            // 2. Imprimir Identificador y Vencimiento de Garant\u00EDa (Si Aplica)
            if (!imei.isEmpty() || diasGarantia > 0) {
                ticketTieneGarantias = true;
                if (!imei.isEmpty()) {
                    sbDetalles.append(String.format("   S/N: %s\n", imei));
                }
                if (diasGarantia > 0) {
                    // Calculamos autom\u00E1ticamente la fecha de vencimiento
                    java.util.Calendar cal = java.util.Calendar.getInstance();
                    cal.add(java.util.Calendar.DAY_OF_YEAR, diasGarantia);
                    String fechaVence = new java.text.SimpleDateFormat("dd/MM/yy").format(cal.getTime());
                    sbDetalles.append(String.format("   Gtia: %d dias (Vence %s)\n", diasGarantia, fechaVence));
                }
            }
        }
        sbDetalles.append("-------------------------------\n");
        Paragraph parrafoDetalles = new Paragraph(sbDetalles.toString(), fNormal);
        documento.add(parrafoDetalles);

        // --- ABONOS HISTORICOS (Si es entrega de apartado) ---
        if (abonosHistoricos != null && !abonosHistoricos.isEmpty()) {
            StringBuilder sbAb = new StringBuilder();
            sbAb.append("\n===============================\n");
            sbAb.append("HISTORIAL DE ABONOS\n");
            sbAb.append("===============================\n");
            sbAb.append(String.format("%-11s %-11s %6s\n", "FECHA", "HORA", "MONTO"));
            sbAb.append("-------------------------------\n");
            java.text.SimpleDateFormat sdfDate = new java.text.SimpleDateFormat("dd/MM/yyyy");
            java.text.SimpleDateFormat sdfTime = new java.text.SimpleDateFormat("HH:mm");
            for (modelo.AbonoApartado a : abonosHistoricos) {
                java.sql.Timestamp t = a.getFechaAbono();
                String d = (t != null) ? sdfDate.format(t) : "";
                String h = (t != null) ? sdfTime.format(t) : "";
                sbAb.append(String.format("%-11s %-11s %,6.2f\n", d, h, a.getMontoAbono()));
            }
            sbAb.append("-------------------------------\n\n");
            Paragraph pAbonos = new Paragraph(sbAb.toString(), fNormal);
            documento.add(pAbonos);
        }
        if (ticketTieneGarantias) {
            String textoPoliticas = emp != null && emp.getPoliticasGarantia() != null ? emp.getPoliticasGarantia()
                    : "Conserve este ticket. La garant\u00EDa no aplica por da\u00F1os f\u00EDsicos, humedad, exposici\u00F3n a l\u00EDquidos o manipulaci\u00F3n por terceros.";

            // 1. T\u00EDtulo Centrado y en Negrita (Usa fBold)
            Paragraph tituloGarantia = new Paragraph("--- POLITICAS DE GARANTIA ---\n", fBold);
            tituloGarantia.setAlignment(Element.ALIGN_CENTER);
            documento.add(tituloGarantia);

            // 2. Texto de las pol\u00EDticas Justificado y Normal (Usa fNormal)
            Paragraph parrafoGarantia = new Paragraph(textoPoliticas + "\n\n", fNormal);
            parrafoGarantia.setAlignment(Element.ALIGN_JUSTIFIED);
            documento.add(parrafoGarantia);
        }

        // --- TOTALES FINALES ---
        String etiquetaTotal = (abonosHistoricos != null && !abonosHistoricos.isEmpty()) ? "TOTAL VENTA:" : "TOTAL A PAGAR:";
        String saldoLinea = (abonosHistoricos != null && !abonosHistoricos.isEmpty()) ? String.format("%-15s L %,8.2f\n", "SALDO PENDIENTE:", 0.0) : "";

        Paragraph totales = new Paragraph(
                String.format("%-15s L %,8.2f\n", "SUBTOTAL:", subtotal) +
                        String.format("%-15s L %,8.2f\n", "I.S.V (15%):", isv) +
                        String.format("%-15s L %,8.2f\n", etiquetaTotal, total) +
                        saldoLinea +
                        "\n===============================\n",
                fBold);
        totales.setAlignment(Element.ALIGN_RIGHT);
        documento.add(totales);

        // Pie de P\u00E1gina
        String msjPie = emp != null && emp.getMensajeTicketPieFactura() != null ? emp.getMensajeTicketPieFactura()
                : "\u00A1Gracias por su preferencia!";
        if (!esFactura) {
            msjPie = emp != null && emp.getMensajeTicketPieRecibo() != null ? emp.getMensajeTicketPieRecibo()
                    : "Este documento no es valido como factura.";
        }
        Paragraph pie = new Paragraph(msjPie, fNormal);
        pie.setAlignment(Element.ALIGN_CENTER);
        documento.add(pie);

        documento.close();
    }

    // ==============================================================================
    // MOTOR DE DIBUJO VECTORIAL PARA EL PDF
    // ==============================================================================

    /**
     * Agrega el bloque de \u00EDcono + texto alineado a la cabecera.
     */
    private static void agregarLineaIcono(Paragraph p, PdfWriter writer, String tipo, String texto) throws Exception {
        if (texto != null && !texto.trim().isEmpty()) {
            Image icon = crearIconoVectorial(writer, tipo);
            p.add(new Chunk(icon, 0, -1)); // -1 alinea el icono ligeramente con el texto
            p.add(new Chunk(" " + texto.trim() + "\n"));
        }
    }

    /**
     * Dibuja los iconos literalmente a trav\u00E9s de figuras geom\u00E9tricas
     * usando PdfContentByte.
     */
    private static Image crearIconoVectorial(PdfWriter writer, String tipo) throws Exception {
        PdfContentByte cb = writer.getDirectContent();
        PdfTemplate tp = cb.createTemplate(10, 10);
        tp.setColorStroke(BaseColor.DARK_GRAY);
        tp.setColorFill(BaseColor.DARK_GRAY);
        tp.setLineWidth(0.8f);

        switch (tipo) {
            case "tel":
                // Dibuja un tel\u00E9fono m\u00F3vil cl\u00E1sico (Bordes redondeados y la
                // rayita del altavoz)
                tp.roundRectangle(2, 1, 6, 8, 1);
                tp.stroke();
                tp.moveTo(4, 2);
                tp.lineTo(6, 2);
                tp.stroke();
                break;
            case "wa":
                // Dibuja una burbuja de chat de WhatsApp (C\u00EDrculo con colita)
                tp.circle(5, 5, 4);
                tp.stroke();
                tp.moveTo(2, 3);
                tp.lineTo(1, 1);
                tp.lineTo(3, 2);
                tp.stroke();
                break;
            case "fb":
                // Dibuja la 'f' caracter\u00EDstica de Facebook en un bloque cuadrado
                tp.rectangle(1, 1, 8, 8);
                tp.fill();
                tp.setColorStroke(BaseColor.WHITE);
                tp.setLineWidth(1.2f);
                tp.moveTo(6, 8);
                tp.lineTo(4, 8);
                tp.lineTo(4, 1);
                tp.stroke();
                tp.moveTo(3, 5);
                tp.lineTo(6, 5);
                tp.stroke();
                break;
            case "mail":
                // Dibuja el sobre de un correo electr\u00F3nico
                tp.rectangle(1, 2, 8, 6);
                tp.stroke();
                tp.moveTo(1, 8);
                tp.lineTo(5, 5);
                tp.lineTo(9, 8);
                tp.stroke();
                break;
            case "web":
                // Dibuja un \u00EDcono global (El mundo de internet)
                tp.circle(5, 5, 4);
                tp.stroke();
                tp.moveTo(1, 5);
                tp.lineTo(9, 5);
                tp.stroke();
                tp.moveTo(5, 1);
                tp.lineTo(5, 9);
                tp.stroke();
                break;
            case "dir":
                // Dibuja un pin de ubicaci\u00F3n (Globo con pico hacia abajo)
                tp.circle(5, 7, 2.5f);
                tp.stroke();
                tp.moveTo(3.2f, 5.2f);
                tp.lineTo(5, 1);
                tp.lineTo(6.8f, 5.2f);
                tp.stroke();
                break;
        }
        return Image.getInstance(tp);
    }

    // ==============================================================================
    // DIBUJO DE \u00CDCONOS VECTORIALES PARA LA VISTA PREVIA (INTERFAZ
    // GR\u00C1FICA)
    // ==============================================================================
    private static class IconoVectorialUI implements Icon {
        private String tipo;

        public IconoVectorialUI(String tipo) {
            this.tipo = tipo;
        }

        @Override
        public int getIconWidth() {
            return 16;
        }

        @Override
        public int getIconHeight() {
            return 16;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(Color.DARK_GRAY);
            g2.setStroke(new BasicStroke(1.5f));

            switch (tipo) {
                case "tel":
                    g2.drawRoundRect(x + 4, y + 1, 8, 13, 4, 4);
                    g2.drawLine(x + 6, y + 3, x + 10, y + 3);
                    break;
                case "wa":
                    g2.drawOval(x + 2, y + 2, 11, 11);
                    g2.drawLine(x + 3, y + 11, x + 1, y + 15);
                    g2.drawLine(x + 1, y + 15, x + 5, y + 12);
                    break;
                case "fb":
                    g2.fillRect(x + 2, y + 2, 12, 12);
                    g2.setColor(Color.WHITE);
                    g2.drawLine(x + 8, y + 14, x + 8, y + 4);
                    g2.drawLine(x + 8, y + 4, x + 11, y + 4);
                    g2.drawLine(x + 5, y + 8, x + 11, y + 8);
                    break;
                case "mail":
                    g2.drawRect(x + 1, y + 4, 14, 9);
                    g2.drawLine(x + 1, y + 4, x + 8, y + 9);
                    g2.drawLine(x + 15, y + 4, x + 8, y + 9);
                    break;
                case "web":
                    g2.drawOval(x + 2, y + 2, 12, 12);
                    g2.drawLine(x + 2, y + 8, x + 14, y + 8);
                    g2.drawLine(x + 8, y + 2, x + 8, y + 14);
                    g2.drawOval(x + 5, y + 2, 6, 12);
                    break;
                case "dir":
                    g2.drawOval(x + 4, y + 1, 8, 8);
                    g2.drawLine(x + 5, y + 8, x + 8, y + 14);
                    g2.drawLine(x + 11, y + 8, x + 8, y + 14);
                    break;
            }
            g2.dispose();
        }
    }

    public static JLabel crearLabelConIcono(String tipoIcono, String texto, Font fuente, Color color) {
        JLabel lbl = new JLabel(" " + texto);
        lbl.setIcon(new IconoVectorialUI(tipoIcono));
        lbl.setFont(fuente);
        lbl.setForeground(color);
        lbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        return lbl;
    }

    // ==============================================================================
    // GENERACI\u00D3N AUTOM\u00C1TICA DESDE LA BASE DE DATOS
    // ==============================================================================

    public static java.io.File generarFactura(int idVenta) throws Exception {
        java.io.File dir = new java.io.File("reportes");
        if (!dir.exists())
            dir.mkdirs(); // Crea la carpeta si no existe
        String ruta = "reportes/Recibo_Venta_" + idVenta + ".pdf";

        String cliente = "CONSUMIDOR FINAL";
        String fecha = "";
        double subtotal = 0, isv = 0, total = 0;
        String metodo = "Efectivo", ref = "", banco = "";
        java.util.List<Object[]> detalles = new java.util.ArrayList<>();

        factory.ConexionFactory cf = new factory.ConexionFactory();
        try (Connection con = cf.getConexion()) {
            // 1. Cabecera de la venta
            String sqlVenta = "SELECT v.fecha_venta, v.subtotal_venta, v.impuesto_venta, v.total_venta, " +
                    "v.referencia_pago, v.banco_pago, c.nombre_cliente, c.apellido_cliente " +
                    "FROM VENTAS v LEFT JOIN CLIENTES c ON v.id_cliente_venta = c.id_cliente " +
                    "WHERE v.id_ventas = ?";
            try (PreparedStatement ps = con.prepareStatement(sqlVenta)) {
                ps.setInt(1, idVenta);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    if (rs.getString("nombre_cliente") != null) {
                        cliente = rs.getString("nombre_cliente") + " "
                                + (rs.getString("apellido_cliente") != null ? rs.getString("apellido_cliente") : "");
                    }
                    fecha = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm").format(rs.getTimestamp("fecha_venta"));
                    subtotal = rs.getDouble("subtotal_venta");
                    isv = rs.getDouble("impuesto_venta");
                    total = rs.getDouble("total_venta");
                    ref = rs.getString("referencia_pago");
                    banco = rs.getString("banco_pago");
                }
            }

            // 2. Detalles de la factura
            String sqlDet = "SELECT identificador_serie, descripcion_venta, cantidad_venta, precio_unitario_venta, subtotal_venta, dias_garantia "
                    +
                    "FROM DETALLES_VENTA WHERE id_ventas = ?";
            try (PreparedStatement ps = con.prepareStatement(sqlDet)) {
                ps.setInt(1, idVenta);
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    detalles.add(new Object[] {
                            0, // id oculto (no importa aqui)
                            rs.getString("identificador_serie"),
                            rs.getString("descripcion_venta"),
                            rs.getInt("cantidad_venta"),
                            rs.getDouble("precio_unitario_venta"),
                            rs.getDouble("subtotal_venta"),
                            rs.getInt("dias_garantia")
                    });
                }
            }
        }

        // Llamamos a tu m\u00E9todo maestro que ya dibuja el PDF maravillosamente
        if (ref != null && ref.startsWith("Pago de Apartado #")) {
            String idApStr = ref.replaceAll("[^0-9]", "");
            if (!idApStr.isEmpty()) {
                int idApartado = Integer.parseInt(idApStr);
                dao.ApartadoDAO daoApartado = new dao.ApartadoDAO();
                java.util.List<modelo.AbonoApartado> abonos = daoApartado.listarAbonos(idApartado);
                generarTicketVentaPDF(ruta, cliente.trim(), fecha, detalles, subtotal, isv, total, true, metodo, ref, banco, abonos);
                return new java.io.File(ruta);
            }
        }
        
        generarTicketVentaPDF(ruta, cliente.trim(), fecha, detalles, subtotal, isv, total, true, metodo, ref, banco);
        return new java.io.File(ruta);
    }

    public static java.io.File generarCertificadoGarantia(int idDetalleVenta) throws Exception {
        java.io.File dir = new java.io.File("reportes");
        if (!dir.exists())
            dir.mkdirs();
        String ruta = "reportes/Certificado_Garantia_" + idDetalleVenta + ".pdf";

        String cliente = "CONSUMIDOR FINAL";
        String idVentaReal = "";
        String producto = "";
        String serie = "N/A";
        String fechaCompra = "";
        String fechaVence = "";
        int dias = 0;

        factory.ConexionFactory cf = new factory.ConexionFactory();
        try (Connection con = cf.getConexion()) {
            String sql = "SELECT c.nombre_cliente, c.apellido_cliente, d.descripcion_venta, " +
                    "d.identificador_serie, v.fecha_venta, d.dias_garantia, v.id_ventas " +
                    "FROM DETALLES_VENTA d INNER JOIN VENTAS v ON d.id_ventas = v.id_ventas " +
                    "LEFT JOIN CLIENTES c ON v.id_cliente_venta = c.id_cliente " +
                    "WHERE d.id_detalle_venta = ?";
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, idDetalleVenta);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    if (rs.getString("nombre_cliente") != null) {
                        cliente = rs.getString("nombre_cliente") + " "
                                + (rs.getString("apellido_cliente") != null ? rs.getString("apellido_cliente") : "");
                    }
                    idVentaReal = "VN-" + rs.getInt("id_ventas");
                    producto = rs.getString("descripcion_venta");
                    if (rs.getString("identificador_serie") != null
                            && !rs.getString("identificador_serie").trim().isEmpty()) {
                        serie = rs.getString("identificador_serie");
                    }

                    java.sql.Timestamp fVenta = rs.getTimestamp("fecha_venta");
                    dias = rs.getInt("dias_garantia");

                    java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm");
                    fechaCompra = sdf.format(fVenta);

                    java.util.Calendar cal = java.util.Calendar.getInstance();
                    cal.setTime(fVenta);
                    cal.add(java.util.Calendar.DAY_OF_YEAR, dias);
                    java.text.SimpleDateFormat sdfVence = new java.text.SimpleDateFormat("dd/MM/yyyy");
                    fechaVence = sdfVence.format(cal.getTime());
                }
            }
        }

        // --- CONFIGURACI\u00D3N ID\u00C9NTICA AL COMPROBANTE (80mm) ---
        Rectangle tamanoTicket = new Rectangle(226, 800);
        // Usamos los mismos m\u00E1rgenes (10) de tu m\u00E9todo principal
        Document documento = new Document(tamanoTicket, 10, 10, 10, 10);
        PdfWriter writer = PdfWriter.getInstance(documento, new FileOutputStream(ruta));
        documento.open();

        // Mismas fuentes exactas del comprobante
        com.itextpdf.text.Font fNormal = FontFactory.getFont(FontFactory.COURIER, 9);
        com.itextpdf.text.Font fBold = FontFactory.getFont(FontFactory.COURIER_BOLD, 9);
        com.itextpdf.text.Font fTitulo = FontFactory.getFont(FontFactory.COURIER_BOLD, 12);

        Empresa emp = SesionGlobal.getEmpresaActual();
        String empNombre = emp != null && emp.getNombreEmpresa() != null ? emp.getNombreEmpresa().toUpperCase()
                : "ORION SYSTEM";
        String empDue\u00F1o = emp != null && emp.getDuenoEmpresa() != null ? "Prop: " + emp.getDuenoEmpresa() : "";
        String empRtn = emp != null && emp.getRtnEmpresa() != null ? "RTN: " + emp.getRtnEmpresa() : "";

        // 1. T\u00CDTULO PRINCIPAL
        Paragraph parrafoNombre = new Paragraph(empNombre + "\n", fTitulo);
        parrafoNombre.setAlignment(Element.ALIGN_CENTER);
        documento.add(parrafoNombre);

        // 2. DUE\u00D1O Y RTN
        Paragraph cabeceraTexto = new Paragraph();
        cabeceraTexto.setAlignment(Element.ALIGN_CENTER);
        cabeceraTexto.setFont(fBold);
        if (!empDue\u00F1o.isEmpty())
            cabeceraTexto.add(new Chunk(empDue\u00F1o + "\n"));
        if (!empRtn.isEmpty())
            cabeceraTexto.add(new Chunk(empRtn + "\n===============================\n"));
        documento.add(cabeceraTexto);

        // 3. SECCI\u00D3N DE \u00CDCONOS VECTORIALES (Igual que en el recibo)
        Paragraph cabeceraIconos = new Paragraph();
        cabeceraIconos.setAlignment(Element.ALIGN_CENTER);
        cabeceraIconos.setFont(fNormal);
        if (emp != null) {
            agregarLineaIcono(cabeceraIconos, writer, "dir", emp.getDireccionEmpresa());
            agregarLineaIcono(cabeceraIconos, writer, "tel", emp.getNumeroTelefono());
            agregarLineaIcono(cabeceraIconos, writer, "tel", emp.getTelefonoSecundario());
            agregarLineaIcono(cabeceraIconos, writer, "fb", emp.getFacebook());
            agregarLineaIcono(cabeceraIconos, writer, "mail", emp.getEmail());
            agregarLineaIcono(cabeceraIconos, writer, "web", emp.getWeb());
        }
        documento.add(cabeceraIconos);

        // 4. SEPARADOR Y T\u00CDTULO DEL DOCUMENTO
        Paragraph pTitulo = new Paragraph(
                "===============================\nCERTIFICADO DE GARANTIA\n===============================\n", fBold);
        pTitulo.setAlignment(Element.ALIGN_CENTER);
        documento.add(pTitulo);

        // 5. CUERPO DEL DOCUMENTO (Usando la misma est\u00E9tica de guiones)
        StringBuilder sbInfo = new StringBuilder();
        sbInfo.append("Cliente: ").append(cliente.trim()).append("\n");
        sbInfo.append("Factura: ").append(idVentaReal).append("\n");
        sbInfo.append("Fecha:   ").append(fechaCompra).append("\n");
        sbInfo.append("-------------------------------\n");
        sbInfo.append("EQUIPO CUBIERTO\n");
        sbInfo.append("-------------------------------\n");

        Paragraph info1 = new Paragraph(sbInfo.toString(), fNormal);
        info1.setAlignment(Element.ALIGN_LEFT);
        documento.add(info1);

        // Producto y Serie con formato indentado
        Paragraph pProducto = new Paragraph(producto + "\nS/N o IMEI: " + serie + "\n", fNormal);
        pProducto.setAlignment(Element.ALIGN_LEFT);
        documento.add(pProducto);

        StringBuilder sbEstado = new StringBuilder();
        sbEstado.append("-------------------------------\n");
        sbEstado.append("ESTADO DE COBERTURA\n");
        sbEstado.append("-------------------------------\n");
        sbEstado.append("Tiempo:      ").append(dias).append(" dias\n");
        sbEstado.append("Vencimiento: ").append(fechaVence).append("\n");
        sbEstado.append("Estado:      VIGENTE\n");
        sbEstado.append("-------------------------------\n\n");

        Paragraph info2 = new Paragraph(sbEstado.toString(), fNormal);
        info2.setAlignment(Element.ALIGN_LEFT);
        documento.add(info2);

        // 6. POL\u00CDTICAS DE GARANT\u00CDA
        String textoPoliticas = emp != null && emp.getPoliticasGarantia() != null ? emp.getPoliticasGarantia()
                : "Conserve este ticket. La garant\u00EDa no aplica por da\u00F1os f\u00EDsicos, humedad, exposici\u00F3n a l\u00EDquidos o manipulaci\u00F3n por terceros.";

        Paragraph tituloPol = new Paragraph("--- POLITICAS DE GARANTIA ---\n", fBold);
        tituloPol.setAlignment(Element.ALIGN_CENTER);
        documento.add(tituloPol);

        Paragraph pol = new Paragraph(textoPoliticas + "\n\n\n\n", fNormal);
        pol.setAlignment(Element.ALIGN_JUSTIFIED);
        documento.add(pol);

        // 7. FIRMA AL PIE (Opcional, pero le da formalidad)
        Paragraph pFirma = new Paragraph("________________________\nFirma / Sello de Validez", fNormal);
        pFirma.setAlignment(Element.ALIGN_CENTER);
        documento.add(pFirma);

        documento.close();
        return new java.io.File(ruta);
    }

    public static void generarTicketApartadoPDF(String rutaDestino, String nombreCliente, String fecha,
            String fechaLimite, java.util.List<Object[]> detalles, double total, double abono, double saldo,
            String metodoPago) throws Exception {
        Rectangle tamanoTicket = new Rectangle(226, 800); // Formato de ticket t\u00E9rmico 80mm
        Document documento = new Document(tamanoTicket, 10, 10, 10, 10);
        PdfWriter writer = PdfWriter.getInstance(documento, new FileOutputStream(rutaDestino));
        documento.open();

        com.itextpdf.text.Font fNormal = FontFactory.getFont(FontFactory.COURIER, 9);
        com.itextpdf.text.Font fBold = FontFactory.getFont(FontFactory.COURIER_BOLD, 9);
        com.itextpdf.text.Font fTitulo = FontFactory.getFont(FontFactory.COURIER_BOLD, 12);

        Empresa emp = SesionGlobal.getEmpresaActual();

        String empNombre = emp != null && emp.getNombreEmpresa() != null ? emp.getNombreEmpresa().toUpperCase()
                : "INVERSIONES OLVAN";
        String empDue\u00F1o = emp != null && emp.getDuenoEmpresa() != null ? "Prop: " + emp.getDuenoEmpresa() : "";
        String empRtn = emp != null && emp.getRtnEmpresa() != null ? "RTN: " + emp.getRtnEmpresa() : "RTN: PENDIENTE";

        // T\u00EDtulo Principal
        Paragraph parrafoNombre = new Paragraph(empNombre + "\n", fTitulo);
        parrafoNombre.setAlignment(Element.ALIGN_CENTER);
        documento.add(parrafoNombre);

        // Cabecera Texto Plano (Due\u00F1o y RTN)
        Paragraph cabeceraTexto = new Paragraph();
        cabeceraTexto.setAlignment(Element.ALIGN_CENTER);
        cabeceraTexto.setFont(fBold);
        if (!empDue\u00F1o.isEmpty())
            cabeceraTexto.add(new Chunk(empDue\u00F1o + "\n"));
        cabeceraTexto.add(new Chunk(empRtn + "\n===============================\n"));
        documento.add(cabeceraTexto);

        // --- SECCI\u00D3N DE DATOS CON \u00CDCONOS DIBUJADOS ---
        Paragraph cabeceraIconos = new Paragraph();
        cabeceraIconos.setAlignment(Element.ALIGN_CENTER);
        cabeceraIconos.setFont(fNormal);

        if (emp != null) {
            agregarLineaIcono(cabeceraIconos, writer, "dir", emp.getDireccionEmpresa());
            agregarLineaIcono(cabeceraIconos, writer, "tel", emp.getNumeroTelefono());
            agregarLineaIcono(cabeceraIconos, writer, "wa", emp.getWhatsapp());
        }
        documento.add(cabeceraIconos);

        Paragraph tituloDoc = new Paragraph(
                "===============================\nCOMPROBANTE DE APARTADO\n===============================\n", fBold);
        tituloDoc.setAlignment(Element.ALIGN_CENTER);
        documento.add(tituloDoc);

        // Informaci\u00F3n de Apartado
        StringBuilder sbInfo = new StringBuilder();
        sbInfo.append("Cliente:      ").append(nombreCliente).append("\n");
        sbInfo.append("Atendido por: ").append(utilidades.SesionGlobal.getUsuarioActual() != null ? utilidades.SesionGlobal.getUsuarioActual().getNombreUsuario() : "N/A").append("\n");
        sbInfo.append("Fecha:        ").append(fecha).append("\n");
        sbInfo.append("L\u00EDmite Pago:  ").append(fechaLimite).append("\n");
        sbInfo.append("Pago V\u00EDa:     ").append(metodoPago).append("\n");
        sbInfo.append("-------------------------------\n");

        Paragraph info = new Paragraph(sbInfo.toString(), fNormal);
        info.setAlignment(Element.ALIGN_LEFT);
        documento.add(info);

        // Detalles
        StringBuilder sbDetalles = new StringBuilder();
        sbDetalles.append(String.format("%-2s %-15s %6s\n", "C.", "DESCRIPCION", "TOTAL"));
        sbDetalles.append("-------------------------------\n");
        for (Object[] fila : detalles) {
            String nom = fila[2].toString();
            int cant = (int) fila[3];
            double totalFila = (double) fila[5];
            int maxLen = 15;
            if (nom.length() <= maxLen) {
                sbDetalles.append(String.format("%-2s %-15s %,6.2f\n", cant, nom, totalFila));
            } else {
                sbDetalles.append(String.format("%-2s %-15s %,6.2f\n", cant, nom.substring(0, maxLen), totalFila));
                int index = maxLen;
                while (index < nom.length()) {
                    int end = Math.min(index + maxLen, nom.length());
                    sbDetalles.append(String.format("   %-15s\n", nom.substring(index, end)));
                    index += maxLen;
                }
            }
        }
        sbDetalles.append("-------------------------------\n");
        Paragraph parrafoDetalles = new Paragraph(sbDetalles.toString(), fNormal);
        documento.add(parrafoDetalles);

        // Totales de Apartado
        Paragraph totales = new Paragraph(
                String.format("%-15s L %,8.2f\n", "TOTAL DE COMPRA:", total) +
                        String.format("%-15s L %,8.2f\n", "ABONO INICIAL:", abono) +
                        String.format("%-15s L %,8.2f\n", "SALDO PENDIENTE:", saldo) +
                        "\n===============================\n",
                fBold);
        totales.setAlignment(Element.ALIGN_RIGHT);
        documento.add(totales);

        String msjPie = emp != null && emp.getMensajeTicketPieRecibo() != null ? emp.getMensajeTicketPieRecibo()
                : "Conserve este ticket para reclamar su producto.";
        String pol = "\n* Nota: Dep\u00F3sito sujeto a cl\u00E1usula de no devoluci\u00F3n por concepto de cancelaci\u00F3n de apartado. El saldo pendiente debe ser cancelado antes de la fecha l\u00EDmite. Pasada dicha fecha, el apartado podr\u00E1 ser anulado sin responsabilidad para la empresa.\n";
        Paragraph pie = new Paragraph(msjPie + "\n" + pol + "\n\n", fNormal);
        pie.setAlignment(Element.ALIGN_CENTER);
        documento.add(pie);

        Paragraph pFirma = new Paragraph("________________________\nFirma del Cliente", fNormal);
        pFirma.setAlignment(Element.ALIGN_CENTER);
        documento.add(pFirma);

        documento.close();
    }

    @SuppressWarnings("unchecked")
    public static void generarTicketCierreCajaPDF(String rutaDestino, java.util.Map<String, Object> c)
            throws Exception {
        Rectangle tamanoTicket = new Rectangle(226, 800);
        Document documento = new Document(tamanoTicket, 10, 10, 10, 10);
        PdfWriter.getInstance(documento, new FileOutputStream(rutaDestino));
        documento.open();

        com.itextpdf.text.Font fNormal = FontFactory.getFont(FontFactory.COURIER, 9);
        com.itextpdf.text.Font fBold = FontFactory.getFont(FontFactory.COURIER_BOLD, 9);
        com.itextpdf.text.Font fTitulo = FontFactory.getFont(FontFactory.COURIER_BOLD, 12);

        modelo.Empresa emp = utilidades.SesionGlobal.getEmpresaActual();
        String empNombre = emp != null && emp.getNombreEmpresa() != null ? emp.getNombreEmpresa().toUpperCase()
                : "INVERSIONES OLVAN";
        String empRtn = emp != null && emp.getRtnEmpresa() != null ? "RTN: " + emp.getRtnEmpresa() : "";

        Paragraph header = new Paragraph();
        header.setAlignment(Element.ALIGN_CENTER);
        header.add(new Chunk(empNombre + "\n", fTitulo));
        if (!empRtn.isEmpty())
            header.add(new Chunk(empRtn + "\n===============================\n", fBold));
        header.add(new Chunk("REPORTE DE ARQUEO DE CAJA\n", fBold));
        header.add(new Chunk("===============================\n", fNormal));
        documento.add(header);

        StringBuilder sb = new StringBuilder();
        sb.append("Cajero: ").append(c.get("cajero_turno")).append("\n");
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm");
        sb.append("Apertura: ").append(sdf.format((java.util.Date) c.get("fecha_apertura"))).append("\n");
        if (c.get("fecha_cierre") != null) {
            sb.append("Cierre:   ").append(sdf.format((java.util.Date) c.get("fecha_cierre"))).append("\n");
            if (c.get("nombre_usuario_cierre") != null) {
                sb.append("Cerrado por: ").append(c.get("nombre_usuario_cierre")).append("\n");
            }
        }
        sb.append("-------------------------------\n");
        sb.append(String.format("%-16s L %,8.2f\n", "Fondo Gaveta:", (double) c.get("monto_apertura")));

        java.util.List<java.util.Map<String, Object>> metodos = (java.util.List<java.util.Map<String, Object>>) c
                .get("metodos");
        if (metodos != null) {
            for (java.util.Map<String, Object> m : metodos) {
                String nombre = (String) m.get("nombre_metodo");
                double total = (double) m.get("total_general");
                if (nombre.length() > 16)
                    nombre = nombre.substring(0, 16);
                sb.append(String.format("%-16s L %,8.2f\n", nombre + ":", total));
            }
        }
        sb.append("-------------------------------\n");
        Paragraph body = new Paragraph(sb.toString(), fNormal);
        documento.add(body);

        double esperado = (double) c.get("efectivo_esperado");
        double real = c.get("monto_cierre_real") != null ? (double) c.get("monto_cierre_real") : 0.0;
        double dif = real - esperado;

        String textoDif = dif == 0 ? "CUADRE PERFECTO:" : (dif < 0 ? "FALTANTE EN CAJA:" : "SOBRANTE EN CAJA:");

        Paragraph totales = new Paragraph(
                String.format("%-18s L %,8.2f\n", "Efectivo Esperado:", esperado) +
                        String.format("%-18s L %,8.2f\n", "Efectivo Real:", real) +
                        String.format("%-18s L %,8.2f\n", textoDif, dif) +
                        "===============================\n\n",
                fBold);
        totales.setAlignment(Element.ALIGN_RIGHT);
        documento.add(totales);

        java.util.List<java.util.Map<String, Object>> prods = (java.util.List<java.util.Map<String, Object>>) c
                .get("productos_vendidos");
        if (prods != null && !prods.isEmpty()) {
            StringBuilder sbProds = new StringBuilder();
            sbProds.append("PRODUCTOS VENDIDOS EN TURNO:\n");
            for (java.util.Map<String, Object> p : prods) {
                String nom = p.get("descripcion").toString();
                int cant = (int) p.get("cantidad");
                double tot = (double) p.get("total_valor");
                if (nom.length() > 15)
                    nom = nom.substring(0, 15);
                sbProds.append(String.format("%-2dx %-15s %,6.2f\n", cant, nom, tot));
            }
            sbProds.append("===============================\n");
            Paragraph prodPar = new Paragraph(sbProds.toString(), fNormal);
            documento.add(prodPar);
        }

        if (c.get("observaciones") != null && !c.get("observaciones").toString().trim().isEmpty()) {
            Paragraph obs = new Paragraph("Obs: " + c.get("observaciones") + "\n\n", fNormal);
            documento.add(obs);
        }

        Paragraph pFirma = new Paragraph(
                "\n\n\n" +
                        "________________________\n" +
                        "Firma del Cajero\n\n\n\n" +
                        "________________________\n" +
                        "Firma Auditor/Gerente\n\n",
                fNormal);
        pFirma.setAlignment(Element.ALIGN_CENTER);
        documento.add(pFirma);

        documento.close();
    }

    public static void generarTicketApartadoPDF(String rutaDestino, String cliente, String fecha, String fechaLimite, double total,
            double saldoPendiente, int idApartado, java.util.List<modelo.DetalleApartado> detalles,
            java.util.List<modelo.AbonoApartado> abonosHistoricos) throws Exception {
        Rectangle tamanoTicket = new Rectangle(226, 800); // Formato de ticket t\u00E9rmico 80mm
        Document documento = new Document(tamanoTicket, 10, 10, 10, 10);
        PdfWriter writer = PdfWriter.getInstance(documento, new FileOutputStream(rutaDestino));
        documento.open();

        com.itextpdf.text.Font fNormal = FontFactory.getFont(FontFactory.COURIER, 9);
        com.itextpdf.text.Font fBold = FontFactory.getFont(FontFactory.COURIER_BOLD, 9);
        com.itextpdf.text.Font fTitulo = FontFactory.getFont(FontFactory.COURIER_BOLD, 12);

        Empresa emp = SesionGlobal.getEmpresaActual();

        String empNombre = emp != null && emp.getNombreEmpresa() != null ? emp.getNombreEmpresa().toUpperCase()
                : "INVERSIONES OLVAN";
        String empDue\u00F1o = emp != null && emp.getDuenoEmpresa() != null ? "Prop: " + emp.getDuenoEmpresa() : "";
        String empRtn = emp != null && emp.getRtnEmpresa() != null ? "RTN: " + emp.getRtnEmpresa() : "RTN: PENDIENTE";

        // T\u00EDtulo Principal
        Paragraph parrafoNombre = new Paragraph(empNombre + "\n", fTitulo);
        parrafoNombre.setAlignment(Element.ALIGN_CENTER);
        documento.add(parrafoNombre);

        // Cabecera Texto Plano (Due\u00F1o y RTN)
        Paragraph cabeceraTexto = new Paragraph();
        cabeceraTexto.setAlignment(Element.ALIGN_CENTER);
        cabeceraTexto.setFont(fBold);
        if (!empDue\u00F1o.isEmpty())
            cabeceraTexto.add(new Chunk(empDue\u00F1o + "\n"));
        cabeceraTexto.add(new Chunk(empRtn + "\n===============================\n"));
        documento.add(cabeceraTexto);

        // --- SECCI\u00D3N DE DATOS CON \u00CDCONOS DIBUJADOS ---
        Paragraph cabeceraIconos = new Paragraph();
        cabeceraIconos.setAlignment(Element.ALIGN_CENTER);
        cabeceraIconos.setFont(fNormal);

        if (emp != null) {
            agregarLineaIcono(cabeceraIconos, writer, "dir", emp.getDireccionEmpresa());
            agregarLineaIcono(cabeceraIconos, writer, "tel", emp.getNumeroTelefono());
            agregarLineaIcono(cabeceraIconos, writer, "tel", emp.getTelefonoSecundario());
            agregarLineaIcono(cabeceraIconos, writer, "wa", emp.getWhatsapp());
            agregarLineaIcono(cabeceraIconos, writer, "fb", emp.getFacebook());
            agregarLineaIcono(cabeceraIconos, writer, "mail", emp.getEmail());
            agregarLineaIcono(cabeceraIconos, writer, "web", emp.getWeb());
        }
        documento.add(cabeceraIconos);

        Paragraph tituloDoc = new Paragraph(
                "===============================\nTICKET DE APARTADO\n===============================\n", fBold);
        tituloDoc.setAlignment(Element.ALIGN_CENTER);
        documento.add(tituloDoc);

        StringBuilder sbInfo = new StringBuilder();
        sbInfo.append("APARTADO #").append(idApartado).append("\n");
        sbInfo.append("Fecha: ").append(fecha).append("\n");
        sbInfo.append("L\u00EDmite Pago: ").append(fechaLimite).append("\n");
        sbInfo.append("Cliente: ").append(cliente).append("\n");
        sbInfo.append("Atendido por: ").append(utilidades.SesionGlobal.getUsuarioActual() != null ? utilidades.SesionGlobal.getUsuarioActual().getNombreUsuario() : "N/A").append("\n");
        sbInfo.append("-------------------------------\n");
        sbInfo.append("C. DESCRIPCION        TOTAL\n");
        sbInfo.append("-------------------------------\n");

        Paragraph info = new Paragraph(sbInfo.toString(), fNormal);
        info.setAlignment(Element.ALIGN_LEFT);
        documento.add(info);

        StringBuilder prods = new StringBuilder();
        for (modelo.DetalleApartado d : detalles) {
            String nom = d.getNombreProducto();
            int maxLen = 15;
            double sub = d.getCantidadApartado() * d.getPrecioUnitarioApartado();
            if (nom.length() <= maxLen) {
                prods.append(String.format("%-2d %-15s %,6.2f\n", d.getCantidadApartado(), nom, sub));
            } else {
                prods.append(
                        String.format("%-2d %-15s %,6.2f\n", d.getCantidadApartado(), nom.substring(0, maxLen), sub));
                int index = maxLen;
                while (index < nom.length()) {
                    int end = Math.min(index + maxLen, nom.length());
                    prods.append(String.format("   %-15s\n", nom.substring(index, end)));
                    index += maxLen;
                }
            }
        }
        prods.append("-------------------------------\n");

        Paragraph pProds = new Paragraph(prods.toString(), fNormal);
        documento.add(pProds);

        if (abonosHistoricos != null && !abonosHistoricos.isEmpty()) {
            StringBuilder sbAbonos = new StringBuilder();
            sbAbonos.append("HISTORIAL DE ABONOS\n");
            sbAbonos.append("-------------------------------\n");
            for (modelo.AbonoApartado ab : abonosHistoricos) {
                String fechaAbono = new java.text.SimpleDateFormat("dd/MM/yy HH:mm").format(ab.getFechaAbono());
                sbAbonos.append(String.format("%-14s L %,13.2f\n", fechaAbono, ab.getMontoAbono()));
            }
            sbAbonos.append("-------------------------------\n");
            Paragraph pAbonos = new Paragraph(sbAbonos.toString(), fNormal);
            documento.add(pAbonos);
        }

        Paragraph totales = new Paragraph(
                String.format("%-15s L %,8.2f\n", "TOTAL ABONADO:", total - saldoPendiente) +
                        String.format("%-15s L %,8.2f\n", "TOTAL APARTADO:", total) +
                        String.format("%-15s L %,8.2f\n", "SALDO PENDIENTE:", saldoPendiente) +
                        "\n===============================\n",
                fBold);
        totales.setAlignment(Element.ALIGN_RIGHT);
        documento.add(totales);

        String msjPie = emp != null && emp.getMensajeTicketPieRecibo() != null ? emp.getMensajeTicketPieRecibo()
                : "Conserve este ticket para reclamar su producto.";
        String pol = "\n* Nota: Dep\u00F3sito sujeto a cl\u00E1usula de no devoluci\u00F3n por concepto de cancelaci\u00F3n de apartado. El saldo pendiente debe ser cancelado antes de la fecha l\u00EDmite. Pasada dicha fecha, el apartado podr\u00E1 ser anulado sin responsabilidad para la empresa.\n";
        Paragraph pie = new Paragraph(msjPie + "\n" + pol + "\n\n", fNormal);
        pie.setAlignment(Element.ALIGN_CENTER);
        documento.add(pie);

        documento.close();
    }

    public static void generarTicketAbonoPDF(String rutaDestino, String cliente, String fecha, String fechaLimite, double abonado,
            double saldoPendiente, String metodo, String ref, String banco, int idApartado) throws Exception {
        Rectangle tamanoTicket = new Rectangle(226, 600);
        Document documento = new Document(tamanoTicket, 10, 10, 10, 10);
        PdfWriter writer = PdfWriter.getInstance(documento, new FileOutputStream(rutaDestino));
        documento.open();

        com.itextpdf.text.Font fNormal = FontFactory.getFont(FontFactory.COURIER, 9);
        com.itextpdf.text.Font fBold = FontFactory.getFont(FontFactory.COURIER_BOLD, 9);
        com.itextpdf.text.Font fTitulo = FontFactory.getFont(FontFactory.COURIER_BOLD, 12);

        Empresa emp = SesionGlobal.getEmpresaActual();

        String empNombre = emp != null && emp.getNombreEmpresa() != null ? emp.getNombreEmpresa().toUpperCase()
                : "INVERSIONES OLVAN";
        String empDue\u00F1o = emp != null && emp.getDuenoEmpresa() != null ? "Prop: " + emp.getDuenoEmpresa() : "";
        String empRtn = emp != null && emp.getRtnEmpresa() != null ? "RTN: " + emp.getRtnEmpresa() : "RTN: PENDIENTE";

        // T\u00EDtulo Principal
        Paragraph parrafoNombre = new Paragraph(empNombre + "\n", fTitulo);
        parrafoNombre.setAlignment(Element.ALIGN_CENTER);
        documento.add(parrafoNombre);

        // Cabecera Texto Plano (Due\u00F1o y RTN)
        Paragraph cabeceraTexto = new Paragraph();
        cabeceraTexto.setAlignment(Element.ALIGN_CENTER);
        cabeceraTexto.setFont(fBold);
        if (!empDue\u00F1o.isEmpty())
            cabeceraTexto.add(new Chunk(empDue\u00F1o + "\n"));
        cabeceraTexto.add(new Chunk(empRtn + "\n===============================\n"));
        documento.add(cabeceraTexto);

        // --- SECCI\u00D3N DE DATOS CON \u00CDCONOS DIBUJADOS ---
        Paragraph cabeceraIconos = new Paragraph();
        cabeceraIconos.setAlignment(Element.ALIGN_CENTER);
        cabeceraIconos.setFont(fNormal);

        if (emp != null) {
            agregarLineaIcono(cabeceraIconos, writer, "dir", emp.getDireccionEmpresa());
            agregarLineaIcono(cabeceraIconos, writer, "tel", emp.getNumeroTelefono());
            agregarLineaIcono(cabeceraIconos, writer, "tel", emp.getTelefonoSecundario());
            agregarLineaIcono(cabeceraIconos, writer, "wa", emp.getWhatsapp());
            agregarLineaIcono(cabeceraIconos, writer, "fb", emp.getFacebook());
            agregarLineaIcono(cabeceraIconos, writer, "mail", emp.getEmail());
            agregarLineaIcono(cabeceraIconos, writer, "web", emp.getWeb());
        }
        documento.add(cabeceraIconos);

        Paragraph tituloDoc = new Paragraph(
                "===============================\nCOMPROBANTE DE ABONO\n===============================\n", fBold);
        tituloDoc.setAlignment(Element.ALIGN_CENTER);
        documento.add(tituloDoc);

        StringBuilder body = new StringBuilder();
        body.append("APARTADO #").append(idApartado).append("\n");
        body.append("Fecha: ").append(fecha).append("\n");
        body.append("L\u00EDmite Pago: ").append(fechaLimite).append("\n");
        body.append("Cliente: ").append(cliente).append("\n");
        body.append("Atendido por: ").append(utilidades.SesionGlobal.getUsuarioActual() != null ? utilidades.SesionGlobal.getUsuarioActual().getNombreUsuario() : "N/A").append("\n");
        body.append("Pago via: ").append(metodo).append("\n");
        if (banco != null && !banco.isEmpty())
            body.append("Banco: ").append(banco).append("\n");
        if (ref != null && !ref.isEmpty())
            body.append("Ref: ").append(ref).append("\n");
        body.append("-------------------------------\n");

        Paragraph pBody = new Paragraph(body.toString(), fNormal);
        pBody.setAlignment(Element.ALIGN_LEFT);
        documento.add(pBody);

        Paragraph totales = new Paragraph(
                String.format("%-15s L %,8.2f\n", "MONTO ABONADO:", abonado) +
                        String.format("%-15s L %,8.2f\n", "NUEVO SALDO:", saldoPendiente) +
                        "\n===============================\n",
                fBold);
        totales.setAlignment(Element.ALIGN_RIGHT);
        documento.add(totales);

        String pol = "\n* Nota: Dep\u00F3sito sujeto a cl\u00E1usula de no devoluci\u00F3n por concepto de cancelaci\u00F3n de apartado. El saldo pendiente debe ser cancelado antes de la fecha l\u00EDmite. Pasada dicha fecha, el apartado podr\u00E1 ser anulado sin responsabilidad para la empresa.\n";
        Paragraph footer = new Paragraph("Comprobante oficial de pago.\n" + pol + "\n\n", fNormal);
        footer.setAlignment(Element.ALIGN_CENTER);
        documento.add(footer);

        documento.close();
    }

    public static void generarTicketCambioPDF(int idDetalleOriginal, modelo.Producto productoSustituto,
            double precioOriginal) {
        try {
            java.io.File dir = new java.io.File("reportes/garantias_y_cambios");
            if (!dir.exists())
                dir.mkdirs();
            String ruta = "reportes/garantias_y_cambios/Comprobante_Cambio_" + idDetalleOriginal + ".pdf";

            Rectangle tamanoTicket = new Rectangle(226, 800);
            Document documento = new Document(tamanoTicket, 10, 10, 15, 15);
            PdfWriter writer = PdfWriter.getInstance(documento, new java.io.FileOutputStream(ruta));
            documento.open();

            com.itextpdf.text.Font fNormal = FontFactory.getFont(FontFactory.COURIER, 9);
            com.itextpdf.text.Font fBold = FontFactory.getFont(FontFactory.COURIER_BOLD, 9);
            com.itextpdf.text.Font fTitulo = FontFactory.getFont(FontFactory.COURIER_BOLD, 12);

            modelo.Empresa emp = utilidades.SesionGlobal.getEmpresaActual();
            String empNombre = emp != null && emp.getNombreEmpresa() != null ? emp.getNombreEmpresa().toUpperCase()
                    : "INVERSIONES OLVAN";
            String empDue\u00F1o = emp != null && emp.getDuenoEmpresa() != null ? "Prop: " + emp.getDuenoEmpresa() : "";
            String empRtn = emp != null && emp.getRtnEmpresa() != null ? "RTN: " + emp.getRtnEmpresa()
                    : "RTN: PENDIENTE";

            // Cabecera Empresa
            Paragraph parrafoNombre = new Paragraph(empNombre + "\n", fTitulo);
            parrafoNombre.setAlignment(Element.ALIGN_CENTER);
            documento.add(parrafoNombre);

            Paragraph cabeceraTexto = new Paragraph();
            cabeceraTexto.setAlignment(Element.ALIGN_CENTER);
            cabeceraTexto.setFont(fBold);
            if (!empDue\u00F1o.isEmpty())
                cabeceraTexto.add(new Chunk(empDue\u00F1o + "\n"));
            cabeceraTexto.add(new Chunk(empRtn + "\n===============================\n"));
            documento.add(cabeceraTexto);

            Paragraph cabeceraIconos = new Paragraph();
            cabeceraIconos.setAlignment(Element.ALIGN_CENTER);
            cabeceraIconos.setFont(fNormal);
            if (emp != null) {
                agregarLineaIcono(cabeceraIconos, writer, "dir", emp.getDireccionEmpresa());
                agregarLineaIcono(cabeceraIconos, writer, "tel", emp.getNumeroTelefono());
                agregarLineaIcono(cabeceraIconos, writer, "wa", emp.getWhatsapp());
                agregarLineaIcono(cabeceraIconos, writer, "fb", emp.getFacebook());
            }
            documento.add(cabeceraIconos);

            // T\u00EDtulo del Documento
            Paragraph tituloDoc = new Paragraph(
                    "===============================\nCOMPROBANTE DE CAMBIO\n===============================\n", fBold);
            tituloDoc.setAlignment(Element.ALIGN_CENTER);
            documento.add(tituloDoc);

            // Informaci\u00F3n de Venta
            StringBuilder sbInfo = new StringBuilder();
            sbInfo.append("Fecha: ")
                    .append(new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm").format(new java.util.Date()))
                    .append("\n");
            sbInfo.append("Venta Orig: #").append(idDetalleOriginal).append("\n");
            sbInfo.append("-------------------------------\n");
            Paragraph info = new Paragraph(sbInfo.toString(), fNormal);
            info.setAlignment(Element.ALIGN_LEFT);
            documento.add(info);

            double dif = productoSustituto.getPrecioVenta() - precioOriginal;

            // Detalles del Cambio
            StringBuilder sbDetalles = new StringBuilder();
            sbDetalles.append("PRODUCTO DEVUELTO:\n");
            sbDetalles.append(String.format("Credito a Favor: L %,8.2f\n\n", precioOriginal));

            sbDetalles.append("NUEVO PRODUCTO ENTREGADO:\n");

            String nom = productoSustituto.getNombreProducto();
            int maxLen = 22;
            if (nom.length() <= maxLen) {
                sbDetalles.append(String.format("%-22s L %,8.2f\n", nom, productoSustituto.getPrecioVenta()));
            } else {
                sbDetalles.append(String.format("%-22s L %,8.2f\n", nom.substring(0, maxLen),
                        productoSustituto.getPrecioVenta()));
                int index = maxLen;
                while (index < nom.length()) {
                    int end = Math.min(index + maxLen, nom.length());
                    sbDetalles.append(String.format("%-22s\n", nom.substring(index, end)));
                    index += maxLen;
                }
            }
            sbDetalles.append("-------------------------------\n");
            documento.add(new Paragraph(sbDetalles.toString(), fNormal));

            // Totales
            Paragraph totales = new Paragraph();
            totales.setFont(fBold);
            if (dif > 0) {
                totales.add(new Chunk(String.format("%-15s L %,8.2f\n", "SALDO A COBRAR:", dif)));
            } else if (dif < 0) {
                totales.add(new Chunk(String.format("%-15s L %,8.2f\n", "SALDO A DEVOLVER:", Math.abs(dif))));
            } else {
                totales.add(new Chunk(String.format("%-15s L %,8.2f\n", "SALDO A COBRAR:", 0.0)));
            }
            totales.add(new Chunk("\n===============================\n", fBold));
            totales.setAlignment(Element.ALIGN_RIGHT);
            documento.add(totales);

            // Pie de P\u00E1gina
            String msjPie = emp != null && emp.getMensajeTicketCambio() != null
                    && !emp.getMensajeTicketCambio().isEmpty() ? emp.getMensajeTicketCambio()
                            : "Gracias por su preferencia.";
            Paragraph footer = new Paragraph(
                    "Firma Cliente: ________________\n\nFirma Cajero: ________________\n\n" + msjPie, fNormal);
            footer.setAlignment(Element.ALIGN_CENTER);
            documento.add(footer);

            documento.close();

            if (java.awt.Desktop.isDesktopSupported()) {
                utilidades.GestorImpresion.procesarImpresion(new java.io.File(ruta),
                        utilidades.GestorImpresion.TIPO_TICKET);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void generarTicketGarantiaPDF(int idDetalleOriginal, String resolucion, String observacion) {
        try {
            java.io.File dir = new java.io.File("reportes/garantias_y_cambios");
            if (!dir.exists())
                dir.mkdirs();
            String ruta = "reportes/garantias_y_cambios/Comprobante_Reclamo_" + idDetalleOriginal + ".pdf";

            Rectangle tamanoTicket = new Rectangle(226, 800);
            Document documento = new Document(tamanoTicket, 10, 10, 15, 15);
            PdfWriter writer = PdfWriter.getInstance(documento, new java.io.FileOutputStream(ruta));
            documento.open();

            com.itextpdf.text.Font fNormal = FontFactory.getFont(FontFactory.COURIER, 9);
            com.itextpdf.text.Font fBold = FontFactory.getFont(FontFactory.COURIER_BOLD, 9);
            com.itextpdf.text.Font fTitulo = FontFactory.getFont(FontFactory.COURIER_BOLD, 12);

            modelo.Empresa emp = utilidades.SesionGlobal.getEmpresaActual();
            String empNombre = emp != null && emp.getNombreEmpresa() != null ? emp.getNombreEmpresa().toUpperCase()
                    : "INVERSIONES OLVAN";
            String empDue\u00F1o = emp != null && emp.getDuenoEmpresa() != null ? "Prop: " + emp.getDuenoEmpresa() : "";
            String empRtn = emp != null && emp.getRtnEmpresa() != null ? "RTN: " + emp.getRtnEmpresa()
                    : "RTN: PENDIENTE";

            // Cabecera Empresa
            Paragraph parrafoNombre = new Paragraph(empNombre + "\n", fTitulo);
            parrafoNombre.setAlignment(Element.ALIGN_CENTER);
            documento.add(parrafoNombre);

            Paragraph cabeceraTexto = new Paragraph();
            cabeceraTexto.setAlignment(Element.ALIGN_CENTER);
            cabeceraTexto.setFont(fBold);
            if (!empDue\u00F1o.isEmpty())
                cabeceraTexto.add(new Chunk(empDue\u00F1o + "\n"));
            cabeceraTexto.add(new Chunk(empRtn + "\n===============================\n"));
            documento.add(cabeceraTexto);

            Paragraph cabeceraIconos = new Paragraph();
            cabeceraIconos.setAlignment(Element.ALIGN_CENTER);
            cabeceraIconos.setFont(fNormal);
            if (emp != null) {
                agregarLineaIcono(cabeceraIconos, writer, "dir", emp.getDireccionEmpresa());
                agregarLineaIcono(cabeceraIconos, writer, "tel", emp.getNumeroTelefono());
                agregarLineaIcono(cabeceraIconos, writer, "wa", emp.getWhatsapp());
                agregarLineaIcono(cabeceraIconos, writer, "fb", emp.getFacebook());
            }
            documento.add(cabeceraIconos);

            // T\u00EDtulo del Documento
            Paragraph tituloDoc = new Paragraph(
                    "===============================\nRECIBO DE RECLAMO\n===============================\n", fBold);
            tituloDoc.setAlignment(Element.ALIGN_CENTER);
            documento.add(tituloDoc);

            // Informaci\u00F3n del Reclamo
            StringBuilder sbInfo = new StringBuilder();
            sbInfo.append("Fecha: ")
                    .append(new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm").format(new java.util.Date()))
                    .append("\n");
            sbInfo.append("Venta Orig: #").append(idDetalleOriginal).append("\n");
            sbInfo.append("-------------------------------\n");
            Paragraph info = new Paragraph(sbInfo.toString(), fNormal);
            info.setAlignment(Element.ALIGN_LEFT);
            documento.add(info);

            // Resolucion y Observaciones
            StringBuilder sbDetalles = new StringBuilder();
            sbDetalles.append("RESOLUCION APLICADA:\n");
            sbDetalles.append(resolucion).append("\n\n");

            sbDetalles.append("OBSERVACIONES / DANO:\n");
            sbDetalles.append(observacion).append("\n");
            sbDetalles.append("===============================\n");
            documento.add(new Paragraph(sbDetalles.toString(), fNormal));

            // Pie de P\u00E1gina
            String msjPie = emp != null && emp.getMensajeTicketReclamo() != null
                    && !emp.getMensajeTicketReclamo().isEmpty() ? emp.getMensajeTicketReclamo()
                            : "Su caso sera procesado. Gracias.";
            Paragraph footer = new Paragraph(
                    "Firma Cliente: ________________\n\nFirma Tecnico: ________________\n\n" + msjPie, fNormal);
            footer.setAlignment(Element.ALIGN_CENTER);
            documento.add(footer);

            documento.close();

            if (java.awt.Desktop.isDesktopSupported()) {
                utilidades.GestorImpresion.procesarImpresion(new java.io.File(ruta),
                        utilidades.GestorImpresion.TIPO_TICKET);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void generarTicketEntregaReparacionPDF(String nombreProducto, String observacion, String nombreCliente) {
        try {
            java.io.File dir = new java.io.File("reportes/garantias_y_cambios");
            if (!dir.exists())
                dir.mkdirs();
            String ruta = "reportes/garantias_y_cambios/Comprobante_Entrega_Reparacion_" + System.currentTimeMillis()
                    + ".pdf";

            Rectangle tamanoTicket = new Rectangle(226, 800);
            Document documento = new Document(tamanoTicket, 10, 10, 15, 15);
            PdfWriter writer = PdfWriter.getInstance(documento, new java.io.FileOutputStream(ruta));
            documento.open();

            com.itextpdf.text.Font fNormal = FontFactory.getFont(FontFactory.COURIER, 9);
            com.itextpdf.text.Font fBold = FontFactory.getFont(FontFactory.COURIER_BOLD, 9);
            com.itextpdf.text.Font fTitulo = FontFactory.getFont(FontFactory.COURIER_BOLD, 12);

            modelo.Empresa emp = utilidades.SesionGlobal.getEmpresaActual();
            String empNombre = emp != null && emp.getNombreEmpresa() != null ? emp.getNombreEmpresa().toUpperCase()
                    : "INVERSIONES OLVAN";
            String empDue\u00F1o = emp != null && emp.getDuenoEmpresa() != null ? "Prop: " + emp.getDuenoEmpresa() : "";
            String empRtn = emp != null && emp.getRtnEmpresa() != null ? "RTN: " + emp.getRtnEmpresa()
                    : "RTN: PENDIENTE";

            // Cabecera Empresa
            Paragraph parrafoNombre = new Paragraph(empNombre + "\n", fTitulo);
            parrafoNombre.setAlignment(Element.ALIGN_CENTER);
            documento.add(parrafoNombre);

            Paragraph cabeceraTexto = new Paragraph();
            cabeceraTexto.setAlignment(Element.ALIGN_CENTER);
            cabeceraTexto.setFont(fBold);
            if (!empDue\u00F1o.isEmpty())
                cabeceraTexto.add(new Chunk(empDue\u00F1o + "\n"));
            cabeceraTexto.add(new Chunk(empRtn + "\n===============================\n"));
            documento.add(cabeceraTexto);

            Paragraph cabeceraIconos = new Paragraph();
            cabeceraIconos.setAlignment(Element.ALIGN_CENTER);
            cabeceraIconos.setFont(fNormal);
            if (emp != null) {
                agregarLineaIcono(cabeceraIconos, writer, "dir", emp.getDireccionEmpresa());
                agregarLineaIcono(cabeceraIconos, writer, "tel", emp.getNumeroTelefono());
                agregarLineaIcono(cabeceraIconos, writer, "wa", emp.getWhatsapp());
                agregarLineaIcono(cabeceraIconos, writer, "fb", emp.getFacebook());
            }
            documento.add(cabeceraIconos);

            // T\u00EDtulo del Documento
            Paragraph tituloDoc = new Paragraph(
                    "===============================\nENTREGA DE EQUIPO\n===============================\n", fBold);
            tituloDoc.setAlignment(Element.ALIGN_CENTER);
            documento.add(tituloDoc);

            // Informaci\u00F3n
            StringBuilder sbInfo = new StringBuilder();
            sbInfo.append("Cliente: ").append(nombreCliente).append("\n");
            sbInfo.append("Atendido por: ").append(utilidades.SesionGlobal.getUsuarioActual() != null ? utilidades.SesionGlobal.getUsuarioActual().getNombreUsuario() : "N/A").append("\n");
            sbInfo.append("Fecha: ")
                    .append(new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm").format(new java.util.Date()))
                    .append("\n");
            sbInfo.append("-------------------------------\n");
            Paragraph info = new Paragraph(sbInfo.toString(), fNormal);
            info.setAlignment(Element.ALIGN_LEFT);
            documento.add(info);

            // Producto
            StringBuilder sbDetalles = new StringBuilder();
            sbDetalles.append("PRODUCTO ENTREGADO:\n");
            sbDetalles.append(nombreProducto).append("\n\n");

            sbDetalles.append("OBSERVACIONES / DETALLES:\n");
            sbDetalles.append(observacion).append("\n");
            sbDetalles.append("===============================\n");
            documento.add(new Paragraph(sbDetalles.toString(), fNormal));

            // Pie de P\u00E1gina
            String msjPie = emp != null && emp.getMensajeTicketEntrega() != null
                    && !emp.getMensajeTicketEntrega().isEmpty() ? emp.getMensajeTicketEntrega()
                            : "Gracias por su paciencia.";
            Paragraph footer = new Paragraph(
                    "\n\n\nFirma Cliente: _________________\n\n\n\nRecib\u00ED conforme: _________________\n\n\n" + msjPie,
                    fNormal);
            footer.setAlignment(Element.ALIGN_CENTER);
            documento.add(footer);

            documento.close();

            if (java.awt.Desktop.isDesktopSupported()) {
                utilidades.GestorImpresion.procesarImpresion(new java.io.File(ruta),
                        utilidades.GestorImpresion.TIPO_TICKET);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
