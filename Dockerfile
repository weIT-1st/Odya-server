FROM ghcr.io/weit-1st/odya-gradle-cache AS build
COPY . /app
WORKDIR /app

RUN ./gradlew assemble

FROM eclipse-temurin:17.0.7_7-jdk AS run

ENV TZ=Asia/Seoul

## spring 패키지 복사
COPY --from=build \
  /app/build/libs/*.jar \
  /app/app.jar

EXPOSE 8080

ENTRYPOINT java \
-Dspring.profiles.active=${APP_PHASE} \
-jar /app/app.jar
