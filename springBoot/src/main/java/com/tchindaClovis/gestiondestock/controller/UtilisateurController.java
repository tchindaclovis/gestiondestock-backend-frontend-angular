package com.tchindaClovis.gestiondestock.controller;

import com.tchindaClovis.gestiondestock.controller.api.UtilisateurApi;
import com.tchindaClovis.gestiondestock.dto.ChangerMotDePasseUtilisateurDto;
import com.tchindaClovis.gestiondestock.dto.UtilisateurDto;
import com.tchindaClovis.gestiondestock.services.UtilisateurService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
public class UtilisateurController implements UtilisateurApi {

    private UtilisateurService utilisateurService;

    @Autowired
    public UtilisateurController(UtilisateurService utilisateurService) {
        this.utilisateurService = utilisateurService;
    }

    @Override
    public UtilisateurDto save(UtilisateurDto dto) {
        return utilisateurService.save(dto);
    }

    @Override
    public UtilisateurDto changerMotDePasse(ChangerMotDePasseUtilisateurDto dto) {
        return utilisateurService.changerMotDePasse(dto);
    }

    @Override
    public UtilisateurDto findById(Integer id) {
        return utilisateurService.findById(id);
    }

    @Override
    public UtilisateurDto findByNom(String nom) {
        return utilisateurService.findByNom(nom);
    }

    @Override
    public UtilisateurDto findByStatut(String statut) {
        return utilisateurService.findByStatut(statut);
    }


    @Override
    public ResponseEntity<UtilisateurDto> findByEmail(@PathVariable("email") String email) {

        UtilisateurDto dto = utilisateurService.findByEmail(email);

        return ResponseEntity.ok(dto);
    }


    @Override
    public List<UtilisateurDto> findAll() {
        return utilisateurService.findAll();
    }


    @Override
    public List<UtilisateurDto> findAllUtilisateurByIdEntreprise(Integer idEntreprise) {

        return utilisateurService.findAllUtilisateurByIdEntreprise(idEntreprise);
    }

    @Override
    public void delete(Integer id) {
        utilisateurService.delete(id);
    }

}
