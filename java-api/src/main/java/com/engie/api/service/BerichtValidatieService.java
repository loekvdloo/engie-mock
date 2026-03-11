package com.engie.api.service;

import com.engie.api.model.BerichtRequest;
import com.engie.api.model.ValidationFout;
import com.engie.api.model.ValidationFout.FoutType;
import com.engie.api.model.ValidationResultaat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Set;

/**
 * Voert alle technische validaties uit op een inkomend bericht.
 *
 * Alle checks refereren naar de foutcodes uit het document:
 * "Business Service Uitwisselen allocatiegegevens elektriciteit v4.0"
 */
@Service
public class BerichtValidatieService {

    private static final Logger log = LoggerFactory.getLogger(BerichtValidatieService.class);

    // Geldige ProcessTypeID-waarden
    private static final Set<String> GELDIGE_PROCESS_TYPES = Set.of(
            "A01", "A02", "A03", "A04", "A05", "A11", "A13", "Z01", "Z02", "Z03"
    );

    // Geldige energie-eenheden per productsoort
    private static final Set<String> GELDIGE_ENERGIE_EENHEDEN = Set.of(
            "KWH", "MWH", "KW", "MW"
    );

    // Geldige combinaties: herkomstindicatie → validatiestatus
    private static final Set<String> GELDIGE_HERKOMST_STATUS_COMBINATIES = Set.of(
            "A01-A01", "A01-A02", "A02-A01", "A02-A03", "A03-A01"
    );

    private final BerichtService berichtService;

    public BerichtValidatieService(BerichtService berichtService) {
        this.berichtService = berichtService;
    }

    /**
     * Voer alle validaties uit op het inkomende bericht.
     * Elke fout wordt gelogd met tijdstempel en foutcode.
     */
    public ValidationResultaat valideer(BerichtRequest request) {
        ValidationResultaat resultaat = new ValidationResultaat();

        // Parse XML eerst — zonder geldige XML kunnen andere checks niet
        Document doc = parseXml(request.getXmlPayload(), resultaat);

        // Validaties uitvoeren (ook als XML niet parseerbaar is, header-checks doen we altijd)
        check669_780_UniekeMessageId(doc, resultaat);
        check780_CorrelationIdConsistent(request, doc, resultaat);
        check650_777_EanCodes(request, doc, resultaat);
        check701_745_754_SoapHeaderConsistent(request, doc, resultaat);

        if (doc != null) {
            check663_PeriodeEenKalenderdag(doc, resultaat);
            check671_AantalPositiesKlopt(doc, resultaat);
            check676_782_PositieNummering(doc, resultaat);
            check776_686_Volume(doc, resultaat);
            check681_747_ProcessTypeId(doc, resultaat);
            check683_HerkomstCombinatie(doc, resultaat);
            check667_668_Producten(doc, resultaat);
            check670_769_UniekeKenmerken(doc, resultaat);
        }

        // Status bepalen
        if (!resultaat.heeftFouten()) {
            resultaat.setStatus(ValidationResultaat.Status.GOEDGEKEURD);
            log.info("[VALIDATIE] status=GOEDGEKEURD");
        } else if (resultaat.heeftSemanischeFouten()) {
            resultaat.setStatus(ValidationResultaat.Status.AFGEWEZEN);
            log.warn("[VALIDATIE] status=AFGEWEZEN fouten={}", resultaat.getFouten().size());
        } else {
            resultaat.setStatus(ValidationResultaat.Status.BEVESTIGD);
            log.warn("[VALIDATIE] status=BEVESTIGD (technische fouten) fouten={}", resultaat.getFouten().size());
        }

        return resultaat;
    }

    // ── Validatieregels ────────────────────────────────────────────────────────

    /**
     * Code 669/780 — MessageID en technicalMessageId zijn uniek en nog niet eerder verwerkt.
     */
    private void check669_780_UniekeMessageId(Document doc, ValidationResultaat resultaat) {
        if (doc == null) return;
        String messageId = xpathText(doc, "//*[local-name()='MessageID' or local-name()='mRID' or local-name()='identification']");
        if (messageId == null || messageId.isBlank()) {
            logEnVoegToe(resultaat, "669", "MessageID ontbreekt in het bericht.", FoutType.TECHNISCH);
            return;
        }
        if (berichtService.isBerichtIdAlVerwerkt(messageId)) {
            logEnVoegToe(resultaat, "669", "MessageID '" + messageId + "' is al eerder verwerkt (duplicaat).", FoutType.TECHNISCH);
        }
    }

