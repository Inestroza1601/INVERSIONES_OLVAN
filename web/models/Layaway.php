<?php
require_once dirname(__DIR__) . '/config/db.php';

class Layaway {
    private $db;

    public function __construct() {
        $this->db = Db::getConnection();
    }

    public function listAll() {
        $sql = "SELECT a.*, c.nombre_cliente, c.apellido_cliente, u.nombre_usuario 
                FROM APARTADOS a
                LEFT JOIN CLIENTES c ON a.id_cliente_apartado = c.id_cliente
                LEFT JOIN USUARIOS u ON a.id_usuario = u.id_usuario
                ORDER BY a.fecha_apartado DESC";
        $stmt = $this->db->query($sql);
        return $stmt->fetchAll();
    }

    public function getDetails($idApartado) {
        $sqlHeader = "SELECT a.*, c.nombre_cliente, c.apellido_cliente, u.nombre_usuario 
                      FROM APARTADOS a
                      LEFT JOIN CLIENTES c ON a.id_cliente_apartado = c.id_cliente
                      LEFT JOIN USUARIOS u ON a.id_usuario = u.id_usuario
                      WHERE a.id_apartado = ?";
        $stmtH = $this->db->prepare($sqlHeader);
        $stmtH->execute([$idApartado]);
        $header = $stmtH->fetch();

        if (!$header) return null;

        $sqlItems = "SELECT d.*, p.codigo_barras_producto 
                     FROM DETALLES_APARTADO d
                     LEFT JOIN INVENTARIO p ON d.id_producto = p.id_producto
                     WHERE d.id_apartado = ?";
        $stmtI = $this->db->prepare($sqlItems);
        $stmtI->execute([$idApartado]);
        $items = $stmtI->fetchAll();

        $sqlAbonos = "SELECT ab.*, m.nombre_metodo, u.nombre_usuario 
                      FROM ABONOS_APARTADO ab
                      LEFT JOIN METODOS_PAGO m ON ab.id_metodo_pago = m.id_metodo_pago
                      LEFT JOIN USUARIOS u ON ab.id_usuario = u.id_usuario
                      WHERE ab.id_apartado = ?
                      ORDER BY ab.fecha_abono ASC";
        $stmtA = $this->db->prepare($sqlAbonos);
        $stmtA->execute([$idApartado]);
        $abonos = $stmtA->fetchAll();

        return [
            'header' => $header,
            'items' => $items,
            'abonos' => $abonos
        ];
    }

