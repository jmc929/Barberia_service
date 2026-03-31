# 💻 Guía: Usar Barberia Localmente con Make (Para Desarrolladores)

Instrucciones para ejecutar sin Docker (ideal si prefieres trabajar con la app en tu máquina).

---

## ⏱️ Tiempo Total: ~5-10 minutos (más tiempo la primera compila)

---

## ✅ Requisitos

Verifica que tienes instalado:

```bash
# Java 21+
java --version
# Expected: openjdk 21.0.10 o similar

# Maven 3.9+
mvn --version
# Expected: Apache Maven 3.9.x

# Git
git --version
```

Si te falta algo, instálalo:

**Ubuntu/Debian:**
```bash
sudo apt install openjdk-21-jdk maven
```

**macOS (con Homebrew):**
```bash
brew install openjdk@21 maven
```

**Windows:**
Descarga e instala desde:
- [OpenJDK 21](https://jdk.java.net/21/)
- [Maven](https://maven.apache.org/download.cgi)

---

## 📥 Paso 1: Clonar y Preparar

```bash
git clone <URL_DEL_REPO>
cd Barberia

# Copiar configuración de base de datos
cp .env.example .env
nano .env  # Editar con tus credenciales de Supabase
```

---

## 🚀 Paso 2: Ejecutar con Make

### Primera vez (Compilar + Ejecutar)

```bash
make dev
```

La primera compilación toma ~30-60 segundos. Verás algo como:

```
🔨 Compilando proyecto...
[INFO] Building Barberia Service 1.0.0
[INFO] --- compiler:3.11.0:compile ---
[INFO] Compiling 7 source files
...
✅ Compilación exitosa
🚀 Iniciando Barberia Backend en http://localhost:8080/api/

  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 :: Spring Boot ::                (v3.1.5)

2026-03-31T16:05:47.143-05:00  INFO 117807 --- [           main] com.barberia.BarberiServiceApplication : Started BarberiServiceApplication in 6.834 seconds
```

**✅ Funciona! Presiona `Ctrl+C` para detener**

### Siguientes veces (sin recompilar)

```bash
make run
```

Mucho más rápido (~2 segundos).

### Solo Compilar (sin ejecutar)

```bash
make build
```

---

## 🔄 Trabajo Diario

### Ciclo Típico de Desarrollo

```bash
# Terminal 1: Ejecutar la app
make run

# Terminal 2: Hacer cambios en el código
# Editar archivos .java
# ...

# Cuando cambios en el código, presiona Ctrl+C en Terminal 1 y ejecuta:
make dev  # Recompila + ejecuta con cambios nuevos
```

---

## 🌐 Acceder a la Aplicación

| Recurso | URL |
|---------|-----|
| **API REST** | http://localhost:8080/api/ |
| **Swagger UI** | http://localhost:8080/api/swagger-ui.html |
| **OpenAPI Spec** | http://localhost:8080/api/v3/api-docs |

---

## ✅ Verificar que Funciona

```bash
curl http://localhost:8080/api/v3/api-docs | jq '.info.title'
```

Resultado esperado:
```
"Barberia Service API"
```

---

## 📝 Comandos Make Disponibles

| Comando | Descripción |
|---------|-------------|
| `make dev` | Compila + Ejecuta (primera vez) |
| `make run` | Solo ejecuta (ya compilado) |
| `make build` | Solo compila sin ejecutar |
| `make clean` | Limpia archivos compilados |
| `make help` | Muestra todos los comandos |

---

## ⚙️ Configuración Maven

El proyecto usa:
- **Spring Boot 3.1.5**
- **Java 21** (target/source)
- **PostgreSQL JDBC Driver 42.6.0**
- **Hibernate 6.2.13.Final**

Todas estas dependencias se descargan automáticamente en el primer `make dev`.

---

## 🐛 Debug

### Ver todos los logs en detalle

```bash
# En make run, agrega debug:
JAVA_OPTS="-Dlogging.level.root=DEBUG" make run
```

### Compilar sin ejecutar tests

```bash
cd backend
mvn clean package -DskipTests
```

### Ir directamente sin Make

```bash
cd backend
export $(cat ../.env | xargs)
java -jar target/barberia-service-1.0.0.jar
```

---

## 🆘 Solucionar Problemas

### ❌ Error: Java not found
```bash
which java
# Si está vacío, instala OpenJDK 21 (ver "Requisitos" arriba)
```

### ❌ Error: "Port 8080 already in use"
```bash
# Otra app usa el puerto. Opciones:

# 1. Mata el proceso anterior:
pkill -f "java.*barberia"

# 2. O usa otro puerto (editar application.properties):
# server.port=8081
```

### ❌ Error: "Cannot invoke password providers"
**Causa:** Las variables en `.env` no se cargan correctamente.

**Solución:**
```bash
# Verifica que .env existe y tiene valores
cat .env

# Si falta, copiar de nuevo:
cp .env.example .env
nano .env  # Editar con credenciales correctas
```

### ❌ Error Maven: OutOfMemoryError
```bash
# Aumentar memoria para Maven:
export MAVEN_OPTS="-Xmx1024m"
make dev
```

---

## 📚 Más Información

- [README.md](./README.md) - Visión general del proyecto
- [ARCHITECTURE.md](./backend/ARCHITECTURE.md) - Estructura del código
- [SETUP-DOCKER.md](./SETUP-DOCKER.md) - Alternativa con Docker

---

## 🚀 Próximos Pasos

1. Revisar los endpoints en Swagger UI
2. Leer la arquitectura del proyecto
3. Comenzar a hacer cambios

¡Happy coding! 🎉
