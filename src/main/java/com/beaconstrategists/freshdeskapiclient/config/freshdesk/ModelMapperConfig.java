package com.beaconstrategists.freshdeskapiclient.config.freshdesk;

import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


//fixme: do I need this bean?
@Configuration
public class ModelMapperConfig {

    @Bean
    public ModelMapper modelMapper() {
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration()
                .setSkipNullEnabled(true) // Skip null values
                .setFieldMatchingEnabled(true) // Enable field matching
                .setAmbiguityIgnored(true); // Ignore ambiguous mappings
        return modelMapper;
    }
}