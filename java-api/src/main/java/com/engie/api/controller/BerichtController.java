package com.engie.api.controller;

import com.engie.api.model.Bericht;
import com.engie.api.model.BerichtRequest;
import com.engie.api.model.OntvangstBevestiging;
import com.engie.api.service.BerichtService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller voor het ontvangen en ophalen van berichten.
 *
 * Endpoints:
 *   POST   /api/berichten          → Ontvang een bericht (JSON met XML payload)
 *   GET    /api/berichten          → Haal alle ontvangen berichten op
 *   GET    /api/berichten/{id}     → Haal één bericht op via ID
 *   DELETE /api/berichten/{id}     → Verwijder een bericht via ID
 */
@RestController
@RequestMapping("/api/berichten")
public class BerichtController {

    private final BerichtService berichtService;

    public BerichtController(BerichtService berichtService) {
        this.berichtService = berichtService;
    }

    /**
     * Ontvang een bericht met XML payload.
     * Geeft direct een technische ontvangstbevestiging terug (202 Accepted).
     *
     * Voorbeeld request body:
     * {
     *   "afzender": "systeem-A",
     *   "onderwerp": "FactuurBericht",
     *   "xmlPayload": "<factuur><id>123</id><bedrag>99.99</bedrag></factuur>"
     * }
     */
    @PostMapping
    public ResponseEntity<OntvangstBevestiging> ontvangBericht(@RequestBody BerichtRequest request) {
        OntvangstBevestiging bevestiging = berichtService.ontvangBericht(request);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(bevestiging);
    }

    /**
     * Haal alle ontvangen berichten op (inclusief XML payload).
     */
    @GetMapping
    public ResponseEntity<List<Bericht>> getAlleBerichten() {
        List<Bericht> berichten = berichtService.getAlleBerichten();
        return ResponseEntity.ok(berichten);
    }

    /**
     * Haal één specifiek bericht op via zijn unieke ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Bericht> getBerichtById(@PathVariable String id) {
        return berichtService.getBerichtById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Verwijder een bericht via ID.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> verwijderBericht(@PathVariable String id) {
        boolean verwijderd = berichtService.verwijderBericht(id);
        return verwijderd
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }
}
