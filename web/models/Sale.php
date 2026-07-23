<?php
require_once dirname(__DIR__) . '/config/db.php';

class Sale {
    private $db;

    public function __construct() {
        $this->db = Db::getConnection();
    }

    public function getPaymentMethods() {
        $sql = "SELECT id_metodo_pago, nombre_metodo FROM METODOS_PAGO";
        $stmt = $this->db->query($sql);
        $methods = $stmt->fetchAll();

        if (empty($methods)) {
            // Inicializar métodos por defecto si está vacío
            $defaults = ["Efectivo", "Tarjeta", "Transferencia"];
            $sqlInsert = "INSERT INTO METODOS_PAGO (nombre_metodo) VALUES (?)";
            $stmtInsert = $this->db->prepare($sqlInsert);
            foreach ($defaults as $m) {
                $stmtInsert->execute([$m]);
            }
            $stmt = $this->db->query($sql);
            $methods = $stmt->fetchAll();
        }
        return $methods;
    }

    public function existsImeiSold($imei) {
        if (empty($imei)) return false;
        $sql = "SELECT COUNT(*) AS total FROM DETALLES_VENTA WHERE identificador_serie = ?";
        $stmt = $this->db->prepare($sql);
        $stmt->execute([trim($imei)]);
        $row = $stmt->fetch();
        return ($row && $row['total'] > 0);
    }

    public function processSale($idCliente, $idUsuario, $idMetodoPago, $subtotal, $impuesto, $total, $referenciaPago, $bancoPago, $detalles) {
        try {
            $this->db->beginTransaction();

            // 1. Insertar la cabecera de la venta
            $sqlVenta = "INSERT INTO VENTAS (fecha_venta, id_cliente_venta, id_usuario, id_metodo_pago, subtotal_venta, impuesto_venta, total_venta, referencia_pago, banco_pago) 
                         VALUES (GETDATE(), ?, ?, ?, ?, ?, ?, ?, ?)";
            
            $ref = !empty($referenciaPago) ? trim($referenciaPago) : null;
            $banco = (!empty($bancoPago) && $bancoPago !== 'Seleccione Banco...') ? trim($bancoPago) : null;

            $stmtV = $this->db->prepare($sqlVenta);
            $stmtV->execute([
                $idCliente,
                $idUsuario,
                $idMetodoPago,
                $subtotal,
                $impuesto,
                $total,
                $ref,
                $banco
            ]);

            // Obtener el ID de la venta generada
            // En SQL Server, lastInsertId() suele funcionar, pero por seguridad podemos usar SCOPE_IDENTITY si es null
            $idVenta = $this->db->lastInsertId();
            if (!$idVenta) {
                $idVenta = $this->db->query("SELECT SCOPE_IDENTITY() AS id")->fetch()['id'];
            }

            if (!$idVenta) {
                throw new Exception("No se pudo obtener el ID de la venta generada.");
            }

            // 2. Preparar statements de detalles y stock
            $sqlDetalle = "INSERT INTO DETALLES_VENTA (id_ventas, id_producto, descripcion_venta, cantidad_venta, precio_unitario_venta, subtotal_venta, identificador_serie, dias_garantia, estado_garantia) 
                           VALUES (?, ?, ?, ?, ?, ?, ?, ?, 'ACTIVA')";
            $stmtDetalle = $this->db->prepare($sqlDetalle);

            $sqlStockActual = "SELECT stock_producto FROM INVENTARIO WHERE id_producto = ?";
            $stmtStockActual = $this->db->prepare($sqlStockActual);

            $sqlStockUpdate = "UPDATE INVENTARIO SET stock_producto = ? WHERE id_producto = ?";
            $stmtStockUpdate = $this->db->prepare($sqlStockUpdate);

            $sqlKardex = "INSERT INTO KARDEX (id_producto, id_usuario, fecha_movimiento_producto, tipo_movimiento_producto, cantidad_producto, stock_restante_producto, referencia_producto) 
                          VALUES (?, ?, GETDATE(), 'Salida', ?, ?, ?)";
            $stmtKardex = $this->db->prepare($sqlKardex);

            // 3. Procesar cada fila de detalle
            foreach ($detalles as $fila) {
                $idProd = $fila['id_producto'];
                $imei = !empty($fila['imei']) ? trim($fila['imei']) : null;
                $nombre = $fila['nombre'];
                $cantidad = $fila['cantidad'];
                $precio = $fila['precio'];
                $subtotFila = $fila['subtotal_fila'];
                $diasGarantia = isset($fila['dias_garantia']) ? $fila['dias_garantia'] : 0;

                // Registrar detalle de venta
                $stmtDetalle->execute([
                    $idVenta,
                    $idProd,
                    $nombre,
                    $cantidad,
                    $precio,
                    $subtotFila,
                    $imei,
                    $diasGarantia
                ]);

                // Consultar stock actual
                $stmtStockActual->execute([$idProd]);
                $stockRow = $stmtStockActual->fetch();
                $stockActual = $stockRow ? $stockRow['stock_producto'] : 0;

                $stockRestante = $stockActual - $cantidad;

                // Actualizar stock
                $stmtStockUpdate->execute([$stockRestante, $idProd]);

                // Registrar en Kardex
                $referenciaKardex = "Venta #" . $idVenta;
                $stmtKardex->execute([
                    $idProd,
                    $idUsuario,
                    $cantidad,
                    $stockRestante,
                    $referenciaKardex
                ]);
            }

            $this->db->commit();
            return $idVenta;

        } catch (Exception $e) {
            if ($this->db->inTransaction()) {
                $this->db->rollBack();
            }
            error_log("Error al procesar venta: " . $e->getMessage());
            return false;
        }
    }

