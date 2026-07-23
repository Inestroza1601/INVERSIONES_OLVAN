<?php
session_start();
header('Content-Type: application/json; charset=utf-8');
require_once dirname(__DIR__) . '/models/CashControl.php';

if (!isset($_SESSION['user_id'])) {
    echo json_encode(['success' => false, 'message' => 'Sesión no iniciada.']);
    exit;
}

$action = $_GET['action'] ?? $_POST['action'] ?? '';
$cashModel = new CashControl();

switch ($action) {
    case 'get_active':
        $active = $cashModel->getActiveSession();
        if ($active) {
            echo json_encode(['success' => true, 'has_active' => true, 'data' => $active]);
        } else {
            echo json_encode(['success' => true, 'has_active' => false]);
        }
        break;

    case 'open':
        $monto = (float)($_POST['monto_apertura'] ?? 0.0);
        $cajero = $_POST['cajero_turno'] ?? '';
        if ($monto < 0) {
            echo json_encode(['success' => false, 'message' => 'El monto inicial no puede ser negativo.']);
            exit;
        }

        $res = $cashModel->openSession($_SESSION['user_id'], $monto, $cajero);
        echo json_encode([
            'success' => $res,
            'message' => $res ? 'Turno de caja abierto correctamente.' : 'Ya existe un turno de caja abierto o hubo un error.'
        ]);
        break;

    case 'get_calculations':
        $id = (int)($_GET['id'] ?? 0);
        if ($id <= 0) {
            // Si no se especifica ID, intentar buscar la caja abierta actual
            $active = $cashModel->getActiveSession();
            if ($active) {
                $id = (int)$active['id_caja'];
            } else {
                echo json_encode(['success' => false, 'message' => 'No hay caja abierta activa ni se especificó un ID.']);
                exit;
            }
        }

        $calcs = $cashModel->getSessionCalculations($id);
        if ($calcs) {
            echo json_encode(['success' => true, 'data' => $calcs]);
        } else {
            echo json_encode(['success' => false, 'message' => 'No se pudieron calcular los datos del arqueo.']);
        }
        break;

    case 'close':
        $idCaja = (int)($_POST['id_caja'] ?? 0);
        $montoReal = (float)($_POST['monto_cierre_real'] ?? 0.0);
        $obs = $_POST['observaciones'] ?? '';

        if ($idCaja <= 0 || $montoReal < 0) {
            echo json_encode(['success' => false, 'message' => 'Parámetros de arqueo no válidos.']);
            exit;
        }

        $res = $cashModel->closeSession($idCaja, $montoReal, $obs);
        echo json_encode([
            'success' => $res,
            'message' => $res ? 'Turno de caja cerrado correctamente.' : 'Error al cerrar el turno de caja. Verifique si ya fue cerrada.'
        ]);
        break;

    case 'list':
        $cajas = $cashModel->listAll();
        echo json_encode(['success' => true, 'data' => $cajas]);
        break;

    case 'low_stock':
        $products = $cashModel->getLowStockProducts();
        echo json_encode(['success' => true, 'data' => $products]);
        break;

    default:
        echo json_encode(['success' => false, 'message' => 'Acción no soportada.']);
        break;
}
