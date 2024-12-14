package com.beaconstrategists.freshdeskapiclient;

import com.beaconstrategists.taccaseapiservice.config.api.JacksonConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import(JacksonConfig.class)
public class FreshdeskApiClientApplication {

    public static void main(String[] args) {
        SpringApplication.run(FreshdeskApiClientApplication.class, args);
    }

}
