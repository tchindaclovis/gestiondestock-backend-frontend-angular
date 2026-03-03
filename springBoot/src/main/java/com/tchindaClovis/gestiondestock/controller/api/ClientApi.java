package com.tchindaClovis.gestiondestock.controller.api;

import com.tchindaClovis.gestiondestock.dto.ClientDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.tchindaClovis.gestiondestock.utils.Constants.APP_ROOT;
@Tag(name = "Clients", description = "API de gestion des clients")
//@RestController
public interface ClientApi {
    @PostMapping(value = APP_ROOT + "/clients/create",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Enregistrer un client",
            description = "Cette méthode permet d'enregistrer ou modifier un client",
            responses = {
                @ApiResponse(responseCode = "200", description = "L'objet client créé / modifié",
                    content = @Content(schema = @Schema(implementation = ClientDto.class))),
                @ApiResponse(responseCode = "400", description = "L'objet client n'est pas valide"),
                @ApiResponse(responseCode = "500", description = "Erreur interne du serveur")
            }
    )
    ClientDto save(@RequestBody ClientDto dto);

    @GetMapping(value = APP_ROOT + "/clients/{idClient}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Rechercher un client par ID",
            description = "Cette méthode permet de rechercher un client par son ID",
            responses = {
                @ApiResponse(responseCode = "200", description = "Le client a été trouvé dans la BDD",
                    content = @Content(schema = @Schema(implementation = ClientDto.class))),
                @ApiResponse(responseCode = "404", description = "Aucun client trouvé dans la BDD avec l'ID fourni")
            }
    )
    ClientDto findById(@PathVariable("idClient") Integer id);

//    @GetMapping(value = APP_ROOT + "/clients/{nom}",
//            produces = MediaType.APPLICATION_JSON_VALUE)
//    @Operation(
//            summary = "Rechercher un client par nom",
//            description = "Cette méthode permet de rechercher un client par son nom",
//            responses = {
//                @ApiResponse(responseCode = "200", description = "Le nom du client a été trouvé dans la BDD",
//                    content = @Content(schema = @Schema(implementation = ClientDto.class))),
//                @ApiResponse(responseCode = "404", description = "Aucun client trouvé avec le nom fourni")
//            }
//    )
//    ClientDto findByNom(@PathVariable("nom")  String nom);

//    @GetMapping(value = APP_ROOT + "/clients/{prenom}",
//            produces = MediaType.APPLICATION_JSON_VALUE)
//    @Operation(
//            summary = "Rechercher un client par prenom",
//            description = "Cette méthode permet de rechercher un client par son prenom",
//            responses = {
//                @ApiResponse(responseCode = "200", description = "Le prenom du client a été trouvé dans la BDD",
//                    content = @Content(schema = @Schema(implementation = ClientDto.class))),
//                @ApiResponse(responseCode = "404", description = "Aucun client trouvé avec le prenom fourni")
//            }
//    )
//    ClientDto findByPrenom(@PathVariable("prenom")  String prenom);

    @GetMapping(value = APP_ROOT + "/clients/all",
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Lister tous les clients",
            description = "Cette méthode permet de renvoyer la liste des clients de la BDD",
            responses = {
                @ApiResponse(responseCode = "200", description = "Liste des clients ou liste vide",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = ClientDto.class))))
            }
    )
    List<ClientDto> findAll();

    @DeleteMapping(value = APP_ROOT + "/clients/delete/{idClient}")
    @Operation(
            summary = "Supprimer un client",
            description = "Cette méthode permet de supprimer un client par son ID",
            responses = {
                @ApiResponse(responseCode = "200", description = "Le client a été supprimé")
            }
    )
    void delete(@PathVariable("idClient") Integer id);
}
