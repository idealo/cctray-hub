FROM eclipse-temurin:17-jre
USER 1001
COPY build/libs/cctray-hub.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
EXPOSE 8080

HEALTHCHECK --interval=1s --timeout=1s --retries=10 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

org.opencontainers.image.source = "https://github.com/idealo/cctray-hub"
