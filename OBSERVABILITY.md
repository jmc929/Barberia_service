# 📊 OBSERVABILIDAD - Prometheus y Grafana

Guía completa para implementar y usar Prometheus + Grafana en Barberia Service.

---

## 📋 Tabla de Contenidos

1. [Introducción](#introducción)
2. [Arquitectura de Observabilidad](#arquitectura-de-observabilidad)
3. [Inicio Rápido](#inicio-rápido)
4. [Acceso a Herramientas](#acceso-a-herramientas)
5. [Dashboards Disponibles](#dashboards-disponibles)
6. [Entender las Métricas](#entender-las-métricas)
7. [Agregar Métricas Personalizadas](#agregar-métricas-personalizadas)
8. [Troubleshooting](#troubleshooting)

---

## 🎯 Introducción

Este proyecto ahora cuenta con un **stack completo de observabilidad** que te permite:

✅ **Monitorear en tiempo real** el comportamiento de la aplicación  
✅ **Recopilar métricas** de JVM, HTTP, y base de datos automáticamente  
✅ **Visualizar datos** con dashboards profesionales en Grafana  
✅ **Detectar problemas** antes de que afecten usuarios  
✅ **Registrar KPIs de negocio** (citas, usuarios, servicios)  

### ¿Cómo funciona?

```
┌─────────────────────────────────────────────────────────────┐
│                    Barberia Service (Java)                  │
│                      Spring Boot 3.1.5                      │
│  ┌────────────────────────────────────────────────────────┐│
│  │              Spring Boot Actuator                      ││
│  │  ┌──────────────────────────────────────────────────┐ ││
│  │  │ /actuator/prometheus → Expone métricas en texto ││ ││
│  │  │ (formato Prometheus)                            ││ ││
│  │  └──────────────────────────────────────────────────┘ ││
│  └────────────────────────────────────────────────────────┘
│                           ↓
│              Métricas en tiempo real
│           (JVM, HTTP, Base de datos)
└─────────────────────────────────────────────────────────────┘
                           ↓
          Docker Network: barberia-network
                           ↓
        ┌──────────────────┬──────────────────┐
        ↓                  ↓                  ↓
    Prometheus         Grafana          Prometheus
    (Puerto 9090)    (Puerto 3000)       (Meta-monitoring)
    
    - Scrapeador    - Visualización    - Almacena  
      de métricas   - Dashboards       - Consulta
    - TSDB          - Alertas            datos
    - Almacena      - Queries            históricos
      datos por
      15 días
```

---

## 🏗️ Arquitectura de Observabilidad

### Componentes Principales

#### 1. **Spring Boot Actuator** (dentro de la aplicación)
- **Propósito:** Expone endpoints de monitoreo
- **Endpoint principal:** `GET /api/actuator/prometheus`
- **Retorna:** Métricas en formato Prometheus (texto plano)
- **Configuración:** `application.properties`

#### 2. **Micrometer** (biblioteca dentro de la aplicación)
- **Propósito:** Abstracción para registrar métricas
- **Registries soportados:** Prometheus, CloudWatch, New Relic, etc.
- **Uso:** Recolecta automáticamente métri cas de JVM, HTTP, BD

#### 3. **Prometheus** (contenedor separado)
- **Propósito:** Servidor de series de tiempo
- **Función:** Scrapeador que periodicamente consume `/actuator/prometheus`
- **Puerto:** 9090 (http://localhost:9090)
- **Almacenamiento:** 15 días de historial
- **Archivo config:** `prometheus.yml`

#### 4. **Grafana** (contenedor separado)
- **Propósito:** Visualización y dashboards
- **Conexión:** Se conecta a Prometheus como datasource
- **Puerto:** 3000 (http://localhost:3000)
- **Usuario:** admin / admin (¡cambiar en producción!)
- **Carpeta provisioning:** `grafana/provisioning/`

### Flujo de Datos

```
Aplicación Java
       ↓
  Micrometer recoge métricas
       ↓
Actuator expone /actuator/prometheus
       ↓
Prometheus (cada 15 segundos) SCRAPEА
       ↓
Prometheus almacena en TSDB
       ↓
Grafana CONSULTA Prometheus
       ↓
Dashboards muestran gráficos
```

---

## 🚀 Inicio Rápido

### Requisitos Previos

- Docker Desktop instalado y corriendo
- Archivo `.env` configurado (igual que antes)
- Credenciales de Supabase en el `.env`

### Paso 1: Compilar la aplicación

```bash
cd backend
mvn clean package -DskipTests
```

La compilación ahora incluye las nuevas dependencias de Actuator y Micrometer.

### Paso 2: Iniciar los contenedores

```bash
# Desde la raíz del proyecto (Barberia_service)
docker-compose up --build
```

**Salida esperada:**

```
barberia-backend    | ... Started BarberiServiceApplication in 6.834 seconds
barberia-prometheus | msg="Server is ready to receive web requests." addr=:9090
barberia-grafana    | logger=context userId=0 orgId=0 msg="Initializing provisioned datasource" name=Prometheus
```

### Paso 3: Esperar a que todo esté listo

```bash
# Verifica que los 3 contenedores estén UP
docker-compose ps

# Resultado esperado:
# NAME                  STATUS
# barberia-backend      Up (healthy)
# barberia-prometheus   Up (healthy)
# barberia-grafana      Up (healthy)
```

### Paso 4: Generar tráfico (para ver métricas)

```bash
# En otra terminal, haz algunas solicitudes a la API
curl http://localhost:8080/api/v1/servicios
curl http://localhost:8080/api/swagger-ui.html
```

---

## 🌐 Acceso a Herramientas

### Prometheus

**URL:** http://localhost:9090

**Panel principal:**
- Verifica que el job `barberia-api` está en estado `UP`
- Ve a: Status → Targets
- Deberías ver: `barberia-api:8080` con estado verde (puede tomar hasta 45 segundos en la primera carga)

**Haz una query:**
1. Haz clic en "Graph"
2. Escribe: `jvm_memory_used_bytes`
3. Presiona Enter
4. Verás el gráfico de memoria en tiempo real

**Nota:** Si inicialmente ves "UNknown" o "Down" en los targets, espera 1-2 minutos. La primera scraping toma tiempo.

### Grafana

**URL:** http://localhost:3000

**Credenciales por defecto:**
- Usuario: `admin`
- Contraseña: `admin`

**Cambiar contraseña (recomendado):**
1. Haz clic en el avatar (arriba a la derecha)
2. Change Password
3. Ingresa nueva contraseña

**Dashboards provistos automáticamente:**
- Los 4 dashboards están ya creados y listos en la carpeta "Barberia Monitoring"
- No necesitas crear nada, solo explorar

### Endpoint de Métricas en la App

**URL:** http://localhost:8080/api/actuator/prometheus

**Muestra:**
```
# HELP jvm_memory_used_bytes JVM memory in bytes
# TYPE jvm_memory_used_bytes gauge
jvm_memory_used_bytes{application="barberia-service",area="heap",id="PS Survivor Space"} 1024000.0
jvm_memory_used_bytes{application="barberia-service",area="heap",id="PS Old Generation"} 2048000.0
...
```

---

## 📊 Dashboards Disponibles

### 1. **System Overview** ⭐ (Recomendado para empezar)
**UID:** `barberia-system-overview`

**Qué ves:**
- ✅ Estado de la API (UP/DOWN)
- ✅ Requests por segundo (throughput)
- ✅ Tasa de errores %
- ✅ Latencia p95
- ✅ Uso de memoria heap
- ✅ Distribución de status HTTP
- ✅ Gráfico de memoria a lo largo del tiempo
- ✅ Actividad de threads

**Uso:** Visión rápida de la salud general de la aplicación

---

### 2. **JVM Metrics**
**UID:** `barberia-jvm-metrics`

**Paneles:**
- 📈 JVM Memory Usage (Heap + Non-Heap)
- 🥧 Available Memory (pie chart)
- 📊 Heap Memory Usage % (gauge)
- 🧵 Thread Count (live vs peak)
- 🗑️ GC Memory Promoted (garbage collection)
- ⏱️ GC Pause Time (cuánto tiempo tarda la recolección de basura)

**Métricas clave:**
- `jvm_memory_used_bytes` - Memoria usada
- `jvm_memory_max_bytes` - Memoria máxima
- `jvm_threads_live_threads` - Threads activos
- `jvm_gc_pause_seconds_sum` - Tiempo de pausa GC

**Uso:** Detectar memory leaks, problemas de GC, mal dimensionamiento

---

### 3. **HTTP Requests**
**UID:** `barberia-http-requests`

**Paneles:**
- 📡 HTTP Requests Rate (requests por segundo)
- ⏱️ HTTP Latency - p95 & p99 (percentiles)
- 🟢 HTTP Status Distribution (200, 400, 500...)
- 🔴 HTTP 5xx Errors gauge
- 🏆 Top 10 Endpoints by Request Count
- 🐢 Top 10 Slowest Endpoints

**Métricas clave:**
- `http_server_requests_seconds_count` - Cantidad de requests
- `http_server_requests_seconds_bucket` - Histograma de latencia
- `status` - Código HTTP

**Uso:** Identificar endpoints lentos, tasas de error, anomalías de tráfico

---

### 4. **Database Metrics**
**UID:** `barberia-database-metrics`

**Paneles:**
- 🔗 JDBC Connection Pool Status (activas, idle, pending)
- 📊 Connection Pool Usage % (gauge)
- ⏱️ Connection Acquire Latency (p95/p99)
- 📈 Connection Creation Rate
- 🗂️ Database Query Latency (p95/p99)
- 📡 Database Query Rate

**Métricas clave:**
- `jdbc_connections_active` - Conexiones en uso
- `jdbc_acquire_ms_bucket` - Histograma de tiempo de adquisición
- `jpa_repository_query_execution_ms_bucket` - Latencia de queries

**Uso:** Diagnosticar cuellos de botella en BD, connection pool issues

---

## 🔍 Entender las Métricas

### Tipos de Métricas en Prometheus

#### 1. **Counter** (Contador)
- Siempre sube (nunca baja)
- Ejemplo: `http_server_requests_seconds_count`
- Uso: Total acumulado de eventos

**Query útil:**
```promql
rate(http_server_requests_seconds_count[1m])  # Requests por segundo (1 min)
increase(http_server_requests_seconds_count[5m])  # Total aumentado en 5 min
```

#### 2. **Gauge** (Medida)
- Puede subir y bajar
- Ejemplo: `jvm_memory_used_bytes`
- Uso: Valor instantáneo

**Query útil:**
```promql
jvm_memory_used_bytes  # Valor actual
increase(jvm_memory_used_bytes[5m])  # Cambio en 5 min
```

#### 3. **Histogram** (Distribución)
- Registra distribución de valores
- Ejemplo: `http_server_requests_seconds_bucket`
- Uso: Latencias, tamaños

**Query útil:**
```promql
histogram_quantile(0.95, rate(http_server_requests_seconds_bucket[5m]))  # p95
histogram_quantile(0.99, rate(http_server_requests_seconds_bucket[5m]))  # p99
```

### Tags Agregados a Todas las Métricas

Gracias a `MonitoringConfig.java`, todas las métricas tienen:

```
application="barberia-service"
service="barberia-api"
version="1.0.0"
environment="development"  (desde properties)
```

### Ejemplos de Queries Útiles

**Memoria total usada:**
```promql
sum(jvm_memory_used_bytes{application="barberia-service"})
```

**Requests exitosos vs errores:**
```promql
sum(rate(http_server_requests_seconds_count{status=~"2.."}[1m]))
sum(rate(http_server_requests_seconds_count{status=~"5.."}[1m]))
```

**Latencia promedio (no ponderada):**
```promql
rate(http_server_requests_seconds_sum[5m]) / rate(http_server_requests_seconds_count[5m])
```

**Threads que crecen constantemente (posible leak):**
```promql
increase(jvm_threads_live_threads[1h]) > 0
```

---

## 🎯 Agregar Métricas Personalizadas

### Opción 1: Métricas de Negocio (RECOMENDADO)

La clase `MetricasPersonalizadasService` en `shared/monitoring/` ya tiene métodos preparados.

**Paso 1: Inyectar el servicio en tu Service**

```java
@Service
public class CitaAgendamientoService {
    
    @Autowired
    private MetricasPersonalizadasService metricas;
    
    public CitaDTO agendar(CitaCreateDTO request, String numeroDocumentoCliente) {
        // ... lógica ...
        
        // Registrar métrica
        metricas.registrarCitaAgendada();
        
        return citaDTO;
    }
}
```

**Paso 2: Implementado**

Eso es todo. Ya está scrapeando en Prometheus.

**Métricas de negocio disponibles:**

```java
metricas.registrarCitaAgendada();        // barberia.citas.agendadas
metricas.registrarCitaCancelada();       // barberia.citas.canceladas
metricas.registrarCitaConfirmada();      // barberia.citas.confirmadas
metricas.registrarUsuarioRegistrado();   // barberia.usuarios.registrados
metricas.registrarUsuarioBloqueado();    // barberia.usuarios.bloqueados
metricas.registrarServicioCreado();      // barberia.servicios.creados
metricas.registrarServicioDeshabilitado();// barberia.servicios.deshabilitados
```

### Opción 2: Métrica Personalizada Genérica

```java
// En cualquier servicio
metricas.registrarContadorGenerico("barberia.reportes.generados", 1);
```

### Opción 3: Métrica de Latencia

```java
@Service
public class MiServicio {
    
    @Autowired
    private MetricasPersonalizadasService metricas;
    
    public void operacionLenta() {
        Timer.Sample sample = metricas.iniciarTiempoOperacion();
        
        try {
            // Hacer algo que tarda
            Thread.sleep(100);
        } finally {
            sample.stop(Timer.builder("mi.operacion.tiempo")
                .publishPercentiles(0.95, 0.99)
                .register(meterRegistry));
        }
    }
}
```

---

## 📈 Agregando Dashboards en Grafana

Si quieres crear un dashboard personalizado:

1. **En Grafana:**
   - Home → Create Dashboard → Add Panel
   - Selecciona Prometheus como datasource
   - Escribe una query (ejemplo: `jvm_memory_used_bytes`)
   - Personaliza título, colores, thresholds
   - Save

2. **Exportar como JSON:**
   - Dashboard Settings → JSON Model
   - Copia el JSON
   - Pégalo en `grafana/provisioning/dashboards/definitions/mi-dashboard.json`
   - Reinicia: `docker-compose restart grafana`

3. **Será auto-cargado próxima vez**

---

## 🔧 Troubleshooting

### Problema: "No data" en Grafana

**Verificar:**

1. ¿La app está corriendo?
```bash
curl http://localhost:8080/api/actuator/prometheus
```

2. ¿Prometheus ve el endpoint?
   - Ve a http://localhost:9090/targets
   - Busca `barberia-api`
   - ¿Está en UP o DOWN?

3. ¿Hay tráfico HTTP?
   - Los contadores necesitan events
   - Haz: `curl http://localhost:8080/api/v1/servicios`

**Solución:**

```bash
# Reinicia todo
docker-compose down
docker-compose up --build
```

---

### Problema: Prometheus no scrapeа

**Error en targets:**
```
Get "http://barberia-api:8080/api/actuator/prometheus": 
dial tcp: lookup barberia-api on 127.0.0.1:53: no such host
```

**Causa:** La app no está en la misma network de Docker

**Solución:**
```bash
# Verifica que docker-compose.yml tiene "networks"
docker network ls  # Debe existir: barberia-network

# Si no, recrea:
docker-compose down --volumes
docker-compose up --build
```

---

### Problema: Grafana no puede conectar a Prometheus

**En Grafana:**
- Configuration → Data Sources → Prometheus
- Test Connection

**Causa:** URL incorrecta o servicio no running

**Verificar:**
```bash
curl http://prometheus:9090  # Desde dentro de Grafana container
docker-compose ps  # Prometheus debe estar UP
```

---

### Problema: Memoria llena de Prometheus

**Prometheus guarda 15 días de datos por defecto.**

Si necesitas cambiar retención:

En `prometheus.yml`:
```yaml
tsdb:
  retention: 7d  # Cambiar de 15d a 7d (o lo que necesites)
```

Reinicia:
```bash
docker-compose restart prometheus
```

---

### Problema: Dashboard no carga

**Verifica logs:**
```bash
docker-compose logs grafana | tail -50
```

**Si el error es "uid already exists":**
- Ve a Grafana
- Busca el dashboard viejo
- Delete
- Reinicia: `docker-compose restart grafana`

---

## 📚 Recursos Adicionales

### Documentación Oficial

- [Prometheus Queries](https://prometheus.io/docs/prometheus/latest/querying/basics/)
- [Micrometer](https://micrometer.io/)
- [Spring Boot Actuator](https://spring.io/guides/gs/actuator-service/)
- [Grafana Dashboards](https://grafana.com/docs/grafana/latest/dashboards/)

### PromQL Cheat Sheet

```promql
# Aumentos
increase(metric[5m])            # Cambio en 5 minutos
rate(metric[5m])                # Rate por segundo

# Percentiles (latencia)
histogram_quantile(0.95, rate(metric_bucket[5m]))

# Top K
topk(10, metric)

# Suma
sum(metric)
sum by (label) (metric)         # Agrupar por etiqueta

# Filtros
metric{job="barberia-api"}
metric{status=~"5.."}           # Regex: 5xx
metric{status!~"5.."}           # NOT regex
```

---

## ✅ Checklist: Producción

Antes de llevar a producción:

- [ ] Cambiar contraseña admin de Grafana
- [ ] Configurar persistencia de volúmenes en producción
- [ ] Aumentar retención de Prometheus (>15 días)
- [ ] Configurar backups de datos
- [ ] Agregar límites de CPU/Memory en docker-compose
- [ ] Configurar alertas (AlertManager)
- [ ] Logging remoto (ELK, Loki, etc.)
- [ ] Configurar HTTPS/TLS

---

## 📞 Soporte

Para dudas sobre:
- **Prometheus:** Ver docs oficial
- **Grafana:** Ver docs oficial
- **Métricas de negocio:** Ver `MetricasPersonalizadasService.java`
- **Actuator config:** Ver `application.properties`

---

**Versión:** 1.0.0  
**Creado:** Mayo 2026  
**Stack:** Prometheus + Grafana + Micrometer + Spring Boot 3.1.5  
