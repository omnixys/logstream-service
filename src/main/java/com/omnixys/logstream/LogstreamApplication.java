package com.omnixys.logstream;

import com.omnixys.logstream.config.AppProperties;
import com.omnixys.logstream.util.Env;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.graphql.autoconfigure.GraphQlSourceBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.graphql.data.federation.FederationSchemaFactory;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

import static com.omnixys.logstream.util.Banner.TEXT;

@SpringBootApplication(proxyBeanMethods = false)
@EnableConfigurationProperties({AppProperties.class})
@EnableWebSecurity
@EnableMethodSecurity
@EnableAsync
@EnableKafka
public class LogstreamApplication {

    public static void main(String[] args) {
        new Env();
        final var app = new SpringApplication(LogstreamApplication.class);
        app.setBanner((_, _, out) -> out.println(TEXT));
        app.run(args);
    }

    @Bean
    public GraphQlSourceBuilderCustomizer customizer(FederationSchemaFactory factory) {
        return builder -> builder.schemaFactory(factory::createGraphQLSchema);
    }

    @Bean
    FederationSchemaFactory federationSchemaFactory() {
        return new FederationSchemaFactory();
    }

}
