package com.tchindaClovis.gestiondestock.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "MvtStock")  //optionnel car par défaut le nom de la classe
public class MvtStock extends AbstractEntity {

    @Column(name = "dateMvt")
    private Instant dateMvt;

    @Column(name = "quantite")
    private BigDecimal quantite;

    @Column(name = "typemvtstock")
    @Enumerated(EnumType.STRING)
    private ETypeMvtStock typeMvt;

    @Column(name = "sourcemvt")  //pour ressortir la destination du mouvement de stock
    @Enumerated(EnumType.STRING)
    private ESourceMvtStock sourceMvt;

    @Column(name = "identreprise")  //entité de convenance qu'on ajoute juste pour certaines dispositions
    private Integer idEntreprise;  //rien à voir avec les règle UML

    @ManyToOne
    @JoinColumn(name = "article")
    private Article article;

}
