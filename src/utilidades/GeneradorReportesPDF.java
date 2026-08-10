package utilidades;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import modelo.Empresa;
import dao.EmpresaDAO;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class GeneradorReportesPDF {

    private static final Font FUENTE_TITULO = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, BaseColor.BLACK);
    private static final Font FUENTE_EMPRESA = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, BaseColor.DARK_GRAY);
    private static final Font FUENTE_NORMAL = FontFactory.getFont(FontFactory.HELVETICA, 10, BaseColor.BLACK);
    private static final Font FUENTE_CABECERA_TABLA = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, BaseColor.WHITE);
    private static final BaseColor COLOR_CABECERA = new BaseColor(42, 157, 114); // Verde corporativo

    public static File generarReporte(String tituloReporte, String[] columnas, List<Object[]> datos, File archivoPdf) throws Exception {
        // Obtener datos de la empresa
        EmpresaDAO empresaDAO = new EmpresaDAO();
        Empresa empresa = empresaDAO.obtenerDatos();
        if (empresa == null) {
            empresa = new Empresa();
        }

        // Crear documento y ruta
        Document documento = new Document(PageSize.A4.rotate()); // Horizontal para tablas anchas
        PdfWriter writer = PdfWriter.getInstance(documento, new FileOutputStream(archivoPdf));
        documento.open();

        // 1. Agregar Encabezado (Logo y Datos de Empresa)
        PdfPTable tablaEncabezado = new PdfPTable(2);
        tablaEncabezado.setWidthPercentage(100);
        tablaEncabezado.setWidths(new float[]{1f, 3f});

        // Intentar cargar el logo
        try {
            java.net.URL urlLogo = GeneradorReportesPDF.class.getResource("/image/logo_inversionesOlvan_sinFondo.png");
            if (urlLogo != null) {
                Image logo = Image.getInstance(urlLogo);
                logo.scaleToFit(130, 130);
                PdfPCell celdaLogo = new PdfPCell(logo);
                celdaLogo.setBorder(Rectangle.NO_BORDER);
                celdaLogo.setHorizontalAlignment(Element.ALIGN_CENTER);
                tablaEncabezado.addCell(celdaLogo);
            } else {
                PdfPCell vacia = new PdfPCell(new Phrase(""));
                vacia.setBorder(Rectangle.NO_BORDER);
                tablaEncabezado.addCell(vacia);
            }
        } catch (Exception e) {
            PdfPCell vacia = new PdfPCell(new Phrase(""));
            vacia.setBorder(Rectangle.NO_BORDER);
            tablaEncabezado.addCell(vacia);
        }

        // Datos de la empresa
        Paragraph infoEmpresa = new Paragraph();
        infoEmpresa.add(new Chunk(empresa.getNombreEmpresa() != null ? empresa.getNombreEmpresa() : "INVERSIONES OLVAN\n", FUENTE_EMPRESA));
        infoEmpresa.add(new Chunk("\nRTN: " + (empresa.getRtnEmpresa() != null ? empresa.getRtnEmpresa() : "N/A"), FUENTE_NORMAL));
        infoEmpresa.add(new Chunk("\nDirecci\u00F3n: " + (empresa.getDireccionEmpresa() != null ? empresa.getDireccionEmpresa() : "N/A"), FUENTE_NORMAL));
        infoEmpresa.add(new Chunk("\nFecha de Emisi\u00F3n: " + new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date()), FUENTE_NORMAL));

        PdfPCell celdaInfo = new PdfPCell(infoEmpresa);
        celdaInfo.setBorder(Rectangle.NO_BORDER);
        celdaInfo.setHorizontalAlignment(Element.ALIGN_LEFT);
        celdaInfo.setVerticalAlignment(Element.ALIGN_MIDDLE);
        tablaEncabezado.addCell(celdaInfo);

        documento.add(tablaEncabezado);
        documento.add(new Paragraph("\n"));

        // 2. T\u00EDtulo del Reporte
        Paragraph pTitulo = new Paragraph(tituloReporte.toUpperCase(), FUENTE_TITULO);
        pTitulo.setAlignment(Element.ALIGN_CENTER);
        documento.add(pTitulo);
        documento.add(new Paragraph("\n"));

        if (tituloReporte.contains("Detallado de Ventas")) {
            double totalDirectas = 0;
            double totalApartados = 0;
            int cantDirectas = 0;
            int cantApartados = 0;
            for (Object[] fila : datos) {
                String tipo = fila[2] != null ? fila[2].toString() : "";
                double subtotal = fila[5] != null ? Double.parseDouble(fila[5].toString()) : 0.0;
                if (tipo.equals("Venta Directa")) {
                    totalDirectas += subtotal;
                    cantDirectas++;
                } else {
                    totalApartados += subtotal;
                    cantApartados++;
                }
            }
            Paragraph pResumen = new Paragraph("Resumen Ejecutivo:", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, BaseColor.BLACK));
            pResumen.add(new Chunk("\nTotal Ventas Directas: L. " + String.format("%,.2f", totalDirectas) + " (" + cantDirectas + " prods)", FUENTE_NORMAL));
            pResumen.add(new Chunk("\nTotal Apartados: L. " + String.format("%,.2f", totalApartados) + " (" + cantApartados + " prods)", FUENTE_NORMAL));
            pResumen.add(new Chunk("\nTotal General: L. " + String.format("%,.2f", (totalDirectas + totalApartados)), FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, BaseColor.DARK_GRAY)));
            pResumen.setSpacingAfter(15f);
            documento.add(pResumen);
        }

        // 3. Tabla "Maciza" de Datos
        PdfPTable tablaDatos = new PdfPTable(columnas.length);
        tablaDatos.setWidthPercentage(100);

        // Cabeceras
        for (String col : columnas) {
            PdfPCell celdaCabecera = new PdfPCell(new Phrase(col, FUENTE_CABECERA_TABLA));
            celdaCabecera.setBackgroundColor(COLOR_CABECERA);
            celdaCabecera.setHorizontalAlignment(Element.ALIGN_CENTER);
            celdaCabecera.setPadding(8f);
            tablaDatos.addCell(celdaCabecera);
        }

        // Datos
        Font fuenteDatos = FontFactory.getFont(FontFactory.HELVETICA, 10, BaseColor.DARK_GRAY);
        boolean alternarColor = false;
        
        for (Object[] fila : datos) {
            BaseColor colorFondo = alternarColor ? new BaseColor(245, 245, 245) : BaseColor.WHITE;
            for (Object valor : fila) {
                String texto = "";
                if (valor != null) {
                    if (valor instanceof Double || valor instanceof Float) {
                        texto = String.format("%,.2f", valor);
                    } else {
                        texto = valor.toString();
                    }
                }
                PdfPCell celdaDato = new PdfPCell(new Phrase(texto, fuenteDatos));
                celdaDato.setBackgroundColor(colorFondo);
                celdaDato.setPadding(6f);
                celdaDato.setHorizontalAlignment(Element.ALIGN_CENTER);
                celdaDato.setBorderColor(new BaseColor(220, 220, 220));
                tablaDatos.addCell(celdaDato);
            }
            alternarColor = !alternarColor;
        }

        documento.add(tablaDatos);
        documento.close();
        writer.close();

        return archivoPdf;
    }
}
