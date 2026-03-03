//package com.tchindaClovis.gestiondestock.controller.api;
//
//import com.tchindaClovis.gestiondestock.dto.CategoryDto;
//import io.swagger.v3.oas.annotations.Operation;
//import io.swagger.v3.oas.annotations.media.ArraySchema;
//import io.swagger.v3.oas.annotations.media.Content;
//import io.swagger.v3.oas.annotations.media.Schema;
//import io.swagger.v3.oas.annotations.responses.ApiResponse;
//import io.swagger.v3.oas.annotations.tags.Tag;
//import org.springframework.http.MediaType;
//import org.springframework.web.bind.annotation.*;
//import java.util.List;
//import static com.tchindaClovis.gestiondestock.utils.Constants.APP_ROOT;
//
//@Tag(name = "Categories", description = "API de gestion des categories")
//public interface CategoryApi {
//    @PostMapping(value = APP_ROOT + "/categories/create",
//            consumes = MediaType.APPLICATION_JSON_VALUE,
//            produces = MediaType.APPLICATION_JSON_VALUE)
//    @Operation(
//            summary = "Enregistrer une categorie",
//            description = "Cette méthode permet d'enregistrer ou modifier une categorie",
//            responses = {
//                @ApiResponse(responseCode = "200", description = "La categorie créé / modifié",
//                    content = @Content(schema = @Schema(implementation = CategoryDto.class))),
//                @ApiResponse(responseCode = "400", description = "La categorie n'est pas valide"),
//                @ApiResponse(responseCode = "500", description = "Erreur interne du serveur")
//            }
//    )
//    CategoryDto save(@RequestBody CategoryDto dto);
//
//    @GetMapping(value = APP_ROOT + "/categories/{idCategory}",
//            produces = MediaType.APPLICATION_JSON_VALUE)
//    @Operation(
//            summary = "Rechercher une categorie par ID",
//            description = "Cette méthode permet de rechercher une categorie par son ID",
//            responses = {
//                @ApiResponse(responseCode = "200", description = "La categorie a été trouvé dans la BDD",
//                    content = @Content(schema = @Schema(implementation = CategoryDto.class))),
//                @ApiResponse(responseCode = "404", description = "Aucune categorie trouvé dans la BDD avec l'ID fourni")
//            }
//    )
//    CategoryDto findById(@PathVariable("idCategory") Integer id);
//
//    @GetMapping(value = APP_ROOT + "/categories/{codeCategory}",
//            produces = MediaType.APPLICATION_JSON_VALUE)
//    @Operation(
//            summary = "Rechercher une categorie par code",
//            description = "Cette méthode permet de rechercher une categorie par son code",
//            responses = {
//                @ApiResponse(responseCode = "200", description = "L'article a été trouvé dans la BDD",
//                    content = @Content(schema = @Schema(implementation = CategoryDto.class))),
//                @ApiResponse(responseCode = "404", description = "Aucune categorie trouvé avec le code fourni")
//            }
//    )
//        CategoryDto findByCode(@PathVariable("codeCategory")  String code);






//    @GetMapping(value = APP_ROOT + "/categories/all",
//            produces = MediaType.APPLICATION_JSON_VALUE)
//    @Operation(
//            summary = "Lister toutes les categories",
//            description = "Cette méthode permet de renvoyer la liste des categories de la BDD",
//            responses = {
//                @ApiResponse(responseCode = "200", description = "Liste des categories ou liste vide",
//                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = CategoryDto.class))))
//            }
//    )
//    List<CategoryDto> findAll();
//
//    @DeleteMapping(value = APP_ROOT + "/categories/delete/{idCategory}")
//    @Operation(
//            summary = "Supprimer une categorie",
//            description = "Cette méthode permet de supprimer une categorie par son ID",
//            responses = {
//                @ApiResponse(responseCode = "200", description = "La categorie  a été supprimé")
//            }
//    )
//    void delete(@PathVariable("idCategory") Integer id);
//}



package com.tchindaClovis.gestiondestock.controller.api;

import com.tchindaClovis.gestiondestock.dto.CategoryDto;
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

@Tag(name = "Categories", description = "API de gestion des categories")
public interface CategoryApi {

    @PostMapping(value = APP_ROOT + "/categories/create",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Enregistrer une categorie",
            description = "Cette méthode permet d'enregistrer ou modifier une categorie",
            responses = {
                    @ApiResponse(responseCode = "200", description = "La categorie créé / modifié",
                            content = @Content(schema = @Schema(implementation = CategoryDto.class))),
                    @ApiResponse(responseCode = "400", description = "La categorie n'est pas valide"),
                    @ApiResponse(responseCode = "500", description = "Erreur interne du serveur")
            }
    )
    CategoryDto save(@RequestBody CategoryDto dto);

    // ✅ URL UNIQUE pour findById
    @GetMapping(value = APP_ROOT + "/categories/id/{idCategory}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Rechercher une categorie par ID",
            description = "Cette méthode permet de rechercher une categorie par son ID",
            responses = {
                    @ApiResponse(responseCode = "200", description = "La categorie a été trouvé dans la BDD",
                            content = @Content(schema = @Schema(implementation = CategoryDto.class))),
                    @ApiResponse(responseCode = "404", description = "Aucune categorie trouvé dans la BDD avec l'ID fourni")
            }
    )
    CategoryDto findById(@PathVariable("idCategory") Integer id);

    // ✅ URL UNIQUE pour findByCode
    @GetMapping(value = APP_ROOT + "/categories/code/{codeCategory}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Rechercher une categorie par code",
            description = "Cette méthode permet de rechercher une categorie par son code",
            responses = {
                    @ApiResponse(responseCode = "200", description = "La categorie a été trouvé dans la BDD",
                            content = @Content(schema = @Schema(implementation = CategoryDto.class))),
                    @ApiResponse(responseCode = "404", description = "Aucune categorie trouvé avec le code fourni")
            }
    )
    CategoryDto findByCode(@PathVariable("codeCategory") String code);


    //imposer les catégories
//    CategoryDto findByCode(
//            @Parameter(
//                    name = "codeCategory",
//                    in = ParameterIn.PATH,
//                    description = "Accepted values: CAT1, CAT2, CAT3",
//                    schema = @Schema(
//                            type = "string",
//                            allowableValues = {"CAT1", "CAT2", "CAT3"},
//                            example = "CAT1"
//                    )
//            )
//            @PathVariable("codeCategory") String code
//    );


    @GetMapping(value = APP_ROOT + "/categories/all",
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Lister toutes les categories",
            description = "Cette méthode permet de renvoyer la liste des categories de la BDD",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Liste des categories ou liste vide",
                            content = @Content(array = @ArraySchema(schema = @Schema(implementation = CategoryDto.class))))
            }
    )
    List<CategoryDto> findAll();

    @DeleteMapping(value = APP_ROOT + "/categories/delete/{idCategory}")
    @Operation(
            summary = "Supprimer une categorie",
            description = "Cette méthode permet de supprimer une categorie par son ID",
            responses = {
                    @ApiResponse(responseCode = "200", description = "La categorie a été supprimé")
            }
    )
    void delete(@PathVariable("idCategory") Integer id);
}