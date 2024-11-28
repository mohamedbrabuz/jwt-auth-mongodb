package org.netprime.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.List;
import java.util.Set;

@Component
public class JwtUtil {

    private final RedisTemplate<String, String> redisTemplate;

    @Getter
    @Value("${jwt.expiration}")
    private long expiration;

    @Getter
    private Key key;

    public JwtUtil(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @PostConstruct
    public void init() {
        this.key = Keys.secretKeyFor(SignatureAlgorithm.HS256);
    }

    // Generate a JWT token for a given username
    public String generateToken(String email, Set<String> roles) {
        return Jwts.builder()
                .setSubject(email)
                .claim("roles", roles)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(key)
                .compact();
    }

    //Validate the token
    public boolean validateToken(String token, String email) {
        try {
            String tokenEmail = getEmailFromToken(token);
            return email.equals(tokenEmail) && !isTokenExpired(token);
        }catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public boolean validateToken(String token) {
        try{
            String email = getEmailFromToken(token);
            List<String> roles = getRolesFromToken(token);
            return email != null && roles != null && Boolean.TRUE.equals(redisTemplate.hasKey("JWT:" + email)) && !isTokenExpired(token);
        }catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    //Check token expiration
    private boolean isTokenExpired(String token) {
        Date expiration = getClaimsFromToken(token).getExpiration();
        return expiration.before(new Date());
    }

    //Retrieve email from token
    public String getEmailFromToken(String token) {
        return getClaimsFromToken(token).getSubject();
    }

    private List<String> getRolesFromToken(String token) {
        Object roles = getClaimsFromToken(token).get("roles");
        if (roles instanceof List<?>) {
            @SuppressWarnings("unchecked")
            List<String> rolesSet = (List<String>) roles;
            return rolesSet;
        }
        return null;
    }

    private Claims getClaimsFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
