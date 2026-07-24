<?php
session_start();
if (!isset($_SESSION['user_id'])) exit;
?>
<div class="card" style="margin-bottom: 20px;">
    <div>
        <h2><i class="fas fa-history" style="color:var(--primary);"></i> Historial de Ventas</h2>
        <p style="color:var(--text-muted); font-size:0.85rem;">Historial completo de ventas realizadas. Consulte detalles y reimprima tickets.</p>
    </div>
</div>

<div class="card">
    <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom: 20px;">
        <input type="text" id="sales-search" class="pos-input" placeholder="Buscar por venta, cliente, método..." style="width:320px;" oninput="renderSalesTable()">
    </div>

    <div class="data-table-container">
        <table class="data-table">
            <thead>
                <tr>
                    <th>Nº Venta</th>
                    <th>Fecha / Hora</th>
                    <th>Cliente</th>
                    <th>Método de Pago</th>
                    <th>Vendedor</th>
                    <th style="text-align:right;">Subtotal</th>
                    <th style="text-align:right;">ISV (15%)</th>
                    <th style="text-align:right;">Total</th>
                    <th style="width:140px; text-align:right;">Acciones</th>
                </tr>
            </thead>
            <tbody id="sales-tbody">
                <!-- Loaded dynamically -->
            </tbody>
        </table>
    </div>
</div>

<!-- ========================================================= -->
<!-- MODAL: DETALLES DE VENTA -->
<!-- ========================================================= -->
<div class="modal-backdrop" id="sale-details-modal" style="display:none;">
    <div class="modal-content large">
        <div class="modal-header">
            <span class="modal-title" id="details-modal-title">Detalle de Venta</span>
            <button class="modal-close" onclick="closeDetailsModal()">&times;</button>
        </div>
        <div class="modal-body">
            <!-- Cabecera del recibo -->
            <div style="display:grid; grid-template-columns: 1fr 1fr; gap:15px; margin-bottom:20px; font-size:0.9rem;">
                <div>
                    <p><strong>Nº Factura / Recibo:</strong> <span id="detail-id"></span></p>
                    <p><strong>Fecha Venta:</strong> <span id="detail-date"></span></p>
                    <p><strong>Cliente:</strong> <span id="detail-client"></span></p>
                </div>
                <div>
                    <p><strong>Vendedor:</strong> <span id="detail-seller"></span></p>
                    <p><strong>Método Pago:</strong> <span id="detail-method"></span></p>
                    <p id="detail-extra-box" style="display:none;"><strong>Referencia / Banco:</strong> <span id="detail-extra"></span></p>
                </div>
            </div>

            <!-- Tabla de artículos -->
            <div style="border:1px solid var(--border-color); border-radius: var(--border-radius-md); overflow:hidden;">
                <table class="data-table" style="font-size:0.85rem;">
                    <thead>
                        <tr>
                            <th>Código</th>
                            <th>Descripción</th>
                            <th style="text-align:center;">Cantidad</th>
                            <th style="text-align:right;">P. Unitario</th>
                            <th style="text-align:right;">Total Fila</th>
                        </tr>
                    </thead>
                    <tbody id="detail-items-tbody">
                        <!-- Loaded dynamically -->
                    </tbody>
                </table>
            </div>

            <!-- Resumen final -->
            <div style="margin-top:15px; display:flex; flex-direction:column; align-items:flex-end; gap:6px; font-size:0.9rem;">
                <p>Subtotal: <strong id="detail-subtotal">L 0.00</strong></p>
                <p>ISV (15%): <strong id="detail-tax">L 0.00</strong></p>
                <p style="font-size:1.1rem; color:var(--primary);">Total Pagado: <strong id="detail-total">L 0.00</strong></p>
            </div>
        </div>
        <div class="modal-footer">
            <button class="btn btn-secondary btn-sm" onclick="closeDetailsModal()">Cerrar</button>
            <button class="btn btn-blue btn-sm" id="detail-print-btn"><i class="fas fa-print"></i> Reimprimir Ticket</button>
        </div>
    </div>
</div>

