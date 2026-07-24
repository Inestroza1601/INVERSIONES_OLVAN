-- ==========================================
-- SCRIPT DE ACTUALIZACIÓN DE BASE DE DATOS
-- MÓDULO DE INVENTARIO DEFECTUOSO
-- ==========================================
USE INVERSIONES_OLVAN;
GO

-- 1. CREACIÓN DE TABLA INVENTARIO_DEFECTUOSO (MERMAS Y GARANTÍAS)
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[INVENTARIO_DEFECTUOSO]') AND type in (N'U'))
BEGIN
    CREATE TABLE INVENTARIO_DEFECTUOSO (
        id_inventarioDefectuoso INT IDENTITY(1,1) PRIMARY KEY,
        id_producto INT NOT NULL FOREIGN KEY REFERENCES INVENTARIO(id_producto),
        id_detalle_venta INT NULL FOREIGN KEY REFERENCES DETALLES_VENTA(id_detalle_venta),
        fecha_ingreso DATETIME NOT NULL DEFAULT GETDATE(),
        cantidad INT NOT NULL,
        motivo_danio VARCHAR(MAX) NOT NULL,
        estado_defecto VARCHAR(100) NOT NULL DEFAULT 'En Bodega'
    );
END
GO

-- 2. ACTUALIZACIONES DE TABLAS EXISTENTES (COLUMNAS NUEVAS)
-- Añadir campos de seguimiento de tiempo a INVENTARIO_DEFECTUOSO para el Historial de Movimientos
IF NOT EXISTS (
    SELECT * FROM sys.columns 
    WHERE Name = N'fecha_envio_proveedor' 
    AND Object_ID = Object_ID(N'INVENTARIO_DEFECTUOSO')
)
BEGIN
    ALTER TABLE INVENTARIO_DEFECTUOSO ADD fecha_envio_proveedor DATETIME NULL;
END
GO

IF NOT EXISTS (
    SELECT * FROM sys.columns 
    WHERE Name = N'fecha_recibido_proveedor' 
    AND Object_ID = Object_ID(N'INVENTARIO_DEFECTUOSO')
)
BEGIN
    ALTER TABLE INVENTARIO_DEFECTUOSO ADD fecha_recibido_proveedor DATETIME NULL;
END
GO

IF NOT EXISTS (
    SELECT * FROM sys.columns 
    WHERE Name = N'fecha_entregado_cliente' 
    AND Object_ID = Object_ID(N'INVENTARIO_DEFECTUOSO')
)
BEGIN
    ALTER TABLE INVENTARIO_DEFECTUOSO ADD fecha_entregado_cliente DATETIME NULL;
END
GO
-- 3. ACTUALIZACIONES A LA TABLA DETALLES_VENTA (MÓDULO DE GARANTÍAS)
IF NOT EXISTS (SELECT * FROM sys.columns WHERE Name = N'estado_garantia' AND Object_ID = Object_ID(N'DETALLES_VENTA'))
BEGIN
    ALTER TABLE DETALLES_VENTA ADD estado_garantia VARCHAR(50) NULL DEFAULT 'VIGENTE';
END
GO

IF NOT EXISTS (SELECT * FROM sys.columns WHERE Name = N'observacion_garantia' AND Object_ID = Object_ID(N'DETALLES_VENTA'))
BEGIN
    ALTER TABLE DETALLES_VENTA ADD observacion_garantia VARCHAR(MAX) NULL;
END
GO

IF NOT EXISTS (SELECT * FROM sys.columns WHERE Name = N'foto_garantia' AND Object_ID = Object_ID(N'DETALLES_VENTA'))
BEGIN
    ALTER TABLE DETALLES_VENTA ADD foto_garantia VARCHAR(MAX) NULL;
END
GO

IF NOT EXISTS (SELECT * FROM sys.columns WHERE Name = N'resolucion_garantia' AND Object_ID = Object_ID(N'DETALLES_VENTA'))
BEGIN
    ALTER TABLE DETALLES_VENTA ADD resolucion_garantia VARCHAR(100) NULL;
END
GO

-- 4. ACTUALIZACIÓN DE INVENTARIO (SOPORTE PARA FOTOS BASE64 GRANDES)
-- Nota: Asegurarse de que el campo de ruta_imagen_producto pueda almacenar cadenas Base64 inmensas
ALTER TABLE INVENTARIO ALTER COLUMN ruta_imagen_producto VARCHAR(MAX) NULL;
GO
