package com.barberia.modules.modulo_agendamiento.services;

import com.barberia.modules.modulo_agendamiento.models.dtos.*;
import com.barberia.modules.modulo_agendamiento.models.entities.Cita;
import com.barberia.modules.modulo_agendamiento.repositories.CitaRepository;
import com.barberia.shared.exceptions.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.CannotSerializeTransactionException;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CitaAgendamientoServiceTest {

    @Mock
    private CitaRepository citaRepository;
    @Mock
    private CitaAgendamientoValidator validator;

    @InjectMocks
    private CitaAgendamientoService citaAgendamientoService;

    private Cita citaActiva;
    private CitaCreateDTO citaCreateDTO;
    private CitaDisponibilidadRequestDTO disponibilidadRequest;
    private CitaReprogramarDTO reprogramarDTO;

    @BeforeEach
    void setUp() {
        citaActiva = Cita.builder()
                .noCita(1L)
                .numeroDocumentoCliente("123")
                .numeroDocumentoPeluquero("456")
                .idServicio(1L)
                .fechaCita(LocalDate.of(2026, 6, 15))
                .horaInicioCita(LocalTime.of(9, 0))
                .horaFinCita(LocalTime.of(10, 0))
                .idEstado(1L)
                .citaConfirmada(false)
                .build();

        citaCreateDTO = CitaCreateDTO.builder()
                .numeroDocumentoPeluquero("456")
                .idServicio(1L)
                .fechaCita(LocalDate.of(2026, 6, 15))
                .horaInicioCita(LocalTime.of(9, 0))
                .horaFinCita(LocalTime.of(10, 0))
                .build();

        disponibilidadRequest = CitaDisponibilidadRequestDTO.builder()
                .numeroDocumentoPeluquero("456")
                .idServicio(1L)
                .fechaCita(LocalDate.of(2026, 6, 15))
                .horaInicioCita(LocalTime.of(9, 0))
                .horaFinCita(LocalTime.of(10, 0))
                .build();

        reprogramarDTO = CitaReprogramarDTO.builder()
                .fechaCita(LocalDate.of(2026, 6, 16))
                .horaInicioCita(LocalTime.of(10, 0))
                .horaFinCita(LocalTime.of(11, 0))
                .build();
    }

    @Test
    void consultarDisponibilidad_cuandoDisponible_retornaDisponible() {
        doNothing().when(validator).validarSolicitudDisponibilidad(any());
        doNothing().when(validator).validarHorarioNegocio(any(), any(), any());
        when(citaRepository.findConflicts(anyString(), any(), anyLong(), any(), any()))
                .thenReturn(List.of());

        CitaDisponibilidadResponseDTO response = citaAgendamientoService.consultarDisponibilidad(disponibilidadRequest);

        assertTrue(response.isDisponible());
        assertEquals("Disponible", response.getMensaje());
    }

    @Test
    void consultarDisponibilidad_cuandoNoDisponible_retornaNoDisponible() {
        doNothing().when(validator).validarSolicitudDisponibilidad(any());
        doNothing().when(validator).validarHorarioNegocio(any(), any(), any());
        when(citaRepository.findConflicts(anyString(), any(), anyLong(), any(), any()))
                .thenReturn(List.of(citaActiva));

        CitaDisponibilidadResponseDTO response = citaAgendamientoService.consultarDisponibilidad(disponibilidadRequest);

        assertFalse(response.isDisponible());
        assertTrue(response.getMensaje().contains("No disponible"));
    }

    @Test
    void consultarDisponibilidad_cuandoRequestNulo_lanzaExcepcion() {
        doThrow(new IllegalArgumentException("Request es requerido"))
                .when(validator).validarSolicitudDisponibilidad(null);

        assertThrows(IllegalArgumentException.class,
                () -> citaAgendamientoService.consultarDisponibilidad(null));
    }

    @Test
    void agendar_cuandoExitoso_retornaCitaDTO() {
        doNothing().when(validator).validarSolicitudAgendamiento(any(), anyString());
        when(validator.obtenerClienteActivo(anyString())).thenReturn(null);
        when(validator.obtenerPeluqueroActivo(anyString())).thenReturn(null);
        doNothing().when(validator).validarHorarioNegocio(any(), any(), any());
        when(citaRepository.findConflictsForUpdate(anyString(), any(), anyLong(), any(), any()))
                .thenReturn(List.of());
        when(citaRepository.save(any(Cita.class))).thenAnswer(i -> {
            Cita c = i.getArgument(0);
            c.setNoCita(10L);
            return c;
        });

        CitaDTO result = citaAgendamientoService.agendar(citaCreateDTO, "123");

        assertNotNull(result);
        assertEquals(10L, result.getNoCita());
        assertEquals("123", result.getNumeroDocumentoCliente());
    }

    @Test
    void agendar_cuandoRequestNulo_lanzaExcepcion() {
        doThrow(new IllegalArgumentException("Request es requerido"))
                .when(validator).validarSolicitudAgendamiento(null, "123");

        assertThrows(IllegalArgumentException.class,
                () -> citaAgendamientoService.agendar(null, "123"));
    }

    @Test
    void agendar_cuandoClienteBloqueado_lanzaExcepcion() {
        doNothing().when(validator).validarSolicitudAgendamiento(any(), anyString());
        when(validator.obtenerClienteActivo("123"))
                .thenThrow(new IllegalArgumentException("Tu cuenta está bloqueada"));

        assertThrows(IllegalArgumentException.class,
                () -> citaAgendamientoService.agendar(citaCreateDTO, "123"));
    }

    @Test
    void agendar_cuandoPeluqueroNoEsBarbero_lanzaExcepcion() {
        doNothing().when(validator).validarSolicitudAgendamiento(any(), anyString());
        when(validator.obtenerClienteActivo(anyString())).thenReturn(null);
        when(validator.obtenerPeluqueroActivo("456"))
                .thenThrow(new IllegalArgumentException("No es barbero"));

        assertThrows(IllegalArgumentException.class,
                () -> citaAgendamientoService.agendar(citaCreateDTO, "123"));
    }

    @Test
    void agendar_cuandoPeluqueroDeshabilitado_lanzaExcepcion() {
        doNothing().when(validator).validarSolicitudAgendamiento(any(), anyString());
        when(validator.obtenerClienteActivo(anyString())).thenReturn(null);
        when(validator.obtenerPeluqueroActivo("456"))
                .thenThrow(new IllegalArgumentException("No se puede, el barbero esta deshabilitado"));

        assertThrows(IllegalArgumentException.class,
                () -> citaAgendamientoService.agendar(citaCreateDTO, "123"));
    }

    @Test
    void agendar_cuandoHayConflicto_lanzaExcepcion() {
        doNothing().when(validator).validarSolicitudAgendamiento(any(), anyString());
        when(validator.obtenerClienteActivo(anyString())).thenReturn(null);
        when(validator.obtenerPeluqueroActivo(anyString())).thenReturn(null);
        doNothing().when(validator).validarHorarioNegocio(any(), any(), any());
        when(citaRepository.findConflictsForUpdate(anyString(), any(), anyLong(), any(), any()))
                .thenReturn(List.of(citaActiva));

        assertThrows(IllegalArgumentException.class,
                () -> citaAgendamientoService.agendar(citaCreateDTO, "123"));
    }

    @Test
    void agendar_cuandoConcurrencia_ReintentaYLanzaExcepcion() {
        doNothing().when(validator).validarSolicitudAgendamiento(any(), anyString());
        when(validator.obtenerClienteActivo(anyString())).thenReturn(null);
        when(validator.obtenerPeluqueroActivo(anyString())).thenReturn(null);
        doNothing().when(validator).validarHorarioNegocio(any(), any(), any());
        when(citaRepository.findConflictsForUpdate(anyString(), any(), anyLong(), any(), any()))
                .thenReturn(List.of());
        when(citaRepository.save(any(Cita.class)))
                .thenThrow(CannotSerializeTransactionException.class); // NOSONAR - deprecated but functional

        assertThrows(CannotSerializeTransactionException.class, // NOSONAR
                () -> citaAgendamientoService.agendar(citaCreateDTO, "123"));
    }

    @Test
    void cancelar_cuandoExitoso_retornaCitaCancelada() {
        doNothing().when(validator).validarNoCita(1L);
        when(citaRepository.findById(1L)).thenReturn(Optional.of(citaActiva));
        doNothing().when(validator).validarPermisoGestionCita(any(), anyString(), anyString());
        when(citaRepository.save(any(Cita.class))).thenReturn(citaActiva);

        CitaDTO result = citaAgendamientoService.cancelar(1L, "Cliente no pudo asistir", "123");

        assertNotNull(result);
        assertEquals("Cliente no pudo asistir", citaActiva.getMotivoCancelacion());
        assertEquals(3L, citaActiva.getIdEstado().longValue());
    }

    @Test
    void cancelar_cuandoMotivoNulo_lanzaExcepcion() {
        doNothing().when(validator).validarNoCita(1L);

        assertThrows(IllegalArgumentException.class,
                () -> citaAgendamientoService.cancelar(1L, null, "123"));
    }

    @Test
    void cancelar_cuandoMotivoVacio_lanzaExcepcion() {
        doNothing().when(validator).validarNoCita(1L);

        assertThrows(IllegalArgumentException.class,
                () -> citaAgendamientoService.cancelar(1L, "", "123"));
    }

    @Test
    void cancelar_cuandoNoEncontrada_lanzaExcepcion() {
        doNothing().when(validator).validarNoCita(99L);
        when(citaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> citaAgendamientoService.cancelar(99L, "motivo", "123"));
    }

    @Test
    void cancelar_cuandoNoTienePermiso_lanzaExcepcion() {
        doNothing().when(validator).validarNoCita(1L);
        when(citaRepository.findById(1L)).thenReturn(Optional.of(citaActiva));
        doThrow(new IllegalArgumentException("No tienes permiso"))
                .when(validator).validarPermisoGestionCita(any(), eq("999"), anyString());

        assertThrows(IllegalArgumentException.class,
                () -> citaAgendamientoService.cancelar(1L, "motivo", "999"));
    }

    @Test
    void cancelar_cuandoCitaNoActiva_lanzaExcepcion() {
        citaActiva.setIdEstado(3L);
        doNothing().when(validator).validarNoCita(1L);
        when(citaRepository.findById(1L)).thenReturn(Optional.of(citaActiva));
        doNothing().when(validator).validarPermisoGestionCita(any(), anyString(), anyString());

        assertThrows(IllegalArgumentException.class,
                () -> citaAgendamientoService.cancelar(1L, "motivo", "123"));
    }

    @Test
    void cancelar_cuandoPeluqueroCancela_exitoso() {
        doNothing().when(validator).validarNoCita(1L);
        when(citaRepository.findById(1L)).thenReturn(Optional.of(citaActiva));
        doNothing().when(validator).validarPermisoGestionCita(any(), anyString(), anyString());
        when(citaRepository.save(any(Cita.class))).thenReturn(citaActiva);

        CitaDTO result = citaAgendamientoService.cancelar(1L, "Cancelado por peluquero", "456");

        assertNotNull(result);
        assertEquals(3L, citaActiva.getIdEstado().longValue());
    }

    @Test
    void confirmar_cuandoExitoso_confirmaCita() {
        doNothing().when(validator).validarNoCita(1L);
        when(citaRepository.findById(1L)).thenReturn(Optional.of(citaActiva));
        when(citaRepository.save(any(Cita.class))).thenReturn(citaActiva);

        CitaDTO result = citaAgendamientoService.confirmar(1L, "456");

        assertNotNull(result);
        assertTrue(citaActiva.getCitaConfirmada());
    }

    @Test
    void confirmar_cuandoNoEsPeluqueroAsignado_lanzaExcepcion() {
        doNothing().when(validator).validarNoCita(1L);
        when(citaRepository.findById(1L)).thenReturn(Optional.of(citaActiva));

        assertThrows(IllegalArgumentException.class,
                () -> citaAgendamientoService.confirmar(1L, "999"));
    }

    @Test
    void confirmar_cuandoCitaNoActiva_lanzaExcepcion() {
        citaActiva.setIdEstado(3L);
        doNothing().when(validator).validarNoCita(1L);
        when(citaRepository.findById(1L)).thenReturn(Optional.of(citaActiva));

        assertThrows(IllegalArgumentException.class,
                () -> citaAgendamientoService.confirmar(1L, "456"));
    }

    @Test
    void confirmar_cuandoCitaNoEncontrada_lanzaExcepcion() {
        doNothing().when(validator).validarNoCita(99L);
        when(citaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> citaAgendamientoService.confirmar(99L, "456"));
    }

    @Test
    void reprogramar_cuandoExitoso_retornaCitaReprogramada() {
        doNothing().when(validator).validarNoCita(1L);
        doNothing().when(validator).validarSolicitudReprogramacion(any(), anyString());
        when(citaRepository.findById(1L)).thenReturn(Optional.of(citaActiva));
        doNothing().when(validator).validarPermisoGestionCita(any(), anyString(), anyString());
        doNothing().when(validator).validarHorarioNegocio(any(), any(), any());
        when(citaRepository.findConflictsForUpdateExcludingNoCita(anyString(), any(), any(), anyLong(), any(), any()))
                .thenReturn(List.of());
        when(citaRepository.save(any(Cita.class))).thenReturn(citaActiva);

        CitaDTO result = citaAgendamientoService.reprogramar(1L, reprogramarDTO, "123");

        assertNotNull(result);
        assertEquals(LocalDate.of(2026, 6, 16), citaActiva.getFechaCita());
    }

    @Test
    void reprogramar_cuandoRequestNulo_lanzaExcepcion() {
        doNothing().when(validator).validarNoCita(1L);
        doThrow(new IllegalArgumentException("Request es requerido"))
                .when(validator).validarSolicitudReprogramacion(null, "123");

        assertThrows(IllegalArgumentException.class,
                () -> citaAgendamientoService.reprogramar(1L, null, "123"));
    }

    @Test
    void reprogramar_cuandoCitaNoEncontrada_lanzaExcepcion() {
        doNothing().when(validator).validarNoCita(99L);
        doNothing().when(validator).validarSolicitudReprogramacion(any(), anyString());
        when(citaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> citaAgendamientoService.reprogramar(99L, reprogramarDTO, "123"));
    }

    @Test
    void reprogramar_cuandoNoTienePermiso_lanzaExcepcion() {
        doNothing().when(validator).validarNoCita(1L);
        doNothing().when(validator).validarSolicitudReprogramacion(any(), anyString());
        when(citaRepository.findById(1L)).thenReturn(Optional.of(citaActiva));
        doThrow(new IllegalArgumentException("No puedes reprogramar"))
                .when(validator).validarPermisoGestionCita(any(), eq("999"), anyString());

        assertThrows(IllegalArgumentException.class,
                () -> citaAgendamientoService.reprogramar(1L, reprogramarDTO, "999"));
    }

    @Test
    void reprogramar_cuandoCitaNoActiva_lanzaExcepcion() {
        citaActiva.setIdEstado(3L);
        doNothing().when(validator).validarNoCita(1L);
        doNothing().when(validator).validarSolicitudReprogramacion(any(), anyString());
        when(citaRepository.findById(1L)).thenReturn(Optional.of(citaActiva));
        doNothing().when(validator).validarPermisoGestionCita(any(), anyString(), anyString());

        assertThrows(IllegalArgumentException.class,
                () -> citaAgendamientoService.reprogramar(1L, reprogramarDTO, "123"));
    }

    @Test
    void reprogramar_cuandoHayConflicto_lanzaExcepcion() {
        doNothing().when(validator).validarNoCita(1L);
        doNothing().when(validator).validarSolicitudReprogramacion(any(), anyString());
        when(citaRepository.findById(1L)).thenReturn(Optional.of(citaActiva));
        doNothing().when(validator).validarPermisoGestionCita(any(), anyString(), anyString());
        doNothing().when(validator).validarHorarioNegocio(any(), any(), any());
        when(citaRepository.findConflictsForUpdateExcludingNoCita(anyString(), any(), any(), anyLong(), any(), any()))
                .thenReturn(List.of(citaActiva));

        assertThrows(IllegalArgumentException.class,
                () -> citaAgendamientoService.reprogramar(1L, reprogramarDTO, "123"));
    }

    @Test
    void reprogramar_cuandoConcurrencia_ReintentaYLanzaExcepcion() {
        doNothing().when(validator).validarNoCita(1L);
        doNothing().when(validator).validarSolicitudReprogramacion(any(), anyString());
        when(citaRepository.findById(1L)).thenReturn(Optional.of(citaActiva));
        doNothing().when(validator).validarPermisoGestionCita(any(), anyString(), anyString());
        doNothing().when(validator).validarHorarioNegocio(any(), any(), any());
        when(citaRepository.findConflictsForUpdateExcludingNoCita(anyString(), any(), any(), anyLong(), any(), any()))
                .thenReturn(List.of());
        when(citaRepository.save(any(Cita.class)))
                .thenThrow(CannotSerializeTransactionException.class); // NOSONAR - deprecated but functional

        assertThrows(CannotSerializeTransactionException.class, // NOSONAR
                () -> citaAgendamientoService.reprogramar(1L, reprogramarDTO, "123"));
    }

    @Test
    void cancelar_cuandoSolicitanteNulo_lanzaExcepcion() {
        doNothing().when(validator).validarNoCita(1L);
        when(citaRepository.findById(1L)).thenReturn(Optional.of(citaActiva));
        doThrow(new IllegalArgumentException("numeroDocumento solicitante no encontrado en el token"))
                .when(validator).validarPermisoGestionCita(any(), isNull(), anyString());

        assertThrows(IllegalArgumentException.class,
                () -> citaAgendamientoService.cancelar(1L, "motivo", null));
    }

    @Test
    void cancelar_cuandoSolicitanteVacio_lanzaExcepcion() {
        doNothing().when(validator).validarNoCita(1L);
        when(citaRepository.findById(1L)).thenReturn(Optional.of(citaActiva));
        doThrow(new IllegalArgumentException("numeroDocumento solicitante no encontrado en el token"))
                .when(validator).validarPermisoGestionCita(any(), eq(""), anyString());

        assertThrows(IllegalArgumentException.class,
                () -> citaAgendamientoService.cancelar(1L, "motivo", ""));
    }
}
