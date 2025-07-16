package com.hotel.scheduler.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Utility class for handling JWT (JSON Web Token) operations in the application.
 * <p>
 * Provides methods for generating, parsing, validating, and extracting claims from JWT tokens.
 * Used for stateless authentication in conjunction with Spring Security.
 * <p>
 * <b>Usage:</b> Injected as a Spring bean wherever JWT operations are needed (e.g., filters, controllers).
 * <p>
 * <b>Configuration:</b> Uses properties <code>app.jwt.secret</code> and <code>app.jwt.expiration</code> from application configuration.
 */
@Component
@Slf4j
public class JwtUtils {

    /**
     * Secret key for signing JWT tokens (configured in application properties).
     */
    @Value("${app.jwt.secret}")
    private String jwtSecret;

    /**
     * JWT token expiration time in milliseconds (configured in application properties).
     */
    @Value("${app.jwt.expiration}")
    private int jwtExpirationMs;

    /**
     * Returns the signing key used for JWT operations.
     *
     * @return the signing {@link Key}
     */
    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    /**
     * Generates a JWT token for the given user principal.
     *
     * @param userPrincipal the authenticated user
     * @return the generated JWT token as a String
     */
    public String generateJwtToken(UserDetails userPrincipal) {
        return generateTokenFromUsername(userPrincipal.getUsername());
    }

    /**
     * Generates a JWT token for the given username.
     *
     * @param username the username to include in the token subject
     * @return the generated JWT token as a String
     */
    public String generateTokenFromUsername(String username) {
        Map<String, Object> claims = new HashMap<>();
        return createToken(claims, username);
    }

    /**
     * Creates a JWT token with the specified claims and subject.
     *
     * @param claims  additional claims to include in the token
     * @param subject the subject (typically the username)
     * @return the generated JWT token as a String
     */
    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Extracts the username (subject) from the given JWT token.
     *
     * @param token the JWT token
     * @return the username (subject) from the token
     */
    public String getUserNameFromJwtToken(String token) {
        return getClaimFromToken(token, Claims::getSubject);
    }

    /**
     * Extracts the expiration date from the given JWT token.
     *
     * @param token the JWT token
     * @return the expiration {@link Date} of the token
     */
    public Date getExpirationDateFromToken(String token) {
        return getClaimFromToken(token, Claims::getExpiration);
    }

    /**
     * Extracts a specific claim from the JWT token using the provided resolver function.
     *
     * @param token          the JWT token
     * @param claimsResolver function to extract the desired claim
     * @param <T>            the type of the claim
     * @return the extracted claim value
     */
    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Retrieves all claims from the JWT token.
     *
     * @param token the JWT token
     * @return the {@link Claims} object containing all claims
     */
    private Claims getAllClaimsFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * Checks if the JWT token is expired.
     *
     * @param token the JWT token
     * @return true if the token is expired, false otherwise
     */
    private Boolean isTokenExpired(String token) {
        final Date expiration = getExpirationDateFromToken(token);
        return expiration.before(new Date());
    }

    /**
     * Validates the JWT token for structure, signature, and expiration.
     *
     * @param authToken the JWT token to validate
     * @return true if the token is valid, false otherwise
     */
    public Boolean validateJwtToken(String authToken) {
        try {
            Jwts.parserBuilder().setSigningKey(getSigningKey()).build().parseClaimsJws(authToken);
            return true;
        } catch (SecurityException e) {
            log.error("Invalid JWT signature: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            log.error("Invalid JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            log.error("JWT token is expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.error("JWT token is unsupported: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("JWT claims string is empty: {}", e.getMessage());
        }
        return false;
    }

    /**
     * Validates the JWT token and checks if it matches the given user details.
     *
     * @param token       the JWT token
     * @param userDetails the user details to match
     * @return true if the token is valid and matches the user, false otherwise
     */
    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = getUserNameFromJwtToken(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    /**
     * Validates the JWT token for structure and signature only (does not check user details).
     *
     * @param token the JWT token
     * @return true if the token is valid, false otherwise
     */
    public Boolean validateTokenOnly(String token) {
        return validateJwtToken(token);
    }
}
