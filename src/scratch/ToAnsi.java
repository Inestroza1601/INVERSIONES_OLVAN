package scratch;

import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.nio.charset.Charset;

public class ToAnsi {
    public static void main(String[] args) throws Exception {
        Path p = Paths.get("src/gui/PanelReportes.java");
        byte[] bytes = Files.readAllBytes(p);
        
        // Remove BOM if exists
        int start = 0;
        if (bytes.length >= 3 && (bytes[0] & 0xFF) == 0xEF && (bytes[1] & 0xFF) == 0xBB && (bytes[2] & 0xFF) == 0xBF) {
            start = 3;
        }
        
        byte[] utf8Bytes = new byte[bytes.length - start];
        System.arraycopy(bytes, start, utf8Bytes, 0, utf8Bytes.length);
        
        // Read as UTF-8
        String content = new String(utf8Bytes, StandardCharsets.UTF_8);
        
        // Write as Windows-1252 (ANSI)
        Files.write(p, content.getBytes(Charset.forName("windows-1252")));
        System.out.println("Converted to ANSI: " + p.toString());
    }
}
