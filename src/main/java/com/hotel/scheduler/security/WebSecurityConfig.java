package com.hotel.scheduler.security;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Spring Security configuration for the Hotel Employee Scheduler application.
 * <p>
 * Configures authentication, authorization, JWT filter, CORS, and session management.
 * <ul>
 *   <li>Enables method-level security with {@code @PreAuthorize}.</li>
 *   <li>Defines security filter chain for REST endpoints and static resources.</li>
 *   <li>Configures JWT-based stateless authentication and custom entry point for unauthorized access.</li>
 *   <li>Allows CORS for frontend integration.</li>
 * </ul>
 * <b>Usage:</b> Registered as a Spring {@code @Configuration} and applied automatically.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@EnableScheduling
@RequiredArgsConstructor
public class WebSecurityConfig {

    /**
     * Service for loading user details from the database.
     */
    private final UserDetailsService userDetailsService;

    /**
     * Password encoder bean for hashing and verifying passwords.
     */
    private final PasswordEncoder passwordEncoder;

    /**
     * Custom entry point for handling unauthorized access (returns JSON error).
     */
    private final AuthEntryPointJwt unauthorizedHandler;

    /**
     * Allowed origins for CORS, loaded from application configuration.
     */
    @Value("${app.cors.allowed-origins}")
    private String allowedOrigins;

    /**
     * JWT authentication filter bean for processing JWT tokens in requests.
     *
     * @return the {@link AuthTokenFilter} bean
     */
    @Bean
    public AuthTokenFilter authenticationJwtTokenFilter() {
        return new AuthTokenFilter(jwtUtils(), userDetailsService, securityEventService(), abuseDetectionService());
    }
    
    /**
     * Security event service bean for enhanced logging.
     *
     * @return the {@link SecurityEventService} bean
     */
    @Bean
    public SecurityEventService securityEventService() {
        return new SecurityEventService();
    }
    
    /**
     * Abuse detection service bean for threat detection.
     *
     * @return the {@link AbuseDetectionService} bean
     */
    @Bean
    public AbuseDetectionService abuseDetectionService() {
        return new AbuseDetectionService(securityEventService(), rateLimitingService());
    }
    
    /**
     * Rate limiting service bean for request throttling.
     *
     * @return the {@link RateLimitingService} bean
     */
    @Bean
    public RateLimitingService rateLimitingService() {
        return new RateLimitingService();
    }

    /**
     * JWT utility bean for token operations (signing, validation, etc).
     *
     * @return the {@link JwtUtils} bean
     */
    @Bean
    public JwtUtils jwtUtils() {
        return new JwtUtils();
    }

