package com.tchindaClovis.gestiondestock.repository;

import com.tchindaClovis.gestiondestock.model.Client;
import com.tchindaClovis.gestiondestock.model.CommandeFournisseur;
import com.tchindaClovis.gestiondestock.model.Entreprise;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EntrepriseRepository extends JpaRepository<Entreprise, Integer> {
    Optional<Entreprise> findByNom(String nom);
    Optional<Entreprise> findEntrepriseByCodeFiscal(String codeFiscal);
}
