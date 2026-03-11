# Engie Bericht API

![Java](https://img.shields.io/badge/Java-25-orange?logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.3-brightgreen?logo=springboot)
![Maven](https://img.shields.io/badge/Maven-3.9.6-blue?logo=apachemaven)
![Tests](https://img.shields.io/badge/Tests-16%20passing-success?logo=junit5)

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
powershell -ExecutionPolicy Bypass -File "C:\Users\loek\engie-mock\java-api\run.ps1"
```

De eerste keer downloadt het script Maven automatisch. Daarna start de applicatie op:

```
http://localhost:8080
```

---

## Testen uitvoeren

```powershell
$MVN = "C:\Users\loek\.m2\wrapper\dists\apache-maven-3.9.6\apache-maven-3.9.6\bin\mvn.cmd"
cd C:\Users\loek\engie-mock\java-api
& $MVN test
```

Verwacht resultaat:

```
Tests run: 7  -- BerichtControllerTest   (alle geslaagd)
Tests run: 9  -- BerichtServiceTest      (alle geslaagd)
BUILD SUCCESS
```

---

## Endpoints

| Methode | URL | Omschrijving | Response |
|---------|-----|--------------|----------|
| `POST` | `/api/berichten` | Stuur een bericht met XML payload | `202 Accepted` + ontvangstbevestiging |
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
  "afzender": "systeem-A",
  "onderwerp": "FactuurBericht",
  "xmlPayload": "<factuur><id>123</id><bedrag>99.99</bedrag></factuur>"
}
```

**Response `202 Accepted`:**

```json
{
  "berichtId": "a3f7c2d1-9e4b-4a1f-8b2e-123456789abc",
  "status": "ONTVANGEN",
  "ontvangstTijd": "2026-03-11T08:53:11",
  "toelichting": "Bericht succesvol ontvangen en geregistreerd."
}
```

### GET — alle berichten ophalen

**URL:** `GET http://localhost:8080/api/berichten`

### GET — één bericht ophalen

**URL:** `GET http://localhost:8080/api/berichten/{berichtId}`

### DELETE — bericht verwijderen

**URL:** `DELETE http://localhost:8080/api/berichten/{berichtId}`

---

## Projectstructuur

```
java-api/
├── pom.xml
├── run.ps1                                          ← Start-script (downloadt Maven automatisch)
└── src/
    ├── main/java/com/engie/api/
    │   ├── EngieBerichtApiApplication.java          ← Spring Boot entry point
    │   ├── controller/
    │   │   └── BerichtController.java               ← REST endpoints
    │   ├── model/
    │   │   ├── BerichtRequest.java                  ← JSON input (afzender, onderwerp, xmlPayload)
    │   │   ├── Bericht.java                         ← Opgeslagen bericht
    │   │   └── OntvangstBevestiging.java            ← Technische bevestiging response
    │   └── service/
    │       └── BerichtService.java                  ← In-memory opslag
    └── test/java/com/engie/api/
        ├── controller/
        │   └── BerichtControllerTest.java           ← 7 controller testen (MockMvc)
        └── service/
            └── BerichtServiceTest.java              ← 9 service testen (unit)
```

---

## Technische details

- **Framework:** Spring Boot 3.4.3
- **Java:** 21+ (getest op 25)
- **Opslag:** In-memory (`ConcurrentHashMap`) — berichten verdwijnen bij herstart
- **Poort:** `8080`
- **Testframework:** JUnit 5 + Mockito + MockMvc

