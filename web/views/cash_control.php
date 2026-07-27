<?php
session_start();
if (!isset($_SESSION['user_id'])) exit;
?>
<div class="card" style="margin-bottom: 20px;">
    <div>
        <h2><i class="fas fa-cash-register" style="color:var(--primary);"></i> Control de Turnos y Cajas</h2>
        <p style="color:var(--text-muted); font-size:0.85rem;">Gestione la apertura, el registro de ingresos y el arqueo/cierre de caja diario.</p>
    </div>
</div>

<!-- ========================================================= -->
<!-- SECCIÓN DE CAJA ABIERTA ACTUAL -->
<!-- ========================================================= -->
<div id="cash-active-session-container" class="card" style="display:none; border-color:var(--primary); background:#fcfefe; margin-bottom:20px;">
    <div style="display:flex; justify-content:space-between; flex-wrap:wrap; gap:30px;">
        <!-- Left: Open calculations details -->
        <div style="flex-grow:1; display:flex; flex-direction:column; gap:12px;">
            <h3 style="color:var(--primary); border-bottom:1px solid var(--border-color); padding-bottom:8px; display:flex; align-items:center; gap:8px;">
                <span class="pulse-indicator" style="background:#27ae60;"></span> Turno de Caja Activo
            </h3>
            <div style="display:grid; grid-template-columns: 1fr 1fr; gap:12px; font-size:0.9rem;">
                <div>
                    <p><strong>Cajero de Apertura:</strong> <span id="cash-act-user"></span></p>
                    <p><strong>Fecha Apertura:</strong> <span id="cash-act-date"></span></p>
                    <p><strong>Efectivo Inicial (Apertura):</strong> <span id="cash-act-start" class="bold"></span></p>
                </div>
                <?php if ($_SESSION['role_id'] != 3): ?>
                <div style="border-left:1px solid var(--border-color); padding-left:20px;">
                    <p>Total Ventas Turno: <span id="cash-act-sales" class="bold"></span></p>
                    <p>Total Abonos Turno: <span id="cash-act-abonos" class="bold"></span></p>
                    <p style="font-size:1.1rem; color:var(--primary); margin-top:8px;">
                        <strong>Efectivo Esperado en Gaveta:</strong> <br>
                        <span id="cash-act-expected" class="bold" style="font-size:1.3rem;">L 0.00</span>
                    </p>
                </div>
                <?php else: ?>
                <div style="border-left:1px solid var(--border-color); padding-left:20px; display:flex; align-items:center; color:var(--text-muted);">
                    <p style="font-size:0.85rem; font-style:italic; line-height:1.4;">
                        <i class="fas fa-eye-slash" style="color:var(--warning); margin-right:4px;"></i> <strong style="color:var(--text-main)">Arqueo a ciegas habilitado.</strong><br>
                        Por políticas de auditoría, el saldo esperado y las transacciones no son visibles para el cajero. Ingrese el dinero en físico de la gaveta.
                    </p>
                </div>
                <?php endif; ?>
            </div>
        </div>

        <!-- Right: Close Form -->
        <div style="width:340px; border-left:1px solid var(--border-color); padding-left:30px;">
            <h4 style="margin-bottom:12px; color:var(--danger);"><i class="fas fa-lock"></i> Arqueo y Cierre de Caja</h4>
            <form id="cash-close-form">
                <input type="hidden" id="cash-close-id-caja">
                <div class="form-group" style="margin-bottom:10px;">
                    <label>Efectivo Contado Real (L)</label>
                    <input type="number" step="0.01" min="0" id="cash-close-monto-real" class="form-input" required placeholder="Ingrese monto contado en físico">
                </div>
                <div class="form-group" style="margin-bottom:10px;">
                    <label>Observaciones / Comentarios</label>
                    <textarea id="cash-close-obs" class="form-input" style="height:60px; font-size:0.8rem; resize:none;" placeholder="Detalles de sobrante/faltante, etc."></textarea>
                </div>
                <button type="submit" class="btn btn-danger" style="width:100%; height:42px; font-weight:700;"><i class="fas fa-lock-open"></i> Realizar Cierre de Caja</button>
            </form>
        </div>
    </div>
</div>

