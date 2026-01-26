package com.stapar.parking.garage.controller;

import com.stapar.parking.garage.repository.SessionRepository;
import com.stapar.parking.garage.service.ParkingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Slf4j
@RestController
@RequiredArgsConstructor
public class WebhookController {

    private final ParkingService parkingService;
    private final SessionRepository sessionRepo;

    public record WebhookEvent(
            String event_type,
            String license_plate,
            LocalDateTime entry_time,
            LocalDateTime exit_time,
            Double lat,
            Double lng
    ) {}

    public record RevenueRequest(LocalDate date, String sector) {}
    public record RevenueResponse(BigDecimal amount, String currency, LocalDateTime timestamp) {}

    @PostMapping("/webhook")
    public ResponseEntity<Void> handleWebhook(@RequestBody WebhookEvent event) {
        log.info("Recebendo evento: {} para a placa: {}", event.event_type(), event.license_plate());
        switch (event.event_type()) {
            case "ENTRY" -> {
                log.info("Processando entrada do veículo: {}", event.license_plate());
                parkingService.processEntry(event.license_plate(), event.entry_time());
            }
            case "PARKED" -> {
                log.info("Veículo {} estacionado em Lat: {}, Lng: {}", event.license_plate(), event.lat(), event.lng());
                parkingService.processParked(event.license_plate(), event.lat(), event.lng());
            }
            case "EXIT" -> {
                log.info("Processando saída do veículo: {}", event.license_plate());
                parkingService.processExit(event.license_plate(), event.exit_time());
            }
            default -> {
                log.warn("Tipo de evento desconhecido recebido: {}", event.event_type());
                return ResponseEntity.badRequest().build();
            }
        }
        log.info("Evento {} processado com sucesso.", event.event_type());
        return ResponseEntity.ok().build();
    }


    @GetMapping("/revenue")
    public ResponseEntity<RevenueResponse> getRevenue(@RequestBody RevenueRequest request) {
        log.info("Consulta de faturamento: Setor {}, Data {}", request.sector(), request.date());

        LocalDateTime start = request.date().atStartOfDay();
        LocalDateTime end = request.date().atTime(23, 59, 59);

        BigDecimal total = sessionRepo.calculateRevenue(request.sector(), start, end);

        log.info("Faturamento calculado para {}: R$ {}", request.sector(), total);
        return ResponseEntity.ok(new RevenueResponse(total, "BRL", LocalDateTime.now()));
    }
}