package com.omnixys.logstream.kafka;

import io.opentelemetry.api.trace.SpanContext;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.apache.kafka.common.header.internals.RecordHeaders;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

/**
 * Hilfsklasse zum Erstellen standardisierter Kafka-Headers.
 * Kapselt Metadaten und unterstützt optional OpenTelemetry-Trace-Kontext.
 *
 * @author Caleb
 * @since 20.04.2025
 * @version 2.0
 */
@Service
@RequiredArgsConstructor
public class KafkaUtilService {

    @Value("${app.name}")
    private String serviceName;

    @Value("${app.version}")
    private String version;
    /**
     * Erstellt Standard-Kafka-Header mit optionalem OpenTelemetry-Tracing.
     *
     * @param topic         Ziel-Topic
     * @param operation     Bezeichnung der Aktion
     * @param spanContext   Aktueller OpenTelemetry-SpanContext (optional)
     * @return Kafka-RecordHeaders mit Metadaten
     */
    public RecordHeaders buildStandardHeaders(String topic, String operation, SpanContext spanContext) {
        RecordHeaders headers = new RecordHeaders();

        // ✨ Meta-Header
        headers.add(header("x-service", serviceName));
        headers.add(header("x-events-name", topic));
        headers.add(header("x-events-version", version));
        headers.add(header("x-events-type", operation));

        // ✨ W3C TraceContext Header für Tempo
        if (spanContext != null && spanContext.isValid()) {
            String traceFlags = spanContext.isSampled() ? "01" : "00";
            String traceparent = String.format("00-%s-%s-%s", spanContext.getTraceId(), spanContext.getSpanId(), traceFlags);
            headers.add(header("traceparent", traceparent));
        }

        return headers;
    }

    private static Header header(String key, String value) {
        return new RecordHeader(key, value.getBytes(StandardCharsets.UTF_8));
    }
}
