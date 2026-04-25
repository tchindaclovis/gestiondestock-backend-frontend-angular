package com.tchindaClovis.gestiondestock.controller.api;

import com.tchindaClovis.gestiondestock.dto.ChangerMotDePasseUtilisateurDto;
import com.tchindaClovis.gestiondestock.dto.UtilisateurDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import static com.tchindaClovis.gestiondestock.utils.Constants.APP_ROOT;

@Tag(name = "Utilisateurs", description = "API de gestion des utilisateurs")
public interface UtilisateurApi {
    @PostMapping(value = APP_ROOT + "/utilisateurs/create",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Enregistrer un utilisateur",
            description = "Cette méthode permet d'enregistrer ou modifier un utilisateur",
            responses = {
                @ApiResponse(responseCode = "200", description = "L'objet utilisateur créé / modifié",
                        content = @Content(schema = @Schema(implementation = UtilisateurDto.class))),
                @ApiResponse(responseCode = "400", description = "L'objet utilisateur n'est pas valide"),
                @ApiResponse(responseCode = "500", description = "Erreur interne du serveur")
            }
    )
    UtilisateurDto save(@RequestBody UtilisateurDto dto);


    @PostMapping(APP_ROOT + "/utilisateurs/update/password")
    UtilisateurDto changerMotDePasse(@RequestBody ChangerMotDePasseUtilisateurDto dto);


    @GetMapping(value = APP_ROOT + "/utilisateurs/find/idutilisateur/{idUtilisateur}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Rechercher un utilisateur par ID",
            description = "Cette méthode permet de rechercher un utilisateur par son ID",
            responses = {
                @ApiResponse(responseCode = "200", description = "L'utilisateur a été trouvé dans la BDD",
                        content = @Content(schema = @Schema(implementation = UtilisateurDto.class))),
                @ApiResponse(responseCode = "404", description = "Aucun utilisateur trouvé dans la BDD avec l'ID fourni")
            }
    )
    UtilisateurDto findById(@PathVariable("idUtilisateur") Integer id);


    @GetMapping(APP_ROOT + "/utilisateurs/find/nomutilisateur/{nom}")
    UtilisateurDto findByNom(@PathVariable("nom") String nom);



    @GetMapping(value = APP_ROOT + "/utilisateurs/find/statututilisateur/{statut}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Rechercher un utilisateur par statut",
            description = "Cette méthode permet de rechercher un utilisateur  par son statut",
            responses = {
                @ApiResponse(responseCode = "200", description = "Le statut de l'utilisateur a été trouvé dans la BDD",
                        content = @Content(schema = @Schema(implementation = UtilisateurDto.class))),
                @ApiResponse(responseCode = "404", description = "Aucun utilisateur trouvé avec le statut fourni")
            }
    )
    UtilisateurDto findByStatut(@PathVariable("statutUtilisateur")  String statut);


    @GetMapping(APP_ROOT + "/utilisateurs/find/emailutilisateur/{email}")
    ResponseEntity<UtilisateurDto> findByEmail(@PathVariable("email") String email);


    @GetMapping(value = APP_ROOT + "/utilisateurs/all",
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Lister tous les utilisateurs",
            description = "Cette méthode permet de renvoyer la liste des utilisateurs de la BDD",
            responses = {
                @ApiResponse(responseCode = "200", description = "Liste des utilisateurs ou liste vide",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = UtilisateurDto.class))))
            }
    )
    List<UtilisateurDto> findAll();


    @GetMapping(value = APP_ROOT + "/utilisateurs/filter/identreprise/{idEntreprise}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    List<UtilisateurDto> findAllUtilisateurByIdEntreprise(@PathVariable("idEntreprise") Integer idEntreprise);

    @DeleteMapping(value = APP_ROOT + "/utilisateurs/delete/{idUtilisateur}")
    @Operation(
            summary = "Supprimer un utilisateur",
            description = "Cette méthode permet de supprimer un utilisateur par son ID",
            responses = {
                @ApiResponse(responseCode = "200", description = "Le utilisateur a été supprimé")
            }
    )
    void delete(@PathVariable("idUtilisateur") Integer id);
}
