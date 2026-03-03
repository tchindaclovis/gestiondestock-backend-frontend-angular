package com.tchindaClovis.gestiondestock.controller;

import com.tchindaClovis.gestiondestock.controller.api.ClientApi;
import com.tchindaClovis.gestiondestock.dto.ClientDto;
import com.tchindaClovis.gestiondestock.services.ClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
public class ClientController implements ClientApi {
    private ClientService clientService;

    @Autowired
    public ClientController(ClientService clientService) {

        this.clientService = clientService;
    }

    @Override
    public ClientDto save(ClientDto dto) {

        return clientService.save(dto);
    }

    @Override
    public ClientDto findById(Integer id) {

        return clientService.findById(id);
    }

//    @Override
//    public ClientDto findByNom(String nom) {
//
//        return clientService.findByNom(nom);
//    }

//    @Override
//    public ClientDto findByPrenom(String prenom) {
//
//        return clientService.findByPrenom(prenom);
//    }

    @Override
    public List<ClientDto> findAll() {

        return clientService.findAll();
    }

    @Override
    public void delete(Integer id) {

        clientService.delete(id);
    }
}
