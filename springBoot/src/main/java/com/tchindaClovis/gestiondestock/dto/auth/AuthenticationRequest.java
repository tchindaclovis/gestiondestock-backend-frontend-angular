package com.tchindaClovis.gestiondestock.dto.auth;

/*
 * Package dto.auth :
 * Contient les objets de transfert de données (DTO)
 * liés à l’authentification.
 *
 * Cette classe représente la requête envoyée
 * par le client lors du login.
 */

import lombok.Builder;
import lombok.Data;


/*
 * @Data (Lombok)
 *
 * Génère automatiquement :
 * - Getters
 * - Setters
 * - toString()
 * - equals()
 * - hashCode()
 *
 * Évite d’écrire du code répétitif (boilerplate).
 */
@Data

/*
 * @Builder (Lombok)
 *
 * Implémente le Design Pattern Builder.
 *
 * Permet de créer des objets de manière fluide :
 *
 * Exemple :
 *
 * AuthenticationRequest request = AuthenticationRequest.builder()
 *      .login("user@email.com")
 *      .password("1234")
 *      .build();
 *
 * Avantages :
 * - Code plus lisible
 * - Objet immutable possible
 * - Meilleure gestion des constructeurs avec plusieurs champs
 */
@Builder
public class AuthenticationRequest {

    /*
     * Champ login :
     *
     * Peut représenter :
     * - Email
     * - Username
     * - Identifiant personnalisé
     *
     * Ce champ sera utilisé par Spring Security
     * dans loadUserByUsername().
     */
    private String login;

    /*
     * Champ password :
     *
     * Mot de passe saisi par l’utilisateur.
     *
     * ⚠️ Important :
     * - Il ne doit jamais être renvoyé dans une réponse API.
     * - Il sera comparé avec le mot de passe encodé en base
     *   via PasswordEncoder.
     */
    private String password;
}

