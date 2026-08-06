package utilidades;

import fi.iki.elonen.NanoHTTPD;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class ServidorFotos extends NanoHTTPD {

    private Consumer<String> onFotoRecibida;
    private boolean mostrarGaleria;

    public ServidorFotos(int port, boolean mostrarGaleria, Consumer<String> onFotoRecibida) throws IOException {
        super(port);
        this.mostrarGaleria = mostrarGaleria;
        this.onFotoRecibida = onFotoRecibida;
    }

    private String getHtmlTemplate() {
        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html>\n");
        sb.append("<html>\n");
        sb.append("<head>\n");
        sb.append("    <meta charset=\"UTF-8\">\n");
        sb.append("    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n");
        sb.append("    <title>Subir Foto al Sistema</title>\n");
        sb.append("    <style>\n");
        sb.append(
                "        body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background-color: #f4f7f6; color: #333; text-align: center; padding: 20px; }\n");
        sb.append(
                "        .container { background: #fff; padding: 30px; border-radius: 12px; box-shadow: 0 4px 10px rgba(0,0,0,0.1); max-width: 400px; margin: 20px auto; }\n");
        sb.append("        h2 { color: #2c3e50; margin-bottom: 20px; }\n");
        sb.append("        .file-upload { position: relative; overflow: hidden; margin: 10px; }\n");
        sb.append(
                "        .btn { background: #3498db; color: #fff; padding: 15px 30px; font-size: 18px; font-weight: bold; border: none; border-radius: 8px; cursor: pointer; width: 100%; margin-top: 15px; }\n");
        sb.append("        .btn:hover { background: #2980b9; }\n");
        sb.append(
                "        input[type=file] { font-size: 100px; position: absolute; left: 0; top: 0; opacity: 0; cursor: pointer; height: 100%; width: 100%; }\n");
        sb.append("        .btn-camera { background: #e74c3c; }\n");
        sb.append("        .btn-camera:hover { background: #c0392b; }\n");
        sb.append("        #preview { max-width: 100%; margin-top: 15px; border-radius: 8px; display: none; }\n");
        sb.append("        #loading { display: none; margin-top: 15px; color: #3498db; font-weight: bold; }\n");
        sb.append("    </style>\n");
        sb.append("</head>\n");
        sb.append("<body>\n");
        sb.append("    <div class=\"container\" id=\"mainContainer\">\n");
        sb.append("        <h2>Escaner de Fotos</h2>\n");
        sb.append("        <p>Por favor, adjunta la foto requerida por el sistema.</p>\n");
        sb.append("        <div class=\"file-upload\">\n");
<<<<<<< HEAD
        sb.append("            <button type=\"button\" class=\"btn btn-camera\">📷 Abrir Cámara</button>\n");
        sb.append(
                "            <input type=\"file\" accept=\"image/*\" capture=\"environment\" class=\"foto-input\" required>\n");
=======
        sb.append("            <button type=\"button\" class=\"btn btn-camera\">Abrir Cámara</button>\n");
        sb.append("            <input type=\"file\" accept=\"image/*\" capture=\"environment\" class=\"foto-input\" required>\n");
>>>>>>> origin/parte-muoz
        sb.append("        </div>\n");

        if (mostrarGaleria) {
            sb.append("        <div class=\"file-upload\">\n");
<<<<<<< HEAD
            sb.append(
                    "            <button type=\"button\" class=\"btn\" style=\"background:#2ecc71;\">🖼️ Abrir Galería</button>\n");
=======
            sb.append("            <button type=\"button\" class=\"btn\" style=\"background:#2ecc71;\">Abrir Galería</button>\n");
>>>>>>> origin/parte-muoz
            sb.append("            <input type=\"file\" accept=\"image/*\" class=\"foto-input\" required>\n");
            sb.append("        </div>\n");
        }

        sb.append("        <img id=\"preview\" src=\"\" alt=\"Vista Previa\">\n");
<<<<<<< HEAD
        sb.append("        <div id=\"loading\">Procesando y subiendo imagen... ⏳</div>\n");
        sb.append(
                "        <button type=\"button\" class=\"btn\" id=\"btnSubmit\" style=\"display:none;\">Subir al Sistema</button>\n");
=======
        sb.append("        <div id=\"loading\">Procesando y subiendo imagen...</div>\n");
        sb.append("        <button type=\"button\" class=\"btn\" id=\"btnSubmit\" style=\"display:none;\">Subir al Sistema</button>\n");
>>>>>>> origin/parte-muoz
        sb.append("    </div>\n");
        sb.append("    <script>\n");
        sb.append("        const fotoInputs = document.querySelectorAll('.foto-input');\n");
        sb.append("        const preview = document.getElementById('preview');\n");
        sb.append("        const btnSubmit = document.getElementById('btnSubmit');\n");
        sb.append("        const loading = document.getElementById('loading');\n");
        sb.append("        const mainContainer = document.getElementById('mainContainer');\n");
        sb.append("        let finalBase64 = null;\n");
        sb.append("        fotoInputs.forEach(input => {\n");
        sb.append("            input.addEventListener('change', function(e) {\n");
        sb.append("                if (this.files && this.files[0]) {\n");
        sb.append("                    const file = this.files[0];\n");
        sb.append("                    const reader = new FileReader();\n");
        sb.append("                    reader.onload = function(event) {\n");
        sb.append("                        const img = new Image();\n");
        sb.append("                        img.onload = function() {\n");
        sb.append("                            const canvas = document.createElement('canvas');\n");
        sb.append("                            const ctx = canvas.getContext('2d');\n");
        sb.append("                            let width = img.width;\n");
        sb.append("                            let height = img.height;\n");
        sb.append("                            const MAX_SIZE = 1200;\n");
        sb.append("                            if (width > height) {\n");
        sb.append("                                if (width > MAX_SIZE) {\n");
        sb.append("                                    height *= MAX_SIZE / width;\n");
        sb.append("                                    width = MAX_SIZE;\n");
        sb.append("                                }\n");
        sb.append("                            } else {\n");
        sb.append("                                if (height > MAX_SIZE) {\n");
        sb.append("                                    width *= MAX_SIZE / height;\n");
        sb.append("                                    height = MAX_SIZE;\n");
        sb.append("                                }\n");
        sb.append("                            }\n");
        sb.append("                            canvas.width = width;\n");
        sb.append("                            canvas.height = height;\n");
        sb.append("                            ctx.drawImage(img, 0, 0, width, height);\n");
        sb.append("                            finalBase64 = canvas.toDataURL('image/jpeg', 0.85);\n");
        sb.append("                            preview.src = finalBase64;\n");
        sb.append("                            preview.style.display = 'block';\n");
        sb.append("                            btnSubmit.style.display = 'block';\n");
        sb.append("                        };\n");
        sb.append("                        img.src = event.target.result;\n");
        sb.append("                    };\n");
        sb.append("                    reader.readAsDataURL(file);\n");
        sb.append("                }\n");
        sb.append("            });\n");
        sb.append("        });\n");
        sb.append("        btnSubmit.addEventListener('click', function() {\n");
        sb.append("            if (finalBase64) {\n");
        sb.append("                btnSubmit.style.display = 'none';\n");
        sb.append("                loading.style.display = 'block';\n");
        sb.append("                fetch('/upload', {\n");
        sb.append("                    method: 'POST',\n");
        sb.append("                    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },\n");
        sb.append("                    body: 'fotoBase64=' + encodeURIComponent(finalBase64)\n");
        sb.append("                }).then(response => response.text()).then(text => {\n");
<<<<<<< HEAD
        sb.append(
                "                    mainContainer.innerHTML = '<h2>¡Foto enviada con éxito! ✅</h2><p>Ya puedes cerrar esta ventana y regresar a la computadora.</p>';\n");
=======
        sb.append("                    mainContainer.innerHTML = '<h2>¡Foto enviada con éxito!</h2><p>Ya puedes cerrar esta ventana y regresar a la computadora.</p>';\n");
>>>>>>> origin/parte-muoz
        sb.append("                }).catch(err => {\n");
        sb.append("                    alert('Error al subir la foto: ' + err);\n");
        sb.append("                    btnSubmit.style.display = 'block';\n");
        sb.append("                    loading.style.display = 'none';\n");
        sb.append("                });\n");
        sb.append("            }\n");
        sb.append("        });\n");
        sb.append("    </script>\n");
        sb.append("</body>\n");
        sb.append("</html>");
        return sb.toString();
    }

    @Override
    public Response serve(IHTTPSession session) {
        if (Method.GET.equals(session.getMethod())) {
            return newFixedLengthResponse(Response.Status.OK, "text/html", getHtmlTemplate());
        } else if (Method.POST.equals(session.getMethod()) && session.getUri().equals("/upload")) {
            try {
                Map<String, String> bodyFiles = new HashMap<>();
                session.parseBody(bodyFiles);
                Map<String, java.util.List<String>> params = session.getParameters();
                if (params.containsKey("fotoBase64")) {
                    String base64 = params.get("fotoBase64").get(0);
                    if (onFotoRecibida != null) {
                        onFotoRecibida.accept(base64);
                    }
                    return newFixedLengthResponse(Response.Status.OK, "text/plain", "OK");
                }
                return newFixedLengthResponse(Response.Status.BAD_REQUEST, "text/plain", "No se recibió archivo.");
            } catch (Exception e) {
                e.printStackTrace();
                return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "text/plain",
                        "Error al procesar la foto.");
            }
        }
        return newFixedLengthResponse(Response.Status.NOT_FOUND, "text/plain", "No encontrado");
    }
}
