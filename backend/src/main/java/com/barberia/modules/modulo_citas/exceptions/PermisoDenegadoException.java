package com.barberia.modules.modulo_citas.exceptions;

/**
 * Excepción lanzada cuando el usuario no tiene permisos para realizar una acción
 */
public class PermisoDenegadoException extends RuntimeException {
    public PermisoDenegadoException(String mensaje) {
        super(mensaje);
    }

    public PermisoDenegadoException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
