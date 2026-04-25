package com.tchindaClovis.gestiondestock.services.impl;

import com.tchindaClovis.gestiondestock.dto.LigneCommandeClientDto;
import com.tchindaClovis.gestiondestock.repository.LigneCommandeClientRepository;
import com.tchindaClovis.gestiondestock.services.LigneCmdClientService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class LigneCmdClientServiceImpl implements LigneCmdClientService {

    private LigneCommandeClientRepository ligneCommandeClientRepository;


    @Autowired
    public LigneCmdClientServiceImpl(LigneCommandeClientRepository ligneCommandeClientRepository) {
        this.ligneCommandeClientRepository = ligneCommandeClientRepository;
    }

    @Override
    public List<LigneCommandeClientDto> findAllLigneCommandeClientByIdEntreprise(Integer idEntreprise) {
        if (idEntreprise == null) {
            log.error("Entreprise ID is null");
            return List.of();
        }
        return ligneCommandeClientRepository.findAllByIdEntreprise(idEntreprise).stream()
                .map(LigneCommandeClientDto::fromEntity)
                .collect(Collectors.toList());
    }
}
