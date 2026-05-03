package com.tchindaClovis.gestiondestock.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
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
@Table(name = "PeriodeValidite")  //optionnel car par défaut le nom de la classe
public class PeriodeValidite extends AbstractEntity {

    @Column(name = "intitule")
    String intitule;

    @Column(name = "debut")
    Date debut;

    @Column(name = "numero")
    Integer numero;

    @Column(name = "fin")
    Date fin;

    @Column(name = "derniere")
    Boolean derniere = Boolean.FALSE;

    @Column(name = "identreprise")  //entité de convenance qu'on ajoute juste pour certaines dispositions
    private Integer idEntreprise;  //rien à voir avec les règle UML
}
