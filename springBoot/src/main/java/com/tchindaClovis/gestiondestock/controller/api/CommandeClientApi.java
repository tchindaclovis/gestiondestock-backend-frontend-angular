package com.tchindaClovis.gestiondestock.controller.api;

import com.tchindaClovis.gestiondestock.dto.ArticleDto;
import com.tchindaClovis.gestiondestock.dto.CommandeClientDto;
import com.tchindaClovis.gestiondestock.dto.LigneCommandeClientDto;
import com.tchindaClovis.gestiondestock.model.EEtatCommande;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import static com.tchindaClovis.gestiondestock.utils.Constants.APP_ROOT;

@Tag(name = "CommandeClients", description = "API de gestion des commandeClients")
public interface CommandeClientApi {
    @PostMapping(value = APP_ROOT + "/commandeclients/create")
    ResponseEntity<CommandeClientDto> save(@RequestBody CommandeClientDto dto);

    @PatchMapping(APP_ROOT + "/commandesclients/update/etat/{idCommandeClient}/{etatCommande}")
    ResponseEntity<CommandeClientDto> updateEtatCommande(@PathVariable("idCommandeClient") Integer idCommandeClient,
                                                         @PathVariable("etatCommande") EEtatCommande etatCommande);

    @PatchMapping(APP_ROOT + "/commandesclients/update/quantite/{idCommandeClient}/{idLigneCommande}/{quantite}")
    ResponseEntity<CommandeClientDto> updateQuantiteCommande(@PathVariable("idCommandeClient") Integer idCommandeClient,
                                                             @PathVariable("idLigneCommande") Integer idLigneCommande,
                                                             @PathVariable("quantite") BigDecimal quantite);

    @PatchMapping(APP_ROOT + "/commandesclients/update/client/{idCommandeClient}/{idClient}")
    ResponseEntity<CommandeClientDto> updateClient(@PathVariable("idCommandeClient") Integer idCommandeClient,
                                                   @PathVariable("idClient") Integer idClient);

    @PatchMapping(APP_ROOT + "/commandesclients/update/article/{idCommandeClient}/{idLigneCommande}/{idArticle}")
    ResponseEntity<CommandeClientDto> updateArticle(@PathVariable("idCommandeClient") Integer idCommandeClient,
                                                    @PathVariable("idLigneCommande") Integer idLigneCommande,
                                                    @PathVariable("idArticle") Integer idArticle);

    @DeleteMapping(APP_ROOT + "/commandesclients/delete/article/{idCommandeClient}/{idLigneCommande}")
    ResponseEntity<CommandeClientDto> deleteArticle(@PathVariable("idCommandeClient") Integer idCommandeClient,
                                                    @PathVariable("idLigneCommande") Integer idLigneCommande);

    @GetMapping(value = APP_ROOT + "/commandeclients/{idCommandeClient}")
    ResponseEntity<CommandeClientDto> findById(@PathVariable("idCommandeClient") Integer id);

//    @GetMapping("/id/{id}") // Ajoutez /id/
//    public ResponseEntity<CommandeClientDto> findById(@PathVariable("id") Integer id) {
//        return ResponseEntity.ok(service.findById(id));
//    }

//    @GetMapping(value = APP_ROOT + "/commandeClients/{codeCommandeClient}")
//    ResponseEntity<CommandeClientDto> findByCode(@PathVariable("codeCommandeClient") String code);

//    @GetMapping("/filter/{code}") // Ajoutez /filter/ ou /code/
//    public ResponseEntity<CommandeClientDto> findByCode(@PathVariable("code") String code) {
//        return ResponseEntity.ok(service.findByCode(code));
//    }
    @GetMapping(value = APP_ROOT + "/commandeclients/all")
    ResponseEntity<List<CommandeClientDto>> findAll();

    @GetMapping(value = APP_ROOT + "/commandeclient/filter/entreprise/{idEntreprise}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    List<CommandeClientDto> findAllCommandeClientByIdEntreprise(@PathVariable("idEntreprise") Integer idEntreprise);

    @GetMapping(APP_ROOT + "/commandesclients/lignesCommande/{idCommandeClient}")
    ResponseEntity<List<LigneCommandeClientDto>> findAllLignesCommandesClientByCommandeClientId(@PathVariable("idCommandeClient") Integer idCommandeClient);

    @DeleteMapping(value = APP_ROOT + "/commandeclients/delete/{idCommandeClient}")
    ResponseEntity delete(@PathVariable("idCommandeClient") Integer id);


    @GetMapping(value = APP_ROOT + "/commandeclients/lastcodecommandeclient")
    @Operation(
            summary  = "Récupérer le dernier code CommandeClient enregistré",
            description = "Cette méthode permet de récupérer le dernier code au format CMCxxxx",
            responses = {
                    @ApiResponse(responseCode  = "200", description  = "Le dernier code a été récupéré / CMC0000 par défaut")
            }
    )
    String getLastCodeCommandeClient();
}
