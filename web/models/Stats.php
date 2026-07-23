<?php
require_once dirname(__DIR__) . '/config/db.php';

class Stats {
    private $db;

    public function __construct() {
        $this->db = Db::getConnection();
    }

    public function getMetrics($filtro) {
        $totalActual = 0.0;
        $totalAnterior = 0.0;
        $ticketProm = 0.0;
        $topProducto = "Sin registros";

        $condicionActual = "";
        $condicionAnterior = "";

        switch ($filtro) {
            case "Día":
                $condicionActual = "CAST(fecha_venta AS DATE) = CAST(GETDATE() AS DATE)";
                $condicionAnterior = "CAST(fecha_venta AS DATE) = CAST(DATEADD(day, -1, GETDATE()) AS DATE)";
                break;
            case "Semana":
                $condicionActual = "DATEPART(week, fecha_venta) = DATEPART(week, GETDATE()) AND YEAR(fecha_venta) = YEAR(GETDATE())";
                $condicionAnterior = "DATEPART(week, fecha_venta) = DATEPART(week, DATEADD(week, -1, GETDATE())) AND YEAR(fecha_venta) = YEAR(DATEADD(week, -1, GETDATE()))";
                break;
            case "Mes":
                $condicionActual = "MONTH(fecha_venta) = MONTH(GETDATE()) AND YEAR(fecha_venta) = YEAR(GETDATE())";
                $condicionAnterior = "MONTH(fecha_venta) = MONTH(DATEADD(month, -1, GETDATE())) AND YEAR(fecha_venta) = YEAR(DATEADD(month, -1, GETDATE()))";
                break;
            case "Año":
                $condicionActual = "YEAR(fecha_venta) = YEAR(GETDATE())";
                $condicionAnterior = "YEAR(fecha_venta) = YEAR(GETDATE()) - 1";
                break;
            default:
                $condicionActual = "MONTH(fecha_venta) = MONTH(GETDATE()) AND YEAR(fecha_venta) = YEAR(GETDATE())";
                $condicionAnterior = "MONTH(fecha_venta) = MONTH(DATEADD(month, -1, GETDATE())) AND YEAR(fecha_venta) = YEAR(DATEADD(month, -1, GETDATE()))";
                break;
        }

        try {
            // 1. Extraer Total Actual y Ticket Promedio
            $sqlActual = "SELECT SUM(total_venta) as total, AVG(total_venta) as promedio FROM VENTAS WHERE $condicionActual";
            $stmt = $this->db->query($sqlActual);
            $resActual = $stmt->fetch();
            if ($resActual) {
                $totalActual = (float)($resActual['total'] ?? 0.0);
                $ticketProm = (float)($resActual['promedio'] ?? 0.0);
            }

            // 2. Extraer Total Período Anterior
            $sqlAnterior = "SELECT SUM(total_venta) as total FROM VENTAS WHERE $condicionAnterior";
            $stmt = $this->db->query($sqlAnterior);
            $resAnterior = $stmt->fetch();
            if ($resAnterior) {
                $totalAnterior = (float)($resAnterior['total'] ?? 0.0);
            }

            // 3. Extraer Top Producto cruzando Detalles y Ventas
            $sqlTop = "SELECT TOP 1 d.descripcion_venta, COUNT(*) as total_compras 
                       FROM DETALLES_VENTA d 
                       INNER JOIN VENTAS v ON d.id_ventas = v.id_ventas 
                       WHERE $condicionActual 
                       GROUP BY d.descripcion_venta 
                       ORDER BY COUNT(*) DESC";
            
            $stmt = $this->db->query($sqlTop);
            $resTop = $stmt->fetch();
            if ($resTop) {
                $topProducto = $resTop['descripcion_venta'];
            }
        } catch (Exception $e) {
            error_log("Error al obtener estadísticas: " . $e->getMessage());
        }

        return [
            'totalActual' => $totalActual,
            'totalAnterior' => $totalAnterior,
            'ticketProm' => $ticketProm,
            'topProducto' => $topProducto
        ];
    }

    public function getChartData() {
        // Obtener ventas diarias de los últimos 7 días
        $sql7Days = "SELECT CAST(fecha_venta AS DATE) as fecha, SUM(total_venta) as total
                     FROM VENTAS 
                     WHERE fecha_venta >= DATEADD(day, -6, CAST(GETDATE() AS DATE))
                     GROUP BY CAST(fecha_venta AS DATE)
                     ORDER BY CAST(fecha_venta AS DATE) ASC";
        
        // Ventas mensuales del año actual
        $sqlMonths = "SELECT MONTH(fecha_venta) as mes, SUM(total_venta) as total
                      FROM VENTAS 
                      WHERE YEAR(fecha_venta) = YEAR(GETDATE())
                      GROUP BY MONTH(fecha_venta)
                      ORDER BY MONTH(fecha_venta) ASC";

        $days = [];
        $months = [];

        try {
            $stmt = $this->db->query($sql7Days);
            $days = $stmt->fetchAll();

            $stmt = $this->db->query($sqlMonths);
            $months = $stmt->fetchAll();
        } catch (Exception $e) {
            error_log("Error al obtener datos de gráficas: " . $e->getMessage());
        }

        return [
            'days' => $days,
            'months' => $months
        ];
    }
}
