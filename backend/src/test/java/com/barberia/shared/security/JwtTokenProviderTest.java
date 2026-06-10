package com.barberia.shared.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider();
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtSecret", "mySecretKeyForJWTTokenGenerationAndValidationWithMinimumLengthAndMoreToBe512Bits");
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtExpiration", 86400000L);
    }

    @Test
    void generateToken_y_validateToken_exitoso() {
        String token = jwtTokenProvider.generateToken("test@test.com", "123", 3);

        assertNotNull(token);
        assertTrue(jwtTokenProvider.validateToken(token));
    }

    @Test
    void validateToken_cuandoTokenInvalido_retornaFalse() {
        assertFalse(jwtTokenProvider.validateToken("invalid-token"));
    }

    @Test
    void getEmailFromToken_retornaEmail() {
        String token = jwtTokenProvider.generateToken("test@test.com", "123", 3);

        String email = jwtTokenProvider.getEmailFromToken(token);

        assertEquals("test@test.com", email);
    }

    @Test
    void getNumeroDocumentoFromToken_retornaDocumento() {
        String token = jwtTokenProvider.generateToken("test@test.com", "123", 3);

        String numeroDocumento = jwtTokenProvider.getNumeroDocumentoFromToken(token);

        assertEquals("123", numeroDocumento);
    }

    @Test
    void getRolFromToken_retornaRol() {
        String token = jwtTokenProvider.generateToken("test@test.com", "123", 3);

        Integer rol = jwtTokenProvider.getRolFromToken(token);

        assertEquals(3, rol);
    }

    @Test
    void validateToken_cuandoTokenExpirado_retornaFalse() {
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtExpiration", -1000L);
        String token = jwtTokenProvider.generateToken("test@test.com", "123", 3);

        assertFalse(jwtTokenProvider.validateToken(token));
    }
}
