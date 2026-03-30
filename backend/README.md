# Backend - Barberia Service

## Descripción
Backend del sistema de gestión de barbería desarrollado con Spring Boot siguiendo una **arquitectura modular en capas**.

## Estructura del Proyecto

```
backend/
├── src/
│   ├── main/
│   │   ├── java/com/barberia/
│   │   │   ├── shared/                     # Código compartido entre módulos
│   │   │   │   ├── config/                 # Configuración de Spring
│   │   │   │   ├── exceptions/             # Excepciones compartidas
│   │   │   │   └── utils/                  # Utilidades globales
│   │   │   │
│   │   │   └── modules/                    # Módulos de la aplicación
│   │   │       │
│   │   │       ├── modulo1/               # MÓDULO 1: Servicios (Cortes, Afeitados, etc)
│   │   │       │   ├── controllers/        # Capa de Presentación (REST)
│   │   │       │   ├── services/           # Capa de Lógica de Negocio
│   │   │       │   ├── repositories/       # Capa de Acceso a Datos
│   │   │       │   └── models/
│   │   │       │       ├── entities/       # Entidades JPA
│   │   │       │       └── dtos/           # Data Transfer Objects
│   │   │       │
│   │   │       └── modulo2/               # MÓDULO 2: Clientes
│   │   │           ├── controllers/        # Capa de Presentación (REST)
│   │   │           ├── services/           # Capa de Lógica de Negocio
│   │   │           ├── repositories/       # Capa de Acceso a Datos
│   │   │           └── models/
│   │   │               ├── entities/       # Entidades JPA
│   │   │               └── dtos/           # Data Transfer Objects
│   │   │
│   │   └── resources/
│   │       └── application.properties
│   └── test/                       # Tests unitarios
├── pom.xml                         # Dependencias Maven
└── README.md
```

## Arquitectura Modular en Capas

### 📦 Componentes Compartidos (Shared)
Código y utilidades que usan todos los módulos:
- **config**: Configuración global de Spring
- **exceptions**: Excepciones personalizadas
- **utils**: ApiResponse, helpers, etc.

### 🔷 MÓDULO 1: Servicios
Gestión de servicios de barbería (cortes, afeitados, etc.)

**Capas:**
- **Controllers** (`/api/servicios`): REST endpoints
- **Services**: Lógica de negocio
- **Repositories**: Acceso a BD
- **Models**: Entidades y DTOs

**Endpoints:**
- `GET /api/servicios` - Obtener todos
- `GET /api/servicios/{id}` - Obtener uno
- `POST /api/servicios` - Crear
- `PUT /api/servicios/{id}` - Actualizar
- `DELETE /api/servicios/{id}` - Desactivar

### 🔷 MÓDULO 2: Clientes
Gestión de clientes y sus datos

**Capas:**
- **Controllers** (`/api/clientes`): REST endpoints
- **Services**: Lógica de negocio
- **Repositories**: Acceso a BD
- **Models**: Entidades y DTOs

**Endpoints:**
- `GET /api/clientes` - Obtener todos
- `GET /api/clientes/{id}` - Obtener uno
- `GET /api/clientes/email/{email}` - Buscar por email
- `POST /api/clientes` - Crear
- `PUT /api/clientes/{id}` - Actualizar
- `DELETE /api/clientes/{id}` - Desactivar

## Capas de la Arquitectura Explicadas

### 1. **Capa de Presentación (Controllers)**
- Recibe y valida solicitudes HTTP
- Retorna respuestas formateadas
- Maneja el mapeo de rutas

### 2. **Capa de Lógica de Negocio (Services)**
- Contiene la lógica de negocio
- Orquesta entre Controllers y Repositories
- Realiza validaciones y transformaciones

### 3. **Capa de Acceso a Datos (Repositories)**
- Interactúa con la base de datos
- Implementa queries JPA
- Abstrae la persistencia de datos

### 4. **Capa de Modelos (Models)**
- **Entities**: Representan tablas de BD (JPA)
- **DTOs**: Objetos para transferencia entre capas

## Ventajas de la Arquitectura Modular

✅ **Escalabilidad**: Fácil agregar nuevos módulos  
✅ **Mantenibilidad**: Cada módulo es independiente  
✅ **Trabajo en equipo**: Múltiples equipos pueden trabajar en paralelo  
✅ **Reutilización**: Código compartido en `shared/`  
✅ **Testabilidad**: Cada módulo puede ser testeado independientemente  

## Cómo Agregar un Nuevo Módulo

1. Crear carpeta en `modules/modulo3/`
2. Crear subdirectorios: `controllers/`, `services/`, `repositories/`, `models/`
3. Crear Entity → DTO → Repository → Service → Controller
4. Los helpers compartidos están en `shared/`

## Requisitos

- Java 17 o superior
- Maven 3.6+
- MySQL 8.0+

## Instalación y Ejecución

### 1. Configurar la base de datos
Actualizar `application.properties`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/barberia_db
spring.datasource.username=root
spring.datasource.password=tu_password
```

### 2. Compilar y ejecutar

```bash
# Compilar
mvn clean install

# Ejecutar la aplicación
mvn spring-boot:run
```

La aplicación estará disponible en: `http://localhost:8080/api`

## Convenciones de Código

- **Controllers**: `XxxController`
- **Services**: `XxxService`
- **Repositories**: `XxxRepository`
- **Entities**: `Xxx` (Pascal case)
- **DTOs**: `XxxDTO`
- **Packages**: `com.barberia.modules.moduloX.{capa}`

## Flujo de una Solicitud HTTP

```
Cliente HTTP
     ↓
Controller (recibe y valida)
     ↓
Service (procesa lógica)
     ↓
Repository (accede a BD)
     ↓
Entity (mapea con tabla)
     ↓
Repository (retorna datos)
     ↓
Service (transforma a DTO)
     ↓
Controller (retorna respuesta)
     ↓
Cliente HTTP
```

## Licencia

Este proyecto es privado.
