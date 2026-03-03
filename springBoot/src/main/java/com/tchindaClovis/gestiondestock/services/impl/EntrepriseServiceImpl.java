package com.tchindaClovis.gestiondestock.services.impl;

import com.tchindaClovis.gestiondestock.dto.EntrepriseDto;
import com.tchindaClovis.gestiondestock.dto.RolesDto;
import com.tchindaClovis.gestiondestock.dto.UtilisateurDto;
import com.tchindaClovis.gestiondestock.exception.EntityNotFoundException;
import com.tchindaClovis.gestiondestock.exception.ErrorCodes;
import com.tchindaClovis.gestiondestock.exception.InvalidEntityException;
import com.tchindaClovis.gestiondestock.model.Entreprise;
import com.tchindaClovis.gestiondestock.repository.EntrepriseRepository;
import com.tchindaClovis.gestiondestock.repository.RolesRepository;
import com.tchindaClovis.gestiondestock.services.EntrepriseService;
import com.tchindaClovis.gestiondestock.services.UtilisateurService;
import com.tchindaClovis.gestiondestock.validator.EntrepriseValidator;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;


@Transactional(rollbackOn = Exception.class)
@Service
@Slf4j
public class EntrepriseServiceImpl implements EntrepriseService {

    private EntrepriseRepository entrepriseRepository;
    private UtilisateurService utilisateurService;
    private RolesRepository rolesRepository;

    @Autowired
    public EntrepriseServiceImpl(EntrepriseRepository entrepriseRepository, UtilisateurService utilisateurService,
                                 RolesRepository rolesRepository) {
        this.entrepriseRepository = entrepriseRepository;
        this.utilisateurService = utilisateurService;
        this.rolesRepository = rolesRepository;
    }

    @Override
    public EntrepriseDto save(EntrepriseDto dto) {
        List<String> errors = EntrepriseValidator.validate(dto);
        if (!errors.isEmpty()) {
            log.error("Entreprise is not valid {}", dto);
            throw new InvalidEntityException("L'entreprise n'est pas valide", ErrorCodes.ENTREPRISE_NOT_VALID, errors);
        }
        EntrepriseDto savedEntreprise = EntrepriseDto.fromEntity(
                entrepriseRepository.save(EntrepriseDto.toEntity(dto))
        );

        UtilisateurDto utilisateur = fromEntreprise(savedEntreprise);

        UtilisateurDto savedUser = utilisateurService.save(utilisateur);

        RolesDto rolesDto = RolesDto.builder()
                .roleName("ADMIN")
                .utilisateur(savedUser)
                .build();

        rolesRepository.save(RolesDto.toEntity(rolesDto));

        return  savedEntreprise;
    }

    private UtilisateurDto fromEntreprise(EntrepriseDto dto) {
        return UtilisateurDto.builder()
                .adresse(dto.getAdresse())
                .nom(dto.getNom().toUpperCase())
                .prenom(dto.getNom().toLowerCase())
                .email(dto.getEmail())
                .numTel(dto.getNumTel())
                .motDePasse(generateRandomPassword())
                .entreprise(dto)
                .dateDeNaissance(Instant.now())
                .photo(dto.getPhoto())
                .build();
    }

    private String generateRandomPassword() {
        return "som3R@nd0mP@$$word";
    }

