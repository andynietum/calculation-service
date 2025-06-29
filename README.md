# Calculation Service

##  Descripción del Proyecto
### Solución Provista
Este servicio expone una API REST que permite realizar el cálculo de una operación entre dos números, aplicando un porcentaje adicional que se obtiene desde un servicio externo. Por otra parte se registra auditoría de los requests que llegan a este servicio almacenando esta información en una base de datos. Este servicio además expone un endpoint para consultar dicha auditoría de forma paginada.
### Diseño y Justificaciones Técnicas
Se propone el diseño de este servicio utilizando una arquitectura en capas, intentando desacoplar al máximo la dependencia entre las mismas. La persistencia y las consultas que involucren accesos a la BDD se realizan a través de la integración con Spring Data y JPA provistas por Spring Boot.<BR><BR>
Con respecto a lo que refiere a validación de parametría en endpoints utilizamos el módulo de Validations también provisto por Spring Boot.
<BR><BR>
Además, se implementa un mecanismo de resiliencia con ***Resilience4j*** que incluye:
- Retry automático si falla el servicio externo.
- Rate limiting para controlar la cantidad de peticiones al endpoint.

Para el mecanismo de caching del valor del porcentaje se decidió utilizar ***Redis*** para tener ese valor disponible para varias réplicas del servicio.<BR><BR>

Por otra parte se detectó que la auditoría de los requests es un cross cutting concern y se decidió implementarla con ***AOP*** para evitar duplicación de código en los diferentes controllers del servicio. A su vez para no afectar el tiempo de respuesta del servicio principal durante el registro de auditoría el método del servicio se anota utilizando ***@Async*** para que esta ejecución se realice en un thread separado.<BR><BR>

Se decidió documentar la API utilizando la integración de ***OpenAPI 3.0*** para SpringBoot

---

## Instrucciones para ejecutar el servicio y la base de datos localmente

### 1. Clonar el repositorio
```bash
git clone https://github.com/andynietum/calculation-service.git

cd calculation-service
```
### 2. Buildear el Servicio y Levantar el entorno local
#### 2.1 Buildear el servicio
Para hacer el build es necesario tener instalado **Gradle 9+**. Desde una consola, y parados en el directorio raiz del proyecto ejecutar el siguiente comando:
```bash
gradle clean build
```
#### 2.2 Levantar el entorno local 
Para poder correr el servicio en un entorno local es necesario tener instalado **Docker** y **Docker Compose**. Desde una consola, y parados en el directorio raiz del proyecto ejecutar el siguiente comando:
```bash
docker-compose up --build
```
Esto iniciará:
 - El servicio Spring Boot (**calculation-service**) en el puerto **8080**.
 - Una base de datos PostgreSQL en el puerto **5432**.
 - Un servidor Redis en el puerto **6379**.
### 3. Verificar el Servicio
Para verificar la correcta salud del servicio, entrar en un navegador en:
```bash
http://localhost:8080/actuator/health
```
O desde una consulta directamente con el siguiente comando:
```
curl "http://localhost:8080/actuator/health"
```
### 4. Ejecución y Documentación de la API
La interacción con la API se puede realizar directamente desde la página de swagger provista por el servicio:
```bash
http://localhost:8080/swagger-ui.html
```

#### 4.1 Cálculo con porcentaje
- Endpoint: **GET /calculation**
- Parámetros:
  - num1 (int) – Primer número a sumar. (obligatorio)
  - num2 (int) – Segundo número a sumar. (obligatorio)

#### 4.2 Auditoría de requests
- Endpoint: **GET /audit**
- Parámetros:
  - page (int) – Página a consultar (por defecto 0).
  - size (int) – Tamaño de página (por defecto 10).

La documentación completa de la API está disponible en la página de Swagger provista por el servicio mencionada anteriormente

### 5. Tecnologías Utilizadas
- Java 21
- Spring Boot + Spring Boot Actuator
- Lombok
- Gradle
- Redis
- PostgreSQL
- Docker + Docker Compose
- Resilience4j
- SpringDoc OpenAPI (Swagger)