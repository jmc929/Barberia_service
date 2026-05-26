package com.barberia.modules.modulo_agenda.controllers;

import com.barberia.modules.modulo_agenda.models.dtos.AgendaResponseDTO;
import com.barberia.modules.modulo_agenda.services.AgendaService;
import com.barberia.shared.utils.ApiResponse;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;
import java.util.stream.Collectors;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.List;

@RestController
@RequestMapping("/api/v1/agenda")
public class AgendaController {
    private final AgendaService agendaService;

    public AgendaController(AgendaService agendaService) {
        this.agendaService = agendaService;
    }

    @GetMapping("/mis-citas")
    @PreAuthorize("hasAuthority('ROLE_2')")
    public ResponseEntity<ApiResponse<CollectionModel<EntityModel<AgendaResponseDTO>>>> miAgenda(Authentication authentication) {
        String numeroDocumento = obtenerNumeroDocumento(authentication);
        List<AgendaResponseDTO> agenda = agendaService.obtenerAgendaPeluquero(numeroDocumento);

        var items = agenda.stream().map(item -> {
            EntityModel<AgendaResponseDTO> model = EntityModel.of(item,
                    linkTo(methodOn(AgendaController.class).miAgenda(authentication)).withRel("autorreferencia"),
                    linkTo(methodOn(com.barberia.modules.modulo_citas.controllers.CitaController.class).obtenerPorId(item.getNoCita())).withRel("ver cita")
            );
            return model;
        }).collect(Collectors.toList());

        CollectionModel<EntityModel<AgendaResponseDTO>> collection = CollectionModel.of(items,
                linkTo(methodOn(AgendaController.class).miAgenda(authentication)).withRel("autorreferencia"),
                linkTo(methodOn(com.barberia.modules.modulo_agendamiento.controllers.AgendamientoController.class).agendar(null, authentication)).withRel("crear cita").withType("POST")
        );

        return ResponseEntity.ok(ApiResponse.success("Agenda obtenida", collection));
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