    @Override
    public EntrepriseDto findById(Integer id) {
        if (id == null) {
            log.error("Entreprise ID is null");
            return null;
        }
        return entrepriseRepository.findById(id)
                .map(EntrepriseDto::fromEntity)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Aucune entreprise avec l'ID = " + id + " n' ete trouve dans la BDD",
                        ErrorCodes.ENTREPRISE_NOT_FOUND)
                );
    }

        @Override
    public EntrepriseDto findByNom(String nom) {
        if(!StringUtils.hasLength(nom)){
            log.error("Entreprise NOM is null");
            return null;
        }
        Optional<Entreprise> entreprise = entrepriseRepository.findByNom(nom);
        return Optional.of(EntrepriseDto.fromEntity(entreprise.get())).orElseThrow(() ->
                new EntityNotFoundException(
                        "Aucune entreprise avec le NOM = " + nom + "n'a ete trouve dans la BDD",
                        ErrorCodes.ENTREPRISE_NOT_FOUND)
        );
    }

    @Override
    public EntrepriseDto findByCodeFiscal(String codeFiscal) {
        if(!StringUtils.hasLength(codeFiscal)){
            log.error("Entreprise CODEFISCAL is null");
            return null;
        }
        Optional<Entreprise> entreprise = entrepriseRepository.findEntrepriseByCodeFiscal(codeFiscal);
        return Optional.of(EntrepriseDto.fromEntity(entreprise.get())).orElseThrow(() ->
                new EntityNotFoundException(
                        "Aucune entreprise avec le CODEFISCAL = " + codeFiscal + "n'a ete trouve dans la BDD",
                        ErrorCodes.ENTREPRISE_NOT_FOUND)
        );
    }

    @Override
    public List<EntrepriseDto> findAll() {
        return entrepriseRepository.findAll().stream()
                .map(EntrepriseDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public void delete(Integer id) {
        if (id == null) {
            log.error("Entreprise ID is null");
            return;
        }
        entrepriseRepository.deleteById(id);
    }
}







//package com.tchindaClovis.gestiondestock.services.impl;
//
//import com.tchindaClovis.gestiondestock.dto.EntrepriseDto;
//import com.tchindaClovis.gestiondestock.dto.RolesDto;
//import com.tchindaClovis.gestiondestock.dto.UtilisateurDto;
//import com.tchindaClovis.gestiondestock.exception.EntityNotFoundException;
//import com.tchindaClovis.gestiondestock.exception.ErrorCodes;
//import com.tchindaClovis.gestiondestock.exception.InvalidEntityException;
//import com.tchindaClovis.gestiondestock.model.Entreprise;
//import com.tchindaClovis.gestiondestock.model.Utilisateur;
//import com.tchindaClovis.gestiondestock.repository.EntrepriseRepository;
//import com.tchindaClovis.gestiondestock.repository.RolesRepository;
//import com.tchindaClovis.gestiondestock.repository.UtilisateurRepository;
//import com.tchindaClovis.gestiondestock.services.EntrepriseService;
//import com.tchindaClovis.gestiondestock.services.UtilisateurService;
//import com.tchindaClovis.gestiondestock.validator.EntrepriseValidator;
//import jakarta.transaction.Transactional;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.stereotype.Service;
//import org.springframework.util.StringUtils;
//import java.time.Instant;
//import java.util.List;
//import java.util.Optional;
//import java.util.stream.Collectors;
//@Transactional(rollbackOn = Exception.class)
//@Service
//@Slf4j
//public class EntrepriseServiceImpl implements EntrepriseService {
//
//    private EntrepriseRepository entrepriseRepository;
//    private UtilisateurService utilisateurService;
//    private RolesRepository rolesRepository;
//
//    @Autowired
//    public EntrepriseServiceImpl(EntrepriseRepository entrepriseRepository, UtilisateurService utilisateurService,
//                                 RolesRepository rolesRepository) {
//        this.entrepriseRepository = entrepriseRepository;
//        this.utilisateurService = utilisateurService;
//        this.rolesRepository = rolesRepository;
//    }
//
//    @Override
//    public EntrepriseDto save(EntrepriseDto dto) {
//        List<String> errors = EntrepriseValidator.validate(dto);
//        if(!errors.isEmpty()){
//            log.error("Entreprise is not valid{}", dto);
//            throw new InvalidEntityException("L'entreprise n'est pas valide", ErrorCodes.ENTREPRISE_NOT_VALID, errors);
//        }
////        Entreprise savedEntreprise = entrepriseRepository.save(EntrepriseDto.toEntity(dto));
////        return EntrepriseDto.fromEntity(savedEntreprise);
//
//        EntrepriseDto savedEntreprise = EntrepriseDto.fromEntity(   //on garde l'objet entreprise
//                entrepriseRepository.save(EntrepriseDto.toEntity(dto))
//        );
//
//        UtilisateurDto utilisateur = fromEntreprise(savedEntreprise); //créer un utilisateur à partir de l'entreprise
//
//        UtilisateurDto savedUser = utilisateurService.save(utilisateur);
//
//        RolesDto rolesDto = RolesDto.builder()
//                .roleName("ADMIN")
//                .utilisateur(savedUser)
//                .build();
//
//        rolesRepository.save(RolesDto.toEntity(rolesDto));
//
//        return  savedEntreprise;
//    }
//
//            //Mapping de l'objet Entreprise vers l'objet utilisateur
//            //car une entreprise c'est un utilisateur derrière
//    private UtilisateurDto fromEntreprise(EntrepriseDto dto) {
//        return UtilisateurDto.builder()
//                .adresse(dto.getAdresse())
//                .nom(dto.getNom())
//                .prenom(dto.getCodeFiscal())
//                .email(dto.getEmail())
//                .motDePasse(generateRandomPassword())
//                .entreprise(dto)
//                .dateDeNaissance(Instant.now())
//                .photo(dto.getPhoto())
//                .build();
//    }
//
//    private String generateRandomPassword() {
//        return "som3R@nd0mP@$$word";
//    }
//
//
//
//    @Autowired
//    private PasswordEncoder passwordEncoder;
//
//    @Autowired
//    private UtilisateurRepository utilisateurRepository;
//
//    public void updatePassword(String email, String newPassword) {
//        Utilisateur utilisateur = utilisateurRepository.findByEmail(email);
//        if (utilisateur != null) {
//            utilisateur.setMotDePasse(passwordEncoder.encode(newPassword));
//            utilisateurRepository.save(utilisateur);
//        }
//    }
//
//
//
//
//    @Override
//    public EntrepriseDto findById(Integer id) {
//        if(id == null){
//            log.error("Entreprise ID is null");
//            return null;
//        }
////        Optional<Entreprise> entreprise = entrepriseRepository.findById(id);
////
////        return Optional.of(EntrepriseDto.fromEntity(entreprise.get())).orElseThrow(() ->
////                new EntityNotFoundException(
////                        "Aucune entreprise avec l'ID = " + id + "n'a ete trouve dans la BDD",
////                        ErrorCodes.ENTREPRISE_NOT_FOUND));
//
//        return entrepriseRepository.findById(id)
//                .map(EntrepriseDto::fromEntity)
//                .orElseThrow(() -> new EntityNotFoundException(
//                        "Aucune entreprise avec l'ID = " + id + " n' ete trouve dans la BDD",
//                        ErrorCodes.ENTREPRISE_NOT_FOUND)
//                );
//    }
//
//    @Override
//    public EntrepriseDto findByNom(String nom) {
//        if(!StringUtils.hasLength(nom)){
//            log.error("Entreprise NOM is null");
//            return null;
//        }
//        Optional<Entreprise> entreprise = entrepriseRepository.findByNom(nom);
//        return Optional.of(EntrepriseDto.fromEntity(entreprise.get())).orElseThrow(() ->
//                new EntityNotFoundException(
//                        "Aucune entreprise avec le NOM = " + nom + "n'a ete trouve dans la BDD",
//                        ErrorCodes.ENTREPRISE_NOT_FOUND)
//        );
//    }
//
//    @Override
//    public EntrepriseDto findByCodeFiscal(String codeFiscal) {
//        if(!StringUtils.hasLength(codeFiscal)){
//            log.error("Entreprise CODEFISCAL is null");
//            return null;
//        }
//        Optional<Entreprise> entreprise = entrepriseRepository.findEntrepriseByCodeFiscal(codeFiscal);
//        return Optional.of(EntrepriseDto.fromEntity(entreprise.get())).orElseThrow(() ->
//                new EntityNotFoundException(
//                        "Aucune entreprise avec le CODEFISCAL = " + codeFiscal + "n'a ete trouve dans la BDD",
//                        ErrorCodes.ENTREPRISE_NOT_FOUND)
//        );
//    }
//
//    @Override
//    public List<EntrepriseDto> findAll() {
//        return entrepriseRepository.findAll().stream()
//                .map(EntrepriseDto::fromEntity)
//                .collect(Collectors.toList());
//    }
//
//    @Override
//    public void delete(Integer id) {
//        if(id == null){
//            log.error("Entreprise ID is null");
//            return;
//        }
//        entrepriseRepository.deleteById(id);
//    }
//}
