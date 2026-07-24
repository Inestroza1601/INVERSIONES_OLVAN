<?php
session_start();
if (!isset($_SESSION['user_id'])) exit;
?>
<div class="card" style="margin-bottom: 20px;">
    <div>
        <h2><i class="fas fa-cogs" style="color:var(--primary);"></i> Configuración del Sistema</h2>
        <p style="color:var(--text-muted); font-size:0.85rem;">Defina los parámetros de la empresa, de facturación y del diseño de los recibos impresos.</p>
    </div>
</div>

<div class="card">
    <form id="settings-form">
        <div style="display:grid; grid-template-columns: 1fr 250px; gap: 30px; flex-wrap:wrap;">
            <!-- Left inputs panel -->
            <div style="display:flex; flex-direction:column; gap:20px;">
                <h3 style="border-bottom:1px solid var(--border-color); padding-bottom:8px;"><i class="fas fa-building" style="color:var(--accent-blue);"></i> Perfil de la Empresa</h3>
                
                <div class="form-group row">
                    <div>
                        <label>Nombre Comercial</label>
                        <input type="text" name="nombre_empresa" id="set-name" class="form-input" required placeholder="INVERSIONES OLVAN">
                    </div>
                    <div>
                        <label>RTN de la Empresa</label>
                        <input type="text" name="rtn_empresa" id="set-rtn" class="form-input" placeholder="0801-1990-123456">
                    </div>
                </div>

                <div class="form-group row">
                    <div>
                        <label>Propietario / Dueño</label>
                        <input type="text" name="dueno_empresa" id="set-owner" class="form-input" placeholder="Ever Chavez">
                    </div>
                    <div>
                        <label>Correo de Contacto</label>
                        <input type="email" name="email_empresa" id="set-email" class="form-input" placeholder="contacto@olvan.com">
                    </div>
                </div>

                <div class="form-group row">
                    <div>
                        <label>Teléfono Principal</label>
                        <input type="text" name="numero_telefono" id="set-phone" class="form-input" placeholder="9999-9999">
                    </div>
                    <div>
                        <label>Teléfono Secundario</label>
                        <input type="text" name="telefono_secundario" id="set-phone-sec" class="form-input" placeholder="2222-2222">
                    </div>
                    <div>
                        <label>WhatsApp</label>
                        <input type="text" name="whatsapp_empresa" id="set-whatsapp" class="form-input" placeholder="9999-9999">
                    </div>
                </div>

                <div class="form-group">
                    <label>Dirección Física</label>
                    <input type="text" name="direccion_empresa" id="set-address" class="form-input" placeholder="Bo. El Centro, Tegucigalpa...">
                </div>

                <div class="form-group row">
                    <div>
                        <label>Sitio Web</label>
                        <input type="text" name="web_empresa" id="set-web" class="form-input" placeholder="www.inversionesolvan.com">
                    </div>
                    <div>
                        <label>Página de Facebook</label>
                        <input type="text" name="facebook_empresa" id="set-fb" class="form-input" placeholder="facebook.com/inversionesolvan">
                    </div>
                </div>

                <div class="form-group">
                    <label style="display:flex; align-items:center; gap:8px; font-weight:600; cursor:pointer;">
                        <input type="checkbox" name="habilitar_facturacion_empresa" id="set-enable-tax" value="1">
                        Habilitar Facturación de Impuestos (ISV 15%)
                    </label>
                </div>

                <h3 style="border-bottom:1px solid var(--border-color); padding-bottom:8px; margin-top:15px;"><i class="fas fa-print" style="color:var(--primary);"></i> Mensajes del Recibo (Pie de Impresión)</h3>
                
                <div class="form-group row">
                    <div>
                        <label>Pie de Factura de Venta</label>
                        <input type="text" name="mensaje_ticket_pie_factura" id="set-pie-invoice" class="form-input" placeholder="Gracias por su compra. Exija su factura.">
                    </div>
                    <div>
                        <label>Pie de Recibo de Caja</label>
                        <input type="text" name="mensaje_ticket_pie_recibo" id="set-pie-receipt" class="form-input" placeholder="Comprobante de compra simple.">
                    </div>
                </div>

                <div class="form-group row">
                    <div>
                        <label>Pie de Ticket de Entrega</label>
                        <input type="text" name="mensaje_ticket_entrega" id="set-pie-delivery" class="form-input" placeholder="Documento para reclamo de productos.">
                    </div>
                    <div>
                        <label>Pie de Cotización</label>
                        <input type="text" name="mensaje_ticket_pie_cotizacion" id="set-pie-quote" class="form-input" placeholder="Precios sujetos a cambios sin previo aviso.">
                    </div>
                </div>
            </div>

            <!-- Right logo upload panel -->
            <div style="border-left:1px solid var(--border-color); padding-left:30px; display:flex; flex-direction:column; align-items:center; gap:20px;">
                <h3 style="width:100%; text-align:center; font-size:1rem; border-bottom:1px solid var(--border-color); padding-bottom:8px;">Logo de Impresión</h3>
                
                <div style="width:150px; height:150px; border:2px dashed var(--border-color); border-radius:var(--border-radius-lg); overflow:hidden; display:flex; align-items:center; justify-content:center;">
                    <img id="set-logo-preview" src="https://cdn-icons-png.flaticon.com/512/869/869045.png" style="max-width:100%; max-height:100%; object-fit:contain;">
                </div>
                
                <div style="width:100%;">
                    <input type="file" id="set-logo-file" class="form-input" onchange="uploadLogoImage()">
                    <input type="hidden" name="logo_empresa_ruta" id="set-logo-url">
                </div>
            </div>
        </div>

        <div style="border-top:1px solid var(--border-color); padding-top:20px; margin-top:30px; display:flex; justify-content:flex-end;">
            <button type="submit" class="btn btn-primary" style="height:48px; padding:0 30px; font-size:1rem;"><i class="fas fa-save"></i> Guardar Configuración</button>
        </div>
    </form>
