package com.tchindaClovis.gestiondestock.repository;

import com.tchindaClovis.gestiondestock.model.CommandeClient;
import com.tchindaClovis.gestiondestock.model.CommandeFournisseur;
import com.tchindaClovis.gestiondestock.model.Vente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface VenteRepository extends JpaRepository<Vente, Integer> {
    Optional<Vente> findVenteByCode(String code);

    @Query("SELECT v FROM Vente v LEFT JOIN FETCH v.ligneVentes WHERE v.id = :id")
    Optional<Vente> findByIdWithLignes(@Param("id") Integer id);

}