<!-- ========================================================= -->
<!-- SECCIÓN DE APERTURA MANUAL SI NO HAY CAJA ABIERTA -->
<!-- ========================================================= -->
<div id="cash-no-session-container" class="card" style="display:none; text-align:center; padding:40px; border-color:var(--warning); background:#fffdf5; margin-bottom:20px;">
    <i class="fas fa-exclamation-triangle" style="font-size:3rem; color:var(--warning); margin-bottom:15px;"></i>
    <h3>No hay ningún turno de caja abierto actualmente</h3>
    <p style="color:var(--text-muted); font-size:0.9rem; max-width:500px; margin:8px auto 20px auto;">
        Debe realizar la apertura de caja para registrar el dinero en efectivo inicial e iniciar operaciones en el sistema.
    </p>
    <button class="btn btn-primary" onclick="openAperturaModal()"><i class="fas fa-cash-register"></i> Abrir Caja Ahora</button>
</div>

<?php if ($_SESSION['role_id'] != 3): ?>
<!-- ========================================================= -->
<!-- HISTORIAL DE ARQUEOS CERRADOS -->
<!-- ========================================================= -->
<div class="card">
    <h3 style="margin-bottom:15px;"><i class="fas fa-history" style="color:var(--text-muted);"></i> Historial de Arqueos Cerrados</h3>
    <div class="data-table-container">
        <table class="data-table">
            <thead>
                <tr>
                    <th>ID Turno</th>
                    <th>Apertura</th>
                    <th>Cierre</th>
                    <th>Cajero</th>
                    <th style="text-align:right;">Ef. Inicial</th>
                    <th style="text-align:right;">Ef. Esperado</th>
                    <th style="text-align:right;">Ef. Real</th>
                    <th style="text-align:right;">Diferencia</th>
                    <th>Estado</th>
                    <th style="width:110px; text-align:right;">Reporte</th>
                </tr>
            </thead>
            <tbody id="cash-history-tbody">
                <!-- Loaded dynamically -->
            </tbody>
        </table>
    </div>
</div>
<?php endif; ?>

<!-- ========================================================= -->
<!-- MODAL: DETALLES COMPLETOS DE ARQUEO -->
<!-- ========================================================= -->
<div class="modal-backdrop" id="cash-report-modal" style="display:none;">
    <div class="modal-content large">
        <div class="modal-header">
            <span class="modal-title" id="cash-report-title">Resumen de Turno de Caja</span>
            <button class="modal-close" onclick="closeReportModal()">&times;</button>
        </div>
        <div class="modal-body" style="display:grid; grid-template-columns: 320px 1fr; gap:25px; max-height:480px; overflow-y:hidden;">
            <!-- Left Panel: Session stats -->
            <div style="display:flex; flex-direction:column; gap:12px; border-right:1px solid var(--border-color); padding-right:20px; overflow-y:auto;">
                <h4 style="color:var(--primary);"><i class="fas fa-receipt"></i> Datos Generales</h4>
                <div class="checkout-client-box" style="flex-direction:column; align-items:flex-start; margin-bottom:0; font-size:0.85rem; gap:6px;">
                    <div><strong>Apertura por:</strong> <span id="rep-user"></span></div>
                    <div><strong>Fecha Apertura:</strong> <span id="rep-date-open"></span></div>
                    <div><strong>Fecha Cierre:</strong> <span id="rep-date-close"></span></div>
                    <div><strong>Estado:</strong> <span id="rep-status"></span></div>
                </div>

                <h4 style="margin-top:10px; color:var(--accent-blue);"><i class="fas fa-coins"></i> Resumen de Efectivo</h4>
                <div class="checkout-client-box" style="flex-direction:column; align-items:flex-start; margin-bottom:0; font-size:0.85rem; gap:6px;">
                    <div>Efectivo Inicial: <strong id="rep-start"></strong></div>
                    <div>Ef. Esperado en Turno: <strong id="rep-expected"></strong></div>
                    <div>Ef. Real Entregado: <strong id="rep-real"></strong></div>
                    <div>Diferencia (Sobrante/Faltante): <strong id="rep-diff" style="font-size:1.05rem;"></strong></div>
                </div>

                <div class="card" style="margin-top:10px; padding:10px; font-size:0.8rem; background:#f8fafc; border-style:dashed;">
                    <strong>Observaciones:</strong>
                    <p id="rep-obs" style="color:var(--text-muted); margin-top:5px; line-height:1.3; font-style:italic;"></p>
                </div>
            </div>

            <!-- Right Panel: Dynamic lists (Sales & Products Sold) -->
            <div style="display:flex; flex-direction:column; gap:20px; overflow-y:auto; padding-right:10px;">
                <!-- Table sales by payment method -->
                <div>
                    <h4 style="margin-bottom:10px;"><i class="fas fa-credit-card" style="color:var(--primary)"></i> Ingresos por Métodos de Pago</h4>
                    <div style="border:1px solid var(--border-color); border-radius:var(--border-radius-md); overflow:hidden;">
                        <table class="data-table" style="font-size:0.8rem;">
                            <thead>
                                <tr>
                                    <th>Método Pago</th>
                                    <th style="text-align:right;">Ventas Turno</th>
                                    <th style="text-align:right;">Abonos Recibidos</th>
                                    <th style="text-align:right;">Total Ingresos</th>
                                </tr>
                            </thead>
                            <tbody id="rep-methods-tbody">
                                <!-- loaded -->
                            </tbody>
                        </table>
                    </div>
                </div>

                <!-- Table products sold -->
                <div>
                    <h4 style="margin-bottom:10px;"><i class="fas fa-dolly" style="color:var(--accent-blue)"></i> Artículos Vendidos en el Turno (Final del Día)</h4>
                    <div style="border:1px solid var(--border-color); border-radius:var(--border-radius-md); overflow:hidden;">
                        <table class="data-table" style="font-size:0.8rem;">
                            <thead>
                                <tr>
                                    <th>Código Barras</th>
                                    <th>Descripción Producto</th>
                                    <th style="text-align:center; width:80px;">Cant. Vendida</th>
                                    <th style="text-align:right; width:100px;">Total Valor</th>
                                </tr>
                            </thead>
                            <tbody id="rep-products-tbody">
                                <!-- loaded -->
                            </tbody>
                        </table>
                    </div>
                </div>
            </div>
        </div>
        <div class="modal-footer">
            <button class="btn btn-secondary btn-sm" onclick="closeReportModal()">Cerrar</button>
            <button class="btn btn-blue btn-sm" id="rep-print-btn"><i class="fas fa-print"></i> Imprimir Reporte de Caja</button>
        </div>
    </div>
