package com.tchindaClovis.gestiondestock.controller;

import com.tchindaClovis.gestiondestock.controller.api.LigneVenteApi;
import com.tchindaClovis.gestiondestock.dto.LigneVenteDto;
import com.tchindaClovis.gestiondestock.services.LigneVenteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class LigneVenteController implements LigneVenteApi {
    private LigneVenteService ligneVenteService;

    @Autowired  //Constructor injection sur le constructeur
    public LigneVenteController(LigneVenteService ligneVenteService){

        this.ligneVenteService = ligneVenteService;
    }

    @Override
    public List<LigneVenteDto> findAllLigneVenteByIdEntreprise(Integer idEntreprise) {

        return ligneVenteService.findAllLigneVenteByIdEntreprise(idEntreprise);
    }
}
