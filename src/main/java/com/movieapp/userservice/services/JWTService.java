package com.movieapp.userservice.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.movieapp.userservice.models.UserPrincipal;
import com.movieapp.userservice.models.Users;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class JWTService {

    private final PrivateKey privateKey;
    private final PublicKey publicKey;

    private static final long EXPIRATION_TIME = 60 * 60 * 1000; // 1 hour

    public JWTService(
        @Value("${jwt.private-key}") String privateKeyString,
        @Value("${jwt.public-key}") String publicKeyString
    ) throws NoSuchAlgorithmException, InvalidKeySpecException {

        if (privateKeyString == null || publicKeyString == null) {
            throw new IllegalStateException("JWT keys are not configured");
        }

        this.privateKey = loadPrivateKey(privateKeyString);
        this.publicKey = loadPublicKey(publicKeyString);
    }

    private PrivateKey loadPrivateKey(String key)
            throws NoSuchAlgorithmException, InvalidKeySpecException {

        byte[] keyBytes = Base64.getDecoder().decode(key);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
        return KeyFactory.getInstance("RSA").generatePrivate(keySpec);
    }

    private PublicKey loadPublicKey(String key)
            throws NoSuchAlgorithmException, InvalidKeySpecException {

        byte[] keyBytes = Base64.getDecoder().decode(key);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
        return KeyFactory.getInstance("RSA").generatePublic(keySpec);
    }

    public String generateToken(Users user) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + EXPIRATION_TIME);

//        Map<String, List<String>> claims = new HashMap<>();
//        claims.put(
//            "roles",
//            user.getAuthorities().stream()
//                .map(a -> a.getAuthority())
//                .toList()
//        );

        return Jwts.builder()
            .setSubject(user.getId().toString())
            .claim("email",user.getEmail())
            .claim("roles", user.getRoles())
            .setIssuedAt(now)
            .setExpiration(expiry)
            .signWith(privateKey, SignatureAlgorithm.RS256)
            .compact();
    }

    public boolean validateToken(String token) {
        try {
            extractClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public String extractUsername(String token) {
    	
        return extractClaims(token).getSubject();
    }
    
    public String extractEmail(String token) {
    	return (String) extractClaims(token).get("email");
    }
    
   

    private Claims extractClaims(String token) {
        return Jwts.parserBuilder()
            .setSigningKey(publicKey)
            .build()
            .parseClaimsJws(token)
            .getBody();
    }
}
