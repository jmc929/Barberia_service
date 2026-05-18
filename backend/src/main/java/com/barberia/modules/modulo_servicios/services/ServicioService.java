package com.barberia.modules.modulo_servicios.services;

import com.barberia.modules.modulo_servicios.models.dtos.ServicioDTO;
import com.barberia.modules.modulo_servicios.models.entities.Servicio;
import com.barberia.modules.modulo_servicios.repositories.ServicioRepository;
import com.barberia.shared.exceptions.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.barberia.modules.modulo_citas.repositories.CitaRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ServicioService {

    private static final Long ESTADO_ACTIVO = 1L;

    @Autowired
    private ServicioRepository servicioRepository;

    @Autowired
    private CitaRepository citaRepository;

    public List<ServicioDTO> obtenerTodos() {
        return servicioRepository.findByIdEstado(1L)
                .stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    public ServicioDTO obtenerPorId(Long id) {
        Servicio servicio = servicioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Servicio no encontrado con id: " + id));
        return convertirADTO(servicio);
    }

    public ServicioDTO crear(ServicioDTO servicioDTO) {
        Servicio servicio = Servicio.builder()
                .nombreServicio(servicioDTO.getNombreServicio())
                .descripcion(servicioDTO.getDescripcion())
                .duracion(servicioDTO.getDuracion())
                .costo(servicioDTO.getCosto())
                .build();
        return convertirADTO(servicioRepository.save(servicio));
    }

    private ServicioDTO convertirADTO(Servicio servicio) {
        return ServicioDTO.builder()
                .idServicio(servicio.getIdServicio())
                .nombreServicio(servicio.getNombreServicio())
                .descripcion(servicio.getDescripcion())
                .duracion(servicio.getDuracion())
                .costo(servicio.getCosto())
                .idEstado(servicio.getIdEstado())
                .build();
    }

    public ServicioDTO actualizar(Long id, ServicioDTO servicioDTO) {
        Servicio servicio = servicioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Servicio no encontrado con id: " + id
                ));

        if (servicioDTO.getNombreServicio() != null) {
            servicio.setNombreServicio(servicioDTO.getNombreServicio());
        }

        if (servicioDTO.getDescripcion() != null) {
            servicio.setDescripcion(servicioDTO.getDescripcion());
        }

        if (servicioDTO.getDuracion() != null && servicioDTO.getDuracion() > 0) {
            servicio.setDuracion(servicioDTO.getDuracion());
        }

        if (servicioDTO.getCosto() != null) {
            servicio.setCosto(servicioDTO.getCosto());
        }

        Servicio actualizado = servicioRepository.save(servicio);

        return convertirADTO(actualizado);
    }

    public void eliminar(Long id) {
        Servicio servicio = servicioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Servicio no encontrado con id: " + id
                ));

        if (citaRepository.existsByIdServicioAndIdEstado(id, ESTADO_ACTIVO)) {
            throw new IllegalStateException(
                    "El servicio no se puede eliminar porque tiene citas activas asociadas."
            );
        }

        servicioRepository.delete(servicio);
    }

    public ServicioDTO deshabilitar(Long id) {
    Servicio servicio = servicioRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException(
                    "Servicio no encontrado con id: " + id
            ));

    servicio.setIdEstado(2L);

    Servicio servicioActualizado = servicioRepository.save(servicio);

    return convertirADTO(servicioActualizado);
    }
}