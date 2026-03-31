#!/bin/bash

# Script para compilar y ejecutar Barberia Backend
# Uso: ./run.sh

cd backend

echo "🔨 Compilando proyecto..."
mvn clean package -DskipTests

if [ $? -eq 0 ]; then
    echo ""
    echo "✅ Compilación exitosa"
    echo "🚀 Iniciando Barberia Backend..."
    echo ""
    java -jar target/barberia-service-1.0.0.jar
else
    echo "❌ Error en la compilación"
    exit 1
fi
