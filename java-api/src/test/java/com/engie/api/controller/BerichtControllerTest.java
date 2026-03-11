package com.engie.api.controller;

import com.engie.api.model.OntvangstBevestiging;
import com.engie.api.service.BerichtService;
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

import com.engie.api.model.Bericht;
import com.engie.api.model.BerichtRequest;

@WebMvcTest(BerichtController.class)
class BerichtControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BerichtService berichtService;

    // ── POST /api/berichten ───────────────────────────────────────────────────

    @Test
    void postBericht_geldigBericht_geeft202MetBevestiging() throws Exception {
        OntvangstBevestiging bevestiging = OntvangstBevestiging.builder()
                .berichtId("test-uuid-123")
                .status("ONTVANGEN")
                .ontvangstTijd(LocalDateTime.now())
                .toelichting("Bericht succesvol ontvangen en geregistreerd.")
                .build();

        when(berichtService.ontvangBericht(any())).thenReturn(bevestiging);

        String body = """
                {
                  "afzender": "systeem-A",
                  "onderwerp": "FactuurBericht",
                  "xmlPayload": "<factuur><id>1</id></factuur>"
                }
                """;

        mockMvc.perform(post("/api/berichten")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.berichtId").value("test-uuid-123"))
                .andExpect(jsonPath("$.status").value("ONTVANGEN"))
                .andExpect(jsonPath("$.ontvangstTijd").exists())
                .andExpect(jsonPath("$.toelichting").exists());
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
        Bericht bericht = new Bericht("id-1", "systeem-A", "Factuur",
                "<factuur/>", LocalDateTime.now(), "ONTVANGEN");

        when(berichtService.getAlleBerichten()).thenReturn(List.of(bericht));

        mockMvc.perform(get("/api/berichten"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("id-1"))
                .andExpect(jsonPath("$[0].afzender").value("systeem-A"))
                .andExpect(jsonPath("$[0].status").value("ONTVANGEN"));
    }

    // ── GET /api/berichten/{id} ───────────────────────────────────────────────

    @Test
    void getBerichtById_bestaandId_geeft200MetBericht() throws Exception {
        Bericht bericht = new Bericht("id-1", "systeem-A", "Factuur",
                "<factuur/>", LocalDateTime.now(), "ONTVANGEN");

        when(berichtService.getBerichtById("id-1")).thenReturn(Optional.of(bericht));

        mockMvc.perform(get("/api/berichten/id-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("id-1"))
                .andExpect(jsonPath("$.afzender").value("systeem-A"));
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
}
