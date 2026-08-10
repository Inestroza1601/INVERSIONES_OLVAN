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

        // 1. Siempre abrir el PDF primero
        abrirPDF(archivoPdf);

        // 2. Verificar si la impresora está conectada antes de preguntar
        PrintService[] services = PrintServiceLookup.lookupPrintServices(null, null);
        boolean impresoraEncontrada = false;
        for (PrintService s : services) {
            if (s.getName().equalsIgnoreCase(impresoraConfigurada)) {
                impresoraEncontrada = true;
                break;
            }
        }

        if (!impresoraEncontrada) {
            JOptionPane.showMessageDialog(null, 
                "Advertencia: No se encontró la impresora o está desconectada (" + impresoraConfigurada + ").\n\nPuede imprimir manualmente desde el visor de PDF que se acaba de abrir.", 
                "Impresora no encontrada", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // 3. Si la encontró, preguntar confirmación
        int respuesta = JOptionPane.showConfirmDialog(null, 
            "El documento se ha abierto para revisión.\n\n¿Está seguro de imprimir este documento en:\n" + impresoraConfigurada + "?", 
            "Confirmar Impresión Directa", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            
        if (respuesta == JOptionPane.YES_OPTION) {
            imprimirConPDFBox(archivoPdf, impresoraConfigurada);
        }
    }

    private static void imprimirConPDFBox(File archivoPdf, String nombreImpresora) {
        try {
            // Verificar si la impresora está disponible
            PrintService[] services = PrintServiceLookup.lookupPrintServices(null, null);
            PrintService servicioSeleccionado = null;
            
            for (PrintService s : services) {
                if (s.getName().equalsIgnoreCase(nombreImpresora)) {
                    servicioSeleccionado = s;
                    break;
                }
            }

            if (servicioSeleccionado == null) {
                JOptionPane.showMessageDialog(null, 
                    "No se encontró la impresora o está desconectada: " + nombreImpresora + "\nSe abrirá el documento PDF.", 
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
            JOptionPane.showMessageDialog(null, 
                "Ocurrió un error al intentar imprimir. Se abrirá el documento PDF.\nDetalle: " + e.getMessage(), 
                "Error de Impresión", JOptionPane.ERROR_MESSAGE);
            abrirPDF(archivoPdf);
        }
    }

    private static void abrirPDF(File archivoPdf) {
        try {
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(archivoPdf);
            } else {
                JOptionPane.showMessageDialog(null, "Apertura automática no soportada en este sistema.", "Aviso", JOptionPane.WARNING_MESSAGE);
            }
        } catch (Exception ex) {
            System.err.println("No se pudo abrir el PDF: " + ex.getMessage());
        }
    }
    
    // Métodos para el panel de configuración
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
