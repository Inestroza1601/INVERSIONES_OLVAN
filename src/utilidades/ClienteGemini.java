package utilidades;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class ClienteGemini {

    private static String getApiKey() {
        modelo.Empresa emp = new dao.EmpresaDAO().obtenerDatos();
        if (emp != null && emp.getApiKeyGemini() != null && !emp.getApiKeyGemini().trim().isEmpty()) {
            return emp.getApiKeyGemini().trim();
        }
        return null;
    }

    public static String analizarError(String stackTrace) {
        String result = realizarPeticion("gemini-flash-latest", stackTrace);
        if (result.contains("Código 404") || result.contains("NOT_FOUND")) {
            // Si el modelo latest no está disponible, intentamos con el 2.5 explícito
            result = realizarPeticion("gemini-2.5-flash", stackTrace);
        }
        return result;
    }

    public static String enviarMensajeChat(java.util.List<String[]> historial) {
        String result = realizarPeticionChat("gemini-flash-latest", historial);
        if (result.contains("Código 404") || result.contains("NOT_FOUND")) {
            result = realizarPeticionChat("gemini-2.5-flash", historial);
        }
        return result;
    }

    private static String realizarPeticion(String modelo, String stackTrace) {
        String prompt = "Soy un sistema de facturacion y tengo un sistema de gestion de errores. "
                + "Encontré este error en consola y necesito que me des un manual paso a paso de cómo solucionarlo rápidamente (sin tocar código). "
                + "En caso de necesitar tocar código, dile al usuario que debe contactarse con soporte de TI.\n\n"
                + "REGLAS DE FORMATO MUY IMPORTANTES:\n"
                + "1. Genera tu respuesta EXCLUSIVAMENTE en formato HTML básico, compatible con JEditorPane de Java.\n"
                + "2. Usa etiquetas como <b>, <i>, <br>, <ul>, <li> para dar un formato elegante y profesional.\n"
                + "3. NO USES Markdown bajo ninguna circunstancia (ni asteriscos **, ni numerales #).\n"
                + "4. NO uses emojis (porque la fuente de Java no los soporta y dibuja cuadros en blanco).\n"
                + "5. No incluyas las etiquetas <html>, <head> ni <body>, devuelve directamente el contenido para inyectar.\n"
                + "6. IMPORTANTE: NO uses entidades HTML para los caracteres (no uses &#225; ni &aacute;, usa directamente á, é, í, ó, ú, ñ). Usa UTF-8 nativo.\n\n"
                + "ERROR:\n" + stackTrace;

        String apiKey = getApiKey();
        if (apiKey == null || apiKey.equals("TU_API_KEY_AQUI") || apiKey.isEmpty()) {
            return "<b>Error:</b> No se ha configurado la API Key de Gemini en la Base de Datos.<br><br>"
                 + "Por favor, configure su llave en el Panel de Administración de la Empresa para poder utilizar la IA.";
        }

        try {
            URL url = new URI("https://generativelanguage.googleapis.com/v1beta/models/" + modelo + ":generateContent?key=" + apiKey).toURL();
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            String safePrompt = prompt.replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "");
            String jsonPayload = "{\"contents\":[{\"parts\":[{\"text\":\"" + safePrompt + "\"}]}]}";

            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonPayload.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            int responseCode = conn.getResponseCode();
            if (responseCode == 200) {
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = in.readLine()) != null) {
                    response.append(line);
                }
                in.close();
                return extraerTextoDelJson(response.toString());
            } else {
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = in.readLine()) != null) {
                    response.append(line);
                }
                in.close();
                if (responseCode == 429) {
                    return "<b>¡Espera un momento!</b><br>La Inteligencia Artificial está procesando demasiadas solicitudes (Límite de cuota gratuita). Por favor, espera unos 20 segundos e intenta de nuevo.";
                }
                return "Error al contactar con Gemini (Código " + responseCode + ").\n\nRespuesta del servidor:\n"
                        + response.toString();
            }
        } catch (Exception e) {
            return "Ocurrió un error al intentar conectarse con la Inteligencia Artificial:\n" + e.getMessage();
        }
    }

    private static String realizarPeticionChat(String modelo, java.util.List<String[]> historial) {
        String apiKey = getApiKey();
        if (apiKey == null || apiKey.equals("TU_API_KEY_AQUI") || apiKey.isEmpty()) {
            return "<b>Error:</b> No se ha configurado la API Key de Gemini en la Base de Datos.<br><br>"
                 + "Por favor, configure su llave en el Panel de Administración de la Empresa para poder utilizar la IA.";
        }

        try {
            URL url = new URI("https://generativelanguage.googleapis.com/v1beta/models/" + modelo + ":generateContent?key=" + apiKey).toURL();
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            StringBuilder jsonBuilder = new StringBuilder();
            jsonBuilder.append("{\"contents\":[");
            for (int i = 0; i < historial.size(); i++) {
                String role = historial.get(i)[0]; // "user" o "model"
                String text = historial.get(i)[1];
                // Sanitizar texto básico para JSON
                String safeText = text.replace("\\", "\\\\")
                                      .replace("\"", "\\\"")
                                      .replace("\n", "\\n")
                                      .replace("\r", "")
                                      .replace("\t", "\\t");

                jsonBuilder.append("{\"role\":\"").append(role)
                           .append("\",\"parts\":[{\"text\":\"").append(safeText).append("\"}]}");
                if (i < historial.size() - 1) {
                    jsonBuilder.append(",");
                }
            }
            jsonBuilder.append("]}");

            String jsonPayload = jsonBuilder.toString();

            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonPayload.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            int responseCode = conn.getResponseCode();
            if (responseCode == 200) {
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = in.readLine()) != null) {
                    response.append(line);
                }
                in.close();
                return extraerTextoDelJson(response.toString());
            } else {
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = in.readLine()) != null) {
                    response.append(line);
                }
                in.close();
                if (responseCode == 429) {
                    return "<b>¡Espera un momento!</b><br>La Inteligencia Artificial está procesando demasiadas solicitudes (Límite de cuota gratuita). Por favor, espera unos 20 segundos e intenta de nuevo.";
                }
                return "Error al contactar con Gemini (Código " + responseCode + ").\n\nRespuesta del servidor:\n"
                        + response.toString();
            }
        } catch (Exception e) {
            return "Ocurrió un error al intentar conectarse con la Inteligencia Artificial:\n" + e.getMessage();
        }
    }

    private static String extraerTextoDelJson(String jsonResponse) {
        try {
            // Un parser ultra-básico de JSON para no añadir dependencias extra
            // (Gson/Jackson)
            String marker = "\"text\": \"";
            int startIndex = jsonResponse.indexOf(marker);
            if (startIndex == -1)
                return "No se pudo extraer la respuesta del texto generado.";
            startIndex += marker.length();

            // Buscar el final de la cadena cuidando de no detenerse en comillas escapadas
            int endIndex = startIndex;
            while (endIndex < jsonResponse.length()) {
                if (jsonResponse.charAt(endIndex) == '\"' && jsonResponse.charAt(endIndex - 1) != '\\') {
                    break;
                }
                endIndex++;
            }

            String text = jsonResponse.substring(startIndex, endIndex);
            // Restaurar los saltos de línea y caracteres especiales (incluyendo escapes
            // unicode HTML)
            text = text.replace("\\n", "\n")
                    .replace("\\\"", "\"")
                    .replace("\\\\", "\\")
                    .replace("\\t", "\t")
                    .replace("\\u003c", "<")
                    .replace("\\u003e", ">")
                    .replace("\\u0026", "&")
                    .replace("\\u003C", "<")
                    .replace("\\u003E", ">")
                    .replace("\\u0026", "&");
            return text;
        } catch (Exception e) {
            return "Error parseando la respuesta de la IA.";
        }
    }
}
