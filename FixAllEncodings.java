import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

public class FixAllEncodings {
    public static void main(String[] args) throws Exception {
        List<String> files = Arrays.asList(
            "src/gui/PanelCrearProducto.java",
            "src/gui/PanelReportes.java"
        );
        
        for (String file : files) {
            byte[] bytes = Files.readAllBytes(Paths.get(file));
            String content = new String(bytes, StandardCharsets.UTF_8);
            
            content = content.replace("Ã\u00A1", "á");
            content = content.replace("Ã\u00A9", "é");
            content = content.replace("Ã\u00AD", "í");
            content = content.replace("Ã\u00B3", "ó");
            content = content.replace("Ã\u00BA", "ú");
            content = content.replace("Ã\u00B1", "ñ");
            
            content = content.replace("Ã\u0081", "Á");
            content = content.replace("Ã\u0089", "É");
            content = content.replace("Ã\u008D", "Í");
            content = content.replace("Ã\u0093", "Ó");
            content = content.replace("Ã\u009A", "Ú");
            content = content.replace("Ã\u0091", "Ñ");
            
            content = content.replace("ǭ", "á");
            content = content.replace("Ǹ", "é");
            content = content.replace("%xito", "Éxito");
            
            // Remove BOM if exists
            if (content.startsWith("\uFEFF")) {
                content = content.substring(1);
            }

            Files.write(Paths.get(file), content.getBytes(StandardCharsets.UTF_8));
            System.out.println("Encoding fixed successfully in " + file);
        }
    }
}
