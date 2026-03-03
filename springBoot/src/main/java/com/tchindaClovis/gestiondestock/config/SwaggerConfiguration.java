package com.tchindaClovis.gestiondestock.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.Components;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfiguration {

    public static final String AUTHORIZATION_HEADER = "Authorization";

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Gestion de stock REST API documentation")
                        .description("""
                                Cette API permet de gérer les utilisateurs d'un système de gestion. Elle offre diverses fonctionnalités pour administrer les utilisateurs, leur rôle et leur sécurité.
                                                                
                                ### Fonctionnalités :
                                -	Créer des profils pour chaque entreprise.
                                -	Une entreprise a un ou plusieurs utilisateurs
                                -	Paramétrer les catégories d’articles (produits)\s
                                -	Une entreprise a un ou plusieurs articles (produits)
                                -	Une entreprise a un ou plusieurs clients ou utilisateur

                                -	Passer des commande clients
                                    -	Une commande client a un seul client
                                    -	Une commande client a un ou plusieurs articles (produits)
                                    -	Une commande client effectue une sortie de stock pour les articles en question

                                -	Passer des commandes fournisseurs
                                    -	Une commande fournisseur a un seul fournisseur
                                    -	Une commande fournisseur a un ou plusieurs articles (produits)
                                    -	Une commande fournisseur effectue une entrée de stock pour les articles en question

                                -	Effectuer les ventes au magasin
                                    -	Une vente a un ou plusieurs articles (produits)
                                    -	Une vente effectue une sortie de stock pour les articles en question

                                -	Consulter l’état de stock de chaque entreprise
                                    -	Voir la quantité de stock de l’article en temps réel\s
                                    -	Effectuer les corrections de stock (mettre à jour le stock)

                                                                
                                ### Utilisateurs par défaut :
                                Une fois l'application lancée, une entreprise peut se faire enregistrer\s
                                 et cela va créer automatiquement un utilisateur avec le role ADMIN qui \s
                                 pourra se faire authentifier pour avoir accès aus autres fonctionnalités \s
                                 de l'application.

                                                                
                                ### Technologies utilisées :
                                - **JAVA17** pour le langage de développement du backend
                                - **Spring Boot** pour le développement du backend
                                - **JPA & Hibernate** pour l'interaction avec la base de données
                                - **Swagger/OpenAPI** pour la documentation de l'API
                                - **Spring Security** pour la gestion des rôles et de la sécurité
                                - **Lombok** pour réduire le code boilerplate
                                                                
                                Cette API est conçue pour être utilisée par des développeurs souhaitant gérer les utilisateurs dans leurs applications, tout en garantissant une intégration facile grâce à des mécanismes de sécurité avancés.
                                """)
                        .version("v1"))
                .addSecurityItem(new SecurityRequirement().addList("JWT"))
                .components(new Components()
                        .addSecuritySchemes("JWT",
                                new SecurityScheme()
                                        .name(AUTHORIZATION_HEADER)
                                        .type(SecurityScheme.Type.APIKEY)
                                        .in(SecurityScheme.In.HEADER)
                                        .description("JWT token")
                        ));
    }


    @Bean
    public GroupedOpenApi publicApi() {
        return GroupedOpenApi.builder()
                .group("REST API V1")
                .packagesToScan("com.tchindaClovis.gestiondestock")
                .pathsToMatch("/**")
                .build();
    }
}





//package com.tchindaClovis.gestiondestock.config;
//
//import io.swagger.v3.oas.annotations.OpenAPIDefinition;
//import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
//import io.swagger.v3.oas.annotations.info.Info;
//import io.swagger.v3.oas.annotations.security.SecurityScheme;
//import io.swagger.v3.oas.annotations.security.SecurityRequirement;
//import io.swagger.v3.oas.models.Components;
//import io.swagger.v3.oas.models.OpenAPI;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
//@Configuration

