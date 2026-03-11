package com.engie.api.service;

import com.engie.api.model.BerichtRequest;
import com.engie.api.model.ValidationFout;
import com.engie.api.model.ValidationResultaat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BerichtValidatieServiceTest {

    private BerichtValidatieService validatieService;
    private BerichtService berichtService;

    @BeforeEach
    void setUp() {
        berichtService = new BerichtService();
        validatieService = new BerichtValidatieService(berichtService);
    }

    // ── Geldige berichten ─────────────────────────────────────────────────────

    @Test
    void geldigBericht_geeftGoedgekeurd() {
        ValidationResultaat resultaat = validatieService.valideer(geldigRequest());
        assertEquals(ValidationResultaat.Status.GOEDGEKEURD, resultaat.getStatus());
        assertTrue(resultaat.getFouten().isEmpty());
    }

    // ── XML validatie ─────────────────────────────────────────────────────────

    @Test
    void legeXmlPayload_geeftTechnischeFout() {
        BerichtRequest request = geldigRequest();
        request.setXmlPayload("");

        ValidationResultaat resultaat = validatieService.valideer(request);

        assertTrue(bevatFoutCode(resultaat, "XML-001"));
        assertEquals(ValidationResultaat.Status.BEVESTIGD, resultaat.getStatus());
    }

    @Test
    void ongedigeXml_geeftTechnischeFout() {
        BerichtRequest request = geldigRequest();
        request.setXmlPayload("<niet-gesloten>");

        ValidationResultaat resultaat = validatieService.valideer(request);

        assertTrue(bevatFoutCode(resultaat, "XML-002"));
    }

    // ── Code 663 — Periode ────────────────────────────────────────────────────

    @Test
    void periodeNietEenKalenderdag_geeftFout663() {
        String xml = """
                <root>
                  <start>2026-03-11T00:00:00Z</start>
                  <end>2026-03-13T00:00:00Z</end>
                </root>
                """;
        BerichtRequest request = geldigRequest();
        request.setXmlPayload(xml);

        ValidationResultaat resultaat = validatieService.valideer(request);

        assertTrue(bevatFoutCode(resultaat, "663"));
        assertEquals(ValidationFout.FoutType.SEMANTISCH, getFout(resultaat, "663").getFoutType());
    }

    @Test
    void periodeEenKalenderdag_geeftGeenFout663() {
        String xml = """
                <root>
                  <start>2026-03-11T00:00:00Z</start>
                  <end>2026-03-12T00:00:00Z</end>
                </root>
                """;
        BerichtRequest request = geldigRequest();
        request.setXmlPayload(xml);

        ValidationResultaat resultaat = validatieService.valideer(request);

        assertFalse(bevatFoutCode(resultaat, "663"));
    }

    // ── Code 671 — Aantal posities ────────────────────────────────────────────

    @Test
    void verkeerAantalPosities_geeftFout671() {
        String xml = """
                <TimeSeries>
                  <resolution>PT15M</resolution>
                  <Point><position>1</position><quantity>100</quantity></Point>
                  <Point><position>2</position><quantity>200</quantity></Point>
                </TimeSeries>
                """;
        BerichtRequest request = geldigRequest();
        request.setXmlPayload(xml);

        ValidationResultaat resultaat = validatieService.valideer(request);

        assertTrue(bevatFoutCode(resultaat, "671"));
    }

    // ── Codes 676/782 — Positienummering ─────────────────────────────────────

    @Test
    void eerstePositieNietEen_geeftFout676() {
        String xml = """
                <TimeSeries>
                  <Point><position>2</position></Point>
                  <Point><position>3</position></Point>
                </TimeSeries>
                """;
        BerichtRequest request = geldigRequest();
        request.setXmlPayload(xml);

        ValidationResultaat resultaat = validatieService.valideer(request);

        assertTrue(bevatFoutCode(resultaat, "676"));
    }

    @Test
    void positieIncrementOnjuist_geeftFout782() {
        String xml = """
                <TimeSeries>
                  <Point><position>1</position></Point>
                  <Point><position>3</position></Point>
                </TimeSeries>
                """;
        BerichtRequest request = geldigRequest();
        request.setXmlPayload(xml);

        ValidationResultaat resultaat = validatieService.valideer(request);

        assertTrue(bevatFoutCode(resultaat, "782"));
    }

    // ── Codes 776/686 — Volume ────────────────────────────────────────────────

    @Test
    void negatiefVolume_geeftFout686() {
        String xml = "<root><quantity>-10</quantity></root>";
        BerichtRequest request = geldigRequest();
        request.setXmlPayload(xml);

        ValidationResultaat resultaat = validatieService.valideer(request);

        assertTrue(bevatFoutCode(resultaat, "686"));
    }

    @Test
    void teVeelDecimalen_geeftFout776() {
        String xml = "<root><quantity>10.12345</quantity></root>";
        BerichtRequest request = geldigRequest();
        request.setXmlPayload(xml);

        ValidationResultaat resultaat = validatieService.valideer(request);

        assertTrue(bevatFoutCode(resultaat, "776"));
    }

    @Test
    void geldigVolume_geeftGeenFout() {
        String xml = "<root><quantity>10.123</quantity></root>";
        BerichtRequest request = geldigRequest();
        request.setXmlPayload(xml);

        ValidationResultaat resultaat = validatieService.valideer(request);

        assertFalse(bevatFoutCode(resultaat, "776"));
        assertFalse(bevatFoutCode(resultaat, "686"));
    }

    // ── Codes 681/747 — ProcessTypeID ────────────────────────────────────────

    @Test
    void ongedigProcessType_geeftFout681() {
        String xml = "<root><processType>ONBEKEND</processType></root>";
        BerichtRequest request = geldigRequest();
        request.setXmlPayload(xml);

        ValidationResultaat resultaat = validatieService.valideer(request);

        assertTrue(bevatFoutCode(resultaat, "681"));
    }

    @Test
    void geldigProcessType_geeftGeenFout681() {
        String xml = "<root><processType>A01</processType></root>";
        BerichtRequest request = geldigRequest();
        request.setXmlPayload(xml);

        ValidationResultaat resultaat = validatieService.valideer(request);

        assertFalse(bevatFoutCode(resultaat, "681"));
    }

    // ── Code 669 — Uniek MessageID ────────────────────────────────────────────

    @Test
    void dubbeleMessageId_geeftFout669() {
        String xml = "<root><mRID>MSG-DUBBEL-001</mRID></root>";
        BerichtRequest request = geldigRequest();
        request.setXmlPayload(xml);

        // Eerste keer OK
        validatieService.valideer(request);
        // Tweede keer duplicaat
        ValidationResultaat resultaat = validatieService.valideer(request);

        assertTrue(bevatFoutCode(resultaat, "669"));
    }

    // ── Codes 650/777 — EAN-18 ───────────────────────────────────────────────

    @Test
    void ongedigEan_geeftFout650() {
        BerichtRequest request = geldigRequest();
        request.setSenderID("123"); // te kort, ongeldig EAN

        ValidationResultaat resultaat = validatieService.valideer(request);

        assertTrue(bevatFoutCode(resultaat, "650"));
    }

    @Test
    void isGeldigEan18_correcteCode_geeftTrue() {
        // 871686700000900000 is een echt geldige EAN-18 testwaarde
        assertTrue(validatieService.isGeldigEan18("871686700000900000"));
    }

    @Test
    void isGeldigEan18_teKort_geeftFalse() {
        assertFalse(validatieService.isGeldigEan18("12345"));
    }

    @Test
    void isGeldigEan18_letters_geeftFalse() {
        assertFalse(validatieService.isGeldigEan18("87168670000090ABCD"));
    }

    // ── Codes 667/668 — Energie-eenheden ─────────────────────────────────────

    @Test
    void ongedigeEenhied_geeftFout667() {
        String xml = "<root><measurementUnit>POUNDS</measurementUnit></root>";
        BerichtRequest request = geldigRequest();
        request.setXmlPayload(xml);

        ValidationResultaat resultaat = validatieService.valideer(request);

        assertTrue(bevatFoutCode(resultaat, "667"));
    }

    @Test
    void geldigeEenheid_geeftGeenFout667() {
        String xml = "<root><measurementUnit>KWH</measurementUnit></root>";
        BerichtRequest request = geldigRequest();
        request.setXmlPayload(xml);

        ValidationResultaat resultaat = validatieService.valideer(request);

        assertFalse(bevatFoutCode(resultaat, "667"));
    }

    // ── Semantische fouten leiden tot AFGEWEZEN ───────────────────────────────

    @Test
    void semantischeFout_leertTotAfgewezen() {
        String xml = "<root><processType>ONBEKEND</processType></root>";
        BerichtRequest request = geldigRequest();
        request.setXmlPayload(xml);

        ValidationResultaat resultaat = validatieService.valideer(request);

        assertEquals(ValidationResultaat.Status.AFGEWEZEN, resultaat.getStatus());
    }

    // ── Hulpfuncties ──────────────────────────────────────────────────────────

    private BerichtRequest geldigRequest() {
        BerichtRequest r = new BerichtRequest();
        r.setSenderID("871686700000900000");
        r.setReceiverID("871686700001600000");
        r.setContentType("AllocationResult");
        r.setCorrelationId("MSG-20260311-001");
        r.setXmlPayload("<AllocationResult><mRID>MSG-UNIEK-" + System.nanoTime() + "</mRID></AllocationResult>");
        return r;
    }

    private boolean bevatFoutCode(ValidationResultaat resultaat, String code) {
        return resultaat.getFouten().stream().anyMatch(f -> code.equals(f.getFoutCode()));
    }

    private ValidationFout getFout(ValidationResultaat resultaat, String code) {
        return resultaat.getFouten().stream()
                .filter(f -> code.equals(f.getFoutCode()))
                .findFirst()
                .orElseThrow();
    }
}
