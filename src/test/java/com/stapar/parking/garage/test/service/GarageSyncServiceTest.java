package com.stapar.parking.garage.test.service;

import com.stapar.parking.garage.repository.SectorRepository;
import com.stapar.parking.garage.repository.SpotRepository;
import com.stapar.parking.garage.service.GarageSyncService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@SpringBootTest
@ActiveProfiles("test")
class GarageSyncServiceTest {

    @InjectMocks
    private GarageSyncService garageSyncService;

    @Mock private SectorRepository sectorRepo;
    @Mock private SpotRepository spotRepo;

    private RestClient.Builder restClientBuilder;
    private MockRestServiceServer mockServer;

    @BeforeEach
    void setUp() {
        mockServer = MockRestServiceServer.bindTo(restClientBuilder).build();
        spotRepo.deleteAll();
        sectorRepo.deleteAll();
    }

    @Test
    @DisplayName("Deve sincronizar setores e vagas com sucesso ao receber JSON do simulador")
    void shouldSyncSuccess() {
        String jsonResponse = """
            {
              "garage": [
                { "sector": "A", "basePrice": 10.0, "max_capacity": 100 }
              ],
              "spots": [
                { "id": 1, "sector": "A", "lat": -23.561684, "lng": -46.655981 }
              ]
            }
            """;

        mockServer.expect(requestTo("http://localhost:8080/garage"))
                .andRespond(withSuccess(jsonResponse, MediaType.APPLICATION_JSON));

        garageSyncService.syncOnStartup();

        assertThat(sectorRepo.count()).isEqualTo(1);
        assertThat(sectorRepo.findByCode("A")).isPresent();
        assertThat(sectorRepo.findByCode("A").get().getMaxCapacity()).isEqualTo(100);

        assertThat(spotRepo.count()).isEqualTo(1);
        assertThat(spotRepo.findByLatAndLng(-23.561684, -46.655981)).isPresent();

        mockServer.verify();
    }

    @Test
    @DisplayName("Deve tratar erro silenciosamente e logar quando o simulador falhar")
    void shouldHandleErrorWhenSimulatorIsDown() {
        // Configura o mock para retornar um erro 500
        mockServer.expect(requestTo("http://localhost:8080/garage"))
                .andRespond(withServerError());

        garageSyncService.syncOnStartup();

        assertThat(sectorRepo.count()).isZero();
        assertThat(spotRepo.count()).isZero();

        mockServer.verify();
    }

    @Test
    @DisplayName("Deve lidar com resposta vazia (null body) do simulador")
    void shouldHandleEmptyBody() {
        mockServer.expect(requestTo("http://localhost:8080/garage"))
                .andRespond(withSuccess("", MediaType.APPLICATION_JSON));

        garageSyncService.syncOnStartup();

        assertThat(sectorRepo.count()).isZero();
        mockServer.verify();
    }
}