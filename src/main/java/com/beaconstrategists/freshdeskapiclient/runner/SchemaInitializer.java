package com.beaconstrategists.freshdeskapiclient.runner;

import com.beaconstrategists.freshdeskapiclient.services.SchemaService;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Configuration
public class SchemaInitializer {

    private final SchemaService schemaService;
    private final Map<String, JsonNode> schemaInstances = new ConcurrentHashMap<>();

    public SchemaInitializer(SchemaService schemaService) {
        this.schemaService = schemaService;
    }

    @Bean
    public ApplicationRunner initializeSchemas() {
        System.out.println("Initializing schemas...");

        return args -> {
            var schemas = schemaService.fetchSchemas();

            JsonNode tacCases = schemaService.findSchemaByName(schemas, "TAC Cases");
            JsonNode rmaCases = schemaService.findSchemaByName(schemas, "RMA Cases");

            if (tacCases != null) {
                schemaInstances.put("TAC Cases", tacCases);
            }
            if (rmaCases != null) {
                schemaInstances.put("RMA Cases", rmaCases);
            }

            System.out.println("\n\n\tInitialized schemas: " + schemaInstances.keySet() + "\n\n\n");
        };
    }

    public JsonNode getSchema(String name) {
        return schemaInstances.get(name);
    }

    public String getSchemaId(String schema) {
        JsonNode schemaInstance = getSchema(schema);
        if (schemaInstance == null) {
            return null;
        }
        return schemaInstance.get("id").asText();
    }
}
