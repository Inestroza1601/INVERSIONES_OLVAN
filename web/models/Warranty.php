<?php
require_once dirname(__DIR__) . '/config/db.php';

class Warranty {
    private $db;

    public function __construct() {
        $this->db = Db::getConnection();
    }

    public function listAll() {
        $sql = "SELECT v.id_ventas, v.id_cliente_venta, c.nombre_cliente, c.apellido_cliente, 
                       d.id_detalle_venta, d.descripcion_venta, d.identificador_serie, v.fecha_venta, 
                       d.dias_garantia, d.estado_garantia 
                FROM DETALLES_VENTA d 
                INNER JOIN VENTAS v ON d.id_ventas = v.id_ventas 
                LEFT JOIN CLIENTES c ON v.id_cliente_venta = c.id_cliente 
                WHERE d.dias_garantia > 0 
                ORDER BY v.fecha_venta DESC";
        
        $stmt = $this->db->query($sql);
        $rows = $stmt->fetchAll();
        
        $hoy = new DateTime();
        $list = [];

        foreach ($rows as $r) {
            $idVenta = $r['id_ventas'];
            $idCliente = $r['id_cliente_venta'];
            
            // Formatear cliente
            $nombreClie = trim($r['nombre_cliente']);
            if (!empty($r['apellido_cliente'])) {
                $nombreClie .= " " . trim($r['apellido_cliente']);
            }
            if ($idCliente == 1 || empty($nombreClie)) {
                $nombreClie = "CONSUMIDOR FINAL";
            }

            $serie = !empty($r['identificador_serie']) ? $r['identificador_serie'] : "N/A";
            
            // Calcular vencimiento
            $fechaVenta = new DateTime($r['fecha_venta']);
            $fechaVencimiento = clone $fechaVenta;
            $fechaVencimiento->modify("+" . $r['dias_garantia'] . " days");

            // Calcular estado
            $estadoBD = $r['estado_garantia'];
            $estado = "VENCIDA";
            if ($estadoBD === 'RECLAMADA') {
                $estado = "RECLAMADA";
            } elseif ($fechaVencimiento > $hoy) {
                $estado = "VIGENTE";
            }

            $list[] = [
                'id_venta' => $idVenta,
                'id_detalle_venta' => $r['id_detalle_venta'],
                'cliente' => $nombreClie,
                'producto' => $r['descripcion_venta'],
                'serie' => $serie,
                'fecha_venta' => $fechaVenta->format('d/MM/Y H:i'), // Formato amigable
                'fecha_venta_raw' => $r['fecha_venta'],
                'fecha_vencimiento' => $fechaVencimiento->format('d/m/Y'),
                'fecha_vencimiento_raw' => $fechaVencimiento->format('Y-m-d'),
                'dias_garantia' => $r['dias_garantia'],
                'estado' => $estado
            ];
        }

        return $list;
    }

    public function applyClaim($idDetalleVenta) {
        $sql = "UPDATE DETALLES_VENTA SET estado_garantia = 'RECLAMADA' WHERE id_detalle_venta = ?";
        $stmt = $this->db->prepare($sql);
        return $stmt->execute([$idDetalleVenta]);
    }
}
