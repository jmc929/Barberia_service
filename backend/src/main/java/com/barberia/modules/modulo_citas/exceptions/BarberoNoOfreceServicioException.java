package com.barberia.modules.modulo_citas.exceptions;

/**
 * Excepción lanzada cuando el barbero no ofrece un servicio específico
 */
public class BarberoNoOfreceServicioException extends RuntimeException {
    public BarberoNoOfreceServicioException(String mensaje) {
        super(mensaje);
    }

    public BarberoNoOfreceServicioException() {
        super("El barbero no ofrece este servicio");
    }

    public BarberoNoOfreceServicioException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
