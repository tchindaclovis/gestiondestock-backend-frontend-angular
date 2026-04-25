package com.tchindaClovis.gestiondestock.services;

import com.tchindaClovis.gestiondestock.dto.LigneCommandeClientDto;
import com.tchindaClovis.gestiondestock.dto.LigneCommandeFournisseurDto;

import java.util.List;

public interface LigneCmdClientService {
    List<LigneCommandeClientDto> findAllLigneCommandeClientByIdEntreprise(Integer idEntreprise);
}
