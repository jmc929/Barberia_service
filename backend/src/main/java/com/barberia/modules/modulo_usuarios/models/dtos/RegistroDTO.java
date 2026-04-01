package com.barberia.modules.modulo_usuarios.models.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegistroDTO {
    private String numeroDocumento;
    private String numeroCelular;
    private String email;
    private String nombrePersona;
    private String contraseña;
    private String confirmarContraseña;
}
