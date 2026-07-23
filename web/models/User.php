<?php
require_once dirname(__DIR__) . '/config/db.php';

class User {
    private $db;

    public function __construct() {
        $this->db = Db::getConnection();
    }

    public static function hashSHA256($password) {
        return hash('sha256', $password);
    }

    public function authenticate($username, $password) {
        $hash = self::hashSHA256($password);
        $sql = "SELECT u.*, r.nombre_rol 
                FROM USUARIOS u 
                INNER JOIN ROLES_USUARIO r ON u.id_rol = r.id_rol 
                WHERE LOWER(u.nombre_usuario) = LOWER(?) AND u.password_hash = ? AND u.estado_usuario = 1";
        
        $stmt = $this->db->prepare($sql);
        $stmt->execute([$username, $hash]);
        return $stmt->fetch();
    }

    public function verifyPassword($userId, $password) {
        $hash = self::hashSHA256($password);
        $sql = "SELECT id_usuario FROM USUARIOS WHERE id_usuario = ? AND password_hash = ? AND estado_usuario = 1";
        $stmt = $this->db->prepare($sql);
        $stmt->execute([$userId, $hash]);
        return $stmt->fetch() ? true : false;
    }

    public function listAll() {
        $sql = "SELECT u.*, r.nombre_rol 
                FROM USUARIOS u 
                INNER JOIN ROLES_USUARIO r ON u.id_rol = r.id_rol 
                ORDER BY u.estado_usuario DESC, u.nombre_usuario ASC";
        $stmt = $this->db->query($sql);
        return $stmt->fetchAll();
    }

    public function listRoles() {
        $sql = "SELECT * FROM ROLES_USUARIO ORDER BY id_rol ASC";
        $stmt = $this->db->query($sql);
        return $stmt->fetchAll();
    }

    public function register($idRol, $username, $password) {
        $hash = self::hashSHA256($password);
        $sql = "INSERT INTO USUARIOS (id_rol, nombre_usuario, password_hash, estado_usuario) VALUES (?, ?, ?, 1)";
        $stmt = $this->db->prepare($sql);
        return $stmt->execute([$idRol, trim($username), $hash]);
    }

    public function update($idUsuario, $idRol, $username, $password = null) {
        if ($password !== null && trim($password) !== '') {
            $hash = self::hashSHA256($password);
            $sql = "UPDATE USUARIOS SET id_rol = ?, nombre_usuario = ?, password_hash = ? WHERE id_usuario = ?";
            $stmt = $this->db->prepare($sql);
            return $stmt->execute([$idRol, trim($username), $hash, $idUsuario]);
        } else {
            $sql = "UPDATE USUARIOS SET id_rol = ?, nombre_usuario = ? WHERE id_usuario = ?";
            $stmt = $this->db->prepare($sql);
            return $stmt->execute([$idRol, trim($username), $idUsuario]);
        }
    }

    public function deactivate($idUsuario) {
        $sql = "UPDATE USUARIOS SET estado_usuario = 0 WHERE id_usuario = ?";
        $stmt = $this->db->prepare($sql);
        return $stmt->execute([$idUsuario]);
    }

    public function activate($idUsuario) {
        $sql = "UPDATE USUARIOS SET estado_usuario = 1 WHERE id_usuario = ?";
        $stmt = $this->db->prepare($sql);
        return $stmt->execute([$idUsuario]);
    }
}
