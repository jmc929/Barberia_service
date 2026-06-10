package com.barberia.modules.modulo_servicios.controllers;

import com.barberia.modules.modulo_servicios.models.dtos.ServicioDTO;
import com.barberia.modules.modulo_servicios.services.ServicioService;
import com.barberia.shared.exceptions.ResourceNotFoundException;
import com.barberia.shared.utils.ApiResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ServicioControllerTest {

    @Mock
    private ServicioService servicioService;

    @InjectMocks
    private ServicioController servicioController;

    private ServicioDTO servicioDTO;

    @BeforeEach
    void setUp() {
        servicioDTO = ServicioDTO.builder()
                .idServicio(1L)
                .nombreServicio("Corte")
                .descripcion("Corte moderno")
                .duracion(30)
                .costo(new BigDecimal("25000"))
                .idEstado(1L)
                .build();
    }

    @Test
    void obtenerTodos_exitoso() {
        when(servicioService.obtenerTodos()).thenReturn(List.of(servicioDTO));

        ResponseEntity<ApiResponse<List<ServicioDTO>>> result = servicioController.obtenerTodos();

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertTrue(result.getBody().isSuccess());
        assertEquals(1, result.getBody().getData().size());
    }

    @Test
    void obtenerPorId_exitoso() {
        when(servicioService.obtenerPorId(1L)).thenReturn(servicioDTO);

        ResponseEntity<ApiResponse<ServicioDTO>> result = servicioController.obtenerPorId(1L);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody().getData());
    }

    @Test
    void obtenerPorId_cuandoNoExiste_retorna404() {
        when(servicioService.obtenerPorId(99L)).thenThrow(new ResourceNotFoundException("No encontrado"));

        ResponseEntity<ApiResponse<ServicioDTO>> result = servicioController.obtenerPorId(99L);

        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
    }

    @Test
    void crear_exitoso() {
        when(servicioService.crear(any())).thenReturn(servicioDTO);

        ResponseEntity<ApiResponse<ServicioDTO>> result = servicioController.crear(servicioDTO);

        assertEquals(HttpStatus.CREATED, result.getStatusCode());
        assertTrue(result.getBody().isSuccess());
    }

    @Test
    void crear_cuandoError_retorna400() {
        when(servicioService.crear(any())).thenThrow(new IllegalArgumentException("Error"));

        ResponseEntity<ApiResponse<ServicioDTO>> result = servicioController.crear(servicioDTO);

        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
    }

    @Test
    void actualizar_exitoso() {
        when(servicioService.actualizar(anyLong(), any())).thenReturn(servicioDTO);

        ResponseEntity<ApiResponse<ServicioDTO>> result = servicioController.actualizar(1L, servicioDTO);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertTrue(result.getBody().isSuccess());
    }

    @Test
    void actualizar_cuandoError_retorna400() {
        when(servicioService.actualizar(anyLong(), any())).thenThrow(new IllegalArgumentException("Error"));

        ResponseEntity<ApiResponse<ServicioDTO>> result = servicioController.actualizar(1L, servicioDTO);

        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
    }

    @Test
    void eliminar_exitoso() {
        doNothing().when(servicioService).eliminar(1L);

        ResponseEntity<ApiResponse<Void>> result = servicioController.eliminar(1L);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertTrue(result.getBody().isSuccess());
    }

    @Test
    void eliminar_cuandoError_retorna400() {
        doThrow(new IllegalStateException("Error")).when(servicioService).eliminar(1L);

        ResponseEntity<ApiResponse<Void>> result = servicioController.eliminar(1L);

        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
    }

    @Test
    void deshabilitar_exitoso() {
        when(servicioService.deshabilitar(1L)).thenReturn(servicioDTO);

        ResponseEntity<ApiResponse<ServicioDTO>> result = servicioController.deshabilitar(1L);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertTrue(result.getBody().isSuccess());
    }

    @Test
    void deshabilitar_cuandoError_retorna400() {
        when(servicioService.deshabilitar(1L)).thenThrow(new RuntimeException("Error"));

        ResponseEntity<ApiResponse<ServicioDTO>> result = servicioController.deshabilitar(1L);

        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
    }
}
