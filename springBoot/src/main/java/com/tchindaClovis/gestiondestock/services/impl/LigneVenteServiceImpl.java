package com.tchindaClovis.gestiondestock.services.impl;

import com.tchindaClovis.gestiondestock.dto.LigneVenteDto;
import com.tchindaClovis.gestiondestock.repository.LigneVenteRepository;
import com.tchindaClovis.gestiondestock.services.LigneVenteService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class LigneVenteServiceImpl implements LigneVenteService {
    private LigneVenteRepository ligneVenteRepository;


    @Autowired
    public LigneVenteServiceImpl(LigneVenteRepository ligneVenteRepository) {
        this.ligneVenteRepository = ligneVenteRepository;
    }

    @Override
    public List<LigneVenteDto> findAllLigneVenteByIdEntreprise(Integer idEntreprise) {
        if (idEntreprise == null) {
            log.error("Entreprise ID is null");
            return List.of();
        }
        return ligneVenteRepository.findAllByIdEntreprise(idEntreprise).stream()
                .map(LigneVenteDto::fromEntity)
                .collect(Collectors.toList());
    }
}