    /**
     * Authentication provider bean using DAO and password encoder.
     *
     * @return the {@link DaoAuthenticationProvider} bean
     */
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder);
        return authProvider;
    }

    /**
     * Authentication manager bean for processing authentication requests.
     *
     * @param authConfig the authentication configuration
     * @return the {@link AuthenticationManager} bean
     * @throws Exception if authentication manager cannot be created
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    /**
     * Configures the main security filter chain for the application.
     * <ul>
     *   <li>Enables CORS and disables CSRF (stateless API).</li>
     *   <li>Sets custom entry point for unauthorized access.</li>
     *   <li>Configures stateless session management.</li>
     *   <li>Defines endpoint access rules by role.</li>
     *   <li>Adds JWT authentication filter before username/password filter.</li>
     *   <li>Disables frame options for H2 console support.</li>
     * </ul>
     *
     * @param http the {@link HttpSecurity} builder
     * @param enhancedSecurityFilter the enhanced security filter for rate limiting and abuse detection
     * @return the built {@link SecurityFilterChain}
     * @throws Exception if configuration fails
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, EnhancedSecurityFilter enhancedSecurityFilter) throws Exception {
        http.cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(AbstractHttpConfigurer::disable)
            .exceptionHandling(exception -> exception.authenticationEntryPoint(unauthorizedHandler))
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Public pages - accessible to everyone
                .requestMatchers("/frontend/login.html").permitAll()
                .requestMatchers("/frontend/register.html").permitAll()
                .requestMatchers("/frontend/register-admin.html").permitAll()
                .requestMatchers("/frontend/super-admin-login.html").permitAll()
                .requestMatchers("/frontend/change-password.html").permitAll()
                
                // Shared static resources - accessible to authenticated users
                .requestMatchers("/frontend/style.css").permitAll()
                .requestMatchers("/frontend/*.css").permitAll()
                .requestMatchers("/frontend/*.js").permitAll()
                .requestMatchers("/frontend/header.html").authenticated()
                
                // Employee pages - require EMPLOYEE role
                .requestMatchers("/frontend/employee-dashboard.html").hasAnyRole("EMPLOYEE", "MANAGER", "ADMIN", "SUPER_ADMIN")
                .requestMatchers("/frontend/employee-notifications.html").hasAnyRole("EMPLOYEE", "MANAGER", "ADMIN", "SUPER_ADMIN")
                .requestMatchers("/frontend/employee-profile.html").hasAnyRole("EMPLOYEE", "MANAGER", "ADMIN", "SUPER_ADMIN")
                .requestMatchers("/frontend/employee-shifts.html").hasAnyRole("EMPLOYEE", "MANAGER", "ADMIN", "SUPER_ADMIN")
                .requestMatchers("/frontend/employee-trades.html").hasAnyRole("EMPLOYEE", "MANAGER", "ADMIN", "SUPER_ADMIN")
                
                // Admin pages - require ADMIN role
                .requestMatchers("/frontend/dashboard.html").hasAnyRole("ADMIN", "SUPER_ADMIN")
                .requestMatchers("/frontend/departments.html").hasAnyRole("ADMIN", "SUPER_ADMIN")
                .requestMatchers("/frontend/employees.html").hasAnyRole("ADMIN", "SUPER_ADMIN")
                .requestMatchers("/frontend/notifications.html").hasAnyRole("ADMIN", "SUPER_ADMIN")
                .requestMatchers("/frontend/reports.html").hasAnyRole("ADMIN", "SUPER_ADMIN")
                .requestMatchers("/frontend/shifts.html").hasAnyRole("ADMIN", "SUPER_ADMIN")
                .requestMatchers("/frontend/trades.html").hasAnyRole("ADMIN", "SUPER_ADMIN")
                .requestMatchers("/frontend/waitlist.html").hasAnyRole("ADMIN", "SUPER_ADMIN")
                .requestMatchers("/frontend/admin-logs.html").hasAnyRole("ADMIN", "SUPER_ADMIN")
                
                // Super Admin pages - require SUPER_ADMIN role
                .requestMatchers("/frontend/super-admin-admins.html").hasRole("SUPER_ADMIN")
                .requestMatchers("/frontend/super-admin-billing.html").hasRole("SUPER_ADMIN")
                
                // AI features - accessible to authenticated users
                .requestMatchers("/frontend/ai-bubble.html").authenticated()
                
                // Remove the old permissive rules
                // .requestMatchers("/frontend/**").permitAll()
                // .requestMatchers("/frontend/*").permitAll()
                
                .requestMatchers("/", "/index.html", "/favicon.ico").permitAll()
                .requestMatchers("/super-admin-login.html").permitAll()
                .requestMatchers("/*.html", "/*.css", "/*.js", "/*.png", "/*.jpg", "/*.ico").permitAll()
                .requestMatchers("/login").permitAll()
                .requestMatchers("/api/super-admin/auth/login").permitAll()
                .requestMatchers("/api/auth/login").permitAll()
                .requestMatchers("/api/auth/register").permitAll()
                .requestMatchers("/api/auth/validate-invitation").permitAll()
                .requestMatchers("/api/auth/change-password").permitAll()
                .requestMatchers("/api/auth/check-email").permitAll()
                .requestMatchers("/api/auth/validate").authenticated()
                .requestMatchers("/api/public/**").permitAll()
                // .requestMatchers("/api/debug/**").permitAll()  // REMOVED: Debug endpoint was a security vulnerability
                .requestMatchers("/h2-console/**").permitAll()
                .requestMatchers("/api/logs/**").hasRole("ADMIN")
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .requestMatchers("/api/manager/**").hasAnyRole("MANAGER", "ADMIN")
                .requestMatchers("/api/auth/request-admin-access").permitAll()
                .requestMatchers("/api/auth/verify-admin-code").permitAll()
                .requestMatchers("/api/auth/register-admin").permitAll()
                .anyRequest().authenticated()
            );

        // Fix H2 console frame issue
        http.headers(headers -> headers.frameOptions(frameOptions -> frameOptions.disable()));

        http.authenticationProvider(authenticationProvider());
        
        // Add enhanced security filter first (rate limiting, abuse detection)
        http.addFilterBefore(enhancedSecurityFilter, UsernamePasswordAuthenticationFilter.class);
        
        // Add custom redirect/rewrite filter before JWT filter
        http.addFilterBefore(new OncePerRequestFilter() {
            @Override
            protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
                    throws ServletException, IOException {
                String path = request.getRequestURI();
                // Redirect root '/' and '/login' to '/frontend/login'
                if ("/".equals(path) || "/login".equals(path)) {
                    response.setStatus(HttpServletResponse.SC_MOVED_PERMANENTLY);
                    response.setHeader("Location", "/frontend/login");
                    return;
                }
                // Rewrite extensionless frontend URLs to .html if file exists
                if (path.matches("^/frontend/[a-zA-Z0-9_-]+$")) {
                    String htmlPath = path + ".html";
                    ClassPathResource resource = new ClassPathResource("static" + htmlPath);
                    if (resource.exists()) {
                        request.getRequestDispatcher(htmlPath).forward(request, response);
                        return;
                    }
                }
                filterChain.doFilter(request, response);
            }
        }, UsernamePasswordAuthenticationFilter.class);
        http.addFilterBefore(authenticationJwtTokenFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Configures CORS to allow requests from configured origins with credentials support.
     * Origins are loaded from the app.cors.allowed-origins property.
     *
     * @return the {@link CorsConfigurationSource} bean
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // Parse comma-separated allowed origins from configuration
        List<String> origins = Arrays.asList(allowedOrigins.split(","));
        configuration.setAllowedOrigins(origins);
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L); // Cache preflight for 1 hour

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
