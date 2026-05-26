package com.barberia.shared.monitoring;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Service;

/**
 * Servicio centralizado para registro de métricas personalizadas de negocio.
 * 
 * Propósito:
 * - Proporcionar métodos simples para registrar eventos de negocio
 * - Evitar esparcir llamadas de Micrometer en todos los servicios
 * - Facilitar el monitoreo de KPIs específicos del dominio
 * 
 * Uso desde otros servicios:
 * @Autowired
 * private MetricasPersonalizadasService metricas;
 * 
 * metricas.registrarCitaAgendada();
 * metricas.registrarCitaCancelada();
 * metricas.registrarUsuarioRegistrado();
 */
@Service
public class MetricasPersonalizadasService {

    private final MeterRegistry meterRegistry;
    
    // Contadores de citas
    private final Counter citasAgendadasCounter;
    private final Counter citasCanceladasCounter;
    private final Counter citasConfirmadasCounter;
    
    // Contadores de usuarios
    private final Counter usuariosRegistradosCounter;
    private final Counter usuariosBloqueadosCounter;
    
    // Contadores de servicios
    private final Counter serviciosCreados;
    private final Counter serviciosDeshabilitados;

    public MetricasPersonalizadasService(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        
        // Inicializar contadores de citas
        this.citasAgendadasCounter = Counter.builder("barberia.citas.agendadas")
            .description("Total de citas agendadas")
            .register(meterRegistry);
            
        this.citasCanceladasCounter = Counter.builder("barberia.citas.canceladas")
            .description("Total de citas canceladas")
            .register(meterRegistry);
            
        this.citasConfirmadasCounter = Counter.builder("barberia.citas.confirmadas")
            .description("Total de citas confirmadas por barbero")
            .register(meterRegistry);
        
        // Inicializar contadores de usuarios
        this.usuariosRegistradosCounter = Counter.builder("barberia.usuarios.registrados")
            .description("Total de usuarios registrados en el sistema")
            .register(meterRegistry);
            
        this.usuariosBloqueadosCounter = Counter.builder("barberia.usuarios.bloqueados")
            .description("Total de usuarios bloqueados")
            .register(meterRegistry);
        
        // Inicializar contadores de servicios
        this.serviciosCreados = Counter.builder("barberia.servicios.creados")
            .description("Total de servicios de barbería creados")
            .register(meterRegistry);
            
        this.serviciosDeshabilitados = Counter.builder("barberia.servicios.deshabilitados")
            .description("Total de servicios deshabilitados")
            .register(meterRegistry);
    }

    // ============================================
    // MÉTRICAS DE CITAS
    // ============================================

    /**
     * Registra que una cita fue agendada exitosamente.
     * Se invoca desde CitaAgendamientoService.agendar()
     */
    public void registrarCitaAgendada() {
        citasAgendadasCounter.increment();
    }

    /**
     * Registra que una cita fue cancelada.
     * Se invoca desde CitaAgendamientoService.cancelar()
     */
    public void registrarCitaCancelada() {
        citasCanceladasCounter.increment();
    }

    /**
     * Registra que una cita fue confirmada por el barbero.
     * Se invoca desde CitaAgendamientoService.confirmar()
     */
    public void registrarCitaConfirmada() {
        citasConfirmadasCounter.increment();
    }

    // ============================================
    // MÉTRICAS DE USUARIOS
    // ============================================

    /**
     * Registra que un usuario se registró en el sistema.
     * Se invoca desde UsuarioService.registrarPersona()
     */
    public void registrarUsuarioRegistrado() {
        usuariosRegistradosCounter.increment();
    }

    /**
     * Registra que un usuario fue bloqueado.
     * Se invoca desde UsuarioService.bloquearUsuario()
     */
    public void registrarUsuarioBloqueado() {
        usuariosBloqueadosCounter.increment();
    }

    // ============================================
    // MÉTRICAS DE SERVICIOS
    // ============================================

    /**
     * Registra que un servicio de barbería fue creado.
     * Se invoca desde ServicioService.crear()
     */
    public void registrarServicioCreado() {
        serviciosCreados.increment();
    }

    /**
     * Registra que un servicio fue deshabilitado.
     * Se invoca desde ServicioService.deshabilitar()
     */
    public void registrarServicioDeshabilitado() {
        serviciosDeshabilitados.increment();
    }

    // ============================================
    // UTILIDADES
    // ============================================

    /**
     * Registra un contador genérico con nombre y valor.
     * Uso para casos especiales no contemplados.
     * 
     * Ejemplo:
     * metricas.registrarContadorGenerico("barberia.reportes.generados", 1);
     */
    public void registrarContadorGenerico(String nombre, double valor) {
        Counter.builder(nombre)
            .register(meterRegistry)
            .increment(valor);
    }

    /**
     * Registra una métrica de tiempo (latencia) de una operación.
     * Devuelve un Timer.Sample que se debe stopear al finalizar la operación.
     * 
     * Ejemplo de uso:
     * Timer.Sample sample = metricas.iniciarTiempoOperacion();
     * try {
     *     // hacer algo
     * } finally {
     *     sample.stop(Timer.builder("operacion.tiempo")
     *         .publishPercentiles(0.95, 0.99)
     *         .register(meterRegistry));
     * }
     */
    public Timer.Sample iniciarTiempoOperacion() {
        return Timer.start(meterRegistry);
    }
}
