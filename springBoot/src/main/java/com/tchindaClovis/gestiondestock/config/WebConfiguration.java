package com.tchindaClovis.gestiondestock.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Classe de configuration Web globale.
 *
 * Cette classe permet de configurer différents aspects de Spring MVC,
 * notamment la configuration CORS (Cross-Origin Resource Sharing).
 */
@Configuration // Indique que cette classe contient des Beans de configuration Spring
public class WebConfiguration {

    /**
     * Bean WebMvcConfigurer
     *
     * Cette méthode retourne une implémentation personnalisée
     * de l’interface WebMvcConfigurer afin de configurer CORS globalement.
     *
     * @return configuration CORS appliquée à toute l'application
     */
    @Bean
    public WebMvcConfigurer corsConfig() {

        // On retourne une classe anonyme qui implémente WebMvcConfigurer
        return new WebMvcConfigurer() {

            /**
             * Méthode appelée automatiquement par Spring
             * pour enregistrer les règles CORS.
             *
             * @param registry registre permettant de définir
             *                 les règles CORS pour chaque route.
             */
            @Override
            public void addCorsMappings(CorsRegistry registry) {

                registry

                        // 🔹 Applique la configuration à toutes les routes
                        // "/**" signifie : toutes les URL de l’application
                        .addMapping("/**")

                        // 🔹 Autorise uniquement cette origine
                        // Ici : ton frontend Angular exécuté sur localhost:4200
                        .allowedOrigins("http://localhost:4200")

                        // 🔹 Autorise les méthodes HTTP suivantes
                        // OPTIONS est important pour les requêtes preflight
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")

                        // 🔹 Autorise tous les headers envoyés par le client
                        // (ex: Authorization, Content-Type)
                        .allowedHeaders("*")

                        // 🔹 Autorise l’envoi des credentials (cookies, Authorization header)
                        // Nécessaire si tu utilises JWT ou cookies d’authentification
                        .allowCredentials(true);
            }
        };
    }
}











//package com.tchindaClovis.gestiondestock.config;
//
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.web.servlet.config.annotation.CorsRegistry;
//import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
//
//@Configuration
//public class WebConfiguration {
//    @Bean
//    public WebMvcConfigurer corsConfig() {
//        return new WebMvcConfigurer() {
//            @Override
//            public void addCorsMappings(CorsRegistry registry) {
//                registry.addMapping("/**") // toutes les routes
//                        .allowedOrigins("http://localhost:4200") // ton frontend Angular
//                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
//                        .allowedHeaders("*")
//                        .allowCredentials(true);
//            }
//        };
//    }
//}