    /**
     * Code 780 — CorrelationID in SOAP-header klopt met Business Document Header.
     */
    private void check780_CorrelationIdConsistent(BerichtRequest request, Document doc, ValidationResultaat resultaat) {
        String headerCorrelationId = request.getCorrelationId();
        if (headerCorrelationId == null || headerCorrelationId.isBlank()) return; // optioneel veld

        if (doc != null) {
            String xmlCorrelationId = xpathText(doc, "//*[local-name()='correlationID' or local-name()='correlationId']");
            if (xmlCorrelationId != null && !xmlCorrelationId.isBlank()
                    && !xmlCorrelationId.equals(headerCorrelationId)) {
                logEnVoegToe(resultaat, "780",
                        "CorrelationID in SOAP-header '" + headerCorrelationId
                                + "' wijkt af van XML '" + xmlCorrelationId + "'.", FoutType.TECHNISCH);
            }
        }
    }

    /**
     * Codes 650/777 — EAN-18 van aansluiting en netgebied is geldig (18 cijfers, geldig Luhn).
     */
    private void check650_777_EanCodes(BerichtRequest request, Document doc, ValidationResultaat resultaat) {
        // SOAP-header EAN's
        controleerEan(request.getSenderID(), "senderID", "650", resultaat);
        controleerEan(request.getReceiverID(), "receiverID", "777", resultaat);

        // EAN's in XML body
        if (doc != null) {
            NodeList eanNodes = xpathNodes(doc, "//*[local-name()='mRID' and string-length(text())=18]");
            for (int i = 0; i < eanNodes.getLength(); i++) {
                String ean = eanNodes.item(i).getTextContent().trim();
                if (!isGeldigEan18(ean)) {
                    logEnVoegToe(resultaat, "650",
                            "EAN-code '" + ean + "' is ongeldig (geen geldige EAN-18).", FoutType.SEMANTISCH);
                }
            }
        }
    }

    /**
     * Code 663 — Periode van dagbericht is exact één kalenderdag.
     */
    private void check663_PeriodeEenKalenderdag(Document doc, ValidationResultaat resultaat) {
        String start = xpathText(doc, "//*[local-name()='start']");
        String end   = xpathText(doc, "//*[local-name()='end']");
        if (start == null || end == null) return;
        try {
            LocalDate startDatum = LocalDate.parse(start.substring(0, 10));
            LocalDate eindDatum  = LocalDate.parse(end.substring(0, 10));
            if (!eindDatum.equals(startDatum.plusDays(1)) && !eindDatum.equals(startDatum)) {
                logEnVoegToe(resultaat, "663",
                        "Periode is geen exacte kalenderdag: start=" + start + " end=" + end, FoutType.SEMANTISCH);
            }
        } catch (DateTimeParseException | StringIndexOutOfBoundsException e) {
            logEnVoegToe(resultaat, "663", "Periode datumformaat ongeldig: " + e.getMessage(), FoutType.TECHNISCH);
        }
    }

    /**
     * Code 671 — Aantal posities in tijdserie klopt met periode en resolutie.
     * Bijv.: 15-min resolutie, 1 dag = 96 posities.
     */
    private void check671_AantalPositiesKlopt(Document doc, ValidationResultaat resultaat) {
        String resolutie = xpathText(doc, "//*[local-name()='resolution']");
        if (resolutie == null) return;

        NodeList punten = xpathNodes(doc, "//*[local-name()='Point']");
        int aantalPunten = punten.getLength();
        if (aantalPunten == 0) return;

        int verwacht = verwachtAantalPunten(resolutie);
        if (verwacht > 0 && aantalPunten != verwacht) {
            logEnVoegToe(resultaat, "671",
                    "Aantal posities " + aantalPunten + " klopt niet met resolutie "
                            + resolutie + " (verwacht: " + verwacht + ").", FoutType.SEMANTISCH);
        }
    }

    /**
     * Codes 676/782 — Eerste positie is '1' en increment van 1 wordt gehanteerd.
     */
    private void check676_782_PositieNummering(Document doc, ValidationResultaat resultaat) {
        NodeList posities = xpathNodes(doc, "//*[local-name()='position']");
        if (posities.getLength() == 0) return;

        String eerste = posities.item(0).getTextContent().trim();
        if (!"1".equals(eerste)) {
            logEnVoegToe(resultaat, "676",
                    "Eerste positie is '" + eerste + "', verwacht '1'.", FoutType.SEMANTISCH);
        }

        for (int i = 1; i < posities.getLength(); i++) {
            int vorige  = Integer.parseInt(posities.item(i - 1).getTextContent().trim());
            int huidig  = Integer.parseInt(posities.item(i).getTextContent().trim());
            if (huidig != vorige + 1) {
                logEnVoegToe(resultaat, "782",
                        "Positie-increment klopt niet: positie " + vorige + " gevolgd door " + huidig + ".", FoutType.SEMANTISCH);
                break;
            }
        }
    }

