<?php
session_start();
header('Content-Type: application/json; charset=utf-8');
require_once dirname(__DIR__) . '/models/Client.php';

if (!isset($_SESSION['user_id'])) {
    echo json_encode(['success' => false, 'message' => 'Sesión no iniciada.']);
    exit;
}

$action = $_GET['action'] ?? $_POST['action'] ?? '';
$clientModel = new Client();

switch ($action) {
    case 'list':
        $onlyActive = isset($_GET['active']) && $_GET['active'] === 'true';
        $clients = $onlyActive ? $clientModel->listActive() : $clientModel->listAll();
        echo json_encode(['success' => true, 'data' => $clients]);
        break;

    case 'save':
        $id = $_POST['id_cliente'] ?? 0;
        $identidad = $_POST['identidad_cliente'] ?? '';
        $nombre = $_POST['nombre_cliente'] ?? '';
        $apellido = $_POST['apellido_cliente'] ?? '';
        $telefono = $_POST['telefono_cliente'] ?? '';
        $correo = $_POST['correo_cliente'] ?? '';

        if ($id == 1) {
            echo json_encode(['success' => false, 'message' => 'No se puede modificar el cliente CONSUMIDOR FINAL.']);
            exit;
        }

        if (empty($identidad) || empty($nombre)) {
            echo json_encode(['success' => false, 'message' => 'Identidad y Nombre son requeridos.']);
            exit;
        }

        // Validar duplicados de identidad
        if ($clientModel->existsIdentity($identidad, $id)) {
            echo json_encode(['success' => false, 'message' => 'Ya existe un cliente activo con ese número de identidad.']);
            exit;
        }

        if ($id > 0) {
            // Actualizar
            $res = $clientModel->update($id, $identidad, $nombre, $apellido, $telefono, $correo);
            $msg = $res ? 'Cliente actualizado correctamente.' : 'Error al actualizar cliente.';
        } else {
            // Registrar
            $res = $clientModel->register($identidad, $nombre, $apellido, $telefono, $correo);
            $msg = $res ? 'Cliente registrado correctamente.' : 'Error al registrar cliente.';
        }

        echo json_encode(['success' => $res, 'message' => $msg]);
        break;

    case 'delete':
        $id = $_POST['id_cliente'] ?? 0;
        if ($id <= 0) {
            echo json_encode(['success' => false, 'message' => 'ID de cliente no válido.']);
            exit;
        }

        if ($id == 1) {
            echo json_encode(['success' => false, 'message' => 'No se puede eliminar el cliente CONSUMIDOR FINAL.']);
            exit;
        }

        $res = $clientModel->deactivate($id);
        echo json_encode(['success' => $res, 'message' => $res ? 'Cliente desactivado correctamente.' : 'Error al desactivar cliente.']);
        break;

    case 'activate':
        $id = $_POST['id_cliente'] ?? 0;
        if ($id <= 0) {
            echo json_encode(['success' => false, 'message' => 'ID de cliente no válido.']);
            exit;
        }

        $res = $clientModel->activate($id);
        echo json_encode(['success' => $res, 'message' => $res ? 'Cliente activado correctamente.' : 'Error al activar cliente.']);
        break;

    default:
        echo json_encode(['success' => false, 'message' => 'Acción no soportada.']);
        break;
}
