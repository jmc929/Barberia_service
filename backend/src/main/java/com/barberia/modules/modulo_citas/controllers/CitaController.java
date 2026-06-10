package com.barberia.modules.modulo_citas.controllers;

import com.barberia.modules.modulo_citas.models.entities.Cita;
import com.barberia.modules.modulo_citas.repositories.CitaRepository;
import com.barberia.shared.utils.ApiResponse;
import com.barberia.modules.modulo_agendamiento.controllers.AgendamientoController;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;

import java.util.Map;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/citas")
public class CitaController {

    private static final String REL_AUTORREFERENCIA = "autorreferencia";
    private static final String REL_CANCELAR = "cancelar cita";
    private static final String REL_REPROGRAMAR = "reprogramar cita";

    private final CitaRepository citaRepository;

    public CitaController(CitaRepository citaRepository) {
        this.citaRepository = citaRepository;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_1')")
    public ResponseEntity<ApiResponse<List<Cita>>> obtenerTodas() {
        List<Cita> citas = citaRepository.findAll();
        return ResponseEntity.ok(ApiResponse.success("Citas obtenidas", citas));
    }

    @GetMapping("/buscar/{noCita}")
    @PreAuthorize("hasAuthority('ROLE_1')")
    public ResponseEntity<ApiResponse<EntityModel<Cita>>> obtenerPorId(@PathVariable Long noCita) {
        return citaRepository.findById(noCita)
                .map(c -> ResponseEntity.ok(ApiResponse.success("Cita obtenida",
                        EntityModel.of(c,
                            linkTo(methodOn(CitaController.class).obtenerPorId(noCita)).withRel(REL_AUTORREFERENCIA),
                            linkTo(methodOn(AgendamientoController.class).cancelar(noCita, null, null)).withRel(REL_CANCELAR).withType("POST"),
                            linkTo(methodOn(AgendamientoController.class).reprogramar(noCita, null, null)).withRel(REL_REPROGRAMAR).withType("PUT"),
                            linkTo(methodOn(AgendamientoController.class).confirmar(noCita, null)).withRel("confirmar cita").withType("PUT")
                        ))))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error("Cita no encontrada")));
    }

    @GetMapping("/historial/{numeroDocumentoCliente}")
    @PreAuthorize("hasAuthority('ROLE_1')")
    public ResponseEntity<ApiResponse<CollectionModel<EntityModel<Cita>>>> historialClienteAdmin(
            @PathVariable String numeroDocumentoCliente) {
        List<Cita> citas = citaRepository.findByNumeroDocumentoCliente(numeroDocumentoCliente);

        List<EntityModel<Cita>> items = citas.stream().map(c ->
            EntityModel.of(c,
                linkTo(methodOn(CitaController.class).obtenerPorId(c.getNoCita())).withRel(REL_AUTORREFERENCIA),
                linkTo(methodOn(AgendamientoController.class).cancelar(c.getNoCita(), null, null)).withRel(REL_CANCELAR).withType("POST"),
                linkTo(methodOn(AgendamientoController.class).reprogramar(c.getNoCita(), null, null)).withRel(REL_REPROGRAMAR).withType("PUT")
            )
        ).collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success("Historial de citas obtenido",
            CollectionModel.of(items,
                linkTo(methodOn(CitaController.class).historialClienteAdmin(numeroDocumentoCliente)).withRel(REL_AUTORREFERENCIA),
                linkTo(methodOn(AgendamientoController.class).agendar(null, null)).withRel("crear cita").withType("POST")
            )
        ));
    }

    @GetMapping("/mi-historial")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<CollectionModel<EntityModel<Cita>>>> miHistorial(Authentication authentication) {
        String numeroDocumento = obtenerNumeroDocumento(authentication);
        List<Cita> citas = citaRepository.findByNumeroDocumentoCliente(numeroDocumento);

        List<EntityModel<Cita>> items = citas.stream().map(c ->
            EntityModel.of(c,
                linkTo(methodOn(CitaController.class).obtenerPorId(c.getNoCita())).withRel(REL_AUTORREFERENCIA),
                linkTo(methodOn(AgendamientoController.class).cancelar(c.getNoCita(), null, authentication)).withRel(REL_CANCELAR).withType("POST"),
                linkTo(methodOn(AgendamientoController.class).reprogramar(c.getNoCita(), null, authentication)).withRel(REL_REPROGRAMAR).withType("PUT")
            )
        ).collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success("Historial de citas obtenido",
            CollectionModel.of(items,
                linkTo(methodOn(CitaController.class).miHistorial(authentication)).withRel(REL_AUTORREFERENCIA),
                linkTo(methodOn(AgendamientoController.class).agendar(null, authentication)).withRel("crear cita").withType("POST")
            )
        ));
    }

    private String obtenerNumeroDocumento(Authentication authentication) {
        Object details = authentication.getDetails();
        if (details instanceof Map<?, ?> detailsMap) {
            Object numeroDocumento = detailsMap.get("numeroDocumento");
            if (numeroDocumento != null) {
                return numeroDocumento.toString();
            }
        }
        return authentication.getName();
    }
}
