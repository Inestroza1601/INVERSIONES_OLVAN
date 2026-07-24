<?php
session_start();
header('Content-Type: application/json; charset=utf-8');
require_once dirname(__DIR__) . '/models/User.php';

if (!isset($_SESSION['user_id'])) {
    echo json_encode(['success' => false, 'message' => 'Sesión no iniciada.']);
    exit;
}

// Controlar accesos por rol si fuese necesario. Por ejemplo, solo administrador (id_rol = 1) puede gestionar usuarios
// Para este sistema dejamos acceso general o validado por rol
$action = $_GET['action'] ?? $_POST['action'] ?? '';
$userModel = new User();

switch ($action) {
    case 'list':
        $users = $userModel->listAll();
        // Ocultar hash por seguridad
        foreach ($users as &$u) {
            unset($u['password_hash']);
        }
        echo json_encode(['success' => true, 'data' => $users]);
        break;

    case 'list_roles':
        $roles = $userModel->listRoles();
        echo json_encode(['success' => true, 'data' => $roles]);
        break;

    case 'save':
        $id = $_POST['id_usuario'] ?? 0;
        $idRol = (int)($_POST['id_rol'] ?? 0);
        $username = trim($_POST['nombre_usuario'] ?? '');
        $password = $_POST['password_usuario'] ?? '';

        if (empty($username) || $idRol <= 0) {
            echo json_encode(['success' => false, 'message' => 'El Nombre de Usuario y Rol son requeridos.']);
            exit;
        }

        if ($id > 0) {
            $res = $userModel->update($id, $idRol, $username, $password);
            $msg = $res ? 'Usuario actualizado correctamente.' : 'Error al actualizar usuario.';
        } else {
            if (empty($password)) {
                echo json_encode(['success' => false, 'message' => 'La contraseña es requerida para nuevos usuarios.']);
                exit;
            }
            $res = $userModel->register($idRol, $username, $password);
            $msg = $res ? 'Usuario registrado correctamente.' : 'Error al registrar usuario.';
        }

        echo json_encode(['success' => $res, 'message' => $msg]);
        break;

    case 'delete':
        $id = $_POST['id_usuario'] ?? 0;
        if ($id <= 0) {
            echo json_encode(['success' => false, 'message' => 'ID de usuario no válido.']);
            exit;
        }

        if ($id == $_SESSION['user_id']) {
            echo json_encode(['success' => false, 'message' => 'No puedes desactivar tu propio usuario activo.']);
            exit;
        }

        $res = $userModel->deactivate($id);
        echo json_encode(['success' => $res, 'message' => $res ? 'Usuario desactivado correctamente.' : 'Error al desactivar usuario.']);
        break;

    case 'activate':
        $id = $_POST['id_usuario'] ?? 0;
        if ($id <= 0) {
            echo json_encode(['success' => false, 'message' => 'ID de usuario no válido.']);
            exit;
        }

        $res = $userModel->activate($id);
        echo json_encode(['success' => $res, 'message' => $res ? 'Usuario activado correctamente.' : 'Error al activar usuario.']);
        break;

    default:
        echo json_encode(['success' => false, 'message' => 'Acción no soportada.']);
        break;
}
?>
