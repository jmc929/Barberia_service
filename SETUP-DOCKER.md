# 🐳 Guía: Usar Barberia con Docker (Para Nuevos Miembros del Team)

¡Hola! Esta es la forma **más fácil y rápida** de ejecutar la aplicación sin instalar dependencias.

---

## ⏱️ Tiempo Total: ~3 minutos

---

## 📥 Paso 1: Clonar el Repositorio

```bash
git clone <URL_DEL_REPO>
cd Barberia
```

---

## 🔧 Paso 2: Configurar Credenciales de Base de Datos

Necesitas las credenciales de Supabase. **Pídele al lead del equipo** los valores para:
- `SPRING_DATASOURCE_URL`
- `SPRING_DATASOURCE_USERNAME` 
- `SPRING_DATASOURCE_PASSWORD`

Luego:

```bash
# Copiar el archivo de ejemplo
cp .env.example .env

# Abrir en tu editor favorito y completar los valores
# (VS Code, Sublime, nano, vim, etc.)
nano .env
```

Debe quedar así (SIN comillas):

```env
SPRING_DATASOURCE_URL=jdbc:postgresql://aws-1-us-east-1.pooler.supabase.com:5432/postgres
SPRING_DATASOURCE_USERNAME=postgres.wacfhjygmoagyegzupuz
SPRING_DATASOURCE_PASSWORD=TU_PASSWORD_AQUI
```

---

## 🐳 Paso 3: Ejecutar con Docker

### Opción A: Con Logs en la Terminal (Recomendado para empezar)

```bash
docker-compose up --build
```

Verás algo como esto:

```
barberia-backend  | ✅ CONEXIÓN A LA BASE DE DATOS ESTABLECIDA CORRECTAMENTE
barberia-backend  | ✅ URL: jdbc:postgresql://aws-1-us-east-1.pooler.supabase.com:5432/postgres
barberia-backend  | ✅ Usuario: postgres.wacfhjygmoagyegzupuz
barberia-backend  | ✅ Driver: PostgreSQL JDBC Driver
barberia-backend  | Started BarberiServiceApplication in 6.834 seconds
```

**¡Listo!** Para detener presiona: `Ctrl+C`

### Opción B: En Background (Segunda vez que ejecutas)

```bash
docker-compose up -d          # Inicia en background
docker-compose logs -f        # Ver logs en tiempo real
docker-compose down           # Detener
```

---

## 🌐 Acceder a la Aplicación

Una vez que esté corriendo:

| Recurso | URL |
|---------|-----|
| **API REST** | http://localhost:8080/api/ |
| **Documentación Swagger** | http://localhost:8080/api/swagger-ui.html |
| **Especificación OpenAPI** | http://localhost:8080/api/v3/api-docs |

---

## ✅ ¿Cómo verificar que funciona?

En otra terminal, corre:

```bash
curl http://localhost:8080/api/v3/api-docs | jq '.info.title'
```

Deberías ver:

```json
"Barberia Service API"
```

✅ **¡Funciona correctamente!**

---

## 🆘 Solucionar Problemas

### ❌ Error: "Cannot find Docker"
Instala [Docker Desktop](https://www.docker.com/products/docker-desktop)

### ❌ Error: "Connection refused" / "No response from database"
**Causa:** Las credenciales en `.env` son incorrectas.

**Solución:**
1. Verifica que `.env` tenga los valores correctos
2. Pídele al lead que verifique las credenciales
3. Para depurar:
   ```bash
   cat .env | grep SPRING_DATASOURCE
   ```

### ❌ Error: "Port 8080 already in use"
Otro proceso está usando el puerto.

**Solución:**
```bash
# Opción 1: Detener contenedores previos
docker-compose down

# Opción 2: Usar otro puerto (editar docker-compose.yml)
# Cambiar: ports: - "8080:8080"
# Por:    ports: - "8081:8080"
```

### ❌ En Kali Linux: "Connection attempt failed"
Docker en Kali tiene aislamiento de red.

**Solución:**
En lugar de Docker, ejecuta localmente (necesitas Java 21 + Maven):

```bash
make dev
# o
./run-dev.sh
```

---

## 📚 Más Información

- Ver [README.md](./README.md) para más detalles
- Ver [ARCHITECTURE.md](./backend/ARCHITECTURE.md) para entender el código
- Contactar al lead si hay dudas

---

## 🚀 Próximos Pasos

Una vez que la app esté corriendo:

1. **Revisar los endpoints disponibles** en Swagger UI
2. **Entender la estructura del código** en [ARCHITECTURE.md](./backend/ARCHITECTURE.md)
3. **Empezar a contribuir** - pídele al lead que te asigne tareas

¡Bienvenido al team! 🎉
