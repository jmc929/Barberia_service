# Barberia Service - Documentación API REST

**Base URL:** `http://localhost:8080/api/v1`  
**Autenticación:** Bearer Token (JWT) — obtenido desde `/auth/login`  
**Swagger UI:** `http://localhost:8080/api/swagger-ui.html`

---

## Índice

- [Autenticación](#autenticación)
- [Usuarios](#usuarios)
- [Servicios](#servicios)
- [Horarios](#horarios)
- [Agendamiento](#agendamiento)
- [Citas](#citas)
- [Códigos de respuesta](#códigos-de-respuesta)

---

## Autenticación

### POST `/auth/login`
Inicia sesión y retorna un token JWT válido por 24 horas.

**Acceso:** Público

**Body:**
```json
{
  "email": "usuario@email.com",
  "contraseña": "miContraseña123"
}
```

**Respuesta exitosa (200):**
```json
{
  "success": true,
  "message": "Login exitoso",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "type": "Bearer",
    "usuario": {
      "numeroDocumento": "123456789",
      "nombrePersona": "Juan Pérez",
      "email": "usuario@email.com",
      "idRol": 3
    }
  }
}
```

**Errores:**
| Código | Descripción |
|--------|-------------|
| 401 | Credenciales incorrectas |
| 500 | Error interno del servidor |

---

## Usuarios

### POST `/personas/registro`
Registra un nuevo usuario en el sistema.

**Acceso:** Público

**Body:**
```json
{
  "numeroDocumento": "123456789",
  "numeroCelular": "3001234567",
  "email": "usuario@email.com",
  "nombrePersona": "Juan Pérez",
  "contraseña": "miContraseña123",
  "confirmarContraseña": "miContraseña123"
}
```

**Respuesta exitosa (201):**
```json
{
  "success": true,
  "message": "Persona registrada exitosamente",
  "data": {
    "numeroDocumento": "123456789",
    "numeroCelular": "3001234567",
    "email": "usuario@email.com",
    "nombrePersona": "Juan Pérez",
    "idEstado": 1,
    "idRol": 3,
    "fechaRegistro": "2026-05-01T10:00:00"
  }
}
```

**Errores:**
| Código | Descripción |
|--------|-------------|
| 400 | Datos inválidos o contraseñas no coinciden |

---

### GET `/personas`
Lista todos los usuarios registrados.

**Acceso:** 🔒 Admin (ROLE_1)

**Headers:**
```
Authorization: Bearer <token>
```

**Respuesta exitosa (200):** Lista de objetos `UsuarioDTO`

---

### GET `/personas/documento/{numeroDocumento}`
Busca un usuario por su número de documento.

**Acceso:** 🔒 Admin (ROLE_1)

**Parámetros de ruta:**
| Parámetro | Tipo | Descripción |
|-----------|------|-------------|
| `numeroDocumento` | String | Cédula o documento del usuario |

**Errores:**
| Código | Descripción |
|--------|-------------|
| 404 | Usuario no encontrado |

---

### GET `/personas/email/{email}`
Busca un usuario por su email.

**Acceso:** 🔒 Admin (ROLE_1)

**Parámetros de ruta:**
| Parámetro | Tipo | Descripción |
|-----------|------|-------------|
| `email` | String | Correo electrónico del usuario |

---

### GET `/personas/barberos`
Lista todos los usuarios con rol de barbero (idRol = 2).

**Acceso:** 🔒 Admin (ROLE_1)

**Respuesta exitosa (200):** Lista de objetos `UsuarioDTO`

---

### PUT `/personas/{numeroDocumento}/rol`
Cambia el rol de un usuario.

**Acceso:** 🔒 Admin (ROLE_1)

**Parámetros de ruta:**
| Parámetro | Tipo | Descripción |
|-----------|------|-------------|
| `numeroDocumento` | String | Documento del usuario |

**Query params:**
| Parámetro | Tipo | Descripción |
|-----------|------|-------------|
| `nuevoRol` | Integer | ID del nuevo rol (1=Admin, 2=Barbero, 3=Cliente) |

**Ejemplo:**
```
PUT /personas/123456789/rol?nuevoRol=2
```

---

## Servicios

### GET `/servicios`
Lista todos los servicios disponibles en el catálogo.

**Acceso:** Público

**Respuesta exitosa (200):**
```json
{
  "success": true,
  "message": "Servicios obtenidos",
  "data": [
    {
      "idServicio": 1,
      "nombreServicio": "Corte de cabello",
      "descripcion": "Corte clásico con tijeras",
      "duracion": 30,
      "costo": 25000.00,
      "idEstado": 1
    }
  ]
}
```

---

### GET `/servicios/buscar/{id}`
Busca un servicio por su ID.

**Acceso:** Público

**Parámetros de ruta:**
| Parámetro | Tipo | Descripción |
|-----------|------|-------------|
| `id` | Long | ID del servicio |

**Errores:**
| Código | Descripción |
|--------|-------------|
| 404 | Servicio no encontrado |

---

### POST `/servicios/crear`
Crea un nuevo servicio en el catálogo.

**Acceso:** 🔒 Admin (ROLE_1)

**Body:**
```json
{
  "nombreServicio": "Corte y barba",
  "descripcion": "Corte de cabello más arreglo de barba",
  "duracion": 45,
  "costo": 40000.00,
  "idEstado": 1
}
```

**Respuesta exitosa (201):** Objeto `ServicioDTO` con `idServicio` generado

---

## Horarios

### GET `/horarios`
Lista los horarios de atención de todos los días.

**Acceso:** Público

**Respuesta exitosa (200):**
```json
{
  "success": true,
  "message": "Horarios obtenidos",
  "data": [
    {
      "idHorarioNegocio": 1,
      "idDia": 1,
      "horaApertura": "08:00:00",
      "horaCierre": "18:00:00",
      "localAbierto": true
    }
  ]
}
```

> Los días van de 1 (Lunes) a 7 (Domingo)

---

### GET `/horarios/buscar/{idDia}`
Retorna el horario de un día específico.

**Acceso:** Público

**Parámetros de ruta:**
| Parámetro | Tipo | Descripción |
|-----------|------|-------------|
| `idDia` | Long | ID del día (1=Lunes, 7=Domingo) |

---

### PUT `/horarios/actualizar/{idDia}`
Actualiza la hora de apertura y cierre de un día.

**Acceso:** 🔒 Admin (ROLE_1)

**Body:**
```json
{
  "horaApertura": "09:00:00",
  "horaCierre": "19:00:00"
}
```

---

### PUT `/horarios/cerrar/{idDia}`
Marca un día como cerrado (sin atención).

**Acceso:** 🔒 Admin (ROLE_1)

**Parámetros de ruta:**
| Parámetro | Tipo | Descripción |
|-----------|------|-------------|
| `idDia` | Long | ID del día a cerrar |

---

## Agendamiento

### POST `/agendamiento/disponibilidad`
Verifica si un barbero tiene disponibilidad en una fecha y hora.

**Acceso:** Público

**Body:**
```json
{
  "numeroDocumentoPeluquero": "987654321",
  "idServicio": 1,
  "fechaCita": "2026-05-15",
  "horaInicioCita": "09:00:00",
  "horaFinCita": "09:30:00"
}
```

**Respuesta exitosa (200):**
```json
{
  "success": true,
  "data": {
    "disponible": true,
    "mensaje": "Horario disponible",
    "numeroDocumentoPeluquero": "987654321",
    "idServicio": 1,
    "fechaCita": "2026-05-15",
    "horaInicioCita": "09:00:00",
    "horaFinCita": "09:30:00"
  }
}
```

---

### POST `/agendamiento/agendar`
Crea una nueva cita. El cliente se toma del token JWT.

**Acceso:** 🔒 Autenticado (cualquier rol)

**Body:**
```json
{
  "numeroDocumentoPeluquero": "987654321",
  "idServicio": 1,
  "fechaCita": "2026-05-15",
  "horaInicioCita": "09:00:00",
  "horaFinCita": "09:30:00"
}
```

**Respuesta exitosa (201):**
```json
{
  "success": true,
  "message": "Cita agendada",
  "data": {
    "noCita": 42,
    "numeroDocumentoCliente": "123456789",
    "numeroDocumentoPeluquero": "987654321",
    "idServicio": 1,
    "fechaCita": "2026-05-15",
    "horaInicioCita": "09:00:00",
    "horaFinCita": "09:30:00",
    "idEstado": 1,
    "citaConfirmada": false,
    "fechaCreacion": "2026-05-01T10:00:00Z"
  }
}
```

---

### POST `/agendamiento/cancelar/{noCita}`
Cancela una cita existente.

**Acceso:** Público

**Parámetros de ruta:**
| Parámetro | Tipo | Descripción |
|-----------|------|-------------|
| `noCita` | Long | Número de la cita |

---

### PUT `/agendamiento/confirmar/{noCita}`
El barbero autenticado confirma una cita asignada a él.

**Acceso:** 🔒 Autenticado (barbero)

**Parámetros de ruta:**
| Parámetro | Tipo | Descripción |
|-----------|------|-------------|
| `noCita` | Long | Número de la cita a confirmar |

---

### PUT `/agendamiento/reprogramar/{noCita}`
Cambia la fecha y hora de una cita existente.

**Acceso:** Público

**Parámetros de ruta:**
| Parámetro | Tipo | Descripción |
|-----------|------|-------------|
| `noCita` | Long | Número de la cita |

**Body:**
```json
{
  "fechaCita": "2026-05-20",
  "horaInicioCita": "10:00:00",
  "horaFinCita": "10:30:00"
}
```

---

## Citas

### GET `/citas`
Lista todas las citas del sistema.

**Acceso:** Público

---

### GET `/citas/buscar/{noCita}`
Busca una cita por su número.

**Acceso:** Público

**Parámetros de ruta:**
| Parámetro | Tipo | Descripción |
|-----------|------|-------------|
| `noCita` | Long | Número de la cita |

**Errores:**
| Código | Descripción |
|--------|-------------|
| 404 | Cita no encontrada |

---

## Códigos de respuesta

| Código | Significado |
|--------|-------------|
| 200 | OK — Solicitud exitosa |
| 201 | Created — Recurso creado |
| 400 | Bad Request — Datos inválidos |
| 401 | Unauthorized — Token inválido o ausente |
| 403 | Forbidden — Sin permisos suficientes |
| 404 | Not Found — Recurso no encontrado |
| 500 | Internal Server Error — Error del servidor |

## Roles

| ID | Rol |
|----|-----|
| 1 | Administrador |
| 2 | Barbero |
| 3 | Cliente |
