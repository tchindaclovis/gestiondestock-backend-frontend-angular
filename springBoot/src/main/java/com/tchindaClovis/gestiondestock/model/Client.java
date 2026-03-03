package com.tchindaClovis.gestiondestock.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "Client")  //optionnel car par défaut le nom de la classe
public class Client extends AbstractEntity{
    @Column(name = "nom")
    private String nom;

    @Column(name = "prenom")
    private String prenom;

    @Embedded //champ embarqué qui peut être utilisé dan plusieurs autres classes
    private Adresse adresse;

    @Column(name = "photo",columnDefinition = "TEXT")
    private String photo;

    @Column(name = "email")
    private String email;

    @Column(name = "numtel")
    private String numTel;

    @Column(name = "identreprise")  //entité de convenance qu'on ajoute juste pour certaines dispositions
    private Integer idEntreprise;  //rien à voir avec les règle UML

    @OneToMany(mappedBy = "client")
//    @OneToMany(mappedBy = "client", cascade = CascadeType.REMOVE)  //permet de suprimer l'objet client avec toutes les commandeClient
    private List<CommandeClient> commandeClients;
}
