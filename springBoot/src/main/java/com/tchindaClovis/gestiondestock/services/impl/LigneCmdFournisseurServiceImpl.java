package com.tchindaClovis.gestiondestock.services.impl;

import com.tchindaClovis.gestiondestock.dto.LigneCommandeFournisseurDto;
import com.tchindaClovis.gestiondestock.repository.LigneCommandeFournisseurRepository;
import com.tchindaClovis.gestiondestock.services.LigneCmdFournisseurService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class LigneCmdFournisseurServiceImpl implements LigneCmdFournisseurService {
    private LigneCommandeFournisseurRepository ligneCommandeFournisseurRepository;


    @Autowired
    public LigneCmdFournisseurServiceImpl(LigneCommandeFournisseurRepository ligneCommandeFournisseurRepository) {
        this.ligneCommandeFournisseurRepository = ligneCommandeFournisseurRepository;
    }

    @Override
    public List<LigneCommandeFournisseurDto> findAllLigneCommandeFournisseurByIdEntreprise(Integer idEntreprise) {
        if (idEntreprise == null) {
            log.error("Entreprise ID is null");
            return List.of();
        }
        return ligneCommandeFournisseurRepository.findAllByIdEntreprise(idEntreprise).stream()
                .map(LigneCommandeFournisseurDto::fromEntity)
                .collect(Collectors.toList());
    }
}
