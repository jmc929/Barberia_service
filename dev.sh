#!/bin/bash

# Script rápido para ejecutar Barberia Backend (sin recompilar)
# Uso: ./dev.sh

cd backend

if [ ! -f "target/barberia-service-1.0.0.jar" ]; then
    echo "⚠️  JAR no encontrado. Compilando primero..."
    mvn clean package -DskipTests
    if [ $? -ne 0 ]; then
        echo "❌ Error en la compilación"
        exit 1
    fi
fi

echo "🚀 Iniciando Barberia Backend en http://localhost:8080/api/"
echo ""
java -jar target/barberia-service-1.0.0.jar
