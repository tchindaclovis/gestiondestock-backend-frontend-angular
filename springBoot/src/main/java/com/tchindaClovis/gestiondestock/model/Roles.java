package com.tchindaClovis.gestiondestock.model;

import jakarta.persistence.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "Roles")  //optionnel car par défaut le nom de la classe
public class Roles extends AbstractEntity{

    @Column(name = "roleName")
    @Enumerated(EnumType.STRING)
    private ERoleName roleName;

    @ManyToOne
    @JoinColumn(name = "idutilisateur")
    private Utilisateur utilisateur;

}
