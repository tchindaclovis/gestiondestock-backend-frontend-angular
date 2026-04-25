package com.tchindaClovis.gestiondestock.repository;

import com.tchindaClovis.gestiondestock.model.CommandeFournisseur;
import com.tchindaClovis.gestiondestock.model.LigneCommandeClient;
import com.tchindaClovis.gestiondestock.model.LigneCommandeFournisseur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface LigneCommandeFournisseurRepository extends JpaRepository<LigneCommandeFournisseur, Integer> {
    List<LigneCommandeFournisseur> findAllByCommandeFournisseurId(Integer idCommande);

    List<LigneCommandeFournisseur> findAllByArticleId(Integer idCommande);

    List<LigneCommandeFournisseur> findAllByIdEntreprise(Integer idEntreprise);

    // LigneVenteRepository.java
    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("DELETE FROM LigneCommandeFournisseur l WHERE l.commandeFournisseur.id = :idCommandeFournisseur")
    void deleteByCommandeFournisseurId(@Param("idCommandeFournisseur") Integer idCommandeFournisseur);
}
