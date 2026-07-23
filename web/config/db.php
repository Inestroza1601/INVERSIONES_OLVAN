<?php
date_default_timezone_set('America/Tegucigalpa');
class Db {
    private static $connection = null;

    public static function getConnection() {
        if (self::$connection === null) {
            // Valores por defecto
            $host = "170.80.140.2";
            $port = "6161";
            $database = "NexarBD";
            $username = "orionsys";
            $password = "123";

            // Intentar leer config.properties de la raíz
            $propsFile = dirname(__DIR__, 2) . '/config.properties';
            if (file_exists($propsFile)) {
                $lines = file($propsFile, FILE_IGNORE_NEW_LINES | FILE_SKIP_EMPTY_LINES);
                foreach ($lines as $line) {
                    if (strpos(trim($line), '#') === 0 || strpos(trim($line), ';') === 0) continue;
                    $parts = explode('=', $line, 2);
                    if (count($parts) === 2) {
                        $key = trim($parts[0]);
                        $value = trim($parts[1]);
                        if ($key === 'IP_SERVIDOR') $host = $value;
                        elseif ($key === 'PORT') $port = $value;
                        elseif ($key === 'BASE_DATOS') $database = $value;
                        elseif ($key === 'USUARIO') $username = $value;
                        elseif ($key === 'PASSWORD') $password = $value;
                    }
                }
            }

            try {
                // 1. Intentar pdo_sqlsrv (Nativo de SQL Server para Windows)
                $dsn = "sqlsrv:Server=$host,$port;Database=$database;Encrypt=true;TrustServerCertificate=true";
                self::$connection = new PDO($dsn, $username, $password, [
                    PDO::ATTR_ERRMODE => PDO::ERRMODE_EXCEPTION,
                    PDO::ATTR_DEFAULT_FETCH_MODE => PDO::FETCH_ASSOC,
                ]);
            } catch (PDOException $e) {
                try {
                    // 2. Intentar dblib (Para sistemas Linux/macOS)
                    $dsn = "dblib:host=$host:$port;dbname=$database";
                    self::$connection = new PDO($dsn, $username, $password, [
                        PDO::ATTR_ERRMODE => PDO::ERRMODE_EXCEPTION,
                        PDO::ATTR_DEFAULT_FETCH_MODE => PDO::FETCH_ASSOC,
                    ]);
                } catch (PDOException $e2) {
                    // 3. Mostrar traductor de errores en pantalla
                    header('Content-Type: text/html; charset=utf-8');
                    echo "<div style='font-family: sans-serif; padding: 25px; max-width: 600px; margin: 50px auto; border: 1px solid #f5c6cb; background-color: #f8d7da; color: #721c24; border-radius: 8px;'>";
                    echo "<h2 style='margin-top: 0; color: #721c24;'>Error al Conectar con la Base de Datos</h2>";
                    echo "<p><strong>ORION SYSTEMS</strong> no puede alcanzar el servidor de base de datos SQL Server.</p>";
                    echo "<hr style='border-top: 1px solid #f5c6cb;'>";
                    echo "<h3>Soluciones Sugeridas:</h3>";
                    echo "<ol style='padding-left: 20px;'>";
                    echo "<li>Asegúrate de que la extensión <code>php_pdo_sqlsrv</code> esté habilitada en tu archivo <code>php.ini</code>.</li>";
                    echo "<li>El servidor de base de datos en <strong>$host:$port</strong> está apagado o bloqueado por el Firewall.</li>";
                    echo "<li>Verifica las credenciales y la IP del servidor en el archivo <code>config.properties</code> de la raíz del sistema.</li>";
                    echo "</ol>";
                    echo "<p style='font-size: 11px; color: #6c757d; margin-bottom: 0;'>Detalle del error: " . htmlspecialchars($e->getMessage()) . "</p>";
                    echo "</div>";
                    exit;
                }
            }
        }
        return self::$connection;
    }
}
