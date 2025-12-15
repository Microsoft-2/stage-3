# ETAPA 1: Compilación
FROM maven:3.8.7-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src ./src
# Compilamos (esto generará el jar y la carpeta lib/)
RUN mvn package -DskipTests

# ETAPA 2: Ejecución
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

# 1. Copiamos las dependencias (Capa pesada que Docker cacheará)
COPY --from=build /app/target/lib /app/lib

# 2. Copiamos TU aplicación (Capa ligera que cambia mucho)
# Ajusta el nombre si tu pom.xml genera otro (ej: search-engine-1.0-SNAPSHOT.jar)
COPY --from=build /app/target/search-engine-1.0-SNAPSHOT.jar app.jar

# Directorio de datos
RUN mkdir -p /app/data
ENV BROKER_URL=tcp://activemq:61616
ENV DATALAKE_PATH=/app/data

# IMPORTANTE: Usamos -cp (Classpath) apuntando al jar Y a la carpeta lib
# El "*" incluye todos los jars de esa carpeta.
# No ponemos la clase Main aquí, Docker Compose nos la pasará en 'command'.
ENTRYPOINT ["java", "-cp", "app.jar:lib/*"]