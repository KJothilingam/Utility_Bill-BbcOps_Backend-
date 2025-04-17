package com.BBC_Ops.BBC_Ops.configuration;

import com.BBC_Ops.BBC_Ops.Model.Employee;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.JwtException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtUtil {

    private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);

    // Use a SecretKey object instead of a string for the secret key
    private final SecretKey SECRET_KEY = Keys.secretKeyFor(SignatureAlgorithm.HS256);

    public String generateToken(Employee employee) {
        String token = Jwts.builder()
                .setSubject(employee.getEmail())
                .claim("userId", employee.getEmployeeId())
                .claim("userName", employee.getName())
                .claim("designation", employee.getDesignation())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 30)) // 30 mins
                .signWith(SECRET_KEY)
                .compact();

        logger.info("Generated JWT token for user: {}", employee.getEmail());
        return token;
    }

    public Claims extractClaims(String token) {
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(SECRET_KEY)
                    .parseClaimsJws(token)
                    .getBody();

            logger.info("Extracted claims for token subject: {}", claims.getSubject());
            return claims;
        } catch (JwtException e) {
            logger.error("Failed to extract claims from token", e);
            throw e;
        }
    }
}
