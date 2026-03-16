package com.tchindaClovis.gestiondestock.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "Utilisateur")  //optionnel car par défaut le nom de la classe
public class Utilisateur extends AbstractEntity{

    @Column(name = "nomUtilisateur")
    private String nom;

    @Column(name = "prenomUtilisateur")
    private String prenom;

    @Column(name = "email")
    private String email;

    @Column(name = "numtel")
    private String numTel;

    @Column(name = "datedenaissance"
            //, columnDefinition = "timestamp without time zone"
    )
    private Instant dateDeNaissance;

    @Column(name = "motdepasse")
    private String motDePasse;

    @Embedded //champ embarqué qui peut être utilisé dan plusieurs autres classes
    private Adresse adresse;

    @Column(name = "photo",columnDefinition = "TEXT",length = 1024)
    private String photo;

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "utilisateur")
    @JsonIgnore
    private List<Roles> roles;

    @ManyToOne
    @JoinColumn(name = "identreprise")
    private Entreprise entreprise;

}
