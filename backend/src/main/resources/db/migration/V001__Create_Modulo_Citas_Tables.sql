-- Flyway Migration: Create Módulo Citas Tables
-- This migration creates all tables for the appointment booking system

-- 1. BARBERO_ESPECIALIDAD
-- Links a barber (Usuario) to a service with specialization level
CREATE TABLE IF NOT EXISTS barbero_especialidad (
    id_barbero_especialidad BIGSERIAL PRIMARY KEY,
    id_usuario_barbero BIGINT NOT NULL,
    id_servicio BIGINT NOT NULL,
    nivel_especialidad VARCHAR(20) NOT NULL CHECK (nivel_especialidad IN ('JUNIOR', 'SENIOR', 'EXPERTO')),
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- Constraints
    FOREIGN KEY (id_usuario_barbero) REFERENCES usuario(id_usuario),
    FOREIGN KEY (id_servicio) REFERENCES servicio(id_servicio),
    UNIQUE (id_usuario_barbero, id_servicio)
);

-- Index for fast lookup
CREATE INDEX IF NOT EXISTS idx_barbero_especialidad_barbero 
ON barbero_especialidad(id_usuario_barbero);

CREATE INDEX IF NOT EXISTS idx_barbero_especialidad_servicio 
ON barbero_especialidad(id_servicio);

-- 2. BARBERO_HORARIO
-- Per-barber schedule override by day of week
-- Replaces HorarioNegocio for that barber when specified
CREATE TABLE IF NOT EXISTS barbero_horario (
    id_barbero_horario BIGSERIAL PRIMARY KEY,
    id_usuario_barbero BIGINT NOT NULL,
    id_dia_semana INTEGER NOT NULL CHECK (id_dia_semana >= 1 AND id_dia_semana <= 7),
    hora_inicio TIME NOT NULL,
    hora_fin TIME NOT NULL,
    disponible BOOLEAN DEFAULT TRUE,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- Constraints
    FOREIGN KEY (id_usuario_barbero) REFERENCES usuario(id_usuario),
    UNIQUE (id_usuario_barbero, id_dia_semana)
);

-- Index for fast lookup
CREATE INDEX IF NOT EXISTS idx_barbero_horario_barbero_dia 
ON barbero_horario(id_usuario_barbero, id_dia_semana);

-- 3. CITA (CORE TABLE)
-- Main appointments table with pessimistic locking support
CREATE TABLE IF NOT EXISTS cita (
    id_cita BIGSERIAL PRIMARY KEY,
    id_cliente BIGINT NOT NULL,
    id_barbero BIGINT NOT NULL,
    id_servicio BIGINT NOT NULL,
    estado_cita INTEGER NOT NULL CHECK (estado_cita IN (1, 2, 3, 4)),
    -- 1=CONFIRMADA, 2=CANCELADA, 3=COMPLETADA, 4=AUSENTE
    fecha_hora TIMESTAMP NOT NULL,
    duracion_minutos INTEGER NOT NULL,
    costo DECIMAL(10, 2),
    notas TEXT,
    motivo_cancelacion TEXT,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    version BIGINT DEFAULT 0,
    
    -- Constraints
    FOREIGN KEY (id_cliente) REFERENCES usuario(id_usuario),
    FOREIGN KEY (id_barbero) REFERENCES usuario(id_usuario),
    FOREIGN KEY (id_servicio) REFERENCES servicio(id_servicio),
    
    -- CRITICAL: Prevents double-booking for confirmed appointments
    UNIQUE (id_barbero, fecha_hora, estado_cita) WHERE estado_cita = 1
);

-- Indexes for conflict detection and queries
CREATE INDEX IF NOT EXISTS idx_cita_barbero_slot 
ON cita(id_barbero, fecha_hora, estado_cita) 
WHERE estado_cita = 1;

CREATE INDEX IF NOT EXISTS idx_cita_cliente 
ON cita(id_cliente, estado_cita);

CREATE INDEX IF NOT EXISTS idx_cita_fecha 
ON cita(fecha_hora, estado_cita);

CREATE INDEX IF NOT EXISTS idx_cita_estado 
ON cita(estado_cita);

CREATE INDEX IF NOT EXISTS idx_cita_barbero_fecha 
ON cita(id_barbero, fecha_hora);

-- 4. CITA_NOTIFICACION
-- Audit log of all notifications sent for appointments
CREATE TABLE IF NOT EXISTS cita_notificacion (
    id_notificacion BIGSERIAL PRIMARY KEY,
    id_cita BIGINT NOT NULL,
    id_usuario_destinatario BIGINT NOT NULL,
    tipo_notificacion VARCHAR(50) NOT NULL CHECK (tipo_notificacion IN ('CONFIRMACION', 'CANCELACION', 'RECORDATORIO_24H', 'CAMBIO_HORARIO')),
    mensaje TEXT NOT NULL,
    fecha_envio TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    leida BOOLEAN DEFAULT FALSE,
    
    -- Constraints
    FOREIGN KEY (id_cita) REFERENCES cita(id_cita) ON DELETE CASCADE,
    FOREIGN KEY (id_usuario_destinatario) REFERENCES usuario(id_usuario)
);

-- Indexes for queries
CREATE INDEX IF NOT EXISTS idx_notificacion_usuario 
ON cita_notificacion(id_usuario_destinatario, leida);

CREATE INDEX IF NOT EXISTS idx_notificacion_cita 
ON cita_notificacion(id_cita);

CREATE INDEX IF NOT EXISTS idx_notificacion_tipo 
ON cita_notificacion(tipo_notificacion);

-- 5. BITACORA_AUDITORIA
-- Complete audit trail of all appointment changes
CREATE TABLE IF NOT EXISTS bitacora_auditoria (
    id_auditoria BIGSERIAL PRIMARY KEY,
    id_cita BIGINT NOT NULL,
    id_usuario_actor BIGINT NOT NULL,
    accion VARCHAR(20) NOT NULL CHECK (accion IN ('CREATE', 'UPDATE', 'CANCEL')),
    estado_anterior INTEGER,
    estado_nuevo INTEGER,
    detalles_cambio JSONB,
    timestamp_evento TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- Constraints
    FOREIGN KEY (id_cita) REFERENCES cita(id_cita) ON DELETE CASCADE,
    FOREIGN KEY (id_usuario_actor) REFERENCES usuario(id_usuario)
);

-- Indexes for audit queries
CREATE INDEX IF NOT EXISTS idx_auditoria_cita 
ON bitacora_auditoria(id_cita);

CREATE INDEX IF NOT EXISTS idx_auditoria_actor 
ON bitacora_auditoria(id_usuario_actor);

CREATE INDEX IF NOT EXISTS idx_auditoria_timestamp 
ON bitacora_auditoria(timestamp_evento DESC);

CREATE INDEX IF NOT EXISTS idx_auditoria_accion 
ON bitacora_auditoria(accion);

-- JSONB index for advanced searches
CREATE INDEX IF NOT EXISTS idx_auditoria_detalles_gin 
ON bitacora_auditoria USING GIN (detalles_cambio);

-- 6. Verify PostgreSQL version for JSONB support
-- PostgreSQL 9.3+ required for JSONB
-- PostgreSQL 9.5+ required for ON CONFLICT clause (if needed later)
-- Current version should be 14+
