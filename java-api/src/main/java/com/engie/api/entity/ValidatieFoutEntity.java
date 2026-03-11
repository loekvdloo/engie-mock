package com.engie.api.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "validatie_fouten")
public class ValidatieFoutEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bericht_id", nullable = false)
    private BerichtEntity bericht;

    @Column(name = "fout_code", length = 20, nullable = false)
    private String foutCode;

    @Column(name = "omschrijving", columnDefinition = "TEXT")
    private String omschrijving;

    @Column(name = "fout_type", length = 20, nullable = false)
    private String foutType;

    @Column(name = "tijdstip", nullable = false)
    private LocalDateTime tijdstip;

    @PrePersist
    void prePersist() {
        if (tijdstip == null) tijdstip = LocalDateTime.now();
    }

    public Long getId() { return id; }

    public BerichtEntity getBericht() { return bericht; }
    public void setBericht(BerichtEntity bericht) { this.bericht = bericht; }

    public String getFoutCode() { return foutCode; }
    public void setFoutCode(String foutCode) { this.foutCode = foutCode; }

    public String getOmschrijving() { return omschrijving; }
    public void setOmschrijving(String omschrijving) { this.omschrijving = omschrijving; }

    public String getFoutType() { return foutType; }
    public void setFoutType(String foutType) { this.foutType = foutType; }

    public LocalDateTime getTijdstip() { return tijdstip; }
    public void setTijdstip(LocalDateTime tijdstip) { this.tijdstip = tijdstip; }
}
