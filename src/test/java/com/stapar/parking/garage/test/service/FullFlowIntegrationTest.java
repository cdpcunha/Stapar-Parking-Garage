package com.stapar.parking.garage.test.service;

import com.stapar.parking.garage.domain.Sector;
import com.stapar.parking.garage.domain.Spot;
import com.stapar.parking.garage.repository.SectorRepository;
import com.stapar.parking.garage.repository.SessionRepository;
import com.stapar.parking.garage.repository.SpotRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class FullFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SectorRepository sectorRepo;

    @Autowired
    private SpotRepository spotRepo;

    @Autowired
    private SessionRepository sessionRepo;

    @BeforeEach
    void setup() {
        sessionRepo.deleteAll();
        spotRepo.deleteAll();
        sectorRepo.deleteAll();
        sectorRepo.save(new Sector(null, "A", new BigDecimal("9.00"), 100));
        spotRepo.save(new Spot(1L, "A", -23.561684, -46.655981));
    }

    @Test
    @DisplayName("Deve executar o ciclo de vida completo de um veículo e calcular receita")
    void testCompleteParkingCycle() throws Exception {
        String entryJson = """
            {
                "license_plate": "TECH-2025",
                "entry_time": "2025-01-01T10:00:00",
                "event_type": "ENTRY"
            }
            """;

        mockMvc.perform(post("/webhook")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(entryJson))
                .andExpect(status().isOk());

        String parkedJson = """
            {
                "license_plate": "TECH-2025",
                "lat": -23.561684,
                "lng": -46.655981,
                "event_type": "PARKED"
            }
            """;

        mockMvc.perform(post("/webhook")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(parkedJson))
                .andExpect(status().isOk());

        String exitJson = """
            {
                "license_plate": "TECH-2025",
                "exit_time": "2025-01-01T12:05:00",
                "event_type": "EXIT"
            }
            """;

        mockMvc.perform(post("/webhook")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(exitJson))
                .andExpect(status().isOk());

        String revenueRequest = """
            {
                "date": "2025-01-01",
                "sector": "A"
            }
            """;

        mockMvc.perform(get("/revenue")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(revenueRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount").value(24.30))
                .andExpect(jsonPath("$.currency").value("BRL"));
    }
}