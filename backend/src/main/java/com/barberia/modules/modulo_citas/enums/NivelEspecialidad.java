package com.barberia.modules.modulo_citas.enums;

/**
 * Niveles de especialidad de un barbero para un servicio
 * JUNIOR: Barbero en formación, tarifas reducidas
 * SENIOR: Barbero con experiencia, tarifa estándar
 * EXPERTO: Barbero experto, tarifa premium
 */
public enum NivelEspecialidad {
    JUNIOR("Barbero en formación"),
    SENIOR("Barbero con experiencia"),
    EXPERTO("Barbero experto");

    private final String descripcion;

    NivelEspecialidad(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
