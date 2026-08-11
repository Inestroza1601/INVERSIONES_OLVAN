import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;
import java.io.IOException;

public class RemoveBoms {
    public static void main(String[] args) throws IOException {
        Path srcPath = Paths.get("src");
        try (Stream<Path> paths = Files.walk(srcPath)) {
            paths.filter(Files::isRegularFile)
                 .filter(p -> p.toString().endsWith(".java"))
                 .forEach(RemoveBoms::processFile);
        }
    }
    
    private static void processFile(Path p) {
        try {
            byte[] bytes = Files.readAllBytes(p);
            if (bytes.length >= 3 && (bytes[0] & 0xFF) == 0xEF && (bytes[1] & 0xFF) == 0xBB && (bytes[2] & 0xFF) == 0xBF) {
                byte[] noBom = new byte[bytes.length - 3];
                System.arraycopy(bytes, 3, noBom, 0, noBom.length);
                Files.write(p, noBom);
                System.out.println("Removed BOM from " + p.toString());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