//public class SwaggerConfiguration {
//
//    public static final String AUTHORIZATION_HEADER = "Authorization";
//
//    @Bean
//    public OpenAPI customOpenAPI() {
//        return new OpenAPI()
//                .info(new io.swagger.v3.oas.models.info.Info()
//                        .title("Gestion de stock REST API")
//                        .description("Gestion de stock API documentation")
//                        .version("v1"))
//                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
//                .components(new Components()
//                        .addSecuritySchemes("bearerAuth", new io.swagger.v3.oas.models.security.SecurityScheme()
//                                .name(AUTHORIZATION_HEADER)
//                                .type(io.swagger.v3.oas.models.security.SecurityScheme.Type.HTTP)
//                                .scheme("bearer")
//                                .bearerFormat("JWT")
//                                .in(io.swagger.v3.oas.models.security.SecurityScheme.In.HEADER)
//                        ));
//    }
//}
//@OpenAPIDefinition(
//        info = @Info(
//                title = "Gestion de stock REST API",
//                description = "Gestion de stock API documentation",
//                version = "v1"
//        ),
//        security = @SecurityRequirement(name = "bearerAuth")
//)
//@SecurityScheme(
//        name = "bearerAuth",
//        type = SecuritySchemeType.HTTP,
//        scheme = "bearer",
//        bearerFormat = "JWT"
//)







//package com.tchindaClovis.gestiondestock.config;
//
//        import io.swagger.v3.oas.models.OpenAPI;
//        import io.swagger.v3.oas.models.info.Info;
//        import io.swagger.v3.oas.models.security.SecurityScheme;
//        import io.swagger.v3.oas.models.security.SecurityRequirement;
//        import io.swagger.v3.oas.models.Components;
//        import org.springframework.context.annotation.Bean;
//        import org.springframework.context.annotation.Configuration;
//        import org.springframework.context.annotation.Primary;
//
//@Configuration
//public class SwaggerConfiguration {
//
//    @Bean
//    @Primary
//    public OpenAPI customOpenAPI() {
//        return new OpenAPI()
//                .info(new Info()
//                        .title("Gestion de stock REST API")
//                        .description("Gestion de stock API documentation")
//                        .version("v1"))
//                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
//                .components(new Components()
//                        .addSecuritySchemes("bearerAuth",
//                                new SecurityScheme()
//                                        .name("Authorization")
//                                        .type(SecurityScheme.Type.HTTP)
//                                        .scheme("bearer")
//                                        .bearerFormat("JWT")
//                                        .in(SecurityScheme.In.HEADER)
//                        ));
//    }
//}




//package com.tchindaClovis.gestiondestock.config;
//
//import io.swagger.v3.oas.models.OpenAPI;
//import io.swagger.v3.oas.models.info.Info;
//import org.springdoc.core.models.GroupedOpenApi;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.context.annotation.Primary;
//import static com.tchindaClovis.gestiondestock.utils.Constants.APP_ROOT;
//
//@Configuration
//public class SwaggerConfiguration {
//
//    /**
//     * Configuration des informations générales de l'API
//     */
//    @Bean
//    @Primary
//    public OpenAPI customOpenAPI() {
//        return new OpenAPI()
//                .info(new Info()
//                        .title("Gestion de stock REST API")
//                        .description("Gestion de stock API documentation")
//                        .version("v1"));
//    }
//
//    /**
//     * Définition d’un groupe d’API (équivalent de Docket dans Springfox)
//     */
//    @Bean
//    public GroupedOpenApi gestionStockApi() {
//        return GroupedOpenApi.builder()
//                .group("REST API V1")
//                .packagesToScan("com.tchindaClovis.gestiondestock.controller")
//                .pathsToMatch("/**")
//                .build();
//    }
//}





