package com.barberia.modules.modulo_horarios.exceptions;

/**
 * Excepción personalizada para errores de validación de horario.
 */
public class HorarioInvalidoException extends RuntimeException {
    public HorarioInvalidoException(String message) {
        super(message);
    }
}
