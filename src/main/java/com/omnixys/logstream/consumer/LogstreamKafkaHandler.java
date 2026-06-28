package com.omnixys.logstream.consumer;

import tools.jackson.databind.ObjectMapper;
import com.omnixys.kafka.annotation.KafkaEvent;
import com.omnixys.kafka.model.KafkaEnvelope;
import com.omnixys.logger.model.LogDTO;
import com.omnixys.logstream.client.LokiClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class LogstreamKafkaHandler {

    private static final String DLQ_TOPIC = "logstream-dlq";

    private final LokiClient lokiClient;
    private final ObjectMapper objectMapper;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @KafkaEvent(topic = "logstream.input")
    public void handle(KafkaEnvelope<LogDTO> envelope, Map<String, String> headers) {

        log.info("CONSUMER Envelope {}", envelope);
        log.info("CONSUMER HEADER {}", headers);

        try {
            LogDTO dto = objectMapper.convertValue(
                    envelope.payload(),
                    LogDTO.class
            );

            Map<String, String> labels = new HashMap<>();
            labels.put("service", safe(dto.service()));
            labels.put("level", safe(dto.level().toString()));
            labels.put("traceparent", safe(headers.get("traceparent")));
            labels.put("traceId", safe(headers.get("x-meta-traceId")));
            labels.put("spanId", safe(headers.get("x-meta-spanId")));

            String logLine = objectMapper.writeValueAsString(dto);

            String timestamp = resolveTimestamp(dto.timestamp().toString());

            Map<String, Object> stream = Map.of(
                    "stream", labels,
                    "values", new String[][]{{timestamp, logLine}}
            );

            lokiClient.push(new Object[]{stream});

        } catch (Exception e) {
            log.error("Logstream processing failed", e);
            sendToDlq(envelope, headers, e);
        }
    }

    private void sendToDlq(KafkaEnvelope<LogDTO> envelope, Map<String, String> headers, Exception error) {
        try {
            String json = objectMapper.writeValueAsString(envelope);
            ProducerRecord<String, String> record = new ProducerRecord<>(DLQ_TOPIC, json);

            headers.forEach((key, value) ->
                    record.headers().add(key, value.getBytes(StandardCharsets.UTF_8))
            );
            record.headers().add("x-error-type", error.getClass().getName().getBytes(StandardCharsets.UTF_8));
            record.headers().add("x-error-message", (error.getMessage() != null ? error.getMessage() : "").getBytes(StandardCharsets.UTF_8));
            record.headers().add("x-original-topic", "logstream.input".getBytes(StandardCharsets.UTF_8));

            kafkaTemplate.send(record);
            log.info("Sent failed record to DLQ topic={}", DLQ_TOPIC);
        } catch (Exception dlqError) {
            log.error("Failed to send record to DLQ topic={}", DLQ_TOPIC, dlqError);
        }
    }

    private static String resolveTimestamp(String timestamp) {
        try {
            if (timestamp != null) {
                return String.valueOf(Instant.parse(timestamp).toEpochMilli() * 1_000_000);
            }
        } catch (Exception ignored) {}

        return String.valueOf(System.currentTimeMillis() * 1_000_000);
    }

    private static String safe(String value) {
        return value != null ? value : "unknown";
    }
}
