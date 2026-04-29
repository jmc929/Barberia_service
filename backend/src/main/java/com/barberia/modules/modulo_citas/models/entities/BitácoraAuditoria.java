package com.barberia.modules.modulo_citas.models.entities;

import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Entidad que registra cambios en citas (auditoría)
 * Permite investigar conflictos y mantener historial completo
 */
@Entity
@Table(
    name = "tbl_auditoria_cita",
    indexes = {
        @Index(name = "idx_auditoria_cita", columnList = "id_cita"),
        @Index(name = "idx_auditoria_actor", columnList = "id_usuario_actor"),
        @Index(name = "idx_auditoria_timestamp", columnList = "timestamp_evento")
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BitácoraAuditoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_auditoria")
    private Long idAuditoria;

    @Column(name = "id_cita", nullable = false)
    private Long idCita;

    @Column(name = "id_usuario_actor")
    private String idUsuarioActor;

    @Column(name = "accion", nullable = false, length = 50)
    private String accion; // CREATE, UPDATE, CANCEL

    @Column(name = "estado_anterior", length = 50)
    private String estadoAnterior;

    @Column(name = "estado_nuevo", length = 50)
    private String estadoNuevo;

    @Type(JsonType.class)
    @Column(name = "detalles_cambio", columnDefinition = "jsonb")
    private Map<String, Object> detallesCambio;

    @Column(name = "timestamp_evento", nullable = false)
    private LocalDateTime timestampEvento;

    @PrePersist
    protected void onCreate() {
        timestampEvento = LocalDateTime.now();
    }
}
