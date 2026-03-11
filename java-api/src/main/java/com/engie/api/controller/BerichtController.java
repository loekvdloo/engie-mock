package com.engie.api.controller;

import com.engie.api.model.Bericht;
import com.engie.api.model.BerichtRequest;
import com.engie.api.model.OntvangstBevestiging;
import com.engie.api.model.ValidationResultaat;
import com.engie.api.service.BerichtService;
import com.engie.api.service.BerichtValidatieService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/berichten")
public class BerichtController {

    private static final Logger log = LoggerFactory.getLogger(BerichtController.class);

    private final BerichtService berichtService;
    private final BerichtValidatieService validatieService;

    public BerichtController(BerichtService berichtService, BerichtValidatieService validatieService) {
        this.berichtService = berichtService;
        this.validatieService = validatieService;
    }

    @PostMapping
    public ResponseEntity<OntvangstBevestiging> ontvangBericht(@RequestBody BerichtRequest request) {
        log.info("[INKOMEND] senderID={} contentType={} tijdstip={}",
                request.getSenderID(), request.getContentType(), LocalDateTime.now());

        ValidationResultaat validatie = validatieService.valideer(request);

        if (validatie.getStatus() == ValidationResultaat.Status.AFGEWEZEN) {
            OntvangstBevestiging afwijzing = OntvangstBevestiging.builder()
                    .status("AFGEWEZEN")
                    .ontvangstTijd(LocalDateTime.now())
                    .toelichting("Bericht afgewezen wegens semantische fouten. Zie validatie voor details.")
                    .validatie(validatie)
                    .build();
            log.warn("[AFGEWEZEN] senderID={} fouten={}", request.getSenderID(), validatie.getFouten().size());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(afwijzing);
        }

        OntvangstBevestiging bevestiging = berichtService.ontvangBericht(request, validatie);

        if (validatie.heeftFouten()) {
            bevestiging.setToelichting("Bericht ontvangen met technische opmerkingen. Zie validatie voor details.");
            bevestiging.setValidatie(validatie);
        }

        log.info("[BEVESTIGD] berichtId={} status={}", bevestiging.getBerichtId(), validatie.getStatus());
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(bevestiging);
    }

    @GetMapping
    public ResponseEntity<List<Bericht>> getAlleBerichten() {
        return ResponseEntity.ok(berichtService.getAlleBerichten());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Bericht> getBerichtById(@PathVariable String id) {
        return berichtService.getBerichtById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> verwijderBericht(@PathVariable String id) {
        return berichtService.verwijderBericht(id)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }
}
