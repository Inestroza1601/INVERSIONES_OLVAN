package utilidades;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Seguridad {

    /**
     * Recibe una contraseña en texto plano y devuelve su hash en SHA-256.
     */
    public static String encriptarSHA256(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes());
            
            // Convertir los bytes a formato Hexadecimal (String)
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
            
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error en el algoritmo de encriptación", e);
        }
    }
}