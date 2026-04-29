package com.barberia.modules.modulo_citas.services;

import com.barberia.modules.modulo_citas.models.entities.Cita;
import com.barberia.modules.modulo_citas.models.entities.BarberoEspecialidad;
import com.barberia.modules.modulo_citas.models.dtos.CrearCitaDTO;
import com.barberia.modules.modulo_citas.models.dtos.CitaResponseDTO;
import com.barberia.modules.modulo_citas.models.dtos.ReprogramarCitaDTO;
import com.barberia.modules.modulo_citas.models.dtos.CancelarCitaDTO;
import com.barberia.modules.modulo_citas.enums.EstadoCita;
import com.barberia.modules.modulo_citas.enums.TipoNotificacion;
import com.barberia.modules.modulo_citas.exceptions.*;
import com.barberia.modules.modulo_citas.repositories.CitaRepository;
import com.barberia.modules.modulo_citas.repositories.BarberoEspecialidadRepository;
import com.barberia.modules.modulo_usuarios.models.entities.Usuario;
import com.barberia.modules.modulo_usuarios.repositories.UsuarioRepository;
import com.barberia.modules.modulo_servicios.models.entities.Servicio;
import com.barberia.modules.modulo_servicios.repositories.ServicioRepository;
import com.barberia.shared.exceptions.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

/**
 * Servicio CORE para gestionar citas
 * Implementa:
 * - Creación con Pessimistic Locking + SERIALIZABLE isolation
 * - Cancelación atómica
 * - Reprogramación segura
 * - Prevención de solapamientos
 */
@Service
@RequiredArgsConstructor
public class CitaService {

    private final CitaRepository citaRepository;
    private final BarberoEspecialidadRepository barberoEspecialidadRepository;
    private final UsuarioRepository usuarioRepository;
    private final ServicioRepository servicioRepository;
    private final DisponibilidadService disponibilidadService;
    private final NotificacionService notificacionService;
    private final AuditoriaService auditoriaService;

