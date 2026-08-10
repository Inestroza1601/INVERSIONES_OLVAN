-- ========================================================
-- Script de Migración: Saldos Históricos de Abonos
-- ========================================================
-- Este script agrega las columnas necesarias para guardar la "foto" 
-- del saldo en el momento en que se realizó cada abono.

-- 1. Agregar las columnas a la tabla ABONOS_APARTADO
ALTER TABLE ABONOS_APARTADO ADD total_historico DECIMAL(10,2) DEFAULT 0.0 NOT NULL;
ALTER TABLE ABONOS_APARTADO ADD saldo_historico DECIMAL(10,2) DEFAULT 0.0 NOT NULL;

-- 2. Al ser datos de prueba, simplemente igualamos el saldo histórico 
-- al saldo actual del apartado (fallback rápido) para evitar nulos.
-- Nota: Para nuevos abonos, el sistema Java guardará el saldo exacto del momento.
UPDATE ab
SET 
    ab.total_historico = ap.total,
    ab.saldo_historico = ap.saldo_restante
FROM ABONOS_APARTADO ab
INNER JOIN APARTADOS ap ON ab.id_apartado = ap.id_apartado;
