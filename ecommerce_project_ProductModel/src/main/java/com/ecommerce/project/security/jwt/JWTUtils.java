package com.ecommerce.project.security.jwt;

import com.ecommerce.project.security.services.Userdetailsimpl;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.util.WebUtils;

import javax.crypto.SecretKey;
import java.security.Key;
import java.util.Date;

/**
 * Utility class for handling JWT (JSON Web Token) operations.
 * <p>
 * This class provides functionality for:
 * <ul>
 *     <li>Generating JWT tokens</li>
 *     <li>Validating JWT tokens</li>
 *     <li>Extracting usernames from JWT tokens</li>
 *     <li>Reading JWTs from HTTP Authorization headers</li>
 * </ul>
 */
@Component
public class JWTUtils {

    private static final Logger logger = LoggerFactory.getLogger("JWTUtils.class");

    /**
     * JWT expiration time in milliseconds.
     * Injected from application properties.
     */


    @Value("${spring.app.jwtExpirationMs}")
    private int jwtExpiration;

    /**
     * JWT secret key used to sign the token.
     * Injected from application properties.
     */

    @Value("${spring.app.jwtSecretKey}")
    private String jwtSecret;

    @Value("${spring.ecom.app.jwtCookieName}")
    private String jwtCookie;


    public String getJwtFromCookie(HttpServletRequest request){
        Cookie cookie= WebUtils.getCookie(request,jwtCookie);
        if(cookie!=null){
            return cookie.getValue();
        }
        else {
            return null;
        }
    }

    public ResponseCookie generateJwtCookie(Userdetailsimpl userPrincipal){
        String jwt=getTokenFromUserName(userPrincipal.getUsername());
        ResponseCookie cookie=ResponseCookie.from(jwtCookie,jwt)
                .path("/api")
                .maxAge(24*60*60)
                .httpOnly(false)
                .build();
        return cookie;

    }

    public ResponseCookie generateCleanJwtCookie(){
        ResponseCookie cookie=ResponseCookie.from(jwtCookie,null)
                .path("/api")
                .build();
        return cookie;

    }

    /**
     * Generates a JWT token using the provided user details.
     *
     * @param username the user details from which the username is extracted.
     * @return a JWT token string.
     */
    public String getTokenFromUserName(String  username) {
        return Jwts.builder()
                .subject(username)
                .issuedAt(new Date())
                .expiration(new Date(new Date().getTime() + jwtExpiration))
                .signWith(key())
                .compact();
    }

    /**
     * Extracts the username (subject) from a given JWT token.
     *
     * @param token the JWT token.
     * @return the username stored as the subject in the token.
     */
    public String getUsernameFromJWTToken(String token) {
        return Jwts.parser()
                .verifyWith((SecretKey) key())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    /**
     * Returns the signing key used for both signing and verifying JWT tokens.
     *
     * @return the cryptographic key derived from the configured secret.
     */
    public Key key() {
        return Keys.hmacShaKeyFor(
                Decoders.BASE64.decode(jwtSecret)
        );
    }

    /**
     * Validates a given JWT token.
     *
     * @param authToken the JWT token to validate.
     * @return {@code true} if the token is valid; {@code false} otherwise.
     */
    public boolean validateToken(String authToken) {
        try {
            System.out.println("validate ");
            Jwts.parser()
                    .verifyWith((SecretKey) key())
                    .build()
                    .parseSignedClaims(authToken);

            return true;
        } catch (MalformedJwtException e) {
            logger.error("Invalid JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            logger.error("Token expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            logger.error("Unsupported token: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.error("JWT claims string is empty: {}", e.getMessage());
        }

        return false;
    }
}
