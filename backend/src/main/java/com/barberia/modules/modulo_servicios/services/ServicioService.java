package com.barberia.modules.modulo_servicios.services;

import com.barberia.modules.modulo_servicios.models.dtos.ServicioDTO;
import com.barberia.modules.modulo_servicios.models.entities.Servicio;
import com.barberia.modules.modulo_servicios.repositories.ServicioRepository;
import com.barberia.shared.exceptions.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ServicioService {

    @Autowired
    private ServicioRepository servicioRepository;

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
}
