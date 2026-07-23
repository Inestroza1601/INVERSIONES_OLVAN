<?php
session_start();
if (!isset($_SESSION['user_id'])) {
    header('Location: login.php');
    exit;
}
require_once __DIR__ . '/models/Company.php';
$companyModel = new Company();
$company = $companyModel->getInfo();
$companyName = $company ? htmlspecialchars($company['nombre_empresa']) : 'Orion Systems';
$companyLogo = ($company && !empty($company['logo_empresa_ruta'])) ? $company['logo_empresa_ruta'] : 'assets/image/logo.png';
?>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title><?php echo $companyName; ?></title>
    <!-- Stylesheets -->
    <link rel="stylesheet" href="assets/css/style.css">
    <!-- FontAwesome icons -->
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
</head>
<body>
    <div class="app-container">
        <!-- Sidebar Navigation -->
        <aside class="sidebar">
            <div class="sidebar-header">
                <img src="<?php echo $companyLogo; ?>" onerror="this.src='https://cdn-icons-png.flaticon.com/512/869/869045.png'" class="sidebar-logo" alt="Logo">
                <div class="sidebar-brand">
                    <?php echo $companyName; ?>
                    <span>Conexión a tu Alcance</span>
                </div>
            </div>
            
            <ul class="sidebar-menu">
                <?php if ($_SESSION['role_id'] != 3): ?>
                <li class="menu-item active">
                    <a href="#" data-view="dashboard">
                        <i class="fas fa-chart-pie"></i>
                        <span>Dashboard</span>
                    </a>
                </li>
                <?php endif; ?>
                <li class="menu-item <?php echo ($_SESSION['role_id'] == 3) ? 'active' : ''; ?>">
                    <a href="#" data-view="pos">
                        <i class="fas fa-shopping-cart"></i>
                        <span>Punto de Venta</span>
                    </a>
                </li>
                <li class="menu-item">
                    <a href="#" data-view="sales_history">
                        <i class="fas fa-history"></i>
                        <span>Historial de Ventas</span>
                    </a>
                </li>
                <li class="menu-item">
                    <a href="#" data-view="layaways">
                        <i class="fas fa-hand-holding-usd"></i>
                        <span>Apartados (Abonos)</span>
                    </a>
                </li>
                <li class="menu-item">
                    <a href="#" data-view="cash_control">
                        <i class="fas fa-cash-register"></i>
                        <span>Control de Caja</span>
                    </a>
                </li>
                <li class="menu-item">
                    <a href="#" data-view="inventory">
                        <i class="fas fa-boxes"></i>
                        <span>Inventario</span>
                    </a>
                </li>
                <li class="menu-item">
                    <a href="#" data-view="clients">
                        <i class="fas fa-users"></i>
                        <span>Clientes</span>
                    </a>
                </li>
                <li class="menu-item">
                    <a href="#" data-view="warranties">
                        <i class="fas fa-shield-alt"></i>
                        <span>Garantías</span>
                    </a>
                </li>
                <?php if ($_SESSION['role_id'] != 3): ?>
                <li class="menu-item">
                    <a href="#" data-view="stats">
                        <i class="fas fa-chart-line"></i>
                        <span>Estadísticas</span>
                    </a>
                </li>
                <li class="menu-item">
                    <a href="#" data-view="users">
                        <i class="fas fa-user-cog"></i>
                        <span>Usuarios</span>
                    </a>
                </li>
                <li class="menu-item">
                    <a href="#" data-view="settings">
                        <i class="fas fa-cogs"></i>
                        <span>Configuración</span>
                    </a>
                </li>
                <?php endif; ?>
            </ul>

            <div class="sidebar-footer">
                <div class="user-info">
                    <span class="user-name"><?php echo htmlspecialchars($_SESSION['username']); ?></span>
                    <span class="user-role"><?php echo htmlspecialchars($_SESSION['role_name']); ?></span>
                </div>
                <button class="btn-logout" id="logout-btn" title="Cerrar Sesión">
                    <i class="fas fa-sign-out-alt"></i>
                </button>
            </div>
        </aside>

        <!-- Main Wrapper -->
        <main class="main-wrapper">
            <header class="main-header">
                <div class="header-title">
                    <h1 id="header-page-title"><?php echo ($_SESSION['role_id'] == 3) ? 'Punto de Venta' : 'Dashboard'; ?></h1>
                </div>
                <div class="header-actions">
                    <div class="datetime-display" id="datetime-display">
                        Cargando fecha y hora...
                    </div>
                </div>
            </header>

            <!-- Dynamic View Content -->
            <div class="content-area" id="content-area">
                <!-- AJAX view content gets injected here -->
            </div>
        </main>
    </div>

    <!-- Global Role and User info for JavaScript -->
    <script>
        const CURRENT_ROLE_ID = <?php echo $_SESSION['role_id']; ?>;
        const CURRENT_USERNAME = <?php echo json_encode($_SESSION['username']); ?>;
    </script>
    <!-- Application Script -->
    <script src="assets/js/app.js"></script>
</body>
</html>
