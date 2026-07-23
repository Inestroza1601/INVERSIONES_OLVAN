<?php
require_once dirname(__DIR__) . '/config/db.php';

class Product {
    private $db;

    public function __construct() {
        $this->db = Db::getConnection();
    }

    // ==========================================
    // MÉTODOS DE PRODUCTO (INVENTARIO)
    // ==========================================

    public function listActive() {
        $sql = "SELECT p.*, c.nombre_categoria, pr.nombre_proveedor, u.nombre_ubicacion 
                FROM INVENTARIO p
                LEFT JOIN CATEGORIAS c ON p.id_categoria = c.id_categoria
                LEFT JOIN PROVEEDORES pr ON p.id_proveedor = pr.id_proveedor
                LEFT JOIN UBICACIONES u ON p.id_ubicacion = u.id_ubicacion
                WHERE p.eliminado_producto = 0 
                ORDER BY p.nombre_producto ASC";
        $stmt = $this->db->query($sql);
        return $stmt->fetchAll();
    }

    public function getById($id) {
        $sql = "SELECT * FROM INVENTARIO WHERE id_producto = ?";
        $stmt = $this->db->prepare($sql);
        $stmt->execute([$id]);
        return $stmt->fetch();
    }

    public function getByBarcode($barcode) {
        $sql = "SELECT * FROM INVENTARIO WHERE codigo_barras_producto = ? AND eliminado_producto = 0";
        $stmt = $this->db->prepare($sql);
        $stmt->execute([$barcode]);
        return $stmt->fetch();
    }

    public function register($data) {
        $sql = "INSERT INTO INVENTARIO (
                    codigo_barras_producto, nombre_producto, id_categoria, id_proveedor, id_ubicacion, 
                    precio_compra_producto, precio_venta_producto, precio_mayorista_producto, 
                    stock_minimo_producto, stock_producto, ruta_imagen_producto, dias_garantia, 
                    requiere_serie, eliminado_producto
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 0)";
        
        $barcode = !empty($data['codigo_barras']) ? trim($data['codigo_barras']) : null;
        $precioMayorista = $data['precio_mayorista'] > 0 ? $data['precio_mayorista'] : null;
        $rutaImagen = !empty($data['ruta_imagen']) ? $data['ruta_imagen'] : null;

        $stmt = $this->db->prepare($sql);
        $res = $stmt->execute([
            $barcode,
            trim($data['nombre']),
            $data['id_categoria'],
            $data['id_proveedor'],
            $data['id_ubicacion'],
            $data['precio_compra'],
            $data['precio_venta'],
            $precioMayorista,
            $data['stock_minimo'],
            $data['stock'],
            $rutaImagen,
            $data['dias_garantia'],
            isset($data['requiere_serie']) && $data['requiere_serie'] ? 1 : 0
        ]);

