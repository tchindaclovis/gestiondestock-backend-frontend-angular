package com.tchindaClovis.gestiondestock.controller;

import com.tchindaClovis.gestiondestock.controller.api.CommandeFournisseurApi;
import com.tchindaClovis.gestiondestock.dto.CommandeClientDto;
import com.tchindaClovis.gestiondestock.dto.CommandeFournisseurDto;
import com.tchindaClovis.gestiondestock.dto.LigneCommandeFournisseurDto;
import com.tchindaClovis.gestiondestock.model.EEtatCommande;
import com.tchindaClovis.gestiondestock.services.CommandeFournisseurService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

@RestController
public class CommandeFournisseurController implements CommandeFournisseurApi {

    private CommandeFournisseurService commandeFournisseurService;

    @Autowired
    public CommandeFournisseurController(CommandeFournisseurService commandeFournisseurService) {

        this.commandeFournisseurService = commandeFournisseurService;
    }

    @Override
    public CommandeFournisseurDto save(CommandeFournisseurDto dto) {
        return commandeFournisseurService.save(dto);
    }

    @Override
    public CommandeFournisseurDto updateEtatCommande(Integer idCommandeFournisseur, EEtatCommande etatCommande) {
        return commandeFournisseurService.updateEtatCommande(idCommandeFournisseur, etatCommande);
    }

    @Override
    public CommandeFournisseurDto updateQuantiteCommande(Integer idCommandeFournisseur, Integer idLigneCommande, BigDecimal quantite) {
        return commandeFournisseurService.updateQuantiteCommande(idCommandeFournisseur, idLigneCommande, quantite);
    }

    @Override
    public CommandeFournisseurDto updateFournisseur(Integer idCommandeFournisseur, Integer idFournisseur) {
        return commandeFournisseurService.updateFournisseur(idCommandeFournisseur, idFournisseur);
    }

    @Override
    public CommandeFournisseurDto updateArticle(Integer idCommandeFournisseur, Integer idLigneCommande, Integer idArticle) {
        return commandeFournisseurService.updateArticle(idCommandeFournisseur, idLigneCommande, idArticle);
    }


    @Override
    public CommandeFournisseurDto findById(Integer id) {
        return commandeFournisseurService.findById(id);
    }

//    @Override
//    public CommandeFournisseurDto findByCode(String code) {
//        return commandeFournisseurService.findByCode(code);
//    }

    @Override
    public List<CommandeFournisseurDto> findAll() {
        return commandeFournisseurService.findAll();
    }

    @Override
    public List<CommandeFournisseurDto> findAllCommandeFournisseurByIdEntreprise(Integer idEntreprise) {

        return commandeFournisseurService.findAllCommandeFournisseurByIdEntreprise(idEntreprise);
    }

    @Override
    public List<LigneCommandeFournisseurDto> findAllLignesCommandesFournisseurByCommandeFournisseurId(Integer idCommandeFournisseur) {
        return commandeFournisseurService.findAllLignesCommandesFournisseurByCommandeFournisseurId(idCommandeFournisseur);
    }

    @Override
    public void delete(Integer id) {
        commandeFournisseurService.delete(id);
    }

    @Override
    public CommandeFournisseurDto deleteArticle(Integer idCommandeFournisseur, Integer idLigneCommande) {
        return commandeFournisseurService.deleteArticle(idCommandeFournisseur, idLigneCommande);
    }

    @Override
    public String getLastCodeCommandeFournisseur() {
        return commandeFournisseurService.getLastCodeCommandeFournisseur();
    }
}
