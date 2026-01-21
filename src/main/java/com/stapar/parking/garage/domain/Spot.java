package com.stapar.parking.garage.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Spot {
    @Id
    private Long id;

    private String sectorCode;
    private Double lat;
    private Double lng;
}