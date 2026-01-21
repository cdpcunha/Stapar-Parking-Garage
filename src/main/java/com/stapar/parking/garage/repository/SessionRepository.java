package com.stapar.parking.garage.repository;

import com.stapar.parking.garage.domain.ParkingSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

public interface SessionRepository extends JpaRepository<ParkingSession, String> {
    // Busca sessão ativa pela placa
    @Query("SELECT p FROM ParkingSession p WHERE p.licensePlate = :plate AND p.status != 'FINISHED'")
    Optional<ParkingSession> findActiveByPlate(String plate);

    // Conta carros atualmente dentro da garagem
    @Query("SELECT COUNT(p) FROM ParkingSession p WHERE p.status != 'FINISHED'")
    long countActiveSessions();

    // Query para o Relatório de Receita
    @Query("SELECT COALESCE(SUM(p.finalAmount), 0) FROM ParkingSession p " +
            "WHERE p.assignedSector = :sector " +
            "AND p.exitTime BETWEEN :start AND :end")
    BigDecimal calculateRevenue(String sector, LocalDateTime start, LocalDateTime end);
}
