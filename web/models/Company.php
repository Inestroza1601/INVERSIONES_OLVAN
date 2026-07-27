<?php
require_once dirname(__DIR__) . '/config/db.php';

class Company {
    private $db;

    public function __construct() {
        $this->db = Db::getConnection();
    }

    public function getInfo() {
        $sql = "SELECT TOP 1 * FROM EMPRESA ORDER BY id_empresa ASC";
        $stmt = $this->db->query($sql);
        return $stmt->fetch();
    }

    public function get() {
        return $this->getInfo();
    }

    public function save($data) {
        $existing = $this->getInfo();
        
        $nombre = trim($data['nombre_empresa'] ?? '');
        $rtn = trim($data['rtn_empresa'] ?? '');
        $dueno = trim($data['dueno_empresa'] ?? '');
        $dir = trim($data['direccion_empresa'] ?? '');
        $estado = isset($data['estado_empresa']) && $data['estado_empresa'] ? 1 : 0;
        $facturacion = isset($data['habilitar_facturacion_empresa']) && $data['habilitar_facturacion_empresa'] ? 1 : 0;
        $tel = trim($data['numero_telefono'] ?? '');
        $telSec = trim($data['telefono_secundario'] ?? '');
        $whatsapp = trim($data['whatsapp_empresa'] ?? '');
        $email = trim($data['email_empresa'] ?? '');
        $web = trim($data['web_empresa'] ?? '');
        $fb = trim($data['facebook_empresa'] ?? '');
        $pieFactura = trim($data['mensaje_ticket_pie_factura'] ?? '');
        $pieRecibo = trim($data['mensaje_ticket_pie_recibo'] ?? '');
        $pieEntrega = trim($data['mensaje_ticket_entrega'] ?? '');
        $pieCotiz = trim($data['mensaje_ticket_pie_cotizacion'] ?? '');
        $logo = trim($data['logo_empresa_ruta'] ?? '');

        if ($existing) {
            // UPDATE
            $sql = "UPDATE EMPRESA SET 
                        nombre_empresa = ?, rtn_empresa = ?, dueño_empresa = ?, direccion_empresa = ?, 
                        estado_empresa = ?, habilitar_facturacion_empresa = ?, numero_telefono = ?, 
                        telefono_secundario = ?, whatsapp_empresa = ?, email_empresa = ?, web_empresa = ?, 
                        facebook_empresa = ?, mensaje_ticket_pie_factura = ?, mensaje_ticket_pie_recibo = ?, 
                        mensaje_ticket_entrega = ?, mensaje_ticket_pie_cotizacion = ?, logo_empresa_ruta = ?
                    WHERE id_empresa = ?";
            $stmt = $this->db->prepare($sql);
            return $stmt->execute([
                $nombre, $rtn, $dueno, $dir, $estado, $facturacion, $tel, $telSec, $whatsapp, $email, $web, $fb,
                $pieFactura, $pieRecibo, $pieEntrega, $pieCotiz, $logo, $existing['id_empresa']
            ]);
        } else {
            // INSERT
            $sql = "INSERT INTO EMPRESA (
                        nombre_empresa, rtn_empresa, dueño_empresa, direccion_empresa, 
                        estado_empresa, habilitar_facturacion_empresa, numero_telefono, 
                        telefono_secundario, whatsapp_empresa, email_empresa, web_empresa, 
                        facebook_empresa, mensaje_ticket_pie_factura, mensaje_ticket_pie_recibo, 
                        mensaje_ticket_entrega, mensaje_ticket_pie_cotizacion, logo_empresa_ruta
                    ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            $stmt = $this->db->prepare($sql);
            return $stmt->execute([
                $nombre, $rtn, $dueno, $dir, $estado, $facturacion, $tel, $telSec, $whatsapp, $email, $web, $fb,
                $pieFactura, $pieRecibo, $pieEntrega, $pieCotiz, $logo
            ]);
        }
    }
}
