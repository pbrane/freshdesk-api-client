package com.beaconstrategists.freshdeskapiclient.config;

import com.beaconstrategists.freshdeskapiclient.dtos.FreshdeskTicketUpdateDto;
import com.beaconstrategists.taccaseapiservice.config.api.GenericFieldPresenceSnakeCaseJsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class JacksonSerializerConfig {
    @Bean
    @Qualifier("fieldPresenceSnakeCaseSerializingObjectMapper")
    public ObjectMapper createCustomObjectMapper() {
        ObjectMapper customMapper = new ObjectMapper();
        customMapper.registerModule(new JavaTimeModule()); // Add support for Java 8+ date/time
        customMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS); // Ensure dates are not serialized as arrays
        customMapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);

        SimpleModule module = new SimpleModule();
        module.addSerializer(FreshdeskTicketUpdateDto.class,
                new GenericFieldPresenceSnakeCaseJsonSerializer<>(customMapper));
        customMapper.registerModule(module);

        return customMapper;
    }

    @Bean
//    @Primary
    @Qualifier("defaultObjectMapperWithJavaTimeModule")
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        return objectMapper;
    }
}
