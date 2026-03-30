package com.barberia.modules.modulo1.services;

import com.barberia.modules.modulo1.models.dtos.ServicioDTO;
import com.barberia.modules.modulo1.models.entities.Servicio;
import com.barberia.shared.exceptions.ResourceNotFoundException;
import com.barberia.modules.modulo1.repositories.ServicioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ServicioService {

    @Autowired
    private ServicioRepository servicioRepository;

    /**
     * Obtener todos los servicios activos
     */
    public List<ServicioDTO> obtenerTodosLosServicios() {
        return servicioRepository.findByActivoTrue()
                .stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    /**
     * Obtener un servicio por ID
     */
    public ServicioDTO obtenerServicioPorId(Long id) {
        Servicio servicio = servicioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Servicio no encontrado con ID: " + id));
        return convertirADTO(servicio);
    }

    /**
     * Crear un nuevo servicio
     */
    public ServicioDTO crearServicio(ServicioDTO servicioDTO) {
        Servicio servicio = new Servicio();
        servicio.setNombre(servicioDTO.getNombre());
        servicio.setDescripcion(servicioDTO.getDescripcion());
        servicio.setPrecio(servicioDTO.getPrecio());
        servicio.setDuracionMinutos(servicioDTO.getDuracionMinutos());
        servicio.setActivo(true);

        Servicio servicioGuardado = servicioRepository.save(servicio);
        return convertirADTO(servicioGuardado);
    }

    /**
     * Actualizar un servicio existente
     */
    public ServicioDTO actualizarServicio(Long id, ServicioDTO servicioDTO) {
        Servicio servicio = servicioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Servicio no encontrado con ID: " + id));

        servicio.setNombre(servicioDTO.getNombre());
        servicio.setDescripcion(servicioDTO.getDescripcion());
        servicio.setPrecio(servicioDTO.getPrecio());
        servicio.setDuracionMinutos(servicioDTO.getDuracionMinutos());

        Servicio servicioActualizado = servicioRepository.save(servicio);
        return convertirADTO(servicioActualizado);
    }

    /**
     * Eliminar lógicamente un servicio
     */
    public void desactivarServicio(Long id) {
        Servicio servicio = servicioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Servicio no encontrado con ID: " + id));
        servicio.setActivo(false);
        servicioRepository.save(servicio);
    }

    /**
     * Convertir entidad a DTO
     */
    private ServicioDTO convertirADTO(Servicio servicio) {
        return new ServicioDTO(
                servicio.getId(),
                servicio.getNombre(),
                servicio.getDescripcion(),
                servicio.getPrecio(),
                servicio.getDuracionMinutos(),
                servicio.getActivo()
        );
    }
}
