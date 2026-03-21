/**
 * Dieses Paket enthält Sicherheitsdienste für Authentifizierung und Benutzerverwaltung.
 * <p>
 * Die Services kommunizieren mit Keycloak zur Anmeldung, Benutzerverwaltung und Tokenverarbeitung.
 * </p>
 *
 * <ul>
 *   <li>{@link com.omnixys.customer.security.service.JwtUserDetailsService} – Erstellt Benutzerdetails aus JWTs.</li>
 *   <li>{@link com.omnixys.customer.security.service.KeycloakService} – Verwaltet Benutzeranmeldung und Registrierung über Keycloak.</li>
 *   <li>{@link com.omnixys.customer.security.service.JwtService} – Verarbeitet JWT-Token und extrahiert Benutzerinformationen.</li>
 * </ul>
 *
 * @since 14.02.2025
 * @author <a href="mailto:caleb-script@outlook.de">Caleb Gyamfi</a>
 * @version 1.0
 */
package com.omnixys.logstream.security.service;
