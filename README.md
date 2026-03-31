# 💈 Barberia Service

Backend REST API para el sistema de gestión de barbería. Construido con Spring Boot 3 + PostgreSQL.

---

## 🚀 Inicio Rápido (Recomendado - Docker)

### Requisitos
- [Docker Desktop](https://www.docker.com/products/docker-desktop) instalado

### Ejecutar la aplicación

```bash
# En la raíz del proyecto
docker-compose up --build
```

**Eso es todo.** La aplicación estará corriendo y verás los logs en la terminal:

```
...
✅ CONEXIÓN A LA BASE DE DATOS ESTABLECIDA CORRECTAMENTE
✅ URL: jdbc:postgresql://db.wacfhjygmoagyegzupuz.supabase.co:5432/postgres
✅ Usuario: postgres
✅ ============================================

Started BarberiServiceApplication in ...
```

**API disponible en:** `http://localhost:8080/api/`

**Para parar:** `Ctrl+C` en la terminal

---

## 🛠️ Desarrollo Local (Sin Docker)

Si prefieres trabajar localmente sin Docker:

### Requisitos
- Java 21+
- Maven 3.9+

### Ejecutar

**Opción 1: Con Make (Recomendado):**
```bash
make dev      # Compila y ejecuta
```

**Opción 2: Con Scripts:**
```bash
./dev.sh      # Rápido (sin recompilar si ya compiló)
./run.sh      # Compila y ejecuta
```

**Opción 3: Comandos Maven:**
```bash
cd backend
mvn clean package -DskipTests
java -jar target/barberia-service-1.0.0.jar
```

---

## 📋 Comandos Disponibles

### Con Docker
```bash
docker-compose up --build    # Compilar y ejecutar
docker-compose up            # Solo ejecutar
docker-compose down          # Parar
```

### Con Make (Development local)
```bash
make dev       # Compilar y ejecutar
make run       # Solo ejecutar (más rápido)
make build     # Solo compilar
make clean     # Limpiar archivos compilados
make help      # Ver todos los comandos
```

---

## 🗂️ Estructura del Proyecto

```
Barberia/
├── backend/
│   ├── src/main/java/com/barberia/
│   │   ├── modules/
│   │   │   ├── modulo1/    (Servicios)
│   │   │   └── modulo2/    (Clientes)
│   │   └── shared/         (Config, Excepciones)
│   ├── pom.xml
│   └── README.md
├── Dockerfile              # Configuración Docker
├── docker-compose.yml      # Orquestación de contenedores
├── Makefile                # Comandos útiles
├── run.sh                  # Script para compilar + ejecutar
└── dev.sh                  # Script rápido (sin recompilar)
```

---

## 🔗 Endpoints

### Cliente API
- `GET /api/cliente` - Listar clientes
- `GET /api/cliente/{id}` - Obtener cliente
- `POST /api/cliente` - Crear cliente
- `PUT /api/cliente/{id}` - Editar cliente
- `DELETE /api/cliente/{id}` - Eliminar cliente

### Servicio API
- `GET /api/servicio` - Listar servicios
- `GET /api/servicio/{id}` - Obtener servicio
- `POST /api/servicio` - Crear servicio
- `PUT /api/servicio/{id}` - Editar servicio
- `DELETE /api/servicio/{id}` - Eliminar servicio

---

## 🗄️ Base de Datos

**Proveedor:** Supabase (PostgreSQL)  
**Host:** `db.wacfhjygmoagyegzupuz.supabase.co`  
**Puerto:** 5432  
**BD:** `postgres`  

### Variables de Entorno

En `docker-compose.yml`:
```yaml
SPRING_DATASOURCE_URL: jdbc:postgresql://...
SPRING_DATASOURCE_USERNAME: postgres
SPRING_DATASOURCE_PASSWORD: fabricaudeA123*
```

---

## 📝 Configuración

### Archivo: `backend/src/main/resources/application.properties`

- `server.port` - Puerto del servidor (default: 8080)
- `spring.datasource.*` - Credenciales de BD
- `spring.jpa.hibernate.ddl-auto` - Estrategia de generación de tablas
- `logging.level.*` - Niveles de log

---

## 🐛 Solución de Problemas

### "Puerto 8080 ya está en uso"
```bash
# Kill el proceso que usa el puerto
lsof -i :8080
kill -9 <PID>

# O cambiar puerto en docker-compose.yml
ports:
  - "8081:8080"  # Usar puerto 8081
```

### "Error de conexión a BD"
- Verificar que Supabase esté activo
- Comprobar credenciales en `application.properties`
- Ver logs: En Docker, los logs aparecen directamente en la terminal

### "Maven or Java no encontrado" (sin Docker)
```bash
# Instalar Docker y usar: docker-compose up --build
```

---

## 👥 Para el Equipo

**Forma más simple:** Todos usan Docker
```bash
docker-compose up --build
```

✅ No hay problemas de versiones  
✅ Funciona igual en Windows, Mac, Linux  
✅ No necesitan instalar nada excepto Docker  
✅ Los logs aparecen en la terminal  

---

## 📄 Licencia

MIT

## ✉️ Contacto

Para dudas sobre la configuración, ver el `ARCHITECTURE.md`
