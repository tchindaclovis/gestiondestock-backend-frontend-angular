package com.tchindaClovis.gestiondestock.services;

import com.tchindaClovis.gestiondestock.dto.ClientDto;
import com.tchindaClovis.gestiondestock.dto.VenteDto;

import java.util.List;

public interface ClientService {
    ClientDto save(ClientDto dto);
    ClientDto findById(Integer id);
    ClientDto findByNom(String nom);
    ClientDto findByStatut(String statut);
    List<ClientDto> findAll();
    List<ClientDto> findAllClientByIdEntreprise(Integer idEntreprise);
    void delete(Integer id);
}
