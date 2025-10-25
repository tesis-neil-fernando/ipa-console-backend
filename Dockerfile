FROM bellsoft/liberica-runtime-container:jre-21-cds-slim-glibc
WORKDIR /app
COPY build/libs/ipa-console-backend-1.0.0.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar"]
