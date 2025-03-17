package com.sclera.rule_engine.syslog.config;

import com.sclera.rule_engine.syslog.dto.TenantDTO;
import com.sclera.rule_engine.syslog.service.WebClientService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtIssuerValidator;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class TenantJwtIssuerValidator implements OAuth2TokenValidator<Jwt> {

    // Autowired service for making web requests
    @Autowired
    public WebClientService webClientService;

    // Map to cache JwtIssuerValidators for different issuers. It Contains key issuer url and value for validating JWT tokens based on their issuer.
    private final Map<String, JwtIssuerValidator> validators = new ConcurrentHashMap<>();

    // Method required by OAuth2TokenValidator interface
    @Override
    public OAuth2TokenValidatorResult validate(Jwt token) {
        // Extract issuer from the JWT
        String issuer = getIssuerFromToken(token);
        // Retrieve or create JwtIssuerValidator for the issuer and validate the token
        JwtIssuerValidator validator = this.validators.computeIfAbsent(issuer, this::checkIssuer);
        // Perform token validation and return the result
        OAuth2TokenValidatorResult result = validator.validate(token);
        log.info("Token validation successful for tenant: {}", issuer);
        return result;
    }

    // Extract issuer from the JWT
    private String getIssuerFromToken(Jwt jwt) {
        return jwt.getIssuer().toString();
    }

    // Check if the issuer is known and create JwtIssuerValidator for it
    private JwtIssuerValidator checkIssuer(String issuer) {
        // Fetch TenantDTO from the web service based on the issuer URL
        TenantDTO tenantDTO = webClientService.getAllTenants(issuer);

        // If the fetched issuer matches the provided issuer, return JwtIssuerValidator for it
        if (tenantDTO != null && tenantDTO.getIssuer() != null && tenantDTO.getIssuer().equals(issuer)) {
            return new JwtIssuerValidator(tenantDTO.getIssuer());
        }

        // Throw exception if the issuer is unknown
        throw new IllegalArgumentException("Unknown tenant");
    }
}
