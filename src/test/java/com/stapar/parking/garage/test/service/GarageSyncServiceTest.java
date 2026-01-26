package com.stapar.parking.garage.test.service;

import com.stapar.parking.garage.domain.Sector;
import com.stapar.parking.garage.domain.Spot;
import com.stapar.parking.garage.repository.SectorRepository;
import com.stapar.parking.garage.repository.SpotRepository;
import com.stapar.parking.garage.response.records.GarageConfigDTO;
import com.stapar.parking.garage.response.records.GarageResponse;
import com.stapar.parking.garage.response.records.SpotConfigDTO;
import com.stapar.parking.garage.service.GarageSyncService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GarageSyncServiceTest {

    @Mock
    private SectorRepository sectorRepo;

    @Mock
    private SpotRepository spotRepo;

    @Mock
    private RestClient.Builder restClientBuilder;

    @Mock
    private RestClient restClient;

    @Mock
    private RestClient.RequestHeadersUriSpec requestHeadersUriSpec;

    @Mock
    private RestClient.RequestHeadersSpec requestHeadersSpec;

    @Mock
    private RestClient.ResponseSpec responseSpec;

    private GarageSyncService garageSyncService;

    @BeforeEach
    void setUp() {
        when(restClientBuilder.baseUrl(anyString())).thenReturn(restClientBuilder);
        when(restClientBuilder.build()).thenReturn(restClient);

        garageSyncService = new GarageSyncService(sectorRepo, spotRepo, restClientBuilder);
    }

    @Test
    @DisplayName("Deve sincronizar com sucesso quando o simulador retorna dados válidos")
    void shouldSyncSuccessfully() {
        var garageData = List.of(new GarageConfigDTO("A", new BigDecimal("10.0"), 100));
        var spotsData = List.of(new SpotConfigDTO(1L, "A", -23.56, -46.65));
        var response = new GarageResponse(garageData, spotsData);

        when(restClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(GarageResponse.class)).thenReturn(response);

        garageSyncService.syncOnStartup();

        verify(spotRepo, times(1)).deleteAll();
        verify(sectorRepo, times(1)).deleteAll();

        verify(sectorRepo, times(1)).save(any(Sector.class));
        verify(spotRepo, times(1)).save(any(Spot.class));
    }

    @Test
    @DisplayName("Deve capturar exceção e não interromper o startup se o simulador falhar")
    void shouldHandleSimulatorErrorSilently() {
        when(restClient.get()).thenThrow(new RuntimeException("Connection Refused"));

        garageSyncService.syncOnStartup();

        verify(sectorRepo, never()).deleteAll();
        verify(sectorRepo, never()).save(any());
    }

    @Test
    @DisplayName("Não deve processar nada se o corpo da resposta for nulo")
    void shouldDoNothingIfResponseBodyIsNull() {
        when(restClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(GarageResponse.class)).thenReturn(null);

        garageSyncService.syncOnStartup();

        verify(sectorRepo, never()).deleteAll();
    }
}