    /**
     * Codes 776/686 — Volume heeft correct aantal decimalen en is positief waar vereist.
     */
    private void check776_686_Volume(Document doc, ValidationResultaat resultaat) {
        NodeList volumes = xpathNodes(doc, "//*[local-name()='quantity' or local-name()='qty']");
        for (int i = 0; i < volumes.getLength(); i++) {
            String waarde = volumes.item(i).getTextContent().trim();
            try {
                double v = Double.parseDouble(waarde);
                if (v < 0) {
                    logEnVoegToe(resultaat, "686",
                            "Volume '" + waarde + "' is negatief, maar moet positief zijn.", FoutType.SEMANTISCH);
                }
                if (waarde.contains(".")) {
                    String decimalen = waarde.substring(waarde.indexOf('.') + 1);
                    if (decimalen.length() > 3) {
                        logEnVoegToe(resultaat, "776",
                                "Volume '" + waarde + "' heeft meer dan 3 decimalen.", FoutType.SEMANTISCH);
                    }
                }
            } catch (NumberFormatException e) {
                logEnVoegToe(resultaat, "776", "Volume '" + waarde + "' is geen geldig getal.", FoutType.TECHNISCH);
            }
        }
    }

    /**
     * Codes 681/747 — ProcessTypeID past bij inhoud en ontvanger van bericht.
     */
    private void check681_747_ProcessTypeId(Document doc, ValidationResultaat resultaat) {
        String processType = xpathText(doc, "//*[local-name()='process.processType' or local-name()='processType' or local-name()='processTypeID']");
        if (processType == null) return;
        String code = processType.trim().toUpperCase();
        if (!GELDIGE_PROCESS_TYPES.contains(code)) {
            logEnVoegToe(resultaat, "681",
                    "ProcessTypeID '" + code + "' is ongeldig of past niet bij de inhoud.", FoutType.SEMANTISCH);
        }
    }

    /**
     * Codes 701/745/754 — senderID, receiverID en contentType in SOAP-header zijn
     * consistent met Business Document Header.
     */
    private void check701_745_754_SoapHeaderConsistent(BerichtRequest request, Document doc, ValidationResultaat resultaat) {
        if (doc == null) return;
        String xmlSender   = xpathText(doc, "//*[local-name()='sender_MarketParticipant.mRID' or local-name()='senderMarketParticipant']");
        String xmlReceiver = xpathText(doc, "//*[local-name()='receiver_MarketParticipant.mRID' or local-name()='receiverMarketParticipant']");

        if (xmlSender != null && request.getSenderID() != null
                && !xmlSender.trim().equals(request.getSenderID().trim())) {
            logEnVoegToe(resultaat, "701",
                    "senderID in SOAP-header '" + request.getSenderID()
                            + "' wijkt af van XML '" + xmlSender + "'.", FoutType.TECHNISCH);
        }
        if (xmlReceiver != null && request.getReceiverID() != null
                && !xmlReceiver.trim().equals(request.getReceiverID().trim())) {
            logEnVoegToe(resultaat, "745",
                    "receiverID in SOAP-header '" + request.getReceiverID()
                            + "' wijkt af van XML '" + xmlReceiver + "'.", FoutType.TECHNISCH);
        }
    }

    /**
     * Code 683 — Combinatie van herkomstindicatie, validatiestatus en reparatiemethodiek is correct.
     */
    private void check683_HerkomstCombinatie(Document doc, ValidationResultaat resultaat) {
        String herkomst = xpathText(doc, "//*[local-name()='origin' or local-name()='originTypeCode']");
        String status   = xpathText(doc, "//*[local-name()='validationStatus' or local-name()='statusCode']");
        if (herkomst == null || status == null) return;

        String combinatie = herkomst.trim() + "-" + status.trim();
        if (!GELDIGE_HERKOMST_STATUS_COMBINATIES.contains(combinatie)) {
            logEnVoegToe(resultaat, "683",
                    "Combinatie herkomstindicatie '" + herkomst + "' en validatiestatus '"
                            + status + "' is niet toegestaan.", FoutType.SEMANTISCH);
        }
    }

