package com.beaconstrategists.freshdeskapiclient.config;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

import java.util.Base64;

@Configuration
public class RestClientConfig {

    @Value("${FRESHDESK_API_KEY:0123456789ABCDEFGHI}")
    private String freshdeskApiKey;

    @Getter
    @Value("${FRESHDESK_BASE_URI:https://domain.freshdesk.com/api/v2}")
    private String freshdeskBaseUri;

    @Bean
    public RestClient restClient() {
        return RestClient.builder()
                .baseUrl(freshdeskBaseUri)
                .defaultHeader("Authorization", "Basic " + getBase64ApiKey())
                .build();
    }

    private String getBase64ApiKey() {
        return Base64.getEncoder().encodeToString((freshdeskApiKey + ":X").getBytes());
    }

}

