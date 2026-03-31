package com.taskmanager.taskmanager.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
//JwtUtil as the token factory + validator.
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expiration;

    // ─── 1. GET SIGNING KEY ───
    private Key getSigningKey(){
        byte[] keyBytes = secret.getBytes();
        return Keys.hmacShaKeyFor(keyBytes);
        // converts your secret string into a cryptographic key
        // used to SIGN the token while creating
        // used to VERIFY the token while validating
    }

    // ─── 2. GENERATE TOKEN ────

    public String generateToken(String username, String role) {

        Map<String, Object> claims = new HashMap<>();
        claims.put("role", role);             // embed role inside token payload

        return Jwts.builder()
                .setClaims(claims)            // payload data (role)
                .setSubject(username)         // who this token belongs to
                .setIssuedAt(new Date())      // token created at
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey())    // sign with secret key
                .compact();                   // build the final token string
    }

    // ─── 3. EXTRACT USERNAME ─────
    public String extractUsername(String token){
        return extractAllClaims(token).getSubject();
        // subject = username we set during generateToken()
    }
    // ─── 4. EXTRACT ROLE ────
    public String extractRole(String token){
        return (String) extractAllClaims(token).get("role");
        // gets the "role" we embedded in claims

    }
    // ─── 5. VALIDATE TOKEN ──
    public boolean validateToken(String token){
        try{
            extractAllClaims(token); // if this doesn't throw → token is valid
            return true;
        } catch (ExpiredJwtException e) {
            System.out.println("Token expired");
        } catch (MalformedJwtException e) {
            System.out.println("Invalid token");
        } catch (Exception e) {
            System.out.println("Token validation failed");
        }
        return false;

    }
    // ─── 6. EXTRACT ALL CLAIMS (private helper) ───
    private Claims extractAllClaims(String token){
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

}
