package com.tchindaClovis.gestiondestock.services;

import com.tchindaClovis.gestiondestock.dto.*;

import java.util.List;

public interface VenteService {
    VenteDto save(VenteDto dto);
    VenteDto findById(Integer idVente);
    VenteDto findByCode(String codeVente);
    List<VenteDto> findAll();

    List<VenteDto> findAllVenteByIdEntreprise(Integer idEntreprise);
    List<LigneVenteDto> findAllLignesVentesByVenteId(Integer idCommande);
    void delete(Integer idVente);

    String getLastCodeVente();
}
