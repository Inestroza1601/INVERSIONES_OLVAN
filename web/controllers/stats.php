<?php
session_start();
header('Content-Type: application/json; charset=utf-8');
require_once dirname(__DIR__) . '/models/Stats.php';

if (!isset($_SESSION['user_id'])) {
    echo json_encode(['success' => false, 'message' => 'Sesión no iniciada.']);
    exit;
}

$action = $_GET['action'] ?? $_POST['action'] ?? '';
$statsModel = new Stats();

switch ($action) {
    case 'get_metrics':
        $filtro = $_GET['filtro'] ?? 'Mes';
        $metrics = $statsModel->getMetrics($filtro);
        echo json_encode(['success' => true, 'metrics' => $metrics]);
        break;

    case 'get_chart':
        $chartData = $statsModel->getChartData();
        echo json_encode(['success' => true, 'data' => $chartData]);
        break;

    default:
        echo json_encode(['success' => false, 'message' => 'Acción no soportada.']);
        break;
}
