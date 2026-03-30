package com.barberia.modules.modulo2.models.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClienteDTO {
    private Long id;
    private String nombre;
    private String email;
    private String telefono;
    private String direccion;
    private Boolean activo;
}
