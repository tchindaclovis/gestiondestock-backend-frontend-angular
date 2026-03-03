package com.tchindaClovis.gestiondestock.controller.api;

import com.tchindaClovis.gestiondestock.dto.ArticleDto;
import com.tchindaClovis.gestiondestock.dto.ClientDto;
import com.tchindaClovis.gestiondestock.dto.EntrepriseDto;
import com.tchindaClovis.gestiondestock.dto.FournisseurDto;
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
@Tag(name = "Entreprises", description = "API de gestion des entreprises")
//@RestController
public interface EntrepriseApi {
    @PostMapping(value = APP_ROOT + "/entreprises/create",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Enregistrer une entreprise",
            description = "Cette méthode permet d'enregistrer ou modifier une entreprise",
            responses = {
                @ApiResponse(responseCode = "200", description = "L'objet entreprise créé / modifié",
                    content = @Content(schema = @Schema(implementation = EntrepriseDto.class))),
                @ApiResponse(responseCode = "400", description = "L'objet entreprise n'est pas valide"),
                @ApiResponse(responseCode = "500", description = "Erreur interne du serveur")
            }
    )
    EntrepriseDto save(@RequestBody EntrepriseDto dto);

    @GetMapping(value = APP_ROOT + "/entreprises/{idEntreprise}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Rechercher une entreprise par ID",
            description = "Cette méthode permet de rechercher une entreprise par son ID",
            responses = {
                @ApiResponse(responseCode = "200", description = "L'entreprise a été trouvé dans la BDD",
                    content = @Content(schema = @Schema(implementation = EntrepriseDto.class))),
                @ApiResponse(responseCode = "404", description = "Aucune entreprise trouvé dans la BDD avec l'ID fourni")
            }
    )
    EntrepriseDto findById(@PathVariable("idEntreprise") Integer id);


    @GetMapping(value = APP_ROOT + "/entreprises/{nom}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Rechercher une entreprise par nom",
            description = "Cette méthode permet de rechercher une entreprise par son nom",
            responses = {
                @ApiResponse(responseCode = "200", description = "Le nom de l'entreprise a été trouvé dans la BDD",
                    content = @Content(schema = @Schema(implementation = EntrepriseDto.class))),
                @ApiResponse(responseCode = "404", description = "Aucune entreprise trouvé avec le nom fourni")
            }
    )
    EntrepriseDto findByNom(@PathVariable("nomEntreprise")  String nom);

    @GetMapping(value = APP_ROOT + "/entreprises/{codeFiscal}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Rechercher une entreprise par code fiscal",
            description = "Cette méthode permet de rechercher une entreprise par son code fiscal",
            responses = {
                @ApiResponse(responseCode = "200", description = "L'article a été trouvé dans la BDD",
                    content = @Content(schema = @Schema(implementation = EntrepriseDto.class))),
                @ApiResponse(responseCode = "404", description = "Aucun article trouvé avec le code fiscal fourni")
            }
    )
    EntrepriseDto findByCodeFiscal(@PathVariable("codeFiscal")  String codeFiscal);

    @GetMapping(value = APP_ROOT + "/entreprises/all",
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Lister toutes les entreprise",
            description = "Cette méthode permet de renvoyer la liste des entreprises de la BDD",
            responses = {
                @ApiResponse(responseCode = "200", description = "Liste des entreprises ou liste vide",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = EntrepriseDto.class))))
            }
    )
    List<EntrepriseDto> findAll();

    @DeleteMapping(value = APP_ROOT + "/entreprises/delete/{idEntreprise}")
    @Operation(
            summary = "Supprimer une entreprise",
            description = "Cette méthode permet de supprimer une entreprise par son ID",
            responses = {
                @ApiResponse(responseCode = "200", description = "L'entreprise a été supprimé")
            }
    )
    void delete(@PathVariable("idEntreprise") Integer id);
}
