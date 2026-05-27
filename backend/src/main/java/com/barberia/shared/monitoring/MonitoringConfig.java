package com.barberia.shared.monitoring;

import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración centralizada de Micrometer y Prometheus.
 * 
 * Responsabilidades:
 * - Configurar el MeterRegistry global
 * - Agregar tags comunes a todas las métricas
 * - Habilitar métricas de JVM, HTTP, Base de Datos automáticamente
 * 
 * Nota: Spring Boot auto-configura Prometheus Registry si micrometer-registry-prometheus
 * está en el classpath y management.metrics.export.prometheus.enabled=true en properties.
 */
@Configuration
public class MonitoringConfig {

    /**
     * Personaliza el MeterRegistry global con tags comunes.
     * Estos tags aparecerán en todas las métricas scrapeadas por Prometheus.
     * 
     * Ejemplo métrica exportada:
     * jvm_memory_used_bytes{area="heap",application="barberia-service",environment="default"} 1024000
     */
    @Bean
    public MeterRegistryCustomizer<MeterRegistry> metricsCommonTags() {
        return registry -> registry.config()
            .commonTags(
                "service", "barberia-api",
                "version", "1.0.0"
            );
    }
}
