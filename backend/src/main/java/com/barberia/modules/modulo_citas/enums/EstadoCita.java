package com.barberia.modules.modulo_citas.enums;

/**
 * Estados posibles de una cita
 * CONFIRMADA: Cita confirmada y activa
 * CANCELADA: Cita cancelada por cliente o barbero
 * COMPLETADA: Cita completada exitosamente
 * AUSENTE: Cliente no asistió
 */
public enum EstadoCita {
    CONFIRMADA(1, "Cita confirmada y activa"),
    CANCELADA(2, "Cita cancelada por cliente o barbero"),
    COMPLETADA(3, "Cita completada exitosamente"),
    AUSENTE(4, "Cliente no asistió");

    private final Integer id;
    private final String descripcion;

    EstadoCita(Integer id, String descripcion) {
        this.id = id;
        this.descripcion = descripcion;
    }

    public Integer getId() {
        return id;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public static EstadoCita fromId(Integer id) {
        for (EstadoCita estado : EstadoCita.values()) {
            if (estado.id.equals(id)) {
                return estado;
            }
        }
        throw new IllegalArgumentException("Estado de cita no válido: " + id);
    }
}
