import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

public class UnicodeEscaper {
    public static void main(String[] args) throws Exception {
        List<String> files = Arrays.asList(
            "src/gui/PanelReportes.java",
            "src/gui/DialogoReclamarGarantia.java",
            "src/gui/DialogoBuscarProductoSustituto.java",
            "src/gui/PanelCrearProducto.java"
        );
        
        for (String file : files) {
            byte[] bytes = Files.readAllBytes(Paths.get(file));
            
            // For PanelCrearProducto which is ANSI, read it as ISO-8859-1
            String content;
            if (file.contains("PanelCrearProducto")) {
                content = new String(bytes, StandardCharsets.ISO_8859_1);
            } else {
                content = new String(bytes, StandardCharsets.UTF_8);
            }
            
            StringBuilder sb = new StringBuilder();
            for (char c : content.toCharArray()) {
                if (c > 127) {
                    sb.append(String.format("\\u%04X", (int) c));
                } else {
                    sb.append(c);
                }
            }
            
            Files.write(Paths.get(file), sb.toString().getBytes(StandardCharsets.US_ASCII));
            System.out.println("Escaped " + file);
        }
    }
}