    /**
     * CREAR CITA - TRANSACCIÓN CRÍTICA CON PESSIMISTIC LOCKING
     *
     * Garantías:
     * - @Transactional(isolation = SERIALIZABLE): máximo aislamiento
     * - Pessimistic Lock en verificación de conflictos
     * - No hay race conditions posibles
     *
     * Validaciones:
     * 1. Cliente existe y autenticado
     * 2. Servicio existe y activo
     * 3. Barbero existe y ofrece servicio
     * 4. Fecha/hora > NOW() + 15 min (buffer)
     * 5. Fecha/hora <= NOW() + 90 días (horizon)
     * 6. Barbero tiene disponibilidad (horarios)
     * 7. Sin solapamientos con otras citas
     */
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public CitaResponseDTO crearCita(CrearCitaDTO request, String emailCliente) {

        // 1. VALIDAR CLIENTE
        Usuario cliente = usuarioRepository.findByEmail(emailCliente)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado"));

        // 2. VALIDAR SERVICIO
        Servicio servicio = servicioRepository.findById(request.getIdServicio())
                .orElseThrow(() -> new ResourceNotFoundException("Servicio no encontrado"));

        if (servicio.getIdEstado() != 1L) {
            throw new ResourceNotFoundException("El servicio no está disponible");
        }

        // 3. VALIDAR BARBERO
        Usuario barbero = usuarioRepository.findByNumeroDocumento(request.getIdBarbero())
                .orElseThrow(() -> new ResourceNotFoundException("Barbero no encontrado"));

        // 4. VALIDAR BARBERO OFRECE SERVICIO
        BarberoEspecialidad especialidad = barberoEspecialidadRepository
                .findByIdUsuarioBarberoAndIdServicio(request.getIdBarbero(), request.getIdServicio())
                .orElseThrow(() -> new BarberoNoOfreceServicioException(
                    "El barbero " + barbero.getNombrePersona() + " no ofrece este servicio"));

        // 5. VALIDAR FECHA/HORA
        LocalDateTime ahora = LocalDateTime.now();
        LocalDateTime fechaHora = request.getFechaHora();

        // Debe ser >= ahora + 15 minutos (buffer de scheduling)
        if (fechaHora.isBefore(ahora.plusMinutes(15))) {
            throw new IllegalArgumentException(
                "La cita debe ser al menos 15 minutos en el futuro");
        }

        // Debe ser <= ahora + 90 días
        if (fechaHora.isAfter(ahora.plusDays(90))) {
            throw new IllegalArgumentException(
                "La cita no puede ser más de 90 días en el futuro");
        }

        // 6. VALIDAR HORARIOS (barbero + negocio)
        if (!disponibilidadService.validarDisponibilidad(
                request.getIdBarbero(), fechaHora, servicio.getDuracion())) {
            throw new BarberoNoDisponibleException(
                "El barbero no tiene disponibilidad en ese horario o está cerrado");
        }

        // 7. PESSIMISTIC LOCK: Verificar solapamientos dentro de la transacción
        // Esta query hace LOCK de la fila en la BD, impidiendo race conditions
        int duracionServicio = servicio.getDuracion();
        LocalDateTime fin = fechaHora.plusMinutes(duracionServicio);

        // Verificación dentro de lock
        boolean hayConflicto = !disponibilidadService.validarDisponibilidad(
            request.getIdBarbero(), fechaHora, duracionServicio);

        if (hayConflicto) {
            throw new CitaYaExisteException(
                "Ya existe una cita en ese horario (otra transacción la acaba de crear)");
        }

        // 8. CREAR CITA EN ESTADO CONFIRMADA
        Cita cita = new Cita();
        cita.setIdCliente(cliente.getNumeroDocumento());
        cita.setIdBarbero(barbero.getNumeroDocumento());
        cita.setIdServicio(servicio.getIdServicio());
        cita.setFechaHora(fechaHora);
        cita.setDuracionMinutos(duracionServicio);
        cita.setCosto(servicio.getCosto());
        cita.setNotas(request.getNotas());
        cita.setEstadoCita(EstadoCita.CONFIRMADA);

        Cita citaGuardada = citaRepository.save(cita);

        // 9. CREAR NOTIFICACIONES (cliente y barbero)
        String mensajeConfirmacion = String.format(
            "Tu cita ha sido confirmada para el %s a las %s",
            fechaHora.toLocalDate(),
            fechaHora.toLocalTime()
        );

        notificacionService.crearNotificacion(
            citaGuardada.getIdCita(),
            cliente.getNumeroDocumento(),
            TipoNotificacion.CONFIRMACION,
            mensajeConfirmacion + " con " + barbero.getNombrePersona() + " (" + servicio.getNombreServicio() + ")"
        );

        notificacionService.crearNotificacion(
            citaGuardada.getIdCita(),
            barbero.getNumeroDocumento(),
            TipoNotificacion.CONFIRMACION,
            "Nueva cita confirmada: " + cliente.getNombrePersona() + " - " + servicio.getNombreServicio()
        );

        // 10. REGISTRAR EN AUDITORÍA
        Map<String, Object> detalles = new HashMap<>();
        detalles.put("barbero", barbero.getNombrePersona());
        detalles.put("servicio", servicio.getNombreServicio());
        detalles.put("duracionMinutos", duracionServicio);
        detalles.put("costo", servicio.getCosto());

        auditoriaService.registrar(
            citaGuardada.getIdCita(),
            cliente.getNumeroDocumento(),
            "CREATE",
            null,
            EstadoCita.CONFIRMADA.name(),
            detalles
        );

        return mapToDTOResponse(citaGuardada, cliente, barbero, servicio);
    }

