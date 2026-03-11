package com.engie.api.model;

import java.time.LocalDateTime;

/**
 * Één validatiefout met foutcode, omschrijving, type en tijdstempel.
 */
public class ValidationFout {

    public enum FoutType {
        /** Technische fout — bericht wordt toch bevestigd */
        TECHNISCH,
        /** Semantische fout — ontvanger mag afwijzen */
        SEMANTISCH
    }

    private String foutCode;
    private String omschrijving;
    private FoutType foutType;
    private LocalDateTime tijdstip;

    public ValidationFout() {}

    public ValidationFout(String foutCode, String omschrijving, FoutType foutType) {
        this.foutCode = foutCode;
        this.omschrijving = omschrijving;
        this.foutType = foutType;
        this.tijdstip = LocalDateTime.now();
    }

    public String getFoutCode() { return foutCode; }
    public void setFoutCode(String foutCode) { this.foutCode = foutCode; }

    public String getOmschrijving() { return omschrijving; }
    public void setOmschrijving(String omschrijving) { this.omschrijving = omschrijving; }

    public FoutType getFoutType() { return foutType; }
    public void setFoutType(FoutType foutType) { this.foutType = foutType; }

    public LocalDateTime getTijdstip() { return tijdstip; }
    public void setTijdstip(LocalDateTime tijdstip) { this.tijdstip = tijdstip; }
}
