package com.tchindaClovis.gestiondestock.services;

import com.tchindaClovis.gestiondestock.dto.ClientDto;

import java.util.List;

public interface ClientService {
    ClientDto save(ClientDto dto);
    ClientDto findById(Integer id);
    ClientDto findByNom(String nom);
    ClientDto findByPrenom(String prenom);
    List<ClientDto> findAll();
    void delete(Integer id);
}
