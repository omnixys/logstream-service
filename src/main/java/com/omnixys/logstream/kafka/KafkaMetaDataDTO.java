package com.omnixys.logstream.kafka;

import java.util.Optional;

public record KafkaMetaDataDTO(
        Optional<String> service,
        Optional<String> version,
        Optional<String> operation
) {
}
