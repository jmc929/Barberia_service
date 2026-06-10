package com.barberia.shared.exceptions;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    @Test
    void handleResourceNotFound_retorna404() {
        ResourceNotFoundException ex = new ResourceNotFoundException("Recurso no encontrado: 123");

        ResponseEntity<Map<String, Object>> response = handler.handleResourceNotFound(ex);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(404, response.getBody().get("status"));
        assertEquals("Recurso no encontrado", response.getBody().get("error"));
        assertEquals("Recurso no encontrado: 123", response.getBody().get("message"));
        assertNotNull(response.getBody().get("timestamp"));
    }

    @Test
    void handleGeneralException_retorna500() {
        Exception ex = new Exception("Error interno");

        ResponseEntity<Map<String, Object>> response = handler.handleGeneralException(ex);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(500, response.getBody().get("status"));
        assertEquals("Error interno del servidor", response.getBody().get("error"));
        assertEquals("Error interno", response.getBody().get("message"));
        assertNotNull(response.getBody().get("timestamp"));
    }
}
