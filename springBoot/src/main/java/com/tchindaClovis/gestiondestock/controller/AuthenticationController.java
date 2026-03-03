package com.tchindaClovis.gestiondestock.controller;

import com.tchindaClovis.gestiondestock.controller.api.AuthenticationApi;
import com.tchindaClovis.gestiondestock.dto.auth.AuthenticationRequest;
import com.tchindaClovis.gestiondestock.dto.auth.AuthenticationResponse;
import com.tchindaClovis.gestiondestock.services.auth.ApplicationUserDetailsService;
import com.tchindaClovis.gestiondestock.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
//@RequestMapping(AUTHENTICATION_ENDPOINT)
public class AuthenticationController implements AuthenticationApi {
    private AuthenticationManager authenticationManager;
    private ApplicationUserDetailsService userDetailsService;
    private JwtUtil jwtUtil;

    // Injection par constructeur (recommandé)
    @Autowired
    public AuthenticationController(
            AuthenticationManager authenticationManager,
            ApplicationUserDetailsService userDetailsService,
            JwtUtil jwtUtil
    ) {
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.jwtUtil = jwtUtil;
    }

    @Override
    public ResponseEntity<AuthenticationResponse> authenticate(@RequestBody AuthenticationRequest request) {
        try {
            // Authentifier l'utilisateur pour voir s'il existe dans la BDD
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getLogin(),
                            request.getPassword()
                    )
            );

            // Charger les détails de l'utilisateur
            final UserDetails userDetails = userDetailsService.loadUserByUsername(request.getLogin());

            // Générer le token JWT
            final String jwtToken = jwtUtil.generateToken(userDetails);

            // Retourner la réponse
            return ResponseEntity.ok(AuthenticationResponse.builder().accessToken(jwtToken).build());

        } catch (BadCredentialsException e) {
        return ResponseEntity
                .status(401)
                .body(AuthenticationResponse
                        .builder()
                        .error("Identifiants invalides")
                        .build()
                );

        } catch (Exception e) {
        return ResponseEntity
                .status(500)
                .body(AuthenticationResponse
                        .builder()
                        .error("Erreur interne du serveur")
                        .build()
                );
        }
    }
}




//package com.tchindaClovis.gestiondestock.controller;
//
//import com.tchindaClovis.gestiondestock.controller.api.AuthenticationApi;
//import com.tchindaClovis.gestiondestock.dto.auth.AuthenticationRequest;
//import com.tchindaClovis.gestiondestock.dto.auth.AuthenticationResponse;
//import com.tchindaClovis.gestiondestock.services.auth.ApplicationUserDetailsService;
//import com.tchindaClovis.gestiondestock.utils.JwtUtil;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.authentication.AuthenticationManager;
//import org.springframework.security.authentication.BadCredentialsException;
//import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.web.bind.annotation.RequestBody;
//import org.springframework.web.bind.annotation.RestController;
//
//@RestController
//public class AuthenticationController implements AuthenticationApi {
//
//    private AuthenticationManager authenticationManager;
//    private ApplicationUserDetailsService userDetailsService;
//    private JwtUtil jwtUtil;
//
//    @Autowired
//    public AuthenticationController(
//            AuthenticationManager authenticationManager,
//            ApplicationUserDetailsService userDetailsService,
//            JwtUtil jwtUtil
//    ) {
//        this.authenticationManager = authenticationManager;
//        this.userDetailsService = userDetailsService;
//        this.jwtUtil = jwtUtil;
//    }
//
//    @Override
//    public ResponseEntity<AuthenticationResponse> authenticate(@RequestBody AuthenticationRequest request) {
//        try {
//            authenticationManager.authenticate(
//                    new UsernamePasswordAuthenticationToken(
//                            request.getLogin(),
//                            request.getPassword()
//                    )
//            );
//
//            final UserDetails userDetails = userDetailsService.loadUserByUsername(request.getLogin());
//            final String jwtToken = jwtUtil.generateToken(userDetails);
//
//            return ResponseEntity.ok(AuthenticationResponse.builder().accessToken(jwtToken).build());
//
//        } catch (BadCredentialsException ex) {
//            return ResponseEntity.status(401).body(AuthenticationResponse.builder()
//                    .error("Identifiants invalides").build());
//        } catch (Exception e) {
//            return ResponseEntity.status(500).body(AuthenticationResponse.builder()
//                    .error("Erreur interne").build());
//        }
//    }
//}






//        } catch (BadCredentialsException e) {
//        return ResponseEntity.status(401).body(
//                AuthenticationResponse.builder()
//                        .error("Identifiants invalides")
//                        .build()
//        );
//    } catch (Exception e) {
//        return ResponseEntity.status(500).body(
//                AuthenticationResponse.builder()
//                        .error("Erreur interne du serveur")
//                        .build()
//        );
//        }




//package com.tchindaClovis.gestiondestock.controller;
//
//import com.tchindaClovis.gestiondestock.services.auth.ApplicationUserDetailsService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.authentication.AuthenticationManager;
//import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestBody;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//import static com.tchindaClovis.gestiondestock.utils.Constants.AUTHENTICATION_ENDPOINT;
//
//@RestController
//@RequestMapping(AUTHENTICATION_ENDPOINT)
//public class AuthentificationController {
//
//    @Autowired
//    private AuthenticationManager authenticationManager;
//
//    @Autowired
//    private ApplicationUserDetailsService userDetailsService;
//
//    @PostMapping("/authenticate")
//    public ResponseEntity<AuthenticationResponse> authenticate(@RequestBody AuthenticationRequest request){
//        authenticationManager.authenticate(
//            new UsernamePasswordAuthenticationToken(
//                    request.getLogin(),
//                    request.getPassword()
//            )
//        );
//
//        final UserDetails userDetails = userDetailsService.loadUserByUsername(request.getLogin());
//        return ResponseEntity.ok(AuthenticationResponse.builder().accessToken("dummy_access_token").build());
//    }
//}
