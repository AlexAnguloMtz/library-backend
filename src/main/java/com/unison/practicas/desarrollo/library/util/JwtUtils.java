package com.unison.practicas.desarrollo.library.util;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.unison.practicas.desarrollo.library.entity.Role;
import com.unison.practicas.desarrollo.library.entity.User;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Date;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class JwtUtils {

    // TODO
    // Inject a real secret from a configuration file
    private final String SECRET = "supersecretkeyyoushouldstoreitsecurelyandlongenough123";

    public String accessTokenForUser(User user) {
        try {
            JWTClaimsSet claims = new JWTClaimsSet.Builder()
                    .subject(user.getEmail())
                    .claim("roles", user.getRoles().stream().map(Role::getName).collect(Collectors.toSet()))
                    .claim("permissions", Set.of())
                    .issueTime(Date.from(Instant.now()))
                    .expirationTime(Date.from(Instant.now().plusSeconds(3600)))
                    .build();

            JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.HS256)
                    .type(JOSEObjectType.JWT)
                    .build();

            SignedJWT signedJWT = new SignedJWT(header, claims);
            signedJWT.sign(new MACSigner(SECRET.getBytes()));

            return signedJWT.serialize();
        } catch (JOSEException e) {
            throw new RuntimeException("Could not generate JWT for user " + user.getEmail(), e);
        }
    }

    public String validateAndGetSubject(String token) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);

            boolean valid = signedJWT.verify(new MACVerifier(SECRET.getBytes()));
            if (!valid) {
                throw new RuntimeException("Invalid JWT signature");
            }

            Date expiration = signedJWT.getJWTClaimsSet().getExpirationTime();
            if (expiration.before(Date.from(Instant.now()))) {
                throw new RuntimeException("JWT has expired");
            }

            return signedJWT.getJWTClaimsSet().getSubject();
        } catch (Exception e) {
            throw new RuntimeException("Failed to validate JWT", e);
        }
    }
}
