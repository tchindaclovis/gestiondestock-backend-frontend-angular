package com.tchindaClovis.gestiondestock.controller.api;

import static com.tchindaClovis.gestiondestock.utils.Constants.AUTHENTICATION_ENDPOINT;
import com.tchindaClovis.gestiondestock.dto.auth.AuthenticationRequest;
import com.tchindaClovis.gestiondestock.dto.auth.AuthenticationResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "Authentications", description = "API de gestion des authentication")
public interface AuthenticationApi {

    @PostMapping(AUTHENTICATION_ENDPOINT + "/authenticate")
    ResponseEntity<AuthenticationResponse> authenticate(@RequestBody AuthenticationRequest request);

}
