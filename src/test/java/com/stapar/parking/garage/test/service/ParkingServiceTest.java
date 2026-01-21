package com.stapar.parking.garage.test.service;

import com.stapar.parking.garage.domain.ParkingSession;
import com.stapar.parking.garage.domain.Sector;
import com.stapar.parking.garage.domain.SessionStatus;
import com.stapar.parking.garage.repository.SectorRepository;
import com.stapar.parking.garage.repository.SessionRepository;
import com.stapar.parking.garage.repository.SpotRepository;
import com.stapar.parking.garage.service.ParkingService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ParkingServiceTest {

    @Mock private SessionRepository sessionRepo;
    @Mock private SectorRepository sectorRepo;
    @Mock private SpotRepository spotRepo;

    @InjectMocks
    private ParkingService service;

    @Test
    @DisplayName("Deve aplicar desconto de 10% quando lotação <= 25%")
    void shouldApplyDiscountWhenLowOccupancy() {
        when(sessionRepo.countActiveSessions()).thenReturn(20L);
        when(sectorRepo.getTotalCapacity()).thenReturn(100);

        service.processEntry("ABC-1234", LocalDateTime.now());

        verify(sessionRepo).save(argThat(session ->
                session.getPriceFactor().compareTo(new BigDecimal("0.90")) == 0
        ));
    }

    @Test
    @DisplayName("Deve aplicar aumento de 25% quando lotação > 75%")
    void shouldApplySurgePriceWhenHighOccupancy() {
        when(sessionRepo.countActiveSessions()).thenReturn(80L);
        when(sectorRepo.getTotalCapacity()).thenReturn(100);

        service.processEntry("ABC-9999", LocalDateTime.now());

        verify(sessionRepo).save(argThat(session ->
                session.getPriceFactor().compareTo(new BigDecimal("1.25")) == 0
        ));
    }

    @Test
    @DisplayName("Não deve permitir entrada se estacionamento lotado")
    void shouldBlockEntryWhenFull() {
        when(sessionRepo.countActiveSessions()).thenReturn(100L);
        when(sectorRepo.getTotalCapacity()).thenReturn(100);

        assertThatThrownBy(() -> service.processEntry("FULL-000", LocalDateTime.now()))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Garagem Lotada");

        verify(sessionRepo, never()).save(any());
    }

    @Test
    @DisplayName("Deve ser grátis se permanência for <= 30 minutos")
    void shouldBeFreeUnder30Minutes() {
        var entryTime = LocalDateTime.of(2025, 1, 1, 10, 0);
        var exitTime = LocalDateTime.of(2025, 1, 1, 10, 29);

        mockActiveSession("FREE-001", entryTime, "A", new BigDecimal("1.0"));
        mockSector("A", new BigDecimal("10.00"));

        service.processExit("FREE-001", exitTime);

        verify(sessionRepo).save(argThat(session ->
                session.getFinalAmount().compareTo(BigDecimal.ZERO) == 0 &&
                        session.getStatus() == SessionStatus.FINISHED
        ));
    }

    @Test
    @DisplayName("Deve cobrar 1 hora cheia se ficar 31 minutos (Preço Base)")
    void shouldChargeOneHourFor31Minutes() {
        var entryTime = LocalDateTime.of(2025, 1, 1, 10, 0);
        var exitTime = LocalDateTime.of(2025, 1, 1, 10, 31); // 31 min

        mockActiveSession("PAY-001", entryTime, "A", new BigDecimal("1.00"));
        mockSector("A", new BigDecimal("10.00"));

        service.processExit("PAY-001", exitTime);

        verify(sessionRepo).save(argThat(session ->
                session.getFinalAmount().compareTo(new BigDecimal("10.00")) == 0
        ));
    }

    @Test
    @DisplayName("Deve calcular corretamente: 2 Horas + Fator Dinâmico 1.25")
    void shouldCalculateComplexPrice() {
        var entryTime = LocalDateTime.of(2025, 1, 1, 10, 0);
        var exitTime = LocalDateTime.of(2025, 1, 1, 11, 5);

        mockActiveSession("RICH-001", entryTime, "B", new BigDecimal("1.25"));
        mockSector("B", new BigDecimal("20.00"));

        service.processExit("RICH-001", exitTime);

        verify(sessionRepo).save(argThat(session ->
                session.getFinalAmount().compareTo(new BigDecimal("50.00")) == 0
        ));
    }

    private void mockActiveSession(String plate, LocalDateTime entry, String sector, BigDecimal factor) {
        var session = ParkingSession.builder()
                .licensePlate(plate)
                .entryTime(entry)
                .assignedSector(sector)
                .priceFactor(factor)
                .status(SessionStatus.PARKED)
                .build();
        when(sessionRepo.findActiveByPlate(plate)).thenReturn(Optional.of(session));
    }

    private void mockSector(String code, BigDecimal price) {
        var sector = new Sector(1L, code, price, 100);
        when(sectorRepo.findByCode(code)).thenReturn(Optional.of(sector));
    }
}