package com.tchindaClovis.gestiondestock.services;

import com.tchindaClovis.gestiondestock.dto.CommandeClientDto;
import com.tchindaClovis.gestiondestock.dto.VenteDto;

import java.util.List;

public interface VenteService {
    VenteDto save(VenteDto dto);
    VenteDto findById(Integer idVente);
    VenteDto findByCode(String codeVente);
    List<VenteDto> findAll();
    void delete(Integer idVente);
}
