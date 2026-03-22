# syntax=docker/dockerfile:1.14.0

ARG JAVA_VERSION=25

# ========================
# BUILDER
# ========================
FROM azul/zulu-openjdk:${JAVA_VERSION}-jdk AS builder

WORKDIR /source

COPY build.gradle.kts gradle.properties settings.gradle.kts ./
COPY gradle ./gradle

RUN ./gradlew dependencies --no-daemon || true

COPY src ./src

RUN ./gradlew --no-daemon bootJar

RUN JAR_FILE=$(ls ./build/libs/*.jar | grep -v plain | head -n 1) && \
    java -Djarmode=tools -jar "$JAR_FILE" extract --layers --destination extracted

# ========================
# RUNTIME
# ========================
FROM azul/zulu-openjdk:${JAVA_VERSION}-jre AS final

WORKDIR /workspace

RUN apt-get update && \
    apt-get install --no-install-recommends --yes dumb-init wget && \
    rm -rf /var/lib/apt/lists/* && \
    groupadd --gid 1000 app && \
    useradd --uid 1000 --gid app --no-create-home app

USER app

COPY --from=builder --chown=app:app /source/extracted/dependencies/ ./
COPY --from=builder --chown=app:app /source/extracted/spring-boot-loader/ ./
COPY --from=builder --chown=app:app /source/extracted/snapshot-dependencies/ ./
COPY --from=builder --chown=app:app /source/extracted/application/ ./

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=5s --retries=3 \
    CMD wget -qO- http://localhost:8080/actuator/health | grep UP || exit 1

ENTRYPOINT [
  "dumb-init",
  "java",
  "--enable-preview",
  "-XX:+UseContainerSupport",
  "-XX:MaxRAMPercentage=75.0",
  "-XX:+ExitOnOutOfMemoryError",
  "org.springframework.boot.loader.launch.JarLauncher"
]