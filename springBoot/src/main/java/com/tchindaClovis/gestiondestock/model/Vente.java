package com.tchindaClovis.gestiondestock.model;

import com.tchindaClovis.gestiondestock.dto.LigneVenteDto;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "Vente")  //optionnel car par défaut le nom de la classe
public class Vente extends AbstractEntity{

    @Column(name = "code")
    private String code;

    @Column(name = "datevente"
            //, columnDefinition = "timestamp without time zone"
    )
    private Instant dateVente;

    @Column(name = "commentaire")
    private String commentaire;

    @Column(name = "identreprise")  //entité de convenance qu'on ajoute juste pour certaines dispositions
    private Integer idEntreprise;  //rien à voir avec les règle UML

    @OneToMany(mappedBy = "vente")
    private List<LigneVente> LigneVentes;

}
