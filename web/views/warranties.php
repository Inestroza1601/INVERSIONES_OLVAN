<?php
session_start();
if (!isset($_SESSION['user_id'])) exit;
?>
<div class="card" style="margin-bottom: 20px;">
    <div>
        <h2><i class="fas fa-shield-alt" style="color:var(--primary);"></i> Control de Garantías</h2>
        <p style="color:var(--text-muted); font-size:0.85rem;">Historial de garantías registradas y reclamadas de artículos vendidos.</p>
    </div>
</div>

<div class="card">
    <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom: 20px; flex-wrap:wrap; gap:15px;">
        <input type="text" id="warr-search" class="pos-input" placeholder="Buscar por cliente, producto o serie..." style="width:300px;" oninput="renderWarrantiesTable()">
        <div style="display:flex; gap:10px; align-items:center;">
            <span style="font-size:0.85rem; font-weight:600; color:var(--text-muted);">Filtrar Estado:</span>
            <select id="warr-filter-status" class="form-select" style="width:150px; height:40px; padding:0 10px;" onchange="renderWarrantiesTable()">
                <option value="TODAS">Todas</option>
                <option value="VIGENTE">Vigentes</option>
                <option value="VENCIDA">Vencidas</option>
                <option value="RECLAMADA">Reclamadas</option>
            </select>
        </div>
    </div>

    <div class="data-table-container">
        <table class="data-table">
            <thead>
                <tr>
                    <th>Código Venta</th>
                    <th>Cliente</th>
                    <th>Producto</th>
                    <th>Nº Serie / IMEI</th>
                    <th>Fecha Venta</th>
                    <th>Vence</th>
                    <th>Estado</th>
                    <th style="width:130px; text-align:right;">Acción</th>
                </tr>
            </thead>
            <tbody id="warranties-tbody">
                <!-- Loaded dynamically -->
            </tbody>
        </table>
    </div>
</div>

<script>
// Warranties listing and claiming logic
(function() {
    let warranties = [];

    window.initWarranties = function() {
        loadWarranties();
    };

    function loadWarranties() {
        fetch('controllers/warranties.php?action=list')
            .then(r => r.json())
            .then(res => {
                if (res.success) {
                    warranties = res.data;
                    renderWarrantiesTable();
                }
            });
    }

    window.renderWarrantiesTable = function() {
        const query = document.getElementById('warr-search').value.toLowerCase();
        const statusFilter = document.getElementById('warr-filter-status').value;
        const tbody = document.getElementById('warranties-tbody');
        tbody.innerHTML = '';

        const filtered = warranties.filter(w => {
            // Filtro por texto
            const matchesText = w.cliente.toLowerCase().includes(query) || 
                                w.producto.toLowerCase().includes(query) || 
                                w.serie.toLowerCase().includes(query) ||
                                ("venta #" + w.id_venta).toLowerCase().includes(query);
            
            // Filtro por estado
            const matchesStatus = (statusFilter === 'TODAS') || (w.estado === statusFilter);

            return matchesText && matchesStatus;
        });

        if (filtered.length === 0) {
            tbody.innerHTML = `<tr><td colspan="8" class="text-center" style="padding:20px; color:var(--text-light);">No se encontraron garantías registradas.</td></tr>`;
            return;
        }

        filtered.forEach(w => {
            let statusBadge = '';
            let claimButton = '';

            if (w.estado === 'VIGENTE') {
                statusBadge = `<span class="badge badge-success">Vigente</span>`;
                claimButton = `<button class="btn btn-primary btn-sm" onclick="claimWarranty(${w.id_detalle_venta}, '${w.producto}', '${w.cliente}')"><i class="fas fa-undo"></i> Reclamar</button>`;
            } else if (w.estado === 'VENCIDA') {
                statusBadge = `<span class="badge badge-danger">Vencida</span>`;
                claimButton = `<span style="font-size:0.8rem; color:var(--text-light); font-weight:500;">Expirada</span>`;
            } else if (w.estado === 'RECLAMADA') {
                statusBadge = `<span class="badge badge-neutral">Reclamada</span>`;
                claimButton = `<span style="font-size:0.8rem; color:var(--primary); font-weight:600;"><i class="fas fa-check"></i> Reclamada</span>`;
            }

            tbody.insertAdjacentHTML('beforeend', `
                <tr>
                    <td class="bold">Venta #${w.id_venta}</td>
                    <td>${w.cliente}</td>
                    <td>
                        <div style="font-weight:600;">${w.producto}</div>
                        <small style="color:var(--text-muted);">${w.dias_garantia} días de garantía</small>
                    </td>
                    <td><code>${w.serie}</code></td>
                    <td>${w.fecha_venta}</td>
                    <td class="bold">${w.fecha_vencimiento}</td>
                    <td>${statusBadge}</td>
                    <td style="text-align:right;">
                        ${claimButton}
                    </td>
                </tr>
            `);
        });
    };

    window.claimWarranty = function(idDetalleVenta, product, client) {
        App.confirm(`¿Seguro que desea aplicar el reclamo de garantía para el producto '${product}' vendido a '${client}'?`, () => {
            const fd = new FormData();
            fd.append('id_detalle_venta', idDetalleVenta);

            fetch('controllers/warranties.php?action=claim', {
                method: 'POST',
                body: fd
            })
            .then(r => r.json())
            .then(res => {
                if (res.success) {
                    App.showToast(res.message);
                    loadWarranties();
                } else {
                    alert(res.message);
                }
            });
        });
    };
})();
</script>
