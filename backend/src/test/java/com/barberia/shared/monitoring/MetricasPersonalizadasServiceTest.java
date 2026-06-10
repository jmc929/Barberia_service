package com.barberia.shared.monitoring;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MetricasPersonalizadasServiceTest {

    private MeterRegistry meterRegistry;
    private MetricasPersonalizadasService metricas;

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        metricas = new MetricasPersonalizadasService(meterRegistry);
    }

    @Test
    void registrarCitaAgendada_incrementaContador() {
        metricas.registrarCitaAgendada();
        double count = meterRegistry.counter("barberia.citas.agendadas").count();
        assertEquals(1.0, count);
    }

    @Test
    void registrarCitaCancelada_incrementaContador() {
        metricas.registrarCitaCancelada();
        double count = meterRegistry.counter("barberia.citas.canceladas").count();
        assertEquals(1.0, count);
    }

    @Test
    void registrarCitaConfirmada_incrementaContador() {
        metricas.registrarCitaConfirmada();
        double count = meterRegistry.counter("barberia.citas.confirmadas").count();
        assertEquals(1.0, count);
    }

    @Test
    void registrarUsuarioRegistrado_incrementaContador() {
        metricas.registrarUsuarioRegistrado();
        double count = meterRegistry.counter("barberia.usuarios.registrados").count();
        assertEquals(1.0, count);
    }

    @Test
    void registrarUsuarioBloqueado_incrementaContador() {
        metricas.registrarUsuarioBloqueado();
        double count = meterRegistry.counter("barberia.usuarios.bloqueados").count();
        assertEquals(1.0, count);
    }

    @Test
    void registrarServicioCreado_incrementaContador() {
        metricas.registrarServicioCreado();
        double count = meterRegistry.counter("barberia.servicios.creados").count();
        assertEquals(1.0, count);
    }

    @Test
    void registrarServicioDeshabilitado_incrementaContador() {
        metricas.registrarServicioDeshabilitado();
        double count = meterRegistry.counter("barberia.servicios.deshabilitados").count();
        assertEquals(1.0, count);
    }

    @Test
    void registrarContadorGenerico_incrementaContador() {
        metricas.registrarContadorGenerico("barberia.test.contador", 5.0);
        double count = meterRegistry.counter("barberia.test.contador").count();
        assertEquals(5.0, count);
    }

    @Test
    void iniciarTiempoOperacion_retornaSample() {
        Timer.Sample sample = metricas.iniciarTiempoOperacion();
        assertNotNull(sample);
    }
}
