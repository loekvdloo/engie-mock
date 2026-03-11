package com.engie.api.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "bericht_logs")
public class BerichtLogEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bericht_id")
    private BerichtEntity bericht;

    @Column(name = "actie", length = 50, nullable = false)
    private String actie;

    @Column(name = "omschrijving", columnDefinition = "TEXT")
    private String omschrijving;

    @Column(name = "processor", length = 255)
    private String processor;

    @Column(name = "tijdstip", nullable = false)
    private LocalDateTime tijdstip;

    @PrePersist
    void prePersist() {
        if (tijdstip == null) tijdstip = LocalDateTime.now();
    }

    // Factory-methode voor gemak
    public static BerichtLogEntity van(BerichtEntity bericht, String actie, String omschrijving) {
        BerichtLogEntity log = new BerichtLogEntity();
        log.bericht = bericht;
        log.actie = actie;
        log.omschrijving = omschrijving;
        log.processor = "engie-bericht-api";
        return log;
    }

    public Long getId() { return id; }

    public BerichtEntity getBericht() { return bericht; }
    public void setBericht(BerichtEntity bericht) { this.bericht = bericht; }

    public String getActie() { return actie; }
    public void setActie(String actie) { this.actie = actie; }

    public String getOmschrijving() { return omschrijving; }
    public void setOmschrijving(String omschrijving) { this.omschrijving = omschrijving; }

    public String getProcessor() { return processor; }
    public void setProcessor(String processor) { this.processor = processor; }

    public LocalDateTime getTijdstip() { return tijdstip; }
}
