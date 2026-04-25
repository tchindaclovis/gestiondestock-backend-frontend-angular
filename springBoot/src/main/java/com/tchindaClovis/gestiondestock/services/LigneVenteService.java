package com.tchindaClovis.gestiondestock.services;

import com.tchindaClovis.gestiondestock.dto.LigneVenteDto;

import java.util.List;

public interface LigneVenteService {
    List<LigneVenteDto> findAllLigneVenteByIdEntreprise(Integer idEntreprise);
}
