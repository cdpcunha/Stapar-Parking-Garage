package com.stapar.parking.garage.response.records;

public record SpotConfigDTO(
        Long id,
        String sector,
        Double lat,
        Double lng
) {}