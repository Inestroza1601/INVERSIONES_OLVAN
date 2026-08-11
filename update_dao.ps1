 = Get-Content src/dao/InventarioDefectuosoDAO.java -Raw

$suffix = " AND ((? = 'INVERSIONES OLVAN (Empresa)' AND d.id_detalle_venta IS NULL) OR d.id_detalle_venta IN (SELECT v.id_detalle_venta FROM DETALLES_VENTA v LEFT JOIN VENTAS ve ON v.id_ventas = ve.id_ventas LEFT JOIN CLIENTES c ON ve.id_cliente_venta = c.id_cliente WHERE c.nombre_cliente = ? OR (? = 'INVERSIONES OLVAN (Empresa)' AND c.nombre_cliente IS NULL)))"

$content = $content -replace 'public List<Map<String, Object>> obtenerDetallesPorProductoYEstado\(int idProducto, String estadoDefecto\) \{', 'public List<Map<String, Object>> obtenerDetallesPorProductoYEstado(int idProducto, String estadoDefecto, String nombreCliente) {'
$content = $content -replace 'WHERE d.id_producto = \? AND d.estado_defecto = \? "\s*\+\s*"ORDER BY d.fecha_ingreso DESC";', "WHERE d.id_producto = ? AND d.estado_defecto = ? " + " " + "ORDER BY d.fecha_ingreso DESC";"

$content = $content -replace 'ps\.setString\(2, estadoDefecto\);', "ps.setString(2, estadoDefecto);
            ps.setString(3, nombreCliente);
            ps.setString(4, nombreCliente);
            ps.setString(5, nombreCliente);"

$content = $content -replace 'public boolean cambiarEstadoMermas\(int idProducto, String estadoActual, String nuevoEstado, int idUsuario, String kardexRef\) \{', 'public boolean cambiarEstadoMermas(int idProducto, String estadoActual, String nuevoEstado, int idUsuario, String kardexRef, String nombreCliente) {'
$content = $content -replace 'WHERE id_producto = \? AND estado_defecto = \?";', "WHERE id_producto = ? AND estado_defecto = ? " + ".replace("d.", "") + "";"
$content = $content -replace 'WHERE id_producto = \? AND estado_defecto = \?"\)\)\{', "WHERE id_producto = ? AND estado_defecto = ? " + ".replace("d.", "") + ")){"

$content = $content -replace 'psC\.setString\(2, estadoActual\);', "psC.setString(2, estadoActual);
                psC.setString(3, nombreCliente);
                psC.setString(4, nombreCliente);
                psC.setString(5, nombreCliente);"
$content = $content -replace 'psU\.setString\(3, estadoActual\);', "psU.setString(3, estadoActual);
                    psU.setString(4, nombreCliente);
                    psU.setString(5, nombreCliente);
                    psU.setString(6, nombreCliente);"

$content = $content -replace 'public boolean reingresarInventario\(int idProducto, String estadoActual, int idUsuario, String observacion\) throws java\.sql\.SQLException \{', 'public boolean reingresarInventario(int idProducto, String estadoActual, int idUsuario, String observacion, String nombreCliente) throws java.sql.SQLException {'
$content = $content -replace 'DELETE FROM INVENTARIO_DEFECTUOSO WHERE id_producto = \? AND estado_defecto = \?";', "DELETE FROM INVENTARIO_DEFECTUOSO WHERE id_producto = ? AND estado_defecto = ? " + ".replace("d.", "") + "";"

$content = $content -replace 'psD\.setString\(2, estadoActual\);', "psD.setString(2, estadoActual);
                    psD.setString(3, nombreCliente);
                    psD.setString(4, nombreCliente);
                    psD.setString(5, nombreCliente);"

$content = $content -replace 'public boolean entregarCliente\(int idProducto, String estadoActual\) \{', 'public boolean entregarCliente(int idProducto, String estadoActual, String nombreCliente) {'

Set-Content src/dao/InventarioDefectuosoDAO.java -Value $content
