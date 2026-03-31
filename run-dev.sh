#!/bin/bash
# Script para cargar .env y ejecutar la aplicación

set -a
source ./.env
set +a

cd backend
java -jar target/barberia-service-1.0.0.jar