<script>
// Sales History Page Logic
(function() {
    let sales = [];

    window.initSalesHistory = function() {
        loadSales();
    };

    function loadSales() {
        fetch('controllers/pos.php?action=list_sales')
            .then(r => r.json())
            .then(res => {
                if (res.success) {
                    sales = res.data;
                    renderSalesTable();
                }
            });
    }

    window.renderSalesTable = function() {
        const query = document.getElementById('sales-search').value.toLowerCase();
        const tbody = document.getElementById('sales-tbody');
        tbody.innerHTML = '';

        const filtered = sales.filter(s => {
            const clientName = (s.nombre_cliente + ' ' + (s.apellido_cliente || '')).toLowerCase();
            return ("venta #" + s.id_ventas).toLowerCase().includes(query) || 
                   clientName.includes(query) || 
                   s.nombre_metodo.toLowerCase().includes(query) || 
                   s.nombre_usuario.toLowerCase().includes(query);
        });

        if (filtered.length === 0) {
            tbody.innerHTML = `<tr><td colspan="9" class="text-center" style="padding:20px; color:var(--text-light);">No se encontraron ventas registradas.</td></tr>`;
            return;
        }

        filtered.forEach(s => {
            const date = new Date(s.fecha_venta).toLocaleDateString('es-HN') + ' ' + new Date(s.fecha_venta).toLocaleTimeString('es-HN', {hour:'2-digit', minute:'2-digit'});
            
            let clientName = s.nombre_cliente + ' ' + (s.apellido_cliente || '');
            if (emptyOrCF(s.nombre_cliente)) clientName = 'CONSUMIDOR FINAL';
            
            tbody.insertAdjacentHTML('beforeend', `
                <tr>
                    <td class="bold">Venta #${s.id_ventas}</td>
                    <td>${date}</td>
                    <td>${clientName}</td>
                    <td>${s.nombre_metodo}</td>
                    <td>${s.nombre_usuario}</td>
                    <td style="text-align:right;">L ${parseFloat(s.subtotal_venta).toFixed(2)}</td>
                    <td style="text-align:right;">L ${parseFloat(s.impuesto_venta).toFixed(2)}</td>
                    <td style="text-align:right;" class="bold">L ${parseFloat(s.total_venta).toFixed(2)}</td>
                    <td style="text-align:right;">
                        <button class="btn btn-secondary btn-sm" onclick="viewSaleDetails(${s.id_ventas})" title="Ver Detalles"><i class="fas fa-eye"></i></button>
                        <button class="btn btn-blue btn-sm" onclick="reprintTicket(${s.id_ventas})" title="Reimprimir Ticket"><i class="fas fa-print"></i></button>
                    </td>
                </tr>
            `);
        });
    };

    function emptyOrCF(name) {
        return !name || name.trim() === '' || name.toLowerCase().includes('consumidor');
    }

    // DETAILS MODAL
    window.viewSaleDetails = function(idVenta) {
        fetch(`controllers/pos.php?action=get_receipt&id_venta=${idVenta}`)
            .then(r => r.json())
            .then(res => {
                if (res.success) {
                    const data = res.data;
                    const venta = data.venta;
                    const detalles = data.detalles;

                    document.getElementById('details-modal-title').textContent = `Detalle de Venta #${venta.id_ventas}`;
                    document.getElementById('detail-id').textContent = venta.id_ventas;
                    
                    const date = new Date(venta.fecha_venta).toLocaleDateString('es-HN') + ' ' + new Date(venta.fecha_venta).toLocaleTimeString('es-HN', {hour:'2-digit', minute:'2-digit'});
                    document.getElementById('detail-date').textContent = date;

                    let clientName = venta.nombre_cliente + ' ' + (venta.apellido_cliente || '');
                    if (emptyOrCF(venta.nombre_cliente)) clientName = 'CONSUMIDOR FINAL';
                    document.getElementById('detail-client').textContent = clientName;

                    document.getElementById('detail-seller').textContent = venta.nombre_usuario || 'Sistema';
                    document.getElementById('detail-method').textContent = venta.nombre_metodo;

                    const extraBox = document.getElementById('detail-extra-box');
                    if (venta.referencia_pago || venta.banco_pago) {
                        let extraText = '';
                        if (venta.banco_pago) extraText += venta.banco_pago;
                        if (venta.referencia_pago) extraText += (extraText ? ' - Ref: ' : 'Ref: ') + venta.referencia_pago;
                        document.getElementById('detail-extra').textContent = extraText;
                        extraBox.style.display = 'block';
                    } else {
                        extraBox.style.display = 'none';
                    }

                    // Renderizar tabla de artículos
                    const tbody = document.getElementById('detail-items-tbody');
                    tbody.innerHTML = '';
                    detalles.forEach(d => {
                        let sn = d.identificador_serie ? `<br><small style="color:var(--text-muted);">S/N: <code>${d.identificador_serie}</code></small>` : '';
                        let w = d.dias_garantia > 0 ? `<br><small style="color:var(--primary); font-weight:500;"><i class="fas fa-shield-alt"></i> Garantía: ${d.dias_garantia} días (${d.estado_garantia})</small>` : '';
                        
                        tbody.insertAdjacentHTML('beforeend', `
                            <tr>
                                <td>${d.codigo_barras_producto || 'N/A'}</td>
                                <td>
                                    <div style="font-weight:600;">${d.descripcion_venta}</div>
                                    ${sn}
                                    ${w}
                                </td>
                                <td class="text-center bold">${d.cantidad_venta}</td>
                                <td style="text-align:right;">L ${parseFloat(d.precio_unitario_venta).toFixed(2)}</td>
                                <td style="text-align:right;" class="bold">L ${parseFloat(d.subtotal_venta).toFixed(2)}</td>
                            </tr>
                        `);
                    });

                    // Totales
                    document.getElementById('detail-subtotal').textContent = 'L ' + parseFloat(venta.subtotal_venta).toFixed(2);
                    document.getElementById('detail-tax').textContent = 'L ' + parseFloat(venta.impuesto_venta).toFixed(2);
                    document.getElementById('detail-total').textContent = 'L ' + parseFloat(venta.total_venta).toFixed(2);

                    // Configurar reimpresión en modal
                    document.getElementById('detail-print-btn').onclick = () => reprintTicket(venta.id_ventas);

                    document.getElementById('sale-details-modal').style.display = 'flex';
                } else {
                    alert(res.message);
                }
            });
    };

    window.closeDetailsModal = function() {
        document.getElementById('sale-details-modal').style.display = 'none';
    };

    window.reprintTicket = function(idVenta) {
        const ticketUrl = `views/ticket.php?id_venta=${idVenta}`;
        window.open(ticketUrl, '_blank', 'width=350,height=600');
    };
})();
</script>
