package com.barberia.modules.modulo_citas.enums;

/**
 * Tipos de notificaciones en el sistema de citas
 * CONFIRMACION: Se confirma una nueva cita
 * CANCELACION: Se cancela una cita existente
 * RECORDATORIO_24H: Recordatorio 24 horas antes
 * CAMBIO_HORARIO: Se reprograma una cita existente
 */
public enum TipoNotificacion {
    CONFIRMACION("Confirmación de cita"),
    CANCELACION("Cancelación de cita"),
    RECORDATORIO_24H("Recordatorio 24 horas antes"),
    CAMBIO_HORARIO("Cambio de horario de cita");

    private final String descripcion;

    TipoNotificacion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
