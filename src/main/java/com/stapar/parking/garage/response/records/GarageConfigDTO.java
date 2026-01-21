package com.stapar.parking.garage.response.records;

import java.math.BigDecimal;

public record GarageConfigDTO(
        String sector,
        BigDecimal basePrice,
        Integer max_capacity
) {}
