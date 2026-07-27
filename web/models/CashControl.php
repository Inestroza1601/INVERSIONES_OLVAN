<?php
require_once dirname(__DIR__) . '/config/db.php';

class CashControl {
    private $db;

    public function __construct() {
        $this->db = Db::getConnection();
    }

    public function getActiveSession() {
        $sql = "SELECT c.*, u.nombre_usuario 
                FROM CONTROL_CAJA c
                LEFT JOIN USUARIOS u ON c.id_usuario_apertura = u.id_usuario
                WHERE c.estado_caja = 'ABIERTA'";
        $stmt = $this->db->query($sql);
        return $stmt->fetch();
    }

    public function openSession($idUsuario, $montoApertura, $cajeroTurno = '') {
        // Validar que no haya una caja abierta ya
        $active = $this->getActiveSession();
        if ($active) {
            return false;
        }

        $sql = "INSERT INTO CONTROL_CAJA (id_usuario_apertura, fecha_apertura, monto_apertura, estado_caja, cajero_turno) 
                VALUES (?, GETDATE(), ?, 'ABIERTA', ?)";
        $stmt = $this->db->prepare($sql);
        return $stmt->execute([$idUsuario, $montoApertura, trim($cajeroTurno)]);
    }

    public function listAll() {
        $sql = "SELECT c.*, u.nombre_usuario 
                FROM CONTROL_CAJA c
                LEFT JOIN USUARIOS u ON c.id_usuario_apertura = u.id_usuario
                ORDER BY c.fecha_apertura DESC";
        $stmt = $this->db->query($sql);
        return $stmt->fetchAll();
    }

    public function getSessionCalculations($idCaja) {
        $sql = "SELECT c.*, u.nombre_usuario 
                FROM CONTROL_CAJA c
                LEFT JOIN USUARIOS u ON c.id_usuario_apertura = u.id_usuario
                WHERE c.id_caja = ?";
        $stmt = $this->db->prepare($sql);
        $stmt->execute([$idCaja]);
        $caja = $stmt->fetch();

        if (!$caja) return null;

        $fechaInicio = $caja['fecha_apertura'];
        $fechaFin = $caja['fecha_cierre'] ? $caja['fecha_cierre'] : date('Y-m-d H:i:s');

        // Obtener el ID del método de pago de efectivo
        $sqlCashId = "SELECT id_metodo_pago FROM METODOS_PAGO WHERE nombre_metodo LIKE '%efectivo%'";
        $cashIdRow = $this->db->query($sqlCashId)->fetch();
        $cashMethodId = $cashIdRow ? (int)$cashIdRow['id_metodo_pago'] : 1;

        // 1. Ventas por método de pago
        $sqlSales = "SELECT id_metodo_pago, SUM(total_venta) as total_metodo, COUNT(id_ventas) as transacciones 
                     FROM VENTAS 
                     WHERE fecha_venta >= ? AND fecha_venta <= ?
                     GROUP BY id_metodo_pago";
        $stmtS = $this->db->prepare($sqlSales);
        $stmtS->execute([$fechaInicio, $fechaFin]);
        $ventasRows = $stmtS->fetchAll();

        // 2. Abonos por método de pago
        $sqlAbonos = "SELECT id_metodo_pago, SUM(monto_abono) as total_metodo, COUNT(id_abono) as transacciones 
                      FROM ABONOS_APARTADO 
                      WHERE fecha_abono >= ? AND fecha_abono <= ?
                      GROUP BY id_metodo_pago";
        $stmtA = $this->db->prepare($sqlAbonos);
        $stmtA->execute([$fechaInicio, $fechaFin]);
        $abonosRows = $stmtA->fetchAll();

        // Cruzar y agrupar por método de pago
        $sqlMethods = "SELECT id_metodo_pago, nombre_metodo FROM METODOS_PAGO";
        $methods = $this->db->query($sqlMethods)->fetchAll();

        $reportMethods = [];
        $totalSalesCash = 0.0;
        $totalAbonosCash = 0.0;
        
        $totalSalesAll = 0.0;
        $totalAbonosAll = 0.0;

        foreach ($methods as $m) {
            $mId = (int)$m['id_metodo_pago'];
            
            $saleData = array_values(array_filter($ventasRows, fn($x) => (int)$x['id_metodo_pago'] === $mId));
            $abonoData = array_values(array_filter($abonosRows, fn($x) => (int)$x['id_metodo_pago'] === $mId));

            $salesTotal = $saleData ? (float)$saleData[0]['total_metodo'] : 0.0;
            $abonosTotal = $abonoData ? (float)$abonoData[0]['total_metodo'] : 0.0;
            
            $salesCount = $saleData ? (int)$saleData[0]['transacciones'] : 0;
            $abonosCount = $abonoData ? (int)$abonoData[0]['transacciones'] : 0;

            $totalSalesAll += $salesTotal;
            $totalAbonosAll += $abonosTotal;

            if ($mId === $cashMethodId) {
                $totalSalesCash = $salesTotal;
                $totalAbonosCash = $abonosTotal;
            }

            $reportMethods[] = [
                'id_metodo' => $mId,
                'nombre_metodo' => $m['nombre_metodo'],
                'total_ventas' => $salesTotal,
                'ventas_count' => $salesCount,
                'total_abonos' => $abonosTotal,
                'abonos_count' => $abonosCount,
                'total_general' => $salesTotal + $abonosTotal
            ];
        }

        // Dinero esperado en efectivo = Inicial + Ventas Efectivo + Abonos Efectivo
        $montoEsperadoEfectivo = (float)$caja['monto_apertura'] + $totalSalesCash + $totalAbonosCash;

        // Productos vendidos en este turno
        $sqlProducts = "SELECT p.codigo_barras_producto, d.descripcion_venta, SUM(d.cantidad_venta) as cantidad_vendida, SUM(d.subtotal_venta) as total_valor
                        FROM DETALLES_VENTA d
                        INNER JOIN VENTAS v ON d.id_ventas = v.id_ventas
                        LEFT JOIN INVENTARIO p ON d.id_producto = p.id_producto
                        WHERE v.fecha_venta >= ? AND v.fecha_venta <= ?
                        GROUP BY p.codigo_barras_producto, d.descripcion_venta
                        ORDER BY cantidad_vendida DESC";
        $stmtP = $this->db->prepare($sqlProducts);
        $stmtP->execute([$fechaInicio, $fechaFin]);
        $productosVendidos = $stmtP->fetchAll();

        return [
            'caja' => $caja,
            'metodos' => $reportMethods,
            'efectivo_esperado' => $montoEsperadoEfectivo,
            'total_ventas_general' => $totalSalesAll,
            'total_abonos_general' => $totalAbonosAll,
            'productos_vendidos' => $productosVendidos
        ];
    }

