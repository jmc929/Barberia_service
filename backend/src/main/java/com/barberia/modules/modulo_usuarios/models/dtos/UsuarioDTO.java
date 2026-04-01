package com.barberia.modules.modulo_usuarios.models.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioDTO {
    private Long id;
    private String numeroDocumento;
    private String numeroCelular;
    private String email;
    private String nombrePersona;
    private Integer idEstado;
    private Integer idRol;
    private LocalDateTime fechaRegistro;
    private LocalDateTime ultimoAcceso;
}
