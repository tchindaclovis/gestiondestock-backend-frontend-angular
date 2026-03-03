package com.tchindaClovis.gestiondestock.controller.api;

import static com.tchindaClovis.gestiondestock.utils.Constants.APP_ROOT;
import com.tchindaClovis.gestiondestock.dto.MvtStockDto;
import java.math.BigDecimal;
import java.util.List;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "MvtStocks", description = "API de gestion des mvtStocks")
public interface MvtStockApi {

    @GetMapping(APP_ROOT + "/mvtstock/stockreel/{idArticle}")
    BigDecimal stockReelArticle(@PathVariable("idArticle") Integer idArticle);

    @GetMapping(APP_ROOT + "/mvtstock/filter/article/{idArticle}")
    List<MvtStockDto> mvtStockArticle(@PathVariable("idArticle") Integer idArticle);

    @PostMapping(APP_ROOT + "/mvtstock/entree")
    MvtStockDto entreeStock(@RequestBody MvtStockDto dto);

    @PostMapping(APP_ROOT + "/mvtstock/sortie")
    MvtStockDto sortieStock(@RequestBody MvtStockDto dto);

    @PostMapping(APP_ROOT + "/mvtstock/correctionpos")
    MvtStockDto correctionStockPos(@RequestBody MvtStockDto dto);

    @PostMapping(APP_ROOT + "/mvtstock/correctionneg")
    MvtStockDto correctionStockNeg(@RequestBody MvtStockDto dto);

}




//package com.tchindaClovis.gestiondestock.controller.api;
//
//import com.tchindaClovis.gestiondestock.dto.ArticleDto;
//import com.tchindaClovis.gestiondestock.dto.ClientDto;
//import com.tchindaClovis.gestiondestock.dto.MvtStockDto;
//import com.tchindaClovis.gestiondestock.dto.UtilisateurDto;
//import io.swagger.v3.oas.annotations.Operation;
//import io.swagger.v3.oas.annotations.media.ArraySchema;
//import io.swagger.v3.oas.annotations.media.Content;
//import io.swagger.v3.oas.annotations.media.Schema;
//import io.swagger.v3.oas.annotations.responses.ApiResponse;
//import io.swagger.v3.oas.annotations.responses.ApiResponses;
//import io.swagger.v3.oas.annotations.tags.Tag;
//import org.springframework.http.MediaType;
//import org.springframework.web.bind.annotation.*;
//
//import java.math.BigDecimal;
//import java.time.Instant;
//import java.util.List;
//
//import static com.tchindaClovis.gestiondestock.utils.Constants.APP_ROOT;
//@Tag(name = "MvtStocks", description = "API de gestion des mvtStocks")
////@RestController
//public interface MvtStockApi {
//    @PostMapping(value = APP_ROOT + "/mvtStocks/create",
//            consumes = MediaType.APPLICATION_JSON_VALUE,
//            produces = MediaType.APPLICATION_JSON_VALUE)
//    @Operation(
//            summary = "Enregistrer un mvtStock",
//            description = "Cette méthode permet d'enregistrer ou modifier un mvtStock",
//            responses = {
//                @ApiResponse(responseCode = "200", description = "L'objet mvtStock créé / modifié",
//                        content = @Content(schema = @Schema(implementation = MvtStockDto.class))),
//                @ApiResponse(responseCode = "400", description = "L'objet mvtStock n'est pas valide"),
//                @ApiResponse(responseCode = "500", description = "Erreur interne du serveur")
//            }
//    )
//    MvtStockDto save(@RequestBody MvtStockDto dto);
//
//    @GetMapping(value = APP_ROOT + "/mvtStocks/{idMvtStock}",
//            produces = MediaType.APPLICATION_JSON_VALUE)
//    @Operation(
//            summary = "Rechercher un mvtStock par ID",
//            description = "Cette méthode permet de rechercher un mvtStock par son ID",
//            responses = {
//                @ApiResponse(responseCode = "200", description = "Le mvtStock a été trouvé dans la BDD",
//                        content = @Content(schema = @Schema(implementation = MvtStockDto.class))),
//                @ApiResponse(responseCode = "404", description = "Aucun mvtStock trouvé dans la BDD avec l'ID fourni")
//            }
//    )
//    MvtStockDto findById(@PathVariable("idMvtStock}") Integer id);
//
//    @GetMapping(value = APP_ROOT + "/mvtStocks/{dateMvt}",
//            produces = MediaType.APPLICATION_JSON_VALUE)
//    @Operation(
//            summary = "Rechercher un mvtStock par date",
//            description = "Cette méthode permet de rechercher un mvtStock par sa date",
//            responses = {
//                @ApiResponse(responseCode = "200", description = "La date du mvtStock a été trouvé dans la BDD",
//                        content = @Content(schema = @Schema(implementation = MvtStockDto.class))),
//                @ApiResponse(responseCode = "404", description = "Aucun mvtStock trouvé avec la date fourni")
//            }
//    )
//    MvtStockDto findByDateMvt(@PathVariable("dateMvt") Instant dateMvt);
//
//    @GetMapping(value = APP_ROOT + "/mvtStocks/{quantite}",
//            produces = MediaType.APPLICATION_JSON_VALUE)
//    @Operation(
//            summary = "Rechercher un mvtStock par quantite",
//            description = "Cette méthode permet de rechercher un mvtStock par sa quantité",
//            responses = {
//                @ApiResponse(responseCode = "200", description = "La quantité du mvtStock a été trouvé dans la BDD",
//                        content = @Content(schema = @Schema(implementation = MvtStockDto.class))),
//                @ApiResponse(responseCode = "404", description = "Aucun mvtStock trouvé avec la quantité fourni")
//            }
//    )
//    MvtStockDto findByQuantite(@PathVariable("quantite") BigDecimal quantite);
//
//    @GetMapping(value = APP_ROOT + "/mvtStocks/all",
//            produces = MediaType.APPLICATION_JSON_VALUE)
//    @Operation(
//            summary = "Lister tous les mvtStocks",
//            description = "Cette méthode permet de renvoyer la liste des mvtStocks de la BDD",
//            responses = {
//                @ApiResponse(responseCode = "200", description = "Liste des mvtStocks ou liste vide",
//                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = MvtStockDto.class))))
//            }
//    )
//    List<MvtStockDto> findAll();
//
//}
