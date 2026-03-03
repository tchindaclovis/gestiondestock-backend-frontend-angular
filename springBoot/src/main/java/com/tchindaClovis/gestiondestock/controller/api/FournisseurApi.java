package com.tchindaClovis.gestiondestock.controller.api;

import com.tchindaClovis.gestiondestock.dto.ArticleDto;
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
@Tag(name = "Fournisseurs", description = "API de gestion des fournisseurs")
//@RestController
public interface FournisseurApi {
    @PostMapping(value = APP_ROOT + "/fournisseurs/create",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Enregistrer un fournisseur",
            description = "Cette méthode permet d'enregistrer ou modifier un fournisseur",
            responses = {
                @ApiResponse(responseCode = "200", description = "L'objet fournisseur créé / modifié",
                        content = @Content(schema = @Schema(implementation = FournisseurDto.class))),
                @ApiResponse(responseCode = "400", description = "L'objet fournisseur n'est pas valide"),
                @ApiResponse(responseCode = "500", description = "Erreur interne du serveur")
            }
    )
    FournisseurDto save(@RequestBody FournisseurDto dto);

    @GetMapping(value = APP_ROOT + "/fournisseurs/{idFournisseur}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Rechercher un fournisseur par ID",
            description = "Cette méthode permet de rechercher un fournisseur par son ID",
            responses = {
                @ApiResponse(responseCode = "200", description = "Le fournisseur a été trouvé dans la BDD",
                    content = @Content(schema = @Schema(implementation = FournisseurDto.class))),
                @ApiResponse(responseCode = "404", description = "Aucun fournisseur trouvé dans la BDD avec l'ID fourni")
            }
    )
    FournisseurDto findById(@PathVariable("idFournisseur") Integer id);

//    @GetMapping(value = APP_ROOT + "/fournisseurs/{nom}",
//            produces = MediaType.APPLICATION_JSON_VALUE)
//    @Operation(
//            summary = "Rechercher un fournisseur par nom",
//            description = "Cette méthode permet de rechercher un fournisseur par son nom",
//            responses = {
//                @ApiResponse(responseCode = "200", description = "Le nom du fournisseur a été trouvé dans la BDD",
//                    content = @Content(schema = @Schema(implementation = FournisseurDto.class))),
//                @ApiResponse(responseCode = "404", description = "Aucun fournisseur trouvé avec le nom fourni")
//            }
//    )
//    FournisseurDto findByNom(@PathVariable("nomFournisseur")  String nom);

    @GetMapping(value = APP_ROOT + "/fournisseurs/all",
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Lister toutes les fournitures",
            description = "Cette méthode permet de renvoyer la liste des fournisseurs de la BDD",
            responses = {
                @ApiResponse(responseCode = "200", description = "Liste des fournisseurs ou liste vide",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = FournisseurDto.class))))
            }
    )
    List<FournisseurDto> findAll();

    @DeleteMapping(value = APP_ROOT + "/fournisseurs/delete/{idFournisseur}")
    @Operation(
            summary = "Supprimer un fournisseur",
            description = "Cette méthode permet de supprimer un fournisseur par son ID",
            responses = {
                @ApiResponse(responseCode = "200", description = "Le fournisseur a été supprimé")
            }
    )
    void delete(@PathVariable("idFournisseur") Integer id);
}
