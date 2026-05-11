package com.tchindaClovis.gestiondestock.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Table(name = "Document")  //optionnel car par défaut le nom de la classe
public class Document extends AbstractEntity{

    @Column(name = "destinataire")
    private String destinataire;

    @Column(name = "modifiable")
    private boolean modifiable = true;

    @Column(name = "identreprise")  //entité de convenance qu'on ajoute juste pour certaines dispositions
    private Integer idEntreprise;  //rien à voir avec les règle UML

    @Column(name = "exercice")
    private String exercice;

    @Column(name = "derniermotifannulation")
    private String dernierMotifAnnulation;

    @Column(name = "dernierrejetteur")
    private String dernierRejetteur;

    @Column(name = "datedernierrejet")
    private Date dateDernierRejet;

    @Column(name = "etat")
    private EEtatDocument etat;

    @Column(name = "type")
    protected ETypeDocument type;

    @Column(name = "source")
    protected ESourceDocument source;

    @Column(name = "date")
    private Date date;

    @Column(name = "datebutoireconfirmation")
    private Date dateButoireConfirmation;

    @Column(name = "datebutoirevalidation")
    private Date dateButoireValidation;

    @Column(name = "datebutoirepayement")
    private Date dateButoirePayement;

    @Column(name = "dateenvoi")
    private Date dateEnvoi;

    @Column(name = "datevalidation")
    private Date dateValidation;

    @Column(name = "datetransfert")
    private Date dateTransfert;

}
