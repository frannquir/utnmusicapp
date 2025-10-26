package com.musicspring.app.music_app.security.service;

import com.musicspring.app.music_app.security.entity.CredentialEntity;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String jwtSecretKey;

    @Value("${jwt.expiration}")
    private Long jwtExpiration;

    @Value("${refresh.token.expiration}")
    private Long refreshTokenExpiration;

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public String generateRefreshToken (UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("type","refresh");
        return buildToken(claims, userDetails, refreshTokenExpiration);
    }

    public boolean validateRefreshToken(String refreshToken,UserDetails userDetails) {
        try{
            Jwts.parserBuilder()
                    .setSigningKey(getSignInKey())
                    .build()
                    .parseClaimsJws(refreshToken);

            final String username = extractUsername(refreshToken);
            return (username.equals(userDetails.getUsername())
                    && !isTokenExpired(refreshToken));
        }catch (JwtException e){
            return false; ///Invalid Token
        }
    }

    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();

        if (userDetails instanceof CredentialEntity credential) {
            Set<String> roleNames = credential.getRoles().stream()
                    .map(roleEntity -> roleEntity.getRole().name())
                    .collect(Collectors.toSet());
            claims.put("roles", roleNames);

            Set<String> permissionNames = credential.getRoles().stream()
                    .flatMap(roleEntity -> roleEntity.getPermits().stream())
                    .map(permitEntity -> permitEntity.getPermit().name())
                    .collect(Collectors.toSet());
            claims.put("permissions", permissionNames);

            if (credential.getUser() != null) {
                claims.put("userId", credential.getUser().getUserId());
            }

            if (credential.getEmail() != null) {
                claims.put("email", credential.getEmail());
            }

        } else {
            Set<String> authorities = userDetails.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toSet());
            claims.put("roles", authorities);
            claims.put("permissions", Set.of());
        }

        return buildToken(claims, userDetails, jwtExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts
                .parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String subjectFromToken = extractUsername(token);

        String emailFromUserDetails = null;
        if (userDetails instanceof CredentialEntity credential) {
            emailFromUserDetails = credential.getEmail();
        }

        boolean isSubjectMatch = emailFromUserDetails != null && subjectFromToken.equalsIgnoreCase(emailFromUserDetails);
        boolean isNotExpired = !isTokenExpired(token);
        boolean isNonLocked = userDetails.isAccountNonLocked();
        boolean isEnabled = userDetails.isEnabled();

        return (isSubjectMatch && isNotExpired && isNonLocked && isEnabled);
    }

    private String buildToken(
            Map<String, Object> extraClaims,
            UserDetails userDetails,
            long expiration
    ) {
        String subject;

        if(userDetails instanceof CredentialEntity credential && credential.getEmail() != null) {
            subject = credential.getEmail();
        } else {
            subject = userDetails.getUsername();
        }

        return Jwts
                .builder()
                .setClaims(extraClaims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() +
                        expiration))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    private boolean isTokenExpired(String token) {
        Date expiration = extractClaim(token, Claims::getExpiration);
        return expiration.before(new Date());
    }
}

