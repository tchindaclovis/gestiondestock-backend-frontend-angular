package com.tchindaClovis.gestiondestock.controller.api;

import com.tchindaClovis.gestiondestock.dto.ArticleDto;
import com.tchindaClovis.gestiondestock.dto.LigneCommandeClientDto;
import com.tchindaClovis.gestiondestock.dto.LigneCommandeFournisseurDto;
import com.tchindaClovis.gestiondestock.dto.LigneVenteDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import static com.tchindaClovis.gestiondestock.utils.Constants.APP_ROOT;

@Tag(name = "Articles", description = "API de gestion des articles")
public interface ArticleApi {

    @PostMapping(value = APP_ROOT + "/articles/create",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Enregistrer un article",
            description = "Cette méthode permet d'enregistrer ou modifier un article",
            responses = {
                @ApiResponse(responseCode = "200", description = "L'objet article créé / modifié",
                    content = @Content(schema = @Schema(implementation = ArticleDto.class))),
                @ApiResponse(responseCode = "400", description = "L'objet article n'est pas valide"),
                @ApiResponse(responseCode = "500", description = "Erreur interne du serveur")
            }
    )
    ArticleDto save(@RequestBody ArticleDto dto);

    @GetMapping(value = APP_ROOT + "/articles/idArticle/{idArticle}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Rechercher un article par ID",
            description = "Cette méthode permet de rechercher un article par son ID",
            responses = {
                @ApiResponse(responseCode = "200", description = "L'article a été trouvé dans la BDD",
                    content = @Content(schema = @Schema(implementation = ArticleDto.class))),
                @ApiResponse(responseCode = "404", description = "Aucun article trouvé dans la BDD avec l'ID fourni")
            }
    )
    ArticleDto findById(@PathVariable("idArticle") Integer id);

    @GetMapping(value = APP_ROOT + "/articles/codeArticle/{codeArticle}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Rechercher un article par code",
            description = "Cette méthode permet de rechercher un article par son code",
            responses = {
                @ApiResponse(responseCode = "200", description = "L'article a été trouvé dans la BDD",
                    content = @Content(schema = @Schema(implementation = ArticleDto.class))),
                @ApiResponse(responseCode = "404", description = "Aucun article trouvé avec le code fourni")
            }
    )
    ArticleDto findByCodeArticle(@PathVariable("codeArticle") String codeArticle);


