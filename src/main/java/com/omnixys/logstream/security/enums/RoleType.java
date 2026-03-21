package com.omnixys.logstream.security.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

/**
 * Definiert verschiedene Benutzerrollen in der Anwendung.
 * <p>
 * Unterstützt JSON-Serialisierung und ermöglicht die Umwandlung von String-Werten in Enum-Werte.
 * </p>
 *
 * @since 14.02.2025
 * @author <a href="mailto:caleb-script@outlook.de">Caleb Gyamfi</a>
 * @version 1.0
 */
@RequiredArgsConstructor
public enum RoleType {

  ADMIN("Admin"),
  SUPREME("Supreme"),
  BASIC("Basic"),
  ELITE("Elite"),
  USER("User");

  public static final String ROLE_PREFIX = "ROLE_";

  private final String role;

  /**
   * Gibt die String-Repräsentation des Rollenwerts zurück.
   *
   * @return die String-Repräsentation der Rolle.
   */
  @JsonValue
  public String getRole() {
    return role;
  }

  /**
   * Erstellt einen Enum-Wert aus einem String-Wert.
   * Unterstützt JSON- und MongoDB-Datenverarbeitung.
   *
   * @param value der String-Wert der Rolle.
   * @return der entsprechende Enum-Wert.
   * @throws IllegalArgumentException wenn der Wert ungültig ist.
   */
  @JsonCreator
  public static RoleType fromValue(final String value) {
    return Arrays.stream(values())
        .filter(role -> role.role.equalsIgnoreCase(value))
        .findFirst()
        .orElseThrow(() -> new IllegalArgumentException(
            String.format("Ungültiger Wert '%s' für RoleType", value)
        ));
  }

  /**
   * Gibt die Rolle mit einem vorangestellten Präfix zurück.
   *
   * @return die Rolle mit dem Präfix 'ROLE_'.
   */
  public String getPrefixedRole() {
    return ROLE_PREFIX + role.toUpperCase();
  }
}
