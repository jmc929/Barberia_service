.PHONY: dev build help run clean

help:
	@echo "📋 Comandos disponibles:"
	@echo ""
	@echo "  make dev        Compila y ejecuta el backend (como npm run dev)"
	@echo "  make run        Solo ejecuta (sin recompilar)"
	@echo "  make build      Solo compila"
	@echo "  make clean      Limpia archivos compilados"

dev:
	@echo "🔨 Compilando proyecto..."
	@cd backend && mvn clean package -DskipTests
	@echo ""
	@echo "✅ Compilación exitosa"
	@echo "🚀 Iniciando Barberia Backend en http://localhost:8080/api/"
	@echo ""
	@./run-dev.sh

run:
	@echo "🚀 Iniciando Barberia Backend en http://localhost:8080/api/"
	@echo ""
	@./run-dev.sh

build:
	@echo "🔨 Compilando proyecto..."
	@cd backend && mvn clean package -DskipTests
	@echo "✅ Build completado"

clean:
	@echo "🗑️  Limpiando archivos compilados..."
	@cd backend && mvn clean
	@echo "✅ Limpieza completada"
