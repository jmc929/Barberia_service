## Despliegue de Barbería Service con Kubernetes

## Primera ejecución
1. Clonar el repositorio
git clone <url-del-repositorio>
cd barberia_service
2. Instalar Docker Desktop
Verificar que Docker Desktop esté iniciado.
3. Habilitar Kubernetes
En Docker Desktop:
Settings → Kubernetes → Enable Kubernetes
Aplicar los cambios y esperar a que Kubernetes inicie.
4. Verificar que Kubernetes esté activo
kubectl get nodes
Debe aparecer:
docker-desktop   Ready
5. Crear el archivo secret.yaml

Dentro de la carpeta k8s, crear un archivo llamado secret.yaml con la siguiente estructura:

apiVersion: v1
kind: Secret

metadata:
  name: barberia-secret

type: Opaque

stringData:
  SPRING_DATASOURCE_URL: <URL_SUPABASE>
  SPRING_DATASOURCE_USERNAME: <USUARIO>
  SPRING_DATASOURCE_PASSWORD: <PASSWORD>

Reemplazar los valores entre < > por las credenciales proporcionadas por el administrador.
6. Construir la imagen Docker
Ubicarse en la raíz del proyecto y ejecutar:
docker build -t barberia_service-barberia-api:latest .
7.  Desplegar en Kubernetes
kubectl apply -f k8s/
8.  Verificar el despliegue
Verificar Pods:
kubectl get pods
Verificar servicios:
kubectl get svc
9. Acceder a la aplicación
Abrir en en swagger:
http://localhost:30080/api/swagger-ui/index.html#/

10.  Ver logs
Obtener los Pods:
kubectl get pods
Consultar logs:
kubectl logs <nombre-del-pod>

11. Detener el despliegue
kubectl delete -f k8s/

## Actualización después de cambios en el backend
Cuando se realicen cambios en el código fuente de Spring Boot, es necesario reconstruir la imagen Docker y reiniciar el Deployment.
1. Descargar los cambios
git pull
2. Reconstruir la imagen Docker
docker build -t barberia_service-barberia-api:latest .
3. Reiniciar el Deployment
kubectl rollout restart deployment barberia-api
4. Verificar que los Pods se recrearon
kubectl get pods
Kubernetes eliminará los Pods antiguos y creará nuevos Pods utilizando la versión actualizada de la imagen.

