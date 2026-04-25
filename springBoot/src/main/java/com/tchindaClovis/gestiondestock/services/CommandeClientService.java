package com.tchindaClovis.gestiondestock.services;

import com.tchindaClovis.gestiondestock.dto.CommandeClientDto;
import com.tchindaClovis.gestiondestock.dto.LigneCommandeClientDto;
import com.tchindaClovis.gestiondestock.model.EEtatCommande;

import java.math.BigDecimal;
import java.util.List;

public interface CommandeClientService {
    CommandeClientDto save(CommandeClientDto dto);
    CommandeClientDto updateEtatCommande(Integer idCommandeClient, EEtatCommande etatCommande);
    CommandeClientDto updateQuantiteCommande(Integer idCommandeClient, Integer idLigneCommande, BigDecimal quantite);

    CommandeClientDto updateClient(Integer idCommandeClient, Integer idClient);

    CommandeClientDto updateArticle(Integer idCommandeClient, Integer idLigneCommande, Integer newIdArticle);

    // Delete article ==> delete LigneCommandeClient
    CommandeClientDto deleteArticle(Integer idCommandeClient, Integer idLigneCommande);
    CommandeClientDto findById(Integer id);
    CommandeClientDto findByCode(String code);
    List<CommandeClientDto> findAll();
    List<LigneCommandeClientDto> findAllLignesCommandesClientByCommandeClientId(Integer idCommandeClient);

    List<CommandeClientDto> findAllCommandeClientByIdEntreprise(Integer idEntreprise);

    void delete(Integer id);

    String getLastCodeCommandeClient();
}
