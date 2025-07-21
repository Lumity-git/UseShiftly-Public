package com.hotel.scheduler.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT authentication filter for processing and validating JWT tokens in
 * incoming HTTP requests.
 * <p>
 * This filter intercepts all requests except static resources and public
 * authentication endpoints.
 * It extracts the JWT from the Authorization header, validates it, and sets the
 * authentication context for the user.
 * If the token is invalid or missing, it returns a 401 Unauthorized response
 * with a JSON error message.
 * <p>
 * <b>Usage:</b> Registered as a Spring bean and automatically applied by Spring
 * Security.
 * <p>
 * <b>Related:</b> Works with {@link JwtUtils} for token operations and
 * AuthEntryPointJwt for handling unauthorized errors.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AuthTokenFilter extends OncePerRequestFilter {

    /**
     * Utility for JWT operations (validation, parsing, etc.).
     */
    private final JwtUtils jwtUtils;

    /**
     * Loads user details for authentication context.
     */
    private final UserDetailsService userDetailsService;

    /**
     * Filters incoming requests to authenticate users based on JWT tokens.
     * <ul>
     * <li>Skips static resources and public auth endpoints.</li>
     * <li>Extracts and validates JWT from Authorization header.</li>
     * <li>Sets authentication in SecurityContext if valid.</li>
     * <li>Returns 401 JSON error if token is invalid or missing.</li>
     * </ul>
     *
     * @param request     the HTTP request
     * @param response    the HTTP response
     * @param filterChain the filter chain
     * @throws ServletException if a servlet error occurs
     * @throws IOException      if an I/O error occurs
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        String path = request.getRequestURI();
        // Skip filter for static resources, frontend files, public endpoints
        if (path.startsWith("/frontend/") ||
                path.startsWith("/static/") ||
                path.startsWith("/public/") ||
                path.startsWith("/api/public/") ||
                path.matches(".*\\.(html|css|js|png|jpg|ico)$") || 
                path.matches("^/frontend/[a-zA-Z0-9_-]+$") ||
                path.equals("/api/auth/login") ||
                path.equals("/api/auth/register") ||
                path.equals("/api/auth/validate-invitation")) {
            filterChain.doFilter(request, response);
            return;
        }
        try {
            String jwt = parseJwt(request);
            log.debug("AuthTokenFilter: Parsed JWT for path {}: {}", path, jwt);
            if (jwt != null) {
                boolean valid = jwtUtils.validateJwtToken(jwt);
                log.debug("AuthTokenFilter: JWT validation result for {}: {}", jwt, valid);
                if (valid) {
                    String username = jwtUtils.getUserNameFromJwtToken(jwt);
                    log.debug("AuthTokenFilter: JWT valid, username: {}", username);
                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                    log.debug("AuthTokenFilter: Loaded UserDetails for {}: roles={}", username,
                            userDetails.getAuthorities());
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            userDetails, null,
                            userDetails.getAuthorities());
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    log.debug("AuthTokenFilter: Authentication set for {}", username);
                    filterChain.doFilter(request, response);
                } else {
                    log.warn("AuthTokenFilter: Invalid JWT for path {}: {}", path, jwt);
                    if (!response.isCommitted()) {
                        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                        response.setContentType("application/json");
                        response.getWriter().write("{\"error\":\"Invalid or missing JWT token\"}");
                    }
                    return;
                }
            } else {
                log.warn("AuthTokenFilter: Missing JWT for path {}", path);
                if (!response.isCommitted()) {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType("application/json");
                    response.getWriter().write("{\"error\":\"Missing JWT token\"}");
                }
                return;
            }
        } catch (Exception e) {
            log.error("AuthTokenFilter: Cannot set user authentication for path {}: {}", path, e.getMessage(), e);
            if (!response.isCommitted()) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json");
                response.getWriter().write("{\"error\":\"Authentication error: " + e.getMessage() + "\"}");
            }
            return;
        }
    }

    /**
     * Extracts the JWT token from the Authorization header of the request.
     *
     * @param request the HTTP request
     * @return the JWT token if present and well-formed, otherwise null
     */
    private String parseJwt(HttpServletRequest request) {
        String headerAuth = request.getHeader("Authorization");

        if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
            return headerAuth.substring(7);
        }

        return null;
    }
}
