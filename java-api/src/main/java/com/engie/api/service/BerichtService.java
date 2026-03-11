package com.engie.api.service;

import com.engie.api.entity.BerichtEntity;
import com.engie.api.entity.BerichtLogEntity;
import com.engie.api.entity.ValidatieFoutEntity;
import com.engie.api.model.Bericht;
import com.engie.api.model.BerichtRequest;
import com.engie.api.model.OntvangstBevestiging;
import com.engie.api.model.ValidationFout;
import com.engie.api.model.ValidationResultaat;
import com.engie.api.repository.BerichtLogRepository;
import com.engie.api.repository.BerichtRepository;
import com.engie.api.repository.ValidatieFoutRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Service die berichten opslaat in de database en beheert.
 * Houdt ook bij welke MessageID's en allocatierun-ID's al verwerkt zijn.
 */
@Service
public class BerichtService {

    private static final Logger log = LoggerFactory.getLogger(BerichtService.class);

    private final BerichtRepository berichtRepository;
    private final ValidatieFoutRepository validatieFoutRepository;
    private final BerichtLogRepository berichtLogRepository;

    /** In-memory tracking voor validatie (per applicatie-run) */
    private final Set<String> verwerkteMeesageIds = ConcurrentHashMap.newKeySet();
    private final Set<String> verwerkteAllocatieIds = ConcurrentHashMap.newKeySet();

    /** Constructor voor productie/Spring DI */
    public BerichtService(BerichtRepository berichtRepository,
                          ValidatieFoutRepository validatieFoutRepository,
                          BerichtLogRepository berichtLogRepository) {
        this.berichtRepository = berichtRepository;
        this.validatieFoutRepository = validatieFoutRepository;
        this.berichtLogRepository = berichtLogRepository;
    }

    /** Constructor voor unit-tests zonder DB */
    BerichtService() {
        this.berichtRepository = null;
        this.validatieFoutRepository = null;
        this.berichtLogRepository = null;
    }

    /**
     * Verwerk een inkomend bericht en geef een technische ontvangstbevestiging terug.
     * Sla het bericht op inclusief validatiefouten en een log-entry.
     */
    @Transactional
    public OntvangstBevestiging ontvangBericht(BerichtRequest request) {
        return ontvangBericht(request, null);
    }

    @Transactional
    public OntvangstBevestiging ontvangBericht(BerichtRequest request, ValidationResultaat validatie) {
        String berichtId = UUID.randomUUID().toString();
        LocalDateTime nu = LocalDateTime.now();

        BerichtEntity entity = new BerichtEntity();
        entity.setId(berichtId);
        entity.setSenderEan(request.getSenderID() != null ? request.getSenderID() : request.getAfzender());
        entity.setReceiverEan(request.getReceiverID());
        entity.setContentType(request.getContentType() != null ? request.getContentType() : request.getOnderwerp());
        entity.setCorrelationId(request.getCorrelationId());
        entity.setXmlPayload(request.getXmlPayload() != null ? request.getXmlPayload() : "");
        entity.setOntvangstTijd(nu);
        entity.setStatus(validatie != null && validatie.heeftFouten() ? "BEVESTIGD" : "ONTVANGEN");

        // Sla validatiefouten op
        if (validatie != null) {
            for (ValidationFout fout : validatie.getFouten()) {
                ValidatieFoutEntity vf = new ValidatieFoutEntity();
                vf.setBericht(entity);
                vf.setFoutCode(fout.getFoutCode());
                vf.setOmschrijving(fout.getOmschrijving());
                vf.setFoutType(fout.getFoutType().name());
                vf.setTijdstip(fout.getTijdstip());
                entity.getValidatieFouten().add(vf);
            }
        }

        // Voeg log-entry toe
        String actie = (validatie != null && validatie.heeftFouten()) ? "VALIDATIE_TECHNISCH" : "ONTVANGEN";
        entity.getLogs().add(BerichtLogEntity.van(entity, actie,
                "Bericht ontvangen van " + entity.getSenderEan()));

        berichtRepository.save(entity);

        log.info("[ONTVANGST] berichtId={} senderID={} contentType={} tijdstip={}",
                berichtId, entity.getSenderEan(), entity.getContentType(), nu);

        return OntvangstBevestiging.builder()
                .berichtId(berichtId)
                .status("ONTVANGEN")
                .ontvangstTijd(nu)
                .toelichting("Bericht succesvol ontvangen en geregistreerd.")
                .build();
    }

    /** Controleer of een messageId al eerder verwerkt is (in-memory + DB) */
    public boolean isBerichtIdAlVerwerkt(String messageId) {
        if (!verwerkteMeesageIds.add(messageId)) return true;
        // Controleer ook in de DB (persistent over herstarten)
        if (berichtRepository != null) {
            return berichtRepository.existsByMessageId(messageId);
        }
        return false;
    }

    /** Controleer of een allocatierun-ID al eerder verwerkt is */
    public boolean isAllocatieIdAlVerwerkt(String allocatieId) {
        return !verwerkteAllocatieIds.add(allocatieId);
    }

    /** Haal alle opgeslagen berichten op. */
    public List<Bericht> getAlleBerichten() {
        return berichtRepository.findAllByOrderByOntvangstTijdDesc()
                .stream().map(this::naarBericht).collect(Collectors.toList());
    }

    /** Haal één bericht op via ID. */
    public Optional<Bericht> getBerichtById(String id) {
        return berichtRepository.findById(id).map(this::naarBericht);
    }

    /** Verwijder een bericht via ID. */
    @Transactional
    public boolean verwijderBericht(String id) {
        if (!berichtRepository.existsById(id)) return false;
        berichtRepository.deleteById(id);
        log.info("[VERWIJDERD] berichtId={}", id);
        return true;
    }

    /** Mapper: entity → DTO */
    private Bericht naarBericht(BerichtEntity e) {
        return Bericht.builder()
                .id(e.getId())
                .afzender(e.getSenderEan())
                .onderwerp(e.getContentType())
                .xmlPayload(e.getXmlPayload())
                .ontvangstTijd(e.getOntvangstTijd())
                .status(e.getStatus())
                .build();
    }
}

