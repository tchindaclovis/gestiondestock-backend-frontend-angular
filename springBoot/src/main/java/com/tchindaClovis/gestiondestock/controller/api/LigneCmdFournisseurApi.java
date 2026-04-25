package com.tchindaClovis.gestiondestock.controller.api;

import com.tchindaClovis.gestiondestock.dto.LigneCommandeFournisseurDto;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

import static com.tchindaClovis.gestiondestock.utils.Constants.APP_ROOT;

@Tag(name = "LigneCommandeFournisseurs", description = "API de gestion des lignes de commande fournisseur")
public interface LigneCmdFournisseurApi {
    @GetMapping(value = APP_ROOT + "/lignecommandefournisseurs/filter/identreprise/{idEntreprise}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    List<LigneCommandeFournisseurDto> findAllLigneCommandeFournisseurByIdEntreprise(
            @PathVariable("idEntreprise") Integer idEntreprise);
}
