<?php
require_once dirname(__DIR__) . '/config/db.php';

class Kardex {
    private $db;

    public function __construct() {
        $this->db = Db::getConnection();
    }

    public function getHistory($idProducto) {
        $sql = "SELECT k.fecha_movimiento_producto, k.tipo_movimiento_producto, k.cantidad_producto, 
                       k.stock_restante_producto, k.referencia_producto, u.nombre_usuario 
                FROM KARDEX k 
                INNER JOIN USUARIOS u ON k.id_usuario = u.id_usuario 
                WHERE k.id_producto = ? 
                ORDER BY k.fecha_movimiento_producto DESC";
        
        $stmt = $this->db->prepare($sql);
        $stmt->execute([$idProducto]);
        return $stmt->fetchAll();
    }

    public function registerMovement($idProducto, $tipoMovimiento, $cantidad, $observacion, $idUsuario) {
        try {
            $this->db->beginTransaction();

            // 1. Obtener el stock actual
            $sqlStock = "SELECT stock_producto FROM INVENTARIO WHERE id_producto = ?";
            $stmtStock = $this->db->prepare($sqlStock);
            $stmtStock->execute([$idProducto]);
            $row = $stmtStock->fetch();
            $stockActual = $row ? (int)$row['stock_producto'] : 0;

            // 2. Calcular nuevo stock
            $nuevoStock = ($tipoMovimiento === 'Salida') ? ($stockActual - $cantidad) : ($stockActual + $cantidad);

            // 3. Insertar movimiento en Kardex
            $sqlInsert = "INSERT INTO KARDEX (id_producto, fecha_movimiento_producto, tipo_movimiento_producto, cantidad_producto, stock_restante_producto, referencia_producto, id_usuario) 
                          VALUES (?, GETDATE(), ?, ?, ?, ?, ?)";
            $stmtInsert = $this->db->prepare($sqlInsert);
            $stmtInsert->execute([
                $idProducto,
                $tipoMovimiento,
                $cantidad,
                $nuevoStock,
                trim($observacion),
                $idUsuario
            ]);

            // 4. Actualizar stock en Inventario
            $sqlUpdate = "UPDATE INVENTARIO SET stock_producto = ? WHERE id_producto = ?";
            $stmtUpdate = $this->db->prepare($sqlUpdate);
            $stmtUpdate->execute([$nuevoStock, $idProducto]);

            $this->db->commit();
            return true;

        } catch (Exception $e) {
            if ($this->db->inTransaction()) {
                $this->db->rollBack();
            }
            error_log("Error al registrar movimiento en Kardex: " . $e->getMessage());
            return false;
        }
    }
}
