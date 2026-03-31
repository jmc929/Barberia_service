# 🚀 QUICKSTART - Barberia Team

Completa los pasos según tu rol y experiencia:

---

## 👥 Elige Tu Ruta

### 🐳 RUTA 1: Usar Docker (RECOMENDADO - Cualquier SO)

**Cuando:** No tienes Java/Maven instalado, quieres algo rápido y portable

**Pasos:**
1. Instala [Docker Desktop](https://www.docker.com/products/docker-desktop)
2. Lee [SETUP-DOCKER.md](./SETUP-DOCKER.md)
3. `docker-compose up --build`
4. Accede a http://localhost:8080/api/swagger-ui.html

**Tiempo:** ~3 minutos  
**Requisitos:** Docker Desktop (1.5 GB RAM mínimo)

---

### 💻 RUTA 2: Usar Make + Java Local (PARA DESARROLLADORES)

**Cuando:** Prefieres trabajar sin contenedores, desarrollo más rápido

**Pasos:**
1. Lee [SETUP-LOCAL.md](./SETUP-LOCAL.md)
2. Instala Java 21 + Maven 3.9
3. `make dev`
4. Accede a http://localhost:8080/api/swagger-ui.html

**Tiempo:** ~10 minutos (primer build)  
**Requisitos:** 500 MB RAM, Java 21, Maven 3.9

---

## 🛠️ Configuración Común (Ambas Rutas)

**Todos necesitan hacer esto:**

```bash
# 1. Clonar repo
git clone <URL>
cd Barberia

# 2. Crear archivo .env con credenciales
cp .env.example .env

# 3. Editar .env con los valores que te pasó el lead
nano .env
```

Valores a completar:
```env
SPRING_DATASOURCE_URL=jdbc:postgresql://aws-1-us-east-1.pooler.supabase.com:5432/postgres
SPRING_DATASOURCE_USERNAME=postgres.wacfhjy...    # ← Completa
SPRING_DATASOURCE_PASSWORD=Tu_password_aqui       # ← Completa
```

---

## ✅ Verificación Rápida

Una vez que la app esté corriendo, verifica en otra terminal:

```bash
curl -s http://localhost:8080/api/v3/api-docs | jq '.info.title'
```

Deberías ver:
```json
"Barberia Service API"
```

---

## 🌐 Accesos

| Recurso | URL |
|---------|-----|
| Swagger UI | http://localhost:8080/api/swagger-ui.html |
| OpenAPI Spec | http://localhost:8080/api/v3/api-docs |
| API Base | http://localhost:8080/api/ |

---

## 📚 Documentación

| Archivo | Para |
|---------|------|
| [SETUP-DOCKER.md](./SETUP-DOCKER.md) | Nuevos con Docker |
| [SETUP-LOCAL.md](./SETUP-LOCAL.md) | Desarrolladores sin Docker |
| [README.md](./README.md) | Visión general del proyecto |
| [backend/ARCHITECTURE.md](./backend/ARCHITECTURE.md) | Entender la estructura del código |

---

## 🤔 ¿Problemas?

### "No instalo Docker ni tengo Java instalado"
**Solución:** Pídele al lead que te prepare una máquina virtual o usa Docker Desktop en Windows/Mac

### "Ambas rutas funcionan en mi máquina, ¿cuál uso?"
**Respuesta:** Depende de tu preferencia:
- **Docker:** Mejor para portabilidad, menos setup
- **Local:** Más rápido, mejor debugging, más control

### "Docker está lento en mi máquina"
**Solución:** Usa SETUP-LOCAL en su lugar

---

## 🎯 Próximos Pasos (Después de que funcione)

1. ✅ Verificar que los endpoints funcionan en Swagger
2. 📖 Leer [ARCHITECTURE.md](./backend/ARCHITECTURE.md)
3. 👨‍💻 Comenzar a codificar
4. 🚀 Hacer tu primer PR

---

## 📞 Contactos

- **Lead técnico:** [Nombre/email]
- **Slack:** #barberia-dev
- **Bug tracker:** [URL del repositorio]

---

## 🎉 ¡Bienvenido al Team!

Cualquier duda, pregunta en Slack o contacta al lead técnico.

Happy coding! 💚
