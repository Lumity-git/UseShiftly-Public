package com.hotel.scheduler.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
     * JWT authentication filter bean for processing JWT tokens in requests.
     *
     * @return the {@link AuthTokenFilter} bean
     */
    @Bean
    public AuthTokenFilter authenticationJwtTokenFilter() {
        return new AuthTokenFilter(jwtUtils(), userDetailsService);
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
     * @return the built {@link SecurityFilterChain}
     * @throws Exception if configuration fails
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(AbstractHttpConfigurer::disable)
            .exceptionHandling(exception -> exception.authenticationEntryPoint(unauthorizedHandler))
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/frontend/**").permitAll()
                .requestMatchers("/frontend/*").permitAll()
                .requestMatchers("/", "/index.html", "/favicon.ico").permitAll()
                .requestMatchers("/super-admin-login.html").permitAll()
                .requestMatchers("/*.html", "/*.css", "/*.js", "/*.png", "/*.jpg", "/*.ico").permitAll()
                .requestMatchers("/api/super-admin/auth/login").permitAll()
                .requestMatchers("/api/auth/login").permitAll()
                .requestMatchers("/api/auth/register").permitAll()
                .requestMatchers("/api/auth/validate-invitation").permitAll()
                .requestMatchers("/api/auth/change-password").permitAll()
                .requestMatchers("/api/auth/validate").authenticated()
                .requestMatchers("/api/public/**").permitAll()
                .requestMatchers("/h2-console/**").permitAll()
                .requestMatchers("/api/logs/**").hasRole("ADMIN")
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .requestMatchers("/api/manager/**").hasAnyRole("MANAGER", "ADMIN")
                .anyRequest().authenticated()
            );

        // Fix H2 console frame issue
        http.headers(headers -> headers.frameOptions(frameOptions -> frameOptions.disable()));

        http.authenticationProvider(authenticationProvider());
        http.addFilterBefore(authenticationJwtTokenFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Configures CORS to allow requests from any origin and standard HTTP methods.
     *
     * @return the {@link CorsConfigurationSource} bean
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of("*"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
