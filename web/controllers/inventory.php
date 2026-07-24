<?php
session_start();
header('Content-Type: application/json; charset=utf-8');
require_once dirname(__DIR__) . '/models/Product.php';
require_once dirname(__DIR__) . '/models/Kardex.php'; // Lo creamos a continuación o agregamos lógica de kardex directa

if (!isset($_SESSION['user_id'])) {
    echo json_encode(['success' => false, 'message' => 'Sesión no iniciada.']);
    exit;
}

$action = $_GET['action'] ?? $_POST['action'] ?? '';
$productModel = new Product();

switch ($action) {
    case 'list':
        $products = $productModel->listActive();
        echo json_encode(['success' => true, 'data' => $products]);
        break;

    case 'save':
        $id = $_POST['id_producto'] ?? 0;
        $data = [
            'codigo_barras' => $_POST['codigo_barras_producto'] ?? '',
            'nombre' => $_POST['nombre_producto'] ?? '',
            'id_categoria' => (int)($_POST['id_categoria'] ?? 0),
            'id_proveedor' => (int)($_POST['id_proveedor'] ?? 0),
            'id_ubicacion' => (int)($_POST['id_ubicacion'] ?? 0),
            'precio_compra' => (float)($_POST['precio_compra_producto'] ?? 0.0),
            'precio_venta' => (float)($_POST['precio_venta_producto'] ?? 0.0),
            'precio_mayorista' => (float)($_POST['precio_mayorista_producto'] ?? 0.0),
            'stock_minimo' => (int)($_POST['stock_minimo_producto'] ?? 0),
            'stock' => (int)($_POST['stock_producto'] ?? 0),
            'ruta_imagen' => $_POST['ruta_imagen_producto'] ?? '',
            'dias_garantia' => (int)($_POST['dias_garantia'] ?? 0),
            'requiere_serie' => isset($_POST['requiere_serie']) && ($_POST['requiere_serie'] === '1' || $_POST['requiere_serie'] === 'true')
        ];

        if (empty($data['nombre'])) {
            echo json_encode(['success' => false, 'message' => 'El nombre del producto es requerido.']);
            exit;
        }

        // Validar duplicado de código de barras
        if (!empty($data['codigo_barras']) && $productModel->existsBarcode($data['codigo_barras'], $id)) {
            echo json_encode(['success' => false, 'message' => 'El código de barras ya está registrado en otro producto activo.']);
            exit;
        }

        if ($id > 0) {
            $res = $productModel->update($id, $data);
            $msg = $res ? 'Producto actualizado correctamente.' : 'Error al actualizar producto.';
        } else {
            $res = $productModel->register($data);
            $msg = $res ? 'Producto registrado correctamente.' : 'Error al registrar producto.';
        }

        echo json_encode(['success' => $res, 'message' => $msg]);
        break;

    case 'delete':
        $id = $_POST['id_producto'] ?? 0;
        if ($id <= 0) {
            echo json_encode(['success' => false, 'message' => 'ID de producto no válido.']);
            exit;
        }

        $res = $productModel->delete($id);
        echo json_encode(['success' => $res, 'message' => $res ? 'Producto eliminado del inventario.' : 'Error al eliminar producto.']);
        break;

    case 'upload_image':
        if (!isset($_FILES['image'])) {
            echo json_encode(['success' => false, 'message' => 'No se subió ninguna imagen.']);
            exit;
        }

        $file = $_FILES['image'];
        $ext = strtolower(pathinfo($file['name'], PATHINFO_EXTENSION));
        $allowed = ['jpg', 'jpeg', 'png', 'webp', 'gif'];

        if (!in_array($ext, $allowed)) {
            echo json_encode(['success' => false, 'message' => 'Formato de imagen no permitido.']);
            exit;
        }

        // Comprimir y convertir la imagen a Base64
        $base64 = compressImageToBase64($file['tmp_name']);
        if ($base64 !== false) {
            echo json_encode(['success' => true, 'url' => $base64]);
        } else {
            echo json_encode(['success' => false, 'message' => 'Error al comprimir y procesar la imagen cargada.']);
        }
        break;

    // --- CATEGORIAS ---
    case 'list_categories':
        echo json_encode(['success' => true, 'data' => $productModel->listCategories()]);
        break;

    case 'save_category':
        $id = $_POST['id_categoria'] ?? 0;
        $nombre = $_POST['nombre_categoria'] ?? '';
        $desc = $_POST['descripcion_categoria'] ?? '';
        $garantias = (int)($_POST['dias_garantias'] ?? 0);

        if (empty($nombre)) {
            echo json_encode(['success' => false, 'message' => 'El nombre de categoría es requerido.']);
            exit;
        }

        if ($id > 0) {
            $res = $productModel->updateCategory($id, $nombre, $desc, $garantias);
        } else {
            $res = $productModel->registerCategory($nombre, $desc, $garantias);
        }

        echo json_encode(['success' => $res, 'message' => $res ? 'Categoría guardada.' : 'Error al guardar categoría.']);
        break;

    // --- PROVEEDORES ---
    case 'list_providers':
        echo json_encode(['success' => true, 'data' => $productModel->listProviders()]);
        break;

    case 'save_provider':
        $id = $_POST['id_proveedor'] ?? 0;
        $nombre = $_POST['nombre_proveedor'] ?? '';
        $encargado = $_POST['nombre_encargado_proveedor'] ?? '';
        $tel = $_POST['telefono_proveedor'] ?? '';
        $dir = $_POST['direccion_proveedor'] ?? '';
        $repuestos = $_POST['tipo_repuestos_proveedor'] ?? '';

        if (empty($nombre)) {
            echo json_encode(['success' => false, 'message' => 'El nombre del proveedor es requerido.']);
            exit;
        }

        if ($id > 0) {
            $res = $productModel->updateProvider($id, $nombre, $encargado, $tel, $dir, $repuestos);
        } else {
            $res = $productModel->registerProvider($nombre, $encargado, $tel, $dir, $repuestos);
        }

        echo json_encode(['success' => $res, 'message' => $res ? 'Proveedor guardado.' : 'Error al guardar proveedor.']);
        break;

    // --- UBICACIONES ---
    case 'list_locations':
        echo json_encode(['success' => true, 'data' => $productModel->listLocations()]);
        break;

    case 'save_location':
        $id = $_POST['id_ubicacion'] ?? 0;
        $nombre = $_POST['nombre_ubicacion'] ?? '';

        if (empty($nombre)) {
            echo json_encode(['success' => false, 'message' => 'El nombre de ubicación es requerido.']);
            exit;
        }

        if ($id > 0) {
            $res = $productModel->updateLocation($id, $nombre);
        } else {
            $res = $productModel->registerLocation($nombre);
        }

        echo json_encode(['success' => $res, 'message' => $res ? 'Ubicación guardada.' : 'Error al guardar ubicación.']);
        break;

    // --- KARDEX ---
    case 'get_kardex':
        $idProducto = $_GET['id_producto'] ?? 0;
        if ($idProducto <= 0) {
            echo json_encode(['success' => false, 'message' => 'ID de producto requerido.']);
            exit;
        }

        // Instanciar Kardex
        $kardexModel = new Kardex();
        $history = $kardexModel->getHistory($idProducto);
        echo json_encode(['success' => true, 'data' => $history]);
        break;

    case 'save_kardex':
        // Registrar un ajuste manual de inventario
        $idProducto = $_POST['id_producto'] ?? 0;
        $tipo = $_POST['tipo_movimiento_producto'] ?? ''; // 'Entrada' o 'Salida'
        $cantidad = (int)($_POST['cantidad_producto'] ?? 0);
        $referencia = $_POST['referencia_producto'] ?? '';
        $idUsuarioFirma = (int)($_POST['id_usuario_firma'] ?? 0); // Validado previamente mediante firma de contraseña

        if ($idProducto <= 0 || !in_array($tipo, ['Entrada', 'Salida']) || $cantidad <= 0 || $idUsuarioFirma <= 0) {
            echo json_encode(['success' => false, 'message' => 'Datos de movimiento inválidos o firma no autorizada.']);
            exit;
        }

        $kardexModel = new Kardex();
        $res = $kardexModel->registerMovement($idProducto, $tipo, $cantidad, $referencia, $idUsuarioFirma);
        echo json_encode([
            'success' => $res, 
            'message' => $res ? 'Ajuste de inventario registrado y Kardex actualizado.' : 'Error al registrar movimiento.'
        ]);
        break;

    default:
        echo json_encode(['success' => false, 'message' => 'Acción no soportada.']);
        break;
}