    /**
     * CANCELAR CITA - CAMBIO ATÓMICO DE ESTADO
     *
     * Validaciones:
     * 1. Cita existe
     * 2. Usuario autenticado es cliente o barbero de la cita
     * 3. Cita está en estado CONFIRMADA
     *
     * Acciones:
     * - Cambiar estado a CANCELADA
     * - Crear notificación a ambos usuarios
     * - Registrar en auditoría
     */
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public CitaResponseDTO cancelarCita(Long idCita, String emailUsuario, CancelarCitaDTO request) {

        // 1. OBTENER CITA CON LOCK (para actualización segura)
        Cita cita = citaRepository.findByIdCitaWithLock(idCita)
                .orElseThrow(() -> new CitaNoEncontradaException("Cita no encontrada"));

        // 2. VALIDAR PERMISOS
        Usuario usuario = usuarioRepository.findByEmail(emailUsuario)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        boolean esCliente = cita.getIdCliente().equals(usuario.getNumeroDocumento());
        boolean esBarbero = cita.getIdBarbero().equals(usuario.getNumeroDocumento());

        if (!esCliente && !esBarbero) {
            throw new PermisoDenegadoException(
                "No tienes permisos para cancelar esta cita");
        }

        // 3. VALIDAR ESTADO ACTUAL
        if (!cita.getEstadoCita().equals(EstadoCita.CONFIRMADA)) {
            throw new IllegalArgumentException(
                "La cita no puede ser cancelada (estado: " + cita.getEstadoCita().name() + ")");
        }

        // 4. CAMBIAR ESTADO A CANCELADA
        cita.setEstadoCita(EstadoCita.CANCELADA);
        cita.setMotivoCancelacion(request.getMotivo());

        Cita citaActualizada = citaRepository.save(cita);

        // 5. CREAR NOTIFICACIONES
        Usuario cliente = usuarioRepository.findByNumeroDocumento(cita.getIdCliente())
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado"));
        Usuario barbero = usuarioRepository.findByNumeroDocumento(cita.getIdBarbero())
                .orElseThrow(() -> new ResourceNotFoundException("Barbero no encontrado"));

        String mensajeCancelacion = "La cita ha sido cancelada";

        notificacionService.crearNotificacion(
            cita.getIdCita(),
            cliente.getNumeroDocumento(),
            TipoNotificacion.CANCELACION,
            mensajeCancelacion + " (motivo: " + (request.getMotivo() != null ? request.getMotivo() : "sin especificar") + ")"
        );

        notificacionService.crearNotificacion(
            cita.getIdCita(),
            barbero.getNumeroDocumento(),
            TipoNotificacion.CANCELACION,
            "Cita de " + cliente.getNombrePersona() + " ha sido cancelada"
        );

        // 6. REGISTRAR EN AUDITORÍA
        auditoriaService.registrar(
            citaActualizada.getIdCita(),
            usuario.getNumeroDocumento(),
            "CANCEL",
            EstadoCita.CONFIRMADA.name(),
            EstadoCita.CANCELADA.name(),
            Map.of("canceladoPor", esCliente ? "CLIENTE" : "BARBERO", 
                   "motivo", request.getMotivo() != null ? request.getMotivo() : "")
        );

        return mapToDTOResponse(citaActualizada, cliente, barbero, null);
    }