    @GetMapping(value = APP_ROOT + "/articles/all",
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Lister tous les articles",
            description = "Cette méthode permet de renvoyer la liste des articles de la BDD",
            responses = {
                @ApiResponse(responseCode = "200", description = "Liste des articles ou liste vide",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = ArticleDto.class))))
            }
    )
    List<ArticleDto> findAll();

    @GetMapping(value = APP_ROOT + "/articles/historique/vente/{idArticle}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    List<LigneVenteDto> findHistoriqueVentes(@PathVariable("idArticle") Integer idArticle);

    @GetMapping(value = APP_ROOT + "/articles/historique/commandeclient/{idArticle}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    List<LigneCommandeClientDto> findHistoriaueCommandeClient(@PathVariable("idArticle") Integer idArticle);

    @GetMapping(value = APP_ROOT + "/articles/historique/commandefournisseur/{idArticle}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    List<LigneCommandeFournisseurDto> findHistoriqueCommandeFournisseur(@PathVariable("idArticle") Integer idArticle);

    @GetMapping(value = APP_ROOT + "/articles/filter/category/{idCategory}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    List<ArticleDto> findAllArticleByIdCategory(@PathVariable("idCategory") Integer idCategory);

    @DeleteMapping(value = APP_ROOT + "/articles/delete/{idArticle}")
    @Operation(
            summary = "Supprimer un article",
            description = "Cette méthode permet de supprimer un article par son ID",
            responses = {
                @ApiResponse(responseCode = "200", description = "L'article a été supprimé")
            }
    )
    void delete(@PathVariable("idArticle") Integer id);
}





//package com.tchindaClovis.gestiondestock.controller.api;
//
//import com.tchindaClovis.gestiondestock.dto.ArticleDto;
//import io.swagger.v3.oas.annotations.Operation;
//import io.swagger.v3.oas.annotations.responses.ApiResponse;
//import io.swagger.v3.oas.annotations.responses.ApiResponses;
//import io.swagger.v3.oas.annotations.tags.Tag;
//import org.springframework.http.MediaType;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//
//import static com.tchindaClovis.gestiondestock.utils.Constants.APP_ROOT;
//
//@Tag(name = "Articles", description = "Gestion des articles de stock")
////@RequestMapping(APP_ROOT + "/articles")
//public interface ArticleApi {
//
//    @PostMapping(value = APP_ROOT + "/articles/create",
//            consumes = MediaType.APPLICATION_JSON_VALUE,
//            produces = MediaType.APPLICATION_JSON_VALUE)
//    @Operation(summary = "Enregistrer un article",
//            description = "Cette méthode permet d'enregistrer ou modifier un article")
//    @ApiResponses(value = {
//            @ApiResponse(responseCode = "200", description = "L'objet article créé / modifié"),
//            @ApiResponse(responseCode = "400", description = "L'objet article n'est pas valide")
//    })
//    ArticleDto save(@RequestBody ArticleDto dto);
//
//    @GetMapping(value = APP_ROOT + "/articles/{idArticle}", produces = MediaType.APPLICATION_JSON_VALUE)
//    @Operation(summary = "Rechercher un article par ID",
//            description = "Cette méthode permet de rechercher un article par son ID")
//    @ApiResponses(value = {
//            @ApiResponse(responseCode = "200", description = "L'article a été trouvé"),
//            @ApiResponse(responseCode = "404", description = "Aucun article trouvé avec cet ID")
//    })
//    ArticleDto findById(@PathVariable("idArticle") Integer id);
//
//    @GetMapping(value = APP_ROOT + "/articles/{codeArticle}", produces = MediaType.APPLICATION_JSON_VALUE)
//    @Operation(summary = "Rechercher un article par code",
//            description = "Cette méthode permet de rechercher un article par son code")
//    @ApiResponses(value = {
//            @ApiResponse(responseCode = "200", description = "L'article a été trouvé"),
//            @ApiResponse(responseCode = "404", description = "Aucun article trouvé avec ce code")
//    })
//    ArticleDto findByCodeArticle(@PathVariable("codeArticle") String codeArticle);
//
//    @GetMapping(value = APP_ROOT + "/articles/all", produces = MediaType.APPLICATION_JSON_VALUE)
//    @Operation(summary = "Lister les articles",
//            description = "Cette méthode renvoie la liste de tous les articles")
//    @ApiResponse(responseCode = "200", description = "Liste des articles (ou vide)")
//    List<ArticleDto> findAll();
//
//    @DeleteMapping(value =APP_ROOT + "/articles/delete/{idArticle}")
//    @Operation(summary = "Supprimer un article",
//            description = "Cette méthode permet de supprimer un article par son ID")
//    @ApiResponse(responseCode = "200", description = "L'article a été supprimé")
//    void delete(@PathVariable("idArticle") Integer id);
//}



//package com.tchindaClovis.gestiondestock.controller.api;
//
//import com.tchindaClovis.gestiondestock.dto.ArticleDto;
//import io.swagger.annotations.Api;
//import io.swagger.annotations.ApiOperation;
//import io.swagger.annotations.ApiResponse;
//import io.swagger.annotations.ApiResponses;
//import org.springframework.http.MediaType;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.PathVariable;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestBody;
//import java.util.List;
//import static com.tchindaClovis.gestiondestock.utils.Constants.APP_ROOT;
//@Api(APP_ROOT +"/articles")
//public interface ArticleApi {
//    @PostMapping(value = APP_ROOT + "/articles/create",
//            consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
//    @ApiOperation(value = "Enregistrer un article",
//            notes = "Cette methode permet d'enregistrer ou modifier un article", response = ArticleDto.class)
//    @ApiResponses(value = {
//            @ApiResponse(code = 200, message = "L'objet article cree / modifie"),
//            @ApiResponse(code = 400, message = "L'objet article n'est pas valide")
//    })
//    ArticleDto save(@RequestBody ArticleDto dto);
//
//    @GetMapping(value = APP_ROOT + "/articles/{idArticle}",produces = MediaType.APPLICATION_JSON_VALUE)
//    @ApiOperation(value = "Rechercher un article par ID",
//            notes = "Cette methode permet de rechercher un article par son ID", response = ArticleDto.class)
//    @ApiResponses(value = {
//            @ApiResponse(code = 200, message = "L'article a été trouvé dans la BDD"),
//            @ApiResponse(code = 404, message = "Aucun article n'existe dans la BDD avec l'ID fourni")
//    })
//    ArticleDto findById(@PathVariable("idArticle") Integer id);
//
//    @GetMapping(value = APP_ROOT + "/articles/{codeArticle}",produces = MediaType.APPLICATION_JSON_VALUE)
//    @ApiOperation(value = "Rechercher un article par code",
//            notes = "Cette methode permet de rechercher un article par son ID", response = ArticleDto.class)
//    @ApiResponses(value = {
//            @ApiResponse(code = 200, message = "L'article a été trouvé dans la BDD"),
//            @ApiResponse(code = 404, message = "Aucun article n'existe dans la BDD avec le code fourni")
//    })
//    ArticleDto findByCodeArticle(@PathVariable("codeArticle")  String codeArticle);
//
//    @GetMapping(value = APP_ROOT + "/articles/all",produces = MediaType.APPLICATION_JSON_VALUE)
//    @ApiOperation(value = "Renvoi la liste des articles",
//            notes = "Cette methode permet de chercher et renvoyer la liste des articles qui existent " +
//                    "dans la BDD", responseContainer = "List<ArticleDto>")
//    @ApiResponses(value = {
//            @ApiResponse(code = 200, message = "La liste des article / une liste vide")
//    })
//    List<ArticleDto> findAll();
//
//    @GetMapping(value = APP_ROOT + "/articles/delete/{idArticle}")
//    @ApiOperation(value = "Supprimer un article",
//            notes = "Cette methode permet de supprimer un article par son ID") //pas besoin de reponse car c'est un void
//    @ApiResponses(value = {
//            @ApiResponse(code = 200, message = "L'article a été supprimé")
//    })
//    void delete(@PathVariable("idArticle") Integer id);
//}
