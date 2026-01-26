package com.stapar.parking.garage.service;

import com.stapar.parking.garage.domain.ParkingSession;
import com.stapar.parking.garage.domain.SessionStatus;
import com.stapar.parking.garage.exception.BusinessException;
import com.stapar.parking.garage.repository.SectorRepository;
import com.stapar.parking.garage.repository.SessionRepository;
import com.stapar.parking.garage.repository.SpotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor // Lombok cria construtor para as injeções
public class ParkingService {

    private final SessionRepository sessionRepo;
    private final SectorRepository sectorRepo;
    private final SpotRepository spotRepo;

    @Transactional
    public void processEntry(String plate, LocalDateTime entryTime) {
        // 1. Verifica se já existe sessão ativa (Idempotência/Segurança)
        if (sessionRepo.findActiveByPlate(plate).isPresent()) {
            throw new BusinessException("Placa informada já está estacionada."); // Ou lançar exceção, dependendo da regra
        }

        // 2. Calcula Lotação Global
        long occupied = sessionRepo.countActiveSessions();
        Integer totalCapacity = sectorRepo.getTotalCapacity();
        if (totalCapacity == null || totalCapacity == 0) totalCapacity = 1; // Evita div por 0

        // Se cheio, lança exceção
        if (occupied >= totalCapacity) {
            throw new BusinessException("Garagem Lotada");
        }

        // 3. Calcula Preço Dinâmico
        double occupationPercentage = (double) occupied / totalCapacity;
        BigDecimal priceFactor = calculateDynamicFactor(occupationPercentage);

        // 4. Salva Sessão
        var session = ParkingSession.builder()
                .licensePlate(plate)
                .entryTime(entryTime)
                .status(SessionStatus.ENTRY)
                .priceFactor(priceFactor)
                .build();

        sessionRepo.save(session);
    }

    //Preço Dinâmico
    private BigDecimal calculateDynamicFactor(double percentage) {
        if (percentage <= 0.25) return new BigDecimal("0.90"); // -10%
        if (percentage <= 0.50) return new BigDecimal("1.00"); // 0%
        if (percentage <= 0.75) return new BigDecimal("1.10"); // +10%
        return new BigDecimal("1.25"); // +25% (até 100%)
    }

    @Transactional
    public void processParked(String plate, Double lat, Double lng) {
        var session = sessionRepo.findActiveByPlate(plate)
                .orElseThrow(() -> new BusinessException("Veículo não deu entrada: " + plate));

        var spot = spotRepo.findByLatAndLng(lat, lng)
                .orElseThrow(() -> new BusinessException("Vaga não encontrada nas coordenadas"));

        session.setAssignedSector(spot.getSectorCode());
        session.setStatus(SessionStatus.PARKED);
        sessionRepo.save(session);
    }

    @Transactional
    public void processExit(String plate, LocalDateTime exitTime) {
        var session = sessionRepo.findActiveByPlate(plate)
                .orElseThrow(() -> new BusinessException("Sessão não encontrada para: " + plate));

        var sector = sectorRepo.findByCode(session.getAssignedSector())
                .orElseThrow(() -> new BusinessException("Setor inválido"));

        BigDecimal finalPrice = calculatePrice(
                session.getEntryTime(),
                exitTime,
                sector.getBasePrice(),
                session.getPriceFactor()
        );

        session.setExitTime(exitTime);
        session.setFinalAmount(finalPrice);
        session.setStatus(SessionStatus.FINISHED);
        sessionRepo.save(session);
    }

    private BigDecimal calculatePrice(LocalDateTime start, LocalDateTime end, BigDecimal basePrice, BigDecimal factor) {
        long minutes = Duration.between(start, end).toMinutes();

        // Primeiros 30 min grátis
        if (minutes <= 30) {
            return BigDecimal.ZERO;
        }

        // Tarifa fixa por hora, arredondando para cima
        long hoursToCharge = (long) Math.ceil(minutes / 60.0);

        BigDecimal grossAmount = basePrice.multiply(new BigDecimal(hoursToCharge));

        return grossAmount.multiply(factor).setScale(2, RoundingMode.HALF_UP);
    }
}