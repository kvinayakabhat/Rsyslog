package com.sclera.rule_engine.syslog.config;


import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.proc.ConfigurableJWTProcessor;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;
import com.nimbusds.jwt.proc.JWTProcessor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;


@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class WebSecurityConfig {



    // creates a JwtDecoder bean for JWT processing
    @Bean
    public JwtDecoder jwtDecoder(JWTProcessor<SecurityContext> jwtProcessor, OAuth2TokenValidator<Jwt> jwtValidator) {
        NimbusJwtDecoder decoder = new NimbusJwtDecoder(jwtProcessor);
        // Create a validator that combines default validators with the custom validator provided
        OAuth2TokenValidator<Jwt> validator = new DelegatingOAuth2TokenValidator<>(
                JwtValidators.createDefault(), jwtValidator);
        // Set the validator for the decoder
        decoder.setJwtValidator(validator);
        return decoder;
    }

    // Creates a bean for TenantJWSKeySelector
    @Bean
    public TenantJWSKeySelector tenantJWSKeySelector() {
        return new TenantJWSKeySelector();
    }

    // Creates a bean for JWTProcessor
    @Bean
    public JWTProcessor<SecurityContext> jwtProcessor() {
        ConfigurableJWTProcessor<SecurityContext> jwtProcessor = new DefaultJWTProcessor<>();
        // Set the JWTClaimsSetAwareJWSKeySelector for the JWTProcessor
        jwtProcessor.setJWTClaimsSetAwareJWSKeySelector(tenantJWSKeySelector());
        return jwtProcessor;
    }

    // Creates a bean for TenantJwtIssuerValidator
    @Bean
    public TenantJwtIssuerValidator tenantJwtIssuerValidator() {
        return new TenantJwtIssuerValidator();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf().ignoringRequestMatchers("/**");
        http
                .cors(Customizer.withDefaults());
        // Request mapping configuration
        http
                .authorizeHttpRequests((authorization) -> authorization
                        .anyRequest().permitAll()
                );
        // Session configuration
        http
                .sessionManagement((session) -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                );

        http
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt
                                .decoder(jwtDecoder(jwtProcessor(), tenantJwtIssuerValidator()))));

        // To add security headers
        http
                .headers(header -> header
                        .referrerPolicy(referrer -> referrer
                                .policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.SAME_ORIGIN)
                        )
                        .permissionsPolicy(permissions -> permissions
                                .policy("camera=(self), microphone=(self), geolocation=(self)")
                        ).and()
                        .contentSecurityPolicy(csp -> csp
                                .policyDirectives("default-src 'self'; img-src *; object-src *")
                        )
                        .httpStrictTransportSecurity(hsts -> hsts
                                .includeSubDomains(true)
                                .maxAgeInSeconds(63072000)
                        )
                        .addHeaderWriter((request, response) -> {
                            response.setHeader("Strict-Transport-Security", "max-age=63072000 ; includeSubDomains");
                        })
                );

        return http.build();
    }

}
