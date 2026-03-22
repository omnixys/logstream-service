//package com.omnixys.logstream.kafka;
//
//import com.fasterxml.jackson.core.type.TypeReference;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.omnixys.logstream.client.LokiClient;
//import com.omnixys.logstream.dto.LogPayload;
//import io.opentelemetry.api.GlobalOpenTelemetry;
//import io.opentelemetry.api.trace.Span;
//import io.opentelemetry.api.trace.SpanKind;
//import io.opentelemetry.api.trace.Tracer;
//import io.opentelemetry.context.Context;
//import io.opentelemetry.context.Scope;
//import io.opentelemetry.context.propagation.TextMapGetter;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.apache.kafka.clients.consumer.ConsumerRecord;
//import org.apache.kafka.common.header.Header;
//import org.apache.kafka.common.header.Headers;
//import org.springframework.kafka.annotation.KafkaListener;
//import org.springframework.stereotype.Service;
//
//import java.nio.charset.StandardCharsets;
//import java.time.Instant;
//import java.util.HashMap;
//import java.util.Map;
//
//@Service
//@RequiredArgsConstructor
//@Slf4j
//public class KafkaConsumerService {
//
//    private static final TypeReference<EventEnvelope<LogPayload>> LOG_EVENT_TYPE =
//            new TypeReference<>() {};
//
//    private final ObjectMapper objectMapper;
//    private final LokiClient lokiClient;
//    private final Tracer tracer;
//
//    @KafkaListener(
//            topics = {
//                    "user.send.logstream",
//                    "event.send.logstream",
//                    "seat.send.logstream",
//                    "invitation.send.logstream",
//                    "ticket.send.logstream",
//                    "notification.send.logstream",
//                    "address.send.logstream",
//                    "authentication.send.logstream"
//            },
//            groupId = "${app.groupId}"
//    )
//    public void consumeLogEvent(byte[] payload, ConsumerRecord<String, byte[]> record) {
//        try {
//            processLogEvent(payload, record);
//        } catch (Exception e) {
//            log.error(
//                    "Kafka log processing failed for topic={} partition={} offset={} -> skipping message",
//                    record.topic(),
//                    record.partition(),
//                    record.offset(),
//                    e
//            );
//        }
//    }
//
//    @KafkaListener(topics = "test", groupId = "${app.groupId}")
//    public void test(byte[] payload, ConsumerRecord<String, byte[]> record) throws Exception {
//        Context extractedContext = GlobalOpenTelemetry.getPropagators()
//                .getTextMapPropagator()
//                .extract(Context.root(), record.headers(), HEADERS_GETTER);
//
//        Span span = tracer.spanBuilder("receive " + record.topic())
//                .setSpanKind(SpanKind.CONSUMER)
//                .setParent(extractedContext)
//                .setAttribute("messaging.system", "kafka")
//                .setAttribute("messaging.operation", "receive")
//                .setAttribute("messaging.destination.name", record.topic())
//                .setAttribute("messaging.kafka.partition", record.partition())
//                .setAttribute("messaging.kafka.message.offset", record.offset())
//                .startSpan();
//
//        Context processingContext = extractedContext.with(span);
//
//        try (Scope ignored = processingContext.makeCurrent()) {
//            logTraceConsumer(record, extractedContext, span);
//
//            TestEvent event = objectMapper.readValue(payload, TestEvent.class);
//            log.info(
//                    "Received test event on topic={} partition={} offset={} message={}",
//                    record.topic(),
//                    record.partition(),
//                    record.offset(),
//                    event.message()
//            );
//        } finally {
//            span.end();
//        }
//    }
//
//    private void processLogEvent(byte[] payload, ConsumerRecord<String, byte[]> record) throws Exception {
//        EventEnvelope<LogPayload> event = objectMapper.readValue(payload, LOG_EVENT_TYPE);
//        LogPayload dto = event.payload();
//
//        KafkaTraceHeaders traceHeaders = extractHeaders(record.headers());
//
//        Context extractedContext = GlobalOpenTelemetry.getPropagators()
//                .getTextMapPropagator()
//                .extract(Context.root(), record.headers(), HEADERS_GETTER);
//
//        Span span = tracer.spanBuilder("receive " + record.topic())
//                .setSpanKind(SpanKind.CONSUMER)
//                .setParent(extractedContext)
//                .setAttribute("messaging.system", "kafka")
//                .setAttribute("messaging.operation", "receive")
//                .setAttribute("messaging.destination.name", record.topic())
//                .setAttribute("messaging.kafka.partition", record.partition())
//                .setAttribute("messaging.kafka.message.offset", record.offset())
//                .startSpan();
//
//        Context processingContext = extractedContext.with(span);
//
//        try (Scope ignored = processingContext.makeCurrent()) {
//            logTraceConsumer(record, extractedContext, span);
//
//            Map<String, String> labels = buildLokiLabels(dto, traceHeaders);
//            String logLine = buildLogLine(dto, traceHeaders, record);
//            String timestampNs = resolveLokiTimestamp(dto);
//
//            Map<String, Object> stream = new HashMap<>();
//            stream.put("stream", labels);
//            stream.put("values", new String[][]{{timestampNs, logLine}});
//
//            lokiClient.push(new Object[]{stream});
//
//            span.setAttribute("log.service", safe(traceHeaders.service(), dto.service()));
//            span.setAttribute("log.operation", safe(traceHeaders.operation(), dto.operation()));
//            span.setAttribute("log.level", safe(dto.level()));
//            span.setAttribute("log.topic", safe(dto.topic()));
//        } catch (Exception e) {
//            span.recordException(e);
//            throw e;
//        } finally {
//            span.end();
//        }
//    }
//
//    private KafkaTraceHeaders extractHeaders(Headers headers) {
//        return new KafkaTraceHeaders(
//                getHeader(headers, "traceparent"),
//                getHeader(headers, "tracestate"),
//                getHeader(headers, "x-event-type"),
//                getHeader(headers, "x-service"),
//                getHeader(headers, "x-event-name"),
//                getHeader(headers, "x-event-version")
//        );
//    }
//
//    private void logTraceConsumer(ConsumerRecord<String, byte[]> record, Context extractedContext, Span span) throws Exception {
//        Span extractedSpan = Span.fromContext(extractedContext);
//
//        Map<String, Object> debugLog = new HashMap<>();
//        debugLog.put("topic", record.topic());
//        debugLog.put("partition", record.partition());
//        debugLog.put("offset", record.offset());
//        debugLog.put("receivedHeaders", extractCarrier(record.headers()));
//        debugLog.put("extractedContext", Map.of(
//                "traceId", extractedSpan.getSpanContext().getTraceId(),
//                "spanId", extractedSpan.getSpanContext().getSpanId(),
//                "isRemote", extractedSpan.getSpanContext().isRemote(),
//                "isValid", extractedSpan.getSpanContext().isValid()
//        ));
//        debugLog.put("spanContext", Map.of(
//                "traceId", span.getSpanContext().getTraceId(),
//                "spanId", span.getSpanContext().getSpanId(),
//                "isRemote", span.getSpanContext().isRemote(),
//                "isValid", span.getSpanContext().isValid()
//        ));
//
//        log.info("TRACE CONSUMER {}", objectMapper.writeValueAsString(debugLog));
//    }
//
//    private Map<String, String> buildLokiLabels(LogPayload dto, KafkaTraceHeaders headers) {
//        Map<String, String> labels = new HashMap<>();
//        labels.put("service", safe(headers.service(), dto.service()));
//        labels.put("level", safe(dto.level()));
//        labels.put("operation", safe(headers.operation(), dto.operation()));
//
//        Span currentSpan = Span.current();
//        if (currentSpan.getSpanContext().isValid()) {
//            labels.put("traceId", currentSpan.getSpanContext().getTraceId());
//        } else {
//            labels.put("traceId", dto.traceContext().traceId());
//        }
//
//
//        if (headers.eventName() != null) {
//            labels.put("eventName", headers.eventName());
//        }
//
//        if (headers.version() != null) {
//            labels.put("eventVersion", headers.version());
//        }
//
//        return labels;
//    }
//
//    private String buildLogLine(
//            LogPayload dto,
//            KafkaTraceHeaders headers,
//            ConsumerRecord<String, byte[]> record
//    ) throws Exception {
//        Span currentSpan = Span.current();
//
//        Map<String, Object> logBody = new HashMap<>();
//        logBody.put("timestamp", dto.timestamp());
//        logBody.put("level", dto.level());
//        logBody.put("service", dto.service());
//        logBody.put("operation", dto.operation());
//        logBody.put("message", dto.message());
//        logBody.put("topic", dto.topic());
//        logBody.put("metadata", dto.metadata() != null ? dto.metadata() : Map.of());
//
//        logBody.put("traceId", currentSpan.getSpanContext().isValid()
//                ? currentSpan.getSpanContext().getTraceId()
//                : null);
//        logBody.put("spanId", currentSpan.getSpanContext().isValid()
//                ? currentSpan.getSpanContext().getSpanId()
//                : null);
//
//        Span parentSpan = Span.fromContext(
//                GlobalOpenTelemetry.getPropagators()
//                        .getTextMapPropagator()
//                        .extract(Context.root(), record.headers(), HEADERS_GETTER)
//        );
//
//        logBody.put("parentSpanId", parentSpan.getSpanContext().isValid()
//                ? parentSpan.getSpanContext().getSpanId()
//                : null);
//
//        logBody.put("traceparent", headers.traceparent());
//        logBody.put("tracestate", headers.tracestate());
//        logBody.put("eventName", headers.eventName());
//        logBody.put("eventVersion", headers.version());
//
//        logBody.put("kafkaTopic", record.topic());
//        logBody.put("kafkaPartition", record.partition());
//        logBody.put("kafkaOffset", record.offset());
//        logBody.put("kafkaKey", record.key());
//
//        return objectMapper.writeValueAsString(logBody);
//    }
//
//    private String resolveLokiTimestamp(LogPayload dto) {
//        try {
//            if (dto.timestamp() != null) {
//                return String.valueOf(Instant.parse(dto.timestamp()).toEpochMilli() * 1_000_000);
//            }
//        } catch (Exception ignored) {
//            log.debug("Could not parse dto.timestamp={} -> falling back to current time", dto.timestamp());
//        }
//
//        return String.valueOf(System.currentTimeMillis() * 1_000_000);
//    }
//
//    private String getHeader(Headers headers, String key) {
//        Header header = headers.lastHeader(key);
//        return header != null ? new String(header.value(), StandardCharsets.UTF_8) : null;
//    }
//
//    private Map<String, String> extractCarrier(Headers headers) {
//        Map<String, String> carrier = new HashMap<>();
//
//        headers.forEach(h -> carrier.put(
//                h.key(),
//                new String(h.value(), StandardCharsets.UTF_8)
//        ));
//
//        return carrier;
//    }
//
//    private static final TextMapGetter<Headers> HEADERS_GETTER = new TextMapGetter<>() {
//        @Override
//        public Iterable<String> keys(Headers carrier) {
//            return carrier.toArray().length == 0
//                    ? java.util.List.of()
//                    : java.util.Arrays.stream(carrier.toArray()).map(Header::key).toList();
//        }
//
//        @Override
//        public String get(Headers carrier, String key) {
//            Header header = carrier.lastHeader(key);
//            return header == null ? null : new String(header.value(), StandardCharsets.UTF_8);
//        }
//    };
//
//    private String safe(String value) {
//        return value != null ? value : "unknown";
//    }
//
//    private String safe(String preferred, String fallback) {
//        return preferred != null ? preferred : safe(fallback);
//    }
//
//    private record KafkaTraceHeaders(
//            String traceparent,
//            String tracestate,
//            String operation,
//            String service,
//            String eventName,
//            String version
//    ) {
//    }
//}