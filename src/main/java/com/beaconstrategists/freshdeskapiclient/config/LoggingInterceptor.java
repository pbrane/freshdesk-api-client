package com.beaconstrategists.freshdeskapiclient.config;

import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.lang.NonNull;

import java.io.IOException;

public class LoggingInterceptor implements ClientHttpRequestInterceptor {

    @Override
    public @NonNull ClientHttpResponse intercept(@NonNull HttpRequest request,
                                                 @NonNull byte[] body,
                                                 @NonNull ClientHttpRequestExecution execution) throws IOException {
        // Log request details
        logRequest(request, body);

        // Execute the request and capture the response
        ClientHttpResponse response = execution.execute(request, body);

        // Log response details
        logResponse(response);

        return response;
    }

    private void logRequest(HttpRequest request, byte[] body) {
        System.out.println("\n\n\tRequest URI: " + request.getURI());
        System.out.println("\tRequest Method: " + request.getMethod());
        System.out.println("\tRequest Headers: " + request.getHeaders());
        System.out.println("\tRequest Body: " + new String(body));
        System.out.println("\n\n");
    }

    private void logResponse(ClientHttpResponse response) throws IOException {
        System.out.println("Response Status Code: " + response.getStatusCode());
        System.out.println("Response Headers: " + response.getHeaders());
    }
}