    public function register($idCliente, $idUsuario, $total, $abonoInicial, $idMetodoPago, $referenciaPago, $bancoPago, $detalles) {
        try {
            $this->db->beginTransaction();

            $saldoPendiente = $total - $abonoInicial;
            $estado = ($saldoPendiente <= 0) ? 'PAGADO' : 'VIGENTE';

            // 1. Insertar Apartado
            $sqlA = "INSERT INTO APARTADOS (id_cliente_apartado, id_usuario, fecha_apartado, total_apartado, saldo_pendiente, estado_apartado, fecha_entrega) 
                     VALUES (?, ?, GETDATE(), ?, ?, ?, NULL)";
            $stmtA = $this->db->prepare($sqlA);
            $stmtA->execute([$idCliente, $idUsuario, $total, $saldoPendiente, $estado]);

            $idApartado = $this->db->lastInsertId();
            if (!$idApartado) {
                $idApartado = $this->db->query("SELECT SCOPE_IDENTITY() AS id")->fetch()['id'];
            }

            // 2. Insertar Detalles y Descontar Stock
            $sqlD = "INSERT INTO DETALLES_APARTADO (id_apartado, id_producto, descripcion_apartado, cantidad_apartado, precio_unitario_apartado, subtotal_apartado, identificador_serie) 
                     VALUES (?, ?, ?, ?, ?, ?, ?)";
            $stmtD = $this->db->prepare($sqlD);

            $sqlStock = "SELECT stock_producto FROM INVENTARIO WHERE id_producto = ?";
            $stmtStock = $this->db->prepare($sqlStock);

            $sqlUpdateStock = "UPDATE INVENTARIO SET stock_producto = ? WHERE id_producto = ?";
            $stmtUpdateStock = $this->db->prepare($sqlUpdateStock);

            $sqlKardex = "INSERT INTO KARDEX (id_producto, id_usuario, fecha_movimiento_producto, tipo_movimiento_producto, cantidad_producto, stock_restante_producto, referencia_producto) 
                          VALUES (?, ?, GETDATE(), 'Salida', ?, ?, ?)";
            $stmtKardex = $this->db->prepare($sqlKardex);

            foreach ($detalles as $item) {
                $idProd = $item['id_producto'];
                $desc = $item['nombre'];
                $cant = $item['cantidad'];
                $precio = $item['precio'];
                $sub = $item['subtotal_fila'];
                $serie = !empty($item['imei']) ? trim($item['imei']) : null;

                $stmtD->execute([$idApartado, $idProd, $desc, $cant, $precio, $sub, $serie]);

                // Consultar y actualizar stock
                $stmtStock->execute([$idProd]);
                $stockRow = $stmtStock->fetch();
                $stockActual = $stockRow ? (int)$stockRow['stock_producto'] : 0;
                $stockNuevo = $stockActual - $cant;

                $stmtUpdateStock->execute([$stockNuevo, $idProd]);

                // Log a Kardex
                $refKardex = "Apartado #" . $idApartado;
                $stmtKardex->execute([$idProd, $idUsuario, $cant, $stockNuevo, $refKardex]);
            }

            // 3. Insertar abono inicial si aplica
            if ($abonoInicial > 0) {
                $sqlAbono = "INSERT INTO ABONOS_APARTADO (id_apartado, id_usuario, id_metodo_pago, fecha_abono, monto_abono, referencia_pago, banco_pago) 
                             VALUES (?, ?, ?, GETDATE(), ?, ?, ?)";
                
                $ref = !empty($referenciaPago) ? trim($referenciaPago) : null;
                $banco = (!empty($bancoPago) && $bancoPago !== 'Seleccione Banco...') ? trim($bancoPago) : null;
                
                $stmtAbono = $this->db->prepare($sqlAbono);
                $stmtAbono->execute([$idApartado, $idUsuario, $idMetodoPago, $abonoInicial, $ref, $banco]);
            }

            $this->db->commit();
            return $idApartado;

        } catch (Exception $e) {
            if ($this->db->inTransaction()) {
                $this->db->rollBack();
            }
            error_log("Error al crear apartado: " . $e->getMessage());
            return false;
        }
    }

    public function addAbono($idApartado, $idUsuario, $idMetodoPago, $monto, $referenciaPago, $bancoPago) {
        try {
            $this->db->beginTransaction();

            // 1. Obtener saldo pendiente
            $sqlCheck = "SELECT saldo_pendiente, total_apartado FROM APARTADOS WHERE id_apartado = ?";
            $stmtCheck = $this->db->prepare($sqlCheck);
            $stmtCheck->execute([$idApartado]);
            $row = $stmtCheck->fetch();

            if (!$row) {
                throw new Exception("Apartado no encontrado.");
            }

            $saldoActual = (float)$row['saldo_pendiente'];
            $saldoNuevo = $saldoActual - $monto;
            if ($saldoNuevo < 0) $saldoNuevo = 0.0;

            $estado = ($saldoNuevo <= 0) ? 'PAGADO' : 'VIGENTE';

            // 2. Registrar Abono
            $sqlAbono = "INSERT INTO ABONOS_APARTADO (id_apartado, id_usuario, id_metodo_pago, fecha_abono, monto_abono, referencia_pago, banco_pago) 
                         VALUES (?, ?, ?, GETDATE(), ?, ?, ?)";
            $ref = !empty($referenciaPago) ? trim($referenciaPago) : null;
            $banco = (!empty($bancoPago) && $bancoPago !== 'Seleccione Banco...') ? trim($bancoPago) : null;
            
            $stmtAbono = $this->db->prepare($sqlAbono);
            $stmtAbono->execute([$idApartado, $idUsuario, $idMetodoPago, $monto, $ref, $banco]);
            
            $idAbono = $this->db->lastInsertId();
            if (!$idAbono) {
                $idAbono = $this->db->query("SELECT SCOPE_IDENTITY() AS id")->fetch()['id'];
            }

            // 3. Actualizar Apartado
            $sqlUpdate = "UPDATE APARTADOS SET saldo_pendiente = ?, estado_apartado = ? WHERE id_apartado = ?";
            $stmtUpdate = $this->db->prepare($sqlUpdate);
            $stmtUpdate->execute([$saldoNuevo, $estado, $idApartado]);

            $this->db->commit();
            return $idAbono;

        } catch (Exception $e) {
            if ($this->db->inTransaction()) {
                $this->db->rollBack();
            }
            error_log("Error al agregar abono: " . $e->getMessage());
            return false;
        }
    }

