package com.tchindaClovis.gestiondestock.controller.api;

import com.tchindaClovis.gestiondestock.dto.LigneVenteDto;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

import static com.tchindaClovis.gestiondestock.utils.Constants.APP_ROOT;

@Tag(name = "LigneVentes", description = "API de gestion des lignes de vente")
public interface LigneVenteApi {
    @GetMapping(value = APP_ROOT + "/ligneventes/filter/identreprise/{idEntreprise}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    List<LigneVenteDto> findAllLigneVenteByIdEntreprise(@PathVariable("idEntreprise") Integer idEntreprise);

}
