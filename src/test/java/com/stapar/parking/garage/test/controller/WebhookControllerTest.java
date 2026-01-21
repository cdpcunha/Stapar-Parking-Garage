package com.stapar.parking.garage.test.controller;

import com.stapar.parking.garage.controller.WebhookController;
import com.stapar.parking.garage.repository.SessionRepository;
import com.stapar.parking.garage.service.ParkingService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(WebhookController.class)
class WebhookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ParkingService parkingService;

    @MockitoBean
    private SessionRepository sessionRepo;

    @Test
    @DisplayName("Deve processar evento ENTRY com sucesso")
    void shouldProcessEntry() throws Exception {
        var event = new WebhookController.WebhookEvent(
                "ENTRY", "ABC-1234", LocalDateTime.now(), null, null, null);

        mockMvc.perform(post("/webhook")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(event)))
                .andExpect(status().isOk());

        verify(parkingService).processEntry(eq("ABC-1234"), any());
    }

    @Test
    @DisplayName("Deve processar evento PARKED com sucesso")
    void shouldProcessParked() throws Exception {
        var event = new WebhookController.WebhookEvent(
                "PARKED", "ABC-1234", null, null, -23.56, -46.65);

        mockMvc.perform(post("/webhook")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(event)))
                .andExpect(status().isOk());

        verify(parkingService).processParked(eq("ABC-1234"), eq(-23.56), eq(-46.65));
    }

    @Test
    @DisplayName("Deve processar evento EXIT com sucesso")
    void shouldProcessExit() throws Exception {
        var event = new WebhookController.WebhookEvent(
                "EXIT", "ABC-1234", null, LocalDateTime.now(), null, null);

        mockMvc.perform(post("/webhook")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(event)))
                .andExpect(status().isOk());

        verify(parkingService).processExit(eq("ABC-1234"), any());
    }

    @Test
    @DisplayName("Deve retornar 400 para evento desconhecido")
    void shouldReturn400ForUnknownEvent() throws Exception {
        var event = new WebhookController.WebhookEvent(
                "INVALID", "ABC-1234", null, null, null, null);

        mockMvc.perform(post("/webhook")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(event)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Deve retornar receita por setor e data")
    void shouldReturnRevenue() throws Exception {
        LocalDate date = LocalDate.of(2025, 1, 1);
        when(sessionRepo.calculateRevenue(eq("A"), any(), any()))
                .thenReturn(new BigDecimal("150.00"));

        var request = new WebhookController.RevenueRequest(date, "A");

        mockMvc.perform(get("/revenue")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount").value(150.00))
                .andExpect(jsonPath("$.currency").value("BRL"));
    }
}