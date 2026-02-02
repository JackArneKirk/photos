package com.example.demo.services.impl;

import static com.example.demo.constant.Constant.LOG_PREFIX;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.example.demo.services.AuthenticationService;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Header;
import io.jsonwebtoken.Jwe;
import io.jsonwebtoken.Jwt;
import io.jsonwebtoken.JwtParserBuilder;
import io.jsonwebtoken.Jwts;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@Service
public class AuthenticationServiceImpl implements AuthenticationService{

    private static final String CLASS_NAME =  AuthenticationServiceImpl.class.getSimpleName();

    private String publicKey;
    private final WebClient webClient;

    public AuthenticationServiceImpl(WebClient webClient){
        this.webClient = webClient;
    }

    @PostConstruct
    public void init() {
        Mono<String> key = webClient.get().uri("/publicKey")
                .retrieve()
                .bodyToMono(String.class);
        publicKey = key.block();
    }

    @Override
    public String authenticateUser(String rawJwt) {

        try{
        byte[] decoded = Base64.getDecoder().decode(rawJwt);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        KeySpec spec = new X509EncodedKeySpec(decoded);
        PublicKey key = keyFactory.generatePublic(spec);

        Jwt<Header, Claims> jwt = Jwts.parser()
                .verifyWith(key)
                .require("", "")
                .build()
                .parse(rawJwt)
                .accept(Jwt.UNSECURED_CLAIMS);
        Claims claims = jwt.getPayload();
        if(claims.containsKey("folders")){

        }
        }catch(NoSuchAlgorithmException | InvalidKeySpecException e){
            log.error("{} -- {} Error checking Jwt", e);
        }
        return "";
    }
    
}
