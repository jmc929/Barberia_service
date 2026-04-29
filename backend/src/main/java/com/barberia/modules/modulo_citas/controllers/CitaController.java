package com.barberia.modules.modulo_citas.controllers;

import com.barberia.modules.modulo_citas.models.dtos.CrearCitaDTO;
import com.barberia.modules.modulo_citas.models.dtos.CitaResponseDTO;
import com.barberia.modules.modulo_citas.models.dtos.ReprogramarCitaDTO;
import com.barberia.modules.modulo_citas.models.dtos.CancelarCitaDTO;
import com.barberia.modules.modulo_citas.models.dtos.DisponibilidadDTO;
import com.barberia.modules.modulo_citas.enums.EstadoCita;
import com.barberia.modules.modulo_citas.services.CitaService;
import com.barberia.modules.modulo_citas.services.DisponibilidadService;
import com.barberia.modules.modulo_servicios.models.entities.Servicio;
import com.barberia.modules.modulo_servicios.repositories.ServicioRepository;
import com.barberia.modules.modulo_usuarios.models.entities.Usuario;
import com.barberia.modules.modulo_usuarios.repositories.UsuarioRepository;
import com.barberia.shared.utils.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Controller para gestionar citas
 * Endpoints públicos y protegidos para creación, cancelación y consulta de disponibilidad
 */
@RestController
@RequestMapping("/api/citas")
@RequiredArgsConstructor
public class CitaController {

    private final CitaService citaService;
    private final DisponibilidadService disponibilidadService;
    private final ServicioRepository servicioRepository;
    private final UsuarioRepository usuarioRepository;

    /**
     * POST: Crear una nueva cita
     * Autenticación requerida (cliente)
     * 
     * @param request DTO con datos de la cita (idBarbero, idServicio, fechaHora, notas)
     * @param authentication Datos del usuario autenticado
     * @return Cita creada con estado CONFIRMADA
     */
    @PostMapping("/crear")
    public ResponseEntity<ApiResponse<CitaResponseDTO>> crearCita(
            @Valid @RequestBody CrearCitaDTO request,
            Authentication authentication) {

        String emailCliente = authentication.getName();
        CitaResponseDTO cita = citaService.crearCita(request, emailCliente);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Cita creada exitosamente", cita));
    }

    /**
     * GET: Obtener disponibilidad de un barbero para una fecha y servicio
     * 
     * @param idBarbero ID del barbero
     * @param idServicio ID del servicio
     * @param fecha Fecha en formato YYYY-MM-DD
     * @return DTO con slots libres, ocupados, y horarios
     */
    @GetMapping("/disponibilidad")
    public ResponseEntity<ApiResponse<DisponibilidadDTO>> obtenerDisponibilidad(
            @RequestParam String idBarbero,
            @RequestParam Long idServicio,
            @RequestParam String fecha) {

        // Obtener servicio
        Servicio servicio = servicioRepository.findById(idServicio)
                .orElseThrow(() -> new com.barberia.shared.exceptions.ResourceNotFoundException(
                    "Servicio no encontrado"));

        // Obtener barbero (para el nombre)
        Usuario barbero = usuarioRepository.findByNumeroDocumento(idBarbero)
                .orElseThrow(() -> new com.barberia.shared.exceptions.ResourceNotFoundException(
                    "Barbero no encontrado"));

        // Parsear fecha
        LocalDate fechaLocal = LocalDate.parse(fecha, DateTimeFormatter.ISO_DATE);

        // Obtener disponibilidad
        DisponibilidadDTO disponibilidad = disponibilidadService.obtenerDisponibilidad(
            idBarbero,
            barbero.getNombrePersona(),
            fechaLocal,
            servicio
        );

        return ResponseEntity.ok(ApiResponse.success("Disponibilidad consultada exitosamente", disponibilidad));
    }

    /**
     * GET: Listar mis citas (del usuario autenticado)
     * 
     * @param estado Filtro opcional de estado (CONFIRMADA, CANCELADA, COMPLETADA)
     * @param page Número de página (default 0)
     * @param size Tamaño de página (default 20)
     * @param authentication Datos del usuario autenticado
     * @return Página de citas
     */
    @GetMapping("/mis-citas")
    public ResponseEntity<ApiResponse<Page<CitaResponseDTO>>> obtenerMisCitas(
            @RequestParam(required = false) String estado,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication) {

        String emailUsuario = authentication.getName();
        EstadoCita estadoFiltro = null;

        if (estado != null && !estado.isEmpty()) {
            try {
                estadoFiltro = EstadoCita.valueOf(estado.toUpperCase());
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Estado inválido: " + estado));
            }
        }

        Pageable pageable = PageRequest.of(page, size);
        Page<CitaResponseDTO> citas = citaService.obtenerMisCitas(emailUsuario, estadoFiltro, pageable);

        return ResponseEntity.ok(ApiResponse.success("Citas listadas exitosamente", citas));
    }

    /**
     * GET: Obtener una cita específica por ID
     * 
     * @param idCita ID de la cita
     * @return Detalles de la cita
     */
    @GetMapping("/{idCita}")
    public ResponseEntity<ApiResponse<CitaResponseDTO>> obtenerCitaPorId(
            @PathVariable Long idCita) {

        CitaResponseDTO cita = citaService.obtenerCitaPorId(idCita);

        return ResponseEntity.ok(ApiResponse.success("Cita obtenida exitosamente", cita));
    }

    /**
     * DELETE: Cancelar una cita
     * Autenticación requerida (cliente o barbero de la cita)
     * 
     * @param idCita ID de la cita a cancelar
     * @param request DTO con motivo de cancelación (opcional)
     * @param authentication Datos del usuario autenticado
     * @return Cita actualizada con estado CANCELADA
     */
    @DeleteMapping("/{idCita}")
    public ResponseEntity<ApiResponse<CitaResponseDTO>> cancelarCita(
            @PathVariable Long idCita,
            @RequestBody(required = false) CancelarCitaDTO request,
            Authentication authentication) {

        if (request == null) {
            request = new CancelarCitaDTO(null);
        }

        String emailUsuario = authentication.getName();
        CitaResponseDTO cita = citaService.cancelarCita(idCita, emailUsuario, request);

        return ResponseEntity.ok(ApiResponse.success("Cita cancelada exitosamente", cita));
    }

    /**
     * PUT: Reprogramar una cita (cambiar fecha/hora o barbero)
     * Autenticación requerida (cliente o barbero de la cita)
     * 
     * @param idCita ID de la cita a reprogramar
     * @param request DTO con nuevas fecha/hora y barbero
     * @param authentication Datos del usuario autenticado
     * @return Cita actualizada
     */
    @PutMapping("/{idCita}/reprogramar")
    public ResponseEntity<ApiResponse<CitaResponseDTO>> reprogramarCita(
            @PathVariable Long idCita,
            @Valid @RequestBody ReprogramarCitaDTO request,
            Authentication authentication) {

        String emailUsuario = authentication.getName();
        CitaResponseDTO cita = citaService.reprogramarCita(idCita, emailUsuario, request);

        return ResponseEntity.ok(ApiResponse.success("Cita reprogramada exitosamente", cita));
    }
}
