FROM eclipse-temurin:21-jdk-alpine AS builder

RUN apk add --no-cache bash curl libstdc++ gcompat

RUN curl -fL https://github.com/coursier/launchers/raw/master/cs-x86_64-pc-linux.gz \
    | gzip -d > /usr/local/bin/cs \
    && chmod +x /usr/local/bin/cs \
    && cs setup --yes --apps sbt

ENV PATH="/root/.local/share/coursier/bin:$PATH"

WORKDIR /app

COPY project/ project/
COPY build.sbt .
RUN sbt update && sbt compile

COPY src/ src/
RUN sbt assembly

FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY --from=builder /app/target/scala-*/brapi-*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]