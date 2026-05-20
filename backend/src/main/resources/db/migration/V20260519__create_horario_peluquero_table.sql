-- Script de migración para crear la tabla horario_peluquero
CREATE TABLE IF NOT EXISTS horario_peluquero (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    peluquero_id BIGINT NOT NULL,
    dia_semana VARCHAR(16) NOT NULL,
    hora_inicio TIME NOT NULL,
    hora_fin TIME NOT NULL,
    fecha_creacion DATETIME NOT NULL,
    fecha_actualizacion DATETIME NOT NULL
    -- Puedes agregar FOREIGN KEY (peluquero_id) REFERENCES usuario(id) si aplica
);
