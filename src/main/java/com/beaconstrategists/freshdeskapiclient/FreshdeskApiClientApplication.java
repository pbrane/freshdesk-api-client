package com.beaconstrategists.freshdeskapiclient;

import com.beaconstrategists.freshdeskapiclient.config.JacksonConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication
public class FreshdeskApiClientApplication {

    public static void main(String[] args) {
        SpringApplication.run(FreshdeskApiClientApplication.class, args);
    }

}
