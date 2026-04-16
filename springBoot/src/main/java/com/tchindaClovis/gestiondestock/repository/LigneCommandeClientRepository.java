package com.tchindaClovis.gestiondestock.repository;

import com.tchindaClovis.gestiondestock.model.LigneCommandeClient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface LigneCommandeClientRepository extends JpaRepository<LigneCommandeClient, Integer> {

    List<LigneCommandeClient> findAllByCommandeClientId(Integer id);

    List<LigneCommandeClient> findAllByArticleId(Integer id);

    // LigneVenteRepository.java
    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("DELETE FROM LigneCommandeClient l WHERE l.commandeClient.id = :idCommandeClient")
    void deleteByCommandeClientId(@Param("idCommandeClient") Integer idCommandeClient);
}
