package com.tchindaClovis.gestiondestock.config;

// ====================== IMPORTS ======================

// Service personnalisé permettant de charger un utilisateur depuis la base
import com.tchindaClovis.gestiondestock.services.auth.ApplicationUserDetailsService;

// Annotations Spring
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// Sécurité Spring
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;

// Encodage des mots de passe
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

// Configuration du filtre de sécurité
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

// CORS
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

// Import des constantes d’URL pour éviter le hardcoding
import static com.tchindaClovis.gestiondestock.utils.Constants.APP_ROOT;
import static com.tchindaClovis.gestiondestock.utils.Constants.AUTHENTICATION_ENDPOINT;


/**
 * ============================================================
 * Classe principale de configuration de la sécurité
 * ============================================================
 *
 * @Configuration
 *  → Indique que cette classe contient des Beans Spring
 *
 * @EnableWebSecurity
 *  → Active la configuration de sécurité Web dans l'application
 *
 * Cette classe :
 *  - Configure les règles d'accès HTTP
 *  - Active l’authentification JWT
 *  - Désactive les sessions (mode stateless)
 *  - Configure le CORS
 *  - Configure l’encodeur de mot de passe
 */
@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

    /**
     * ============================================================
     * Bean AuthenticationManager
     * ============================================================
     *
     * L'AuthenticationManager est le composant central
     * utilisé par Spring Security pour authentifier un utilisateur.
     *
     * Il est automatiquement configuré par Spring via
     * AuthenticationConfiguration.
     *
     * Ce Bean sera utilisé notamment lors du login.
     */
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authenticationConfiguration) throws Exception {

        // Récupération de l'AuthenticationManager configuré par Spring
        return authenticationConfiguration.getAuthenticationManager();
    }


    /**
     * ============================================================
     * Configuration principale du filtre de sécurité HTTP
     * ============================================================
     *
     * SecurityFilterChain remplace désormais WebSecurityConfigurerAdapter
     * (déprécié dans Spring Security moderne).
     *
     * Cette méthode définit :
     *  - les endpoints publics
     *  - les endpoints protégés
     *  - la gestion des sessions
     *  - l’intégration du filtre JWT
     */
    @Bean
    public SecurityFilterChain filterChain(
            HttpSecurity http,
            AuthenticationManager authenticationManager,
            ApplicationUserDetailsService userDetailsService,
            com.tchindaClovis.gestiondestock.config.ApplicationRequestFilter applicationRequestFilter
    ) throws Exception {

        http

                // =====================================================
                // Activation du CORS
                // Permet au frontend Angular (localhost:4200)
                // d’appeler le backend
                // =====================================================
                .cors().and()

                // =====================================================
                // Désactivation CSRF
                // Inutile en API REST stateless (JWT)
                // =====================================================
                .csrf(csrf -> csrf.disable())

                // =====================================================
                // Configuration des règles d'autorisation
                // =====================================================
                .authorizeHttpRequests(auth -> auth

                        // Endpoints accessibles sans authentification
                        .requestMatchers(
                                "/" + AUTHENTICATION_ENDPOINT + "/authenticate", // login
                                "/" + APP_ROOT + "/entreprises/create",        // création entreprise
                                "/" + APP_ROOT + "/utilisateurs/update/password",
                                "/v2/api-docs",
                                "/swagger-resources/**",
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/webjars/**",
                                "/api/files/upload",
                                "/api/files/delete/**"
                        ).permitAll()

                        // Toutes les autres requêtes nécessitent authentification
                        .anyRequest().authenticated()
                )

                // =====================================================
                // Configuration de la gestion des sessions
                // =====================================================
                .sessionManagement(session -> session

                        // STATELESS → pas de session HTTP
                        // Chaque requête doit contenir un JWT valide
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // =====================================================
                // Injection de l'AuthenticationManager personnalisé
                // =====================================================
                .authenticationManager(authenticationManager)

                // =====================================================
                // Ajout du filtre JWT personnalisé
                //
                // Il sera exécuté AVANT UsernamePasswordAuthenticationFilter
                // afin de vérifier le token dans chaque requête.
                // =====================================================
                .addFilterBefore(
                        applicationRequestFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        // Construction finale de la chaîne de filtres
        return http.build();
    }


    /**
     * ============================================================
     * Configuration CORS personnalisée
     * ============================================================
     *
     * Permet d’autoriser le frontend Angular à appeler l’API.
     *
     * Ici :
     *  - Autorise localhost:4200
     *  - Autorise tous les headers
     *  - Autorise toutes les méthodes HTTP
     *  - Autorise les credentials (cookies, headers d’auth)
     */
    @Bean
    public CorsFilter corsFilter() {

        CorsConfiguration config = new CorsConfiguration();

        // Origine autorisée (frontend Angular)
        config.addAllowedOrigin("http://localhost:4200");

        // Autoriser tous les headers
        config.addAllowedHeader("*");

        // Autoriser toutes les méthodes HTTP (GET, POST, PUT, DELETE...)
        config.addAllowedMethod("*");

        // Autoriser les credentials (important pour JWT dans headers)
        config.setAllowCredentials(true);

        // Appliquer cette configuration à toutes les routes
        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration("/**", config);

        return new CorsFilter(source);
    }


    /**
     * ============================================================
     * Bean PasswordEncoder
     * ============================================================
     *
     * BCrypt est un algorithme de hachage sécurisé.
     *
     * Pourquoi l’utiliser ?
     *  - Le mot de passe n’est jamais stocké en clair
     *  - Protection contre attaques par rainbow tables
     *  - Recommandé en production
     */
    @Bean
    public PasswordEncoder passwordEncoder() {

        return new BCryptPasswordEncoder();
    }
}





//package com.tchindaClovis.gestiondestock.config;
//
//import com.tchindaClovis.gestiondestock.services.auth.ApplicationUserDetailsService;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.security.authentication.AuthenticationManager;
//import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
//import org.springframework.security.config.http.SessionCreationPolicy;
//import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.security.web.SecurityFilterChain;
//import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
//import org.springframework.web.cors.CorsConfiguration;
//import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
//import org.springframework.web.filter.CorsFilter;
//import static com.tchindaClovis.gestiondestock.utils.Constants.APP_ROOT;
//import static com.tchindaClovis.gestiondestock.utils.Constants.AUTHENTICATION_ENDPOINT;
//
//@Configuration
//@EnableWebSecurity
//public class SecurityConfiguration {
//
//    @Bean
//    public AuthenticationManager authenticationManager(
//            AuthenticationConfiguration authenticationConfiguration) throws Exception {
//        return authenticationConfiguration.getAuthenticationManager();
//    }
//
//    @Bean
//    public SecurityFilterChain filterChain(HttpSecurity http,
//                                           AuthenticationManager authenticationManager,
//                                           ApplicationUserDetailsService userDetailsService,
//                                           com.tchindaClovis.gestiondestock.config.ApplicationRequestFilter applicationRequestFilter) throws Exception {
//
//        http
//                .cors().and()
//                .csrf(csrf -> csrf.disable())
//                .authorizeHttpRequests(auth -> auth
//                        .requestMatchers(
//                                "/" + AUTHENTICATION_ENDPOINT + "/authenticate",
//                                "/" + APP_ROOT + "/entreprises/create",
//                                "/" + APP_ROOT + "/utilisateurs/update/password",
//                                "/v2/api-docs",
//                                "/swagger-resources/**",
//                                "/swagger-ui/**",
//                                "/v3/api-docs/**",
//                                "/webjars/**",
//                                "/api/files/upload",
//                                "/api/files/delete/**"
//                        ).permitAll()
//                        .anyRequest().authenticated()
//                )
//                .sessionManagement(session -> session
//                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
//                )
//                // on utilise l'AuthenticationManager déjà injecté
//                .authenticationManager(authenticationManager)
//                // ajout du filtre JWT personnalisé
//                .addFilterBefore(applicationRequestFilter, UsernamePasswordAuthenticationFilter.class);
//
//        return http.build();
//    }
//
//    @Bean
//    public CorsFilter corsFilter() {
//        CorsConfiguration config = new CorsConfiguration();
//        config.addAllowedOrigin("http://localhost:4200");
//        config.addAllowedHeader("*");
//        config.addAllowedMethod("*");
//        config.setAllowCredentials(true);
//
//        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
//        source.registerCorsConfiguration("/**", config);
//        return new CorsFilter(source);
//    }
//
//    @Bean
//    public PasswordEncoder passwordEncoder() {
//        return new BCryptPasswordEncoder();
//    }
//}

