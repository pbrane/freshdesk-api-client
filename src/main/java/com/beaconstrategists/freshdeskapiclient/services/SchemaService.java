package com.beaconstrategists.freshdeskapiclient.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SchemaService {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final Map<String, JsonNode> schemaMap = new ConcurrentHashMap<>();

    public SchemaService(RestClient restClient, ObjectMapper objectMapper) {
        this.restClient = restClient;
        this.objectMapper = objectMapper;
    }

    /**
     * Fetches all schemas from the Freshdesk API.
     * @return List of schemas as JsonNode objects.
     */
    public List<JsonNode> fetchSchemas() {
        String response = restClient.get()
                .uri("/custom_objects/schemas")
                .retrieve()
                .body(String.class);

        try {
            JsonNode rootNode = objectMapper.readTree(response);
            return objectMapper.convertValue(rootNode.get("schemas"), new TypeReference<List<JsonNode>>() {});
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse schemas response", e);
        }
    }

    /**
     * Initializes the schemas by storing them in a map for easy access.
     */
    public void initializeSchemas() {
        var schemas = fetchSchemas();
        schemas.forEach(schema -> {
            String name = schema.get("name").asText();
            schemaMap.put(name, schema);
        });
    }

    /**
     * Retrieves a schema by its name.
     *
     * @param name The name of the schema.
     * @return The schema as a JsonNode, or null if not found.
     */
    public JsonNode getSchemaByName(String name) {
        return schemaMap.get(name);
    }

    public String getSchemaIdByName(String name) {
        return schemaMap.get(name).get("id").asText();
    }
}
