package com.tchindaClovis.gestiondestock.controller;

import com.tchindaClovis.gestiondestock.controller.api.CommandeClientApi;
import com.tchindaClovis.gestiondestock.dto.ArticleDto;
import com.tchindaClovis.gestiondestock.dto.CommandeClientDto;
import com.tchindaClovis.gestiondestock.dto.LigneCommandeClientDto;
import com.tchindaClovis.gestiondestock.model.EEtatCommande;
import com.tchindaClovis.gestiondestock.services.CommandeClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

@RestController
public class CommandeClientController implements CommandeClientApi {
    private CommandeClientService commandeClientService;

    @Autowired
    public CommandeClientController(CommandeClientService commandeClientService) {
        this.commandeClientService = commandeClientService;
    }

    @Override
    public ResponseEntity<CommandeClientDto> save(CommandeClientDto dto) {
        return ResponseEntity.ok(commandeClientService.save(dto));

        //pour ne pas renvoyer l'objet qui a été créé (avantage du ResponseEntity)
//        commandeClientService.save(dto);
//        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Override
    public ResponseEntity<CommandeClientDto> updateEtatCommande(Integer idCommandeClient, EEtatCommande etatCommande) {
        return ResponseEntity.ok(commandeClientService.updateEtatCommande(idCommandeClient, etatCommande));
    }

    @Override
    public ResponseEntity<CommandeClientDto> updateQuantiteCommande(Integer idCommandeClient, Integer idLigneCommande, BigDecimal quantite) {
        return ResponseEntity.ok(commandeClientService.updateQuantiteCommande(idCommandeClient, idLigneCommande, quantite));
    }

    @Override
    public ResponseEntity<CommandeClientDto> updateClient(Integer idCommandeClient, Integer idClient) {
        return ResponseEntity.ok(commandeClientService.updateClient(idCommandeClient, idClient));
    }

    @Override
    public ResponseEntity<CommandeClientDto> deleteArticle(Integer idCommandeClient, Integer idLigneCommande) {
        return ResponseEntity.ok(commandeClientService.deleteArticle(idCommandeClient, idLigneCommande));
    }

    @Override
    public ResponseEntity<CommandeClientDto> updateArticle(Integer idCommandeClient, Integer idLigneCommande, Integer idArticle) {
        return ResponseEntity.ok(commandeClientService.updateArticle(idCommandeClient, idLigneCommande, idArticle));
    }

    @Override
    public ResponseEntity<CommandeClientDto> findById(Integer id) {
        return ResponseEntity.ok(commandeClientService.findById(id));
    }

//    @Override
//    public ResponseEntity<CommandeClientDto> findByCode(String code) {
//        return ResponseEntity.ok(commandeClientService.findByCode(code));
//    }

    @Override
    public ResponseEntity<List<CommandeClientDto>> findAll() {
        return ResponseEntity.ok(commandeClientService.findAll());
    }

    @Override
    public List<CommandeClientDto> findAllCommandeClientByIdEntreprise(Integer idEntreprise) {

        return commandeClientService.findAllCommandeClientByIdEntreprise(idEntreprise);
    }

    @Override
    public ResponseEntity<List<LigneCommandeClientDto>> findAllLignesCommandesClientByCommandeClientId(Integer idCommandeClient) {
        return ResponseEntity.ok(commandeClientService.findAllLignesCommandesClientByCommandeClientId(idCommandeClient));
    }

    @Override
    public ResponseEntity delete(Integer id) {
        commandeClientService.delete(id);
        return ResponseEntity.ok().build();
    }

    @Override
    public String getLastCodeCommandeClient() {
        return commandeClientService.getLastCodeCommandeClient();
    }
}
