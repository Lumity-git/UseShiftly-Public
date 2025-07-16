package com.hotel.scheduler.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Authentication entry point for handling unauthorized access attempts in the application.
 * <p>
 * This component is triggered whenever an unauthenticated user tries to access a protected REST endpoint.
 * It returns a JSON response with HTTP 401 status and a descriptive error message, instead of the default HTML error page.
 * <p>
 * Used by Spring Security as the entry point for authentication exceptions (e.g., invalid or missing JWT token).
 *
 * <p><b>Usage:</b> Registered as a Spring bean and configured in the security configuration.
 *
 * <p><b>Related:</b> Works in conjunction with {@link com.hotel.scheduler.security.JwtUtils} and AuthController for JWT-based authentication.
 */
@Component
@Slf4j
public class AuthEntryPointJwt implements AuthenticationEntryPoint {

    /**
     * Handles unauthorized access attempts by sending a JSON response with error details.
     *
     * @param request       the HttpServletRequest
     * @param response      the HttpServletResponse
     * @param authException the exception thrown due to authentication failure
     * @throws IOException      if an input or output error occurs
     * @throws ServletException if a servlet error occurs
     *
     * <p>Response body example:
     * <pre>{
     *   "status": 401,
     *   "error": "Unauthorized",
     *   "message": "Full authentication is required to access this resource",
     *   "path": "/api/protected-endpoint"
     * }</pre>
     */
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException, ServletException {
        log.error("Unauthorized error: {}", authException.getMessage());

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        final Map<String, Object> body = new HashMap<>();
        body.put("status", HttpServletResponse.SC_UNAUTHORIZED);
        body.put("error", "Unauthorized");
        body.put("message", authException.getMessage());
        body.put("path", request.getServletPath());

        final ObjectMapper mapper = new ObjectMapper();
        mapper.writeValue(response.getOutputStream(), body);
    }
}
