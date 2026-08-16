FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /workspace

COPY pom.xml ./
COPY common-logging/pom.xml common-logging/pom.xml
COPY gym-core-service/pom.xml gym-core-service/pom.xml
COPY workload-service/pom.xml workload-service/pom.xml
COPY discovery-service/pom.xml discovery-service/pom.xml
COPY gateway-service/pom.xml gateway-service/pom.xml
COPY gym-automation-tests/pom.xml gym-automation-tests/pom.xml
RUN --mount=type=cache,id=m2-workload,target=/root/.m2 \
    mvn -q -pl workload-service -am dependency:go-offline

COPY common-logging/src common-logging/src
COPY workload-service/src workload-service/src

RUN --mount=type=cache,id=m2-workload,target=/root/.m2 \
    mvn -q -pl workload-service -am package -DskipTests && \
    java -Djarmode=tools -jar workload-service/target/*.jar extract --layers --destination extracted && \
    mv extracted/application/*.jar extracted/application/application.jar

FROM eclipse-temurin:21-jre-alpine
RUN addgroup -S -g 1000 app && \
    adduser -S -u 1000 -G app app && \
    mkdir -p /application/logs && \
    chown -R app:app /application
WORKDIR /application

COPY --chown=app:app --from=build /workspace/extracted/dependencies/ ./
COPY --chown=app:app --from=build /workspace/extracted/spring-boot-loader/ ./
COPY --chown=app:app --from=build /workspace/extracted/snapshot-dependencies/ ./
COPY --chown=app:app --from=build /workspace/extracted/application/ ./
USER app

RUN java -XX:ArchiveClassesAtExit=application.jsa \
    -Dspring.context.exit=onRefresh \
    -Deureka.client.enabled=false \
    -jar application.jar

EXPOSE 8082
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75 -XX:+UseContainerSupport -XX:SharedArchiveFile=/application/application.jsa"
ENTRYPOINT ["java", "-jar", "application.jar"]
