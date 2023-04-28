package edu.npu.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.Map;
import java.util.function.Function;

/**
 * @author : [wangminan]
 * @description : [JWT工具类]
 */
@Component
public class JwtTokenProvider {

    @Value("${var.jwt.secret}")
    private String secret;

    @Value("${var.jwt.token-expiration}")
    private long jwtAccessExpirationInMs;

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public String generateToken(
            UserDetails userDetails
    ) {
        Date currentTime = new Date();
        Date expireTime = new Date(currentTime.getTime() + jwtAccessExpirationInMs);
        return Jwts
                .builder()
                .setSubject(userDetails.getUsername())
                .signWith(getKey(), SignatureAlgorithm.HS256)
                .setIssuedAt(currentTime)
                .setExpiration(expireTime)
                .compact();
    }

    public String generateToken(
            Map<String, Object> extraClaims,
            UserDetails userDetails
    ) {
        return Jwts
                .builder()
                .setClaims(extraClaims)
                .setSubject(userDetails.getUsername())
                .signWith(getKey(), SignatureAlgorithm.HS256)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + jwtAccessExpirationInMs))
                .compact();
    }

    /**
     * 验证token是否有效
     * @param token token
     * @param userDetails 登录用户的具体信息
     * @return token是否有效
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String userName = extractUsername(token);
        return userName.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        if (extractExpiration(token) != null) {
            return extractExpiration(token).before(new Date());
        }
        return true;
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private Claims extractAllClaims(String token) {
        return Jwts
                .parserBuilder()
                // setSigningKey(String)的方法已经被弃用，使用setSigningKey(function)替代
                .setSigningKey(getKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Key getKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }
}
