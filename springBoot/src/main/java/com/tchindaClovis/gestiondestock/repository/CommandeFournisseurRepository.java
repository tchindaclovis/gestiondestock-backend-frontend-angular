package com.tchindaClovis.gestiondestock.repository;

import com.tchindaClovis.gestiondestock.model.CommandeFournisseur;
import com.tchindaClovis.gestiondestock.model.CommandeFournisseur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CommandeFournisseurRepository extends JpaRepository<CommandeFournisseur, Integer> {
    Optional<CommandeFournisseur> findCommandeFournisseurByCode(String code);
    List<CommandeFournisseur> findAllByFournisseurId(Integer id);

    @Query("SELECT f FROM CommandeFournisseur f LEFT JOIN FETCH f.ligneCommandeFournisseurs WHERE f.id = :id")
    Optional<CommandeFournisseur> findByIdWithLignes(@Param("id") Integer id);

    List<CommandeFournisseur> findAllByIdEntreprise(Integer idEntreprise);

    // Cette méthode va trier par codeCommandeFournisseur descendant et prendre le premier (le plus grand)
    Optional<CommandeFournisseur> findTopByOrderByCodeDesc();
}
