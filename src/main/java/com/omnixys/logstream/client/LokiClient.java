package com.omnixys.logstream.client;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class LokiClient {

    private final WebClient webClient;

    @Value("${logstream.loki.url}")
    private String lokiUrl;

    public void push(Object streams) {
        webClient.post()
                .uri(lokiUrl)
                .bodyValue(Map.of("streams", streams))
                .retrieve()
                .bodyToMono(Void.class)
                .block(); // blocking for reliability (can be async later)
    }
}