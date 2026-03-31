# 💈 Barberia Service

Backend REST API para el sistema de gestión de barbería. Construido con Spring Boot 3 + PostgreSQL.

---

## 🎯 ¿Por dónde empezar?

### ✅ Para Nuevos Miembros del Team:
👉 **Lee [SETUP-DOCKER.md](./SETUP-DOCKER.md)** - La forma más fácil (3 minutos)

### 👨‍💻 Para Desarrolladores sin Docker:
👉 **Lee [SETUP-LOCAL.md](./SETUP-LOCAL.md)** - Con Java + Maven localmente

---

## 🚀 Inicio Rápido con Docker (RECOMENDADO PARA TEAM)

### ✅ Requisitos
- [Docker Desktop](https://www.docker.com/products/docker-desktop) instalado
- Acceso a las credenciales de Supabase (pídele al lead)

### 📋 Pasos (Primera vez)

**1️⃣ Clonar el repo:**
```bash
git clone <repo-url>
cd Barberia
```

**2️⃣ Crear archivo `.env` con credenciales:**
```bash
cp .env.example .env

# Edita .env con las credenciales que te pasó el lead:
# SPRING_DATASOURCE_URL=...
# SPRING_DATASOURCE_USERNAME=...
# SPRING_DATASOURCE_PASSWORD=...
```

**3️⃣ Construir e iniciar:**
```bash
docker-compose up --build
```

**¡Listo!** Verás los logs en tiempo real:

```
...
✅ CONEXIÓN A LA BASE DE DATOS ESTABLECIDA CORRECTAMENTE
✅ URL: jdbc:postgresql://aws-1-us-east-1.pooler.supabase.com:5432/postgres
✅ Usuario: postgres.wacfhjygmoagyegzupuz
✅ Driver: PostgreSQL JDBC Driver
✅ ============================================

Started BarberiServiceApplication in 6.834 seconds
```

### 🌐 Acceder a la API

| Recurso | URL |
|---------|-----|
| **API Base** | http://localhost:8080/api/ |
| **Swagger UI** | http://localhost:8080/api/swagger-ui.html |
| **OpenAPI Spec** | http://localhost:8080/api/v3/api-docs |

### 🛑 Para detener

```bash
docker-compose down
```

### 📦 Comandos útiles

| Comando | Descripción |
|---------|-------------|
| `docker-compose up --build` | Construir e iniciar (con logs en terminal) |
| `docker-compose up -d` | Iniciar en background |
| `docker-compose logs -f` | Ver logs (si está en background) |
| `docker-compose down` | Detener y limpiar contenedores |
| `docker-compose ps` | Ver estado de contenedores |

---

## 🔐 Variables de Entorno

El proyecto usa un archivo `.env` para las credenciales de BD.

⚠️ **IMPORTANTE:** `.env` está en `.gitignore` - **nunca se sube al repositorio** por seguridad.

### Cómo configurar:

```bash
cp .env.example .env
# Luego edita .env con tus credenciales
```

## 🛠️ Desarrollo Local (Sin Docker - Alternativa)

Si prefieres trabajar directo con Java sin Docker:

### ✅ Requisitos
- Java 21+ (OpenJDK o similar)
- Maven 3.9+
- Archivo `.env` configurado (ver sección anterior)

### 🚀 Ejecutar

**Opción 1: Con Make (Recomendado - Linux/Mac):**
```bash
make dev      # Compila y ejecuta
# o solo ejecutar si ya está compilado:
make run
```

**Opción 2: Con Script Shell:**
```bash
./run-dev.sh  # Sourcea .env y ejecuta la app
```

**Opción 3: Comandos Maven directos:**
```bash
cd backend
mvn clean package -DskipTests

# Luego cargar variables y ejecutar:
export $(cat ../.env | grep -v '^#' | xargs)
java -jar target/barberia-service-1.0.0.jar
```

---

## 📁 Estructura del Proyecto

```
Barberia/
├── backend/                          # Aplicación Spring Boot
│   ├── src/main/java/com/barberia/
│   │   ├── BarberiServiceApplication.java
│   │   ├── modules/
│   │   │   ├── modulo_usuarios/    # Gestión de Usuarios
│   │   │   │   ├── models/entities/
│   │   │   │   ├── models/dtos/
│   │   │   │   ├── repositories/
│   │   │   │   ├── services/
│   │   │   │   └── controllers/
│   │   │   └── modulo_auth/        # Autenticación y Autorización
│   │   │       ├── models/entities/
│   │   │       ├── models/dtos/
│   │   │       ├── repositories/
│   │   │       ├── services/
│   │   │       └── controllers/
│   │   ├── shared/                 # Código compartido
│   │   │   ├── config/
│   │   │   ├── exceptions/
│   │   │   └── utils/
│   │   └── resources/
│   │       └── application.properties
│   ├── pom.xml
│   └── target/                     # Compilados (generado)
├── Dockerfile                       # Build multi-stage 
├── docker-compose.yml              # Orquestación Docker
├── Makefile                        # Comandos build
├── run-dev.sh                      # Script de ejecución (sourcea .env)
├── .env                            # Credenciales (git-ignored ⚠️)
├── .env.example                    # Template de .env (en repo)
└── README.md                       # Este archivo
```

## � Troubleshooting - Docker en Kali Linux

Si al ejecutar `docker-compose up` ves el error:
```
org.postgresql.util.PSQLException: The connection attempt failed.
```

**Causa:** En Kali Linux, Docker tiene aislamiento de red que impide que el contenedor alcance servidores externos como Supabase.

### Soluciones (en orden de facilidad):

#### ✅ Opción 1: Usa el modo local (RECOMENDADO para Kali)
```bash
# En lugar de Docker, ejecuta localmente:
make dev
# La app funcionará en http://localhost:8080/api/
```

#### ⚠️ Opción 2: Usa `--network=host` (menos seguro, solo local)
**En `docker-compose.yml`**, agrega bajo `barberia-api`:
```yaml
services:
  barberia-api:
    network_mode: "host"
```

**Luego:**
```bash
docker-compose up --build
```

#### 🔧 Opción 3: Configurar bridge de red (avanzado)
Consulta la documentación de Docker para tu versión de Kali.

---

## �📋 Comandos Disponibles

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

## 🧪 Pruebas de Endpoints

### Swagger/OpenAPI (Recomendado)

Una vez que la aplicación esté corriendo, accede a:

```
http://localhost:8080/swagger-ui.html
```

**Swagger proporciona:**
- ✅ Documentación interactiva de todos los endpoints
- ✅ Interfaz visual para probar sin necesidad de Postman
- ✅ Descripciones de parámetros y respuestas
- ✅ Modelos de datos auto-documentados

**Tu equipo puede probar directamente:**
1. Abre `http://localhost:8080/swagger-ui.html`
2. Selecciona un endpoint
3. Click en **"Try it out"**
4. Llena los parámetros
5. Click **"Execute"**


