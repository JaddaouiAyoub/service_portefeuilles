package com.example.service_portefeuilles.dto;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
public class CreationPortefeuilleRequestDto {
    private Long utilisateurId;
    private Devise devise; // Par exemple : "USD", "EUR", "MAD"
    private Double balanceInitiale; // Solde initial

    public Long getutilisateurId() {
        return this.utilisateurId;
    }
    public Devise getDevise() {
        return this.devise;
    }

    public Double getBalanceInitiale() {
        return balanceInitiale;
    }

    public void setUtilisateurId(Long utilisateurId) {
        this.utilisateurId = utilisateurId;
    }

    public void setDevise(Devise devise) {
        this.devise = devise;
    }

    public void setBalanceInitiale(Double balanceInitiale) {
        this.balanceInitiale = balanceInitiale;
    }
}
