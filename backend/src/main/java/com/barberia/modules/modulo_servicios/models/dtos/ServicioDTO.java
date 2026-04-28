package com.barberia.modules.modulo_servicios.models.dtos;

import lombok.*;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServicioDTO {
    private Long idServicio;
    private String nombreServicio;
    private String descripcion;
    private Integer duracion;
    private BigDecimal costo;
    private Long idEstado;
}