    public function closeSession($idCaja, $montoReal, $observaciones) {
        try {
            $this->db->beginTransaction();

            $calcs = $this->getSessionCalculations($idCaja);
            if (!$calcs) {
                throw new Exception("Arqueo de caja no encontrado.");
            }

            $esperado = (float)$calcs['efectivo_esperado'];
            $diferencia = $montoReal - $esperado;

            $sql = "UPDATE CONTROL_CAJA 
                    SET fecha_cierre = GETDATE(), 
                        monto_cierre_esperado = ?, 
                        monto_cierre_real = ?, 
                        diferencia_caja = ?, 
                        estado_caja = 'CERRADA', 
                        observaciones = ? 
                    WHERE id_caja = ? AND estado_caja = 'ABIERTA'";
            $stmt = $this->db->prepare($sql);
            $stmt->execute([$esperado, $montoReal, $diferencia, trim($observaciones), $idCaja]);

            $this->db->commit();
            return true;
        } catch (Exception $e) {
            if ($this->db->inTransaction()) {
                $this->db->rollBack();
            }
            error_log("Error al cerrar caja: " . $e->getMessage());
            return false;
        }
    }

    public function getLowStockProducts() {
        $sql = "SELECT p.*, c.nombre_categoria, u.nombre_ubicacion 
                FROM INVENTARIO p
                LEFT JOIN CATEGORIAS c ON p.id_categoria = c.id_categoria
                LEFT JOIN UBICACIONES u ON p.id_ubicacion = u.id_ubicacion
                WHERE p.eliminado_producto = 0 AND p.stock_producto <= p.stock_minimo_producto
                ORDER BY p.stock_producto ASC";
        $stmt = $this->db->query($sql);
        return $stmt->fetchAll();
    }
}
