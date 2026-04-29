package com.barberia.modules.modulo_citas.models.entities;

import com.barberia.modules.modulo_citas.enums.TipoNotificacion;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entidad que registra las notificaciones enviadas a usuarios sobre cambios en citas
 * Permite trazabilidad completa de comunicaciones
 */
@Entity
@Table(
    name = "tbl_cita_notificacion",
    indexes = {
        @Index(name = "idx_notificacion_usuario", columnList = "id_usuario_destinatario,leida"),
        @Index(name = "idx_notificacion_cita", columnList = "id_cita")
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CitaNotificacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_notificacion")
    private Long idNotificacion;

    @Column(name = "id_cita", nullable = false)
    private Long idCita;

    @Column(name = "id_usuario_destinatario", nullable = false)
    private String idUsuarioDestinatario;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_notificacion", nullable = false)
    private TipoNotificacion tipoNotificacion;

    @Column(name = "mensaje", nullable = false, columnDefinition = "TEXT")
    private String mensaje;

    @Column(name = "fecha_envio", nullable = false)
    private LocalDateTime fechaEnvio;

    @Column(name = "leida", nullable = false)
    private Boolean leida = false;

    @PrePersist
    protected void onCreate() {
        fechaEnvio = LocalDateTime.now();
    }
}
