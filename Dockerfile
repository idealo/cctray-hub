FROM eclipse-temurin:17-jre
USER 1001
COPY build/libs/cctray-hub.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
EXPOSE 8080
EXPOSE 8081

HEALTHCHECK --interval=1s --timeout=1s --retries=10 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8081/actuator/health || exit 1
