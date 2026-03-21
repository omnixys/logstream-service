/**
 * Dieses Paket enthält Sicherheitsklassen für die Authentifizierung und Benutzerverwaltung.
 * <p>
 * Die enthaltenen Klassen unterstützen JWT-Authentifizierung, Benutzerverwaltung mit Keycloak
 * und benutzerdefinierte Sicherheitsmechanismen.
 * </p>
 *
 * <ul>
 *   <li>{@link com.omnixys.customer.security.CustomAuthenticationToken} – Repräsentiert ein benutzerdefiniertes Authentifizierungstoken.</li>
 *   <li>{@link com.omnixys.customer.security.CustomUserDetails} – Enthält benutzerdefinierte Benutzerinformationen.</li>
 *   <li>{@link com.omnixys.customer.security.JwtToUserDetailsConverter} – Konvertiert ein JWT in `UserDetails`.</li>
 *   <li>{@link com.omnixys.customer.security.KeycloakRepository} – Kommuniziert mit dem Keycloak-Server.</li>
 * </ul>
 *
 * <p>
 * Enthaltene Sub-Packages:
 * <ul>
 *   <li>{@link com.omnixys.customer.security.dto} – Enthält DTOs für Sicherheit und Authentifizierung.</li>
 *   <li>{@link com.omnixys.customer.security.enums} – Definiert Enum-Klassen für Rollen, Token und Scopes.</li>
 *   <li>{@link com.omnixys.customer.security.service} – Enthält Sicherheitsservices für JWT-Verarbeitung und Keycloak-Integration.</li>
 * </ul>
 * </p>
 *
 * @since 14.02.2025
 * @author <a href="mailto:caleb-script@outlook.de">Caleb Gyamfi</a>
 * @version 1.0
 */
package com.omnixys.logstream.security;