package com.omnixys.logstream.dto;

import com.omnixys.logstream.kafka.TraceContextDTO;
import lombok.Data;

import java.util.Map;

public record LogPayload (
    String level,
    String message,
    Map<String, Object> metadata,
    String service,
    String operation,
    String timestamp,
    String topic,
    TraceContextDTO traceContext
){
}