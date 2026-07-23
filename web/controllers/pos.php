<?php
session_start();
header('Content-Type: application/json; charset=utf-8');
require_once dirname(__DIR__) . '/models/Sale.php';
require_once dirname(__DIR__) . '/models/Product.php';

if (!isset($_SESSION['user_id'])) {
    echo json_encode(['success' => false, 'message' => 'Sesión no iniciada.']);
    exit;
}

$action = $_GET['action'] ?? $_POST['action'] ?? '';
$saleModel = new Sale();

switch ($action) {
    case 'search_product':
        $barcode = $_GET['barcode'] ?? '';
        if (empty($barcode)) {
            echo json_encode(['success' => false, 'message' => 'Código de barras no proporcionado.']);
            exit;
        }

        $prodModel = new Product();
        $product = $prodModel->getByBarcode($barcode);

        if ($product) {
            echo json_encode(['success' => true, 'product' => $product]);
        } else {
            echo json_encode(['success' => false, 'message' => 'Producto no encontrado o inactivo.']);
        }
        break;

    case 'get_payment_methods':
        echo json_encode(['success' => true, 'data' => $saleModel->getPaymentMethods()]);
        break;

    case 'check_imei':
        $imei = $_GET['imei'] ?? '';
        if (empty($imei)) {
            echo json_encode(['success' => false, 'message' => 'IMEI vacío.']);
            exit;
        }

        $exists = $saleModel->existsImeiSold($imei);
        echo json_encode(['success' => true, 'exists' => $exists]);
        break;

    case 'process_sale':
        $idCliente = (int)($_POST['id_cliente'] ?? 1); // 1 = Consumidor Final
        $idUsuario = $_SESSION['user_id'];
        $idMetodoPago = (int)($_POST['id_metodo_pago'] ?? 0);
        $subtotal = (float)($_POST['subtotal'] ?? 0.0);
        $impuesto = (float)($_POST['impuesto'] ?? 0.0);
        $total = (float)($_POST['total'] ?? 0.0);
        $referenciaPago = $_POST['referencia_pago'] ?? null;
        $bancoPago = $_POST['banco_pago'] ?? null;
        
        $detallesJson = $_POST['detalles'] ?? '';
        if (empty($detallesJson)) {
            echo json_encode(['success' => false, 'message' => 'El carrito está vacío.']);
            exit;
        }

        $detalles = json_decode($detallesJson, true);
        if (!is_array($detalles) || count($detalles) === 0) {
            echo json_encode(['success' => false, 'message' => 'Estructura del carrito no válida.']);
            exit;
        }

        if ($idMetodoPago <= 0) {
            echo json_encode(['success' => false, 'message' => 'Seleccione un método de pago válido.']);
            exit;
        }

        // Validaciones previas de stock y requerimiento de IMEI
        $prodModel = new Product();
        foreach ($detalles as $index => $fila) {
            $p = $prodModel->getById($fila['id_producto']);
            if (!$p) {
                echo json_encode(['success' => false, 'message' => "El producto {$fila['nombre']} no existe en inventario."]);
                exit;
            }

            if ($p['stock_producto'] < $fila['cantidad']) {
                echo json_encode(['success' => false, 'message' => "Stock insuficiente para '{$p['nombre_producto']}'. Stock disponible: {$p['stock_producto']}"]);
                exit;
            }

            if ($p['requiere_serie'] && empty($fila['imei'])) {
                echo json_encode(['success' => false, 'message' => "El producto '{$p['nombre_producto']}' requiere número de serie/IMEI."]);
                exit;
            }

            if ($p['requiere_serie'] && !empty($fila['imei'])) {
                if ($saleModel->existsImeiSold($fila['imei'])) {
                    echo json_encode(['success' => false, 'message' => "El IMEI/Serie '{$fila['imei']}' ya fue vendido en otra transacción."]);
                    exit;
                }
            }
            
            // Asignar dias de garantia del producto original para registrar
            $detalles[$index]['dias_garantia'] = (int)$p['dias_garantia'];
        }

        // Registrar venta
        $idVenta = $saleModel->processSale($idCliente, $idUsuario, $idMetodoPago, $subtotal, $impuesto, $total, $referenciaPago, $bancoPago, $detalles);

        if ($idVenta) {
            echo json_encode(['success' => true, 'message' => 'Venta cobrada y guardada correctamente.', 'id_venta' => $idVenta]);
        } else {
            echo json_encode(['success' => false, 'message' => 'Ocurrió un error en el servidor al registrar la venta.']);
        }
        break;

    case 'list_sales':
        $sales = $saleModel->listSales();
        echo json_encode(['success' => true, 'data' => $sales]);
        break;

    case 'get_receipt':
        $id = (int)($_GET['id_venta'] ?? 0);
        if ($id <= 0) {
            echo json_encode(['success' => false, 'message' => 'ID de venta no válido.']);
            exit;
        }
        $receipt = $saleModel->getReceipt($id);
        if ($receipt) {
            echo json_encode(['success' => true, 'data' => $receipt]);
        } else {
            echo json_encode(['success' => false, 'message' => 'Venta no encontrada.']);
        }
        break;

    default:
        echo json_encode(['success' => false, 'message' => 'Acción no soportada.']);
        break;
}
