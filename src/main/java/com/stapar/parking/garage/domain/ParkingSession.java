package com.stapar.parking.garage.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParkingSession {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String licensePlate;

    @Enumerated(EnumType.STRING)
    private SessionStatus status;

    private LocalDateTime entryTime;
    private LocalDateTime exitTime;
    private String assignedSector;
    private BigDecimal priceFactor;
    private BigDecimal finalAmount;
}