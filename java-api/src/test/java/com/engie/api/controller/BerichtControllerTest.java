package com.engie.api.controller;

import com.engie.api.model.Bericht;
import com.engie.api.model.BerichtRequest;
import com.engie.api.model.OntvangstBevestiging;
import com.engie.api.model.ValidationFout;
import com.engie.api.model.ValidationResultaat;
import com.engie.api.service.BerichtService;
import com.engie.api.service.BerichtValidatieService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BerichtController.class)
class BerichtControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BerichtService berichtService;

    @MockBean
    private BerichtValidatieService validatieService;

    // ── POST /api/berichten ─── geldig bericht ────────────────────────────────

    @Test
    void postBericht_geldigBericht_geeft202MetBevestiging() throws Exception {
        ValidationResultaat geldig = new ValidationResultaat();
        geldig.setStatus(ValidationResultaat.Status.GOEDGEKEURD);
        when(validatieService.valideer(any())).thenReturn(geldig);

        OntvangstBevestiging bevestiging = OntvangstBevestiging.builder()
                .berichtId("test-uuid-123")
                .status("ONTVANGEN")
                .ontvangstTijd(LocalDateTime.now())
                .toelichting("Bericht succesvol ontvangen en geregistreerd.")
                .build();
        when(berichtService.ontvangBericht(any())).thenReturn(bevestiging);

        mockMvc.perform(post("/api/berichten")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(geldigBerichtJson()))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.berichtId").value("test-uuid-123"))
                .andExpect(jsonPath("$.status").value("ONTVANGEN"));
    }

    // ── POST /api/berichten ─── technische fouten ─────────────────────────────

    @Test
    void postBericht_technischeFout_geeft202MetValidatieDetails() throws Exception {
        ValidationResultaat bevestigd = new ValidationResultaat();
        bevestigd.setStatus(ValidationResultaat.Status.BEVESTIGD);
        bevestigd.voegFoutToe("669", "MessageID al eerder verwerkt.", ValidationFout.FoutType.TECHNISCH);
        when(validatieService.valideer(any())).thenReturn(bevestigd);

        OntvangstBevestiging bevestiging = OntvangstBevestiging.builder()
                .berichtId("test-uuid-456")
                .status("ONTVANGEN")
                .ontvangstTijd(LocalDateTime.now())
                .toelichting("Bericht ontvangen met technische opmerkingen. Zie validatie voor details.")
                .validatie(bevestigd)
                .build();
        when(berichtService.ontvangBericht(any())).thenReturn(bevestiging);

        mockMvc.perform(post("/api/berichten")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(geldigBerichtJson()))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.validatie.fouten[0].foutCode").value("669"))
                .andExpect(jsonPath("$.validatie.fouten[0].foutType").value("TECHNISCH"));
    }

    // ── POST /api/berichten ─── semantische fouten ────────────────────────────

    @Test
    void postBericht_semantischeFout_geeft400MetAfwijzing() throws Exception {
        ValidationResultaat afgewezen = new ValidationResultaat();
        afgewezen.setStatus(ValidationResultaat.Status.AFGEWEZEN);
        afgewezen.voegFoutToe("650", "EAN-code ongeldig.", ValidationFout.FoutType.SEMANTISCH);
        when(validatieService.valideer(any())).thenReturn(afgewezen);

        mockMvc.perform(post("/api/berichten")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(geldigBerichtJson()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("AFGEWEZEN"))
                .andExpect(jsonPath("$.validatie.fouten[0].foutCode").value("650"))
                .andExpect(jsonPath("$.validatie.fouten[0].foutType").value("SEMANTISCH"));
    }

    // ── GET /api/berichten ────────────────────────────────────────────────────

    @Test
    void getAlleBerichten_legeStore_geeftLegeArray() throws Exception {
        when(berichtService.getAlleBerichten()).thenReturn(List.of());

        mockMvc.perform(get("/api/berichten"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void getAlleBerichten_metBerichten_geeftAlleItems() throws Exception {
        Bericht bericht = new Bericht("id-1", "871686700000900000", "AllocationResult",
                "<AllocationResult/>", LocalDateTime.now(), "ONTVANGEN");
        when(berichtService.getAlleBerichten()).thenReturn(List.of(bericht));

        mockMvc.perform(get("/api/berichten"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("id-1"))
                .andExpect(jsonPath("$[0].status").value("ONTVANGEN"));
    }

    // ── GET /api/berichten/{id} ───────────────────────────────────────────────

    @Test
    void getBerichtById_bestaandId_geeft200MetBericht() throws Exception {
        Bericht bericht = new Bericht("id-1", "871686700000900000", "AllocationResult",
                "<AllocationResult/>", LocalDateTime.now(), "ONTVANGEN");
        when(berichtService.getBerichtById("id-1")).thenReturn(Optional.of(bericht));

        mockMvc.perform(get("/api/berichten/id-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("id-1"));
    }

    @Test
    void getBerichtById_onbestaandId_geeft404() throws Exception {
        when(berichtService.getBerichtById("bestaat-niet")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/berichten/bestaat-niet"))
                .andExpect(status().isNotFound());
    }

    // ── DELETE /api/berichten/{id} ────────────────────────────────────────────

    @Test
    void verwijderBericht_bestaandId_geeft204() throws Exception {
        when(berichtService.verwijderBericht("id-1")).thenReturn(true);

        mockMvc.perform(delete("/api/berichten/id-1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void verwijderBericht_onbestaandId_geeft404() throws Exception {
        when(berichtService.verwijderBericht("bestaat-niet")).thenReturn(false);

        mockMvc.perform(delete("/api/berichten/bestaat-niet"))
                .andExpect(status().isNotFound());
    }

    // ── Helper ────────────────────────────────────────────────────────────────

    private String geldigBerichtJson() {
        return """
                {
                  "senderID":    "871686700000900000",
                  "receiverID":  "871686700001600000",
                  "contentType": "AllocationResult",
                  "correlationId": "MSG-20260311-001",
                  "xmlPayload":  "<AllocationResult><mRID>MSG-001</mRID></AllocationResult>"
                }
                """;
    }
}