//                 🔄 Correspondance des éléments :
//        SpringFox (Swagger 2)	        SpringDoc (OpenAPI 3)
//        @EnableSwagger2	            @OpenAPIDefinition ou configuration via OpenAPI bean
//        Docket	                    OpenAPI
//        ApiInfoBuilder	            Info dans OpenAPI
//        ApiKey	                    SecurityScheme avec type HTTP et scheme bearer
//        SecurityContext	            SecurityRequirement
//        SecurityReference	        Implémenté automatiquement




//package com.example.GestionClinique.configuration.swaggerConfig;
//
//
//import io.swagger.v3.oas.models.Components;
//import io.swagger.v3.oas.models.ExternalDocumentation;
//import io.swagger.v3.oas.models.OpenAPI;
//import io.swagger.v3.oas.models.info.Info;
//import io.swagger.v3.oas.models.info.License;
//import io.swagger.v3.oas.models.security.SecurityRequirement;
//import io.swagger.v3.oas.models.security.SecurityScheme;
//import org.springdoc.core.models.GroupedOpenApi;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.context.annotation.Primary;
//
//@Configuration
//public class SwaggerConfiguration { // Renamed from SwaggerConfig for clarity
//
//    @Bean
//    @Primary //
//    public OpenAPI customOpenAPI() {
//        return new OpenAPI()
//                .info(new io.swagger.v3.oas.models.info.Info()
//                        .title("API avec JWT")
//                        .version("1.0"))
//                 .components(new Components()
//                        .addSecuritySchemes("BearerAuth", new SecurityScheme()
//                                .name("Authorization")
//                                .type(SecurityScheme.Type.HTTP)
//                                .scheme("bearer")
//                                .bearerFormat("JWT")))
//                .addSecurityItem(new SecurityRequirement().addList("BearerAuth"));
//    }
//
//    @Bean
//    public OpenAPI gestionCliniqueOpenAPI() {
//        return new OpenAPI()
//                .info(new Info().title("Gestion Clinique Rest API") // Your title
//                        .description("Documentation Api de Gestion d'une clinique") // Your description
//                        .version("v0.0.1") // Your application version
//                        .license(new License().name("Apache 2.0").url("https://springdoc.org")))
//                .externalDocs(new ExternalDocumentation()
//                        .description("Gestion Clinique Wiki Documentation")
//                        .url("https://springdoc.org/")); // Or your actual wiki URL
//    }
//
//
//    @Bean
//    public GroupedOpenApi publicApi() {
//        return GroupedOpenApi.builder()
//                .group("Gestion Clinique API") // The name of your group
//                .pathsToMatch("/**") // Document all paths. Adjust if you have a specific base path, e.g., "/api/**"
//                .packagesToScan("com.example.GestionClinique") // Scan your main package
//                .build();
//    }
//
//}




//package com.tchindaClovis.gestiondestock.config;
//
//import org.springframework.context.annotation.Configuration;
//import springfox.documentation.builders.ApiInfoBuilder;
//import springfox.documentation.builders.PathSelectors;
//import springfox.documentation.builders.RequestHandlerSelectors;
//import springfox.documentation.spi.DocumentationType;
//import springfox.documentation.spring.web.plugins.Docket;
//import springfox.documentation.swagger2.annotations.EnableSwagger2;
//
//import static com.tchindaClovis.gestiondestock.utils.Constants.APP_ROOT;
//
//@Configuration
//@EnableSwagger2 // activer Swagger ou pour dire à spring d'exécuter cette classe au démarrage de l'application
//public class SwaggerConfiguration {
//
//    public Docket api(){
//        return new Docket(DocumentationType.SWAGGER_2)
//                .apiInfo(
//                        new ApiInfoBuilder()
//                                .description("Gestion de stock API documentation")
//                                .title("Gestion de stock REST API")
//                                .build()
//                )
//                .groupName("REST API V1")
//                .select()
//                .apis(RequestHandlerSelectors.basePackage("com.tchindaClovis.gestiondestock"))
//                .paths(PathSelectors.ant(APP_ROOT + "/**"))
//                .build();
//    }
//}
