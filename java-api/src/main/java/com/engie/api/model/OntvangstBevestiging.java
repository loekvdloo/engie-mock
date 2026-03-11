package com.engie.api.model;

import java.time.LocalDateTime;

public class OntvangstBevestiging {

    private String berichtId;
    private String status;
    private LocalDateTime ontvangstTijd;
    private String toelichting;
    /** Validatieresultaat — aanwezig als er fouten zijn */
    private ValidationResultaat validatie;

    public OntvangstBevestiging() {}

    public OntvangstBevestiging(String berichtId, String status, LocalDateTime ontvangstTijd, String toelichting) {
        this.berichtId = berichtId;
        this.status = status;
        this.ontvangstTijd = ontvangstTijd;
        this.toelichting = toelichting;
    }

    public String getBerichtId() { return berichtId; }
    public void setBerichtId(String berichtId) { this.berichtId = berichtId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getOntvangstTijd() { return ontvangstTijd; }
    public void setOntvangstTijd(LocalDateTime ontvangstTijd) { this.ontvangstTijd = ontvangstTijd; }

    public String getToelichting() { return toelichting; }
    public void setToelichting(String toelichting) { this.toelichting = toelichting; }

    public ValidationResultaat getValidatie() { return validatie; }
    public void setValidatie(ValidationResultaat validatie) { this.validatie = validatie; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String berichtId, status, toelichting;
        private LocalDateTime ontvangstTijd;
        private ValidationResultaat validatie;
        public Builder berichtId(String berichtId) { this.berichtId = berichtId; return this; }
        public Builder status(String status) { this.status = status; return this; }
        public Builder ontvangstTijd(LocalDateTime ontvangstTijd) { this.ontvangstTijd = ontvangstTijd; return this; }
        public Builder toelichting(String toelichting) { this.toelichting = toelichting; return this; }
        public Builder validatie(ValidationResultaat validatie) { this.validatie = validatie; return this; }
        public OntvangstBevestiging build() {
            OntvangstBevestiging o = new OntvangstBevestiging(berichtId, status, ontvangstTijd, toelichting);
            o.setValidatie(validatie);
            return o;
        }
    }
}
