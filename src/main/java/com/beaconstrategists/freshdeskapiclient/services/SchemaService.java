package com.beaconstrategists.freshdeskapiclient.services;

import com.beaconstrategists.freshdeskapiclient.config.RestClientConfig;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;

@Service
public class SchemaService {

    private final RestClientConfig restClientConfig;
    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    @Value("${FRESHDESK_SCHEMA_URI:/custom_objects/schemas}")
    private String schemaUri;

    public SchemaService(RestClientConfig restClientConfig, RestClient restClient, ObjectMapper objectMapper) {
        this.restClientConfig = restClientConfig;
        this.restClient = restClient;
        this.objectMapper = objectMapper;
    }

    public List<JsonNode> fetchSchemas() {
        System.out.println("\n\n\tFetching schemas...");
        System.out.println("\tFreshDesk base URL: " + restClientConfig.getFreshdeskBaseUrl());
        System.out.println("\tSchema URI: " + schemaUri + "\n\n\n");

        String response = restClient.get()
                .uri(schemaUri)
                .retrieve()
                .body(String.class);

        try {
            JsonNode rootNode = objectMapper.readTree(response);
            return objectMapper.convertValue(rootNode.get("schemas"), new TypeReference<List<JsonNode>>() {});
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse schemas response", e);
        }
    }

    public JsonNode findSchemaByName(List<JsonNode> schemas, String name) {
        return schemas.stream()
                .filter(schema -> name.equals(schema.get("name").asText()))
                .findFirst()
                .orElse(null);
    }
}
