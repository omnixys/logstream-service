package com.omnixys.logstream.kafka;

import java.time.Instant;
import java.util.Map;

public record EventEnvelope<T>(
        String eventId,
        String eventType,
        String eventName,
        String eventVersion,
        String service,
        String operation,
        Instant timestamp,
        T payload,
        Map<String, String> metadata
) {
}