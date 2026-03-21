package com.omnixys.logstream.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Constants {
    private static final Logger LOGGER = LoggerFactory.getLogger(Constants.class);

    public static final String PROBLEM_PATH = "/problem";
    public static final String GRAPHQL_ENDPOINT = "/graphql";

    public static final String ID_PATTERN = "[\\da-f]{8}-[\\da-f]{4}-[\\da-f]{4}-[\\da-f]{4}-[\\da-f]{12}";
    public static final String VERSION_NUMBER_MISSING = "Versionsnummer fehlt";

    private static final String PAYMENT_SCHEMA_ENV = System.getenv("PAYMENT_SERVICE_SCHEMA");
    private static final String PAYMENT_HOST_ENV = System.getenv("PAYMENT_SERVICE_HOST");
    private static final String PAYMENT_PORT_ENV = System.getenv("PAYMENT_SERVICE_PORT");

    private static final String PAYMENT_SCHEMA = PAYMENT_SCHEMA_ENV == null ? "http" : PAYMENT_SCHEMA_ENV;
    private static final String PAYMENT_HOST = PAYMENT_HOST_ENV == null ? "localhost" : PAYMENT_HOST_ENV;

    // Verhindert, dass diese Klasse instanziiert wird
    private Constants() {
        throw new UnsupportedOperationException("Diese Klasse darf nicht instanziiert werden.");
    }
}
