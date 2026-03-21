package com.omnixys.logstream.kafka;


public record TraceContextDTO(
        String traceId,
        String spanId,
        String parentSpanId,
        String sampled,

        boolean valid
) {}