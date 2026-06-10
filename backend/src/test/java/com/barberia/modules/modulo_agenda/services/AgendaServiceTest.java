package com.barberia.modules.modulo_agenda.services;

import com.barberia.modules.modulo_agenda.models.dtos.AgendaResponseDTO;
import com.barberia.modules.modulo_citas.models.entities.Cita;
import com.barberia.modules.modulo_citas.repositories.CitaRepository;
import com.barberia.modules.modulo_servicios.models.entities.Servicio;
import com.barberia.modules.modulo_servicios.repositories.ServicioRepository;
import com.barberia.modules.modulo_usuarios.models.entities.Usuario;
import com.barberia.modules.modulo_usuarios.repositories.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AgendaServiceTest {

    @Mock
    private CitaRepository citaRepository;
    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private ServicioRepository servicioRepository;

    @InjectMocks
    private AgendaService agendaService;

    private Cita cita;
    private Usuario cliente;
    private Servicio servicio;

    @BeforeEach
    void setUp() {
        cliente = Usuario.builder()
                .numeroDocumento("123")
                .nombrePersona("Cliente Test")
                .build();

        servicio = Servicio.builder()
                .idServicio(1L)
                .nombreServicio("Corte de cabello")
                .build();

        cita = Cita.builder()
                .noCita(1L)
                .numeroDocumentoCliente("123")
                .numeroDocumentoPeluquero("456")
                .idServicio(1L)
                .fechaCita(LocalDate.of(2026, 6, 15))
                .horaInicioCita(LocalTime.of(9, 0))
                .horaFinCita(LocalTime.of(10, 0))
                .idEstado(1L)
                .build();
    }

    @Test
    void obtenerAgendaPeluquero_exitoso() {
        when(citaRepository.findByNumeroDocumentoPeluqueroAndIdEstadoOrderByFechaCitaAscHoraInicioCitaAsc(
                "456", 1L)).thenReturn(List.of(cita));
        when(usuarioRepository.findByNumeroDocumento("123")).thenReturn(Optional.of(cliente));
        when(servicioRepository.findById(1L)).thenReturn(Optional.of(servicio));

        List<AgendaResponseDTO> result = agendaService.obtenerAgendaPeluquero("456");

        assertEquals(1, result.size());
        assertEquals("Cliente Test", result.get(0).getNombreCliente());
        assertEquals("Corte de cabello", result.get(0).getNombreServicio());
        assertEquals("ACTIVO", result.get(0).getEstado());
    }

    @Test
    void obtenerAgendaPeluquero_cuandoClienteNoExiste() {
        when(citaRepository.findByNumeroDocumentoPeluqueroAndIdEstadoOrderByFechaCitaAscHoraInicioCitaAsc(
                "456", 1L)).thenReturn(List.of(cita));
        when(usuarioRepository.findByNumeroDocumento("123")).thenReturn(Optional.empty());
        when(servicioRepository.findById(1L)).thenReturn(Optional.of(servicio));

        List<AgendaResponseDTO> result = agendaService.obtenerAgendaPeluquero("456");

        assertEquals(1, result.size());
        assertEquals("Desconocido", result.get(0).getNombreCliente());
    }

    @Test
    void obtenerAgendaPeluquero_cuandoServicioNoExiste() {
        when(citaRepository.findByNumeroDocumentoPeluqueroAndIdEstadoOrderByFechaCitaAscHoraInicioCitaAsc(
                "456", 1L)).thenReturn(List.of(cita));
        when(usuarioRepository.findByNumeroDocumento("123")).thenReturn(Optional.of(cliente));
        when(servicioRepository.findById(1L)).thenReturn(Optional.empty());

        List<AgendaResponseDTO> result = agendaService.obtenerAgendaPeluquero("456");

        assertEquals(1, result.size());
        assertEquals("Desconocido", result.get(0).getNombreServicio());
    }

    @Test
    void obtenerAgendaPeluquero_cuandoEstadoCompletada_usaNombreCorrecto() {
        cita.setIdEstado(6L);
        when(citaRepository.findByNumeroDocumentoPeluqueroAndIdEstadoOrderByFechaCitaAscHoraInicioCitaAsc(
                "456", 1L)).thenReturn(List.of(cita));
        when(usuarioRepository.findByNumeroDocumento("123")).thenReturn(Optional.of(cliente));
        when(servicioRepository.findById(1L)).thenReturn(Optional.of(servicio));

        List<AgendaResponseDTO> result = agendaService.obtenerAgendaPeluquero("456");

        assertEquals("COMPLETADA", result.get(0).getEstado());
    }

    @Test
    void obtenerAgendaPeluquero_cuandoEstadoCancelada_usaNombreCorrecto() {
        cita.setIdEstado(3L);
        when(citaRepository.findByNumeroDocumentoPeluqueroAndIdEstadoOrderByFechaCitaAscHoraInicioCitaAsc(
                "456", 1L)).thenReturn(List.of(cita));
        when(usuarioRepository.findByNumeroDocumento("123")).thenReturn(Optional.of(cliente));
        when(servicioRepository.findById(1L)).thenReturn(Optional.of(servicio));

        List<AgendaResponseDTO> result = agendaService.obtenerAgendaPeluquero("456");

        assertEquals("CANCELADA", result.get(0).getEstado());
    }

    @Test
    void obtenerAgendaPeluquero_cuandoEstadoNulo_retornaDesconocido() {
        cita.setIdEstado(null);
        when(citaRepository.findByNumeroDocumentoPeluqueroAndIdEstadoOrderByFechaCitaAscHoraInicioCitaAsc(
                "456", 1L)).thenReturn(List.of(cita));
        when(usuarioRepository.findByNumeroDocumento("123")).thenReturn(Optional.of(cliente));
        when(servicioRepository.findById(1L)).thenReturn(Optional.of(servicio));

        List<AgendaResponseDTO> result = agendaService.obtenerAgendaPeluquero("456");

        assertEquals("DESCONOCIDO", result.get(0).getEstado());
    }
}
