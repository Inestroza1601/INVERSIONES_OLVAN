<?php
session_start();
if (!isset($_SESSION['user_id'])) exit;

require_once dirname(__DIR__) . '/models/CashControl.php';
require_once dirname(__DIR__) . '/models/Company.php';

$idCaja = (int)($_GET['id_caja'] ?? 0);
if ($idCaja <= 0) {
    echo "ID de caja no válido.";
    exit;
}

$cashModel = new CashControl();
$calcs = $cashModel->getSessionCalculations($idCaja);

if (!$calcs) {
    echo "Arqueo de caja no encontrado.";
    exit;
}

$caja = $calcs['caja'];
$metodos = $calcs['metodos'];
$productos = $calcs['productos_vendidos'];

$companyModel = new Company();
$company = $companyModel->get();
?>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <title>Reporte de Caja #<?php echo $caja['id_caja']; ?></title>
    <style>
        @page {
            size: 80mm auto;
            margin: 0;
        }
        body {
            font-family: 'Courier New', Courier, monospace;
            font-size: 11px;
            width: 72mm;
            margin: 0 auto;
            padding: 5mm 0;
            color: #000;
            background: #fff;
        }
        .text-center {
            text-align: center;
        }
        .text-right {
            text-align: right;
        }
        .bold {
            font-weight: bold;
        }
        .divider {
            border-top: 1px dashed #000;
            margin: 8px 0;
        }
        .header-logo {
            max-width: 45mm;
            max-height: 25mm;
            display: block;
            margin: 0 auto 5px auto;
            object-fit: contain;
        }
        .meta-table, .report-table {
            width: 100%;
            font-size: 11px;
            margin-bottom: 5px;
            border-collapse: collapse;
        }
        .meta-table td, .report-table td, .report-table th {
            padding: 2px 0;
        }
        .report-table th {
            border-bottom: 1px dashed #000;
            text-align: left;
        }
        @media print {
            body {
                width: 72mm;
                margin: 0 auto;
                padding: 0;
            }
            .no-print {
                display: none;
            }
        }
    </style>
