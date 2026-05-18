package com.barberia.modules.modulo_usuarios.models.dtos;

// Campos opcionales, validación se maneja en el servicio
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdatePerfilDTO {

    private String numeroCelular;

    private String email;

    private String nombrePersona;

    // Campos opcionales para cambio de contraseña
    private String currentPassword;
    private String newPassword;
    private String confirmarNuevaPassword;
}
