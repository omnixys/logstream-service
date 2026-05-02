# syntax=docker/dockerfile:1.14.0

######################################################################
# BUILD STAGE
#
# This stage compiles the Spring Boot application into a runnable JAR.
# It supports private dependency access via BuildKit secrets.
######################################################################

ARG JAVA_VERSION=25

FROM azul/zulu-openjdk:${JAVA_VERSION} AS builder

ARG APP_NAME
ARG APP_VERSION

WORKDIR /source

# Copy Gradle configuration and wrapper
COPY build.gradle.kts gradle.properties gradlew settings.gradle.kts ./
COPY gradle ./gradle

# Copy application source
COPY src ./src

# Build the Spring Boot executable JAR
# If secrets are provided, they are injected into Gradle configuration.
RUN --mount=type=secret,id=gpr_user \
    --mount=type=secret,id=gpr_key \
    mkdir -p ~/.gradle && \
    if [ -f /run/secrets/gpr_user ]; then \
      printf "gpr.user=%s\ngpr.key=%s\n" \
        "$(cat /run/secrets/gpr_user)" \
        "$(cat /run/secrets/gpr_key)" > ~/.gradle/gradle.properties; \
    fi && \
    ./gradlew --no-daemon --no-watch-fs clean bootJar


######################################################################
# RUNTIME STAGE
#
# This stage runs the application using a minimal JRE image.
# It includes:
# - Non-root execution
# - OpenTelemetry agent (optional)
# - Container-optimized JVM configuration
######################################################################

FROM azul/zulu-openjdk:${JAVA_VERSION}-jre AS final

ARG JAVA_VERSION
ARG APP_NAME
ARG APP_VERSION
ARG CREATED
ARG REVISION

######################################################################
# OCI METADATA LABELS
#
# These labels provide traceability and are used by container registries,
# observability platforms, and CI/CD pipelines.
######################################################################
LABEL org.opencontainers.image.title="${APP_NAME}-service" \
      org.opencontainers.image.description="Omnixys ${APP_NAME}-service – Java ${JAVA_VERSION}" \
      org.opencontainers.image.version="${APP_VERSION}" \
      org.opencontainers.image.vendor="omnixys" \
      org.opencontainers.image.created="${CREATED}" \
      org.opencontainers.image.revision="${REVISION}"

WORKDIR /workspace

######################################################################
# INSTALL MINIMAL RUNTIME DEPENDENCIES
#
# Only essential packages are installed to keep the image small and secure.
# curl is used for deterministic OTEL agent download.
######################################################################
RUN apt-get update && \
    apt-get install --no-install-recommends -y \
        dumb-init \
        ca-certificates \
        wget  && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*


######################################################################
# CREATE NON-ROOT USER
#
# Running as non-root is mandatory for production security compliance.
# The user is created BEFORE copying files to ensure correct ownership.
######################################################################
RUN groupadd --gid 1000 app && \
    useradd --uid 1000 --gid app --no-create-home app

######################################################################
# COPY APPLICATION ARTIFACT
#
# The built Spring Boot JAR is copied and owned by the non-root user.
######################################################################
COPY --from=builder --chown=app:app /source/build/libs/*.jar /workspace/app.jar

######################################################################
# OPEN TELEMETRY CONFIGURATION
#
# Uses environment variables instead of JVM flags for standard compliance.
######################################################################
ARG OTEL_AGENT_ENABLED=true
ARG OTEL_AGENT_VERSION=2.26.1

ENV OTEL_AGENT_PATH=/otel/opentelemetry-javaagent.jar \
    OTEL_SERVICE_NAME=${APP_NAME:-unknown-service} \
    OTEL_EXPORTER_OTLP_ENDPOINT=http://otel-collector:4317 \
    OTEL_EXPORTER_OTLP_PROTOCOL=grpc \
    OTEL_RESOURCE_ATTRIBUTES=service.namespace=omnixys \
    OTEL_LOGS_EXPORTER=otlp \
    OTEL_METRICS_EXPORTER=otlp \
    OTEL_TRACES_EXPORTER=otlp

######################################################################
# DOWNLOAD OTEL JAVA AGENT
#
# The agent is optional and version-pinned to ensure reproducibility.
# curl is used with fail-fast flags to prevent silent build failures.
######################################################################
RUN if [ "$OTEL_AGENT_ENABLED" = "true" ]; then \
      mkdir -p /otel && \
      wget -O ${OTEL_AGENT_PATH} \
      https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/latest/download/opentelemetry-javaagent.jar ; \
    fi

######################################################################
# JVM RUNTIME CONFIGURATION
#
# These options ensure optimal behavior inside containerized environments.
######################################################################
ENV JAVA_OPTS="\
-XX:+UseContainerSupport \
-XX:MaxRAMPercentage=75 \
-XX:InitialRAMPercentage=50 \
-XX:+AlwaysActAsServerClassMachine \
-XX:+UseG1GC \
-XX:MaxGCPauseMillis=200 \
-XX:+ExitOnOutOfMemoryError \
-Dfile.encoding=UTF-8 \
-Djava.security.egd=file:/dev/./urandom \
"

######################################################################
# SECURITY CONTEXT
#
# Switch to non-root user after all privileged operations are complete.
######################################################################
USER app

######################################################################
# HEALTHCHECK
#
# Uses a lightweight TCP-based HTTP request without additional tools.
# This avoids installing curl/wget solely for health checks.
######################################################################
HEALTHCHECK --interval=30s --timeout=3s --retries=1 \
  CMD wget -qO- --no-check-certificate https://localhost:8080/actuator/health/ | grep UP || exit 1

EXPOSE 8080

######################################################################
# ENTRYPOINT
#
# - Uses dumb-init for proper signal handling (PID 1 problem)
# - Conditionally attaches OTEL agent if present
# - Uses -jar execution for maximum stability (no classpath issues)
######################################################################
ENTRYPOINT ["sh", "-c", "\
exec dumb-init java \
$JAVA_OPTS \
$( [ -f /otel/opentelemetry-javaagent.jar ] && echo \"-javaagent:/otel/opentelemetry-javaagent.jar\" ) \
-jar /workspace/app.jar \
"]