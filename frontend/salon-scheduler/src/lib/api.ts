// Capa simple de cliente HTTP para la API de la Barbería.
// La URL base se toma de VITE_API_BASE_URL (sin barra final).
// El backend devuelve { success, message, data } envuelto en ApiResponse.

export interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T | null;
}

export class ApiError extends Error {
  status: number;
  constructor(message: string, status: number) {
    super(message);
    this.status = status;
  }
}

/**
 * El backend usa `server.servlet.context-path=/api` y los controladores están en
 * `/api/v1/...`, así que cada URL es: `{origen}/api` + `/api/v1/...`.
 * Si `VITE_API_BASE_URL` es solo el host (p. ej. `http://localhost:8080`), hay que
 * añadir `/api`; si no, las peticiones van a `/api/v1/...` y Spring Security devuelve 403.
 */
function resolveApiBaseUrl(raw: string | undefined): string {
  const fallback = "http://localhost:8080/api";
  if (!raw?.trim()) return fallback;
  let base = raw.trim().replace(/\/$/, "");
  if (!base.endsWith("/api")) base = `${base}/api`;
  return base;
}

const BASE_URL = resolveApiBaseUrl(import.meta.env.VITE_API_BASE_URL as string | undefined);

const TOKEN_KEY = "barberia_token";
const USER_KEY = "barberia_user";

export const tokenStorage = {
  get: () => sessionStorage.getItem(TOKEN_KEY),
  set: (t: string) => sessionStorage.setItem(TOKEN_KEY, t),
  clear: () => {
    sessionStorage.removeItem(TOKEN_KEY);
    sessionStorage.removeItem(USER_KEY);
  },
};

export const userStorage = {
  get: <T = unknown>(): T | null => {
    const raw = sessionStorage.getItem(USER_KEY);
    return raw ? (JSON.parse(raw) as T) : null;
  },
  set: (u: unknown) => sessionStorage.setItem(USER_KEY, JSON.stringify(u)),
};

export async function apiFetch<T>(
  path: string,
  options: RequestInit = {}
): Promise<T> {
  const token = tokenStorage.get();
  const headers: Record<string, string> = {
    "Content-Type": "application/json",
    ...(options.headers as Record<string, string>),
  };
  if (token) headers.Authorization = `Bearer ${token}`;

  let res: Response;
  try {
    res = await fetch(`${BASE_URL}${path}`, { ...options, headers });
  } catch (e) {
    throw new ApiError("No se pudo conectar con el servidor", 0);
  }

  let body: ApiResponse<T> | null = null;
  try {
    body = (await res.json()) as ApiResponse<T>;
  } catch {
    // sin cuerpo JSON
  }

  if (!res.ok || !body || body.success === false) {
    const msg = body?.message || `Error ${res.status}`;
    throw new ApiError(msg, res.status);
  }
  return (body.data as T) ?? (undefined as unknown as T);
}

// ===== DTOs (tipos basados en el OpenAPI) =====
export interface UsuarioDTO {
  numeroDocumento: string;
  numeroCelular: string;
  email: string;
  nombrePersona: string;
  idEstado: number;
  idRol: number;
  fechaRegistro?: string;
}
export interface LoginResponseDTO {
  token: string;
  type: string;
  usuario: UsuarioDTO;
}
export interface ServicioDTO {
  idServicio?: number;
  nombreServicio: string;
  descripcion: string;
  duracion: number;
  costo: number;
  idEstado?: number;
}
export interface LocalTime {
  hour: number;
  minute: number;
  second?: number;
  nano?: number;
}
export interface CitaDTO {
  noCita: number;
  numeroDocumentoCliente: string;
  numeroDocumentoPeluquero: string;
  idServicio: number;
  fechaCita: string;
  horaInicioCita: LocalTime;
  horaFinCita: LocalTime;
  idEstado: number;
  citaConfirmada?: boolean;
  fechaCreacion?: string;
}
export interface HorarioNegocioDTO {
  idHorarioNegocio: number;
  idDia: number;
  horaApertura: LocalTime;
  horaCierre: LocalTime;
  localAbierto: boolean;
}

