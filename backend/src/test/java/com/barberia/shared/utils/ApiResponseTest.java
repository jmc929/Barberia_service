package com.barberia.shared.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ApiResponseTest {

    @Test
    void success_creaRespuestaConSuccessTrue() {
        ApiResponse<String> response = ApiResponse.success("Operación exitosa", "data");

        assertTrue(response.isSuccess());
        assertEquals("Operación exitosa", response.getMessage());
        assertEquals("data", response.getData());
    }

    @Test
    void error_creaRespuestaConSuccessFalse() {
        ApiResponse<String> response = ApiResponse.error("Error ocurrió");

        assertFalse(response.isSuccess());
        assertEquals("Error ocurrió", response.getMessage());
        assertNull(response.getData());
    }

    @Test
    void of_creaRespuestaPersonalizada() {
        ApiResponse<Integer> response = ApiResponse.of(true, "Mensaje", 42);

        assertTrue(response.isSuccess());
        assertEquals("Mensaje", response.getMessage());
        assertEquals(42, response.getData());
    }
}
