package utilidades;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import javax.swing.ImageIcon;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.Base64;
import java.util.Iterator;

public class ImagenHelper {

    /**
     * Obtiene una ImageIcon a partir de una ruta física o un string Base64.
     */
    public static ImageIcon obtenerIcono(String valor, int width, int height) {
        if (valor == null || valor.trim().isEmpty()) {
            return null;
        }
        
        if (valor.contains("|")) {
            valor = valor.split("\\|")[0];
        }

        try {
            BufferedImage img = null;

            if (valor.startsWith("data:image/") || valor.length() > 500) {
                // Es Base64
                String base64Data = valor;
                if (valor.contains(",")) {
                    base64Data = valor.split(",")[1];
                }
                byte[] bytes = Base64.getDecoder().decode(base64Data);
                try (ByteArrayInputStream bais = new ByteArrayInputStream(bytes)) {
                    img = ImageIO.read(bais);
                }
            } else {
                // Es ruta local
                File file = new File(valor);
                if (file.exists()) {
                    img = ImageIO.read(file);
                }
            }

            if (img != null) {
                int originalWidth = img.getWidth();
                int originalHeight = img.getHeight();
                
                double ratioX = (double) width / originalWidth;
                double ratioY = (double) height / originalHeight;
                double ratio = Math.min(ratioX, ratioY);
                
                int targetWidth = (int) (originalWidth * ratio);
                int targetHeight = (int) (originalHeight * ratio);
                
                if (targetWidth <= 0) targetWidth = 1;
                if (targetHeight <= 0) targetHeight = 1;

                Image scaled = img.getScaledInstance(targetWidth, targetHeight, Image.SCALE_SMOOTH);
                return new ImageIcon(scaled);
            }
        } catch (Exception e) {
            System.err.println("Error al cargar imagen en ImagenHelper: " + e.getMessage());
        }
        return null;
    }

    /**
     * Escala una imagen utilizando interpolación bicúbica de alta calidad y reducciones progresivas (bilineales) si es necesario.
     */
    public static ImageIcon escalarConAltaCalidad(Image img, int targetWidth, int targetHeight) {
        if (img == null) return null;
        
        int w = img.getWidth(null);
        int h = img.getHeight(null);
        if (w <= 0 || h <= 0) return new ImageIcon(img);
        
        // Si es upscale, usar Bicubic directamente
        if (w <= targetWidth && h <= targetHeight) {
            BufferedImage bimg = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = bimg.createGraphics();
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.drawImage(img, 0, 0, targetWidth, targetHeight, null);
            g2.dispose();
            return new ImageIcon(bimg);
        }
        
        // Si es downscale severo (ej. 1000px a 60px), usar filtrado progresivo
        BufferedImage scratch = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = scratch.createGraphics();
        g2.drawImage(img, 0, 0, null);
        g2.dispose();
        
        while (w > targetWidth * 2 || h > targetHeight * 2) {
            w = (w > targetWidth * 2) ? w / 2 : targetWidth;
            h = (h > targetHeight * 2) ? h / 2 : targetHeight;
            
            BufferedImage temp = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
            g2 = temp.createGraphics();
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2.drawImage(scratch, 0, 0, w, h, null);
            g2.dispose();
            scratch = temp;
        }
        
        BufferedImage imgFinal = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_ARGB);
        g2 = imgFinal.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.drawImage(scratch, 0, 0, targetWidth, targetHeight, null);
        g2.dispose();
        
        return new ImageIcon(imgFinal);
    }

    /**
     * Lee un archivo de imagen local y lo convierte directamente a Base64
     * SIN aplicar ninguna compresión ni redimensionamiento para mantener la calidad 100% original.
     */
    public static String comprimirYConvertirABase64(File file) {
        try {
            byte[] bytes = java.nio.file.Files.readAllBytes(file.toPath());
            String b64 = java.util.Base64.getEncoder().encodeToString(bytes);
            
            // Determinar el MIME type básico según la extensión
            String mimeType = "image/png"; // por defecto
            String name = file.getName().toLowerCase();
            if (name.endsWith(".jpg") || name.endsWith(".jpeg")) {
                mimeType = "image/jpeg";
            } else if (name.endsWith(".gif")) {
                mimeType = "image/gif";
            }
            
            return "data:" + mimeType + ";base64," + b64;
        } catch (Exception e) {
            System.err.println("Error al convertir imagen a Base64: " + e.getMessage());
        }
        return null;
    }

    /**
     * Convierte un BufferedImage (por ejemplo descargado de internet) a Base64
     * guardando la máxima calidad posible en formato PNG.
     */
    public static String convertirImagenABase64(BufferedImage img) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(img, "png", baos);
            byte[] bytes = baos.toByteArray();
            String b64 = Base64.getEncoder().encodeToString(bytes);
            return "data:image/png;base64," + b64;
        } catch (Exception e) {
            System.err.println("Error al convertir BufferedImage a Base64: " + e.getMessage());
        }
        return null;
    }
}