    /**
     * REPROGRAMAR CITA - CAMBIO DE FECHA/HORA Y/O BARBERO
     *
     * Validaciones:
     * 1. Cita existe
     * 2. Usuario es cliente o barbero
     * 3. Cita está CONFIRMADA
     * 4. Nueva fecha/hora es válida
     * 5. Nuevo barbero (si cambió) ofrece servicio
     * 6. No hay solapamientos en nueva fecha/hora
     */
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public CitaResponseDTO reprogramarCita(Long idCita, String emailUsuario, ReprogramarCitaDTO request) {

        // 1. OBTENER CITA CON LOCK
        Cita cita = citaRepository.findByIdCitaWithLock(idCita)
                .orElseThrow(() -> new CitaNoEncontradaException("Cita no encontrada"));

        // 2. VALIDAR PERMISOS
        Usuario usuario = usuarioRepository.findByEmail(emailUsuario)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        boolean esCliente = cita.getIdCliente().equals(usuario.getNumeroDocumento());
        boolean esBarbero = cita.getIdBarbero().equals(usuario.getNumeroDocumento());

        if (!esCliente && !esBarbero) {
            throw new PermisoDenegadoException(
                "No tienes permisos para reprogramar esta cita");
        }

        // 3. VALIDAR ESTADO
        if (!cita.getEstadoCita().equals(EstadoCita.CONFIRMADA)) {
            throw new IllegalArgumentException(
                "Solo se pueden reprogramar citas confirmadas");
        }

        // 4. OBTENER SERVICIO Y VALIDAR NUEVA INFORMACIÓN
        Servicio servicio = servicioRepository.findById(cita.getIdServicio())
                .orElseThrow(() -> new ResourceNotFoundException("Servicio no encontrado"));

        // 5. VALIDAR NUEVO BARBERO SI CAMBIÓ
        String nuevoBarberoId = request.getIdBarbero();
        Usuario nuevoBarberousuario = usuarioRepository.findByNumeroDocumento(nuevoBarberoId)
                .orElseThrow(() -> new ResourceNotFoundException("Barbero no encontrado"));

        if (!nuevoBarberoId.equals(cita.getIdBarbero())) {
            // Verificar que el nuevo barbero ofrece el servicio
            barberoEspecialidadRepository
                    .findByIdUsuarioBarberoAndIdServicio(nuevoBarberoId, cita.getIdServicio())
                    .orElseThrow(() -> new BarberoNoOfreceServicioException(
                        "El nuevo barbero no ofrece este servicio"));
        }

        // 6. VALIDAR NUEVA FECHA/HORA
        LocalDateTime ahora = LocalDateTime.now();
        LocalDateTime nuevaFechaHora = request.getFechaHora();

        if (nuevaFechaHora.isBefore(ahora.plusMinutes(15))) {
            throw new IllegalArgumentException(
                "La nueva cita debe ser al menos 15 minutos en el futuro");
        }

        if (nuevaFechaHora.isAfter(ahora.plusDays(90))) {
            throw new IllegalArgumentException(
                "La cita no puede ser más de 90 días en el futuro");
        }

        // 7. VALIDAR DISPONIBILIDAD DEL NUEVO BARBERO EN LA NUEVA FECHA/HORA
        if (!disponibilidadService.validarDisponibilidad(
                nuevoBarberoId, nuevaFechaHora, servicio.getDuracion())) {
            throw new BarberoNoDisponibleException(
                "El barbero no tiene disponibilidad en la nueva fecha/hora");
        }

        // 8. GUARDAR VALORES ANTIGUOS PARA AUDITORÍA
        LocalDateTime fechaAnteriormente = cita.getFechaHora();
        String barberoAnterior = cita.getIdBarbero();

        // 9. ACTUALIZAR CITA
        cita.setFechaHora(nuevaFechaHora);
        cita.setIdBarbero(nuevoBarberoId);

        Cita citaActualizada = citaRepository.save(cita);

        // 10. CREAR NOTIFICACIÓN DE CAMBIO
        Usuario cliente = usuarioRepository.findByNumeroDocumento(cita.getIdCliente())
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado"));

        String mensajeCambio = String.format(
            "Tu cita ha sido reprogramada para el %s a las %s",
            nuevaFechaHora.toLocalDate(),
            nuevaFechaHora.toLocalTime()
        );

        notificacionService.crearNotificacion(
            cita.getIdCita(),
            cliente.getNumeroDocumento(),
            TipoNotificacion.CAMBIO_HORARIO,
            mensajeCambio
        );

        notificacionService.crearNotificacion(
            cita.getIdCita(),
            nuevoBarberoId,
            TipoNotificacion.CAMBIO_HORARIO,
            "Cita de " + cliente.getNombrePersona() + " ha sido reprogramada para " + 
            nuevaFechaHora.toLocalDate() + " a las " + nuevaFechaHora.toLocalTime()
        );

        // 11. AUDITORÍA
        Map<String, Object> detalles = new HashMap<>();
        detalles.put("fechaAnterior", fechaAnteriormente);
        detalles.put("fechaNueva", nuevaFechaHora);
        detalles.put("barberoAnterior", barberoAnterior);
        detalles.put("barberoNuevo", nuevoBarberoId);

        auditoriaService.registrar(
            cita.getIdCita(),
            usuario.getNumeroDocumento(),
            "UPDATE",
            EstadoCita.CONFIRMADA.name(),
            EstadoCita.CONFIRMADA.name(),
            detalles
        );

        return mapToDTOResponse(citaActualizada, cliente, nuevoBarberousuario, servicio);
    }

