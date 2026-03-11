package com.engie.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * JSON-envelope voor een inkomend bericht.
 *
 * SOAP-header velden en de XML business-document payload.
 *
 * Voorbeeld:
 * {
 *   "senderID":      "8716867000009",
 *   "receiverID":    "8716867000016",
 *   "contentType":   "AllocationResult",
 *   "correlationId": "MSG-20260311-001",
 *   "xmlPayload":    "<AllocationResult>...</AllocationResult>"
 * }
 */
public class BerichtRequest {

    /** EAN-18 van de verzender (SOAP-header) */
    @JsonProperty("senderID")
    private String senderID;

    /** EAN-18 van de ontvanger (SOAP-header) */
    @JsonProperty("receiverID")
    private String receiverID;

    /** Type bericht (SOAP-header) */
    @JsonProperty("contentType")
    private String contentType;

    /** Correlatie-ID (SOAP-header) */
    @JsonProperty("correlationId")
    private String correlationId;

    /** De volledige XML business-document als string */
    @JsonProperty("xmlPayload")
    private String xmlPayload;

    // Backwards-compatibility velden
    @JsonProperty("afzender")
    private String afzender;

    @JsonProperty("onderwerp")
    private String onderwerp;

    public String getSenderID() { return senderID != null ? senderID : afzender; }
    public void setSenderID(String senderID) { this.senderID = senderID; }

    public String getReceiverID() { return receiverID; }
    public void setReceiverID(String receiverID) { this.receiverID = receiverID; }

    public String getContentType() { return contentType != null ? contentType : onderwerp; }
    public void setContentType(String contentType) { this.contentType = contentType; }

    public String getCorrelationId() { return correlationId; }
    public void setCorrelationId(String correlationId) { this.correlationId = correlationId; }

    public String getXmlPayload() { return xmlPayload; }
    public void setXmlPayload(String xmlPayload) { this.xmlPayload = xmlPayload; }

    // Backwards-compatibility getters
    public String getAfzender() { return afzender != null ? afzender : senderID; }
    public void setAfzender(String afzender) { this.afzender = afzender; }

    public String getOnderwerp() { return onderwerp != null ? onderwerp : contentType; }
    public void setOnderwerp(String onderwerp) { this.onderwerp = onderwerp; }
}