package com.tchindaClovis.gestiondestock.services;

import com.tchindaClovis.gestiondestock.dto.ChangerMotDePasseUtilisateurDto;
import com.tchindaClovis.gestiondestock.dto.ClientDto;
import com.tchindaClovis.gestiondestock.dto.UtilisateurDto;
import java.util.List;

public interface UtilisateurService {
    UtilisateurDto save(UtilisateurDto dto);
    UtilisateurDto findById(Integer id);
    UtilisateurDto findByNom(String  nom);
    UtilisateurDto findByStatut(String statut);
    List<UtilisateurDto> findAll();

    List<UtilisateurDto> findAllUtilisateurByIdEntreprise(Integer idEntreprise);
    UtilisateurDto findByEmail(String email);
    void delete(Integer id);

    UtilisateurDto changerMotDePasse(ChangerMotDePasseUtilisateurDto dto);
}
