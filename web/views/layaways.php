<?php
session_start();
if (!isset($_SESSION['user_id'])) exit;
?>
<div class="card" style="margin-bottom: 20px;">
    <div style="display:flex; justify-content:space-between; align-items:center; flex-wrap:wrap; gap:15px;">
        <div>
            <h2><i class="fas fa-hand-holding-usd" style="color:var(--primary);"></i> Control de Apartados</h2>
            <p style="color:var(--text-muted); font-size:0.85rem;">Gestión de reservas de artículos por abonos, registro de pagos y entrega de mercancía.</p>
        </div>
        <button class="btn btn-primary" onclick="openNewLayawayModal()"><i class="fas fa-plus"></i> Nuevo Apartado</button>
    </div>
</div>

<div class="card">
    <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom: 20px; flex-wrap:wrap; gap:15px;">
        <input type="text" id="lay-search" class="pos-input" placeholder="Buscar por cliente o código..." style="width:300px;" oninput="renderLayawaysTable()">
        <div style="display:flex; gap:10px; align-items:center;">
            <span style="font-size:0.85rem; font-weight:600; color:var(--text-muted);">Estado:</span>
            <select id="lay-filter-status" class="form-select" style="width:150px; height:40px; padding:0 10px;" onchange="renderLayawaysTable()">
                <option value="TODOS">Todos</option>
                <option value="VIGENTE">Vigentes (Pagando)</option>
                <option value="PAGADO">Pagados (Sin retirar)</option>
                <option value="ENTREGADO">Entregados</option>
                <option value="CANCELADO">Cancelados</option>
            </select>
        </div>
    </div>

    <div class="data-table-container">
        <table class="data-table">
            <thead>
                <tr>
                    <th>Código</th>
                    <th>Fecha</th>
                    <th>Cliente</th>
                    <th style="text-align:right;">Total</th>
                    <th style="text-align:right;">Saldo Pendiente</th>
                    <th style="text-align:center;">Progreso</th>
                    <th>Estado</th>
                    <th style="width:160px; text-align:right;">Acciones</th>
                </tr>
            </thead>
            <tbody id="layaways-tbody">
                <!-- Loaded dynamically -->
            </tbody>
        </table>
    </div>
</div>

<!-- ========================================================= -->
<!-- MODAL: DETALLES DE APARTADO & HISTORIAL DE ABONOS -->
<!-- ========================================================= -->
<div class="modal-backdrop" id="lay-details-modal" style="display:none;">
    <div class="modal-content large">
        <div class="modal-header">
            <span class="modal-title" id="lay-details-title">Detalle de Apartado</span>
            <button class="modal-close" onclick="closeDetailsModal()">&times;</button>
        </div>
        <div class="modal-body" style="display:grid; grid-template-columns: 1fr 340px; gap:25px;">
            <!-- Left panel: layaway items and abonos list -->
            <div style="display:flex; flex-direction:column; gap:15px; overflow-y:auto; max-height:480px; padding-right:5px;">
                <h4>Artículos Reservados</h4>
                <div style="border:1px solid var(--border-color); border-radius:var(--border-radius-md); overflow:hidden;">
                    <table class="data-table" style="font-size:0.8rem;">
                        <thead>
                            <tr>
                                <th>Descripción</th>
                                <th style="text-align:center; width:60px;">Cant.</th>
                                <th style="text-align:right; width:80px;">Precio</th>
                                <th style="text-align:right; width:80px;">Total</th>
                            </tr>
                        </thead>
                        <tbody id="lay-detail-items-tbody">
                            <!-- items -->
                        </tbody>
                    </table>
                </div>

                <h4 style="margin-top:10px;">Historial de Pagos / Abonos</h4>
                <div style="border:1px solid var(--border-color); border-radius:var(--border-radius-md); overflow:hidden;">
                    <table class="data-table" style="font-size:0.8rem;">
                        <thead>
                            <tr>
                                <th>Fecha</th>
                                <th>Monto</th>
                                <th>Método</th>
                                <th>Ref / Banco</th>
                                <th style="width:40px;"></th>
                            </tr>
                        </thead>
                        <tbody id="lay-detail-abonos-tbody">
                            <!-- abonos -->
                        </tbody>
                    </table>
                </div>
            </div>

            <!-- Right panel: stats and new payment form -->
            <div style="border-left:1px solid var(--border-color); padding-left:20px; display:flex; flex-direction:column; gap:15px;">
                <div class="checkout-client-box" style="margin-bottom:0; flex-direction:column; align-items:flex-start; gap:8px;">
                    <div><strong>Cliente:</strong> <span id="lay-detail-client-name"></span></div>
                    <div><strong>Fecha Creación:</strong> <span id="lay-detail-date"></span></div>
                    <div><strong>Total Reservado:</strong> <span id="lay-detail-total-amount" class="bold"></span></div>
                    <div><strong>Saldo Restante:</strong> <span id="lay-detail-pending-amount" class="bold" style="color:var(--danger)"></span></div>
                </div>

                <!-- Add Abono Form -->
                <div id="lay-add-abono-box" class="card" style="padding:15px; margin-bottom:0; border-color:var(--primary); background:var(--primary-light);">
                    <h4 style="margin-bottom:10px; color:var(--primary);"><i class="fas fa-plus"></i> Registrar Abono</h4>
                    <form id="lay-abono-form">
                        <input type="hidden" id="lay-abono-id-apartado">
                        <div class="form-group" style="margin-bottom:10px;">
                            <label>Monto de Abono (L)</label>
                            <input type="number" step="0.01" id="lay-abono-monto" class="form-input" required placeholder="L 0.00" min="0.01">
                        </div>
                        <div class="form-group" style="margin-bottom:10px;">
                            <label>Método de Pago</label>
                            <select id="lay-abono-metodo" class="form-select" required></select>
                        </div>
                        <div id="lay-abono-extra-fields" style="display:none; border-top:1px dashed var(--primary); padding-top:8px; margin-top:8px;">
                            <div class="form-group" style="margin-bottom:10px;">
                                <label>Banco</label>
                                <select id="lay-abono-banco" class="form-select">
                                    <option>Seleccione Banco...</option>
                                    <option>Banco Atlántida</option>
                                    <option>BAC Credomatic</option>
                                    <option>Banco Ficohsa</option>
                                    <option>Banpaís</option>
                                    <option>Banco de Occidente</option>
                                    <option>Banco Banrural</option>
                                    <option>Banco Promerica</option>
                                    <option>Banco LAFISE</option>
                                    <option>ACH - Transferencia Interbancaria</option>
                                </select>
                            </div>
                            <div class="form-group" style="margin-bottom:10px;">
                                <label>Nº Referencia</label>
                                <input type="text" id="lay-abono-ref" class="form-input" placeholder="Transacción / Voucher">
                            </div>
                        </div>
                        <button type="submit" class="btn btn-primary" style="width:100%; height:40px; margin-top:8px;">Guardar Pago</button>
                    </form>
                </div>

                <!-- Delivery/Cancel actions panel -->
                <div id="lay-actions-box" style="display:flex; flex-direction:column; gap:10px; margin-top:auto;">
                    <!-- buttons loaded dynamically -->
                </div>
            </div>
        </div>
    </div>
