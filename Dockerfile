FROM eclipse-temurin:21.0.1_12-jre
USER 1001
COPY build/libs/cctray-hub.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
EXPOSE 8080
EXPOSE 8081
