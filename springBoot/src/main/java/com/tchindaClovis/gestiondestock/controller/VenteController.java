package com.tchindaClovis.gestiondestock.controller;

import com.tchindaClovis.gestiondestock.controller.api.VenteApi;
import com.tchindaClovis.gestiondestock.dto.ArticleDto;
import com.tchindaClovis.gestiondestock.dto.LigneCommandeClientDto;
import com.tchindaClovis.gestiondestock.dto.LigneVenteDto;
import com.tchindaClovis.gestiondestock.dto.VenteDto;
import com.tchindaClovis.gestiondestock.services.VenteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
public class VenteController implements VenteApi {
    private VenteService venteService;

    @Autowired
    public VenteController(VenteService venteService) {
        this.venteService = venteService;
    }

    @Override
    public VenteDto save(VenteDto dto) {
        return venteService.save(dto);
    }

    @Override
    public VenteDto findById(Integer idVente) {
        return venteService.findById(idVente);
    }

    @Override
    public VenteDto findByCode(String codeVente) {
        return venteService.findByCode(codeVente);
    }

    @Override
    public List<VenteDto> findAll() {
        return venteService.findAll();
    }

    @Override
    public List<VenteDto> findAllVenteByIdEntreprise(Integer idEntreprise) {

        return venteService.findAllVenteByIdEntreprise(idEntreprise);
    }

    @Override
    public ResponseEntity<List<LigneVenteDto>> findAllLignesVentesByVenteId(Integer idVente) {
        return ResponseEntity.ok(venteService.findAllLignesVentesByVenteId(idVente));
    }

    @Override
    public void delete(Integer idVente) {
        venteService.delete(idVente);
    }

    @Override
    public String getLastCodeVente() {
        return venteService.getLastCodeVente();
    }
}
