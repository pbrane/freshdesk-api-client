package com.beaconstrategists.freshdeskapiclient.mappers;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Component
public class GenericMapper {

    private final ModelMapper modelMapper;

    public GenericMapper(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    public <S, D> D map(S source, Class<D> destinationClass) {
        configureMapper(); // Optional: Apply custom configuration
        return modelMapper.map(source, destinationClass);
    }

    public <S, D> void map(S source, D destination) {
        configureMapper(); // Optional: Apply custom configuration
        modelMapper.map(source, destination);
    }

    private void configureMapper() {
        modelMapper.getConfiguration()
                .setSkipNullEnabled(true)
                .setFieldMatchingEnabled(true)
                .setAmbiguityIgnored(true);

        // Add custom converter for LocalDate -> OffsetDateTime
        modelMapper.addConverter(context -> {
            LocalDate source = context.getSource();
            return source != null ? source.atStartOfDay().atOffset(ZoneOffset.UTC) : null;
        }, LocalDate.class, OffsetDateTime.class);
    }
}
