package com.omnixys.logstream.security;

import lombok.Getter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Collection;

/**
 * Repräsentiert ein benutzerdefiniertes Authentifizierungstoken.
 * <p>
 * Dieses Token speichert sowohl die JWT-Daten als auch die Benutzerinformationen
 * und wird zur Authentifizierung in Spring Security verwendet.
 * </p>
 *
 * @since 14.02.2025
 * @author <a href="mailto:caleb-script@outlook.de">Caleb Gyamfi</a>
 * @version 1.0
 */
@Getter
public class CustomAuthenticationToken extends AbstractAuthenticationToken {
    private final UserDetails userDetails;
    private final Jwt jwt;

    /**
     * Erstellt eine neue Instanz des `CustomAuthenticationToken`.
     *
     * @param userDetails Die Benutzerinformationen.
     * @param jwt         Das JWT-Token.
     * @param authorities Die zugewiesenen Berechtigungen.
     */
    public CustomAuthenticationToken(UserDetails userDetails, Jwt jwt, Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.userDetails = userDetails;
        this.jwt = jwt;
        setAuthenticated(true);
    }

    @Override
    public Object getCredentials() {
        return jwt.getTokenValue();
    }

    @Override
    public Object getPrincipal() {
        return userDetails;
    }
}