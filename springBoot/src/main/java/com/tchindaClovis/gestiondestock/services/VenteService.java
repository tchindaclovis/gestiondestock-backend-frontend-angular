package com.tchindaClovis.gestiondestock.services;

import com.tchindaClovis.gestiondestock.dto.CommandeClientDto;
import com.tchindaClovis.gestiondestock.dto.LigneCommandeClientDto;
import com.tchindaClovis.gestiondestock.dto.LigneVenteDto;
import com.tchindaClovis.gestiondestock.dto.VenteDto;

import java.util.List;

public interface VenteService {
    VenteDto save(VenteDto dto);
    VenteDto findById(Integer idVente);
    VenteDto findByCode(String codeVente);
    List<VenteDto> findAll();
    List<LigneVenteDto> findAllLignesVentesByVenteId(Integer idCommande);
    void delete(Integer idVente);
}