</div>

<script>
// Cash control page logic
(function() {
    let history = [];

    window.initCashControl = function() {
        checkActiveSession();
        if (typeof CURRENT_ROLE_ID !== 'undefined' && CURRENT_ROLE_ID != 3) {
            loadHistory();
        }

        // Close form submit
        document.getElementById('cash-close-form').addEventListener('submit', e => {
            e.preventDefault();
            submitClose();
        });
    };

    function checkActiveSession() {
        fetch('controllers/cash_control.php?action=get_active')
            .then(r => r.json())
            .then(res => {
                const actContainer = document.getElementById('cash-active-session-container');
                const noContainer = document.getElementById('cash-no-session-container');
                
                actContainer.style.display = 'none';
                noContainer.style.display = 'none';

                if (res.success && res.has_active) {
                    const c = res.data;
                    document.getElementById('cash-close-id-caja').value = c.id_caja;
                    document.getElementById('cash-act-user').textContent = c.cajero_turno ? c.cajero_turno : c.nombre_usuario;
                    
                    const date = new Date(c.fecha_apertura).toLocaleDateString('es-HN') + ' ' + new Date(c.fecha_apertura).toLocaleTimeString('es-HN', {hour:'2-digit', minute:'2-digit'});
                    document.getElementById('cash-act-date').textContent = date;
                    document.getElementById('cash-act-start').textContent = 'L ' + parseFloat(c.monto_apertura).toLocaleString('es-HN', {minimumFractionDigits:2});

                    if (document.getElementById('cash-act-sales')) {
                        // Cargar cálculos en tiempo real
                        fetch(`controllers/cash_control.php?action=get_calculations&id=${c.id_caja}`)
                            .then(r => r.json())
                            .then(resC => {
                                if (resC.success) {
                                    const data = resC.data;
                                    document.getElementById('cash-act-sales').textContent = 'L ' + parseFloat(data.total_ventas_general).toFixed(2);
                                    document.getElementById('cash-act-abonos').textContent = 'L ' + parseFloat(data.total_abonos_general).toFixed(2);
                                    document.getElementById('cash-act-expected').textContent = 'L ' + parseFloat(data.efectivo_esperado).toLocaleString('es-HN', {minimumFractionDigits:2});
                                }
                            });
                    }

                    actContainer.style.display = 'block';
                } else {
                    noContainer.style.display = 'block';
                }
            });
    }

    function loadHistory() {
        fetch('controllers/cash_control.php?action=list')
            .then(r => r.json())
            .then(res => {
                if (res.success) {
                    history = res.data;
                    renderHistoryTable();
                }
            });
    }

    function renderHistoryTable() {
        const tbody = document.getElementById('cash-history-tbody');
        tbody.innerHTML = '';

        if (history.length === 0) {
            tbody.innerHTML = `<tr><td colspan="10" class="text-center" style="padding:20px; color:var(--text-light);">No hay historial de cajas cerradas.</td></tr>`;
            return;
        }

        history.forEach(c => {
            const dateOpen = new Date(c.fecha_apertura).toLocaleDateString('es-HN') + ' ' + new Date(c.fecha_apertura).toLocaleTimeString('es-HN', {hour:'2-digit', minute:'2-digit'});
            
            const dateClose = c.fecha_cierre 
                ? new Date(c.fecha_cierre).toLocaleDateString('es-HN') + ' ' + new Date(c.fecha_cierre).toLocaleTimeString('es-HN', {hour:'2-digit', minute:'2-digit'})
                : '-';

            const start = parseFloat(c.monto_apertura);
            const expected = c.monto_cierre_esperado ? parseFloat(c.monto_cierre_esperado) : 0;
            const real = c.monto_cierre_real ? parseFloat(c.monto_cierre_real) : 0;
            const diff = c.diferencia_caja ? parseFloat(c.diferencia_caja) : 0;

            let badge = '';
            if (c.estado_caja === 'ABIERTA') badge = '<span class="badge badge-success">Abierta</span>';
            else badge = '<span class="badge badge-neutral">Cerrada</span>';

            let diffText = '';
            if (c.estado_caja === 'CERRADA') {
                if (diff === 0) diffText = '<span style="color:#27ae60; font-weight:700;">L 0.00</span>';
                else if (diff > 0) diffText = `<span style="color:#27ae60; font-weight:700;">L +${diff.toFixed(2)} (Sobrante)</span>`;
                else diffText = `<span style="color:var(--danger); font-weight:700;">L ${diff.toFixed(2)} (Faltante)</span>`;
            } else {
                diffText = '-';
            }

            tbody.insertAdjacentHTML('beforeend', `
                <tr>
                    <td class="bold">Arqueo #${c.id_caja}</td>
                    <td>${dateOpen}</td>
                    <td>${dateClose}</td>
                    <td>${c.nombre_usuario}</td>
                    <td style="text-align:right;">L ${start.toFixed(2)}</td>
                    <td style="text-align:right;">${c.fecha_cierre ? 'L ' + expected.toFixed(2) : '-'}</td>
                    <td style="text-align:right;">${c.fecha_cierre ? 'L ' + real.toFixed(2) : '-'}</td>
                    <td style="text-align:right;">${diffText}</td>
                    <td>${badge}</td>
                    <td style="text-align:right;">
                        <button class="btn btn-secondary btn-sm" onclick="viewCashReport(${c.id_caja})" style="padding:4px 8px; font-size:0.8rem;"><i class="fas fa-file-invoice"></i> Reporte</button>
                    </td>
                </tr>
            `);
        });
    }

    // ABRIR CAJA DESDE VISTA
    window.openAperturaModal = function() {
        // Ejecutará el helper global inyectado en app.js
        App.showCashOpeningModal(() => {
            checkActiveSession();
            loadHistory();
        });
    };

    // SUBMIT CIERRE
    function submitClose() {
        const id = document.getElementById('cash-close-id-caja').value;
        const real = parseFloat(document.getElementById('cash-close-monto-real').value);
        const obs = document.getElementById('cash-close-obs').value;

        App.confirm('¿Confirma que desea cerrar el turno de caja actual y finalizar el arqueo del día?', () => {
            const fd = new FormData();
            fd.append('id_caja', id);
            fd.append('monto_cierre_real', real);
            fd.append('observaciones', obs);

            fetch('controllers/cash_control.php?action=close', {
                method: 'POST',
                body: fd
            })
            .then(r => r.json())
            .then(res => {
                if (res.success) {
                    App.showToast(res.message);
                    
                    // Imprimir reporte de cierre
                    printCashTicket(id);

                    checkActiveSession();
                    loadHistory();
                } else {
                    alert(res.message);
                }
            });
        });
    }

    // REPORT DETAILED MODAL
    window.viewCashReport = function(idCaja) {
        fetch(`controllers/cash_control.php?action=get_calculations&id=${idCaja}`)
            .then(r => r.json())
            .then(res => {
                if (res.success) {
                    const data = res.data;
                    const c = data.caja;

                    document.getElementById('cash-report-title').textContent = `Reporte de Cierre de Caja (Arqueo #${c.id_caja})`;
                    document.getElementById('rep-user').textContent = c.nombre_usuario || 'Cajero';
                    
                    const dateO = new Date(c.fecha_apertura).toLocaleDateString('es-HN') + ' ' + new Date(c.fecha_apertura).toLocaleTimeString('es-HN', {hour:'2-digit', minute:'2-digit'});
                    document.getElementById('rep-date-open').textContent = dateO;
                    
                    const dateC = c.fecha_cierre 
                        ? new Date(c.fecha_cierre).toLocaleDateString('es-HN') + ' ' + new Date(c.fecha_cierre).toLocaleTimeString('es-HN', {hour:'2-digit', minute:'2-digit'})
                        : 'Turno Abierto';
                    document.getElementById('rep-date-close').textContent = dateC;

                    document.getElementById('rep-status').innerHTML = c.estado_caja === 'ABIERTA' 
                        ? '<span class="badge badge-success">Abierta</span>' 
                        : '<span class="badge badge-neutral">Cerrada</span>';

                    document.getElementById('rep-start').textContent = 'L ' + parseFloat(c.monto_apertura).toFixed(2);
                    document.getElementById('rep-expected').textContent = 'L ' + parseFloat(data.efectivo_esperado).toFixed(2);
                    
                    if (c.estado_caja === 'CERRADA') {
                        document.getElementById('rep-real').textContent = 'L ' + parseFloat(c.monto_cierre_real).toFixed(2);
                        const diffVal = parseFloat(c.diferencia_caja);
                        const diffEl = document.getElementById('rep-diff');
                        
                        if (diffVal === 0) {
                            diffEl.style.color = '#27ae60';
                            diffEl.textContent = 'L 0.00 (Cuadrada)';
                        } else if (diffVal > 0) {
                            diffEl.style.color = '#27ae60';
                            diffEl.textContent = `L +${diffVal.toFixed(2)} (Sobrante)`;
                        } else {
                            diffEl.style.color = 'var(--danger)';
                            diffEl.textContent = `L ${diffVal.toFixed(2)} (Faltante)`;
                        }
                    } else {
                        document.getElementById('rep-real').textContent = '-';
                        document.getElementById('rep-diff').textContent = '-';
                        document.getElementById('rep-diff').style.color = 'var(--text-main)';
                    }

                    document.getElementById('rep-obs').textContent = c.observaciones ? c.observaciones : 'Sin observaciones.';

                    // Renderizar tabla ingresos por métodos
                    const mBody = document.getElementById('rep-methods-tbody');
                    mBody.innerHTML = '';
                    data.metodos.forEach(m => {
                        mBody.insertAdjacentHTML('beforeend', `
                            <tr>
                                <td class="bold">${m.nombre_metodo}</td>
                                <td style="text-align:right;">L ${m.total_ventas.toFixed(2)} <span style="font-size:0.75rem; color:var(--text-muted)">(${m.ventas_count})</span></td>
                                <td style="text-align:right;">L ${m.total_abonos.toFixed(2)} <span style="font-size:0.75rem; color:var(--text-muted)">(${m.abonos_count})</span></td>
                                <td style="text-align:right;" class="bold">L ${m.total_general.toFixed(2)}</td>
                            </tr>
                        `);
                    });

                    // Renderizar tabla productos vendidos
                    const pBody = document.getElementById('rep-products-tbody');
                    pBody.innerHTML = '';
                    if (data.productos_vendidos.length === 0) {
                        pBody.innerHTML = `<tr><td colspan="4" class="text-center" style="color:var(--text-light); padding:15px;">Ningún producto vendido en este turno.</td></tr>`;
                    } else {
                        data.productos_vendidos.forEach(p => {
                            pBody.insertAdjacentHTML('beforeend', `
                                <tr>
                                    <td><code>${p.codigo_barras_producto || 'N/A'}</td>
                                    <td class="bold">${p.descripcion_venta}</td>
                                    <td class="text-center bold" style="color:var(--primary);">${p.cantidad_vendida}</td>
                                    <td style="text-align:right;" class="bold">L ${parseFloat(p.total_valor).toFixed(2)}</td>
                                </tr>
                            `);
                        });
                    }

                    // Configurar reimpresión
                    document.getElementById('rep-print-btn').onclick = () => printCashTicket(c.id_caja);

                    document.getElementById('cash-report-modal').style.display = 'flex';
                } else {
                    alert(res.message);
                }
            });
    };

    window.closeReportModal = function() {
        document.getElementById('cash-report-modal').style.display = 'none';
    };

    window.printCashTicket = function(idCaja) {
        const ticketUrl = `views/ticket_caja.php?id_caja=${idCaja}`;
        window.open(ticketUrl, '_blank', 'width=350,height=600');
    };
})();
</script>
