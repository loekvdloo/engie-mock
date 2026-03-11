package com.engie.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class BerichtRequest {

    @JsonProperty("afzender")
    private String afzender;

    @JsonProperty("onderwerp")
    private String onderwerp;

    @JsonProperty("xmlPayload")
    private String xmlPayload;

    public String getAfzender() { return afzender; }
    public void setAfzender(String afzender) { this.afzender = afzender; }

    public String getOnderwerp() { return onderwerp; }
    public void setOnderwerp(String onderwerp) { this.onderwerp = onderwerp; }

    public String getXmlPayload() { return xmlPayload; }
    public void setXmlPayload(String xmlPayload) { this.xmlPayload = xmlPayload; }
}