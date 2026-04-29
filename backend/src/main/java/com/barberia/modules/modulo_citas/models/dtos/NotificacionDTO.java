package com.barberia.modules.modulo_citas.models.dtos;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.barberia.modules.modulo_citas.enums.TipoNotificacion;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO para mostrar notificaciones del usuario
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificacionDTO {

    private Long idNotificacion;

    private Long idCita;

    private String tipo;

    private String mensaje;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime fechaEnvio;

    private Boolean leida;
}
