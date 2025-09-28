package com.unison.practicas.desarrollo.library.util;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.unison.practicas.desarrollo.library.entity.user.Permission;
import com.unison.practicas.desarrollo.library.entity.user.User;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Date;

@Component
public class JwtUtils {

    // TODO
    // Inject a real secret from a configuration file
    private final String SECRET = "supersecretkeyyoushouldstoreitsecurelyandlongenough123";

    public String accessTokenForUser(User user) {
        try {
            Instant now = Instant.now();
            JWTClaimsSet claims = new JWTClaimsSet.Builder()
                    .subject(user.getEmail())
                    .claim("role", user.getRole().getSlug())
                    .claim("permissions", user.getRole().getPermissions().stream().map(Permission::getName).toList())
                    .issueTime(Date.from(now))
                    .expirationTime(Date.from(now.plusSeconds(3600)))
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
