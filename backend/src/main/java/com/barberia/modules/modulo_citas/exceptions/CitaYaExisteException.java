package com.barberia.modules.modulo_citas.exceptions;

/**
 * Excepción lanzada cuando ya existe una cita en el mismo horario
 */
public class CitaYaExisteException extends RuntimeException {
    public CitaYaExisteException(String mensaje) {
        super(mensaje);
    }

    public CitaYaExisteException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
