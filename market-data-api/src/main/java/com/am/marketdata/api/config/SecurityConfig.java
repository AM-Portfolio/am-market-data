package com.am.marketdata.api.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

        private static final Logger log = LoggerFactory.getLogger(SecurityConfig.class);

        @Value("${security.enabled:true}")
        private boolean securityEnabled;

        @Value("${jwt.secret}")
        private String jwtSecret;

        @PostConstruct
        public void init() {
                log.info("=".repeat(80));
                log.info("SECURITY CONFIGURATION INITIALIZED");
                log.info("=".repeat(80));
                log.info("Security Mode: {}", securityEnabled ? "ENABLED (Production)" : "DISABLED (Development)");
                log.info("JWT Secret Length: {} characters", jwtSecret.length());
                log.info("JWT Secret (masked): {}...{}", 
                        jwtSecret.substring(0, Math.min(4, jwtSecret.length())),
                        jwtSecret.length() > 4 ? jwtSecret.substring(jwtSecret.length() - 4) : "");
                
                if (!securityEnabled) {
                        log.warn("⚠️  WARNING: Security is DISABLED! All endpoints are PUBLIC.");
                        log.warn("⚠️  This should ONLY be used in development/testing environments.");
                        log.warn("⚠️  DO NOT use in production!");
                } else {
                        log.info("✓ Security ENABLED - JWT validation active");
                        log.info("✓ Protected endpoints: /api/v1/**");
                        log.info("✓ Public endpoints: /actuator/health, /swagger-ui/**, /v3/api-docs/**");
                }
                log.info("=".repeat(80));
        }

        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
                log.info("Configuring Security Filter Chain...");
                
                http
                                // Disable CSRF (stateless REST API with JWT)
                                .csrf(csrf -> {
                                        csrf.disable();
                                        log.debug("CSRF protection disabled (stateless API)");
                                })

                                // Stateless session management (no cookies, JWT-based)
                                .sessionManagement(session -> {
                                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS);
                                        log.debug("Session management: STATELESS");
                                });

                if (securityEnabled) {
                        // ✅ PRODUCTION MODE: JWT Authentication Enabled
                        log.info("Applying PRODUCTION security configuration (JWT enabled)");
                        
                        http.authorizeHttpRequests(auth -> {
                                log.debug("Configuring authorization rules...");
                                auth
                                        // ✅ PUBLIC ENDPOINTS - No authentication required
                                        .requestMatchers(
                                                        "/actuator/health", // Docker health check
                                                        "/actuator/health/live", // Kubernetes liveness probe
                                                        "/actuator/health/ready", // Kubernetes readiness probe
                                                        "/swagger-ui/**", // Swagger API documentation
                                                        "/v3/api-docs/**", // OpenAPI specification
                                                        "/v3/api-docs.yaml" // OpenAPI YAML
                                        ).permitAll()

                                        // ✅ PROTECTED ENDPOINTS - Require valid JWT
                                        .requestMatchers(
                                                        "/api/v1/**" // All API operations
                                        ).authenticated()

                                        // ❌ Deny all other endpoints (fail secure)
                                        .anyRequest().denyAll();
                                
                                log.debug("Authorization rules configured: Public endpoints allowed, /api/v1/** requires JWT");
                        })

                        // ✅ ZERO TRUST: Enforce JWT Validation
                        .oauth2ResourceServer(oauth2 -> {
                                oauth2.jwt(jwt -> jwt.decoder(jwtDecoder()));
                                log.debug("OAuth2 Resource Server configured with JWT decoder");
                        });
                        
                        log.info("✓ Security configuration applied successfully (JWT ENABLED)");

                } else {
                        // ⚠️ DEVELOPMENT MODE: Security Disabled (All endpoints public)
                        log.warn("Applying DEVELOPMENT security configuration (ALL ENDPOINTS PUBLIC)");
                        
                        http.authorizeHttpRequests(auth -> {
                                auth.anyRequest().permitAll();
                                log.warn("All requests permitted without authentication");
                        });
                        
                        log.warn("✓ Security configuration applied (JWT DISABLED - DEV MODE)");
                }

                // Disable HTTP Basic authentication (not needed, using JWT)
                http.httpBasic(basic -> {
                        basic.disable();
                        log.debug("HTTP Basic authentication disabled");
                })
                // Disable form login (API Gateway handles authentication)
                .formLogin(form -> {
                        form.disable();
                        log.debug("Form login disabled");
                });

                log.info("Security Filter Chain configuration completed");
                return http.build();
        }

        @Bean
        @ConditionalOnProperty(name = "security.enabled", havingValue = "true", matchIfMissing = true)
        public JwtDecoder jwtDecoder() {
                // Use HS256 (Symmetric Key) to match Auth Service
                SecretKey key = new SecretKeySpec(jwtSecret.getBytes(), "HmacSHA256");
                return NimbusJwtDecoder.withSecretKey(key).build();
        }
}
