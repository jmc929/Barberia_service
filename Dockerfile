# Etapa de compilación
FROM maven:3.9.12-eclipse-temurin-21 AS builder

WORKDIR /app

# Copiar archivos del proyecto
COPY backend/pom.xml .
COPY backend/src ./src

# Compilar sin ejecutar tests
RUN mvn clean package -DskipTests

# Etapa de ejecución (imagen más ligera)
FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

# Copiar JAR compilado desde la etapa anterior
COPY --from=builder /app/target/barberia-service-1.0.0.jar .

# Exponer puerto
EXPOSE 8080

# Variables de entorno (pueden ser sobrescritas)
ENV SPRING_DATASOURCE_URL=jdbc:postgresql://db.wacfhjygmoagyegzupuz.supabase.co:5432/postgres
ENV SPRING_DATASOURCE_USERNAME=postgres
ENV SPRING_DATASOURCE_PASSWORD=fabricaudeA123*

# Ejecutar la aplicación
ENTRYPOINT ["java", "-jar", "barberia-service-1.0.0.jar"]
