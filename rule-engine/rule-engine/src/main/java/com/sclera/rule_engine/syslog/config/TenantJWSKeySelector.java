package com.sclera.rule_engine.syslog.config;

import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.KeySourceException;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.jwk.source.RemoteJWKSet;
import com.nimbusds.jose.proc.JWSAlgorithmFamilyJWSKeySelector;
import com.nimbusds.jose.proc.JWSKeySelector;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jose.util.DefaultResourceRetriever;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.proc.JWTClaimsSetAwareJWSKeySelector;
import com.sclera.rule_engine.syslog.dto.TenantDTO;
import com.sclera.rule_engine.syslog.service.WebClientService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.net.MalformedURLException;
import java.net.URL;
import java.security.Key;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.nimbusds.jose.proc.JWSAlgorithmFamilyJWSKeySelector.fromJWKSource;

@Slf4j
public class TenantJWSKeySelector implements JWTClaimsSetAwareJWSKeySelector<SecurityContext> {

    // Autowired service for making web requests
    @Autowired
    public WebClientService webClientService;

    // Map to cache JWSKeySelectors for different issuers. It Contains key issuer url and Value is Public key information.
    private final Map<String, JWSKeySelector<SecurityContext>> selectors = new ConcurrentHashMap<>();

    // Method required by JWTClaimsSetAwareJWSKeySelector interface
    @Override
    public List<? extends Key> selectKeys(JWSHeader jwsHeader, JWTClaimsSet jwtClaimsSet, SecurityContext securityContext) throws KeySourceException {
        // Fetch JWSKeySelector for the issuer and select keys based on JWSHeader and SecurityContext
        String issuer = getIssuerUrlFromToken(jwtClaimsSet);
        return this.selectors
                .computeIfAbsent(issuer,this::checkIssuer)
                .selectJWSKeys(jwsHeader, securityContext);  // This line selects cryptographic keys based on the JWS header and security context for verifying JSON Web Signatures (JWS).
    }

    // Extract issuer URL from JWT claims set
    private String getIssuerUrlFromToken(JWTClaimsSet claimSet) {
        return (String) claimSet.getClaim("iss");
    }


    // Check if the issuer is known and fetch its public key
    private JWSKeySelector<SecurityContext> checkIssuer(String issuer) {
        // Fetch TenantDTO from the web service based on the issuer URL
        TenantDTO tenantDTO = webClientService.getAllTenants(issuer);

        // If the fetched issuer matches the provided issuer, return JWSKeySelector for its public key
        if (tenantDTO != null && tenantDTO.getIssuer() != null && tenantDTO.getIssuer().equals(issuer)) {
            return getIssuerPublicKey(tenantDTO.getJwksUri());
        }

        // Log error if the issuer is unknown
        log.error("Unknown tenant: {}", issuer);
        throw new IllegalArgumentException("Unknown tenant");
    }

    // Fetch JWSKeySelector for the issuer's public key from its JWKS URI
    private JWSKeySelector<SecurityContext> getIssuerPublicKey(String uri) {
        try {
            // Construct JWKSource from the provided JWKS URI and return JWSKeySelector
            return getJwksPublicKey(new URL(uri));
        } catch (Exception ex) {
            throw new IllegalArgumentException(ex);
        }
    }

    // Method to fetch JWSKeySelector for JWKS URL
    public static <C extends SecurityContext> JWSAlgorithmFamilyJWSKeySelector<C> getJwksPublicKey(URL jwkSetURL) throws KeySourceException, MalformedURLException {
        // Construct JWKSource from the provided JWKS URL
        JWKSource<C> jwkSource = new RemoteJWKSet<>(
                new URL(jwkSetURL.toString()),
                new DefaultResourceRetriever(500000, 500000, 500000), // Set resource retriever with timeouts
                new DefaultJWKSetCache(1800000)); // Set cache expiry time
        // Return JWSKeySelector constructed from JWKSource
        return fromJWKSource(jwkSource);
    }

}