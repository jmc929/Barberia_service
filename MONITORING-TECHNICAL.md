# 🔧 OBSERVABILIDAD - Guía Técnica para Desarrolladores

Documentación técnica profunda sobre la implementación de Prometheus + Grafana.

---

## Tabla de Contenidos

1. [Cambios Realizados en el Proyecto](#cambios-realizados)
2. [Detalles de Implementación](#detalles-de-implementación)
3. [Integración con Arquitectura Modular](#integración-con-arquitectura)
4. [Cómo Funcionan las Métricas](#cómo-funcionan-las-métricas)
5. [Extensión de Métricas](#extensión-de-métricas)

---

## 📝 Cambios Realizados en el Proyecto

### Dependencias Agregadas (pom.xml)

**Antes:**
```xml
<!-- spring-boot-starter-web -->
<!-- spring-boot-starter-data-jpa -->
<!-- ... -->
<!-- spring-boot-starter-test -->
```

**Después:**
```xml
<!-- spring-boot-starter-web -->
<!-- spring-boot-starter-data-jpa -->
<!-- ... -->

<!-- Spring Boot Actuator (Observabilidad y Monitoreo) -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>

<!-- Micrometer Prometheus Registry (Métricas para Prometheus) -->
<dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-registry-prometheus</artifactId>
</dependency>
```

**Por qué estas versiones:**
- `spring-boot-starter-actuator`: Viene con Spring Boot 3.1.5, auto-configurable
- `micrometer-registry-prometheus`: Version compatible con Spring Boot 3.1.5, auto-included

### Configuración (application.properties)

**Nuevas propiedades:**

```properties
# Actuator - Monitoreo y Observabilidad
management.endpoints.web.exposure.include=health,info,prometheus,metrics
management.endpoints.web.base-path=/actuator
management.endpoint.health.show-details=always
management.endpoint.health.probes.enabled=true
management.health.livenessState.enabled=true
management.health.readinessState.enabled=true

# Prometheus - Métricas Micrometer
management.metrics.export.prometheus.enabled=true
management.metrics.tags.application=${spring.application.name}
management.metrics.tags.environment=${spring.profiles.active:default}
```

**Qué hace cada una:**

| Propiedad | Valor | Propósito |
|-----------|-------|----------|
| `management.endpoints.web.exposure.include` | `health,info,prometheus,metrics` | Expone solo estos endpoints (por seguridad) |
| `management.endpoints.web.base-path` | `/actuator` | Ruta base (no `/api/actuator`, separado del API) |
| `management.endpoint.health.show-details` | `always` | Muestra detalles de health (BD, etc.) |
| `management.endpoint.health.probes.enabled` | `true` | Habilita Liveness y Readiness probes |
| `management.metrics.export.prometheus.enabled` | `true` | Habilita formato Prometheus |
| `management.metrics.tags.application` | `barberia-service` | Tag agregado a todas las métricas |
| `management.metrics.tags.environment` | `development` | Tag de entorno (desde perfil activo) |

### Estructura de Directorios Nueva

```
backend/src/main/java/com/barberia/shared/
│
├── config/                    (existente)
├── exceptions/                (existente)
├── security/                  (existente)
├── utils/                     (existente)
│
└── monitoring/                (NUEVO)
    ├── MonitoringConfig.java
    └── MetricasPersonalizadasService.java
```

### Docker Compose Actualizado

**Cambios:**

1. **barberia-api:** Se agregó `healthcheck` y `depends_on: prometheus`
2. **prometheus:** Contenedor NUEVO (puerto 9090)
3. **grafana:** Contenedor NUEVO (puerto 3000)
4. **volumes:** Agregados `prometheus-data` y `grafana-data` para persistencia
5. **network:** Compartida entre los 3 servicios

**¿Por qué esta estructura?**

- Separación de responsabilidades (la app NO incluye Prometheus/Grafana)
- Fácil desplegar en producción (podría ser en servidores diferentes)
- Escalable (podrías tener varias apps scrapeando a un Prometheus)

---

## 🏗️ Detalles de Implementación

### MonitoringConfig.java

```java
@Configuration
public class MonitoringConfig {

    @Bean
    public MeterRegistryCustomizer<MeterRegistry> metricsCommonTags() {
        return registry -> registry.config()
            .commonTags(
                "service", "barberia-api",
                "version", "1.0.0"
            );
    }
}
```

**Qué hace:**
- Personaliza el `MeterRegistry` global de Micrometer
- Agrega tags comunes a TODAS las métricas (servicio, versión)
- Se ejecuta en startup de Spring

**En Prometheus, verás:**
```
jvm_memory_used_bytes{service="barberia-api",version="1.0.0",application="barberia-service",...}
```

**Alternativa:** Si quisieras cambiar tags dinámicamente:

```java
@Bean
public MeterRegistryCustomizer<MeterRegistry> dynamicTags() {
    return registry -> {
        String version = getVersionDynamicamente();
        registry.config().commonTags("version", version);
    };
}
```

---

### MetricasPersonalizadasService.java

**Patrón usado:** Service inyectable con métodos específicos

```java
@Service
public class MetricasPersonalizadasService {
    private final MeterRegistry meterRegistry;
    private final Counter citasAgendadasCounter;
    
    public MetricasPersonalizadasService(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        this.citasAgendadasCounter = Counter.builder("barberia.citas.agendadas")
            .description("Total de citas agendadas")
            .register(meterRegistry);
    }
    
    public void registrarCitaAgendada() {
        citasAgendadasCounter.increment();
    }
}
```

**¿Por qué esta estructura?**

1. **Encapsulación:** Los métodos ocultan la complejidad de Micrometer
2. **Reutilización:** Se inyecta en cualquier servicio
3. **Testing:** Fácil de mockear en tests
4. **Centralización:** Todas las métricas custom en un solo lugar
5. **Sem prácticas:** Nombres de métricas consistentes

**Alternativa (directa, no recomendada):**

```java
@Service
public class CitaService {
    
    @Autowired
    private MeterRegistry meterRegistry;  // ❌ Acoplamiento directo
    
    public void agendar() {
        Counter.builder("barberia.citas.agendadas")
            .register(meterRegistry)
            .increment();
    }
}
```

**Problemas de la alternativa:**
- Repetido en múltiples servicios
- Difícil cambiar un nombre de métrica
- Acoplamiento a Micrometer

---

## 🏛️ Integración con Arquitectura Modular

### ¿Cómo encaja el monitoring en la arquitectura?

```
Arquitectura Modular ORIGINAL:

modules/
  ├── modulo_usuarios/
  │   ├── controllers/
  │   ├── services/
  │   ├── repositories/
  │   └── models/
  │
  ├── modulo_citas/
  │   ├── controllers/
  │   ├── services/
  │   ├── repositories/
  │   └── models/
  │
  └── modulo_servicios/
      └── ...

shared/
  ├── config/
  ├── exceptions/
  ├── utils/
  └── security/


DESPUÉS de agregar Observabilidad:

modules/
  ├── modulo_usuarios/
  │   ├── controllers/
  │   ├── services/        ← INYECTA MetricasPersonalizadasService
  │   ├── repositories/
  │   └── models/
  │
  ├── modulo_citas/
  │   ├── controllers/
  │   ├── services/        ← INYECTA MetricasPersonalizadasService
  │   ├── repositories/
  │   └── models/
  │
  └── modulo_servicios/
      └── ...

shared/
  ├── config/
  ├── exceptions/
  ├── utils/
  ├── security/
  │
  └── monitoring/          ← NUEVO, centraliza observabilidad
      ├── MonitoringConfig.java
      └── MetricasPersonalizadasService.java
```

### ¿Por qué monitoring va en shared/?

1. **Transversal:** Todos los módulos lo necesitan
2. **No es específico:** No pertenece a usuario, cita, etc.
3. **Reutilizable:** Como config, exceptions, utils
4. **Mantenimiento:** Un solo lugar para cambiar

### Ejemplo: Cómo integrar en CitaAgendamientoService

**Antes:**
```java
@Service
public class CitaAgendamientoService {
    
    @Autowired
    private CitaRepository citaRepository;
    
    public CitaDTO agendar(CitaCreateDTO request, String numeroDocumento) {
        // ... validaciones ...
        Cita cita = new Cita();
        cita = citaRepository.save(cita);
        return convertToDTO(cita);
    }
}
```

**Después:**
```java
@Service
public class CitaAgendamientoService {
    
    @Autowired
    private CitaRepository citaRepository;
    
    @Autowired  // ← Nuevo: Inyectar servicio de métricas
    private MetricasPersonalizadasService metricas;
    
    public CitaDTO agendar(CitaCreateDTO request, String numeroDocumento) {
        // ... validaciones ...
        Cita cita = new Cita();
        cita = citaRepository.save(cita);
        
        // Registrar métrica de negocio
        metricas.registrarCitaAgendada();  // ← Nueva línea
        
        return convertToDTO(cita);
    }
}
```

**Impacto:**
- ✅ 1 línea agregada
- ✅ Sin cambio en lógica de negocio
- ✅ Sin cambio en contrato de métodos
- ✅ Modularidad intacta

---

## 📊 Cómo Funcionan las Métricas

### Flujo Completo

```
1. APLICACIÓN
   CitaService.agendar() es llamado
       ↓
   metricas.registrarCitaAgendada()
       ↓
   citasAgendadasCounter.increment()  // Counter internal de Micrometer
       ↓
   MeterRegistry (Prometheus Registry) almacena el valor

2. SPRING BOOT ACTUATOR
   GET /api/actuator/prometheus
       ↓
   Prometheus Registry exporta todas las métricas en formato TEXT
   (barberia.citas.agendadas = 42)
       ↓
   HTTP 200 con texto

3. PROMETHEUS
   Cada 15 segundos (según prometheus.yml):
       ↓
   GET http://barberia-api:8080/api/actuator/prometheus
       ↓
   Parseapromtxt
       ↓
   Almacena en TSDB (TimeSeries Database)
       ↓
   Mantiene último valor + histórico por 15 días

4. GRAFANA
   User abre dashboard
       ↓
   Dashboard consulta: 
   histogram_quantile(0.95, rate(http_server_requests_seconds_bucket[5m]))
       ↓
   Grafana hace request a Prometheus:
   GET http://prometheus:9090/api/v1/query?query=...
       ↓
   Prometheus retorna series de tiempo
       ↓
   Grafana dibuja gráfico
```

### Métricas Automáticas vs Personalizadas

**AUTOMÁTICAS** (Sin hacer nada):
- JVM Memory (Micrometer automatic)
- Threads (Micrometer automatic)
- HTTP Requests (Spring Web automatic con Micrometer)
- Database Connections (HikariCP auto)

```promql
jvm_memory_used_bytes       # Automática
jvm_threads_live_threads    # Automática
http_server_requests_seconds_bucket  # Automática
jdbc_connections_active     # Automática (HikariCP)
```

**PERSONALIZADAS** (Llamar método):
- Citas agendadas (agregamos código)
- Usuarios registrados (agregamos código)
- Servicios creados (agregamos código)

```promql
barberia.citas.agendadas        # Personalizada
barberia.usuarios.registrados   # Personalizada
barberia.servicios.creados      # Personalizada
```

### Tipos de Métricas Micrometer

#### 1. Counter

```java
Counter counter = Counter.builder("nombre")
    .description("Descripción")
    .register(meterRegistry);

counter.increment();        // +1
counter.increment(5);       // +5
```

**Características:**
- Solo sube
- Nunca resetea (acumulativo)
- Ideal para: eventos, solicitudes totales

**Query Prometheus:**
```promql
counter_name                          # Valor actual (ej: 1000)
rate(counter_name[1m])                # Por segundo
increase(counter_name[5m])            # Incremento en 5 min
```

#### 2. Gauge

```java
AtomicInteger gauge = new AtomicInteger(0);
Gauge.builder("nombre", gauge::get)
    .register(meterRegistry);

gauge.incrementAndGet();    // Puede subir o bajar
gauge.decrementAndGet();
```

**Características:**
- Puede subir y bajar
- Valor instantáneo
- Ideal para: memoria, threads, conexiones activas

**Query Prometheus:**
```promql
gauge_name                    # Valor actual
increase(gauge_name[5m])      # Cambio en 5 min (puede ser negativo)
```

#### 3. Histogram

```java
Timer timer = Timer.builder("nombre")
    .publishPercentiles(0.95, 0.99)
    .register(meterRegistry);

timer.record(() -> {
    // Operación que tarda
});
```

**Características:**
- Registra distribución de valores
- Crea buckets (automáticos)
- Ideal para: latencias, tamaños

**Query Prometheus:**
```promql
histogram_quantile(0.95, rate(nombre_bucket[5m]))  # p95
histogram_quantile(0.99, rate(nombre_bucket[5m]))  # p99
```

---

## 🚀 Extensión de Métricas

### Caso 1: Agregar métrica simple

**Requisito:** Contar cuántas reportes se generan

**Implementación:**

1. Agregar en `MetricasPersonalizadasService`:

```java
private final Counter reportesGenerados;

public MetricasPersonalizadasService(MeterRegistry meterRegistry) {
    // ... existente ...
    
    this.reportesGenerados = Counter.builder("barberia.reportes.generados")
        .description("Total de reportes generados")
        .register(meterRegistry);
}

public void registrarReporteGenerado() {
    reportesGenerados.increment();
}
```

2. Usar en el servicio de reportes:

```java
@Service
public class ReporteService {
    
    @Autowired
    private MetricasPersonalizadasService metricas;
    
    public Reporte generarReporte(ReporteRequest request) {
        // ... generar ...
        metricas.registrarReporteGenerado();
        return reporte;
    }
}
```

3. Query en Prometheus:

```promql
barberia.reportes.generados           # Total
rate(barberia.reportes.generados[1m]) # Por minuto
```

---

### Caso 2: Métrica etiquetada (tags dinámicos)

**Requisito:** Contar citas por tipo de servicio

**Implementación:**

```java
public void registrarCitaAgendada(String tipoServicio) {
    Counter.builder("barberia.citas.agendadas")
        .tag("servicio", tipoServicio)  // ← Tag dinámico
        .description("Citas agendadas por servicio")
        .register(meterRegistry)
        .increment();
}
```

**Uso:**

```java
public CitaDTO agendar(CitaCreateDTO request) {
    // ...
    String tipoServicio = request.getServicio();  // "corte", "afeitado", etc
    metricas.registrarCitaAgendada(tipoServicio);
}
```

**Queries:**

```promql
barberia.citas.agendadas{servicio="corte"}      # Citas de corte
barberia.citas.agendadas{servicio="afeitado"}   # Citas de afeitado
sum by (servicio) (barberia.citas.agendadas)    # Agrupar por servicio
```

---

### Caso 3: Métrica de latencia personalizada

**Requisito:** Medir tiempo de procesamiento de reportes

**Implementación:**

```java
public void medirLatenciaReporte(Callable<Reporte> operacion) throws Exception {
    Timer timer = Timer.builder("barberia.reporte.latencia")
        .description("Latencia de generación de reportes")
        .publishPercentiles(0.5, 0.95, 0.99)  // p50, p95, p99
        .register(meterRegistry);
    
    timer.recordCallable(operacion);
}
```

**Uso:**

```java
public Reporte generarReporte(ReporteRequest request) {
    return metricas.medirLatenciaReporte(() -> {
        // Lógica de generación
        return repo.generar(request);
    });
}
```

**Queries:**

```promql
histogram_quantile(0.50, rate(barberia_reporte_latencia_bucket[5m]))  # p50
histogram_quantile(0.95, rate(barberia_reporte_latencia_bucket[5m]))  # p95
histogram_quantile(0.99, rate(barberia_reporte_latencia_bucket[5m]))  # p99
```

---

### Caso 4: Usar MeterRegistry directamente

**Para casos especiales no contemplados:**

```java
@Autowired
private MeterRegistry meterRegistry;

public void eventoEspecial() {
    // Counter one-off
    Counter.builder("evento.especial")
        .register(meterRegistry)
        .increment();
    
    // Gauge dinámico
    Gauge.builder("conexiones.activas", 
        () -> obtenerConexionesActivas())
        .register(meterRegistry);
}
```

---

## 🔐 Seguridad del Endpoint Prometheus

### Situación Actual

El endpoint `/api/actuator/prometheus` está **público** (sin autenticación).

**¿Por qué?** Para que Prometheus pueda scrapear sin credenciales.

### Si necesitas protegerlo (Advanced)

1. **Excluir del Spring Security:**

```java
@Configuration
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/actuator/prometheus").permitAll()  // ← Permitir public
                .requestMatchers("/api/actuator/**").authenticated()      // ← Otros actúan protegidos
                .anyRequest().authenticated()
            )
            // ...
        return http.build();
    }
}
```

2. **O con Basic Auth (Prometheus con credenciales):**

Agregar en `prometheus.yml`:
```yaml
scrape_configs:
  - job_name: 'barberia-api'
    basic_auth:
      username: prometheus
      password: secret123
    static_configs:
      - targets: ['barberia-api:8080']
```

---

## 📋 Checklist: Verificación

Después de hacer deploy, verifica:

```bash
# 1. App está corriendo
curl http://localhost:8080/api/v1/servicios
# Respuesta: 200 OK

# 2. Actuator expone métricas
curl http://localhost:8080/api/actuator/prometheus | head -20
# Respuesta: Líneas con "# HELP", "# TYPE", valores

# 3. Prometheus scrapeó
curl http://localhost:9090/api/v1/query?query=up
# Response: barberia-api should be 1

# 4. Grafana ve Prometheus
curl http://localhost:3000/api/datasources
# Response: Datasource Prometheus con status "ok"

# 5. Genera tráfico para ver métricas
for i in {1..100}; do
  curl http://localhost:8080/api/v1/servicios
done

# 6. Query en Prometheus
curl 'http://localhost:9090/api/v1/query?query=rate(http_server_requests_seconds_count%5B1m%5D)'
# Response: Debe tener valores > 0
```

---

**Fin de documentación técnica**
