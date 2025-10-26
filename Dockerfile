# syntax=docker/dockerfile:1

########### Etapa 1: Build ###########
FROM gradle:8.10-jdk21 AS build
WORKDIR /app

# Copiamos todo el proyecto (usa .dockerignore para excluir lo innecesario)
COPY . .

# Si existe el wrapper, úsalo; si no, usa el gradle del contenedor
RUN if [ -f ./gradlew ]; then \
      chmod +x ./gradlew && ./gradlew --no-daemon clean bootJar; \
    else \
      gradle --no-daemon clean bootJar; \
    fi

########### Etapa 2: Runtime ###########
FROM bellsoft/liberica-runtime-container:jre-21-cds-slim-glibc
WORKDIR /app

# Copiamos el JAR generado (Spring Boot produce un único fat jar)
COPY --from=build /app/build/libs/*.jar /app/app.jar

ENV TZ=America/Lima
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]
