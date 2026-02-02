package com.example.demo.config;

import static com.example.demo.constant.Constant.LOG_PREFIX;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
public class WebClientConfig {

    @Value("${auth.url}")
    private String authURL;

    @Bean
    public WebClient webClient() {
        log.info("{} web bean initialised", LOG_PREFIX);
        return WebClient.builder()
                .baseUrl(authURL)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

}