</div>

<!-- ========================================================= -->
<!-- MODAL: REGISTRAR NUEVO APARTADO -->
<!-- ========================================================= -->
<div class="modal-backdrop" id="lay-create-modal" style="display:none;">
    <div class="modal-content large">
        <div class="modal-header">
            <span class="modal-title"><i class="fas fa-hand-holding-usd" style="color:var(--primary)"></i> Crear Nuevo Apartado</span>
            <button class="modal-close" onclick="closeNewLayawayModal()">&times;</button>
        </div>
        <div class="modal-body" style="display:grid; grid-template-columns: 1fr 340px; gap:20px; max-height:480px; overflow-y:hidden;">
            <!-- Left panel: shopping list -->
            <div style="display:flex; flex-direction:column; background:#f8fafc; border:1px solid var(--border-color); border-radius:var(--border-radius-md); overflow:hidden;">
                <div style="padding:12px; border-bottom:1px solid var(--border-color); display:flex; justify-content:space-between; align-items:center; background:#fff;">
                    <span id="lay-nc-client-label" style="font-weight:700;">CONSUMIDOR FINAL</span>
                    <button class="btn btn-secondary btn-sm" onclick="layOpenClientSearch()"><i class="fas fa-user-check"></i> Cambiar Cliente</button>
                </div>
                <!-- Cart rows -->
                <div style="flex-grow:1; overflow-y:auto; padding:10px;" id="lay-nc-cart-box">
                    <!-- Dynamic cart list -->
                </div>
                <div style="padding:10px; border-top:1px solid var(--border-color); background:#fff; display:flex; justify-content:flex-end;">
                    <button class="btn btn-blue btn-sm" onclick="layOpenCatalog()"><i class="fas fa-plus"></i> Agregar Artículos</button>
                </div>
            </div>

            <!-- Right panel: totals and initial abono -->
            <div style="display:flex; flex-direction:column; gap:12px;">
                <div class="checkout-summary" style="margin-top:0; border:none; padding-top:0;">
                    <div class="summary-row">
                        <span>Total Productos:</span>
                        <span id="lay-nc-subtotal">L 0.00</span>
                    </div>
                    <div class="summary-row">
                        <span>ISV (15%):</span>
                        <span id="lay-nc-tax">L 0.00</span>
                    </div>
                    <div class="summary-row total" style="border-bottom:1px solid var(--border-color); padding-bottom:10px; margin-bottom:10px;">
                        <span>Total Reservado:</span>
                        <span id="lay-nc-total" class="total-amount">L 0.00</span>
                    </div>
                </div>

                <div class="form-group">
                    <label>Abono Inicial (L)</label>
                    <input type="number" step="0.01" id="lay-nc-abono" class="form-input" required placeholder="L 0.00" value="0.00">
                </div>

                <div id="lay-nc-pay-box" class="card" style="padding:12px; margin-bottom:0; display:none; border-style:dashed;">
                    <div class="form-group" style="margin-bottom:8px;">
                        <label>Método de Abono Inicial</label>
                        <select id="lay-nc-method" class="form-select"></select>
                    </div>
                    <div id="lay-nc-extra-fields" style="display:none;">
                        <div class="form-group" style="margin-bottom:8px;">
                            <label>Banco</label>
                            <select id="lay-nc-bank" class="form-select">
                                <option>Seleccione Banco...</option>
                                <option>Banco Atlántida</option>
                                <option>BAC Credomatic</option>
                                <option>Banco Ficohsa</option>
                                <option>Banpaís</option>
                                <option>Banco de Occidente</option>
                            </select>
                        </div>
                        <div class="form-group" style="margin-bottom:0;">
                            <label id="lay-nc-ref-label">Nº Referencia:</label>
                            <input type="text" id="lay-nc-ref" class="form-input" placeholder="Transacción / Voucher">
                        </div>
                    </div>
                </div>

                <button class="btn btn-primary" onclick="laySubmitCreate()" style="width:100%; height:48px; font-weight:700; margin-top:auto;">
                    <i class="fas fa-check"></i> Registrar Reservación
                </button>
            </div>
        </div>
    </div>