    public function getReceipt($idVenta) {
        $sqlVenta = "SELECT v.*, c.nombre_cliente, c.apellido_cliente, m.nombre_metodo, u.nombre_usuario 
                     FROM VENTAS v 
                     LEFT JOIN CLIENTES c ON v.id_cliente_venta = c.id_cliente 
                     LEFT JOIN METODOS_PAGO m ON v.id_metodo_pago = m.id_metodo_pago 
                     LEFT JOIN USUARIOS u ON v.id_usuario = u.id_usuario
                     WHERE v.id_ventas = ?";
        
        $stmtV = $this->db->prepare($sqlVenta);
        $stmtV->execute([$idVenta]);
        $venta = $stmtV->fetch();

        if (!$venta) return null;

        $sqlDetalle = "SELECT d.*, p.codigo_barras_producto 
                       FROM DETALLES_VENTA d
                       LEFT JOIN INVENTARIO p ON d.id_producto = p.id_producto
                       WHERE d.id_ventas = ?";
        $stmtD = $this->db->prepare($sqlDetalle);
        $stmtD->execute([$idVenta]);
        $detalles = $stmtD->fetchAll();

        return [
            'venta' => $venta,
            'detalles' => $detalles
        ];
    }

    public function listSales() {
        $sql = "SELECT v.id_ventas, v.fecha_venta, v.subtotal_venta, v.impuesto_venta, v.total_venta, 
                       v.referencia_pago, v.banco_pago, c.nombre_cliente, c.apellido_cliente, 
                       m.nombre_metodo, u.nombre_usuario 
                FROM VENTAS v 
                LEFT JOIN CLIENTES c ON v.id_cliente_venta = c.id_cliente 
                LEFT JOIN METODOS_PAGO m ON v.id_metodo_pago = m.id_metodo_pago 
                LEFT JOIN USUARIOS u ON v.id_usuario = u.id_usuario 
                ORDER BY v.fecha_venta DESC";
        $stmt = $this->db->query($sql);
        return $stmt->fetchAll();
    }
}
