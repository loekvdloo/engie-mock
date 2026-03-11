package com.engie.api.service;

import com.engie.api.model.Bericht;
import com.engie.api.model.BerichtRequest;
import com.engie.api.model.OntvangstBevestiging;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service die berichten in-memory opslaat en beheert.
 * Houdt ook bij welke MessageID's en allocatierun-ID's al verwerkt zijn.
 */
@Service
public class BerichtService {

    private static final Logger log = LoggerFactory.getLogger(BerichtService.class);

    private final ConcurrentHashMap<String, Bericht> berichtenStore = new ConcurrentHashMap<>();

    /** Bijhouden van reeds verwerkte berichtID's (TEN-500023, Code 669) */
    private final Set<String> verwerkteMeesageIds = ConcurrentHashMap.newKeySet();

    /** Bijhouden van reeds verwerkte allocatierun-ID's (Code 769) */
    private final Set<String> verwerkteAllocatieIds = ConcurrentHashMap.newKeySet();

    /**
     * Verwerk een inkomend bericht en geef een technische ontvangstbevestiging terug.
     */
    public OntvangstBevestiging ontvangBericht(BerichtRequest request) {
        String berichtId = UUID.randomUUID().toString();
        LocalDateTime nu = LocalDateTime.now();

        Bericht bericht = Bericht.builder()
                .id(berichtId)
                .afzender(request.getSenderID())
                .onderwerp(request.getContentType())
                .xmlPayload(request.getXmlPayload())
                .ontvangstTijd(nu)
                .status("ONTVANGEN")
                .build();

        berichtenStore.put(berichtId, bericht);
        log.info("[ONTVANGST] berichtId={} senderID={} contentType={} tijdstip={}",
                berichtId, request.getSenderID(), request.getContentType(), nu);

        return OntvangstBevestiging.builder()
                .berichtId(berichtId)
                .status("ONTVANGEN")
                .ontvangstTijd(nu)
                .toelichting("Bericht succesvol ontvangen en geregistreerd.")
                .build();
    }

    /** Controleer of een messageId al eerder verwerkt is */
    public boolean isBerichtIdAlVerwerkt(String messageId) {
        return !verwerkteMeesageIds.add(messageId);
    }

    /** Controleer of een allocatierun-ID al eerder verwerkt is */
    public boolean isAllocatieIdAlVerwerkt(String allocatieId) {
        return !verwerkteAllocatieIds.add(allocatieId);
    }

    /** Haal alle opgeslagen berichten op. */
    public List<Bericht> getAlleBerichten() {
        return new ArrayList<>(berichtenStore.values());
    }

    /** Haal één bericht op via ID. */
    public Optional<Bericht> getBerichtById(String id) {
        return Optional.ofNullable(berichtenStore.get(id));
    }

    /** Verwijder een bericht via ID. */
    public boolean verwijderBericht(String id) {
        return berichtenStore.remove(id) != null;
    }
}

