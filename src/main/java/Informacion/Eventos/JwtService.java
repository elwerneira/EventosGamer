package Informacion.Eventos;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.time.Instant;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
public class JwtService {
    static final long LIFETIME_SECONDS = 900;
    private final SecretKey key;

    public JwtService(@Value("${security.jwt.secret}") String secret) {
        key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
    }

    public String issue(Authentication authentication) {
        Instant now = Instant.now();
        return Jwts.builder().issuer("greegame").subject(authentication.getName())
            .issuedAt(Date.from(now)).expiration(Date.from(now.plusSeconds(LIFETIME_SECONDS)))
            .claim("roles", authentication.getAuthorities().stream().map(Object::toString).toList())
            .signWith(key, Jwts.SIG.HS256).compact();
    }

    public String subject(String token) {
        var signed = Jwts.parser().verifyWith(key).requireIssuer("greegame").build().parseSignedClaims(token);
        Claims claims = signed.getPayload();
        if (!"HS256".equals(signed.getHeader().getAlgorithm()) || claims.getExpiration() == null
                || claims.getSubject() == null || claims.getSubject().isBlank()) {
            throw new JwtException("Token inválido.");
        }
        return claims.getSubject();
    }
}
