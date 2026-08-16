FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /workspace

COPY pom.xml ./
COPY discovery-service/pom.xml discovery-service/pom.xml
RUN --mount=type=cache,id=m2-discovery,target=/root/.m2 \
    mvn -q -pl discovery-service dependency:go-offline

COPY discovery-service/src discovery-service/src
RUN --mount=type=cache,id=m2-discovery,target=/root/.m2 \
    mvn -q -pl discovery-service package -DskipTests

FROM eclipse-temurin:21-jre-alpine
RUN addgroup -S -g 1000 app && adduser -S -u 1000 -G app app
WORKDIR /app
COPY --chown=app:app --from=build /workspace/discovery-service/target/*.jar app.jar
USER app

EXPOSE 8761
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75 -XX:+UseContainerSupport"
ENTRYPOINT ["java", "-jar", "app.jar"]
