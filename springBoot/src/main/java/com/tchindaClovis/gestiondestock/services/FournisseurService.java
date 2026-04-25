package com.tchindaClovis.gestiondestock.services;
;
import com.tchindaClovis.gestiondestock.dto.FournisseurDto;
import com.tchindaClovis.gestiondestock.dto.VenteDto;

import java.util.List;

public interface FournisseurService {
    FournisseurDto save(FournisseurDto dto);
    FournisseurDto findById(Integer id);
    FournisseurDto findByNom(String nom);
    FournisseurDto findByStatut(String statut);
    List<FournisseurDto> findAll();

    List<FournisseurDto> findAllFournisseurByIdEntreprise(Integer idEntreprise);
    void delete(Integer id);
}
