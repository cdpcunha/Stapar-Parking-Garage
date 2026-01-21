package com.stapar.parking.garage.repository;

import com.stapar.parking.garage.domain.Sector;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface SectorRepository extends JpaRepository<Sector, Long> {
    Optional<Sector> findByCode(String code);

    @Query("SELECT SUM(s.maxCapacity) FROM Sector s")
    Integer getTotalCapacity();
}