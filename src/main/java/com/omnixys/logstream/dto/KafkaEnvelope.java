package com.omnixys.logstream.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.time.Instant;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public record KafkaEnvelope(
        String eventId,
        String eventType,
        String eventName,
        String eventVersion,
        String service,
        String operation,
        Instant timestamp,
        LogPayload payload,
        Map<String, String> metadata
) {
}