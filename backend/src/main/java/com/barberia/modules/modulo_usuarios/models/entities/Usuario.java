package com.barberia.modules.modulo_usuarios.models.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "tbl_personas", uniqueConstraints = {
    @UniqueConstraint(columnNames = "numero_documento"),
    @UniqueConstraint(columnNames = "email")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Usuario {

    @Id
    @Column(name = "numero_documento", length = 20)
    private String numeroDocumento;

    @Column(name = "numero_celular", nullable = false, length = 20)
    private String numeroCelular;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @Column(name = "nombre_persona", nullable = false, length = 255)
    private String nombrePersona;

    @Column(name = "contraseña_hasheada", nullable = false)
    private String contrasenaHasheada;

    @Column(name = "id_estado", nullable = false)
    @Builder.Default
    private Integer idEstado = 1;

    @Column(name = "id_rol", nullable = false)
    @Builder.Default
    private Integer idRol = 3;

    @Column(name = "fecha_registro", nullable = false, updatable = false)
    private LocalDateTime fechaRegistro;


    @PrePersist
    protected void onCreate() {
        if (fechaRegistro == null) {
            fechaRegistro = LocalDateTime.now();
        }
    }
}
