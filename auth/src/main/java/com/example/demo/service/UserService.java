package com.example.demo.service;

import static com.example.demo.constant.Constant.LOG_PREFIX;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;

import org.springframework.stereotype.Service;

import com.example.demo.model.Role;
import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;

import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class UserService {

    private final String CLASS_NAME = UserService.class.getSimpleName();

    private PrivateKey privateKey;

    private PublicKey publicKey;

    private final PasswordEncoder passwordEncoder;

    @Value("${user.token-exp}")
    private int tokenExpiryTime;

    private UserRepository repo;

    public UserService(UserRepository repo, PublicKey publicKey, PrivateKey privateKey, PasswordEncoder passwordEncoder) {
        this.repo = repo;
        this.privateKey = privateKey;
        this.publicKey = publicKey;
        this.passwordEncoder = passwordEncoder;
        log.info("{} -- {} initialised.", LOG_PREFIX, CLASS_NAME);
    }

    public User getUser(String username, PrivateKey privateKey, PublicKey publicKey) {
        User user = repo.findByUsername(username);
        if (user != null) {
            return user;
        }
        return null;
    }

    public String getPublicKey(){
        log.info("Key encoding: {}, key: {}", publicKey.getFormat(), Base64.getEncoder().encodeToString(publicKey.getEncoded()));
        return new String(Base64.getEncoder().encodeToString(publicKey.getEncoded()));
    }

    public String authenticateUser(String username, String pass) {
        User user = repo.findByUsername(username);
        log.info("{} -- {} user: {}. Found: {}", LOG_PREFIX, CLASS_NAME, username, user);
        log.info("{} -- {} does it match? {}", LOG_PREFIX, CLASS_NAME, passwordEncoder.matches(pass, user.getPassHash()));
        if (user != null && passwordEncoder.matches(pass, user.getPassHash())) {
            return generateJWT(user);
        }
        return "";
    }

    private String generateJWT(User user) {

            Instant now = Instant.now();

            String jwt = Jwts.builder()
                    .subject(LOG_PREFIX)
                    .claim("userID", Long.toString(user.getId()))
                    .claim("role", user.getRole().toString())
                    .issuedAt(Date.from(now))
                    .expiration(Date.from(now.plusMillis(100000000)))
                    .signWith(privateKey)
                    .compact();
            //log.info("{} -- {} jwt exp: {}", LOG_PREFIX, CLASS_NAME, Jwts.parser().build().parse(jwt).getPayload());
            return jwt;
        
    }

    public boolean createUser(String username, String email, String password, Role role) {
        try {
            String hashedPass = passwordEncoder.encode(password);//BCrypt.hashpw(password, BCrypt.gensalt());
            User newUser = new User();
            newUser.setUsername(username);
            newUser.setEmail(email);
            newUser.setPassHash(hashedPass);
            newUser.setRole(role);
            log.info("user: ID = {}, uName= {}, email= {}, role={}, pass={}", newUser.getId(), newUser.getUsername(), newUser.getEmail(), newUser.getRole(), newUser.getPassHash());
            repo.save(newUser);
            return true;
        } catch (IllegalArgumentException e) {
            log.error("{} -- {} IllegalArgumentException saving user", LOG_PREFIX, CLASS_NAME);
            return false;
        }
    }
}
