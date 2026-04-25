package com.tchindaClovis.gestiondestock.controller;

import com.tchindaClovis.gestiondestock.controller.api.LigneCmdClientApi;
import com.tchindaClovis.gestiondestock.dto.ArticleDto;
import com.tchindaClovis.gestiondestock.dto.LigneCommandeClientDto;
import com.tchindaClovis.gestiondestock.services.ArticleService;
import com.tchindaClovis.gestiondestock.services.LigneCmdClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class LigneCmdClientController implements LigneCmdClientApi {
    private LigneCmdClientService ligneCmdClientService;

    @Autowired  //Constructor injection sur le constructeur
    public LigneCmdClientController(LigneCmdClientService ligneCmdClientService){

        this.ligneCmdClientService = ligneCmdClientService;
    }

    @Override
    public List<LigneCommandeClientDto> findAllLigneCommandeClientByIdEntreprise(Integer idEntreprise) {

        return ligneCmdClientService.findAllLigneCommandeClientByIdEntreprise(idEntreprise);
    }
}
