<?php
require_once __DIR__ . '/web/config/db.php';
echo "VENTAS:\n";
$stmt = Db::getConnection()->query("SELECT COLUMN_NAME FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'VENTAS'");
print_r($stmt->fetchAll(PDO::FETCH_COLUMN));
