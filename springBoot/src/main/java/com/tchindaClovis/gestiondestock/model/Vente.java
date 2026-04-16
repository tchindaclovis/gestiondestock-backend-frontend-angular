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
@Table(name = "Vente")
public class Vente extends AbstractEntity {

    @Column(name = "code")
    private String code;

    @Column(name = "datevente")
    private Instant dateVente;

    @Column(name = "commentaire")
    private String commentaire;

    @ManyToOne
    @JoinColumn(name = "idclient")
    private Client client;

    @Column(name = "identreprise")
    private Integer idEntreprise;

//    @OneToMany(mappedBy = "vente", fetch = FetchType.LAZY) // Gardez LAZY par défaut
//    private List<LigneVente> ligneVentes;

    @OneToMany(mappedBy = "vente", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @ToString.Exclude // <--- AJOUTEZ CECI
    private List<LigneVente> ligneVentes;

    @PrePersist
    @PreUpdate // Ajouté pour vérifier aussi lors des modifications
    protected void validateAndCreateDate() {
        // 1. On définit une date limite à 1000 jours dans le futur (très suffisant pour une vente)
        //    // plusSeconds() est une méthode directe de Instant qui ne demande pas d'import supplémentaire
        Instant maxReasonableDate = Instant.now().plusSeconds(86400 * 1000);

        // 2. Si la date est nulle OU délirante (trop loin dans le futur), on force NOW()
        if (this.dateVente == null || this.dateVente.isAfter(maxReasonableDate)) {
            this.dateVente = Instant.now();
        }
    }
}








//package com.tchindaClovis.gestiondestock.model;
//
//import com.tchindaClovis.gestiondestock.dto.LigneVenteDto;
//import jakarta.persistence.*;
//import lombok.*;
//
//import java.math.BigDecimal;
//import java.time.Instant;
//import java.time.LocalDateTime;
//import java.util.List;
//
//@Data
//@NoArgsConstructor
//@AllArgsConstructor
//@EqualsAndHashCode(callSuper = true)
//@Entity
//@Table(name = "Vente")  //optionnel car par défaut le nom de la classe
//public class Vente extends AbstractEntity{
//
//    @Column(name = "code")
//    private String code;
//
//    @Column(name = "datevente")
//    private Instant dateVente;
//
//    @Column(name = "commentaire")
//    private String commentaire;
//
//    @ManyToOne
//    @JoinColumn(name = "idclient")
//    private Client client;
//
//    @Column(name = "identreprise")  //entité de convenance qu'on ajoute juste pour certaines dispositions
//    private Integer idEntreprise;  //rien à voir avec les règle UML
//
//    @OneToMany(mappedBy = "vente")
//    private List<LigneVente> ligneVentes;
//
//
//    // Dans ton entité Vente.java
//    @PrePersist
//    protected void onCreate() {
//        if (this.dateVente == null) {
//            this.dateVente = Instant.now(); // Force la date actuelle à l'enregistrement
//        }
//    }
//
//}
