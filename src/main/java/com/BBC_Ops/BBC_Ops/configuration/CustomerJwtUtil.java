package com.BBC_Ops.BBC_Ops.configuration;

import com.BBC_Ops.BBC_Ops.Model.Customer;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class CustomerJwtUtil {

    private static final Logger logger = LoggerFactory.getLogger(CustomerJwtUtil.class);
//    private final SecretKey SECRET_KEY = Keys.secretKeyFor(SignatureAlgorithm.HS256);

    private final SecretKey secretKey;

    public CustomerJwtUtil(SecretKey secretKey) {
        this.secretKey = secretKey;
    }
    public String generateToken(Customer customer) {
        String token = Jwts.builder()
                .setSubject(customer.getEmail())
                .claim("customerId", customer.getCustomerId())
                .claim("customerName", customer.getName())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 30)) // 30 mins
                .signWith(secretKey)
                .compact();

        logger.info("Generated JWT token for customer: {}", customer.getEmail());
        return token;
    }

    public Claims extractClaims(String token) {
        try {
            return Jwts.parser()
                    .setSigningKey(secretKey)
                    .parseClaimsJws(token)
                    .getBody();
        } catch (JwtException e) {
            logger.error("Failed to extract claims from token", e);
            throw e;
        }
    }
}
