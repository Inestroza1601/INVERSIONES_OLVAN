<?php
session_start();
header('Content-Type: application/json; charset=utf-8');
require_once dirname(__DIR__) . '/models/Warranty.php';

if (!isset($_SESSION['user_id'])) {
    echo json_encode(['success' => false, 'message' => 'Sesión no iniciada.']);
    exit;
}

$action = $_GET['action'] ?? $_POST['action'] ?? '';
$warrantyModel = new Warranty();

switch ($action) {
    case 'list':
        $warranties = $warrantyModel->listAll();
        echo json_encode(['success' => true, 'data' => $warranties]);
        break;

    case 'claim':
        $idDetalleVenta = (int)($_POST['id_detalle_venta'] ?? 0);
        if ($idDetalleVenta <= 0) {
            echo json_encode(['success' => false, 'message' => 'ID de detalle de venta no válido.']);
            exit;
        }

        $res = $warrantyModel->applyClaim($idDetalleVenta);
        echo json_encode([
            'success' => $res, 
            'message' => $res ? 'Garantía reclamada y guardada como RECLAMADA.' : 'Error al registrar reclamo de garantía.'
        ]);
        break;

    default:
        echo json_encode(['success' => false, 'message' => 'Acción no soportada.']);
        break;
}
