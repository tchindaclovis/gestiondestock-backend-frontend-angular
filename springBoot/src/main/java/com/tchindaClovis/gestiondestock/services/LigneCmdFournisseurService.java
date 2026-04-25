package com.tchindaClovis.gestiondestock.services;

import com.tchindaClovis.gestiondestock.dto.LigneCommandeFournisseurDto;

import java.util.List;

public interface LigneCmdFournisseurService {
    List<LigneCommandeFournisseurDto> findAllLigneCommandeFournisseurByIdEntreprise(Integer idEntreprise);
}