    /**
     * Codes 667/668 — Alle producten passen bij de productsoort;
     * energie-eenheden passen bij producten.
     */
    private void check667_668_Producten(Document doc, ValidationResultaat resultaat) {
        NodeList eenheden = xpathNodes(doc, "//*[local-name()='measurement_Unit.name' or local-name()='measurementUnit']");
        for (int i = 0; i < eenheden.getLength(); i++) {
            String eenheid = eenheden.item(i).getTextContent().trim().toUpperCase();
            if (!GELDIGE_ENERGIE_EENHEDEN.contains(eenheid)) {
                logEnVoegToe(resultaat, "667",
                        "Energie-eenheid '" + eenheid + "' past niet bij het product.", FoutType.SEMANTISCH);
            }
        }
    }

    /**
     * Codes 670/769 — Het kenmerk van het bericht is uniek;
     * allocatierun identificatie is uniek.
     */
    private void check670_769_UniekeKenmerken(Document doc, ValidationResultaat resultaat) {
        String allocatieId = xpathText(doc, "//*[local-name()='allocatierunID' or local-name()='allocationRunID' or local-name()='runID']");
        if (allocatieId != null && !allocatieId.isBlank()) {
            if (berichtService.isAllocatieIdAlVerwerkt(allocatieId)) {
                logEnVoegToe(resultaat, "769",
                        "AllocatierunID '" + allocatieId + "' is al eerder verwerkt (duplicaat).", FoutType.SEMANTISCH);
            }
        }

        String kenmerk = xpathText(doc, "//*[local-name()='subject' or local-name()='berichtKenmerk' or local-name()='messageCharacteristic']");
        if (kenmerk != null && !kenmerk.isBlank()) {
            if (berichtService.isBerichtIdAlVerwerkt("kenmerk:" + kenmerk)) {
                logEnVoegToe(resultaat, "670",
                        "Berichtkenmerk '" + kenmerk + "' is al eerder gebruikt.", FoutType.SEMANTISCH);
            }
        }
    }

    // ── Hulpfuncties ───────────────────────────────────────────────────────────

    private Document parseXml(String xml, ValidationResultaat resultaat) {
        if (xml == null || xml.isBlank()) {
            logEnVoegToe(resultaat, "XML-001", "xmlPayload is leeg of ontbreekt.", FoutType.TECHNISCH);
            return null;
        }
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            return factory.newDocumentBuilder()
                    .parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            logEnVoegToe(resultaat, "XML-002", "xmlPayload bevat geen geldige XML: " + e.getMessage(), FoutType.TECHNISCH);
            return null;
        }
    }

    private String xpathText(Document doc, String expression) {
        try {
            XPath xpath = XPathFactory.newInstance().newXPath();
            String result = (String) xpath.evaluate(expression, doc, XPathConstants.STRING);
            return result == null || result.isBlank() ? null : result.trim();
        } catch (Exception e) {
            return null;
        }
    }

    private NodeList xpathNodes(Document doc, String expression) {
        try {
            XPath xpath = XPathFactory.newInstance().newXPath();
            return (NodeList) xpath.evaluate(expression, doc, XPathConstants.NODESET);
        } catch (Exception e) {
            return new org.w3c.dom.NodeList() {
                public org.w3c.dom.Node item(int index) { return null; }
                public int getLength() { return 0; }
            };
        }
    }

    private void controleerEan(String ean, String veld, String code, ValidationResultaat resultaat) {
        if (ean == null || ean.isBlank()) return;
        if (!isGeldigEan18(ean)) {
            logEnVoegToe(resultaat, code,
                    veld + " '" + ean + "' is geen geldige EAN-18 code.", FoutType.SEMANTISCH);
        }
    }

    /**
     * EAN-18 validatie: exact 18 cijfers + Luhn checksum.
     */
    boolean isGeldigEan18(String ean) {
        if (ean == null || !ean.matches("\\d{18}")) return false;
        int som = 0;
        for (int i = 0; i < 17; i++) {
            int cijfer = Character.getNumericValue(ean.charAt(i));
            som += (i % 2 == 0) ? cijfer : cijfer * 3;
        }
        int checkCijfer = (10 - (som % 10)) % 10;
        return checkCijfer == Character.getNumericValue(ean.charAt(17));
    }

    private int verwachtAantalPunten(String resolutie) {
        return switch (resolutie.toUpperCase().trim()) {
            case "PT15M" -> 96;
            case "PT30M" -> 48;
            case "PT60M", "PT1H" -> 24;
            default -> 0;
        };
    }

    private void logEnVoegToe(ValidationResultaat resultaat, String code, String omschrijving, FoutType type) {
        log.warn("[VALIDATIEFOUT] code={} type={} omschrijving=\"{}\" tijdstip={}",
                code, type, omschrijving, java.time.LocalDateTime.now());
        resultaat.voegFoutToe(code, omschrijving, type);
    }
}
