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

@Component
public class ApplicationRequestFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private ApplicationUserDetailsService userDetailsService;

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
//
//import com.tchindaClovis.gestiondestock.services.auth.ApplicationUserDetailsService;
//import com.tchindaClovis.gestiondestock.utils.JwtUtil;
//import jakarta.servlet.FilterChain;
//import jakarta.servlet.ServletException;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import org.jboss.logging.MDC;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
//import org.springframework.stereotype.Component;
//import org.springframework.util.StringUtils;
//import org.springframework.web.filter.OncePerRequestFilter;
//import java.io.IOException;
//
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





//package com.tchindaClovis.gestiondestock.config;
//
//import com.tchindaClovis.gestiondestock.services.auth.ApplicationUserDetailsService;
//import com.tchindaClovis.gestiondestock.utils.JwtUtil;
//import jakarta.servlet.FilterChain;
//import jakarta.servlet.ServletException;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import org.jboss.logging.MDC;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
//import org.springframework.stereotype.Component;
//import org.springframework.util.StringUtils;
//import org.springframework.web.filter.OncePerRequestFilter;
//import java.io.IOException;
//
//@Component
//public class ApplicationRequestFilter extends OncePerRequestFilter {
//
//    private JwtUtil jwtUtil;
//    private ApplicationUserDetailsService userDetailsService;
//
//    // Injection par constructeur (déjà présent)
//    @Autowired
//    public ApplicationRequestFilter(JwtUtil jwtUtil, ApplicationUserDetailsService userDetailsService) {
//        this.jwtUtil = jwtUtil;
//        this.userDetailsService = userDetailsService;
//    }
//
//    @Override
//    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
//            throws ServletException, IOException {
//
//        // =========================================================================
//        // NOUVEAU: Contourner la vérification JWT pour les chemins publics
//        // =========================================================================
//        // Ces chemins doivent correspondre EXACTEMENT à ceux dans SecurityConfiguration
//        // On vérifie le chemin relatif (URI)
//        String servletPath = request.getServletPath();
//
//        if (servletPath.startsWith("/gestiondestock/v1/auth/authenticate") ||
//                servletPath.startsWith("/gestiondestock/v1/entreprises/create") ||
//                servletPath.startsWith("/gestiondestock/v1/utilisateurs/update/password")) {
//
//            // Si c'est une URL publique, on passe directement au filtre suivant sans vérifier le JWT
//            MDC.put("idEntreprise", null); // Assurez-vous que l'ID d'entreprise est vide pour les requêtes publiques
//            chain.doFilter(request, response);
//            return;
//        }
//        // =========================================================================
//
//        final String authHeader = request.getHeader("Authorization");
//        String userEmail = null;
//        String jwt = null;
//        String idEntreprise = null;
//
//        if (StringUtils.hasLength(authHeader) && authHeader.startsWith("Bearer ")){
//            jwt = authHeader.substring(7);
//            // Si le JWT est mal formé, MalformedJwtException sera levée ici.
//            // En excluant les paths publics ci-dessus, cela devrait être évité sur ces paths.
//            userEmail = jwtUtil.extractUsername(jwt);
//            idEntreprise = jwtUtil.extractIdEntreprise(jwt);
//        }
//
//        if (StringUtils.hasLength(userEmail) && SecurityContextHolder.getContext().getAuthentication() == null){
//            UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);
//            if (jwtUtil.validateToken(jwt, userDetails)){
//                UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(
//                        userDetails, null, userDetails.getAuthorities()
//                );
//                usernamePasswordAuthenticationToken.setDetails(
//                        new WebAuthenticationDetailsSource().buildDetails(request)
//                );
//                SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
//            }
//        }
//        MDC.put("idEntreprise",idEntreprise);
//        chain.doFilter(request, response);
//    }
//}