    public function deliver($idApartado) {
        $sql = "UPDATE APARTADOS SET estado_apartado = 'ENTREGADO', fecha_entrega = GETDATE() WHERE id_apartado = ? AND estado_apartado = 'PAGADO'";
        $stmt = $this->db->prepare($sql);
        return $stmt->execute([$idApartado]);
    }

    public function cancel($idApartado, $idUsuario) {
        try {
            $this->db->beginTransaction();

            // 1. Obtener estado para validar
            $sqlCheck = "SELECT estado_apartado FROM APARTADOS WHERE id_apartado = ?";
            $stmtCheck = $this->db->prepare($sqlCheck);
            $stmtCheck->execute([$idApartado]);
            $row = $stmtCheck->fetch();

            if (!$row || $row['estado_apartado'] === 'CANCELADO' || $row['estado_apartado'] === 'ENTREGADO') {
                throw new Exception("El apartado no puede ser cancelado en su estado actual.");
            }

            // 2. Actualizar estado
            $sqlCancel = "UPDATE APARTADOS SET estado_apartado = 'CANCELADO' WHERE id_apartado = ?";
            $stmtCancel = $this->db->prepare($sqlCancel);
            $stmtCancel->execute([$idApartado]);

            // 3. Devolver productos al inventario
            $sqlItems = "SELECT id_producto, cantidad_apartado FROM DETALLES_APARTADO WHERE id_apartado = ?";
            $stmtItems = $this->db->prepare($sqlItems);
            $stmtItems->execute([$idApartado]);
            $items = $stmtItems->fetchAll();

            $sqlStock = "SELECT stock_producto FROM INVENTARIO WHERE id_producto = ?";
            $stmtStock = $this->db->prepare($sqlStock);

            $sqlUpdateStock = "UPDATE INVENTARIO SET stock_producto = ? WHERE id_producto = ?";
            $stmtUpdateStock = $this->db->prepare($sqlUpdateStock);

            $sqlKardex = "INSERT INTO KARDEX (id_producto, id_usuario, fecha_movimiento_producto, tipo_movimiento_producto, cantidad_producto, stock_restante_producto, referencia_producto) 
                          VALUES (?, ?, GETDATE(), 'Entrada', ?, ?, ?)";
            $stmtKardex = $this->db->prepare($sqlKardex);

            foreach ($items as $item) {
                $idProd = $item['id_producto'];
                $cant = $item['cantidad_apartado'];

                // Consultar stock actual
                $stmtStock->execute([$idProd]);
                $stockRow = $stmtStock->fetch();
                $stockActual = $stockRow ? (int)$stockRow['stock_producto'] : 0;
                $stockNuevo = $stockActual + $cant;

                // Devolver stock
                $stmtUpdateStock->execute([$stockNuevo, $idProd]);

                // Registrar en Kardex
                $refKardex = "Cancelación Apartado #" . $idApartado;
                $stmtKardex->execute([$idProd, $idUsuario, $cant, $stockNuevo, $refKardex]);
            }

            $this->db->commit();
            return true;

        } catch (Exception $e) {
            if ($this->db->inTransaction()) {
                $this->db->rollBack();
            }
            error_log("Error al cancelar apartado: " . $e->getMessage());
            return false;
        }
    }

    public function getAbono($idAbono) {
        $sql = "SELECT ab.*, a.total_apartado, a.saldo_pendiente, c.nombre_cliente, c.apellido_cliente, m.nombre_metodo, u.nombre_usuario 
                FROM ABONOS_APARTADO ab
                INNER JOIN APARTADOS a ON ab.id_apartado = a.id_apartado
                INNER JOIN CLIENTES c ON a.id_cliente_apartado = c.id_cliente
                INNER JOIN METODOS_PAGO m ON ab.id_metodo_pago = m.id_metodo_pago
                INNER JOIN USUARIOS u ON ab.id_usuario = u.id_usuario
                WHERE ab.id_abono = ?";
        $stmt = $this->db->prepare($sql);
        $stmt->execute([$idAbono]);
        return $stmt->fetch();
    }
}
