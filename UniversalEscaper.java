import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.nio.charset.Charset;
import java.util.stream.Stream;

public class UniversalEscaper {
    public static void main(String[] args) throws Exception {
        Path srcPath = Paths.get("src");
        try (Stream<Path> paths = Files.walk(srcPath)) {
            paths.filter(Files::isRegularFile)
                 .filter(p -> p.toString().endsWith(".java"))
                 .forEach(UniversalEscaper::processFile);
        }
    }
    
    private static void processFile(Path p) {
        try {
            byte[] bytes = Files.readAllBytes(p);
            
            // Remove BOM if exists
            int start = 0;
            if (bytes.length >= 3 && (bytes[0] & 0xFF) == 0xEF && (bytes[1] & 0xFF) == 0xBB && (bytes[2] & 0xFF) == 0xBF) {
                start = 3;
            }
            byte[] contentBytes = new byte[bytes.length - start];
            System.arraycopy(bytes, start, contentBytes, 0, contentBytes.length);
            
            // Read as correct charset
            String content;
            if (p.toString().contains("PanelCrearProducto")) {
                content = new String(contentBytes, Charset.forName("windows-1252"));
            } else {
                content = new String(contentBytes, StandardCharsets.UTF_8);
            }
            
            // Check if there are any non-ASCII characters or if BOM was removed
            boolean needsUpdate = start > 0;
            StringBuilder sb = new StringBuilder(content.length() + 100);
            for (char c : content.toCharArray()) {
                if (c > 127) {
                    sb.append(String.format("\\u%04X", (int) c));
                    needsUpdate = true;
                } else {
                    sb.append(c);
                }
            }
            
            if (needsUpdate) {
                Files.write(p, sb.toString().getBytes(StandardCharsets.US_ASCII));
                System.out.println("Escaped " + p.toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
