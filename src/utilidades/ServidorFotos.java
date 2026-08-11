package utilidades;

import fi.iki.elonen.NanoHTTPD;
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
        sb.append("    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no\">\n");
        sb.append("    <title>Captura de Fotos</title>\n");
        sb.append("    <style>\n");
        sb.append("        body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background-color: #ecfdf5; color: #064e3b; text-align: center; padding: 20px; margin: 0; }\n");
        sb.append("        .container { background: #ffffff; padding: 30px; border-radius: 16px; box-shadow: 0 4px 20px rgba(16, 185, 129, 0.12); max-width: 400px; margin: 20px auto; border: 1px solid #d1fae5; }\n");
        sb.append("        h2 { color: #047857; margin-bottom: 5px; font-weight: 700; }\n");
        sb.append("        p { color: #059669; font-size: 14px; margin-bottom: 25px; }\n");
        if (maxFotos > 1) {
            sb.append("        .grid { display: grid; grid-template-columns: 1fr 1fr; gap: 15px; margin-bottom: 25px; }\n");
        } else {
            sb.append("        .grid { display: flex; justify-content: center; margin-bottom: 25px; }\n");
        }
        sb.append("        .slot { position: relative; width: 100%; max-width: 200px; aspect-ratio: 1; border: 2px dashed #34d399; border-radius: 14px; display: flex; align-items: center; justify-content: center; overflow: hidden; background: #f0fdf4; margin: 0 auto; cursor: pointer; transition: all 0.2s ease; }\n");
        sb.append("        .slot:active { transform: scale(0.97); }\n");
        sb.append("        .slot:hover { background: #d1fae5; border-color: #10b981; }\n");
        sb.append("        .slot img { width: 100%; height: 100%; object-fit: cover; display: none; z-index: 5; position: absolute; top: 0; left: 0; }\n");
        sb.append("        .slot-text { color: #10b981; font-size: 15px; font-weight: bold; }\n");
        sb.append("        .slot-icon { font-size: 24px; margin-bottom: 5px; display: block; }\n");
        
        sb.append("        .btn { background: #10b981; color: #fff; padding: 16px; font-size: 18px; font-weight: bold; border: none; border-radius: 12px; cursor: pointer; width: 100%; transition: background 0.3s, transform 0.1s; box-shadow: 0 4px 10px rgba(16,185,129,0.3); }\n");
        sb.append("        .btn:active { transform: scale(0.98); }\n");
        sb.append("        .btn:hover { background: #059669; }\n");
        
        sb.append("        #loading { display: none; margin-top: 20px; color: #059669; font-weight: bold; font-size: 16px; }\n");
        sb.append("        .spinner { border: 4px solid rgba(16, 185, 129, 0.2); border-left-color: #10b981; border-radius: 50%; width: 30px; height: 30px; animation: spin 1s linear infinite; margin: 0 auto 10px auto; }\n");
        sb.append("        @keyframes spin { 0% { transform: rotate(0deg); } 100% { transform: rotate(360deg); } }\n");
        
        // CSS for Modal Menu
        sb.append("        .modal-overlay { position: fixed; top: 0; left: 0; right: 0; bottom: 0; background: rgba(0, 0, 0, 0.5); display: none; align-items: flex-end; justify-content: center; z-index: 100; backdrop-filter: blur(4px); padding: 15px; animation: fadeIn 0.2s; }\n");
        sb.append("        .modal { background: #fff; padding: 25px 20px; border-radius: 20px; width: 100%; max-width: 380px; box-shadow: 0 -5px 25px rgba(0,0,0,0.1); text-align: center; animation: slideUp 0.3s ease-out; }\n");
        sb.append("        .modal h3 { color: #064e3b; margin-top: 0; margin-bottom: 20px; font-size: 20px; }\n");
        sb.append("        .modal-buttons { display: flex; flex-direction: column; gap: 12px; }\n");
        sb.append("        .modal-btn { padding: 15px; border-radius: 12px; border: none; font-size: 16px; font-weight: 600; cursor: pointer; transition: 0.2s; }\n");
        sb.append("        .modal-btn:active { transform: scale(0.98); }\n");
        sb.append("        @keyframes slideUp { from { transform: translateY(100%); opacity: 0; } to { transform: translateY(0); opacity: 1; } }\n");
        sb.append("        @keyframes fadeIn { from { opacity: 0; } to { opacity: 1; } }\n");
        
        sb.append("    </style>\n");
        sb.append("</head>\n");
        sb.append("<body>\n");
        sb.append("    <div class=\"container\" id=\"mainContainer\">\n");
        sb.append("        <h2>C\u00E1mara</h2>\n");
        if (maxFotos > 1) {
            sb.append("        <p>Toca los recuadros para capturar fotos.</p>\n");
        } else {
            sb.append("        <p>Toca el recuadro para capturar la foto.</p>\n");
        }
        sb.append("        <div class=\"grid\">\n");
        for (int i = 1; i <= maxFotos; i++) {
            sb.append("            <div class=\"slot\" id=\"slot" + i + "\" onclick=\"handleSlotClick(" + (i - 1) + ")\">\n");
            sb.append("                <div style=\"text-align: center;\">\n");
            sb.append("                    <span class=\"slot-icon\">\uD83D\uDCF7</span>\n");
            sb.append("                    <span class=\"slot-text\">Foto " + (maxFotos > 1 ? i : "") + "</span>\n");
            sb.append("                </div>\n");
            sb.append("                <img id=\"preview" + i + "\" src=\"\" alt=\"\">\n");
            sb.append("            </div>\n");
        }
        sb.append("        </div>\n");
        
        // Modal HTML
        sb.append("        <div class=\"modal-overlay\" id=\"modalMenu\" onclick=\"if(event.target === this) closeModal()\">\n");
        sb.append("            <div class=\"modal\">\n");
        sb.append("                <h3>Opciones de Fotograf\u00EDa</h3>\n");
        sb.append("                <div class=\"modal-buttons\">\n");
        sb.append("                    <button class=\"modal-btn\" style=\"background:#10b981; color:#fff;\" onclick=\"actionRetake()\">\uD83D\uDCF7 Tomar de nuevo</button>\n");
        if (mostrarGaleria) {
            sb.append("                    <button class=\"modal-btn\" style=\"background:#d1fae5; color:#064e3b;\" onclick=\"actionUpload()\">\uD83D\uDDBC\uFE0F Subir desde el tel\u00E9fono</button>\n");
        }
        sb.append("                    <button class=\"modal-btn\" style=\"background:#fee2e2; color:#dc2626;\" onclick=\"actionDelete()\">\uD83D\uDDD1\uFE0F Eliminar foto</button>\n");
        sb.append("                    <button class=\"modal-btn\" style=\"background:#f3f4f6; color:#4b5563; margin-top: 10px;\" onclick=\"closeModal()\">Cancelar</button>\n");
        sb.append("                </div>\n");
        sb.append("            </div>\n");
        sb.append("        </div>\n");
        
        // Hidden inputs that will be targeted by JS
        sb.append("        <input type=\"file\" id=\"inputCamara\" accept=\"image/*\" capture=\"environment\" style=\"display:none;\">\n");
        if (mostrarGaleria) {
            sb.append("        <input type=\"file\" id=\"inputGaleria\" accept=\"image/*\" style=\"display:none;\">\n");
        }
        
        sb.append("        <div id=\"loading\">\n");
        sb.append("            <div class=\"spinner\"></div>\n");
        sb.append("            Subiendo al sistema...\n");
        sb.append("        </div>\n");
        sb.append("        <button type=\"button\" class=\"btn\" id=\"btnSubmit\" style=\"display:none; margin-top: 15px;\">Subir al Sistema</button>\n");
        sb.append("    </div>\n");
        sb.append("    <script>\n");
        sb.append("        const inputCamara = document.getElementById('inputCamara');\n");
        if (mostrarGaleria) {
            sb.append("        const inputGaleria = document.getElementById('inputGaleria');\n");
        }
        sb.append("        const btnSubmit = document.getElementById('btnSubmit');\n");
        sb.append("        const loading = document.getElementById('loading');\n");
        sb.append("        const mainContainer = document.getElementById('mainContainer');\n");
        sb.append("        const modalMenu = document.getElementById('modalMenu');\n");
        sb.append("        let fotosBase64 = new Array(" + maxFotos + ").fill(null);\n");
        sb.append("        let currentTargetSlot = -1;\n");
        
        sb.append("        function handleSlotClick(slotIndex) {\n");
        sb.append("            currentTargetSlot = slotIndex;\n");
        sb.append("            if (fotosBase64[slotIndex] !== null) {\n");
        sb.append("                // Si ya hay foto, mostrar men\u00FA modal\n");
        sb.append("                modalMenu.style.display = 'flex';\n");
        sb.append("            } else {\n");
        sb.append("                // Si est\u00E1 vac\u00EDo, abrir c\u00E1mara directamente\n");
        sb.append("                inputCamara.click();\n");
        sb.append("            }\n");
        sb.append("        }\n");
        
        sb.append("        function closeModal() { modalMenu.style.display = 'none'; }\n");
        
        sb.append("        function actionRetake() {\n");
        sb.append("            closeModal();\n");
        sb.append("            inputCamara.click();\n");
        sb.append("        }\n");
        
        if (mostrarGaleria) {
            sb.append("        function actionUpload() {\n");
            sb.append("            closeModal();\n");
            sb.append("            inputGaleria.click();\n");
            sb.append("        }\n");
        }
        
        sb.append("        function actionDelete() {\n");
        sb.append("            closeModal();\n");
        sb.append("            fotosBase64[currentTargetSlot] = null;\n");
        sb.append("            const preview = document.getElementById('preview' + (currentTargetSlot + 1));\n");
        sb.append("            preview.src = '';\n");
        sb.append("            preview.style.display = 'none';\n");
        sb.append("            checkSubmitButton();\n");
        sb.append("        }\n");
        
        sb.append("        function checkSubmitButton() {\n");
        sb.append("            const fotosValidas = fotosBase64.filter(f => f !== null);\n");
        sb.append("            if (fotosValidas.length > 0) {\n");
        sb.append("                btnSubmit.style.display = 'block';\n");
        sb.append("            } else {\n");
        sb.append("                btnSubmit.style.display = 'none';\n");
        sb.append("            }\n");
        sb.append("        }\n");
        
        sb.append("        function processFiles(files) {\n");
        sb.append("            if (files && files.length > 0) {\n");
        sb.append("                const file = files[0];\n");
        sb.append("                const reader = new FileReader();\n");
        sb.append("                reader.onload = function(event) {\n");
        sb.append("                    const img = new Image();\n");
        sb.append("                    img.onload = function() {\n");
        sb.append("                        const canvas = document.createElement('canvas');\n");
        sb.append("                        const ctx = canvas.getContext('2d');\n");
        sb.append("                        let width = img.width;\n");
        sb.append("                        let height = img.height;\n");
        sb.append("                        const MAX_SIZE = 1200;\n");
        sb.append("                        if (width > height) {\n");
        sb.append("                            if (width > MAX_SIZE) {\n");
        sb.append("                                height *= MAX_SIZE / width;\n");
        sb.append("                                width = MAX_SIZE;\n");
        sb.append("                            }\n");
        sb.append("                        } else {\n");
        sb.append("                            if (height > MAX_SIZE) {\n");
        sb.append("                                width *= MAX_SIZE / height;\n");
        sb.append("                                height = MAX_SIZE;\n");
        sb.append("                            }\n");
        sb.append("                        }\n");
        sb.append("                        canvas.width = width;\n");
        sb.append("                        canvas.height = height;\n");
        sb.append("                        ctx.drawImage(img, 0, 0, width, height);\n");
        sb.append("                        const finalBase64 = canvas.toDataURL('image/jpeg', 0.85);\n");
        sb.append("                        fotosBase64[currentTargetSlot] = finalBase64;\n");
        sb.append("                        const preview = document.getElementById('preview' + (currentTargetSlot + 1));\n");
        sb.append("                        preview.src = finalBase64;\n");
        sb.append("                        preview.style.display = 'block';\n");
        sb.append("                        checkSubmitButton();\n");
        sb.append("                    };\n");
        sb.append("                    img.src = event.target.result;\n");
        sb.append("                };\n");
        sb.append("                reader.readAsDataURL(file);\n");
        sb.append("            }\n");
        sb.append("            inputCamara.value = '';\n");
        if (mostrarGaleria) {
            sb.append("            inputGaleria.value = '';\n");
        }
        sb.append("        }\n");
        
        sb.append("        inputCamara.addEventListener('change', function(e) { processFiles(this.files); });\n");
        if (mostrarGaleria) {
            sb.append("        inputGaleria.addEventListener('change', function(e) { processFiles(this.files); });\n");
        }
        
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
        sb.append("                    mainContainer.innerHTML = '<h2><span style=\"font-size:50px; color:#10b981;\">\u2713</span><br>\u00A1Fotos Subidas!</h2><p style=\"font-size:16px;\">Las fotos fueron procesadas exitosamente.<br><b>Ya puedes cerrar esta ventana.</b></p>';\n");
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
                return newFixedLengthResponse(Response.Status.BAD_REQUEST, "text/plain", "No se recibi\u00F3 archivo.");
            } catch (Exception e) {
                e.printStackTrace();
                return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "text/plain", "Error al procesar la foto.");
            }
        }
        return newFixedLengthResponse(Response.Status.NOT_FOUND, "text/plain", "No encontrado");
    }

    public static String guardarImagenDefectuosoDirecto(int idDefectuoso, String base64, int numFoto) {
        try {
            if (base64 == null || base64.isEmpty()) return null;
            if (base64.contains(",")) base64 = base64.split(",")[1];
            byte[] imageBytes = java.util.Base64.getDecoder().decode(base64);
            
            String fileName = "defectuoso_" + idDefectuoso + "_foto" + numFoto + ".jpg";
            String userHome = System.getProperty("user.home");
            java.io.File dir = new java.io.File(userHome + "/InversionesOlvan/Garantias");
            if (!dir.exists()) dir.mkdirs();
            
            java.io.File destFile = new java.io.File(dir, fileName);
            java.nio.file.Files.write(destFile.toPath(), imageBytes);
            return destFile.getAbsolutePath();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
