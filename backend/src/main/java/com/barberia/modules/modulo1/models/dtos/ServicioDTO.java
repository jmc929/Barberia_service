package com.barberia.modules.modulo1.models.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ServicioDTO {
    private Long id;
    private String nombre;
    private String descripcion;
    private Double precio;
    private Integer duracionMinutos;
    private Boolean activo;
}
