package com.barberia.modules.modulo_horarios.services;

import com.barberia.modules.modulo_servicios.repositories.ServicioRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EspecialidadesPeluqueroServiceTest {

    @Mock
    private EntityManager entityManager;
    @Mock
    private ServicioRepository servicioRepository;
    @Mock
    private Query query;

    @InjectMocks
    private EspecialidadesPeluqueroService especialidadesPeluqueroService;

    @Test
    void asociarServicio_exitoso() {
        when(servicioRepository.existsById(1L)).thenReturn(true);
        when(entityManager.createNativeQuery(anyString())).thenReturn(query);
        when(query.setParameter(anyString(), any())).thenReturn(query);
        when(query.executeUpdate()).thenReturn(1);

        assertDoesNotThrow(() -> especialidadesPeluqueroService.asociarServicio("123", 1L));
    }

    @Test
    void asociarServicio_cuandoDocumentoNulo_lanzaExcepcion() {
        assertThrows(IllegalArgumentException.class,
                () -> especialidadesPeluqueroService.asociarServicio(null, 1L));
    }

    @Test
    void asociarServicio_cuandoDocumentoVacio_lanzaExcepcion() {
        assertThrows(IllegalArgumentException.class,
                () -> especialidadesPeluqueroService.asociarServicio("", 1L));
    }

    @Test
    void asociarServicio_cuandoServicioNoExiste_lanzaExcepcion() {
        when(servicioRepository.existsById(99L)).thenReturn(false);

        assertThrows(IllegalArgumentException.class,
                () -> especialidadesPeluqueroService.asociarServicio("123", 99L));
    }
}