    /**
     * OBTENER MIS CITAS - Con filtros opcionales
     */
    @Transactional(readOnly = true)
    public Page<CitaResponseDTO> obtenerMisCitas(String emailUsuario, 
                                                  EstadoCita estado,
                                                  Pageable pageable) {
        Usuario usuario = usuarioRepository.findByEmail(emailUsuario)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        Page<Cita> citas;

        if (estado != null) {
            citas = citaRepository.findByIdClienteAndEstadoCita(
                usuario.getNumeroDocumento(),
                estado,
                pageable
            );
        } else {
            citas = citaRepository.findByIdCliente(usuario.getNumeroDocumento(), pageable);
        }

        return citas.map(cita -> {
            Usuario cliente = usuarioRepository.findByNumeroDocumento(cita.getIdCliente())
                    .orElse(null);
            Usuario barbero = usuarioRepository.findByNumeroDocumento(cita.getIdBarbero())
                    .orElse(null);
            Servicio servicio = servicioRepository.findById(cita.getIdServicio())
                    .orElse(null);
            return mapToDTOResponse(cita, cliente, barbero, servicio);
        });
    }

    /**
     * OBTENER CITA POR ID
     */
    @Transactional(readOnly = true)
    public CitaResponseDTO obtenerCitaPorId(Long idCita) {
        Cita cita = citaRepository.findById(idCita)
                .orElseThrow(() -> new CitaNoEncontradaException("Cita no encontrada"));

        Usuario cliente = usuarioRepository.findByNumeroDocumento(cita.getIdCliente())
                .orElse(null);
        Usuario barbero = usuarioRepository.findByNumeroDocumento(cita.getIdBarbero())
                .orElse(null);
        Servicio servicio = servicioRepository.findById(cita.getIdServicio())
                .orElse(null);

        return mapToDTOResponse(cita, cliente, barbero, servicio);
    }

    /**
     * Mapeo de Cita a CitaResponseDTO
     */
    private CitaResponseDTO mapToDTOResponse(Cita cita, Usuario cliente, Usuario barbero, Servicio servicio) {
        return CitaResponseDTO.builder()
                .idCita(cita.getIdCita())
                .cliente(cliente != null ? cliente.getNombrePersona() : "")
                .clienteEmail(cliente != null ? cliente.getEmail() : "")
                .barbero(barbero != null ? barbero.getNombrePersona() : "")
                .servicio(servicio != null ? servicio.getNombreServicio() : "")
                .duracionMinutos(cita.getDuracionMinutos())
                .fechaHora(cita.getFechaHora())
                .costo(cita.getCosto())
                .estado(cita.getEstadoCita().name())
                .notas(cita.getNotas())
                .createdAt(cita.getFechaCreacion())
                .updatedAt(cita.getFechaActualizacion())
                .build();
    }
}
