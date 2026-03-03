package com.tchindaClovis.gestiondestock.services;
;
import com.tchindaClovis.gestiondestock.dto.FournisseurDto;
import java.util.List;

public interface FournisseurService {
    FournisseurDto save(FournisseurDto dto);
    FournisseurDto findById(Integer id);
    FournisseurDto findByNom(String nom);
    List<FournisseurDto> findAll();
    void delete(Integer id);
}
