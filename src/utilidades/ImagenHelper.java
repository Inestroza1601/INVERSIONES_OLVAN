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
     * Lee un archivo de imagen local, lo redimensiona a un máximo de 350px de ancho
     * manteniendo la relación de aspecto, lo comprime en JPEG a 70% de calidad,
     * y retorna el string Base64 listo para guardar en la BD.
     */
    public static String comprimirYConvertirABase64(File file) {
        try {
            BufferedImage original = ImageIO.read(file);
            if (original == null) return null;

            int originalWidth = original.getWidth();
            int originalHeight = original.getHeight();

            int targetWidth = 350;
            if (originalWidth <= targetWidth) {
                targetWidth = originalWidth;
            }
            int targetHeight = (originalHeight * targetWidth) / originalWidth;

            // Redimensionar con calidad
            BufferedImage resized = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = resized.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.drawImage(original, 0, 0, targetWidth, targetHeight, null);
            g.dispose();

            // Comprimir JPEG 70%
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpg");
            if (!writers.hasNext()) throw new IllegalStateException("No se encontró compresor JPEG");
            ImageWriter writer = writers.next();

            try (ImageOutputStream ios = ImageIO.createImageOutputStream(baos)) {
                writer.setOutput(ios);
                ImageWriteParam param = writer.getDefaultWriteParam();
                if (param.canWriteCompressed()) {
                    param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                    param.setCompressionQuality(0.70f); // 70% Calidad
                }
                writer.write(null, new IIOImage(resized, null, null), param);
            }
            writer.dispose();

            byte[] bytes = baos.toByteArray();
            String b64 = Base64.getEncoder().encodeToString(bytes);
            return "data:image/jpeg;base64," + b64;
        } catch (Exception e) {
            System.err.println("Error al comprimir imagen a Base64: " + e.getMessage());
        }
        return null;
    }
}
