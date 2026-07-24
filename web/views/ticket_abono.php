<?php
session_start();
if (!isset($_SESSION['user_id'])) exit;

require_once dirname(__DIR__) . '/models/Layaway.php';
require_once dirname(__DIR__) . '/models/Company.php';

$idAbono = (int)($_GET['id_abono'] ?? 0);
if ($idAbono <= 0) {
    echo "ID de abono no válido.";
    exit;
}

$layawayModel = new Layaway();
$abono = $layawayModel->getAbono($idAbono);
if (!$abono) {
    echo "Abono no encontrado.";
    exit;
}

$companyModel = new Company();
$company = $companyModel->get();
?>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <title>Comprobante de Abono #<?php echo $abono['id_abono']; ?></title>
    <style>
        @page {
            size: 80mm auto;
            margin: 0;
        }
        body {
            font-family: 'Courier New', Courier, monospace;
            font-size: 12px;
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
        .meta-table {
            width: 100%;
            font-size: 11px;
            margin-bottom: 5px;
        }
        .meta-table td {
            padding: 2px 0;
        }
        .totals-table {
            width: 100%;
            margin-top: 10px;
        }
        .totals-table td {
            padding: 3px 0;
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

    <!-- Botón flotante para reimprimir manualmente -->
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
        <?php if (!empty($company['numero_telefono'])): ?>
            <span>Tel: <?php echo htmlspecialchars($company['numero_telefono']); ?></span><br>
        <?php endif; ?>
    </div>

    <div class="divider"></div>

    <div class="text-center bold" style="font-size: 13px;">
        COMPROBANTE DE ABONO<br>
        RECIBO Nº: AB-<?php echo str_pad($abono['id_abono'], 6, '0', STR_PAD_LEFT); ?>
    </div>

    <div class="divider"></div>

    <table class="meta-table">
        <tr>
            <td class="bold" style="width: 40%;">Apartado:</td>
            <td>#<?php echo $abono['id_apartado']; ?></td>
        </tr>
        <tr>
            <td class="bold">Fecha Abono:</td>
            <td><?php echo date('d/m/Y h:i A', strtotime($abono['fecha_abono'])); ?></td>
        </tr>
        <tr>
            <td class="bold">Cliente:</td>
            <td><?php echo htmlspecialchars($abono['nombre_cliente'] . ' ' . ($abono['apellido_cliente'] ?? '')); ?></td>
        </tr>
        <tr>
            <td class="bold">Atendido por:</td>
            <td><?php echo htmlspecialchars($abono['nombre_usuario']); ?></td>
        </tr>
    </table>

    <div class="divider"></div>

    <div style="font-size:13px; margin: 10px 0;">
        Método de Pago: <span class="bold"><?php echo htmlspecialchars($abono['nombre_metodo']); ?></span><br>
        <?php if ($abono['banco_pago']): ?>
            Banco: <span class="bold"><?php echo htmlspecialchars($abono['banco_pago']); ?></span><br>
        <?php endif; ?>
        <?php if ($abono['referencia_pago']): ?>
            Referencia: <span class="bold"><?php echo htmlspecialchars($abono['referencia_pago']); ?></span><br>
        <?php endif; ?>
    </div>

    <div style="background:#f2f2f2; padding: 10px; text-align:center; font-size:16px; border: 1px solid #000; border-radius: 4px;">
        Monto Abonado:<br>
        <span class="bold" style="font-size: 20px;">L <?php echo number_format($abono['monto_abono'], 2); ?></span>
    </div>

    <div class="divider"></div>

    <div class="bold">ESTADO DE CUENTA:</div>
    <table class="totals-table">
        <tr>
            <td>Total del Apartado:</td>
            <td class="text-right">L <?php echo number_format($abono['total_apartado'], 2); ?></td>
        </tr>
        <tr>
            <td>Monto Cancelado:</td>
            <td class="text-right">L <?php echo number_format($abono['total_apartado'] - $abono['saldo_pendiente'], 2); ?></td>
        </tr>
        <tr class="bold">
            <td style="font-size:13px;">SALDO PENDIENTE:</td>
            <td class="text-right" style="font-size:13px; border-top: 1px solid #000;">L <?php echo number_format($abono['saldo_pendiente'], 2); ?></td>
        </tr>
    </table>

    <div class="divider"></div>

    <div class="text-center" style="margin-top:25px; font-size: 10px;">
        <?php if (!empty($company['mensaje_ticket_pie_recibo'])): ?>
            <span><?php echo htmlspecialchars($company['mensaje_ticket_pie_recibo']); ?></span><br><br>
        <?php endif; ?>
        <span>¡Gracias por su abono!</span><br>
        <span>Exija su comprobante de pago.</span>
    </div>

    <div style="margin-top: 40px; text-align:center;">
        <span>__________________________</span><br>
        <span style="font-size: 9px; text-transform: uppercase;">Firma Cliente</span>
    </div>

</body>
</html>
