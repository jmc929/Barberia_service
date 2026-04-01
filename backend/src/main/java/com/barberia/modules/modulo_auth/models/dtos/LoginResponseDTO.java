package com.barberia.modules.modulo_auth.models.dtos;

import com.barberia.modules.modulo_usuarios.models.dtos.UsuarioDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponseDTO {
    private String token;
    @Builder.Default
    private String type = "Bearer";
    private UsuarioDTO usuario;
}
