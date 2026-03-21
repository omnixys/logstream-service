package com.omnixys.logstream.kafka;

public final class KafkaHeaderKeys {

    public static final String TRACE_ID = "x-trace-id";
    public static final String EVENT_NAME = "x-event-name";
    public static final String EVENT_TYPE = "x-event-type";
    public static final String EVENT_VERSION = "x-event-version";
    public static final String SERVICE = "x-service";

    private KafkaHeaderKeys() {}
}
