package com.stapar.parking.garage.response.records;

import java.util.List;

public record GarageResponse(
        List<GarageConfigDTO> garage,
        List<SpotConfigDTO> spots
) {}
