# Engie Bericht API

![Java](https://img.shields.io/badge/Java-25-orange?logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.3-brightgreen?logo=springboot)
![Maven](https://img.shields.io/badge/Maven-3.9.6-blue?logo=apachemaven)
![Tests](https://img.shields.io/badge/Tests-43%20passing-success?logo=junit5)

> REST API voor het ontvangen van berichten (JSON met XML payload) en het retourneren van een technische ontvangstbevestiging.

---

## Vereisten

| Tool | Versie |
|------|--------|
| Java | 21 of hoger (getest op 25) |
| Maven | Niet nodig — wordt automatisch gedownload via `run.ps1` |

---

## Applicatie starten

```powershell
cd java-api
powershell -ExecutionPolicy Bypass -File ".\run.ps1"
```

De eerste keer downloadt het script Maven automatisch naar `~\.m2\`. Daarna start de applicatie op:

```
http://localhost:8080
```

---

## Testen uitvoeren

```powershell
cd java-api
powershell -ExecutionPolicy Bypass -File ".\test.ps1"
```

Verwacht resultaat:

```
Tests run: 9  -- BerichtControllerTest        (alle geslaagd)
Tests run: 13 -- BerichtServiceTest           (alle geslaagd)
Tests run: 21 -- BerichtValidatieServiceTest  (alle geslaagd)
Tests run: 43, Failures: 0, Errors: 0
BUILD SUCCESS
```

---

## Endpoints

| Methode | URL | Omschrijving | Response |
|---------|-----|--------------|----------|
| `POST` | `/api/berichten` | Stuur een bericht met XML payload + validatie | `202 Accepted` (OK/technisch) of `400 Bad Request` (semantisch) |
| `GET` | `/api/berichten` | Haal alle ontvangen berichten op | `200 OK` + lijst |
| `GET` | `/api/berichten/{id}` | Haal één bericht op via ID | `200 OK` of `404` |
| `DELETE` | `/api/berichten/{id}` | Verwijder een bericht | `204 No Content` of `404` |

---

## Postman voorbeelden

### POST — bericht sturen

**URL:** `POST http://localhost:8080/api/berichten`  
**Headers:** `Content-Type: application/json`  
**Body:**

```json
{
  "senderID":     "871686700000900000",
  "receiverID":   "871686700001600000",
  "contentType":  "AllocationResult",
  "correlationId": "MSG-20260311-001",
  "xmlPayload":   "<AllocationResult><mRID>MSG-001</mRID></AllocationResult>"
}
```

> Backwards-compatibel: `afzender`, `onderwerp` kunnen ook nog worden meegestuurd.

**Response `202 Accepted` (GOEDGEKEURD):**

```json
{
  "berichtId": "a3f7c2d1-9e4b-4a1f-8b2e-123456789abc",
  "status": "ONTVANGEN",
  "ontvangstTijd": "2026-03-11T08:53:11",
  "toelichting": "Bericht succesvol ontvangen en geregistreerd.",
  "validatie": {
    "status": "GOEDGEKEURD",
    "fouten": []
  }
}
```

**Response `202 Accepted` (BEVESTIGD — technische opmerkingen):**

```json
{
  "status": "ONTVANGEN",
  "toelichting": "Bericht ontvangen met technische opmerkingen. Zie validatie voor details.",
  "validatie": {
    "status": "BEVESTIGD",
    "fouten": [
      {
        "foutCode": "669",
        "omschrijving": "MessageID ontbreekt in het bericht.",
        "foutType": "TECHNISCH",
        "tijdstip": "2026-03-11T08:53:11"
      }
    ]
  }
}
```

**Response `400 Bad Request` (AFGEWEZEN — semantische fout):**

```json
{
  "status": "AFGEWEZEN",
  "toelichting": "Bericht afgewezen wegens semantische fouten. Zie validatie voor details.",
  "validatie": {
    "status": "AFGEWEZEN",
    "fouten": [
      {
        "foutCode": "681",
        "omschrijving": "ProcessTypeID 'ONBEKEND' is ongeldig.",
        "foutType": "SEMANTISCH"
      }
    ]
  }
}
```

### GET — alle berichten ophalen

**URL:** `GET http://localhost:8080/api/berichten`

### GET — één bericht ophalen

**URL:** `GET http://localhost:8080/api/berichten/{berichtId}`

### DELETE — bericht verwijderen

**URL:** `DELETE http://localhost:8080/api/berichten/{berichtId}`

---

## Technische validatie

Het `POST /api/berichten` endpoint voert automatisch alle technische validaties uit per de spec *Business Service Uitwisselen allocatiegegevens elektriciteit v4.0*:

| Code | Type | Omschrijving |
|------|------|--------------|
| XML-001 | TECHNISCH | XML payload is leeg of ontbreekt |
| XML-002 | TECHNISCH | XML is syntactisch ongeldig |
| 669 | TECHNISCH | MessageID is duplicaat of ontbreekt |
| 780 | TECHNISCH | CorrelationID in SOAP-header wijkt af van XML |
| 650 | SEMANTISCH | senderID is geen geldige EAN-18 |
| 777 | SEMANTISCH | EAN-codes in XML zijn ongeldig |
| 663 | SEMANTISCH | Periode is geen exacte kalenderdag |
| 671 | SEMANTISCH | Aantal posities klopt niet met resolutie |
| 676 | SEMANTISCH | Eerste positie is niet 1 |
| 782 | SEMANTISCH | Positie-increment is niet 1 |
| 686 | SEMANTISCH | Volume is negatief |
| 776 | SEMANTISCH | Volume heeft meer dan 3 decimalen |
| 681/747 | SEMANTISCH | ProcessTypeID is ongeldig of past niet bij inhoud/ontvanger |
| 701/745/754 | SEMANTISCH | SOAP-header senderID/receiverID wijkt af van XML |
| 683 | SEMANTISCH | Herkomst/status-combinatie is ongeldig |
| 667/668 | SEMANTISCH | Energie-eenheid (measurementUnit) is ongeldig |
| 670/769 | SEMANTISCH | allocatierunID of berichtKenmerk is niet uniek |

**Validatiestatus:**
- `GOEDGEKEURD` → geen fouten gevonden → `202 Accepted`
- `BEVESTIGD` → alleen technische fouten → `202 Accepted` + foutdetails
- `AFGEWEZEN` → semantische fouten gevonden → `400 Bad Request`

---

## Projectstructuur

```
java-api/
├── pom.xml
├── run.ps1                                              ← Start-script (downloadt Maven automatisch)
└── src/
    ├── main/java/com/engie/api/
    │   ├── EngieBerichtApiApplication.java              ← Spring Boot entry point
    │   ├── controller/
    │   │   └── BerichtController.java                   ← REST endpoints + validatieflow
    │   ├── model/
    │   │   ├── BerichtRequest.java                      ← JSON input (SOAP-header velden + xmlPayload)
    │   │   ├── Bericht.java                             ← Opgeslagen bericht
    │   │   ├── OntvangstBevestiging.java                ← Technische bevestiging response
    │   │   ├── ValidationFout.java                      ← Enkelvoudige validatiefout (code, type)
    │   │   └── ValidationResultaat.java                 ← Validatieresultaat (GOEDGEKEURD/BEVESTIGD/AFGEWEZEN)
    │   └── service/
    │       ├── BerichtService.java                      ← In-memory opslag + ID-tracking
    │       └── BerichtValidatieService.java             ← 15+ validatieregels per spec
    └── test/java/com/engie/api/
        ├── controller/
        │   └── BerichtControllerTest.java               ← 9 controller testen (MockMvc)
        └── service/
            ├── BerichtServiceTest.java                  ← 13 service testen (unit)
            └── BerichtValidatieServiceTest.java         ← 21 validatie testen (unit)
```

---

## Technische details

- **Framework:** Spring Boot 3.4.3
- **Java:** 21+ (getest op 25)
- **Opslag:** In-memory (`ConcurrentHashMap`) — berichten verdwijnen bij herstart
- **Poort:** `8080`
- **Testframework:** JUnit 5 + Mockito + MockMvc
- **Validatie:** XPath XML-parsing, EAN-18 Luhn-checksum, 15+ regels per spec
- **Logging:** SLF4J — elk validatie-event inclusief foutcode, type en tijdstip

