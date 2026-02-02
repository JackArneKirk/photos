package com.example.demo.controllers;

import static com.example.demo.constant.Constant.LOG_PREFIX;

import java.io.IOException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.reactive.function.client.WebClient;

import com.example.demo.model.User;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class AuthenticationFilter extends OncePerRequestFilter {

    private static final String CLASS_NAME = AuthenticationFilter.class.getSimpleName();

    private PublicKey publicKey;
    private final WebClient webClient;

    public AuthenticationFilter(WebClient webClient) {
        this.webClient = webClient;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");
        log.info("addr: " + request.getRequestURI());
        if (request.getRequestURI().startsWith("/h2-console")) {
            // TODO - only accept admins in future
            filterChain.doFilter(request, response);
        } else {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                SecurityContextHolder.clearContext();
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                log.info("provided auth" + authHeader);
                log.info("{} -- {} Filtering out unauthorized request", LOG_PREFIX, CLASS_NAME);
                return;
            }

            try {
                User user = authenticateUser(authHeader.substring(7));
                UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(user.getUserID(),
                        null, List.of());
                SecurityContextHolder.getContext().setAuthentication(auth);
                filterChain.doFilter(request, response);
            } catch (NotAuthenticatedException | JwtException e) {
                log.error("{} -- {} Error authenticating user: {}. Message: {}", LOG_PREFIX, CLASS_NAME, e.getClass(),
                        e.getMessage());
                SecurityContextHolder.clearContext();
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            }
        }
    }

    @PostConstruct
    public void init() {
        try {
            Mono<String> key = webClient.get().uri("/publicKey")
                    .retrieve()
                    .bodyToMono(String.class);
            byte[] decoded = Base64.getDecoder().decode(key.block());
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            KeySpec spec = new X509EncodedKeySpec(decoded);
            publicKey = keyFactory.generatePublic(spec);
            log.info("{} -- {} Successfully read public key", LOG_PREFIX, CLASS_NAME);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            log.error("{} -- {} Error reading public key: {}, message: {}", LOG_PREFIX, CLASS_NAME,
                    e.getClass().getSimpleName(), e.getMessage());
        } catch (Exception e) {
            log.error("ERROR: {}, message: {}", e.getClass().getSimpleName(), e.getMessage());
        }
    }

    private User authenticateUser(String rawJwt) throws NotAuthenticatedException, JwtException {
        Claims claims = Jwts.parser()
                .verifyWith(publicKey)
                .build()
                .parseSignedClaims(rawJwt)
                .getPayload();
        log.info("claims: ", String.join(", ", claims.keySet()));
        if (claims.containsKey("userID") && claims.containsKey("role")) {
            log.info("{} -- {} claims valid!!", LOG_PREFIX, CLASS_NAME);
            return new User(claims.get("userID", String.class), claims.get("role", String.class));
        }
        throw new NotAuthenticatedException("Could not authenticate user.");
    }

}
