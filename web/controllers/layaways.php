<?php
session_start();
header('Content-Type: application/json; charset=utf-8');
require_once dirname(__DIR__) . '/models/Layaway.php';
require_once dirname(__DIR__) . '/models/Product.php';

if (!isset($_SESSION['user_id'])) {
    echo json_encode(['success' => false, 'message' => 'Sesión no iniciada.']);
    exit;
}

$action = $_GET['action'] ?? $_POST['action'] ?? '';
$layawayModel = new Layaway();

switch ($action) {
    case 'list':
        $layaways = $layawayModel->listAll();
        echo json_encode(['success' => true, 'data' => $layaways]);
        break;

    case 'get':
        $id = (int)($_GET['id'] ?? 0);
        if ($id <= 0) {
            echo json_encode(['success' => false, 'message' => 'ID de apartado no válido.']);
            exit;
        }
        $data = $layawayModel->getDetails($id);
        if ($data) {
            echo json_encode(['success' => true, 'data' => $data]);
        } else {
            echo json_encode(['success' => false, 'message' => 'Apartado no encontrado.']);
        }
        break;

    case 'save':
        $idCliente = (int)($_POST['id_cliente'] ?? 0);
        $idUsuario = $_SESSION['user_id'];
        $total = (float)($_POST['total'] ?? 0.0);
        $abonoInicial = (float)($_POST['abono_inicial'] ?? 0.0);
        $idMetodoPago = (int)($_POST['id_metodo_pago'] ?? 0);
        $referenciaPago = $_POST['referencia_pago'] ?? null;
        $bancoPago = $_POST['banco_pago'] ?? null;
        
        $detallesJson = $_POST['detalles'] ?? '';
        if (empty($detallesJson) || $idCliente <= 0 || $total <= 0) {
            echo json_encode(['success' => false, 'message' => 'Datos de apartado incompletos.']);
            exit;
        }

        $detalles = json_decode($detallesJson, true);
        if (!is_array($detalles) || count($detalles) === 0) {
            echo json_encode(['success' => false, 'message' => 'El carrito está vacío.']);
            exit;
        }

        // Validar stocks
        $prodModel = new Product();
        foreach ($detalles as $item) {
            $p = $prodModel->getById($item['id_producto']);
            if (!$p) {
                echo json_encode(['success' => false, 'message' => "El producto {$item['nombre']} no existe."]);
                exit;
            }
            if ($p['stock_producto'] < $item['cantidad']) {
                echo json_encode(['success' => false, 'message' => "Stock insuficiente para '{$p['nombre_producto']}'. Disponible: {$p['stock_producto']}"]);
                exit;
            }
            if ($p['requiere_serie'] && empty($item['imei'])) {
                echo json_encode(['success' => false, 'message' => "El producto '{$p['nombre_producto']}' requiere IMEI/Serie."]);
                exit;
            }
        }

        $idApartado = $layawayModel->register($idCliente, $idUsuario, $total, $abonoInicial, $idMetodoPago, $referenciaPago, $bancoPago, $detalles);

        if ($idApartado) {
            echo json_encode([
                'success' => true, 
                'message' => 'Apartado registrado y stock reservado correctamente.', 
                'id_apartado' => $idApartado,
                'abono_inicial' => $abonoInicial
            ]);
        } else {
            echo json_encode(['success' => false, 'message' => 'Error al procesar el registro del apartado.']);
        }
        break;

    case 'add_abono':
        $idApartado = (int)($_POST['id_apartado'] ?? 0);
        $idUsuario = $_SESSION['user_id'];
        $idMetodo = (int)($_POST['id_metodo_pago'] ?? 0);
        $monto = (float)($_POST['monto_abono'] ?? 0.0);
        $ref = $_POST['referencia_pago'] ?? null;
        $banco = $_POST['banco_pago'] ?? null;

        if ($idApartado <= 0 || $idMetodo <= 0 || $monto <= 0) {
            echo json_encode(['success' => false, 'message' => 'Datos de abono no válidos.']);
            exit;
        }

        $idAbono = $layawayModel->addAbono($idApartado, $idUsuario, $idMetodo, $monto, $ref, $banco);

        if ($idAbono) {
            echo json_encode([
                'success' => true, 
                'message' => 'Abono registrado correctamente.',
                'id_abono' => $idAbono
            ]);
        } else {
            echo json_encode(['success' => false, 'message' => 'Error al procesar el abono en el servidor.']);
        }
        break;

    case 'deliver':
        $id = (int)($_POST['id_apartado'] ?? 0);
        if ($id <= 0) {
            echo json_encode(['success' => false, 'message' => 'ID de apartado no válido.']);
            exit;
        }

        $res = $layawayModel->deliver($id);
        echo json_encode([
            'success' => $res, 
            'message' => $res ? 'Apartado entregado con éxito al cliente.' : 'Error al registrar entrega. Verifique que el apartado esté completamente pagado.'
        ]);
        break;

    case 'cancel':
        $id = (int)($_POST['id_apartado'] ?? 0);
        $idUsuarioFirma = (int)($_POST['id_usuario_firma'] ?? 0); // Validado previamente mediante firma de contraseña

        if ($id <= 0 || $idUsuarioFirma <= 0) {
            echo json_encode(['success' => false, 'message' => 'Autorización no válida.']);
            exit;
        }

        $res = $layawayModel->cancel($id, $idUsuarioFirma);
        echo json_encode([
            'success' => $res, 
            'message' => $res ? 'Apartado cancelado y stock devuelto al inventario.' : 'Error al cancelar apartado. Verifique el estado del mismo.'
        ]);
        break;

    default:
        echo json_encode(['success' => false, 'message' => 'Acción no soportada.']);
        break;
}
