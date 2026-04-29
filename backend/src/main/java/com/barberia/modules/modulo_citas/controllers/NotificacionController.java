package com.barberia.modules.modulo_citas.controllers;

import com.barberia.modules.modulo_citas.models.dtos.NotificacionDTO;
import com.barberia.modules.modulo_citas.services.NotificacionService;
import com.barberia.shared.utils.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * Controller para gestionar notificaciones de citas
 */
@RestController
@RequestMapping("/api/notificaciones")
@RequiredArgsConstructor
public class NotificacionController {

    private final NotificacionService notificacionService;

    /**
     * GET: Obtener mis notificaciones
     * Autenticación requerida
     * 
     * @param leidas Filtro opcional (true/false/null)
     * @param page Número de página (default 0)
     * @param size Tamaño de página (default 20)
     * @param authentication Datos del usuario autenticado
     * @return Página de notificaciones
     */
    @GetMapping("/mis-notificaciones")
    public ResponseEntity<ApiResponse<Page<NotificacionDTO>>> obtenerMisNotificaciones(
            @RequestParam(required = false) Boolean leidas,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication) {

        String idUsuario = authentication.getName();
        Pageable pageable = PageRequest.of(page, size);

        Page<NotificacionDTO> notificaciones;

        if (leidas != null && !leidas) {
            notificaciones = notificacionService.obtenerNotificacionesNoLeidas(idUsuario, pageable);
        } else {
            notificaciones = notificacionService.obtenerMisNotificaciones(idUsuario, pageable);
        }

        return ResponseEntity.ok(ApiResponse.success("Notificaciones listadas exitosamente", notificaciones));
    }

    /**
     * PUT: Marcar una notificación como leída
     * Autenticación requerida
     * 
     * @param idNotificacion ID de la notificación
     * @return Notificación actualizada
     */
    @PutMapping("/{idNotificacion}/marcar-leida")
    public ResponseEntity<ApiResponse<NotificacionDTO>> marcarComoLeida(
            @PathVariable Long idNotificacion) {

        NotificacionDTO notificacion = notificacionService.marcarComoLeida(idNotificacion);

        return ResponseEntity.ok(ApiResponse.success("Notificación marcada como leída", notificacion));
    }

    /**
     * GET: Contar notificaciones no leídas del usuario
     * Autenticación requerida
     * 
     * @param authentication Datos del usuario autenticado
     * @return Conteo de notificaciones no leídas
     */
    @GetMapping("/no-leidas/count")
    public ResponseEntity<ApiResponse<Long>> contarNotificacionesNoLeidas(
            Authentication authentication) {

        String idUsuario = authentication.getName();
        long count = notificacionService.contarNotificacionesNoLeidas(idUsuario);

        return ResponseEntity.ok(ApiResponse.success("Notificaciones no leídas contadas", count));
    }
}
