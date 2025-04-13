package io.neif.coworkingplacehub.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
public class JWTUtil {

    @Value("${jwt.secret-key}")
    private String secretKey;


    public String generateToken(String username, String role, UUID id) {

        return Jwts.builder()
                .claim("role", role)
                .claim("username", username)
                .claim("id", id.toString())
                .subject("username")
                .issuer("coworking-place-hub")
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(Date.from(ZonedDateTime.now().plusMinutes(60).toInstant()))
                .signWith(Keys.hmacShaKeyFor(secretKey.getBytes()))
                .compact();

    }

    public Map<String, String> validateTokenAndRetrieveClaims(String token) {
        JWTVerifier verifier = JWT.require(Algorithm.HMAC512(secretKey))
                .withIssuer("coworking-place-hub")
                .build();

        DecodedJWT jwt = verifier.verify(token);

        String username = jwt.getClaim("username").asString();
        String role = jwt.getClaim("role").asString();
        UUID id = UUID.fromString(jwt.getClaim("id").asString());
        Map<String, String> claims = new HashMap<>();
        claims.put("username", username);
        claims.put("role", role);
        claims.put("id", id.toString());
        System.out.println("Декодированный username: " + jwt.getClaim("username").asString());
        System.out.println("Декодированная role: " + jwt.getClaim("role").asString());
        System.out.println("Проверка токена: username=" + username + ", role=" + role);
        return claims;
    }

}
