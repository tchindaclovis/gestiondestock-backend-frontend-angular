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
public class MvtStock extends Document {

    @Column(name = "dateMvt")
    private Instant dateMvt;

    @Column(name = "quantite")
    private BigDecimal quantite;

//    @Column(name = "codecorrection")
//    private String codeCorrection;

    @Column(name = "typemvtstock")
    @Enumerated(EnumType.STRING)
    private ETypeMvtStock typeMvt;

    @Column(name = "sourcemvt")  //pour ressortir la destination du mouvement de stock
    @Enumerated(EnumType.STRING)
    private ESourceMvtStock sourceMvt;

    @Column(name = "code_source") // Le champ magique pour Angular
    private String codeSource;

    @Column(name = "codecommandeclient")
    private String codeCommandeClient;

    @Column(name = "cout_total")
    private BigDecimal coutTotal;

    @ManyToOne
    @JoinColumn(name = "article")
    private Article article;

    @ManyToOne
    @JoinColumn(name = "vente")
    private Vente vente;

}
