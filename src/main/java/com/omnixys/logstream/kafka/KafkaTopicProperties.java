package com.omnixys.logstream.kafka;

import lombok.RequiredArgsConstructor;

/**
 * Zentrale Konfiguration der Kafka-Topic-Namen.
 * <p>
 * Die Namen folgen dem Schema: {@code <service>.<entities>.<events>}.
 * </p>
 *
 * @author Caleb
 * @since 20.04.2025
 */
@RequiredArgsConstructor
public final class KafkaTopicProperties {

    public static final String TOPIC_CREATE_USER_ADDRESSES ="address.createUserAddresses.authentication";
    public static final String TOPIC_TEST = "test";

}
