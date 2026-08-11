package utilidades;

import java.awt.Desktop;
import java.io.File;
import java.util.prefs.Preferences;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.swing.JOptionPane;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.printing.PDFPageable;
import java.awt.print.PrinterJob;

public class GestorImpresion {

    public static final String TIPO_TICKET = "TICKET";
    public static final String TIPO_A4 = "A4";
    
    // Preferencias locales
    private static final String PREF_IMPRESORA_TICKET = "impresora_tickets";
    private static final String PREF_IMPRESORA_A4 = "impresora_a4";

    public static void procesarImpresion(File archivoPdf, String tipo) {
        if (archivoPdf == null || !archivoPdf.exists()) return;

        Preferences prefs = Preferences.userNodeForPackage(GestorImpresion.class);
        String impresoraConfigurada = "";
        
        if (TIPO_TICKET.equals(tipo)) {
            impresoraConfigurada = prefs.get(PREF_IMPRESORA_TICKET, "");
        } else if (TIPO_A4.equals(tipo)) {
            impresoraConfigurada = prefs.get(PREF_IMPRESORA_A4, "");
        }

        // Si no hay impresora configurada o seleccionaron "Seleccione una impresora..."
        if (impresoraConfigurada.isEmpty() || impresoraConfigurada.startsWith("Seleccione")) {
            abrirPDF(archivoPdf);
            return;
        }

        // Imprimir directamente sin abrir el PDF ni preguntar confirmación
        imprimirConPDFBox(archivoPdf, impresoraConfigurada);
    }

    private static void imprimirConPDFBox(File archivoPdf, String nombreImpresora) {
        try {
            // Verificar si la impresora est\u00E1 disponible
            PrintService[] services = PrintServiceLookup.lookupPrintServices(null, null);
            PrintService servicioSeleccionado = null;
            
            for (PrintService s : services) {
                if (s.getName().equalsIgnoreCase(nombreImpresora)) {
                    servicioSeleccionado = s;
                    break;
                }
            }

            if (servicioSeleccionado == null) {
                utilidades.Mensajes.showMessageDialog(null, 
                    "No se encontr\u00F3 la impresora o est\u00E1 desconectada: " + nombreImpresora + "\nSe abrir\u00E1 el documento PDF.", 
                    "Impresora no encontrada", JOptionPane.WARNING_MESSAGE);
                abrirPDF(archivoPdf);
                return;
            }

            // Usar Apache PDFBox para imprimir de manera nativa e invisible
            try (PDDocument document = PDDocument.load(archivoPdf)) {
                PrinterJob job = PrinterJob.getPrinterJob();
                job.setPageable(new PDFPageable(document));
                job.setPrintService(servicioSeleccionado);
                job.print();
            }

        } catch (Exception e) {
            System.err.println("Error al intentar imprimir el PDF: " + e.getMessage());
            e.printStackTrace();
            utilidades.Mensajes.showMessageDialog(null, 
                "Ocurri\u00F3 un error al intentar imprimir. Se abrir\u00E1 el documento PDF.\nDetalle: " + e.getMessage(), 
                "Error de Impresi\u00F3n", JOptionPane.ERROR_MESSAGE);
            abrirPDF(archivoPdf);
        }
    }

    private static void abrirPDF(File archivoPdf) {
        try {
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(archivoPdf);
            } else {
                utilidades.Mensajes.showMessageDialog(null, "Apertura autom\u00E1tica no soportada en este sistema.", "Aviso", JOptionPane.WARNING_MESSAGE);
            }
        } catch (Exception ex) {
            System.err.println("No se pudo abrir el PDF: " + ex.getMessage());
        }
    }
    
    // M\u00E9todos para el panel de configuraci\u00F3n
    public static void guardarImpresoraTicket(String nombre) {
        Preferences.userNodeForPackage(GestorImpresion.class).put(PREF_IMPRESORA_TICKET, nombre);
    }
    
    public static String obtenerImpresoraTicket() {
        return Preferences.userNodeForPackage(GestorImpresion.class).get(PREF_IMPRESORA_TICKET, "");
    }
    
    public static void guardarImpresoraA4(String nombre) {
        Preferences.userNodeForPackage(GestorImpresion.class).put(PREF_IMPRESORA_A4, nombre);
    }
    
    public static String obtenerImpresoraA4() {
        return Preferences.userNodeForPackage(GestorImpresion.class).get(PREF_IMPRESORA_A4, "");
    }
}

