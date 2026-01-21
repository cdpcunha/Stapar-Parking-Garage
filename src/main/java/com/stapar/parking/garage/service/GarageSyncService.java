package com.stapar.parking.garage.service;

import com.stapar.parking.garage.domain.Sector;
import com.stapar.parking.garage.domain.Spot;
import com.stapar.parking.garage.repository.SectorRepository;
import com.stapar.parking.garage.repository.SpotRepository;
import com.stapar.parking.garage.response.records.GarageResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

@Service
@Slf4j
public class GarageSyncService {

    private final SectorRepository sectorRepo;
    private final SpotRepository spotRepo;
    private final RestClient restClient;

    public GarageSyncService(SectorRepository sectorRepo,
                             SpotRepository spotRepo,
                             RestClient.Builder builder) {
        this.sectorRepo = sectorRepo;
        this.spotRepo = spotRepo;
        this.restClient = builder.baseUrl("http://localhost:8080").build();
    }

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void syncOnStartup() {
        try {
            GarageResponse response = restClient.get()
                    .uri("/garage")
                    .retrieve()
                    .body(GarageResponse.class);

            if (response != null) {
                spotRepo.deleteAll();
                sectorRepo.deleteAll();

                response.garage().forEach(g -> {
                    var sector = new Sector(null, g.sector(), g.basePrice(), g.max_capacity());
                    sectorRepo.save(sector);
                });

                response.spots().forEach(s -> {
                    var spot = new Spot(s.id(), s.sector(), s.lat(), s.lng());
                    spotRepo.save(spot);
                });
                log.info("Sincronização com simulador finalizada com sucesso.");
            }
        } catch (Exception e) {
            log.error("Erro ao sincronizar com simulador: {}", e.getMessage());
        }
    }
}