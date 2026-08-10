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
    private int maxFotos;

    public ServidorFotos(int port, boolean mostrarGaleria, int maxFotos, Consumer<String> onFotoRecibida) throws IOException {
        super(port);
        this.mostrarGaleria = mostrarGaleria;
        this.maxFotos = maxFotos;
        this.onFotoRecibida = onFotoRecibida;
    }

    private String getHtmlTemplate() {
        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html>\n");
        sb.append("<html>\n");
        sb.append("<head>\n");
        sb.append("    <meta charset=\"UTF-8\">\n");
        sb.append("    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n");
        sb.append("    <title>Subir Fotos</title>\n");
        sb.append("    <style>\n");
        sb.append("        body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background-color: #f4f7f6; color: #333; text-align: center; padding: 20px; }\n");
        sb.append("        .container { background: #fff; padding: 30px; border-radius: 12px; box-shadow: 0 4px 10px rgba(0,0,0,0.1); max-width: 400px; margin: 20px auto; }\n");
        sb.append("        h2 { color: #2c3e50; margin-bottom: 20px; }\n");
        if (maxFotos > 1) {
            sb.append("        .grid { display: grid; grid-template-columns: 1fr 1fr; gap: 15px; margin-bottom: 20px; }\n");
        } else {
            sb.append("        .grid { display: flex; justify-content: center; margin-bottom: 20px; }\n");
        }
        sb.append("        .slot { position: relative; width: 100%; max-width: 200px; aspect-ratio: 1; border: 2px dashed #bdc3c7; border-radius: 8px; display: flex; align-items: center; justify-content: center; overflow: hidden; background: #fafafa; margin: 0 auto; }\n");
        sb.append("        .slot input[type=file] { position: absolute; left: 0; top: 0; opacity: 0; cursor: pointer; height: 100%; width: 100%; z-index: 10; }\n");
        sb.append("        .slot img { width: 100%; height: 100%; object-fit: cover; display: none; z-index: 5; position: absolute; top: 0; left: 0; }\n");
        sb.append("        .slot-text { color: #7f8c8d; font-size: 14px; font-weight: bold; }\n");
        sb.append("        .btn { background: #3498db; color: #fff; padding: 15px 30px; font-size: 18px; font-weight: bold; border: none; border-radius: 8px; cursor: pointer; width: 100%; margin-top: 15px; }\n");
        sb.append("        .btn:hover { background: #2980b9; }\n");
        sb.append("        #loading { display: none; margin-top: 15px; color: #3498db; font-weight: bold; }\n");
        sb.append("    </style>\n");
        sb.append("</head>\n");
        sb.append("<body>\n");
        sb.append("    <div class=\"container\" id=\"mainContainer\">\n");
        sb.append("        <h2>Escaner de Fotos</h2>\n");
        if (maxFotos > 1) {
            sb.append("        <p>Toca cada recuadro para capturar (Máximo " + maxFotos + " fotos).</p>\n");
        } else {
            sb.append("        <p>Toca el recuadro para capturar la foto.</p>\n");
        }
        sb.append("        <div class=\"grid\">\n");
        for (int i = 1; i <= maxFotos; i++) {
            sb.append("            <div class=\"slot\" id=\"slot" + i + "\">\n");
            sb.append("                <span class=\"slot-text\">Foto " + (maxFotos > 1 ? i : "") + "</span>\n");
            sb.append("                <img id=\"preview" + i + "\" src=\"\" alt=\"\">\n");
            sb.append("            </div>\n");
        }
        sb.append("        </div>\n");
        sb.append("        <div style=\"display:flex; flex-direction:column; gap:10px; margin-top:20px;\">\n");
        sb.append("            <button type=\"button\" class=\"btn\" onclick=\"document.getElementById('inputCamara').click()\" style=\"background:#27ae60;\">Tomar Fotografía</button>\n");
        if (mostrarGaleria) {
            sb.append("            <button type=\"button\" class=\"btn\" onclick=\"document.getElementById('inputGaleria').click()\">Abrir Galería</button>\n");
            sb.append("            <button type=\"button\" class=\"btn\" onclick=\"document.getElementById('inputArchivos').click()\" style=\"background:#95a5a6;\">Abrir Archivos</button>\n");
        }
        sb.append("        </div>\n");
        
        sb.append("        <input type=\"file\" id=\"inputCamara\" accept=\"image/*\" capture=\"environment\" class=\"foto-input\" style=\"display:none;\">\n");
        if (mostrarGaleria) {
            sb.append("        <input type=\"file\" id=\"inputGaleria\" accept=\"image/*\" multiple class=\"foto-input\" style=\"display:none;\">\n");
            sb.append("        <input type=\"file\" id=\"inputArchivos\" multiple class=\"foto-input\" style=\"display:none;\">\n");
        }
        sb.append("        <div id=\"loading\">Procesando y subiendo imágenes...</div>\n");
        sb.append("        <button type=\"button\" class=\"btn\" id=\"btnSubmit\" style=\"display:none;\">Subir al Sistema</button>\n");
        sb.append("    </div>\n");
        sb.append("    <script>\n");
        sb.append("        const fotoInputs = document.querySelectorAll('.foto-input');\n");
        sb.append("        const btnSubmit = document.getElementById('btnSubmit');\n");
        sb.append("        const loading = document.getElementById('loading');\n");
        sb.append("        const mainContainer = document.getElementById('mainContainer');\n");
        sb.append("        let fotosBase64 = new Array(" + maxFotos + ").fill(null);\n");
        sb.append("        fotoInputs.forEach(input => {\n");
        sb.append("            input.addEventListener('change', function(e) {\n");
        sb.append("                if (this.files && this.files.length > 0) {\n");
        sb.append("                    let files = Array.from(this.files);\n");
        sb.append("                    let startSlot = fotosBase64.indexOf(null);\n");
        sb.append("                    if (startSlot === -1) startSlot = 0;\n");
        sb.append("                    files.slice(0, " + maxFotos + ").forEach((file, index) => {\n");
        sb.append("                        let currentSlot = (startSlot + index) % " + maxFotos + ";\n");
        sb.append("                        const reader = new FileReader();\n");
        sb.append("                        reader.onload = function(event) {\n");
        sb.append("                            const img = new Image();\n");
        sb.append("                            img.onload = function() {\n");
        sb.append("                                const canvas = document.createElement('canvas');\n");
        sb.append("                                const ctx = canvas.getContext('2d');\n");
        sb.append("                                let width = img.width;\n");
        sb.append("                                let height = img.height;\n");
        sb.append("                                const MAX_SIZE = 1200;\n");
        sb.append("                                if (width > height) {\n");
        sb.append("                                    if (width > MAX_SIZE) {\n");
        sb.append("                                        height *= MAX_SIZE / width;\n");
        sb.append("                                        width = MAX_SIZE;\n");
        sb.append("                                    }\n");
        sb.append("                                } else {\n");
        sb.append("                                    if (height > MAX_SIZE) {\n");
        sb.append("                                        width *= MAX_SIZE / height;\n");
        sb.append("                                        height = MAX_SIZE;\n");
        sb.append("                                    }\n");
        sb.append("                                }\n");
        sb.append("                                canvas.width = width;\n");
        sb.append("                                canvas.height = height;\n");
        sb.append("                                ctx.drawImage(img, 0, 0, width, height);\n");
        sb.append("                                const finalBase64 = canvas.toDataURL('image/jpeg', 0.85);\n");
        sb.append("                                fotosBase64[currentSlot] = finalBase64;\n");
        sb.append("                                const preview = document.getElementById('preview' + (currentSlot + 1));\n");
        sb.append("                                preview.src = finalBase64;\n");
        sb.append("                                preview.style.display = 'block';\n");
        sb.append("                                btnSubmit.style.display = 'block';\n");
        sb.append("                            };\n");
        sb.append("                            img.src = event.target.result;\n");
        sb.append("                        };\n");
        sb.append("                        reader.readAsDataURL(file);\n");
        sb.append("                    });\n");
        sb.append("                }\n");
        sb.append("            });\n");
        sb.append("        });\n");
        sb.append("        btnSubmit.addEventListener('click', function() {\n");
        sb.append("            const fotosValidas = fotosBase64.filter(f => f !== null);\n");
        sb.append("            if (fotosValidas.length > 0) {\n");
        sb.append("                btnSubmit.style.display = 'none';\n");
        sb.append("                loading.style.display = 'block';\n");
        sb.append("                const combinedBase64 = fotosValidas.join('|');\n");
        sb.append("                fetch('/upload', {\n");
        sb.append("                    method: 'POST',\n");
        sb.append("                    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },\n");
        sb.append("                    body: 'fotoBase64=' + encodeURIComponent(combinedBase64)\n");
        sb.append("                }).then(response => response.text()).then(text => {\n");
        sb.append("                    mainContainer.innerHTML = '<h2>¡Fotos enviadas con éxito!</h2><p>Ya puedes cerrar esta ventana y regresar a la computadora.</p>';\n");
        sb.append("                }).catch(err => {\n");
        sb.append("                    alert('Error al subir las fotos: ' + err);\n");
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
                return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "text/plain", "Error al procesar la foto.");
            }
        }
        return newFixedLengthResponse(Response.Status.NOT_FOUND, "text/plain", "No encontrado");
    }
}
