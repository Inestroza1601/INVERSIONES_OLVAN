<?php
session_start();
if (!isset($_SESSION['user_id'])) exit;
?>
<!-- Load Chart.js CDN for stunning interactive reports -->
<script src="https://cdn.jsdelivr.net/npm/chart.js"></script>

<div class="card" style="margin-bottom: 20px;">
    <div style="display:flex; justify-content:space-between; align-items:center; flex-wrap:wrap; gap:15px;">
        <div>
            <h2><i class="fas fa-chart-line" style="color:var(--primary);"></i> Estadísticas y Reportes Analíticos</h2>
            <p style="color:var(--text-muted); font-size:0.85rem;">Analice el rendimiento de ventas e ingresos del sistema.</p>
        </div>
        <div style="display:flex; gap:10px; align-items:center;">
            <span style="font-size:0.85rem; font-weight:600; color:var(--text-muted);">Período:</span>
            <select id="stats-period-filter" class="form-select" style="width:140px; height:40px; padding:0 10px;" onchange="loadStatsMetrics()">
                <option value="Día">Hoy (Día)</option>
                <option value="Semana">Semana</option>
                <option value="Mes" selected>Mes Actual</option>
                <option value="Año">Año Actual</option>
            </select>
        </div>
    </div>
</div>

<!-- Metrics Summary Cards -->
<div class="metrics-grid">
    <div class="metric-card sales">
        <div class="metric-info">
            <span class="metric-title">Ingresos del Período</span>
            <span class="metric-value" id="stats-sales-value">L 0.00</span>
            <span class="metric-trend" id="stats-sales-trend">Cargando...</span>
        </div>
        <div class="metric-icon">
            <i class="fas fa-coins"></i>
        </div>
    </div>

    <div class="metric-card ticket">
        <div class="metric-info">
            <span class="metric-title">Ticket Promedio</span>
            <span class="metric-value" id="stats-ticket-value">L 0.00</span>
            <span class="metric-trend" style="color: var(--text-muted)">Por Venta</span>
        </div>
        <div class="metric-icon">
            <i class="fas fa-file-invoice-dollar"></i>
        </div>
    </div>

    <div class="metric-card product">
        <div class="metric-info">
            <span class="metric-title">Producto Estrella</span>
            <span class="metric-value" id="stats-product-value" style="font-size:1.1rem; white-space: nowrap; text-overflow: ellipsis; overflow: hidden; max-width:180px;">Sin registros</span>
            <span class="metric-trend" style="color: var(--text-muted)">Más Vendido</span>
        </div>
        <div class="metric-icon">
            <i class="fas fa-trophy"></i>
        </div>
    </div>
</div>

<!-- Chart Graphics -->
<div style="display:grid; grid-template-columns: 1fr 1fr; gap:20px; margin-top:20px; flex-wrap:wrap;">
    <div class="card">
        <h3 style="margin-bottom:15px;"><i class="fas fa-calendar-day" style="color:var(--accent-blue)"></i> Ventas Diarias (Últimos 7 días)</h3>
        <div style="height: 250px; position: relative;">
            <canvas id="chart-daily-sales"></canvas>
        </div>
    </div>
    <div class="card">
        <h3 style="margin-bottom:15px;"><i class="fas fa-calendar-alt" style="color:var(--primary)"></i> Ventas Mensuales (Año Actual)</h3>
        <div style="height: 250px; position: relative;">
            <canvas id="chart-monthly-sales"></canvas>
        </div>
    </div>
</div>

<script>
// Stats views logic
(function() {
    let dailyChart = null;
    let monthlyChart = null;

    window.initStats = function() {
        loadStatsMetrics();
        loadCharts();
    };

    window.loadStatsMetrics = function() {
        const period = document.getElementById('stats-period-filter').value;
        fetch(`controllers/stats.php?action=get_metrics&filtro=${period}`)
            .then(r => r.json())
            .then(res => {
                if (res.success) {
                    const metrics = res.metrics;
                    document.getElementById('stats-sales-value').textContent = 'L ' + parseFloat(metrics.totalActual).toLocaleString('es-HN', {minimumFractionDigits:2, maximumFractionDigits:2});
                    document.getElementById('stats-ticket-value').textContent = 'L ' + parseFloat(metrics.ticketProm).toLocaleString('es-HN', {minimumFractionDigits:2, maximumFractionDigits:2});
                    document.getElementById('stats-product-value').textContent = metrics.topProducto;

                    // Calcular tendencia
                    const trendEl = document.getElementById('stats-sales-trend');
                    if (metrics.totalAnterior > 0) {
                        const diff = ((metrics.totalActual - metrics.totalAnterior) / metrics.totalAnterior) * 100;
                        const sign = diff >= 0 ? '+' : '';
                        trendEl.className = `metric-trend ${diff >= 0 ? 'trend-up' : 'trend-down'}`;
                        trendEl.innerHTML = `<i class="fas fa-arrow-${diff >= 0 ? 'up' : 'down'}"></i> ${sign}${diff.toFixed(1)}% vs período ant. (${parseFloat(metrics.totalAnterior).toLocaleString('es-HN', {maximumFractionDigits:0})})`;
                    } else {
                        trendEl.className = 'metric-trend';
                        trendEl.style.color = 'var(--text-muted)';
                        trendEl.textContent = 'Sin historial anterior';
                    }
                }
            });
    };

    function loadCharts() {
        fetch('controllers/stats.php?action=get_chart')
            .then(r => r.json())
            .then(res => {
                if (res.success) {
                    renderDailyChart(res.data.days);
                    renderMonthlyChart(res.data.months);
                }
            });
    }

    function renderDailyChart(data) {
        const ctx = document.getElementById('chart-daily-sales').getContext('2d');
        
        const labels = data.map(d => {
            const date = new Date(d.fecha);
            return date.toLocaleDateString('es-HN', { weekday: 'short', day: 'numeric' });
        });
        const values = data.map(d => parseFloat(d.total));

        if (dailyChart) dailyChart.destroy();

        dailyChart = new Chart(ctx, {
            type: 'line',
            data: {
                labels: labels,
                datasets: [{
                    label: 'Ventas (L)',
                    data: values,
                    borderColor: '#0d6efd',
                    backgroundColor: 'rgba(13, 110, 253, 0.05)',
                    borderWidth: 3,
                    fill: true,
                    tension: 0.3
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: { legend: { display: false } },
                scales: {
                    y: { beginAtZero: true, grid: { borderDash: [5, 5] } },
                    x: { grid: { display: false } }
                }
            }
        });
    }

    function renderMonthlyChart(data) {
        const ctx = document.getElementById('chart-monthly-sales').getContext('2d');
        
        const monthNames = ["Ene", "Feb", "Mar", "Abr", "May", "Jun", "Jul", "Ago", "Sep", "Oct", "Nov", "Dic"];
        const labels = data.map(d => monthNames[d.mes - 1]);
        const values = data.map(d => parseFloat(d.total));

        if (monthlyChart) monthlyChart.destroy();

        monthlyChart = new Chart(ctx, {
            type: 'bar',
            data: {
                labels: labels,
                datasets: [{
                    label: 'Ingresos (L)',
                    data: values,
                    backgroundColor: '#27ae60',
                    borderRadius: 6
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: { legend: { display: false } },
                scales: {
                    y: { beginAtZero: true, grid: { borderDash: [5, 5] } },
                    x: { grid: { display: false } }
                }
            }
        });
    }
})();
</script>
