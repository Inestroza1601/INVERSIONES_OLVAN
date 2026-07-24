<?php
session_start();
header('Content-Type: application/json; charset=utf-8');
require_once dirname(__DIR__) . '/models/User.php';

$action = $_GET['action'] ?? $_POST['action'] ?? '';
$userModel = new User();

switch ($action) {
    case 'login':
        $username = $_POST['username'] ?? '';
        $password = $_POST['password'] ?? '';

        if (empty($username) || empty($password)) {
            echo json_encode(['success' => false, 'message' => 'Por favor, ingrese usuario y contraseña.']);
            exit;
        }

        $user = $userModel->authenticate($username, $password);

        if ($user) {
            $_SESSION['user_id'] = $user['id_usuario'];
            $_SESSION['username'] = $user['nombre_usuario'];
            $_SESSION['role_id'] = $user['id_rol'];
            $_SESSION['role_name'] = $user['nombre_rol'];
            
            echo json_encode([
                'success' => true, 
                'user' => [
                    'username' => $user['nombre_usuario'],
                    'role' => $user['nombre_rol']
                ]
            ]);
        } else {
            echo json_encode(['success' => false, 'message' => 'Usuario o contraseña incorrectos.']);
        }
        break;

    case 'verify_signature':
        // Se valida firma utilizando contraseña (como en Kardex)
        if (!isset($_SESSION['user_id'])) {
            echo json_encode(['success' => false, 'message' => 'Sesión no iniciada.']);
            exit;
        }

        $password = $_POST['password'] ?? '';
        if (empty($password)) {
            echo json_encode(['success' => false, 'message' => 'Contraseña requerida para firmar.']);
            exit;
        }

        // Puede ser que busquemos validar cualquier usuario activo que firme, o el usuario actual
        // En Java se busca el hash en toda la tabla y devuelve el ID del usuario que corresponda:
        // "Valida la firma SOLO con la contraseña. Encripta lo que el usuario escribe y busca ese Hash en la BD."
        // Replicamos esta lógica exacta:
        $hash = User::hashSHA256($password);
        
        // Usar db directamente para buscar por hash
        $db = Db::getConnection();
        $stmt = $db->prepare("SELECT id_usuario, nombre_usuario FROM USUARIOS WHERE password_hash = ? AND estado_usuario = 1");
        $stmt->execute([$hash]);
        $signer = $stmt->fetch();

        if ($signer) {
            echo json_encode([
                'success' => true,
                'user_id' => $signer['id_usuario'],
                'username' => $signer['nombre_usuario']
            ]);
        } else {
            echo json_encode(['success' => false, 'message' => 'Firma inválida. Contraseña incorrecta.']);
        }
        break;

    case 'logout':
        session_destroy();
        echo json_encode(['success' => true]);
        break;

    default:
        echo json_encode(['success' => false, 'message' => 'Acción no válida.']);
        break;
}
