package com.engie.api.model;

import java.time.LocalDateTime;

public class Bericht {

    private String id;
    private String afzender;
    private String onderwerp;
    private String xmlPayload;
    private LocalDateTime ontvangstTijd;
    private String status;

    public Bericht() {}

    public Bericht(String id, String afzender, String onderwerp, String xmlPayload,
                   LocalDateTime ontvangstTijd, String status) {
        this.id = id;
        this.afzender = afzender;
        this.onderwerp = onderwerp;
        this.xmlPayload = xmlPayload;
        this.ontvangstTijd = ontvangstTijd;
        this.status = status;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getAfzender() { return afzender; }
    public void setAfzender(String afzender) { this.afzender = afzender; }

    public String getOnderwerp() { return onderwerp; }
    public void setOnderwerp(String onderwerp) { this.onderwerp = onderwerp; }

    public String getXmlPayload() { return xmlPayload; }
    public void setXmlPayload(String xmlPayload) { this.xmlPayload = xmlPayload; }

    public LocalDateTime getOntvangstTijd() { return ontvangstTijd; }
    public void setOntvangstTijd(LocalDateTime ontvangstTijd) { this.ontvangstTijd = ontvangstTijd; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String id, afzender, onderwerp, xmlPayload, status;
        private LocalDateTime ontvangstTijd;
        public Builder id(String id) { this.id = id; return this; }
        public Builder afzender(String afzender) { this.afzender = afzender; return this; }
        public Builder onderwerp(String onderwerp) { this.onderwerp = onderwerp; return this; }
        public Builder xmlPayload(String xmlPayload) { this.xmlPayload = xmlPayload; return this; }
        public Builder ontvangstTijd(LocalDateTime ontvangstTijd) { this.ontvangstTijd = ontvangstTijd; return this; }
        public Builder status(String status) { this.status = status; return this; }
        public Bericht build() { return new Bericht(id, afzender, onderwerp, xmlPayload, ontvangstTijd, status); }
    }
}
