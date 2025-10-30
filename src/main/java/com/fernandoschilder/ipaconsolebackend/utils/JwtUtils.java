package com.fernandoschilder.ipaconsolebackend.utils;

import java.util.Base64;
import java.util.Date;

import com.fernandoschilder.ipaconsolebackend.security.UserDetailsImpl;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
public class JwtUtils {
    private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);

    @Value("${fernandoschilder.app.jwtSecret}")
    private String jwtSecret;

    @Value("${fernandoschilder.app.jwtExpirationMs}")
    private int jwtExpirationMs;

    public String generateJwtToken(Authentication authentication) {
        UserDetailsImpl userPrincipal = (UserDetailsImpl) authentication.getPrincipal();
        Algorithm algorithm = getAlgorithm();
        Date now = new Date();
        Date exp = new Date(now.getTime() + jwtExpirationMs);
        return JWT.create()
                .withSubject(userPrincipal.getUsername())
                .withIssuedAt(now)
                .withExpiresAt(exp)
                .sign(algorithm);
    }

    private Algorithm getAlgorithm() {
        // Expect jwtSecret to be Base64-encoded; if not, decode will throw IllegalArgumentException
        byte[] keyBytes;
        try {
            keyBytes = Base64.getDecoder().decode(jwtSecret);
        } catch (IllegalArgumentException e) {
            // Not base64? fall back to raw bytes of the string
            keyBytes = jwtSecret.getBytes();
        }
        return Algorithm.HMAC256(keyBytes);
    }

    public String getUserNameFromJwtToken(String token) {
        try {
            JWTVerifier verifier = JWT.require(getAlgorithm()).build();
            DecodedJWT jwt = verifier.verify(token);
            return jwt.getSubject();
        } catch (JWTVerificationException e) {
            logger.error("Failed to parse JWT and extract subject: {}", e.getMessage());
            return null;
        }
    }

    public boolean validateJwtToken(String authToken) {
        try {
            JWTVerifier verifier = JWT.require(getAlgorithm()).build();
            verifier.verify(authToken);
            return true;
        } catch (JWTVerificationException e) {
            logger.error("Invalid JWT token: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.error("JWT claims string is empty: {}", e.getMessage());
        }
        return false;
    }

}