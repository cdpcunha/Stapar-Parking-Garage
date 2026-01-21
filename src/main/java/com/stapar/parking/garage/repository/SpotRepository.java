package com.stapar.parking.garage.repository;

import com.stapar.parking.garage.domain.Spot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SpotRepository extends JpaRepository<Spot, Long> {
    // Busca exata por lat/lng para descobrir o setor
    Optional<Spot> findByLatAndLng(Double lat, Double lng);
}