        if ($res && $barcode === null) {
            $idGenerado = $this->db->lastInsertId();
            if ($idGenerado > 0) {
                $sqlUpdate = "UPDATE INVENTARIO SET codigo_barras_producto = ? WHERE id_producto = ?";
                $stmtUpdate = $this->db->prepare($sqlUpdate);
                $stmtUpdate->execute([$idGenerado, $idGenerado]);
            }
        }
        return $res;
    }

    public function update($id, $data) {
        $sql = "UPDATE INVENTARIO SET 
                    codigo_barras_producto = ?, nombre_producto = ?, id_categoria = ?, id_proveedor = ?, 
                    id_ubicacion = ?, precio_compra_producto = ?, precio_venta_producto = ?, 
                    precio_mayorista_producto = ?, stock_minimo_producto = ?, ruta_imagen_producto = ?, 
                    dias_garantia = ?, requiere_serie = ? 
                WHERE id_producto = ?";
        
        $barcode = !empty($data['codigo_barras']) ? trim($data['codigo_barras']) : null;
        $precioMayorista = $data['precio_mayorista'] > 0 ? $data['precio_mayorista'] : null;
        $rutaImagen = !empty($data['ruta_imagen']) ? $data['ruta_imagen'] : null;

        $stmt = $this->db->prepare($sql);
        return $stmt->execute([
            $barcode,
            trim($data['nombre']),
            $data['id_categoria'],
            $data['id_proveedor'],
            $data['id_ubicacion'],
            $data['precio_compra'],
            $data['precio_venta'],
            $precioMayorista,
            $data['stock_minimo'],
            $rutaImagen,
            $data['dias_garantia'],
            isset($data['requiere_serie']) && $data['requiere_serie'] ? 1 : 0,
            $id
        ]);
    }

    public function delete($id) {
        $sql = "UPDATE INVENTARIO SET eliminado_producto = 1 WHERE id_producto = ?";
        $stmt = $this->db->prepare($sql);
        return $stmt->execute([$id]);
    }

    public function existsBarcode($barcode, $excludeId = 0) {
        if (empty($barcode)) return false;
        $sql = "SELECT id_producto FROM INVENTARIO WHERE codigo_barras_producto = ? AND id_producto != ? AND eliminado_producto = 0";
        $stmt = $this->db->prepare($sql);
        $stmt->execute([trim($barcode), $excludeId]);
        return $stmt->fetch() ? true : false;
    }

    // ==========================================
    // MÉTODOS DE SOPORTE (CATEGORÍAS)
    // ==========================================

    public function listCategories() {
        $sql = "SELECT * FROM CATEGORIAS ORDER BY nombre_categoria ASC";
        $stmt = $this->db->query($sql);
        return $stmt->fetchAll();
    }

    public function registerCategory($nombre, $desc, $garantias) {
        $sql = "INSERT INTO CATEGORIAS (nombre_categoria, descripcion_categoria, dias_garantias) VALUES (?, ?, ?)";
        $stmt = $this->db->prepare($sql);
        return $stmt->execute([trim($nombre), trim($desc), $garantias]);
    }

    public function updateCategory($id, $nombre, $desc, $garantias) {
        $sql = "UPDATE CATEGORIAS SET nombre_categoria = ?, descripcion_categoria = ?, dias_garantias = ? WHERE id_categoria = ?";
        $stmt = $this->db->prepare($sql);
        return $stmt->execute([trim($nombre), trim($desc), $garantias, $id]);
    }

    // ==========================================
    // MÉTODOS DE SOPORTE (PROVEEDORES)
    // ==========================================

    public function listProviders() {
        $sql = "SELECT * FROM PROVEEDORES WHERE estado_proveedor = 1 ORDER BY nombre_proveedor ASC";
        $stmt = $this->db->query($sql);
        return $stmt->fetchAll();
    }

    public function registerProvider($nombre, $encargado, $tel, $dir, $repuestos) {
        $sql = "INSERT INTO PROVEEDORES (nombre_proveedor, nombre_encargado_proveedor, telefono_proveedor, direccion_proveedor, tipo_repuestos_proveedor, estado_proveedor) 
                VALUES (?, ?, ?, ?, ?, 1)";
        $stmt = $this->db->prepare($sql);
        return $stmt->execute([trim($nombre), trim($encargado), trim($tel), trim($dir), trim($repuestos)]);
    }

    public function updateProvider($id, $nombre, $encargado, $tel, $dir, $repuestos) {
        $sql = "UPDATE PROVEEDORES SET nombre_proveedor = ?, nombre_encargado_proveedor = ?, telefono_proveedor = ?, 
                                       direccion_proveedor = ?, tipo_repuestos_proveedor = ? 
                WHERE id_proveedor = ?";
        $stmt = $this->db->prepare($sql);
        return $stmt->execute([trim($nombre), trim($encargado), trim($tel), trim($dir), trim($repuestos), $id]);
    }

    // ==========================================
    // MÉTODOS DE SOPORTE (UBICACIONES)
    // ==========================================

    public function listLocations() {
        $sql = "SELECT * FROM UBICACIONES ORDER BY nombre_ubicacion ASC";
        $stmt = $this->db->query($sql);
        return $stmt->fetchAll();
    }

    public function registerLocation($nombre) {
        $sql = "INSERT INTO UBICACIONES (nombre_ubicacion) VALUES (?)";
        $stmt = $this->db->prepare($sql);
        return $stmt->execute([trim($nombre)]);
    }

    public function updateLocation($id, $nombre) {
        $sql = "UPDATE UBICACIONES SET nombre_ubicacion = ? WHERE id_ubicacion = ?";
        $stmt = $this->db->prepare($sql);
        return $stmt->execute([trim($nombre), $id]);
    }
}
