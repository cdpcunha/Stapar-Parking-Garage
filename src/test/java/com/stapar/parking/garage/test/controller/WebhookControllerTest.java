package com.stapar.parking.garage.test.controller;


import com.stapar.parking.garage.repository.SessionRepository;
import com.stapar.parking.garage.service.ParkingService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@ExtendWith(MockitoExtension.class)
class WebhookControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private ParkingService parkingService; // Mocka o serviço
    @Mock
    private SessionRepository sessionRepo; // Mocka o repo usado no controller

    @Test
    @DisplayName("Deve aceitar evento ENTRY e retornar 200")
    void shouldAcceptEntryEvent() throws Exception {
        var json = """
            {
              "license_plate": "ABC-1234",
              "entry_time": "2025-01-01T12:00:00",
              "event_type": "ENTRY"
            }
        """;

        mockMvc.perform(post("/webhook")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Deve retornar erro 400 para evento inválido")
    void shouldReturnErrorOnInvalidEvent() throws Exception {
        var json = """
            { "event_type": "INVALIDO" }
        """;

        mockMvc.perform(post("/webhook")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /revenue deve retornar faturamento formatado")
    void shouldReturnRevenue() throws Exception {
        // Mock do retorno do repositório
        given(sessionRepo.calculateRevenue(any(), any(), any()))
                .willReturn(new BigDecimal("150.50"));

        var requestJson = """
            {
              "date": "2025-01-01",
              "sector": "A"
            }
        """;

        mockMvc.perform(get("/revenue")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount").value(150.50))
                .andExpect(jsonPath("$.currency").value("BRL"));
    }
}