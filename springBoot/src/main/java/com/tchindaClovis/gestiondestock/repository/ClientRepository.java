package com.tchindaClovis.gestiondestock.repository;

import com.tchindaClovis.gestiondestock.model.Article;
import com.tchindaClovis.gestiondestock.model.Client;
import com.tchindaClovis.gestiondestock.model.Vente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ClientRepository extends JpaRepository<Client, Integer> {
    Optional<Client> findClientByNom(String nom);
    Optional<Client> findClientByStatut(String statut);

    List<Client> findAllByIdEntreprise(Integer idEntreprise);
}
