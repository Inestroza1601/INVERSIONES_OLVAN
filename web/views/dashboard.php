<?php
session_start();
if (!isset($_SESSION['user_id'])) exit;
?>
<div class="welcome-box" style="margin-bottom: 24px;">
    <h2>¡Hola, <?php echo htmlspecialchars($_SESSION['username']); ?>!</h2>
    <p style="color:var(--text-muted); font-size: 0.9rem;">Bienvenido de nuevo al panel administrativo de Orion Systems.</p>
</div>

<!-- Metrics Summary -->
<div class="metrics-grid">
    <div class="metric-card sales">
        <div class="metric-info">
            <span class="metric-title">Ventas del Mes</span>
            <span class="metric-value" id="dash-sales-value">L 0.00</span>
            <span class="metric-trend" id="dash-sales-trend">
                <!-- Trend indicator loaded dynamically -->
            </span>
        </div>
        <div class="metric-icon">
            <i class="fas fa-wallet"></i>
        </div>
    </div>
    
    <div class="metric-card ticket">
        <div class="metric-info">
            <span class="metric-title">Ticket Promedio</span>
            <span class="metric-value" id="dash-ticket-value">L 0.00</span>
            <span class="metric-trend" style="color: var(--text-muted)">Por Transacción</span>
        </div>
        <div class="metric-icon">
            <i class="fas fa-receipt"></i>
        </div>
    </div>

    <div class="metric-card product">
        <div class="metric-info">
            <span class="metric-title">Top Vendido (Mes)</span>
            <span class="metric-value" id="dash-product-value" style="font-size:1.15rem; white-space: nowrap; text-overflow: ellipsis; overflow: hidden; max-width: 180px;">Sin registros</span>
            <span class="metric-trend" style="color: var(--text-muted)">Producto Estrella</span>
        </div>
        <div class="metric-icon">
            <i class="fas fa-crown"></i>
        </div>
    </div>
</div>

<!-- Quick Navigation Links -->
<div class="card" style="margin-top: 20px;">
    <h3 style="margin-bottom: 15px;"><i class="fas fa-bolt" style="color:var(--primary)"></i> Accesos Rápidos</h3>
    <div style="display:grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: 15px;">
        <button class="btn btn-blue" onclick="App.loadView('pos')" style="height: 60px; font-size: 1rem;">
            <i class="fas fa-shopping-cart"></i> Abrir Punto de Venta
        </button>
        <button class="btn btn-primary" onclick="App.loadView('inventory')" style="height: 60px; font-size: 1rem;">
            <i class="fas fa-boxes"></i> Gestionar Inventario
        </button>
        <button class="btn btn-secondary" onclick="App.loadView('clients')" style="height: 60px; font-size: 1rem;">
            <i class="fas fa-users"></i> Control de Clientes
        </button>
        <button class="btn btn-secondary" onclick="App.loadView('warranties')" style="height: 60px; font-size: 1rem;">
            <i class="fas fa-shield-alt"></i> Ver Garantías
        </button>
    </div>
</div>

<!-- Alerta de Bajo Stock -->
<div class="card" style="margin-top: 20px; border-color: var(--danger); background: #fffcfc;">
    <h3 style="margin-bottom: 15px; color: var(--danger); display: flex; align-items: center; gap: 8px;">
        <i class="fas fa-exclamation-triangle" style="animation: pulse 1.5s infinite;"></i> Alertas de Bajo Stock
    </h3>
    <div class="data-table-container">
        <table class="data-table" style="font-size: 0.85rem;">
            <thead>
                <tr>
                    <th>Código</th>
                    <th>Producto</th>
                    <th>Ubicación</th>
                    <th style="text-align:center;">Stock Mínimo</th>
                    <th style="text-align:center; color: var(--danger);">Stock Actual</th>
                    <th style="text-align:center;">Estado</th>
                </tr>
            </thead>
            <tbody id="dash-low-stock-tbody">
                <tr><td colspan="6" class="text-center" style="padding:15px; color:var(--text-light);">Cargando alertas...</td></tr>
            </tbody>
        </table>
    </div>
</div>

<style>
@keyframes pulse {
    0% { transform: scale(1); opacity: 1; }
    50% { transform: scale(1.1); opacity: 0.7; }
    100% { transform: scale(1); opacity: 1; }
}
</style>

<script>
window.initDashboard = function() {
    // Cargar métricas por defecto del "Mes"
    fetch('controllers/stats.php?action=get_metrics&filtro=Mes')
        .then(r => r.json())
        .then(res => {
            if (res.success) {
                const metrics = res.metrics;
                document.getElementById('dash-sales-value').textContent = 'L ' + parseFloat(metrics.totalActual).toLocaleString('es-HN', {minimumFractionDigits: 2, maximumFractionDigits: 2});
                document.getElementById('dash-ticket-value').textContent = 'L ' + parseFloat(metrics.ticketProm).toLocaleString('es-HN', {minimumFractionDigits: 2, maximumFractionDigits: 2});
                document.getElementById('dash-product-value').textContent = metrics.topProducto;

                // Calcular tendencia
                const trendEl = document.getElementById('dash-sales-trend');
                if (metrics.totalAnterior > 0) {
                    const diff = ((metrics.totalActual - metrics.totalAnterior) / metrics.totalAnterior) * 100;
                    const sign = diff >= 0 ? '+' : '';
                    trendEl.className = `metric-trend ${diff >= 0 ? 'trend-up' : 'trend-down'}`;
                    trendEl.innerHTML = `<i class="fas fa-arrow-${diff >= 0 ? 'up' : 'down'}"></i> ${sign}${diff.toFixed(1)}% vs mes ant.`;
                } else {
                    trendEl.className = 'metric-trend';
                    trendEl.style.color = 'var(--text-muted)';
                    trendEl.textContent = 'Sin historial anterior';
                }
            }
        })
        .catch(err => {
            console.error('Error al cargar métricas de dashboard:', err);
        });

    // Cargar alertas de bajo stock
    fetch('controllers/cash_control.php?action=low_stock')
        .then(r => r.json())
        .then(res => {
            if (res.success) {
                const tbody = document.getElementById('dash-low-stock-tbody');
                tbody.innerHTML = '';

                if (res.data.length === 0) {
                    tbody.innerHTML = `
                        <tr>
                            <td colspan="6" class="text-center" style="color:#27ae60; padding:15px; font-weight:600;">
                                <i class="fas fa-check-circle"></i> Todos los productos tienen stock suficiente.
                            </td>
                        </tr>
                    `;
                    return;
                }

                res.data.forEach(p => {
                    tbody.insertAdjacentHTML('beforeend', `
                        <tr>
                            <td><code>${p.codigo_barras_producto || 'N/A'}</code></td>
                            <td class="bold">${p.nombre_producto}</td>
                            <td>${p.nombre_ubicacion || 'General'}</td>
                            <td class="text-center bold">${p.stock_minimo_producto}</td>
                            <td class="text-center bold" style="color:var(--danger); font-size:1rem;">${p.stock_producto}</td>
                            <td class="text-center">
                                <span class="badge badge-danger">
                                    ${p.stock_producto === 0 ? 'AGOTADO' : 'STOCK BAJO'}
                                </span>
                            </td>
                        </tr>
                    `);
                });
            }
        })
        .catch(err => {
            console.error('Error al cargar alertas de stock:', err);
        });
};
</script>
