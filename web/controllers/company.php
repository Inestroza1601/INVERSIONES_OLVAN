<?php
session_start();
header('Content-Type: application/json; charset=utf-8');
require_once dirname(__DIR__) . '/models/Company.php';

if (!isset($_SESSION['user_id'])) {
    echo json_encode(['success' => false, 'message' => 'Sesión no iniciada.']);
    exit;
}

$action = $_GET['action'] ?? $_POST['action'] ?? '';
$companyModel = new Company();

switch ($action) {
    case 'get':
        $info = $companyModel->getInfo();
        if ($info) {
            echo json_encode(['success' => true, 'data' => $info]);
        } else {
            echo json_encode(['success' => false, 'message' => 'No se encontró la configuración de la empresa.']);
        }
        break;

    case 'save':
        // Guardar configuración
        $res = $companyModel->save($_POST);
        echo json_encode([
            'success' => $res, 
            'message' => $res ? 'Configuración de la empresa guardada correctamente.' : 'Error al guardar la configuración.'
        ]);
        break;

    default:
        echo json_encode(['success' => false, 'message' => 'Acción no soportada.']);
        break;
}