</head>
<body onload="window.print()">

    <div class="no-print" style="position:fixed; top:10px; right:10px; z-index:9999;">
        <button onclick="window.print()" style="padding:8px 15px; background:#27ae60; color:#fff; border:none; border-radius:4px; font-weight:bold; cursor:pointer;">Imprimir</button>
    </div>

    <div class="text-center">
        <?php if (!empty($company['logo_empresa_ruta'])): ?>
            <img class="header-logo" src="<?php echo '../' . ltrim($company['logo_empresa_ruta'], './'); ?>" alt="Logo">
        <?php endif; ?>
        
        <span class="bold" style="font-size: 14px;"><?php echo htmlspecialchars($company['nombre_empresa']); ?></span><br>
        <?php if (!empty($company['rtn_empresa'])): ?>
            <span>RTN: <?php echo htmlspecialchars($company['rtn_empresa']); ?></span><br>
        <?php endif; ?>
        <?php if (!empty($company['direccion_empresa'])): ?>
            <span style="font-size:10px;"><?php echo htmlspecialchars($company['direccion_empresa']); ?></span><br>
        <?php endif; ?>
    </div>

    <div class="divider"></div>

    <div class="text-center bold" style="font-size: 12px;">
        REPORTE DE ARQUEO Y CIERRE DE CAJA<br>
        TURNO Nº: CQ-<?php echo str_pad($caja['id_caja'], 6, '0', STR_PAD_LEFT); ?>
    </div>

    <div class="divider"></div>

    <table class="meta-table">
        <tr>
            <td class="bold" style="width: 45%;">Cajero:</td>
            <td><?php echo htmlspecialchars(!empty($caja['cajero_turno']) ? $caja['cajero_turno'] : ($caja['nombre_usuario'] ?? 'Sistema')); ?></td>
        </tr>
        <tr>
            <td class="bold">Apertura:</td>
            <td><?php echo date('d/m/Y h:i A', strtotime($caja['fecha_apertura'])); ?></td>
        </tr>
        <tr>
            <td class="bold">Cierre:</td>
            <td><?php echo $caja['fecha_cierre'] ? date('d/m/Y h:i A', strtotime($caja['fecha_cierre'])) : 'Turno Abierto'; ?></td>
        </tr>
        <tr>
            <td class="bold">Estado:</td>
            <td class="bold"><?php echo htmlspecialchars($caja['estado_caja']); ?></td>
        </tr>
    </table>

    <div class="divider"></div>

    <div class="bold" style="margin-bottom: 5px;">RESUMEN DE EFECTIVO GAVETA</div>
    <table class="meta-table">
        <tr>
            <td>(+) Efectivo Inicial:</td>
            <td class="text-right">L <?php echo number_format($caja['monto_apertura'], 2); ?></td>
        </tr>
        <tr>
            <td>(+) Ventas en Efectivo:</td>
            <?php 
                $efectivoVentas = 0;
                $efectivoAbonos = 0;
                foreach ($metodos as $m) {
                    if (stripos($m['nombre_metodo'], 'efectivo') !== false) {
                        $efectivoVentas = $m['total_ventas'];
                        $efectivoAbonos = $m['total_abonos'];
                    }
                }
            ?>
            <td class="text-right">L <?php echo number_format($efectivoVentas, 2); ?></td>
        </tr>
        <tr>
            <td>(+) Abonos en Efectivo:</td>
            <td class="text-right">L <?php echo number_format($efectivoAbonos, 2); ?></td>
        </tr>
        <tr class="bold">
            <td style="border-top:1px dashed #000; padding-top:4px;">(=) EFECTIVO ESPERADO:</td>
            <td class="text-right" style="border-top:1px dashed #000; padding-top:4px;">L <?php echo number_format($calcs['efectivo_esperado'], 2); ?></td>
        </tr>
        <?php if ($caja['estado_caja'] === 'CERRADA'): ?>
        <tr>
            <td>(=) Efectivo Contado Real:</td>
            <td class="text-right">L <?php echo number_format($caja['monto_cierre_real'], 2); ?></td>
        </tr>
        <tr class="bold" style="font-size:12px;">
            <td>DIFERENCIA:</td>
            <td class="text-right">
                L <?php 
                    $diff = (float)$caja['diferencia_caja'];
                    echo number_format($diff, 2); 
                    if ($diff === 0.0) echo " (OK)";
                    elseif ($diff > 0) echo " (SOB)";
                    else echo " (FAL)";
                ?>
            </td>
        </tr>
        <?php endif; ?>
    </table>

    <div class="divider"></div>

    <div class="bold" style="margin-bottom: 5px;">INGRESOS POR MÉTODO DE PAGO</div>
    <table class="report-table">
        <thead>
            <tr>
                <th>Método</th>
                <th class="text-right">Ventas</th>
                <th class="text-right">Abonos</th>
                <th class="text-right">Total</th>
            </tr>
        </thead>
        <tbody>
            <?php foreach ($metodos as $m): ?>
                <tr>
                    <td><?php echo htmlspecialchars($m['nombre_metodo']); ?></td>
                    <td class="text-right"><?php echo number_format($m['total_ventas'], 2); ?></td>
                    <td class="text-right"><?php echo number_format($m['total_abonos'], 2); ?></td>
                    <td class="text-right bold"><?php echo number_format($m['total_general'], 2); ?></td>
                </tr>
            <?php endforeach; ?>
        </tbody>
    </table>

    <div class="divider"></div>

    <div class="bold" style="margin-bottom: 5px;">DESGLOSE DE PRODUCTOS VENDIDOS</div>
    <table class="report-table">
        <thead>
            <tr>
                <th>Descripción</th>
                <th class="text-center" style="width: 30px;">Cant</th>
                <th class="text-right" style="width: 60px;">Total</th>
            </tr>
        </thead>
        <tbody>
            <?php if (count($productos) === 0): ?>
                <tr>
                    <td colspan="3" class="text-center" style="padding:10px 0;">No se registraron productos vendidos.</td>
                </tr>
            <?php else: ?>
                <?php foreach ($productos as $p): ?>
                    <tr>
                        <td><?php echo htmlspecialchars($p['descripcion_venta']); ?></td>
                        <td class="text-center bold"><?php echo $p['cantidad_vendida']; ?></td>
                        <td class="text-right">L<?php echo number_format($p['total_valor'], 0); ?></td>
                    </tr>
                <?php endforeach; ?>
            <?php endif; ?>
        </tbody>
    </table>

    <?php if ($caja['observaciones']): ?>
        <div class="divider"></div>
        <div style="font-size:10px; font-style:italic;">
            <strong>Observaciones:</strong><br>
            <?php echo htmlspecialchars($caja['observaciones']); ?>
        </div>
    <?php endif; ?>

    <div class="divider"></div>

    <div style="margin-top: 35px; text-align:center; font-size:10px;">
        <span>__________________________</span><br>
        <span style="font-size: 8px;">Firma del Cajero</span>
    </div>

    <div style="margin-top: 35px; text-align:center; font-size:10px;">
        <span>__________________________</span><br>
        <span style="font-size: 8px;">Firma de Auditoría / Propietario</span>
    </div>

    <div class="text-center" style="margin-top: 20px; font-size: 9px; color: #555;">
        <span>Reporte Impreso el: <?php echo date('d/m/Y h:i A'); ?></span>
    </div>

</body>
</html>
