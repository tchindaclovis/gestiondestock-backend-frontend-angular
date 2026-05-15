package com.tchindaClovis.gestiondestock.config;

import com.tchindaClovis.gestiondestock.model.auth.ExtendedUser;
import com.tchindaClovis.gestiondestock.services.auth.ApplicationUserDetailsService;
import com.tchindaClovis.gestiondestock.utils.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;

//@Component <--- SUPPRIMEZ CETTE LIGNE
public class ApplicationRequestFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final ApplicationUserDetailsService userDetailsService;

    // Constructeur pour l'instanciation manuelle
    public ApplicationRequestFilter(JwtUtil jwtUtil, ApplicationUserDetailsService userDetailsService) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }


    //Ayant supprimé @Component, on a plus besoin d'injection automatique car c'est fait par constructeur ci-dessus
//    @Autowired
//    private JwtUtil jwtUtil;
//
//    @Autowired
//    private ApplicationUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        final String authorizationHeader = request.getHeader("Authorization");
        String username = null;
        String jwt = null;

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            jwt = authorizationHeader.substring(7);
            try {
                username = jwtUtil.extractUsername(jwt);
            } catch (Exception e) {
                // token invalide -> on continue sans auth
                logger.debug("Impossible d'extraire le username du token: " + e.getMessage());
            }
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                // charge l'utilisateur (depuis la BDD)
                var userDetails = userDetailsService.loadUserByUsername(username);

                // validate token contre userDetails
                if (jwtUtil.validateToken(jwt, userDetails)) {

                    // Extraire id entreprise depuis le token
                    Integer idEntreprise = jwtUtil.extractIdEntreprise(jwt);

                    // Reconstruire un ExtendedUser contenant idEntreprise et authorities
                    ExtendedUser extended = null;
                    if (userDetails instanceof ExtendedUser) {
                        // copie si userDetails est déjà un ExtendedUser
                        extended = (ExtendedUser) userDetails;
                        if (idEntreprise != null) extended.setIdEntreprise(idEntreprise);
                    } else {
                        // création d'un ExtendedUser à partir de userDetails
                        extended = new ExtendedUser(userDetails.getUsername(),
                                userDetails.getPassword(),
                                idEntreprise,
                                userDetails.getAuthorities());
                    }

                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(extended, null, extended.getAuthorities());
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            } catch (Exception ex) {
                logger.debug("Erreur pendant l'authentification via JWT: " + ex.getMessage());
                // on ne rethrow pas : laisse la requête continuer (sera rejetée si endpoint protégé)
            }
        }

        filterChain.doFilter(request, response);
    }
}








//package com.tchindaClovis.gestiondestock.config;

//@Component
//public class ApplicationRequestFilter extends OncePerRequestFilter { //filtre pour intercepter les requêtes
//
//    private JwtUtil jwtUtil;
//
//    private ApplicationUserDetailsService userDetailsService;
//    @Autowired
//    public ApplicationRequestFilter(JwtUtil jwtUtil, ApplicationUserDetailsService userDetailsService) {
//        this.jwtUtil = jwtUtil;
//        this.userDetailsService = userDetailsService;
//    }
//
//    @Override
//    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
//            throws ServletException, IOException {
//        final String authHeader = request.getHeader("Authorization"); // obtenir le header avec la valeur "Authorization
//        String userEmail = null;
//        String jwt = null;
//        String idEntreprise = null;
//
//        if (StringUtils.hasLength(authHeader) && authHeader.startsWith("Bearer ")){
//            jwt = authHeader.substring(7); //extraire le header
//            userEmail = jwtUtil.extractUsername(jwt); //extraire le username à partir de jwt
//            idEntreprise = jwtUtil.extractIdEntreprise(jwt);
//        }
//
//        if (StringUtils.hasLength(userEmail) && SecurityContextHolder.getContext().getAuthentication() == null){
//            UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail); //je recupère l'utilisateur en utilisant le userDetailsService
//            if (jwtUtil.validateToken(jwt, userDetails)){ //vérifi si le token est valide pour cette utilisateur
//                UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(
//                        userDetails, null, userDetails.getAuthorities() //chager mon utilisateur en créant un objet de type
//                );
//                usernamePasswordAuthenticationToken.setDetails(
//                        new WebAuthenticationDetailsSource().buildDetails(request) //je donne les détails à traver la requête
//                );
//                SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken); // récupérer le contexte et établir l'authentification
//            }
//        }
//        MDC.put("idEntreprise",idEntreprise); //pour stocker mes objets idEntreprise
//        chain.doFilter(request, response);
//    }
//}








