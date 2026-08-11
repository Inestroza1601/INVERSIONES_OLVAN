import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;

public class FixEncodingSafe {
    public static void main(String[] args) throws Exception {
        Path srcPath = Paths.get("src");
        try (Stream<Path> paths = Files.walk(srcPath)) {
            paths.filter(Files::isRegularFile)
                 .filter(p -> p.toString().endsWith(".java"))
                 .forEach(FixEncodingSafe::processFile);
        }
    }
    
    private static void processFile(Path p) {
        try {
            byte[] bytes = Files.readAllBytes(p);
            
            // Read as UTF-8. If it contains Ã¡, it means the ANSI file was read as UTF-8 earlier,
            // or the UTF-8 file contains the literal characters Ã¡.
            String content = new String(bytes, StandardCharsets.UTF_8);
            String original = content;
            
            // Double-encoded UTF-8 replacements (when UTF-8 is read as ANSI and saved as UTF-8)
            content = content.replace("Ã\u00A1", "\\u00E1"); // á
            content = content.replace("Ã\u00A9", "\\u00E9"); // é
            content = content.replace("Ã\u00AD", "\\u00ED"); // í
            content = content.replace("Ã\u00B3", "\\u00F3"); // ó
            content = content.replace("Ã\u00BA", "\\u00FA"); // ú
            content = content.replace("Ã\u00B1", "\\u00F1"); // ñ
            
            content = content.replace("Ã\u0081", "\\u00C1"); // Á
            content = content.replace("Ã\u0089", "\\u00C9"); // É
            content = content.replace("Ã\u008D", "\\u00CD"); // Í
            content = content.replace("Ã\u0093", "\\u00D3"); // Ó
            content = content.replace("Ã\u009A", "\\u00DA"); // Ú
            content = content.replace("Ã\u0091", "\\u00D1"); // Ñ
            
            // Other garbage characters caused by ANSI decoding
            content = content.replace("ǭ", "\\u00E1"); // á in some cases
            content = content.replace("Ǹ", "\\u00E9"); // é
            
            if (!content.equals(original)) {
                Files.write(p, content.getBytes(StandardCharsets.UTF_8));
                System.out.println("Fixed safe " + p.toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
