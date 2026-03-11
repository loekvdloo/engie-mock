package com.engie.api.service;

import com.engie.api.model.Bericht;
import com.engie.api.model.BerichtRequest;
import com.engie.api.model.OntvangstBevestiging;
import com.engie.api.model.ValidationFout;
import com.engie.api.model.ValidationResultaat;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class BerichtServiceTest {

    @Autowired
    private BerichtService service;

    // ── ontvangBericht ────────────────────────────────────────────────────────

    @Test
    void ontvangBericht_geeftBevestigingTerug() {
        BerichtRequest request = maakRequest();

        OntvangstBevestiging bevestiging = service.ontvangBericht(request);

        assertNotNull(bevestiging.getBerichtId());
        assertEquals("ONTVANGEN", bevestiging.getStatus());
        assertNotNull(bevestiging.getOntvangstTijd());
        assertNotNull(bevestiging.getToelichting());
    }

    @Test
    void ontvangBericht_slaatBerichtOpInStore() {
        BerichtRequest request = maakRequest();

        OntvangstBevestiging bevestiging = service.ontvangBericht(request);
        Optional<Bericht> opgeslagen = service.getBerichtById(bevestiging.getBerichtId());

        assertTrue(opgeslagen.isPresent());
        assertEquals("systeem-A", opgeslagen.get().getAfzender());
        assertEquals("FactuurBericht", opgeslagen.get().getOnderwerp());
        assertEquals("<factuur><id>1</id></factuur>", opgeslagen.get().getXmlPayload());
        assertEquals("ONTVANGEN", opgeslagen.get().getStatus());
    }

    @Test
    void ontvangBericht_genereertUniekIdPerBericht() {
        BerichtRequest request = maakRequest();

        OntvangstBevestiging b1 = service.ontvangBericht(request);
        OntvangstBevestiging b2 = service.ontvangBericht(request);

        assertNotEquals(b1.getBerichtId(), b2.getBerichtId());
    }

    @Test
    void ontvangBericht_metAfgewezenValidatie_slaatStatusAfgewezenOp() {
        ValidationResultaat validatie = new ValidationResultaat();
        validatie.setStatus(ValidationResultaat.Status.AFGEWEZEN);
        validatie.voegFoutToe("650", "EAN ongeldig.", ValidationFout.FoutType.SEMANTISCH);

        OntvangstBevestiging bevestiging = service.ontvangBericht(maakRequest(), validatie);
        Optional<Bericht> opgeslagen = service.getBerichtById(bevestiging.getBerichtId());

        assertTrue(opgeslagen.isPresent());
        assertEquals("AFGEWEZEN", opgeslagen.get().getStatus());
    }

    @Test
    void ontvangBericht_metBevestigdValidatie_slaatStatusBevestigdOp() {
        ValidationResultaat validatie = new ValidationResultaat();
        validatie.setStatus(ValidationResultaat.Status.BEVESTIGD);
        validatie.voegFoutToe("669", "Duplicate MessageID.", ValidationFout.FoutType.TECHNISCH);

        OntvangstBevestiging bevestiging = service.ontvangBericht(maakRequest(), validatie);
        Optional<Bericht> opgeslagen = service.getBerichtById(bevestiging.getBerichtId());

        assertTrue(opgeslagen.isPresent());
        assertEquals("BEVESTIGD", opgeslagen.get().getStatus());
    }

    @Test
    void ontvangBericht_metGoedgekeurdValidatie_slaatStatusOntvangenOp() {
        ValidationResultaat validatie = new ValidationResultaat();
        validatie.setStatus(ValidationResultaat.Status.GOEDGEKEURD);

        OntvangstBevestiging bevestiging = service.ontvangBericht(maakRequest(), validatie);
        Optional<Bericht> opgeslagen = service.getBerichtById(bevestiging.getBerichtId());

        assertTrue(opgeslagen.isPresent());
        assertEquals("ONTVANGEN", opgeslagen.get().getStatus());
    }

    // ── getAlleBerichten ──────────────────────────────────────────────────────

    @Test
    void getAlleBerichten_leegBijStart() {
        List<Bericht> berichten = service.getAlleBerichten();
        assertTrue(berichten.isEmpty());
    }

    @Test
    void getAlleBerichten_bevatAlleOntvangen() {
        service.ontvangBericht(maakRequest());
        service.ontvangBericht(maakRequest());

        List<Bericht> berichten = service.getAlleBerichten();
        assertEquals(2, berichten.size());
    }

    // ── getBerichtById ────────────────────────────────────────────────────────

    @Test
    void getBerichtById_bestaandId_geeftBericht() {
        OntvangstBevestiging bevestiging = service.ontvangBericht(maakRequest());

        Optional<Bericht> bericht = service.getBerichtById(bevestiging.getBerichtId());

        assertTrue(bericht.isPresent());
    }

    @Test
    void getBerichtById_onbestaandId_geeftLeeg() {
        Optional<Bericht> bericht = service.getBerichtById("bestaat-niet");
        assertFalse(bericht.isPresent());
    }

    // ── verwijderBericht ──────────────────────────────────────────────────────

    @Test
    void verwijderBericht_bestaandId_verwijdertEnGeeftTrue() {
        OntvangstBevestiging bevestiging = service.ontvangBericht(maakRequest());
        String id = bevestiging.getBerichtId();

        boolean verwijderd = service.verwijderBericht(id);

        assertTrue(verwijderd);
        assertFalse(service.getBerichtById(id).isPresent());
    }

    @Test
    void verwijderBericht_onbestaandId_geeftFalse() {
        boolean verwijderd = service.verwijderBericht("bestaat-niet");
        assertFalse(verwijderd);
    }

    // ── helper ───────────────────────────────────────────────────────────────

    private BerichtRequest maakRequest() {
        BerichtRequest r = new BerichtRequest();
        r.setAfzender("systeem-A");
        r.setOnderwerp("FactuurBericht");
        r.setXmlPayload("<factuur><id>1</id></factuur>");
        return r;
    }

    // ── isBerichtIdAlVerwerkt ─────────────────────────────────────────────────

    @Test
    void isBerichtIdAlVerwerkt_eersteKeer_geeftFalse() {
        assertFalse(service.isBerichtIdAlVerwerkt("MSG-001"));
    }

    @Test
    void isBerichtIdAlVerwerkt_tweedeKeer_geeftTrue() {
        service.isBerichtIdAlVerwerkt("MSG-001");
        assertTrue(service.isBerichtIdAlVerwerkt("MSG-001"));
    }

    // ── isAllocatieIdAlVerwerkt ───────────────────────────────────────────────

    @Test
    void isAllocatieIdAlVerwerkt_eersteKeer_geeftFalse() {
        assertFalse(service.isAllocatieIdAlVerwerkt("ALLOC-001"));
    }

    @Test
    void isAllocatieIdAlVerwerkt_tweedeKeer_geeftTrue() {
        service.isAllocatieIdAlVerwerkt("ALLOC-001");
        assertTrue(service.isAllocatieIdAlVerwerkt("ALLOC-001"));
    }
}
