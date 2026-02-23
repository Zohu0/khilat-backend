package e_commerce.khilat.admin;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtUtil {

    // 1. Define the raw string
    private static final String SECRET_STRING = 
            "this_is_a_very_secure_secret_key_which_is_32_chars_long";
    
    // 2. Convert it to a SecretKey object that all methods can "see"
    private final SecretKey ALGO_KEY = Keys.hmacShaKeyFor(SECRET_STRING.getBytes(StandardCharsets.UTF_8));

    private static final long EXPIRATION_TIME = 60 * 60 * 1000; // 1 hour

    public String generateToken(String email) {
        return Jwts.builder()
                .subject(email)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(ALGO_KEY) // Now it can resolve this variable
                .compact();
    }

    public String extractEmail(String token) {
        return Jwts.parser()
                .verifyWith(ALGO_KEY) // Now it can resolve this variable
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    public boolean validateToken(String token) {
        try {
            extractEmail(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}