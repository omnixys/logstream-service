package com.omnixys.logstream.security;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Collection;

/**
 * Repräsentiert benutzerdefinierte Benutzerinformationen für die Authentifizierung.
 * <p>
 * Diese Klasse wird für OAuth2-Authentifizierung verwendet und enthält keine Passwortinformationen.
 * </p>
 *
 * @since 14.02.2025
 * @author <a href="mailto:caleb-script@outlook.de">Caleb Gyamfi</a>
 * @version 1.0
 */
@Getter
public class CustomUserDetails implements UserDetails {
    private final String username;
    private final Collection<? extends GrantedAuthority> authorities;
    private final Jwt jwt;

    /**
     * Erstellt eine Instanz von `CustomUserDetails`.
     *
     * @param username    Der Benutzername.
     * @param authorities Die zugewiesenen Berechtigungen.
     */
    public CustomUserDetails(String username, Collection<? extends GrantedAuthority> authorities, final Jwt jwt) {
        this.username = username;
        this.authorities = authorities;
        this.jwt = jwt;
    }

    @Override
    public String getPassword() {
        return null; // Da wir OAuth2 verwenden, gibt es kein Passwort
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    public String getToken() {
        return jwt.getTokenValue();
    }
}
