package com.example.demo.config;

import static com.example.demo.constant.Constant.LOG_PREFIX;

import java.io.IOException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.security.web.SecurityFilterChain;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
public class AuthConfig {

    private String CLASS_NAME = AuthConfig.class.getSimpleName();

    @Value("${key.private}")
    private Resource privateKeyString;

    @Value("${key.public}")
    private Resource publicKeyString;

    @Bean
    public PrivateKey privateKey() throws IOException {
        String pem = new String(privateKeyString.getInputStream().readAllBytes());
        return loadKey(pem, PrivateKey.class);
    }

    @Bean
    public PublicKey publicKey() throws IOException {
        String pem = new String(publicKeyString.getInputStream().readAllBytes());
        return loadKey(pem, PublicKey.class);
    }

    @Bean
    public PasswordEncoder passHash(){
        return new BCryptPasswordEncoder();
    }

    // @Bean
    // public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception{

    //     httpSecurity.csrf(crsf -> crsf.disable())
    //         .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
    //         .formLogin(form -> form.disable())
    //         .httpBasic(basic -> basic.disable());

    //     return httpSecurity.build();        
    // }

    private <T extends Key> T loadKey(String pem, Class<T> keyType) {
        String clean = pem
                .replaceAll("-----BEGIN [A-Z ]+-----", "")
                .replaceAll("-----END [A-Z ]+-----", "")
                .replaceAll("\\s", "");
        try {
            byte[] decoded = Base64.getDecoder().decode(clean);

            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            KeySpec spec;
            if (PrivateKey.class.equals(keyType)) {
                spec = new PKCS8EncodedKeySpec(decoded);
                return keyType.cast(keyFactory.generatePrivate(spec));
            } else if (PublicKey.class.equals(keyType)) {
                spec = new X509EncodedKeySpec(decoded);
                return keyType.cast(keyFactory.generatePublic(spec));
            }
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            log.error("{} -- {} {} when parsing keys: {}", LOG_PREFIX, CLASS_NAME, e.getClass().getSimpleName(), e.getMessage());
        }
        throw new IllegalArgumentException("nooo: " + keyType);
    }

}
