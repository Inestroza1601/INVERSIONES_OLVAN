-- Script de migracion generado de SQL Server a MySQL
SET FOREIGN_KEY_CHECKS=0;

SET FOREIGN_KEY_CHECKS=0;

DROP TABLE IF EXISTS `ABONOS_APARTADO`;
CREATE TABLE `ABONOS_APARTADO` (
  `id_abono` INT NOT NULL AUTO_INCREMENT,
  `id_apartado` INT NOT NULL,
  `id_usuario` INT NOT NULL,
  `id_metodo_pago` INT NOT NULL,
  `fecha_abono` DATETIME NOT NULL,
  `monto_abono` DECIMAL(18,4) NOT NULL,
  `referencia_pago` VARCHAR(100),
  `banco_pago` VARCHAR(100),
  `total_historico` DECIMAL(10,2) NOT NULL,
  `saldo_historico` DECIMAL(10,2) NOT NULL,
  PRIMARY KEY (`id_abono`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


DROP TABLE IF EXISTS `APARTADOS`;
CREATE TABLE `APARTADOS` (
  `id_apartado` INT NOT NULL AUTO_INCREMENT,
  `id_cliente_apartado` INT NOT NULL,
  `id_usuario` INT NOT NULL,
  `fecha_apartado` DATETIME NOT NULL,
  `total_apartado` DECIMAL(18,4) NOT NULL,
  `saldo_pendiente` DECIMAL(18,4) NOT NULL,
  `estado_apartado` VARCHAR(20) NOT NULL,
  `fecha_entrega` DATETIME,
  PRIMARY KEY (`id_apartado`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


DROP TABLE IF EXISTS `CATEGORIAS`;
CREATE TABLE `CATEGORIAS` (
  `id_categoria` INT NOT NULL AUTO_INCREMENT,
  `nombre_categoria` VARCHAR(100) NOT NULL,
  `descripcion_categoria` VARCHAR(255),
  `dias_garantias` INT NOT NULL,
  PRIMARY KEY (`id_categoria`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


DROP TABLE IF EXISTS `CLIENTES`;
CREATE TABLE `CLIENTES` (
  `id_cliente` INT NOT NULL AUTO_INCREMENT,
  `identidad_cliente` VARCHAR(50) NOT NULL,
  `nombre_cliente` VARCHAR(100) NOT NULL,
  `apellido_cliente` VARCHAR(100),
  `telefono_cliente` VARCHAR(20),
  `correo_cliente` VARCHAR(100),
  `estado_cliente` TINYINT(1) NOT NULL,
  PRIMARY KEY (`id_cliente`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


DROP TABLE IF EXISTS `CONTROL_CAJA`;
CREATE TABLE `CONTROL_CAJA` (
  `id_caja` INT NOT NULL AUTO_INCREMENT,
  `id_usuario_apertura` INT NOT NULL,
  `fecha_apertura` DATETIME NOT NULL,
  `monto_apertura` DECIMAL(18,4) NOT NULL,
  `fecha_cierre` DATETIME,
  `monto_cierre_real` DECIMAL(18,4),
  `monto_cierre_esperado` DECIMAL(18,4),
  `diferencia_caja` DECIMAL(18,4),
  `estado_caja` VARCHAR(20) NOT NULL,
  `observaciones` VARCHAR(500),
  `cajero_turno` VARCHAR(100),
  `id_usuario_cierre` INT,
  PRIMARY KEY (`id_caja`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


DROP TABLE IF EXISTS `CONTROL_ERRORES_SISTEMA`;
CREATE TABLE `CONTROL_ERRORES_SISTEMA` (
  `id_error` INT NOT NULL AUTO_INCREMENT,
  `fecha_error` DATETIME NOT NULL,
  `modulo_error` VARCHAR(100) NOT NULL,
  `mensaje_error` VARCHAR(255) NOT NULL,
  `detalle_tecnico_error` LONGTEXT,
  `estado_error` TINYINT(1) NOT NULL,
  PRIMARY KEY (`id_error`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


DROP TABLE IF EXISTS `DETALLES_APARTADO`;
CREATE TABLE `DETALLES_APARTADO` (
  `id_detalle_apartado` INT NOT NULL AUTO_INCREMENT,
  `id_apartado` INT NOT NULL,
  `id_producto` INT NOT NULL,
  `descripcion_apartado` VARCHAR(250) NOT NULL,
  `cantidad_apartado` INT NOT NULL,
  `precio_unitario_apartado` DECIMAL(18,4) NOT NULL,
  `subtotal_apartado` DECIMAL(18,4) NOT NULL,
  `identificador_serie` VARCHAR(100),
  PRIMARY KEY (`id_detalle_apartado`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


DROP TABLE IF EXISTS `DETALLES_VENTA`;
CREATE TABLE `DETALLES_VENTA` (
  `id_detalle_venta` INT NOT NULL AUTO_INCREMENT,
  `id_ventas` INT NOT NULL,
  `id_producto` INT NOT NULL,
  `descripcion_venta` VARCHAR(255) NOT NULL,
  `cantidad_venta` INT NOT NULL,
  `precio_unitario_venta` DECIMAL(10,2) NOT NULL,
  `subtotal_venta` DECIMAL(10,2) NOT NULL,
  `identificador_serie` VARCHAR(100),
  `dias_garantia` INT,
  `estado_garantia` VARCHAR(20),
  `observacion_garantia` LONGTEXT,
  `foto_garantia` LONGTEXT,
  `resolucion_garantia` VARCHAR(100),
  PRIMARY KEY (`id_detalle_venta`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


DROP TABLE IF EXISTS `EMPRESA`;
CREATE TABLE `EMPRESA` (
  `id_empresa` INT NOT NULL AUTO_INCREMENT,
  `nombre_empresa` VARCHAR(150) NOT NULL,
  `rtn_empresa` VARCHAR(20) NOT NULL,
  `dueño_empresa` VARCHAR(100) NOT NULL,
  `direccion_empresa` VARCHAR(255) NOT NULL,
  `estado_empresa` TINYINT(1) NOT NULL,
  `habilitar_facturacion_empresa` TINYINT(1) NOT NULL,
  `mensaje_ticket_pie_factura` VARCHAR(255),
  `mensaje_ticket_pie_recibo` VARCHAR(255),
  `mensaje_ticket_entrega` VARCHAR(255),
  `mensaje_ticket_pie_cotizacion` VARCHAR(255),
  `web_empresa` VARCHAR(100),
  `facebook_empresa` VARCHAR(100),
  `whatsapp_empresa` VARCHAR(20),
  `email_empresa` VARCHAR(100),
  `telefono_secundario` VARCHAR(20),
  `numero_telefono` VARCHAR(20) NOT NULL,
  `politicas_garantia` VARCHAR(500),
  `mensaje_ticket_cambio` LONGTEXT,
  `mensaje_ticket_reclamo` LONGTEXT,
  `imagen_logo` LONGTEXT,
  PRIMARY KEY (`id_empresa`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


DROP TABLE IF EXISTS `ERRORES_SISTEMA`;
CREATE TABLE `ERRORES_SISTEMA` (
  `id_error` INT NOT NULL AUTO_INCREMENT,
  `fecha_suceso` DATETIME,
  `origen` VARCHAR(255),
  `resumen` VARCHAR(500),
  `stacktrace` LONGTEXT,
  PRIMARY KEY (`id_error`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


DROP TABLE IF EXISTS `INVENTARIO`;
CREATE TABLE `INVENTARIO` (
  `id_producto` INT NOT NULL AUTO_INCREMENT,
  `codigo_barras_producto` VARCHAR(100),
  `nombre_producto` VARCHAR(150) NOT NULL,
  `id_categoria` INT NOT NULL,
  `id_proveedor` INT NOT NULL,
  `id_ubicacion` INT NOT NULL,
  `precio_compra_producto` DECIMAL(10,2) NOT NULL,
  `precio_venta_producto` DECIMAL(10,2) NOT NULL,
  `precio_mayorista_producto` DECIMAL(10,2),
  `stock_minimo_producto` INT NOT NULL,
  `stock_producto` INT NOT NULL,
  `eliminado_producto` TINYINT(1) NOT NULL,
  `dias_garantia` INT,
  `requiere_serie` TINYINT(1),
  `incluye_impuesto` TINYINT(1) NOT NULL,
  `imagen_producto` LONGTEXT,
  PRIMARY KEY (`id_producto`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


DROP TABLE IF EXISTS `INVENTARIO_DEFECTUOSO`;
CREATE TABLE `INVENTARIO_DEFECTUOSO` (
  `id_inventarioDefectuoso` INT NOT NULL AUTO_INCREMENT,
  `id_producto` INT NOT NULL,
  `id_detalle_venta` INT,
  `fecha_ingreso` DATETIME,
  `cantidad` INT NOT NULL,
  `motivo_danio` LONGTEXT NOT NULL,
  `estado_defecto` VARCHAR(50),
  `fecha_envio_proveedor` DATETIME,
  `fecha_recibido_proveedor` DATETIME,
  `fecha_entregado_cliente` DATETIME,
  `foto` LONGTEXT,
  PRIMARY KEY (`id_inventarioDefectuoso`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


DROP TABLE IF EXISTS `KARDEX`;
CREATE TABLE `KARDEX` (
  `id_kardex` INT NOT NULL AUTO_INCREMENT,
  `id_producto` INT NOT NULL,
  `id_usuario` INT NOT NULL,
  `fecha_movimiento_producto` DATETIME NOT NULL,
  `tipo_movimiento_producto` VARCHAR(50) NOT NULL,
  `cantidad_producto` INT NOT NULL,
  `stock_restante_producto` INT NOT NULL,
  `referencia_producto` VARCHAR(255),
  PRIMARY KEY (`id_kardex`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


DROP TABLE IF EXISTS `METODOS_PAGO`;
CREATE TABLE `METODOS_PAGO` (
  `id_metodo_pago` INT NOT NULL AUTO_INCREMENT,
  `nombre_metodo` VARCHAR(50) NOT NULL,
  PRIMARY KEY (`id_metodo_pago`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


DROP TABLE IF EXISTS `PERMISOS`;
CREATE TABLE `PERMISOS` (
  `id_permiso` INT NOT NULL AUTO_INCREMENT,
  `nombre_permiso` VARCHAR(50) NOT NULL,
  `descripcion` VARCHAR(150),
  PRIMARY KEY (`id_permiso`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


DROP TABLE IF EXISTS `PROVEEDORES`;
CREATE TABLE `PROVEEDORES` (
  `id_proveedor` INT NOT NULL AUTO_INCREMENT,
  `nombre_proveedor` VARCHAR(150) NOT NULL,
  `nombre_encargado_proveedor` VARCHAR(100),
  `telefono_proveedor` VARCHAR(20) NOT NULL,
  `direccion_proveedor` VARCHAR(255),
  `tipo_repuestos_proveedor` VARCHAR(150),
  `estado_proveedor` TINYINT(1) NOT NULL,
  PRIMARY KEY (`id_proveedor`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


DROP TABLE IF EXISTS `REGISTROS_CAI`;
CREATE TABLE `REGISTROS_CAI` (
  `id_cai` INT NOT NULL AUTO_INCREMENT,
  `id_empresa` INT NOT NULL,
  `cai_empresa` VARCHAR(50) NOT NULL,
  `rango_facturacion_inicial` VARCHAR(50) NOT NULL,
  `rango_facturacion_final` VARCHAR(50) NOT NULL,
  `fecha_limite_emision` DATE NOT NULL,
  `porcentaje_impuesto` DECIMAL(5,2) NOT NULL,
  `estado_cai` TINYINT(1) NOT NULL,
  PRIMARY KEY (`id_cai`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


DROP TABLE IF EXISTS `ROL_PERMISOS`;
CREATE TABLE `ROL_PERMISOS` (
  `id_rol` INT NOT NULL,
  `id_permiso` INT NOT NULL,
  PRIMARY KEY (`id_rol`, `id_permiso`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


DROP TABLE IF EXISTS `ROLES_USUARIO`;
CREATE TABLE `ROLES_USUARIO` (
  `id_rol` INT NOT NULL AUTO_INCREMENT,
  `nombre_rol` VARCHAR(50) NOT NULL,
  `descripcion_rol` VARCHAR(255),
  PRIMARY KEY (`id_rol`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


DROP TABLE IF EXISTS `UBICACIONES`;
CREATE TABLE `UBICACIONES` (
  `id_ubicacion` INT NOT NULL AUTO_INCREMENT,
  `nombre_ubicacion` VARCHAR(100) NOT NULL,
  PRIMARY KEY (`id_ubicacion`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


DROP TABLE IF EXISTS `USUARIOS`;
CREATE TABLE `USUARIOS` (
  `id_usuario` INT NOT NULL AUTO_INCREMENT,
  `id_rol` INT NOT NULL,
  `nombre_usuario` VARCHAR(100) NOT NULL,
  `password_hash` VARCHAR(255) NOT NULL,
  `estado_usuario` TINYINT(1) NOT NULL,
  `email_usuario` VARCHAR(100),
  `token_recuperacion` VARCHAR(10),
  `expiracion_token` DATETIME,
  PRIMARY KEY (`id_usuario`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


DROP TABLE IF EXISTS `VENTAS`;
CREATE TABLE `VENTAS` (
  `id_ventas` INT NOT NULL AUTO_INCREMENT,
  `fecha_venta` DATETIME NOT NULL,
  `id_cliente_venta` INT NOT NULL,
  `id_usuario` INT NOT NULL,
  `id_metodo_pago` INT NOT NULL,
  `id_orden_venta` VARCHAR(50),
  `subtotal_venta` DECIMAL(12,2) NOT NULL,
  `impuesto_venta` DECIMAL(12,2) NOT NULL,
  `total_venta` DECIMAL(12,2) NOT NULL,
  `referencia_pago` VARCHAR(100),
  `banco_pago` VARCHAR(100),
  PRIMARY KEY (`id_ventas`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;



-- Relaciones (Llaves Foraneas)
ALTER TABLE `ABONOS_APARTADO` ADD CONSTRAINT `FK__ABONOS_AP__id_ap__02FC7413` FOREIGN KEY (`id_apartado`) REFERENCES `APARTADOS` (`id_apartado`);
ALTER TABLE `ABONOS_APARTADO` ADD CONSTRAINT `FK__ABONOS_AP__id_me__04E4BC85` FOREIGN KEY (`id_metodo_pago`) REFERENCES `METODOS_PAGO` (`id_metodo_pago`);
ALTER TABLE `ABONOS_APARTADO` ADD CONSTRAINT `FK__ABONOS_AP__id_us__03F0984C` FOREIGN KEY (`id_usuario`) REFERENCES `USUARIOS` (`id_usuario`);
ALTER TABLE `APARTADOS` ADD CONSTRAINT `FK__APARTADOS__id_cl__797309D9` FOREIGN KEY (`id_cliente_apartado`) REFERENCES `CLIENTES` (`id_cliente`);
ALTER TABLE `APARTADOS` ADD CONSTRAINT `FK__APARTADOS__id_us__7A672E12` FOREIGN KEY (`id_usuario`) REFERENCES `USUARIOS` (`id_usuario`);
ALTER TABLE `CONTROL_CAJA` ADD CONSTRAINT `FK__CONTROL_C__id_us__08B54D69` FOREIGN KEY (`id_usuario_apertura`) REFERENCES `USUARIOS` (`id_usuario`);
ALTER TABLE `DETALLES_APARTADO` ADD CONSTRAINT `FK__DETALLES___id_ap__7F2BE32F` FOREIGN KEY (`id_apartado`) REFERENCES `APARTADOS` (`id_apartado`);
ALTER TABLE `DETALLES_APARTADO` ADD CONSTRAINT `FK__DETALLES___id_pr__00200768` FOREIGN KEY (`id_producto`) REFERENCES `INVENTARIO` (`id_producto`);
ALTER TABLE `DETALLES_VENTA` ADD CONSTRAINT `FK__DETALLES___id_pr__693CA210` FOREIGN KEY (`id_producto`) REFERENCES `INVENTARIO` (`id_producto`);
ALTER TABLE `DETALLES_VENTA` ADD CONSTRAINT `FK__DETALLES___id_ve__68487DD7` FOREIGN KEY (`id_ventas`) REFERENCES `VENTAS` (`id_ventas`);
ALTER TABLE `INVENTARIO` ADD CONSTRAINT `FK__INVENTARI__id_ca__6A30C649` FOREIGN KEY (`id_categoria`) REFERENCES `CATEGORIAS` (`id_categoria`);
ALTER TABLE `INVENTARIO` ADD CONSTRAINT `FK__INVENTARI__id_pr__6B24EA82` FOREIGN KEY (`id_proveedor`) REFERENCES `PROVEEDORES` (`id_proveedor`);
ALTER TABLE `INVENTARIO` ADD CONSTRAINT `FK__INVENTARI__id_ub__6C190EBB` FOREIGN KEY (`id_ubicacion`) REFERENCES `UBICACIONES` (`id_ubicacion`);
ALTER TABLE `INVENTARIO_DEFECTUOSO` ADD CONSTRAINT `FK__INVENTARI__id_de__19DFD96B` FOREIGN KEY (`id_detalle_venta`) REFERENCES `DETALLES_VENTA` (`id_detalle_venta`);
ALTER TABLE `INVENTARIO_DEFECTUOSO` ADD CONSTRAINT `FK__INVENTARI__id_pr__18EBB532` FOREIGN KEY (`id_producto`) REFERENCES `INVENTARIO` (`id_producto`);
ALTER TABLE `KARDEX` ADD CONSTRAINT `FK__KARDEX__id_produ__6D0D32F4` FOREIGN KEY (`id_producto`) REFERENCES `INVENTARIO` (`id_producto`);
ALTER TABLE `KARDEX` ADD CONSTRAINT `FK__KARDEX__id_usuar__6E01572D` FOREIGN KEY (`id_usuario`) REFERENCES `USUARIOS` (`id_usuario`);
ALTER TABLE `REGISTROS_CAI` ADD CONSTRAINT `FK__REGISTROS__id_em__6EF57B66` FOREIGN KEY (`id_empresa`) REFERENCES `EMPRESA` (`id_empresa`);
ALTER TABLE `ROL_PERMISOS` ADD CONSTRAINT `FK__ROL_PERMI__id_pe__22751F6C` FOREIGN KEY (`id_permiso`) REFERENCES `PERMISOS` (`id_permiso`);
ALTER TABLE `ROL_PERMISOS` ADD CONSTRAINT `FK__ROL_PERMI__id_ro__2180FB33` FOREIGN KEY (`id_rol`) REFERENCES `ROLES_USUARIO` (`id_rol`);
ALTER TABLE `USUARIOS` ADD CONSTRAINT `FK__USUARIOS__id_rol__6FE99F9F` FOREIGN KEY (`id_rol`) REFERENCES `ROLES_USUARIO` (`id_rol`);
ALTER TABLE `VENTAS` ADD CONSTRAINT `FK__VENTAS__id_clien__70DDC3D8` FOREIGN KEY (`id_cliente_venta`) REFERENCES `CLIENTES` (`id_cliente`);
ALTER TABLE `VENTAS` ADD CONSTRAINT `FK__VENTAS__id_metod__72C60C4A` FOREIGN KEY (`id_metodo_pago`) REFERENCES `METODOS_PAGO` (`id_metodo_pago`);
ALTER TABLE `VENTAS` ADD CONSTRAINT `FK__VENTAS__id_usuar__71D1E811` FOREIGN KEY (`id_usuario`) REFERENCES `USUARIOS` (`id_usuario`);

SET FOREIGN_KEY_CHECKS=1;
