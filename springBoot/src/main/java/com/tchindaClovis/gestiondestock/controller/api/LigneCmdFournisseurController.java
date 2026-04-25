package com.tchindaClovis.gestiondestock.controller.api;

import com.tchindaClovis.gestiondestock.dto.LigneCommandeClientDto;
import com.tchindaClovis.gestiondestock.dto.LigneCommandeFournisseurDto;
import com.tchindaClovis.gestiondestock.services.LigneCmdClientService;
import com.tchindaClovis.gestiondestock.services.LigneCmdFournisseurService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class LigneCmdFournisseurController implements LigneCmdFournisseurApi {
    private LigneCmdFournisseurService ligneCmdFournisseurService;

    @Autowired  //Constructor injection sur le constructeur
    public LigneCmdFournisseurController(LigneCmdFournisseurService ligneCmdFournisseurService){

        this.ligneCmdFournisseurService = ligneCmdFournisseurService;
    }

    @Override
    public List<LigneCommandeFournisseurDto> findAllLigneCommandeFournisseurByIdEntreprise(Integer idEntreprise) {

        return ligneCmdFournisseurService.findAllLigneCommandeFournisseurByIdEntreprise(idEntreprise);
    }
}
