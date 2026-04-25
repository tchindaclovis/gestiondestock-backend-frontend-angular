package com.tchindaClovis.gestiondestock.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "CommandeClient")  //optionnel car par défaut le nom de la classe
public class CommandeClient extends AbstractEntity{

    @Column(name = "code")
    private String code;

    @Column(name = "datecommande")
    private Instant dateCommande;

    @Column(name = "etatcommande")
    @Enumerated(EnumType.STRING)
    private EEtatCommande etatCommande;

    @Column(name = "identreprise")  //entité de convenance qu'on ajoute juste pour certaines dispositions
    private Integer idEntreprise;  //rien à voir avec les règle UML

    @ManyToOne
    @JoinColumn(name = "idclient")
    private Client client;


    @OneToMany(mappedBy = "commandeClient", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @ToString.Exclude // <--- AJOUTEZ CECI
    private List<LigneCommandeClient> ligneCommandeClients;


    @PrePersist
    @PreUpdate // Ajouté pour vérifier aussi lors des modifications
    protected void validateAndCreateDate() {
        // 1. On définit une date limite à 1000 jours dans le futur (très suffisant pour une commande)
        //    // plusSeconds() est une méthode directe de Instant qui ne demande pas d'import supplémentaire
        Instant maxReasonableDate = Instant.now().plusSeconds(86400 * 1000);

        // 2. Si la date est nulle OU délirante (trop loin dans le futur), on force NOW()
        if (this.dateCommande == null || this.dateCommande.isAfter(maxReasonableDate)) {
            this.dateCommande = Instant.now();
        }
    }
}
