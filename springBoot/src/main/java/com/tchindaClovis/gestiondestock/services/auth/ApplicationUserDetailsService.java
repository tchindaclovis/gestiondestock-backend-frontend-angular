package com.tchindaClovis.gestiondestock.services.auth;

/*
 * Package services.auth :
 * Contient les classes liées à l’authentification et à Spring Security.
 *
 * Cette classe permet à Spring Security de charger un utilisateur
 * depuis la base de données lors du processus de login.
 */

import com.tchindaClovis.gestiondestock.dto.UtilisateurDto;
import com.tchindaClovis.gestiondestock.model.auth.ExtendedUser;
import com.tchindaClovis.gestiondestock.services.UtilisateurService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;


/*
 * @Service
 *
 * Indique que cette classe est un composant Spring de type Service.
 *
 * Elle sera automatiquement détectée par le Component Scan
 * et enregistrée comme Bean dans le contexte Spring.
 *
 * Spring Security utilisera ce Bean pour authentifier les utilisateurs.
 */
@Service
public class ApplicationUserDetailsService implements UserDetailsService {

    /*
     * Service métier permettant d'accéder aux utilisateurs
     * depuis la base de données.
     */
    private UtilisateurService service;


    /*
     * Injection par constructeur (bonne pratique).
     *
     * @Autowired
     * Permet à Spring d’injecter automatiquement
     * l’implémentation de UtilisateurService.
     *
     * 👉 L’injection par constructeur est recommandée
     * car elle rend la dépendance obligatoire et immuable.
     */
    @Autowired
    public ApplicationUserDetailsService(UtilisateurService service) {
        this.service = service;
    }


    /*
     * Méthode obligatoire de l’interface UserDetailsService.
     *
     * Cette méthode est appelée automatiquement par Spring Security
     * lors du processus d’authentification.
     *
     * Paramètre :
     * - email : correspond au username fourni lors du login.
     *
     * Elle doit retourner un objet UserDetails.
     */
    @Override
    public UserDetails loadUserByUsername(String email)
            throws UsernameNotFoundException {

        /*
         * Recherche de l’utilisateur en base via le service métier.
         *
         * Si l’utilisateur n’existe pas,
         * il est recommandé de lancer UsernameNotFoundException.
         */
        UtilisateurDto utilisateur = service.findByEmail(email);

        if (utilisateur == null) {
            throw new UsernameNotFoundException(
                    "Aucun utilisateur trouvé avec l'email : " + email
            );
        }

        /*
         * Création de la liste des autorités (roles/permissions).
         *
         * SimpleGrantedAuthority :
         * Implémentation concrète représentant un rôle.
         *
         * Exemple :
         * ROLE_ADMIN
         * ROLE_USER
         */
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();

        /*
         * Pour chaque rôle de l'utilisateur,
         * on transforme le nom du rôle en GrantedAuthority.
         */
        utilisateur.getRoles().forEach(role ->
                authorities.add(
                        new SimpleGrantedAuthority(role.getRoleName())
                )
        );

        /*
         * Retour d’un objet UserDetails.
         *
         * Ici on retourne un ExtendedUser,
         * qui est une classe personnalisée implémentant UserDetails.
         *
         * Paramètres :
         * - email
         * - mot de passe
         * - id entreprise (multi-tenant)
         * - liste des rôles
         *
         * ExtendedUser permet d’ajouter des informations
         * supplémentaires au UserDetails standard.
         */
        return new ExtendedUser(
                utilisateur.getEmail(),
                utilisateur.getMotDePasse(),
                utilisateur.getEntreprise().getId(),
                authorities
        );
    }
}






//package com.tchindaClovis.gestiondestock.services.auth;
//
//        import com.tchindaClovis.gestiondestock.exception.EntityNotFoundException;
//        import com.tchindaClovis.gestiondestock.exception.ErrorCodes;
//        import com.tchindaClovis.gestiondestock.model.Utilisateur;
//        import com.tchindaClovis.gestiondestock.repository.UtilisateurRepository;
//        import org.springframework.beans.factory.annotation.Autowired;
//        import org.springframework.security.core.userdetails.User;
//        import org.springframework.security.core.userdetails.UserDetails;
//        import org.springframework.security.core.userdetails.UserDetailsService;
//        import org.springframework.security.core.userdetails.UsernameNotFoundException;
//        import org.springframework.stereotype.Service;
//        import java.util.Collections;
//
//@Service
//public class ApplicationUserDetailsService implements UserDetailsService {
//    @Autowired
//    private UtilisateurRepository repository;
//
//    @Override
//    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException{
//        Utilisateur utilisateur = repository.findByEmail(email).orElseThrow(() ->
//                new EntityNotFoundException("Aucun utilisateur avec l'email fourni", ErrorCodes.UTILISATEUR_NOT_FOUND));
//
//        return new User(utilisateur.getEmail(), utilisateur.getMotDePasse(), Collections.emptyList());
//    }
//}