</div>

<!-- ========================================================= -->
<!-- MODAL: SELECCIONAR CLIENTE (APARTADOS) -->
<!-- ========================================================= -->
<div class="modal-backdrop" id="lay-client-modal" style="display:none;">
    <div class="modal-content">
        <div class="modal-header">
            <span class="modal-title"><i class="fas fa-users" style="color:var(--primary)"></i> Seleccionar Cliente</span>
            <button class="modal-close" onclick="layCloseClientSearch()">&times;</button>
        </div>
        <div class="modal-body" style="padding:15px 24px;">
            <input type="text" id="lay-client-filter" class="form-input" placeholder="Buscar por nombre o identidad..." style="margin-bottom:15px;" oninput="layRenderClientList()">
            <div style="max-height:300px; overflow-y:auto; border:1px solid var(--border-color); border-radius:var(--border-radius-md);">
                <table class="data-table" style="font-size:0.85rem;">
                    <thead>
                        <tr>
                            <th>Identidad</th>
                            <th>Nombre</th>
                            <th></th>
                        </tr>
                    </thead>
                    <tbody id="lay-client-table-body">
                        <!-- Loaded dynamically -->
                    </tbody>
                </table>
            </div>
        </div>
    </div>
</div>

<!-- ========================================================= -->
<!-- MODAL: CATÁLOGO DE PRODUCTOS (APARTADOS) -->
<!-- ========================================================= -->
<div class="modal-backdrop" id="lay-catalog-modal" style="display:none;">
    <div class="modal-content large">
        <div class="modal-header">
            <span class="modal-title"><i class="fas fa-th" style="color:var(--primary)"></i> Catálogo de Productos</span>
            <button class="modal-close" onclick="layCloseCatalog()">&times;</button>
        </div>
        <div class="modal-body" style="padding:15px 24px;">
            <input type="text" id="lay-catalog-filter" class="form-input" placeholder="Buscar por nombre o código..." style="margin-bottom:15px;" oninput="layRenderCatalog()">
            <div style="max-height:350px; overflow-y:auto; border:1px solid var(--border-color); border-radius:var(--border-radius-md);">
                <table class="data-table" style="font-size:0.85rem;">
                    <thead>
                        <tr>
                            <th style="width:60px;">Foto</th>
                            <th>Código</th>
                            <th>Descripción</th>
                            <th>Precio</th>
                            <th>Stock</th>
                            <th></th>
                        </tr>
                    </thead>
                    <tbody id="lay-catalog-table-body">
                        <!-- Loaded dynamically -->
                    </tbody>
                </table>
            </div>
        </div>
    </div>
</div>

