package com.tchindaClovis.gestiondestock.controller.api;

import com.tchindaClovis.gestiondestock.dto.CommandeFournisseurDto;
import com.tchindaClovis.gestiondestock.dto.LigneCommandeFournisseurDto;
import com.tchindaClovis.gestiondestock.model.EEtatCommande;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static com.tchindaClovis.gestiondestock.utils.Constants.*;

@Tag(name = "CommandeFournisseurs", description = "API de gestion des commandeFournisseurs")
public interface CommandeFournisseurApi {

    @PostMapping(value = COMMANDE_FOURNISSEUR_ENDPOINT + "/create",
            produces = MediaType.APPLICATION_JSON_VALUE)
    CommandeFournisseurDto save(@RequestBody CommandeFournisseurDto dto);


    @PostMapping(value = COMMANDE_FOURNISSEUR_ENDPOINT + "/createimpact",
            produces = MediaType.APPLICATION_JSON_VALUE)
    CommandeFournisseurDto saveImpact(@RequestBody CommandeFournisseurDto dto);

//    @PostMapping(value = COMMANDE_FOURNISSEUR_ENDPOINT + "/create/{idCommandeFournisseur}/{etatCommande}",
//            produces = MediaType.APPLICATION_JSON_VALUE)
//    CommandeFournisseurDto saveImpact(@PathVariable("idCommandeFournisseur") Integer idCommandeFournisseur,
//                                      @PathVariable("etatCommande") EEtatCommande etatCommande,
//                                      @RequestBody CommandeFournisseurDto dto);

    @PatchMapping(value = COMMANDE_FOURNISSEUR_ENDPOINT + "/update/etat/{idCommandeFournisseur}/{etatCommande}/{dateConfirmation}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    CommandeFournisseurDto updateEtatCommande(@PathVariable("idCommandeFournisseur") Integer idCommandeFournisseur,
                                              @PathVariable("etatCommande") EEtatCommande etatCommande,
                                              @PathVariable("dateConfirmation") Instant dateConfirmation);

    @PatchMapping(value = COMMANDE_FOURNISSEUR_ENDPOINT + "/update/quantite/{idCommandeFournisseur}/{idLigneCommande}/{quantite}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    CommandeFournisseurDto updateQuantiteCommande(@PathVariable("idCommandeFournisseur") Integer idCommandeFournisseur,
                                                  @PathVariable("idLigneCommande") Integer idLigneCommande,
                                                  @PathVariable("quantite") BigDecimal quantite);

    @PatchMapping(value = COMMANDE_FOURNISSEUR_ENDPOINT + "/update/fournisseur/{idCommandeFournisseur}/{idFournisseur}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    CommandeFournisseurDto updateFournisseur(@PathVariable("idCommandeFournisseur") Integer idCommandeFournisseur,
                                             @PathVariable("idFournisseur") Integer idFournisseur);

    @PatchMapping(value = COMMANDE_FOURNISSEUR_ENDPOINT + "/update/article/{idCommandeFournisseur}/{idLigneCommande}/{idArticle}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    CommandeFournisseurDto updateArticle(@PathVariable("idCommandeFournisseur") Integer idCommandeFournisseur,
                                         @PathVariable("idLigneCommande") Integer idLigneCommande,
                                         @PathVariable("idArticle") Integer idArticle);

    @GetMapping(value = COMMANDE_FOURNISSEUR_ENDPOINT + "/{idCommandeFournisseur}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    CommandeFournisseurDto findById(@PathVariable("idCommandeFournisseur") Integer id);

//    @GetMapping(value = COMMANDE_FOURNISSEUR_ENDPOINT + "/{codeCommandeFournisseur}")
//    CommandeFournisseurDto findByCode(@PathVariable("codeCommandeFournisseur") String code);

    @GetMapping(value = COMMANDE_FOURNISSEUR_ENDPOINT + "/all",
            produces = MediaType.APPLICATION_JSON_VALUE)
    List<CommandeFournisseurDto> findAll();

    @GetMapping(value = APP_ROOT + "/commandefournisseurs/filter/entreprise/{idEntreprise}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    List<CommandeFournisseurDto> findAllCommandeFournisseurByIdEntreprise(@PathVariable("idEntreprise") Integer idEntreprise);

    @GetMapping(value = COMMANDE_FOURNISSEUR_ENDPOINT + "/lignescommande/{idCommandeFournisseur}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    List<LigneCommandeFournisseurDto> findAllLignesCommandesFournisseurByCommandeFournisseurId(@PathVariable("idCommandeFournisseur") Integer idCommandeFournisseur);


    @DeleteMapping(value = COMMANDE_FOURNISSEUR_ENDPOINT + "/delete/{idCommandeFournisseur}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    void delete(@PathVariable("idCommandeFournisseur") Integer id);

    @DeleteMapping(value = COMMANDE_FOURNISSEUR_ENDPOINT + "/delete/article/{idCommandeFournisseur}/{idLigneCommande}")
    CommandeFournisseurDto deleteArticle(@PathVariable("idCommandeFournisseur") Integer idCommandeFournisseur, @PathVariable("idLigneCommande") Integer idLigneCommande);


    @GetMapping(value = APP_ROOT + "/commandefournisseurs/lastcodecommandefournisseur",
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary  = "Récupérer le dernier code CommandeFournisseur enregistré",
            description = "Cette méthode permet de récupérer le dernier code au format CMFxxxx",
            responses = {
                    @ApiResponse(responseCode  = "200", description  = "Le dernier code a été récupéré / CMF0000 par défaut")
            }
    )
    String getLastCodeCommandeFournisseur();
}
