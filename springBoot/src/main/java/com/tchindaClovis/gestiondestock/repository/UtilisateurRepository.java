package com.tchindaClovis.gestiondestock.repository;

import aj.org.objectweb.asm.commons.Remapper;
import com.tchindaClovis.gestiondestock.dto.UtilisateurDto;
import com.tchindaClovis.gestiondestock.model.Client;
import com.tchindaClovis.gestiondestock.model.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UtilisateurRepository extends JpaRepository<Utilisateur, Integer> {


    // JPQL query
    @Query(value = "select u from Utilisateur u where u.email = :email")
    Optional<Utilisateur> findUtilisateurByEmail(@Param("email") String email);// elle va chercher un utilisateur par son email dans la BDD

//    @Query(value = "select u from Utilisateur u where u.nom = :nom")
//    Optional<Utilisateur> findUtilisateurByNom(@Param("nom") String nom);
    List<Utilisateur> findAllByEntrepriseId(Integer id);

    Optional<Utilisateur> findByStatut(String statut);
    Optional<Utilisateur> findByNom(String nom);
}
