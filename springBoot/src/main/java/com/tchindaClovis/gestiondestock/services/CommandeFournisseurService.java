package com.tchindaClovis.gestiondestock.services;

import com.tchindaClovis.gestiondestock.dto.CommandeClientDto;
import com.tchindaClovis.gestiondestock.dto.CommandeFournisseurDto;
import com.tchindaClovis.gestiondestock.dto.LigneCommandeFournisseurDto;
import com.tchindaClovis.gestiondestock.model.EEtatCommande;

import java.math.BigDecimal;
import java.util.List;
public interface CommandeFournisseurService {
    CommandeFournisseurDto save(CommandeFournisseurDto dto);

    CommandeFournisseurDto updateEtatCommande(Integer idCommandeFournisseur, EEtatCommande etatCommande);

    CommandeFournisseurDto updateQuantiteCommande(Integer idCommandeFournisseur, Integer idLigneCommande, BigDecimal quantite);

    CommandeFournisseurDto updateFournisseur(Integer idCommandeFournisseur, Integer idFournisseur);

    CommandeFournisseurDto updateArticle(Integer idCommandeFournisseur, Integer idLigneCommande, Integer idArticle);
    CommandeFournisseurDto findById(Integer id);
    CommandeFournisseurDto findByCode(String code);
    List<CommandeFournisseurDto> findAll();
    List<LigneCommandeFournisseurDto> findAllLignesCommandesFournisseurByCommandeFournisseurId(Integer idCommandeFournisseur);
    void delete(Integer id);
    // Delete article ==> delete LigneCommandeFournisseur
    CommandeFournisseurDto deleteArticle(Integer idCommandeFournisseur, Integer idLigneCommande);

    String getLastCodeCommandeFournisseur();

    List<CommandeFournisseurDto> findAllCommandeFournisseurByIdEntreprise(Integer idEntreprise);

}
