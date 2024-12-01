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
public class CompanyService {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final Map<String, String> companyMap = new ConcurrentHashMap<>();

    public CompanyService(RestClient restClient, ObjectMapper objectMapper) {
        this.restClient = restClient;
        this.objectMapper = objectMapper;
    }

    public List<JsonNode> fetchCompanies() {
        String response = restClient.get()
                .uri("/companies")
                .retrieve()
                .body(String.class);

        try {
            return objectMapper.readValue(response, new TypeReference<List<JsonNode>>() {});
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse companies response", e);
        }
    }

    public void initializeCompanies() {
        var companies = fetchCompanies();
        companies.forEach(company -> {
            String name = company.get("name").asText();
            String id = company.get("id").asText();
            companyMap.put(name, id);
        });
    }

    public String getCompanyIdByName(String name) {
        return companyMap.get(name);
    }
}
