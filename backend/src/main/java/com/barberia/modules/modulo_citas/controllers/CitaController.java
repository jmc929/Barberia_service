package com.barberia.modules.modulo_citas.controllers;

import com.barberia.modules.modulo_citas.models.entities.Cita;
import com.barberia.modules.modulo_citas.repositories.CitaRepository;
import com.barberia.shared.utils.ApiResponse;
import com.barberia.modules.modulo_agendamiento.controllers.AgendamientoController;
import org.springframework.hateoas.EntityModel;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;

import java.util.Map;
import java.util.List;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/citas")
public class CitaController {

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
                .map(c -> {
                        EntityModel<Cita> model = EntityModel.of(c,
                            linkTo(methodOn(CitaController.class).obtenerPorId(noCita)).withRel("autorreferencia"),
                            linkTo(methodOn(AgendamientoController.class).cancelar(noCita, null, null)).withRel("cancelar cita").withType("POST"),
                            linkTo(methodOn(AgendamientoController.class).reprogramar(noCita, null, null)).withRel("reprogramar cita").withType("PUT"),
                            linkTo(methodOn(AgendamientoController.class).confirmar(noCita, null)).withRel("confirmar cita").withType("PUT")
                        );
                    return ResponseEntity.ok(ApiResponse.success("Cita obtenida", model));
                })
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error("Cita no encontrada")));
    }

        @GetMapping("/historial/{numeroDocumentoCliente}")
        @PreAuthorize("hasAuthority('ROLE_1')")
        public ResponseEntity<ApiResponse<CollectionModel<EntityModel<Cita>>>> historialClienteAdmin(
            @PathVariable String numeroDocumentoCliente) {

        List<Cita> citas = citaRepository.findByNumeroDocumentoCliente(numeroDocumentoCliente);

        var items = citas.stream().map(c -> {
            EntityModel<Cita> model = EntityModel.of(c,
                linkTo(methodOn(CitaController.class).obtenerPorId(c.getNoCita())).withRel("autorreferencia"),
                linkTo(methodOn(com.barberia.modules.modulo_agendamiento.controllers.AgendamientoController.class).cancelar(c.getNoCita(), null, null)).withRel("cancelar cita").withType("POST"),
                linkTo(methodOn(com.barberia.modules.modulo_agendamiento.controllers.AgendamientoController.class).reprogramar(c.getNoCita(), null, null)).withRel("reprogramar cita").withType("PUT")
            );
            return model;
        }).collect(Collectors.toList());

        CollectionModel<EntityModel<Cita>> collection = CollectionModel.of(items,
            linkTo(methodOn(CitaController.class).historialClienteAdmin(numeroDocumentoCliente)).withRel("autorreferencia"),
            linkTo(methodOn(com.barberia.modules.modulo_agendamiento.controllers.AgendamientoController.class).agendar(null, null)).withRel("crear cita").withType("POST")
        );

        return ResponseEntity.ok(
            ApiResponse.success("Historial de citas obtenido", collection)
        );
        }
        @GetMapping("/mi-historial")
        @PreAuthorize("isAuthenticated()")
        public ResponseEntity<ApiResponse<CollectionModel<EntityModel<Cita>>>> miHistorial(Authentication authentication) {
        String numeroDocumento = obtenerNumeroDocumento(authentication);

        List<Cita> citas = citaRepository.findByNumeroDocumentoCliente(numeroDocumento);

        var items = citas.stream().map(c -> {
            EntityModel<Cita> model = EntityModel.of(c,
                linkTo(methodOn(CitaController.class).obtenerPorId(c.getNoCita())).withRel("autorreferencia"),
                linkTo(methodOn(com.barberia.modules.modulo_agendamiento.controllers.AgendamientoController.class).cancelar(c.getNoCita(), null, authentication)).withRel("cancelar cita").withType("POST"),
                linkTo(methodOn(com.barberia.modules.modulo_agendamiento.controllers.AgendamientoController.class).reprogramar(c.getNoCita(), null, authentication)).withRel("reprogramar cita").withType("PUT")
            );
            return model;
        }).collect(Collectors.toList());

        CollectionModel<EntityModel<Cita>> collection = CollectionModel.of(items,
            linkTo(methodOn(CitaController.class).miHistorial(authentication)).withRel("autorreferencia"),
            linkTo(methodOn(com.barberia.modules.modulo_agendamiento.controllers.AgendamientoController.class).agendar(null, authentication)).withRel("crear cita").withType("POST")
        );

        return ResponseEntity.ok(
            ApiResponse.success("Historial de citas obtenido", collection)
        );
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
