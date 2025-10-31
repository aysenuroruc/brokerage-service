package security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Component
public class JwtUtil {

        @Value("${app.jwt.secret}")
        private String secret;

        @Value("${app.jwt.expirationMillis}")
        private long expirationMillis;

        private SecretKey key;

        @PostConstruct
        public void init() {
            key = Keys.hmacShaKeyFor(secret.getBytes());
        }

        public String generateToken(String username, List<String> roles, Long customerId) {
            long now = System.currentTimeMillis();
            return Jwts.builder()
                    .setSubject(username)
                    .setIssuedAt(new Date(now))
                    .setExpiration(new Date(now + expirationMillis))
                    .addClaims(Map.of("roles", roles, "customerId", customerId))
                    .signWith(key, SignatureAlgorithm.HS256)
                    .compact();
        }
}
