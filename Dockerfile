FROM gradle:8.8.0-jdk17 AS builder
WORKDIR /usr/src

RUN --mount=type=cache,target=/root/.gradle mkdir -p /usr/src

COPY . .

RUN --mount=type=cache,target=/root/.gradle gradle clean build -x test

FROM bellsoft/liberica-openjdk-alpine:17
WORKDIR /app

ARG JAR_FILE=build/libs/*.jar
COPY --from=builder /usr/src/${JAR_FILE} app.jar

ARG PORT=8080
ENV PORT=${PORT}

EXPOSE ${PORT}

ENTRYPOINT ["java", "-jar", "/app/app.jar"]