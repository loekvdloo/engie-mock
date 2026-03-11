package com.engie.api.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Resultaat van alle validatiechecks op een ingediend bericht.
 *
 * Status:
 *   GOEDGEKEURD  — geen fouten
 *   BEVESTIGD    — technische fouten, maar bericht wordt alsnog bevestigd
 *   AFGEWEZEN    — semantische fouten, bericht afgewezen
 */
public class ValidationResultaat {

    public enum Status { GOEDGEKEURD, BEVESTIGD, AFGEWEZEN }

    private Status status;
    private List<ValidationFout> fouten = new ArrayList<>();
    private LocalDateTime tijdstip = LocalDateTime.now();

    public ValidationResultaat() {}

    public boolean heeftSemanischeFouten() {
        return fouten.stream().anyMatch(f -> f.getFoutType() == ValidationFout.FoutType.SEMANTISCH);
    }

    public boolean heeftFouten() {
        return !fouten.isEmpty();
    }

    public void voegFoutToe(ValidationFout fout) {
        fouten.add(fout);
    }

    public void voegFoutToe(String code, String omschrijving, ValidationFout.FoutType type) {
        fouten.add(new ValidationFout(code, omschrijving, type));
    }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

    public List<ValidationFout> getFouten() { return fouten; }
    public void setFouten(List<ValidationFout> fouten) { this.fouten = fouten; }

    public LocalDateTime getTijdstip() { return tijdstip; }
    public void setTijdstip(LocalDateTime tijdstip) { this.tijdstip = tijdstip; }
}
