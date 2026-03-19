FROM eclipse-temurin:21-jdk AS build
WORKDIR /app

COPY .mvn/ .mvn/
COPY mvnw mvnw
COPY mvnw.cmd mvnw.cmd
COPY pom.xml pom.xml
COPY src/ src/

RUN ./mvnw clean package -Dmaven.test.skip=true

FROM eclipse-temurin:21-jre
WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

COPY .mvn/ .mvn/
COPY mvnw mvnw
COPY mvnw.cmd mvnw.cmd
COPY pom.xml pom.xml
COPY src/ src/

RUN chmod +x mvnw
RUN ./mvnw clean package -Dmaven.test.skip=true

FROM eclipse-temurin:21-jre
WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]
