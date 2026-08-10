-- 1. Crear tabla de PERMISOS
IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='PERMISOS' and xtype='U')
BEGIN
    CREATE TABLE PERMISOS (
        id_permiso INT IDENTITY(1,1) PRIMARY KEY,
        nombre_permiso VARCHAR(50) NOT NULL UNIQUE,
        descripcion VARCHAR(150)
    );
END
GO

-- 2. Crear tabla intermedia ROL_PERMISOS
IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='ROL_PERMISOS' and xtype='U')
BEGIN
    CREATE TABLE ROL_PERMISOS (
        id_rol INT NOT NULL,
        id_permiso INT NOT NULL,
        PRIMARY KEY (id_rol, id_permiso),
        FOREIGN KEY (id_rol) REFERENCES ROLES_USUARIO(id_rol),
        FOREIGN KEY (id_permiso) REFERENCES PERMISOS(id_permiso)
    );
END
GO

-- 3. Poblar tabla de PERMISOS con los permisos básicos del sistema
INSERT INTO PERMISOS (nombre_permiso, descripcion) 
SELECT 'ACCESO_ADMINISTRACION', 'Acceso al panel de Configuración y Gestión de Usuarios'
WHERE NOT EXISTS (SELECT 1 FROM PERMISOS WHERE nombre_permiso = 'ACCESO_ADMINISTRACION');

INSERT INTO PERMISOS (nombre_permiso, descripcion) 
SELECT 'ACCESO_INVENTARIO', 'Ver productos en el inventario'
WHERE NOT EXISTS (SELECT 1 FROM PERMISOS WHERE nombre_permiso = 'ACCESO_INVENTARIO');

INSERT INTO PERMISOS (nombre_permiso, descripcion) 
SELECT 'ELIMINAR_PRODUCTOS', 'Permite eliminar productos permanentemente'
WHERE NOT EXISTS (SELECT 1 FROM PERMISOS WHERE nombre_permiso = 'ELIMINAR_PRODUCTOS');

INSERT INTO PERMISOS (nombre_permiso, descripcion) 
SELECT 'ACCESO_CLIENTES', 'Acceso al directorio de clientes'
WHERE NOT EXISTS (SELECT 1 FROM PERMISOS WHERE nombre_permiso = 'ACCESO_CLIENTES');

INSERT INTO PERMISOS (nombre_permiso, descripcion) 
SELECT 'ELIMINAR_CLIENTES', 'Permite eliminar clientes permanentemente'
WHERE NOT EXISTS (SELECT 1 FROM PERMISOS WHERE nombre_permiso = 'ELIMINAR_CLIENTES');

INSERT INTO PERMISOS (nombre_permiso, descripcion) 
SELECT 'ACCESO_POS', 'Acceso al módulo de Punto de Venta (Facturación)'
WHERE NOT EXISTS (SELECT 1 FROM PERMISOS WHERE nombre_permiso = 'ACCESO_POS');

INSERT INTO PERMISOS (nombre_permiso, descripcion) 
SELECT 'CANCELAR_VENTAS', 'Permite anular ventas en curso en el POS'
WHERE NOT EXISTS (SELECT 1 FROM PERMISOS WHERE nombre_permiso = 'CANCELAR_VENTAS');

INSERT INTO PERMISOS (nombre_permiso, descripcion) 
SELECT 'APLICAR_DESCUENTOS', 'Permite realizar descuentos manuales en POS'
WHERE NOT EXISTS (SELECT 1 FROM PERMISOS WHERE nombre_permiso = 'APLICAR_DESCUENTOS');

INSERT INTO PERMISOS (nombre_permiso, descripcion) 
SELECT 'ACCESO_CAJA', 'Permite realizar aperturas, cierres y salidas de caja'
WHERE NOT EXISTS (SELECT 1 FROM PERMISOS WHERE nombre_permiso = 'ACCESO_CAJA');

INSERT INTO PERMISOS (nombre_permiso, descripcion) 
SELECT 'ACCESO_APARTADOS', 'Permite registrar y gestionar apartados'
WHERE NOT EXISTS (SELECT 1 FROM PERMISOS WHERE nombre_permiso = 'ACCESO_APARTADOS');

INSERT INTO PERMISOS (nombre_permiso, descripcion) 
SELECT 'ACCESO_VENTAS', 'Ver el historial general de ventas'
WHERE NOT EXISTS (SELECT 1 FROM PERMISOS WHERE nombre_permiso = 'ACCESO_VENTAS');

INSERT INTO PERMISOS (nombre_permiso, descripcion) 
SELECT 'ANULAR_FACTURAS', 'Permite realizar devoluciones y anular facturas'
WHERE NOT EXISTS (SELECT 1 FROM PERMISOS WHERE nombre_permiso = 'ANULAR_FACTURAS');

INSERT INTO PERMISOS (nombre_permiso, descripcion) 
SELECT 'ACCESO_GARANTIAS', 'Gestionar proceso de garantías'
WHERE NOT EXISTS (SELECT 1 FROM PERMISOS WHERE nombre_permiso = 'ACCESO_GARANTIAS');

INSERT INTO PERMISOS (nombre_permiso, descripcion) 
SELECT 'ACCESO_ESTADISTICAS', 'Ver gráficas financieras y reportes'
WHERE NOT EXISTS (SELECT 1 FROM PERMISOS WHERE nombre_permiso = 'ACCESO_ESTADISTICAS');
GO

-- 4. Asignar todos los permisos al Administrador Maestro (id_rol = 1 por defecto)
-- (Nota: Esto asume que el id_rol 1 es el Administrador o Programador)
INSERT INTO ROL_PERMISOS (id_rol, id_permiso)
SELECT 1, p.id_permiso FROM PERMISOS p
WHERE NOT EXISTS (SELECT 1 FROM ROL_PERMISOS WHERE id_rol = 1 AND id_permiso = p.id_permiso);
GO
