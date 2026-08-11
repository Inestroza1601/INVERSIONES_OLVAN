import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.util.regex.*;
import java.util.stream.Stream;

public class FixText {
    public static void main(String[] args) throws Exception {
        Path srcPath = Paths.get("src");
        try (Stream<Path> paths = Files.walk(srcPath)) {
            paths.filter(Files::isRegularFile)
                 .filter(p -> p.toString().endsWith(".java"))
                 .forEach(FixText::processFile);
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
            
            // Read as ISO-8859-1 to preserve exact bytes mapped to chars 0-255
            String content = new String(contentBytes, StandardCharsets.ISO_8859_1);
            String original = content;
            
            // Replace common words using regex to match any weird characters
            // Replace with unicode escapes so it's always safe
            content = content.replaceAll("Fecha Inv[^a-z]lida", "Fecha Inv\\\\u00E1lida");
            content = content.replaceAll("v[^a-z]lid(a|o)", "v\\\\u00E1lid$1");
            content = content.replaceAll("autom[^a-z]ticamente", "autom\\\\u00E1ticamente");
            content = content.replaceAll("[^a-zA-Z]xito", "\\\\u00C9xito");
            content = content.replaceAll("electr[^a-z]nico", "electr\\\\u00F3nico");
            content = content.replaceAll("conexi[^a-z]n", "conexi\\\\u00F3n");
            content = content.replaceAll("Env[^a-z]o", "Env\\\\u00EDo");
            content = content.replaceAll("direcci[^a-z]n", "direcci\\\\u00F3n");
            content = content.replaceAll("qui[^a-z]n", "qui\\\\u00E9n");
            content = content.replaceAll("versi[^a-z]n", "versi\\\\u00F3n");
            content = content.replaceAll("Categor[^a-z]a", "Categor\\\\u00EDa");
            content = content.replaceAll("Ubicaci[^a-z]n", "Ubicaci\\\\u00F3n");
            content = content.replaceAll("Garant[^a-z]a", "Garant\\\\u00EDa");
            content = content.replaceAll("Fotograf[^a-z]a", "Fotograf\\\\u00EDa");
            content = content.replaceAll("M[^a-z]nimo", "M\\\\u00EDnimo");
            content = content.replaceAll("c[^a-z]digo", "c\\\\u00F3digo");
            content = content.replaceAll("C[^a-z]digo", "C\\\\u00F3digo");
            content = content.replaceAll("a[^a-z]o", "a\\\\u00F1o");
            content = content.replaceAll("Din[^a-z]mico", "Din\\\\u00E1mico");
            content = content.replaceAll("d[^a-z]as", "d\\\\u00EDas");
            content = content.replaceAll("D[^a-z]as", "D\\\\u00EDas");
            content = content.replaceAll("M[^a-z]x", "M\\\\u00E1x");
            content = content.replaceAll("R[^a-z]pido", "R\\\\u00E1pido");
            content = content.replaceAll("Selecci[^a-z]n", "Selecci\\\\u00F3n");
            content = content.replaceAll("num[^a-z]ricos", "num\\\\u00E9ricos");
            content = content.replaceAll("trav[^a-z]s", "trav\\\\u00E9s");
            content = content.replaceAll("Descripci[^a-z]n", "Descripci\\\\u00F3n");
            content = content.replaceAll("est[^a-z]\\b", "est\\\\u00E1");
            content = content.replaceAll("Edici[^a-z]n", "Edici\\\\u00F3n");
            content = content.replaceAll("vac[^a-z]o", "vac\\\\u00EDo");
            content = content.replaceAll("L[^a-z]mite", "L\\\\u00EDmite");
            content = content.replaceAll("Im[^a-z]genes", "Im\\\\u00E1genes");
            
            // Also replace Ã¡ with \u00E1 in general, just in case
            content = content.replace("Ã¡", "\\u00E1");
            content = content.replace("Ã©", "\\u00E9");
            content = content.replace("Ã­", "\\u00ED"); // Note: usually it's Ã.
            content = content.replace("Ã³", "\\u00F3");
            content = content.replace("Ãº", "\\u00FA");
            content = content.replace("Ã±", "\\u00F1");
            
            if (!content.equals(original) || start > 0) {
                Files.write(p, content.getBytes(StandardCharsets.ISO_8859_1));
                System.out.println("Fixed " + p.toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
