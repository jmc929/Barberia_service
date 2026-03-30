package com.barberia.modules.modulo2.services;

import com.barberia.modules.modulo2.models.dtos.ClienteDTO;
import com.barberia.modules.modulo2.models.entities.Cliente;
import com.barberia.shared.exceptions.ResourceNotFoundException;
import com.barberia.modules.modulo2.repositories.ClienteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ClienteService {

    @Autowired
    private ClienteRepository clienteRepository;

    /**
     * Obtener todos los clientes activos
     */
    public List<ClienteDTO> obtenerTodosLosClientes() {
        return clienteRepository.findByActivoTrue()
                .stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    /**
     * Obtener un cliente por ID
     */
    public ClienteDTO obtenerClientePorId(Long id) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado con ID: " + id));
        return convertirADTO(cliente);
    }

    /**
     * Buscar cliente por email
     */
    public ClienteDTO obtenerClientePorEmail(String email) {
        Cliente cliente = clienteRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado con Email: " + email));
        return convertirADTO(cliente);
    }

    /**
     * Crear un nuevo cliente
     */
    public ClienteDTO crearCliente(ClienteDTO clienteDTO) {
        // Verificar si el email ya existe
        if (clienteRepository.findByEmail(clienteDTO.getEmail()).isPresent()) {
            throw new RuntimeException("El email ya está registrado");
        }

        Cliente cliente = new Cliente();
        cliente.setNombre(clienteDTO.getNombre());
        cliente.setEmail(clienteDTO.getEmail());
        cliente.setTelefono(clienteDTO.getTelefono());
        cliente.setDireccion(clienteDTO.getDireccion());
        cliente.setActivo(true);

        Cliente clienteGuardado = clienteRepository.save(cliente);
        return convertirADTO(clienteGuardado);
    }

    /**
     * Actualizar un cliente existente
     */
    public ClienteDTO actualizarCliente(Long id, ClienteDTO clienteDTO) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado con ID: " + id));

        cliente.setNombre(clienteDTO.getNombre());
        cliente.setTelefono(clienteDTO.getTelefono());
        cliente.setDireccion(clienteDTO.getDireccion());

        Cliente clienteActualizado = clienteRepository.save(cliente);
        return convertirADTO(clienteActualizado);
    }

    /**
     * Desactivar un cliente
     */
    public void desactivarCliente(Long id) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado con ID: " + id));
        cliente.setActivo(false);
        clienteRepository.save(cliente);
    }

    /**
     * Convertir entidad a DTO
     */
    private ClienteDTO convertirADTO(Cliente cliente) {
        return new ClienteDTO(
                cliente.getId(),
                cliente.getNombre(),
                cliente.getEmail(),
                cliente.getTelefono(),
                cliente.getDireccion(),
                cliente.getActivo()
        );
    }
}
