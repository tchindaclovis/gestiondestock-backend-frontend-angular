package com.tchindaClovis.gestiondestock.controller.api;

import com.tchindaClovis.gestiondestock.dto.LigneCommandeClientDto;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

import static com.tchindaClovis.gestiondestock.utils.Constants.APP_ROOT;

@Tag(name = "LigneCommandeClients", description = "API de gestion des lignes de commande client")
public interface LigneCmdClientApi {
    @GetMapping(value = APP_ROOT + "/lignecommandeclients/filter/identreprise/{idEntreprise}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    List<LigneCommandeClientDto> findAllLigneCommandeClientByIdEntreprise(
            @PathVariable("idEntreprise") Integer idEntreprise);
}
