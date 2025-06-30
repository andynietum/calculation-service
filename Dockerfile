FROM eclipse-temurin:21-jdk as builder

WORKDIR /app

# Copiar wrapper y archivos de configuración primero (para aprovechar cache)
COPY gradlew .
COPY gradle/ gradle/
COPY build.gradle settings.gradle ./

# Ejecutar un build vacío para que solo se cachee la descarga de dependencias
RUN ./gradlew dependencies --no-daemon || return 0

# Copiar el código fuente del proyecto
COPY src/ src/

# Build de la app
RUN ./gradlew build -x test --no-daemon

# ----------- Imagen final -------------
FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=builder /app/build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]