<script>
// Layaway dashboard logic
(function() {
    let layaways = [];
    let paymentMethods = [];
    let clients = [];
    let products = [];
    let activeClient = { id_cliente: 1, name: 'CONSUMIDOR FINAL' };
    let cart = [];
    let activeLayawayId = null;

    window.initLayaways = function() {
        loadLayaways();
        loadPaymentMethods();
        loadMetadata();

        // Payment method abono change extra fields handler
        const payMethod = document.getElementById('lay-abono-metodo');
        payMethod.addEventListener('change', () => {
            const opt = payMethod.options[payMethod.selectedIndex];
            const text = opt.text.toLowerCase();
            const extra = document.getElementById('lay-abono-extra-fields');
            if (text.includes('tarjeta') || text.includes('transferencia')) {
                extra.style.display = 'block';
            } else {
                extra.style.display = 'none';
                document.getElementById('lay-abono-ref').value = '';
                document.getElementById('lay-abono-banco').selectedIndex = 0;
            }
        });

        // initial abono POS change handler
        const initAbono = document.getElementById('lay-nc-abono');
        initAbono.addEventListener('input', () => {
            const val = parseFloat(initAbono.value) || 0;
            const payBox = document.getElementById('lay-nc-pay-box');
            if (val > 0) {
                payBox.style.display = 'block';
            } else {
                payBox.style.display = 'none';
                document.getElementById('lay-nc-ref').value = '';
                document.getElementById('lay-nc-bank').selectedIndex = 0;
                document.getElementById('lay-nc-extra-fields').style.display = 'none';
            }
        });

        const initMethod = document.getElementById('lay-nc-method');
        initMethod.addEventListener('change', () => {
            const opt = initMethod.options[initMethod.selectedIndex];
            const text = opt.text.toLowerCase();
            const extra = document.getElementById('lay-nc-extra-fields');
            if (text.includes('tarjeta') || text.includes('transferencia')) {
                document.getElementById('lay-nc-ref-label').textContent = text.includes('transferencia') ? 'Nº Referencia / ACH:' : 'Nº Voucher / Referencia:';
                extra.style.display = 'block';
            } else {
                extra.style.display = 'none';
                document.getElementById('lay-nc-ref').value = '';
                document.getElementById('lay-nc-bank').selectedIndex = 0;
            }
        });

        // Form submit handlers
        document.getElementById('lay-abono-form').addEventListener('submit', e => {
            e.preventDefault();
            saveAbono();
        });
    };

    function loadLayaways() {
        fetch('controllers/layaways.php?action=list')
            .then(r => r.json())
            .then(res => {
                if (res.success) {
                    layaways = res.data;
                    renderLayawaysTable();
                }
            });
    }

    function loadPaymentMethods() {
        fetch('controllers/pos.php?action=get_payment_methods')
            .then(r => r.json())
            .then(res => {
                if (res.success) {
                    paymentMethods = res.data;
                    
                    const selAbono = document.getElementById('lay-abono-metodo');
                    const selInit = document.getElementById('lay-nc-method');
                    
                    selAbono.innerHTML = '';
                    selInit.innerHTML = '';
                    
                    paymentMethods.forEach(m => {
                        selAbono.insertAdjacentHTML('beforeend', `<option value="${m.id_metodo_pago}">${m.nombre_metodo}</option>`);
                        selInit.insertAdjacentHTML('beforeend', `<option value="${m.id_metodo_pago}">${m.nombre_metodo}</option>`);
                    });
                }
            });
    }

    function loadMetadata() {
        fetch('controllers/clients.php?action=list&active=true')
            .then(r => r.json())
            .then(res => { if (res.success) clients = res.data; });
            
        fetch('controllers/inventory.php?action=list')
            .then(r => r.json())
            .then(res => { if (res.success) products = res.data; });
    }

    window.renderLayawaysTable = function() {
        const query = document.getElementById('lay-search').value.toLowerCase();
        const filterStatus = document.getElementById('lay-filter-status').value;
        const tbody = document.getElementById('layaways-tbody');
        tbody.innerHTML = '';

        const filtered = layaways.filter(l => {
            const clientName = (l.nombre_cliente + ' ' + (l.apellido_cliente || '')).toLowerCase();
            const matchesQuery = clientName.includes(query) || ("#" + l.id_apartado).includes(query);
            const matchesStatus = (filterStatus === 'TODOS') || (l.estado_apartado === filterStatus);
            return matchesQuery && matchesStatus;
        });

        if (filtered.length === 0) {
            tbody.innerHTML = `<tr><td colspan="8" class="text-center" style="padding:20px; color:var(--text-light);">No se encontraron apartados registrados.</td></tr>`;
            return;
        }

        filtered.forEach(l => {
            const date = new Date(l.fecha_apartado).toLocaleDateString('es-HN');
            const total = parseFloat(l.total_apartado);
            const pending = parseFloat(l.saldo_pendiente);
            const progress = ((total - pending) / total) * 100;
            
            let badge = '';
            if (l.estado_apartado === 'VIGENTE') badge = '<span class="badge badge-warning">Vigente (Abonando)</span>';
            else if (l.estado_apartado === 'PAGADO') badge = '<span class="badge badge-success">Pagado (Sin retirar)</span>';
            else if (l.estado_apartado === 'ENTREGADO') badge = '<span class="badge badge-success" style="background:#e6fffa; color:#234e52; border:1px solid #b2f5ea;">Entregado</span>';
            else if (l.estado_apartado === 'CANCELADO') badge = '<span class="badge badge-neutral">Cancelado</span>';

            const clientName = l.nombre_cliente + ' ' + (l.apellido_cliente || '');

            tbody.insertAdjacentHTML('beforeend', `
                <tr>
                    <td class="bold">Apartado #${l.id_apartado}</td>
                    <td>${date}</td>
                    <td>${clientName}</td>
                    <td style="text-align:right;">L ${total.toFixed(2)}</td>
                    <td style="text-align:right;" class="bold ${pending > 0 ? 'trend-down' : 'trend-up'}">L ${pending.toFixed(2)}</td>
                    <td style="text-align:center; vertical-align:middle; width:150px;">
                        <div style="background:#e2e8f0; border-radius:10px; height:8px; overflow:hidden; position:relative; width:100%;">
                            <div style="background:var(--primary); height:100%; width:${progress}%;"></div>
                        </div>
                        <small style="font-size:0.75rem; color:var(--text-muted); font-weight:600;">${progress.toFixed(0)}% pagado</small>
                    </td>
                    <td>${badge}</td>
                    <td style="text-align:right;">
                        <button class="btn btn-secondary btn-sm" onclick="viewLayawayDetails(${l.id_apartado})" title="Ver Detalles/Abonar"><i class="fas fa-folder-open"></i> Abrir</button>
                    </td>
                </tr>
            `);
        });
    };

    // DETAILS & PAY MODAL
    window.viewLayawayDetails = function(id) {
        activeLayawayId = id;
        fetch(`controllers/layaways.php?action=get&id=${id}`)
            .then(r => r.json())
            .then(res => {
                if (res.success) {
                    const data = res.data;
                    const header = data.header;
                    const items = data.items;
                    const abonos = data.abonos;

                    document.getElementById('lay-details-title').textContent = `Detalles de Apartado #${header.id_apartado}`;
                    document.getElementById('lay-abono-id-apartado').value = header.id_apartado;

                    const clientFullName = header.nombre_cliente + ' ' + (header.apellido_cliente || '');
                    document.getElementById('lay-detail-client-name').textContent = clientFullName;
                    
                    const date = new Date(header.fecha_apartado).toLocaleDateString('es-HN') + ' ' + new Date(header.fecha_apartado).toLocaleTimeString('es-HN', {hour:'2-digit', minute:'2-digit'});
                    document.getElementById('lay-detail-date').textContent = date;
                    
                    document.getElementById('lay-detail-total-amount').textContent = 'L ' + parseFloat(header.total_apartado).toFixed(2);
                    document.getElementById('lay-detail-pending-amount').textContent = 'L ' + parseFloat(header.saldo_pendiente).toFixed(2);

                    // Render items list
                    const itemsTbody = document.getElementById('lay-detail-items-tbody');
                    itemsTbody.innerHTML = '';
                    items.forEach(it => {
                        let serial = it.identificador_serie ? `<br><small style="color:var(--text-muted);">S/N: <code>${it.identificador_serie}</code></small>` : '';
                        itemsTbody.insertAdjacentHTML('beforeend', `
                            <tr>
                                <td><div style="font-weight:600;">${it.descripcion_apartado}</div>${serial}</td>
                                <td class="text-center bold">${it.cantidad_apartado}</td>
                                <td style="text-align:right;">L ${parseFloat(it.precio_unitario_apartado).toFixed(2)}</td>
                                <td style="text-align:right;" class="bold">L ${parseFloat(it.subtotal_apartado).toFixed(2)}</td>
                            </tr>
                        `);
                    });

                    // Render abonos list
                    const abonosTbody = document.getElementById('lay-detail-abonos-tbody');
                    abonosTbody.innerHTML = '';
                    if (abonos.length === 0) {
                        abonosTbody.innerHTML = `<tr><td colspan="5" class="text-center" style="color:var(--text-light); padding:15px;">Ningún abono registrado.</td></tr>`;
                    } else {
                        abonos.forEach(ab => {
                            const dateAb = new Date(ab.fecha_abono).toLocaleDateString('es-HN');
                            let refText = '-';
                            if (ab.banco_pago) refText = ab.banco_pago;
                            if (ab.referencia_pago) refText += (ab.banco_pago ? ' / ' : '') + ab.referencia_pago;

                            abonosTbody.insertAdjacentHTML('beforeend', `
                                <tr>
                                    <td>${dateAb}</td>
                                    <td class="bold">L ${parseFloat(ab.monto_abono).toFixed(2)}</td>
                                    <td>${ab.nombre_metodo}</td>
                                    <td>${refText}</td>
                                    <td style="text-align:center;">
                                        <button class="btn btn-secondary btn-sm" onclick="printAbonoTicket(${ab.id_abono})" title="Imprimir Comprobante" style="padding:2px 6px; height:24px;"><i class="fas fa-print"></i></button>
                                    </td>
                                </tr>
                            `);
                        });
                    }

                    // Configurar formulario de abono
                    const formBox = document.getElementById('lay-add-abono-box');
                    const actionsBox = document.getElementById('lay-actions-box');
                    
                    formBox.style.display = 'none';
                    actionsBox.innerHTML = '';

                    const isPending = parseFloat(header.saldo_pendiente) > 0;
                    const isVigente = header.estado_apartado === 'VIGENTE';
                    const isPagado = header.estado_apartado === 'PAGADO';

                    if (isVigente && isPending) {
                        formBox.style.display = 'block';
                        document.getElementById('lay-abono-monto').value = '';
                        document.getElementById('lay-abono-monto').max = header.saldo_pendiente;
                        document.getElementById('lay-abono-ref').value = '';
                        document.getElementById('lay-abono-banco').selectedIndex = 0;
                        document.getElementById('lay-abono-extra-fields').style.display = 'none';
                    }

                    // Cancel button
                    if (isVigente || isPagado) {
                        actionsBox.insertAdjacentHTML('beforeend', `
                            <button class="btn btn-danger" style="width:100%; height:44px; font-weight:700;" onclick="cancelLayaway(${header.id_apartado})">
                                <i class="fas fa-times-circle"></i> Cancelar Reservación
                            </button>
                        `);
                    }

                    // Deliver button
                    if (isPagado) {
                        actionsBox.insertAdjacentHTML('beforeend', `
                            <button class="btn btn-primary" style="width:100%; height:48px; font-weight:800; background:#27ae60;" onclick="deliverLayaway(${header.id_apartado})">
                                <i class="fas fa-truck"></i> Entregar Mercancía
                            </button>
                        `);
                    }

                    document.getElementById('lay-details-modal').style.display = 'flex';
                } else {
                    alert(res.message);
                }
            });
    };

    window.closeDetailsModal = function() {
        document.getElementById('lay-details-modal').style.display = 'none';
        activeLayawayId = null;
    };

    function saveAbono() {
        const id = document.getElementById('lay-abono-id-apartado').value;
        const monto = parseFloat(document.getElementById('lay-abono-monto').value);
        const metodo = document.getElementById('lay-abono-metodo').value;
        const ref = document.getElementById('lay-abono-ref').value;
        const banco = document.getElementById('lay-abono-banco').value;

        App.checkActiveCashSession((caja) => {
            const fd = new FormData();
            fd.append('id_apartado', id);
            fd.append('monto_abono', monto);
            fd.append('id_metodo_pago', metodo);
            fd.append('referencia_pago', ref);
            fd.append('banco_pago', banco);

            fetch('controllers/layaways.php?action=add_abono', {
                method: 'POST',
                body: fd
            })
            .then(r => r.json())
            .then(res => {
                if (res.success) {
                    App.showToast(res.message);
                    
                    // Imprimir recibo de abono
                    printAbonoTicket(res.id_abono);

                    loadLayaways();
                    // Recargar el modal
                    viewLayawayDetails(id);
                } else {
                    alert(res.message);
                }
            })
            .catch(err => {
                alert('Error al registrar abono.');
            });
        });
    }

    window.printAbonoTicket = function(idAbono) {
        const ticketUrl = `views/ticket_abono.php?id_abono=${idAbono}`;
        window.open(ticketUrl, '_blank', 'width=350,height=600');
    };

    window.deliverLayaway = function(id) {
        App.confirm('¿Confirmar que la mercancía ha sido entregada y retirada físicamente por el cliente?', () => {
            const fd = new FormData();
            fd.append('id_apartado', id);

            fetch('controllers/layaways.php?action=deliver', {
                method: 'POST',
                body: fd
            })
            .then(r => r.json())
            .then(res => {
                if (res.success) {
                    App.showToast(res.message);
                    loadLayaways();
                    closeDetailsModal();
                } else {
                    alert(res.message);
                }
            });
        });
    };

    window.cancelLayaway = function(id) {
        App.confirm('Al cancelar, los productos se devolverán al inventario activo. ¿Desea continuar?', () => {
            App.askSignature((userId) => {
                const fd = new FormData();
                fd.append('id_apartado', id);
                fd.append('id_usuario_firma', userId);

                fetch('controllers/layaways.php?action=cancel', {
                    method: 'POST',
                    body: fd
                })
                .then(r => r.json())
                .then(res => {
                    if (res.success) {
                        App.showToast(res.message);
                        loadLayaways();
                        closeDetailsModal();
                    } else {
                        alert(res.message);
                    }
                });
            });
        });
    };

    // =========================================================
    // CREATION LAYAWAY WIZARD LOGIC
    // =========================================================
    window.openNewLayawayModal = function() {
        document.getElementById('lay-create-modal').style.display = 'flex';
        activeClient = { id_cliente: 1, name: 'CONSUMIDOR FINAL' };
        document.getElementById('lay-nc-client-label').textContent = 'CONSUMIDOR FINAL';
        cart = [];
        document.getElementById('lay-nc-abono').value = '0.00';
        document.getElementById('lay-nc-pay-box').style.display = 'none';
        document.getElementById('lay-nc-ref').value = '';
        document.getElementById('lay-nc-bank').selectedIndex = 0;
        document.getElementById('lay-nc-extra-fields').style.display = 'none';
        renderCreateCart();
    };

    window.closeNewLayawayModal = function() {
        document.getElementById('lay-create-modal').style.display = 'none';
    };

    function renderCreateCart() {
        const box = document.getElementById('lay-nc-cart-box');
        if (cart.length === 0) {
            box.innerHTML = `
                <div style="text-align:center; padding:50px 10px; color:var(--text-light);">
                    <i class="fas fa-dolly-flatbed" style="font-size:2.5rem; display:block; margin-bottom:10px;"></i>
                    Ningún artículo agregado. Agregue artículos desde el catálogo.
                </div>
            `;
            recalculateCreateTotals(0);
            return;
        }

        box.innerHTML = '';
        let sumSub = 0;

        cart.forEach((item, idx) => {
            sumSub += item.subtotal_fila;
            const img = item.ruta_imagen ? item.ruta_imagen.split('|')[0] : 'https://cdn-icons-png.flaticon.com/512/869/869045.png';
            
            let imeiField = '';
            if (item.requiere_serie) {
                imeiField = `
                    <input type="text" class="imei-field" style="width:180px; margin-top:5px;" placeholder="Serie / IMEI..." 
                           value="${item.imei}" onchange="layUpdateCartImei(${idx}, this.value)">
                `;
            }

            box.insertAdjacentHTML('beforeend', `
                <div style="background:#fff; border:1px solid var(--border-color); border-radius:var(--border-radius-md); padding:10px; display:flex; gap:10px; margin-bottom:10px; align-items:center;">
                    <img src="${img}" style="width:40px; height:40px; object-fit:cover; border-radius:4px;" onerror="this.src='https://cdn-icons-png.flaticon.com/512/869/869045.png'">
                    <div style="flex-grow:1; overflow:hidden;">
                        <div style="font-weight:600; font-size:0.85rem; white-space:nowrap; text-overflow:ellipsis; overflow:hidden;">${item.nombre}</div>
                        ${imeiField}
                    </div>
                    <div class="pos-qty-ctrl" style="gap:5px;">
                        <button class="pos-qty-btn" style="width:24px; height:24px;" onclick="layUpdateQty(${idx}, -1)">&minus;</button>
                        <span class="pos-qty-val" style="font-size:0.85rem; width:15px;">${item.cantidad}</span>
                        <button class="pos-qty-btn" style="width:24px; height:24px;" onclick="layUpdateQty(${idx}, 1)">&plus;</button>
                    </div>
                    <div style="text-align:right; font-weight:700; font-size:0.85rem; min-width:80px;">L ${item.subtotal_fila.toFixed(2)}</div>
                    <button class="pos-item-remove" style="padding:5px;" onclick="layRemoveCartItem(${idx})"><i class="fas fa-times"></i></button>
                </div>
            `);
        });

        recalculateCreateTotals(sumSub);
    }

    function recalculateCreateTotals(subtotal) {
        const tax = subtotal * 0.15;
        const total = subtotal + tax;
        document.getElementById('lay-nc-subtotal').textContent = 'L ' + subtotal.toFixed(2);
        document.getElementById('lay-nc-tax').textContent = 'L ' + tax.toFixed(2);
        document.getElementById('lay-nc-total').textContent = 'L ' + total.toFixed(2);
    }

    window.layUpdateQty = function(index, change) {
        const next = cart[index].cantidad + change;
        if (next <= 0) {
            layRemoveCartItem(index);
            return;
        }
        if (next > cart[index].stock_max) {
            App.showToast(`Stock insuficiente. Stock actual: ${cart[index].stock_max}`, 'error');
            return;
        }
        cart[index].cantidad = next;
        cart[index].subtotal_fila = next * cart[index].precio;
        renderCreateCart();
    };

    window.layUpdateCartImei = function(index, val) {
        cart[index].imei = val.trim();
    };

    window.layRemoveCartItem = function(index) {
        cart.splice(index, 1);
        renderCreateCart();
    };

    // Client selection logic (apartados)
    window.layOpenClientSearch = function() {
        document.getElementById('lay-client-modal').style.display = 'flex';
        document.getElementById('lay-client-filter').value = '';
        layRenderClientList();
    };

    window.layCloseClientSearch = function() {
        document.getElementById('lay-client-modal').style.display = 'none';
    };

    window.layRenderClientList = function() {
        const query = document.getElementById('lay-client-filter').value.toLowerCase();
        const tbody = document.getElementById('lay-client-table-body');
        tbody.innerHTML = '';

        const filtered = clients.filter(c => {
            const name = (c.nombre_cliente + ' ' + (c.apellido_cliente || '')).toLowerCase();
            const id = (c.identidad_cliente || '').toLowerCase();
            return name.includes(query) || id.includes(query);
        });

        filtered.forEach(c => {
            const fullName = c.nombre_cliente + ' ' + (c.apellido_cliente || '');
            tbody.insertAdjacentHTML('beforeend', `
                <tr>
                    <td>${c.identidad_cliente || 'N/A'}</td>
                    <td class="bold">${fullName}</td>
                    <td style="text-align:right;">
                        <button class="btn btn-primary btn-sm" onclick="laySelectClient(${c.id_cliente}, '${fullName.replace(/'/g, "\\'")}')">Seleccionar</button>
                    </td>
                </tr>
            `);
        });
    };

    window.laySelectClient = function(id, name) {
        activeClient = { id_cliente: id, name: name };
        document.getElementById('lay-nc-client-label').textContent = name;
        layCloseClientSearch();
    };

    // Catalog logic (apartados)
    window.layOpenCatalog = function() {
        document.getElementById('lay-catalog-modal').style.display = 'flex';
        document.getElementById('lay-catalog-filter').value = '';
        layRenderCatalog();
    };

    window.layCloseCatalog = function() {
        document.getElementById('lay-catalog-modal').style.display = 'none';
    };

    window.layRenderCatalog = function() {
        const query = document.getElementById('lay-catalog-filter').value.toLowerCase();
        const tbody = document.getElementById('lay-catalog-table-body');
        tbody.innerHTML = '';

        const filtered = products.filter(p => {
            const name = p.nombre_producto.toLowerCase();
            const barcode = (p.codigo_barras_producto || '').toLowerCase();
            return name.includes(query) || barcode.includes(query);
        });

        filtered.forEach(p => {
            const imgFull = p.ruta_imagen_producto ? p.ruta_imagen_producto : 'https://cdn-icons-png.flaticon.com/512/869/869045.png';
            const imgFirst = p.ruta_imagen_producto ? p.ruta_imagen_producto.split('|')[0] : 'https://cdn-icons-png.flaticon.com/512/869/869045.png';
            tbody.insertAdjacentHTML('beforeend', `
                <tr>
                    <td><img src="${imgFirst}" style="width:30px; height:30px; object-fit:contain; background:#f8fafc; border-radius:4px; cursor:pointer;" onclick="App.showImagePreview('${imgFull}')" onerror="this.src='https://cdn-icons-png.flaticon.com/512/869/869045.png'"></td>
                    <td>${p.codigo_barras_producto || 'N/A'}</td>
                    <td class="bold">${p.nombre_producto}</td>
                    <td>L ${parseFloat(p.precio_venta_producto).toFixed(2)}</td>
                    <td class="bold">${p.stock_producto}</td>
                    <td style="text-align:right;">
                        <button class="btn btn-blue btn-sm" onclick="layAddFromCatalog(${p.id_producto})" ${p.stock_producto <= 0 ? 'disabled' : ''}>Agregar</button>
                    </td>
                </tr>
            `);
        });
    };

    window.layAddFromCatalog = function(id) {
        const p = products.find(prod => prod.id_producto == id);
        if (p) {
            const idx = cart.findIndex(it => it.id_producto == p.id_producto);
            if (idx >= 0) {
                layUpdateQty(idx, 1);
            } else {
                cart.push({
                    id_producto: p.id_producto,
                    nombre: p.nombre_producto,
                    precio: parseFloat(p.precio_venta_producto),
                    cantidad: 1,
                    subtotal_fila: parseFloat(p.precio_venta_producto),
                    stock_max: p.stock_producto,
                    requiere_serie: p.requiere_serie,
                    ruta_imagen: p.ruta_imagen_producto,
                    imei: ''
                });
            }
            renderCreateCart();
            layCloseCatalog();
        }
    };

    // SUBMIT CREATE LAYAWAY
    window.laySubmitCreate = function() {
        if (cart.length === 0) {
            App.showToast('Agregue artículos al apartado.', 'error');
            return;
        }

        const missingSeries = cart.filter(it => it.requiere_serie && !it.imei);
        if (missingSeries.length > 0) {
            App.showToast('Por favor ingrese el número de serie/IMEI para los artículos requeridos.', 'error');
            return;
        }

        let subtotal = 0;
        cart.forEach(it => subtotal += it.subtotal_fila);
        const tax = subtotal * 0.15;
        const total = subtotal + tax;

        const abono = parseFloat(document.getElementById('lay-nc-abono').value) || 0;
        if (abono > total) {
            App.showToast('El abono inicial no puede ser mayor que el total reservado.', 'error');
            return;
        }

        const method = document.getElementById('lay-nc-method').value;
        const ref = document.getElementById('lay-nc-ref').value;
        const bank = document.getElementById('lay-nc-bank').value;

        App.checkActiveCashSession((caja) => {
            const fd = new FormData();
            fd.append('id_cliente', activeClient.id_cliente);
            fd.append('total', total.toFixed(4));
            fd.append('abono_inicial', abono.toFixed(4));
            fd.append('id_metodo_pago', method);
            fd.append('referencia_pago', ref);
            fd.append('banco_pago', bank);
            fd.append('detalles', JSON.stringify(cart));

            fetch('controllers/layaways.php?action=save', {
                method: 'POST',
                body: fd
            })
            .then(r => r.json())
            .then(res => {
                if (res.success) {
                    App.showToast(res.message);
                    
                    // Si hizo abono inicial, imprimir recibo de abono
                    if (abono > 0) {
                        fetch(`controllers/layaways.php?action=get&id=${res.id_apartado}`)
                            .then(r => r.json())
                            .then(resG => {
                                if (resG.success && resG.data.abonos.length > 0) {
                                    printAbonoTicket(resG.data.abonos[0].id_abono);
                                }
                            });
                    }

                    loadLayaways();
                    closeNewLayawayModal();
                } else {
                    alert(res.message);
                }
            })
            .catch(err => {
                alert('Error al registrar apartado.');
            });
        });
    };
})();
</script>
