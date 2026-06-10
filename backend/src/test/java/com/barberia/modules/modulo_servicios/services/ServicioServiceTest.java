package com.barberia.modules.modulo_servicios.services;

import com.barberia.modules.modulo_citas.repositories.CitaRepository;
import com.barberia.modules.modulo_servicios.models.dtos.ServicioDTO;
import com.barberia.modules.modulo_servicios.models.entities.Servicio;
import com.barberia.modules.modulo_servicios.repositories.ServicioRepository;
import com.barberia.shared.exceptions.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ServicioServiceTest {

    @Mock
    private ServicioRepository servicioRepository;
    @Mock
    private CitaRepository citaRepository;

    @InjectMocks
    private ServicioService servicioService;

    private Servicio servicio;
    private ServicioDTO servicioDTO;

    @BeforeEach
    void setUp() {
        servicio = Servicio.builder()
                .idServicio(1L)
                .nombreServicio("Corte de cabello")
                .descripcion("Corte moderno")
                .duracion(30)
                .costo(new BigDecimal("25000"))
                .idEstado(1L)
                .build();

        servicioDTO = ServicioDTO.builder()
                .nombreServicio("Corte de cabello")
                .descripcion("Corte moderno")
                .duracion(30)
                .costo(new BigDecimal("25000"))
                .build();
    }

    @Test
    void obtenerTodos_retornaSoloActivos() {
        when(servicioRepository.findByIdEstado(1L)).thenReturn(List.of(servicio));

        List<ServicioDTO> result = servicioService.obtenerTodos();

        assertEquals(1, result.size());
        assertEquals("Corte de cabello", result.get(0).getNombreServicio());
    }

    @Test
    void obtenerPorId_exitoso() {
        when(servicioRepository.findById(1L)).thenReturn(Optional.of(servicio));

        ServicioDTO result = servicioService.obtenerPorId(1L);

        assertNotNull(result);
        assertEquals(1L, result.getIdServicio());
    }

    @Test
    void obtenerPorId_cuandoNoExiste_lanzaExcepcion() {
        when(servicioRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> servicioService.obtenerPorId(99L));
    }

    @Test
    void crear_exitoso() {
        when(servicioRepository.save(any(Servicio.class))).thenAnswer(i -> {
            Servicio s = i.getArgument(0);
            s.setIdServicio(2L);
            return s;
        });

        ServicioDTO result = servicioService.crear(servicioDTO);

        assertNotNull(result);
        assertEquals("Corte de cabello", result.getNombreServicio());
    }

    @Test
    void actualizar_exitoso() {
        ServicioDTO updateDTO = ServicioDTO.builder()
                .nombreServicio("Corte actualizado")
                .descripcion("Descripcion actualizada")
                .duracion(45)
                .costo(new BigDecimal("30000"))
                .build();
        when(servicioRepository.findById(1L)).thenReturn(Optional.of(servicio));
        when(servicioRepository.save(any(Servicio.class))).thenReturn(servicio);

        ServicioDTO result = servicioService.actualizar(1L, updateDTO);

        assertNotNull(result);
        assertEquals("Corte actualizado", servicio.getNombreServicio());
        assertEquals(45, servicio.getDuracion());
        assertEquals(0, new BigDecimal("30000").compareTo(servicio.getCosto()));
    }

    @Test
    void actualizar_cuandoNoExiste_lanzaExcepcion() {
        when(servicioRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> servicioService.actualizar(99L, servicioDTO));
    }

    @Test
    void actualizar_camposParciales() {
        ServicioDTO updateParcial = ServicioDTO.builder()
                .costo(new BigDecimal("35000"))
                .build();
        when(servicioRepository.findById(1L)).thenReturn(Optional.of(servicio));
        when(servicioRepository.save(any(Servicio.class))).thenReturn(servicio);

        servicioService.actualizar(1L, updateParcial);

        assertEquals("Corte de cabello", servicio.getNombreServicio());
        assertEquals(0, new BigDecimal("35000").compareTo(servicio.getCosto()));
    }

    @Test
    void eliminar_exitoso() {
        when(servicioRepository.findById(1L)).thenReturn(Optional.of(servicio));
        when(citaRepository.existsByIdServicioAndIdEstado(1L, 1L)).thenReturn(false);

        assertDoesNotThrow(() -> servicioService.eliminar(1L));
        verify(servicioRepository, times(1)).delete(servicio);
    }

    @Test
    void eliminar_cuandoTieneCitasActivas_lanzaExcepcion() {
        when(servicioRepository.findById(1L)).thenReturn(Optional.of(servicio));
        when(citaRepository.existsByIdServicioAndIdEstado(1L, 1L)).thenReturn(true);

        assertThrows(IllegalStateException.class,
                () -> servicioService.eliminar(1L));
    }

    @Test
    void eliminar_cuandoNoExiste_lanzaExcepcion() {
        when(servicioRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> servicioService.eliminar(99L));
    }

    @Test
    void deshabilitar_exitoso() {
        when(servicioRepository.findById(1L)).thenReturn(Optional.of(servicio));
        when(servicioRepository.save(any(Servicio.class))).thenReturn(servicio);

        ServicioDTO result = servicioService.deshabilitar(1L);

        assertNotNull(result);
        assertEquals(2L, result.getIdEstado().longValue());
    }

    @Test
    void deshabilitar_cuandoNoExiste_lanzaExcepcion() {
        when(servicioRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> servicioService.deshabilitar(99L));
    }
}
