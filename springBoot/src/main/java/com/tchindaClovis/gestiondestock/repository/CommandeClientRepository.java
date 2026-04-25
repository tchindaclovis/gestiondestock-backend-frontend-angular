package com.tchindaClovis.gestiondestock.repository;

import com.tchindaClovis.gestiondestock.model.Article;
import com.tchindaClovis.gestiondestock.model.CommandeClient;
import com.tchindaClovis.gestiondestock.model.Vente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CommandeClientRepository extends JpaRepository<CommandeClient, Integer> {
    Optional<CommandeClient> findCommandeClientByCode(String code);
    List<CommandeClient> findAllByClientId(Integer id);

    @Query("SELECT c FROM CommandeClient c LEFT JOIN FETCH c.ligneCommandeClients WHERE c.id = :id")
    Optional<CommandeClient> findByIdWithLignes(@Param("id") Integer id);

    List<CommandeClient> findAllByIdEntreprise(Integer idEntreprise);

    // Cette méthode va trier par codeCommandeClient descendant et prendre le premier (le plus grand)
    Optional<CommandeClient> findTopByOrderByCodeDesc();
}
