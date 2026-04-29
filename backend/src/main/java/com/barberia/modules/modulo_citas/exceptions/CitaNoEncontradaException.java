package com.barberia.modules.modulo_citas.exceptions;

/**
 * Excepción lanzada cuando no se encuentra una cita
 */
public class CitaNoEncontradaException extends RuntimeException {
    public CitaNoEncontradaException(String mensaje) {
        super(mensaje);
    }

    public CitaNoEncontradaException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
