package com.barberia.modules.modulo_citas.exceptions;

/**
 * Excepción lanzada cuando un barbero no tiene disponibilidad para agendar una cita
 */
public class BarberoNoDisponibleException extends RuntimeException {
    public BarberoNoDisponibleException(String mensaje) {
        super(mensaje);
    }

    public BarberoNoDisponibleException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
