package com.tchindaClovis.gestiondestock.repository;

import com.tchindaClovis.gestiondestock.model.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UtilisateurRepository extends JpaRepository<Utilisateur, Integer> {

//    Optional<Utilisateur> findByNom(String nom);
//    Optional<Utilisateur> findByPrenom(String prenom);

    // JPQL query
    @Query(value = "select u from Utilisateur u where u.email = :email")
    Optional<Utilisateur> findUtilisateurByEmail(@Param("email") String email);// elle va chercher un utilisateur par son email dans la BDD

}
