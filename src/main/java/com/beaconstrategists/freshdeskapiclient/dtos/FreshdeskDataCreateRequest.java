package com.beaconstrategists.freshdeskapiclient.dtos;

import lombok.Data;

@Data //fixme: check this Lombok configuration
public class FreshdeskDataCreateRequest<T> {

    private T data;

    public FreshdeskDataCreateRequest(T data) {
        this.data = data;
    }
}
