package com.barberia.modules.modulo_usuarios.repositories;

import com.barberia.modules.modulo_usuarios.models.entities.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByNumeroDocumento(String numeroDocumento);
    Optional<Usuario> findByEmail(String email);
    boolean existsByNumeroDocumento(String numeroDocumento);
    boolean existsByEmail(String email);
}
