# Arquitectura Modular en Capas - Documentación

## Índice
1. [Concepto General](#concepto-general)
2. [Estructura de Carpetas](#estructura-de-carpetas)
3. [Componentes Compartidos](#componentes-compartidos)
4. [Anatomía de un Módulo](#anatomía-de-un-módulo)
5. [Creando un Nuevo Módulo](#creando-un-nuevo-módulo)
6. [Guía de Desarrollo](#guía-de-desarrollo)

## Concepto General

La arquitectura modular en capas combina dos patrones:

1. **Modularidad**: La aplicación se divide en módulos independientes
2. **Capas**: Cada módulo tiene sus propias capas (Controller → Service → Repository)

### Beneficios

- 🏗️ **Estructura Escalable**: Agregar nuevas funcionalidades sin afectar las existentes
- 👥 **Trabajo en Equipo**: Diferentes equipos pueden trabajar en diferentes módulos
- 🧪 **Testeable**: Cada módulo se puede probar independientemente
- 📦 **Reutilizable**: Código compartido en la carpeta `shared/`
- 🔧 **Mantenible**: Código organizado y fácil de encontrar

## Estructura de Carpetas

```
backend/src/main/java/com/barberia/
│
├── shared/                          ← Código compartido
│   ├── config/                      ← Configuración global
│   │   └── AppConfig.java
│   ├── exceptions/                  ← Excepciones personalizadas
│   │   ├── ResourceNotFoundException.java
│   │   └── GlobalExceptionHandler.java
│   └── utils/                       ← Utilidades comunes
│       └── ApiResponse.java
│
├── modules/                         ← Módulos específicos
│   │
│   ├── modulo_usuarios/             ← Gestión de Usuarios
│   │   ├── controllers/
│   │   │   └── UsuarioController.java
│   │   ├── services/
│   │   │   └── UsuarioService.java
│   │   ├── repositories/
│   │   │   └── UsuarioRepository.java
│   │   └── models/
│   │       ├── entities/
│   │       │   └── Usuario.java
│   │       └── dtos/
│   │           └── UsuarioDTO.java
│   │
│   ├── modulo_auth/                 ← Autenticación y Autorización
│   │   ├── controllers/
│   │   │   └── AuthController.java
│   │   ├── services/
│   │   │   └── AuthService.java
│   │   ├── repositories/
│   │   │   └── (usa UsuarioRepository del modulo_usuarios)
│   │   └── models/
│   │       ├── entities/
│   │       │   └── Token.java
│   │       └── dtos/
│   │           ├── LoginDTO.java
│   │           └── TokenDTO.java
│   │
│   └── modulo_servicios/            ← Próximos módulos
│       └── ... (misma estructura)
│
└── BarberiServiceApplication.java   ← Punto de entrada
```

## Componentes Compartidos

### 📁 shared/config/
Configuración de Spring y la aplicación.

```java
@Configuration
public class AppConfig {
    // Beans compartidos
}
```

### 📁 shared/exceptions/
Excepciones globales y handler de errores.

```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<...> handleResourceNotFound(...) { }
}
```

### 📁 shared/utils/
Utilidades reutilizables en toda la aplicación.

```java
public class ApiResponse<T> {
    private boolean success;
    private String message;
    private T data;
}
```

## Anatomía de un Módulo

Cada módulo contiene una arquitectura en capas:

### 1️⃣ Models

**Entities (Base de Datos)**
```java
@Entity
@Table(name = "servicios")
public class Servicio {
    @Id
    private Long id;
    @Column
    private String nombre;
    // ... más campos
}
```

**DTOs (Transferencia de Datos)**
```java
@Data
public class ServicioDTO {
    private Long id;
    private String nombre;
    // ... campos públicos
}
```

### 2️⃣ Repository Layer

Acceso a base de datos con Spring Data JPA:

```java
@Repository
public interface ServicioRepository extends JpaRepository<Servicio, Long> {
    List<Servicio> findByActivoTrue();
    Optional<Servicio> findByNombreIgnoreCase(String nombre);
}
```

### 3️⃣ Service Layer

Lógica de negocio:

```java
@Service
public class ServicioService {
    @Autowired
    private ServicioRepository servicioRepository;
    
    public List<ServicioDTO> obtenerTodosLosServicios() {
        return servicioRepository.findByActivoTrue()
            .stream()
            .map(this::convertirADTO)
            .collect(Collectors.toList());
    }
    
    // ... más métodos
}
```

**Responsabilidades del Service:**
- ✅ Validar datos
- ✅ Aplicar reglas de negocio
- ✅ Transformar Entities a DTOs
- ✅ Orquestar operaciones complejas

### 4️⃣ Controller Layer

REST API endpoints:

```java
@RestController
@RequestMapping("/api/servicios")
public class ServicioController {
    @Autowired
    private ServicioService servicioService;
    
    @GetMapping
    public ResponseEntity<ApiResponse<List<ServicioDTO>>> obtenerTodos() {
        List<ServicioDTO> servicios = servicioService.obtenerTodosLosServicios();
        return ResponseEntity.ok(
            ApiResponse.success("Servicios obtenidos", servicios)
        );
    }
}
```

**Responsabilidades del Controller:**
- ✅ Recibir solicitudes HTTP
- ✅ Validar entrada
- ✅ Llamar al Service
- ✅ Retornar respuesta formateada

## Creando un Nuevo Módulo

Imagina que quieres crear un módulo `modulo_servicios` para gestionar **Servicios de Barbería** (cortes, afeitados, etc):

### Paso 1: Crear la estructura de carpetas

```
modules/modulo_servicios/
├── controllers/
├── services/
├── repositories/
└── models/
    ├── entities/
    └── dtos/
```

### Paso 2: Crear la Entity

```java
// modules/modulo_servicios/models/entities/Servicio.java
@Entity
@Table(name = "servicios")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Servicio {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String nombre;
    
    @Column
    private String descripcion;
    
    @Column(nullable = false)
    private BigDecimal precio;
    
    @Column
    private Integer duracionMinutos;
    
    @Column(nullable = false)
    private Boolean activo = true;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime creadoEn = LocalDateTime.now();
}
```

### Paso 3: Crear el DTO

```java
// modules/modulo_servicios/models/dtos/ServicioDTO.java
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ServicioDTO {
    private Long id;
    private String nombre;
    private String descripcion;
    private BigDecimal precio;
    private Integer duracionMinutos;
    private Boolean activo;
}
```

### Paso 4: Crear el Repository

```java
// modules/modulo_servicios/repositories/ServicioRepository.java
@Repository
public interface ServicioRepository extends JpaRepository<Servicio, Long> {
    List<Servicio> findByActivoTrue();
    Optional<Servicio> findByNombreIgnoreCase(String nombre);
    List<Servicio> findByPrecioBetween(BigDecimal min, BigDecimal max);
}
```

### Paso 5: Crear el Service

```java
// modules/modulo_servicios/services/ServicioService.java
@Service
public class ServicioService {
    @Autowired
    private ServicioRepository servicioRepository;
    
    public List<ServicioDTO> obtenerTodosLosServicios() {
        return servicioRepository.findByActivoTrue()
            .stream()
            .map(this::convertirADTO)
            .collect(Collectors.toList());
    }
    
    public Optional<ServicioDTO> obtenerPorId(Long id) {
        return servicioRepository.findById(id)
            .map(this::convertirADTO);
    }
    
    public ServicioDTO crearServicio(ServicioDTO servicioDTO) {
        Servicio servicio = Servicio.builder()
            .nombre(servicioDTO.getNombre())
            .descripcion(servicioDTO.getDescripcion())
            .precio(servicioDTO.getPrecio())
            .duracionMinutos(servicioDTO.getDuracionMinutos())
            .activo(true)
            .build();
        Servicio guardado = servicioRepository.save(servicio);
        return convertirADTO(guardado);
    }
    
    private ServicioDTO convertirADTO(Servicio servicio) {
        return new ServicioDTO(servicio.getId(), servicio.getNombre(),
            servicio.getDescripcion(), servicio.getPrecio(),
            servicio.getDuracionMinutos(), servicio.getActivo());
    }
}
```

### Paso 6: Crear el Controller

```java
// modules/modulo_servicios/controllers/ServicioController.java
@RestController
@RequestMapping("/api/servicios")
public class ServicioController {
    @Autowired
    private ServicioService servicioService;
    
    @GetMapping
    public ResponseEntity<ApiResponse<List<ServicioDTO>>> obtenerTodos() {
        List<ServicioDTO> servicios = servicioService.obtenerTodosLosServicios();
        return ResponseEntity.ok(
            ApiResponse.success("Servicios obtenidos correctamente", servicios)
        );
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ServicioDTO>> obtenerPorId(@PathVariable Long id) {
        return servicioService.obtenerPorId(id)
            .map(servicio -> ResponseEntity.ok(
                ApiResponse.success("Servicio obtenido", servicio)
            ))
            .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error("Servicio no encontrado")));
    }
    
    @PostMapping
    public ResponseEntity<ApiResponse<ServicioDTO>> crear(@RequestBody ServicioDTO servicioDTO) {
        ServicioDTO creado = servicioService.crearServicio(servicioDTO);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success("Servicio creado", creado));
    }
}
```

## Guía de Desarrollo

### Convenciones de Nombres

| Componente | Patrón | Ejemplo |
|-----------|--------|---------|
| Entity | `{Nombre}` | `Usuario`, `Servicio`, `Token` |
| DTO | `{Nombre}DTO` | `UsuarioDTO`, `ServicioDTO`, `LoginDTO` |
| Repository | `{Nombre}Repository` | `UsuarioRepository`, `ServicioRepository` |
| Service | `{Nombre}Service` | `UsuarioService`, `ServicioService` |
| Controller | `{Nombre}Controller` | `UsuarioController`, `ServicioController` |
| Package | `com.barberia.modules.{modulo}.{capa}` | `com.barberia.modules.modulo_usuarios.controllers` |
| Módulo | `modulo_{nombre}` | `modulo_usuarios`, `modulo_auth`, `modulo_servicios` |

### Flujo de Desarrollo

```
1. Diseña la Entity (tabla en BD)
   ↓
2. Crea el DTO (objeto para API)
   ↓
3. Implementa el Repository (acceso a BD)
   ↓
4. Implementa el Service (lógica de negocio)
   ↓
5. Implementa el Controller (REST API)
   ↓
6. Test cada capa
```

### Best Practices

✅ **Usar DTOs**: Nunca expongas Entities directamente en los endpoints  
✅ **Validar en Service**: La lógica de validación va en Service, no en Controller  
✅ **Reutilizar Excepciones**: Usa las excepciones de `shared/exceptions/`  
✅ **Usar ApiResponse**: Todas las respuestas deben usar `ApiResponse`  
✅ **Inyección de Dependencias**: Usa `@Autowired` en lugar de `new`  
✅ **Comments**: Documenta métodos públicos con JavaDoc  
✅ **Tests**: Crea tests para cada componente  

### Importes Comunes

```java
// Controller
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

// Service
import org.springframework.stereotype.Service;
import java.util.stream.Collectors;

// Repository  
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

// Entity
import jakarta.persistence.*;
import lombok.*;

// Shared
import com.barberia.shared.exceptions.ResourceNotFoundException;
import com.barberia.shared.utils.ApiResponse;
```

## Troubleshooting

### ❌ No encuentro una clase que espero que esté

Verifica que esté en el package correcto:
```
com.barberia.modules.{tuModulo}.{capa}.{NombreClase}
```

### ❌ Tengo una dependencia circular entre módulos

La solución es mover el código compartido a `shared/` o refactorizar la lógica.

### ❌ El DTO está mostrando más datos de los que quiero

Crea DTOs específicos para diferentes casos de uso:
- `ServicioDTO`: Información general
- `ServicioDetailDTO`: Información completa
- `ServicioCreateDTO`: Solo los campos para crear

---

**¡Listo!** Ahora tienes una arquitectura modular escalable y bien organizada. 🚀
