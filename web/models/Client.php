<?php
require_once dirname(__DIR__) . '/config/db.php';

class Client {
    private $db;

    public function __construct() {
        $this->db = Db::getConnection();
    }

    public function listActive() {
        $sql = "SELECT * FROM CLIENTES WHERE estado_cliente = 1 ORDER BY nombre_cliente ASC";
        $stmt = $this->db->query($sql);
        return $stmt->fetchAll();
    }

    public function listAll() {
        $sql = "SELECT * FROM CLIENTES ORDER BY estado_cliente DESC, nombre_cliente ASC";
        $stmt = $this->db->query($sql);
        return $stmt->fetchAll();
    }

    public function register($identidad, $nombre, $apellido, $telefono, $correo) {
        $sql = "INSERT INTO CLIENTES (identidad_cliente, nombre_cliente, apellido_cliente, telefono_cliente, correo_cliente, estado_cliente) 
                VALUES (?, ?, ?, ?, ?, 1)";
        $stmt = $this->db->prepare($sql);
        return $stmt->execute([
            trim($identidad),
            trim($nombre),
            trim($apellido),
            trim($telefono),
            trim($correo)
        ]);
    }

    public function update($idCliente, $identidad, $nombre, $apellido, $telefono, $correo) {
        $sql = "UPDATE CLIENTES SET identidad_cliente = ?, nombre_cliente = ?, apellido_cliente = ?, 
                                    telefono_cliente = ?, correo_cliente = ? 
                WHERE id_cliente = ?";
        $stmt = $this->db->prepare($sql);
        return $stmt->execute([
            trim($identidad),
            trim($nombre),
            trim($apellido),
            trim($telefono),
            trim($correo),
            $idCliente
        ]);
    }

    public function deactivate($idCliente) {
        $sql = "UPDATE CLIENTES SET estado_cliente = 0 WHERE id_cliente = ?";
        $stmt = $this->db->prepare($sql);
        return $stmt->execute([$idCliente]);
    }

    public function activate($idCliente) {
        $sql = "UPDATE CLIENTES SET estado_cliente = 1 WHERE id_cliente = ?";
        $stmt = $this->db->prepare($sql);
        return $stmt->execute([$idCliente]);
    }

    public function existsIdentity($identidad, $excludeId = 0) {
        $sql = "SELECT id_cliente FROM CLIENTES WHERE identidad_cliente = ? AND id_cliente != ? AND estado_cliente = 1";
        $stmt = $this->db->prepare($sql);
        $stmt->execute([trim($identidad), $excludeId]);
        return $stmt->fetch() ? true : false;
    }
}
