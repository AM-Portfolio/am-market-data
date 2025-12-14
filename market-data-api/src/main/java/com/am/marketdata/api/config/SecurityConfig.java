package com.am.marketdata.api.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
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
import jakarta.annotation.PostConstruct;

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

        if (securityEnabled) {
            log.info("Mode: PRODUCTION (Security ENABLED)");
            log.info("✓ JWT Validation is ACTIVE");
            log.info("✓ Protected endpoints: /api/v1/**");
        } else {
            log.info("Mode: DEVELOPMENT (Security DISABLED)");
            log.warn("⚠️  WARNING: All endpoints are PUBLIC");
        }
        log.info("=".repeat(80));
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        log.info("Configuring Security Filter Chain...");

        http
                .csrf(csrf -> {
                    csrf.disable();
                    log.debug("CSRF protection disabled (stateless API)");
                })
                .sessionManagement(session -> {
                    session.sessionCreationPolicy(SessionCreationPolicy.STATELESS);
                    log.debug("Session management: STATELESS");
                });

        if (securityEnabled) {
            log.info("Applying PRODUCTION security configuration (JWT enabled)");

            http.authorizeHttpRequests(auth -> {
                auth
                        .requestMatchers(
                                "/actuator/health",
                                "/actuator/health/live",
                                "/actuator/health/ready",
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/v3/api-docs.yaml")
                        .permitAll()
                        .requestMatchers("/api/v1/**").authenticated()
                        .anyRequest().denyAll();
            })
                    .oauth2ResourceServer(oauth2 -> {
                        oauth2.jwt(jwt -> jwt.decoder(jwtDecoder()));
                    });

        } else {
            log.warn("Applying DEVELOPMENT security configuration (ALL ENDPOINTS PUBLIC)");
            http.authorizeHttpRequests(auth -> {
                auth.anyRequest().permitAll();
            });
        }

        http.httpBasic(basic -> basic.disable())
                .formLogin(form -> form.disable());

        return http.build();
    }

    @Bean
    @ConditionalOnProperty(name = "security.enabled", havingValue = "true", matchIfMissing = true)
    public JwtDecoder jwtDecoder() {
        SecretKey key = new SecretKeySpec(jwtSecret.getBytes(), "HmacSHA256");
        return NimbusJwtDecoder.withSecretKey(key).build();
    }
}
