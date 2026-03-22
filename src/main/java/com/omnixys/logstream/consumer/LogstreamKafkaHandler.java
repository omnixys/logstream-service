package com.omnixys.logstream.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.omnixys.kafka.annotation.KafkaEvent;
import com.omnixys.kafka.model.KafkaEnvelope;
import com.omnixys.logger.model.LogDTO;
import com.omnixys.logstream.client.LokiClient;
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import io.opentelemetry.context.propagation.TextMapGetter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Kafka handler for logstream ingestion.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class LogstreamKafkaHandler {

    private final LokiClient lokiClient;
    private final ObjectMapper objectMapper;

    private static final TextMapGetter<Map<String, String>> GETTER =
            new TextMapGetter<>() {
                @Override
                public Iterable<String> keys(Map<String, String> carrier) {
                    return carrier.keySet();
                }

                @Override
                public String get(Map<String, String> carrier, String key) {
                    return carrier.get(key);
                }
            };

    @KafkaEvent(topic = "logstream.input")
    public void handle(KafkaEnvelope<LogDTO> envelope, Map<String, String> headers) {

        Tracer tracer = GlobalOpenTelemetry.getTracer("omnixys.kafka");

        // 🔥 CONTEXT EXTRACT
        Context parentContext = GlobalOpenTelemetry.get()
                .getPropagators()
                .getTextMapPropagator()
                .extract(Context.current(), headers, GETTER);

        Span span = tracer.spanBuilder("kafka logstream consume")
                .setSpanKind(SpanKind.CONSUMER)
                .setParent(parentContext)
                .startSpan();


        LogDTO dto = objectMapper.convertValue(
                envelope.payload(),
                LogDTO.class
        );


        try (Scope scope = span.makeCurrent()) {

            Map<String, String> labels = new HashMap<>();
            labels.put("service", safe(dto.service()));
            labels.put("level", safe(dto.level().toString()));

            String logLine = objectMapper.writeValueAsString(dto);

            String timestamp = resolveTimestamp(dto.timestamp().toString());

            Map<String, Object> stream = Map.of(
                    "stream", labels,
                    "values", new String[][]{{timestamp, logLine}}
            );

            lokiClient.push(new Object[]{stream});

        } catch (Exception e) {
            span.recordException(e);
            span.setAttribute("error", true);
            log.error("Logstream processing failed", e);
        }
    }

    private String resolveTimestamp(String timestamp) {
        try {
            if (timestamp != null) {
                return String.valueOf(Instant.parse(timestamp).toEpochMilli() * 1_000_000);
            }
        } catch (Exception ignored) {}

        return String.valueOf(System.currentTimeMillis() * 1_000_000);
    }

    private String safe(String value) {
        return value != null ? value : "unknown";
    }
}