package com.stapar.parking.garage.controller;

import com.stapar.parking.garage.repository.SessionRepository;
import com.stapar.parking.garage.service.ParkingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

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
        switch (event.event_type()) {
            case "ENTRY" -> parkingService.processEntry(event.license_plate(), event.entry_time());
            case "PARKED" -> parkingService.processParked(event.license_plate(), event.lat(), event.lng());
            case "EXIT" -> parkingService.processExit(event.license_plate(), event.exit_time());
            default -> { return ResponseEntity.badRequest().build(); }
        }
        return ResponseEntity.ok().build();
    }

    @GetMapping("/revenue")
    public ResponseEntity<RevenueResponse> getRevenue(@RequestBody RevenueRequest request) {
        // Define inicio e fim do dia
        LocalDateTime start = request.date().atStartOfDay();
        LocalDateTime end = request.date().atTime(23, 59, 59);

        BigDecimal total = sessionRepo.calculateRevenue(request.sector(), start, end);

        return ResponseEntity.ok(new RevenueResponse(total, "BRL", LocalDateTime.now()));
    }
}