</div>

<script>
// Settings view logic
(function() {
    window.initSettings = function() {
        loadSettings();

        document.getElementById('settings-form').addEventListener('submit', e => {
            e.preventDefault();
            saveSettings();
        });
    };

    function loadSettings() {
        fetch('controllers/company.php?action=get')
            .then(r => r.json())
            .then(res => {
                if (res.success) {
                    const data = res.data;
                    document.getElementById('set-name').value = data.nombre_empresa || '';
                    document.getElementById('set-rtn').value = data.rtn_empresa || '';
                    document.getElementById('set-owner').value = data.dueño_empresa || '';
                    document.getElementById('set-email').value = data.email_empresa || '';
                    document.getElementById('set-phone').value = data.numero_telefono || '';
                    document.getElementById('set-phone-sec').value = data.telefono_secundario || '';
                    document.getElementById('set-whatsapp').value = data.whatsapp_empresa || '';
                    document.getElementById('set-address').value = data.direccion_empresa || '';
                    document.getElementById('set-web').value = data.web_empresa || '';
                    document.getElementById('set-fb').value = data.facebook_empresa || '';
                    document.getElementById('set-enable-tax').checked = (data.habilitar_facturacion_empresa === 1 || data.habilitar_facturacion_empresa === true || data.habilitar_facturacion_empresa === '1');
                    document.getElementById('set-pie-invoice').value = data.mensaje_ticket_pie_factura || '';
                    document.getElementById('set-pie-receipt').value = data.mensaje_ticket_pie_recibo || '';
                    document.getElementById('set-pie-delivery').value = data.mensaje_ticket_entrega || '';
                    document.getElementById('set-pie-quote').value = data.mensaje_ticket_pie_cotizacion || '';
                    
                    document.getElementById('set-logo-url').value = data.logo_empresa_ruta || '';
                    document.getElementById('set-logo-preview').src = data.logo_empresa_ruta ? data.logo_empresa_ruta : 'https://cdn-icons-png.flaticon.com/512/869/869045.png';
                }
            });
    }

    window.uploadLogoImage = function() {
        const fileInput = document.getElementById('set-logo-file');
        if (fileInput.files.length === 0) return;

        const fd = new FormData();
        // Reutilizamos el endpoint de subida de imágenes de inventario
        fd.append('image', fileInput.files[0]);

        fetch('controllers/inventory.php?action=upload_image', {
            method: 'POST',
            body: fd
        })
        .then(r => r.json())
        .then(res => {
            if (res.success) {
                document.getElementById('set-logo-url').value = res.url;
                document.getElementById('set-logo-preview').src = res.url;
                App.showToast('Logo subido con éxito.');
            } else {
                alert(res.message);
            }
        });
    };

    function saveSettings() {
        const form = document.getElementById('settings-form');
        const fd = new FormData(form);
        
        // Agregar checkbox explícitamente ya que FormData no lo incluye si está desmarcado
        if (!document.getElementById('set-enable-tax').checked) {
            fd.set('habilitar_facturacion_empresa', '0');
        }

        fetch('controllers/company.php?action=save', {
            method: 'POST',
            body: fd
        })
        .then(r => r.json())
        .then(res => {
            if (res.success) {
                App.showToast(res.message);
                loadSettings();
                App.fetchCompanyInfo(); // Actualizar cabecera/título global
            } else {
                alert(res.message);
            }
        });
    }
})();
</script>
