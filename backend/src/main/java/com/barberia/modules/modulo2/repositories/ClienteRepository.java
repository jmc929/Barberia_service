package com.barberia.modules.modulo2.repositories;

import com.barberia.modules.modulo2.models.entities.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {
    
    List<Cliente> findByActivoTrue();
    
    Optional<Cliente> findByEmail(String email);
    
    Optional<Cliente> findByTelefono(String telefono);
    
    List<Cliente> findByNombreContainingIgnoreCase(String nombre);
}
