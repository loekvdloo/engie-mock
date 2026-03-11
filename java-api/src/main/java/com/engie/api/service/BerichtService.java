package com.engie.api.service;

import com.engie.api.model.Bericht;
import com.engie.api.model.BerichtRequest;
import com.engie.api.model.OntvangstBevestiging;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service die berichten in-memory opslaat en beheert.
 * Bij herstart van de applicatie worden berichten gewist.
 */
@Service
public class BerichtService {

    private final ConcurrentHashMap<String, Bericht> berichtenStore = new ConcurrentHashMap<>();

    /**
     * Verwerk een inkomend bericht en geef een technische ontvangstbevestiging terug.
     */
    public OntvangstBevestiging ontvangBericht(BerichtRequest request) {
        String berichtId = UUID.randomUUID().toString();
        LocalDateTime nu = LocalDateTime.now();

        Bericht bericht = Bericht.builder()
                .id(berichtId)
                .afzender(request.getAfzender())
                .onderwerp(request.getOnderwerp())
                .xmlPayload(request.getXmlPayload())
                .ontvangstTijd(nu)
                .status("ONTVANGEN")
                .build();

        berichtenStore.put(berichtId, bericht);

        return OntvangstBevestiging.builder()
                .berichtId(berichtId)
                .status("ONTVANGEN")
                .ontvangstTijd(nu)
                .toelichting("Bericht succesvol ontvangen en geregistreerd.")
                .build();
    }

    /**
     * Haal alle opgeslagen berichten op.
     */
    public List<Bericht> getAlleBerichten() {
        return new ArrayList<>(berichtenStore.values());
    }

    /**
     * Haal één bericht op via ID.
     */
    public Optional<Bericht> getBerichtById(String id) {
        return Optional.ofNullable(berichtenStore.get(id));
    }

    /**
     * Verwijder een bericht via ID.
     */
    public boolean verwijderBericht(String id) {
        return berichtenStore.remove(id) != null;
    }
}
