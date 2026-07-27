<?php
session_start();
if (!isset($_SESSION['user_id'])) {
    exit('Acceso denegado.');
}
require_once dirname(__DIR__) . '/models/Sale.php';
require_once dirname(__DIR__) . '/models/Company.php';

$idVenta = (int)($_GET['id_venta'] ?? 0);
if ($idVenta <= 0) {
    exit('ID de venta no válido.');
}

$saleModel = new Sale();
$companyModel = new Company();

$receipt = $saleModel->getReceipt($idVenta);
$company = $companyModel->getInfo();

if (!$receipt) {
    exit('No se encontraron registros de la venta.');
}

$venta = $receipt['venta'];
$detalles = $receipt['detalles'];

$companyName = $company ? $company['nombre_empresa'] : 'ORION SYSTEMS';
$companyRtn = $company ? $company['rtn_empresa'] : '';
$companyPhone = $company ? $company['numero_telefono'] : '';
$companyWhatsapp = $company ? $company['whatsapp_empresa'] : '';
$companyEmail = $company ? $company['email_empresa'] : '';
$companyAddress = $company ? $company['direccion_empresa'] : '';

// Determinar el pie de ticket según facturación habilitada o recibo simple
$esFactura = ($company && $company['habilitar_facturacion_empresa'] === 1);
$pieTicket = $esFactura ? ($company['mensaje_ticket_pie_factura'] ?? '') : ($company['mensaje_ticket_pie_recibo'] ?? '');
if (empty($pieTicket)) {
    $pieTicket = "¡Gracias por su compra! Conexión a tu Alcance";
}
?>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <title>Imprimir Ticket #<?php echo $idVenta; ?></title>
    <style>
        * {
            box-sizing: border-box;
            margin: 0;
            padding: 0;
        }
        body {
            font-family: 'Courier New', Courier, monospace;
            font-size: 12px;
            color: #000;
            background-color: #fff;
            padding: 10px;
            width: 80mm;
            margin: 0 auto;
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
        .logo-box {
            margin-bottom: 5px;
        }
        .logo-box img {
            max-width: 50px;
            height: auto;
        }
        .header-title {
            font-size: 16px;
            font-weight: bold;
            margin-bottom: 2px;
            text-transform: uppercase;
        }
        .info-lines {
            margin-bottom: 10px;
            font-size: 11px;
        }
        .divider {
            border-top: 1px dashed #000;
            margin: 8px 0;
        }
        .meta-table, .items-table {
            width: 100%;
            border-collapse: collapse;
            font-size: 11px;
        }
        .meta-table td {
            padding: 2px 0;
        }
        .items-table th {
            padding: 4px 0;
            border-bottom: 1px dashed #000;
            font-weight: bold;
            text-align: left;
        }
        .items-table td {
            padding: 6px 0;
            vertical-align: top;
        }
        .totals-box {
            width: 100%;
            margin-top: 8px;
            font-size: 11px;
        }
        .totals-row {
            display: flex;
            justify-content: space-between;
            padding: 2px 0;
        }
        .totals-row.grand-total {
            font-size: 13px;
            font-weight: bold;
            border-top: 1px dashed #000;
            padding-top: 4px;
        }
        .footer-msg {
            margin-top: 15px;
            font-size: 10px;
            text-align: center;
            line-height: 1.4;
        }
        .serial-text {
            font-size: 9px;
            color: #333;
            display: block;
            margin-top: 2px;
            padding-left: 5px;
        }
        
        /* Controles no imprimibles */
        .no-print-bar {
            position: fixed;
            top: 0;
            left: 0;
            width: 100%;
            background-color: #2d3748;
            padding: 10px;
            display: flex;
            justify-content: center;
            gap: 10px;
            z-index: 9999;
        }
        .btn-print, .btn-back {
            padding: 6px 14px;
            font-family: sans-serif;
            font-size: 12px;
            font-weight: bold;
            cursor: pointer;
            border: none;
            border-radius: 4px;
        }
        .btn-print {
            background-color: #38a169;
            color: #fff;
        }
        .btn-back {
            background-color: #e2e8f0;
            color: #2d3748;
        }
        
        @media print {
            .no-print-bar {
                display: none !important;
            }
            body {
                padding: 0;
                width: 100%;
            }
        }
    </style>
</head>
<body>

    <!-- Barra de Controles superior (no imprimible) -->
    <div class="no-print-bar">
        <button class="btn-print" onclick="window.print()">Imprimir Ticket</button>
        <button class="btn-back" onclick="window.close();">Cerrar Ventana</button>
    </div>
    
    <!-- Margen superior para compensar la barra flotante al visualizar -->
    <div style="height: 35px;" class="no-print-bar"></div>

    <!-- Ticket térmico -->
    <div class="text-center logo-box">
        <?php if ($company && !empty($company['logo_empresa_ruta'])): ?>
            <img src="../<?php echo htmlspecialchars($company['logo_empresa_ruta']); ?>" onerror="this.style.display='none'">
        <?php endif; ?>
    </div>
    
    <div class="text-center header-title">
        <?php echo htmlspecialchars($companyName); ?>
    </div>
    
    <div class="text-center info-lines">
        <?php if (!empty($companyRtn)): ?>RTN: <?php echo htmlspecialchars($companyRtn); ?><br><?php endif; ?>
        <?php if (!empty($companyPhone)): ?>Tel: <?php echo htmlspecialchars($companyPhone); ?><?php endif; ?>
        <?php if (!empty($companyWhatsapp)): ?> | WhatsApp: <?php echo htmlspecialchars($companyWhatsapp); ?><?php endif; ?><br>
        <?php if (!empty($companyEmail)): ?>Email: <?php echo htmlspecialchars($companyEmail); ?><br><?php endif; ?>
        <?php if (!empty($companyAddress)): ?><?php echo htmlspecialchars($companyAddress); ?><br><?php endif; ?>
    </div>

    <div class="divider"></div>

    <!-- Metadatos de la Venta -->
    <table class="meta-table">
        <tr>
            <td class="bold">Ticket Nº:</td>
            <td><?php echo $idVenta; ?></td>
        </tr>
        <tr>
            <td class="bold">Fecha:</td>
            <td><?php echo date('d/m/Y H:i', strtotime($venta['fecha_venta'])); ?></td>
        </tr>
        <tr>
            <td class="bold">Atendió:</td>
            <td><?php echo htmlspecialchars($venta['nombre_usuario'] ?? 'Sistema'); ?></td>
        </tr>
        <tr>
            <td class="bold">Cliente:</td>
            <td>
                <?php 
                $nombreC = trim($venta['nombre_cliente'] ?? '');
                if (!empty($venta['apellido_cliente'])) $nombreC .= ' ' . trim($venta['apellido_cliente']);
                echo htmlspecialchars(empty($nombreC) ? 'CONSUMIDOR FINAL' : $nombreC);
                ?>
            </td>
        </tr>
        <tr>
            <td class="bold">Met. Pago:</td>
            <td><?php echo htmlspecialchars($venta['nombre_metodo'] ?? 'Efectivo'); ?></td>
        </tr>
        <?php if (!empty($venta['referencia_pago'])): ?>
        <tr>
            <td class="bold">Ref/Vouch:</td>
            <td><?php echo htmlspecialchars($venta['referencia_pago']); ?></td>
        </tr>
        <?php endif; ?>
        <?php if (!empty($venta['banco_pago'])): ?>
        <tr>
            <td class="bold">Banco:</td>
            <td><?php echo htmlspecialchars($venta['banco_pago']); ?></td>
        </tr>
        <?php endif; ?>
    </table>

    <div class="divider"></div>

    <!-- Items -->
    <table class="items-table">
        <thead>
            <tr>
                <th>Descripción</th>
                <th class="text-center" style="width: 40px;">Cant</th>
                <th class="text-right" style="width: 70px;">Total</th>
            </tr>
        </thead>
        <tbody>
            <?php foreach ($detalles as $d): ?>
            <tr>
                <td>
                    <?php echo htmlspecialchars($d['descripcion_venta']); ?>
                    <?php if (!empty($d['identificador_serie'])): ?>
                        <span class="serial-text">S/N: <?php echo htmlspecialchars($d['identificador_serie']); ?></span>
                    <?php endif; ?>
                    <?php if ($d['dias_garantia'] > 0): ?>
                        <span class="serial-text">Garantía: <?php echo $d['dias_garantia']; ?> días</span>
                    <?php endif; ?>
                </td>
                <td class="text-center"><?php echo $d['cantidad_venta']; ?></td>
                <td class="text-right">L <?php echo number_format($d['subtotal_venta'], 2); ?></td>
            </tr>
            <?php endforeach; ?>
        </tbody>
    </table>

    <div class="divider"></div>

    <!-- Totales -->
    <div class="totals-box">
        <div class="totals-row">
            <span>Subtotal:</span>
            <span>L <?php echo number_format($venta['subtotal_venta'], 2); ?></span>
        </div>
        <div class="totals-row">
            <span>Impuesto (15%):</span>
            <span>L <?php echo number_format($venta['impuesto_venta'], 2); ?></span>
        </div>
        <div class="totals-row grand-total">
            <span>TOTAL A PAGAR:</span>
            <span>L <?php echo number_format($venta['total_venta'], 2); ?></span>
        </div>
    </div>

    <!-- Mensaje final / Pie -->
    <div class="footer-msg">
        <?php echo nl2br(htmlspecialchars($pieTicket)); ?>
        <br><br>
        Generado por Orion Systems Web Portal
    </div>

    <script>
        // Auto trigger window print upon load
        window.addEventListener('load', () => {
            setTimeout(() => {
                window.print();
            }, 500);
        });
    </script>
</body>
</html>
