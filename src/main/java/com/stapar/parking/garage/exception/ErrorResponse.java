package com.stapar.parking.garage.exception;

import org.springframework.http.HttpStatus;

import java.time.Instant;

public record ErrorResponse(
        HttpStatus status,
        String detail,
        Instant time
) {
}
