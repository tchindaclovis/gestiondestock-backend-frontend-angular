package com.tchindaClovis.gestiondestock.repository;

import com.tchindaClovis.gestiondestock.model.CommandeFournisseur;
import com.tchindaClovis.gestiondestock.model.LigneCommandeClient;
import com.tchindaClovis.gestiondestock.model.LigneVente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface LigneVenteRepository extends JpaRepository<LigneVente, Integer> {
    List<LigneVente> findAllByArticleId(Integer idArticle);

    List<LigneVente> findAllByVenteId(Integer id);

    List<LigneVente> findAllByIdEntreprise(Integer idEntreprise);

    // LigneVenteRepository.java
    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("DELETE FROM LigneVente l WHERE l.vente.id = :idVente")
    void deleteByVenteId(@Param("idVente") Integer idVente);
}
