package com.tchindaClovis.gestiondestock.controller.api;

import com.tchindaClovis.gestiondestock.dto.CategoryDto;
import com.tchindaClovis.gestiondestock.dto.VenteDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.tchindaClovis.gestiondestock.utils.Constants.APP_ROOT;

@Tag(name = "Ventes", description = "API de gestion des ventes")
public interface VenteApi {
    @PostMapping(value = APP_ROOT + "/ventes/create",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Enregistrer une ventee",
            description = "Cette méthode permet d'enregistrer ou modifier une vente",
            responses = {
                @ApiResponse(responseCode = "200", description = "La vente créé / modifié",
                        content = @Content(schema = @Schema(implementation = VenteDto.class))),
                @ApiResponse(responseCode = "400", description = "La vente n'est pas valide"),
                @ApiResponse(responseCode = "500", description = "Erreur interne du serveur")
            }
    )
    VenteDto save(@RequestBody VenteDto dto);

    @GetMapping(value = APP_ROOT + "/ventes/{idVente}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Rechercher une vente par ID",
            description = "Cette méthode permet de rechercher une vente par son ID",
            responses = {
                @ApiResponse(responseCode = "200", description = "La vente a été trouvé dans la BDD",
                        content = @Content(schema = @Schema(implementation = VenteDto.class))),
                @ApiResponse(responseCode = "404", description = "Aucune vente trouvé dans la BDD avec l'ID fourni")
            }
    )
    VenteDto findById(@PathVariable("idVente") Integer id);

    @GetMapping(value = APP_ROOT + "/ventes/{codeVente}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Rechercher une vente par code",
            description = "Cette méthode permet de rechercher une vente par son code",
            responses = {
                @ApiResponse(responseCode = "200", description = "L'article a été trouvé dans la BDD",
                        content = @Content(schema = @Schema(implementation = VenteDto.class))),
                @ApiResponse(responseCode = "404", description = "Aucune vente trouvé avec le code fourni")
            }
     )
    VenteDto findByCode(@PathVariable("codeVente")  String code);

    @GetMapping(value = APP_ROOT + "/ventes/all",
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Lister toutes les ventes",
            description = "Cette méthode permet de renvoyer la liste des ventes de la BDD",
            responses = {
                @ApiResponse(responseCode = "200", description = "Liste des ventes ou liste vide",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = VenteDto.class))))
            }
    )
    List<VenteDto> findAll();

    @DeleteMapping(value = APP_ROOT + "/ventes/delete/{idVente}")
    @Operation(
            summary = "Supprimer une vente",
            description = "Cette méthode permet de supprimer une vente par son ID",
            responses = {
                @ApiResponse(responseCode = "200", description = "La vente  a été supprimé")
            }
    )
    void delete(@PathVariable("idVente") Integer id);
}

