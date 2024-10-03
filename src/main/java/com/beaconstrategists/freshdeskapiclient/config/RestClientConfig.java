package com.beaconstrategists.freshdeskapiclient.config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

import java.util.Base64;

@Configuration
public class RestClientConfig {

    @Value("${freshdesk.api.key}")
    private String freshdeskApiKey;

    @Value("${freshdesk.base.url}")
    private String freshdeskBaseUrl;

    @Bean
    public RestClient restClient() {
        return RestClient.builder()
                .baseUrl(freshdeskBaseUrl)
                .defaultHeader("Authorization", "Basic " + getBase64ApiKey())
                .build();
    }

    private String getBase64ApiKey() {
        return Base64.getEncoder().encodeToString((freshdeskApiKey + ":X").getBytes());
    }
}

