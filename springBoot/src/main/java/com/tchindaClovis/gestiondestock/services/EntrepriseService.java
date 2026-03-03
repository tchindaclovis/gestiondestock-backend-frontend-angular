package com.tchindaClovis.gestiondestock.services;

import com.tchindaClovis.gestiondestock.dto.EntrepriseDto;
import java.util.List;

public interface EntrepriseService {
    EntrepriseDto save(EntrepriseDto dto);
    EntrepriseDto findById(Integer id);
    EntrepriseDto findByNom(String nom);
    EntrepriseDto findByCodeFiscal(String codeFiscal);
    List<EntrepriseDto> findAll();
    void delete(Integer id);
}