function compressImageToBase64($tmpPath) {
    $imgData = @file_get_contents($tmpPath);
    if (!$imgData) return false;

    // Cargar imagen de forma segura sin importar extensión
    $src = @imagecreatefromstring($imgData);
    if (!$src) return false;

    $width = imagesx($src);
    $height = imagesy($src);

    $maxDim = 350; // Resolución máx en píxeles para thumbs catalog
    if ($width > $maxDim || $height > $maxDim) {
        $ratio = min($maxDim / $width, $maxDim / $height);
        $newWidth = (int)($width * $ratio);
        $newHeight = (int)($height * $ratio);
    } else {
        $newWidth = $width;
        $newHeight = $height;
    }

    $dst = imagecreatetruecolor($newWidth, $newHeight);
    
    // Conservar color blanco de fondo si hay transparencias
    $white = imagecolorallocate($dst, 255, 255, 255);
    imagefill($dst, 0, 0, $white);

    imagecopyresampled($dst, $src, 0, 0, 0, 0, $newWidth, $newHeight, $width, $height);

    ob_start();
    imagejpeg($dst, null, 70); // 70% calidad comprimida
    $compressed = ob_get_clean();

    imagedestroy($src);
    imagedestroy($dst);

    return 'data:image/jpeg;base64,' . base64_encode($compressed);
}