// ===== Endpoints =====
export const api = {
  auth: {
    login: (email: string, contraseña: string) =>
      apiFetch<LoginResponseDTO>("/api/v1/auth/login", {
        method: "POST",
        body: JSON.stringify({ email, contraseña }),
      }),
  },
  personas: {
    registrar: (payload: {
      numeroDocumento: string;
      numeroCelular: string;
      email: string;
      nombrePersona: string;
      contrasena: string;
      confirmarContrasena: string;
    }) => {
      // El backend espera las claves con eñe (contraseña / confirmarContraseña).
      const body: Record<string, string> = {
        numeroDocumento: payload.numeroDocumento,
        numeroCelular: payload.numeroCelular,
        email: payload.email,
        nombrePersona: payload.nombrePersona,
      };
      body["contrase\u00f1a"] = payload.contrasena;
      body["confirmarContrase\u00f1a"] = payload.confirmarContrasena;
      return apiFetch<UsuarioDTO>("/api/v1/personas/registro", {
        method: "POST",
        body: JSON.stringify(body),
      });
    },
    barberos: () => apiFetch<UsuarioDTO[]>("/api/v1/personas/barberos"),
    todas: () => apiFetch<UsuarioDTO[]>("/api/v1/personas"),
  },
  servicios: {
    todos: () => apiFetch<ServicioDTO[]>("/api/v1/servicios"),
    porId: (id: number) => apiFetch<ServicioDTO>(`/api/v1/servicios/buscar/${id}`),
  },
  horarios: {
    todos: () => apiFetch<HorarioNegocioDTO[]>("/api/v1/horarios"),
  },
  citas: {
    todas: () => apiFetch<CitaDTO[]>("/api/v1/citas"),
    porId: (no: number) => apiFetch<CitaDTO>(`/api/v1/citas/buscar/${no}`),
  },
  agendamiento: {
    disponibilidad: (payload: {
      numeroDocumentoPeluquero: string;
      idServicio: number;
      fechaCita: string;
      horaInicioCita: string;
      horaFinCita: string;
    }) =>
      apiFetch<{
        disponible: boolean;
        mensaje: string;
      }>("/api/v1/agendamiento/disponibilidad", {
        method: "POST",
        body: JSON.stringify(payload),
      }),
    agendar: (payload: {
      numeroDocumentoPeluquero: string;
      idServicio: number;
      fechaCita: string;
      horaInicioCita: string;
      horaFinCita: string;
    }) =>
      apiFetch<CitaDTO>("/api/v1/agendamiento/agendar", {
        method: "POST",
        body: JSON.stringify(payload),
      }),
    cancelar: (no: number) =>
      apiFetch<CitaDTO>(`/api/v1/agendamiento/cancelar/${no}`, { method: "POST" }),
    confirmar: (no: number) =>
      apiFetch<CitaDTO>(`/api/v1/agendamiento/confirmar/${no}`, { method: "PUT" }),
    reprogramar: (
      no: number,
      payload: { fechaCita: string; horaInicioCita: string; horaFinCita: string }
    ) =>
      apiFetch<CitaDTO>(`/api/v1/agendamiento/reprogramar/${no}`, {
        method: "PUT",
        body: JSON.stringify(payload),
      }),
  },
};

export function formatLocalTime(t?: LocalTime | null): string {
  if (!t) return "--:--";
  const hh = String(t.hour ?? 0).padStart(2, "0");
  const mm = String(t.minute ?? 0).padStart(2, "0");
  return `${hh}:${mm}`;
}

export const DIAS_SEMANA = [
  "Lunes",
  "Martes",
  "Miércoles",
  "Jueves",
  "Viernes",
  "Sábado",
  "Domingo",
];