-- Flyway Migration: Configure Transactional Constraints for Cita Module
-- This migration sets up transaction isolation levels and performance tuning

-- 1. Analyze tables for better query planning (run after initial data loading)
ANALYZE barbero_especialidad;
ANALYZE barbero_horario;
ANALYZE cita;
ANALYZE cita_notificacion;
ANALYZE bitacora_auditoria;

-- 2. Set work_mem for better sorting performance in complex queries
-- This is session-level but can be set globally in postgresql.conf
-- SET work_mem = '256MB'; -- Recommended for larger datasets

-- 3. Create a view for easier conflict detection queries
CREATE OR REPLACE VIEW vw_cita_confirmada AS
SELECT 
    c.id_cita,
    c.id_barbero,
    c.fecha_hora,
    c.duracion_minutos,
    (c.fecha_hora + (c.duracion_minutos || ' minutes')::INTERVAL) as fecha_fin,
    c.estado_cita,
    c.id_cliente,
    c.id_servicio
FROM cita c
WHERE c.estado_cita = 1; -- Only confirmed appointments

-- 4. Create function for transaction isolation level verification
CREATE OR REPLACE FUNCTION verify_isolation_level()
RETURNS text AS $$
BEGIN
    RETURN (SELECT current_setting('transaction_isolation'));
END;
$$ LANGUAGE plpgsql;

-- 5. Notes about SERIALIZABLE isolation:
-- - SET SESSION CHARACTERISTICS AS TRANSACTION ISOLATION LEVEL SERIALIZABLE;
-- - Required for double-booking prevention with pessimistic locking
-- - Slightly slower than READ_COMMITTED but necessary for data integrity
-- - Flyway will run this migration in the default transaction isolation level
-- - Spring @Transactional(isolation=SERIALIZABLE) sets it per-transaction

-- 6. Grant necessary permissions (if using roles)
-- GRANT ALL PRIVILEGES ON TABLE cita TO barberia_app_role;
-- GRANT ALL PRIVILEGES ON TABLE barbero_especialidad TO barberia_app_role;
-- GRANT ALL PRIVILEGES ON TABLE barbero_horario TO barberia_app_role;
-- GRANT ALL PRIVILEGES ON TABLE cita_notificacion TO barberia_app_role;
-- GRANT ALL PRIVILEGES ON TABLE bitacora_auditoria TO barberia_app_role;

-- 7. Performance comments (informational only)
COMMENT ON TABLE cita IS 'Core appointments table. UNIQUE constraint prevents double-booking. Uses pessimistic locking with SERIALIZABLE isolation.';
COMMENT ON INDEX idx_cita_barbero_slot IS 'Critical index for conflict detection. Optimized for the UNIQUE constraint scope.';
COMMENT ON TABLE bitacora_auditoria IS 'Complete audit trail. JSONB column stores complex change details. GIN index enables advanced searches.';
