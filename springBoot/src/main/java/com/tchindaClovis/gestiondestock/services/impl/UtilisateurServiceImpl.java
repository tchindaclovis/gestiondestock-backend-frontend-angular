package com.tchindaClovis.gestiondestock.services.impl;

import com.tchindaClovis.gestiondestock.dto.ChangerMotDePasseUtilisateurDto;
import com.tchindaClovis.gestiondestock.dto.ClientDto;
import com.tchindaClovis.gestiondestock.dto.UtilisateurDto;
import com.tchindaClovis.gestiondestock.exception.EntityNotFoundException;
import com.tchindaClovis.gestiondestock.exception.ErrorCodes;
import com.tchindaClovis.gestiondestock.exception.InvalidEntityException;
import com.tchindaClovis.gestiondestock.exception.InvalidOperationException;
import com.tchindaClovis.gestiondestock.model.Utilisateur;
import com.tchindaClovis.gestiondestock.repository.UtilisateurRepository;
import com.tchindaClovis.gestiondestock.services.UtilisateurService;
import com.tchindaClovis.gestiondestock.validator.UtilisateurValidator;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@Slf4j
public class UtilisateurServiceImpl implements UtilisateurService {

    private UtilisateurRepository utilisateurRepository;
    private PasswordEncoder passwordEncoder;

    @Autowired
    public UtilisateurServiceImpl(UtilisateurRepository utilisateurRepository,
                                  PasswordEncoder passwordEncoder) {
        this.utilisateurRepository = utilisateurRepository;
        this.passwordEncoder = passwordEncoder;
    }


    @Override
    public UtilisateurDto save(UtilisateurDto dto) {

        // Validation des champs
        List<String> errors = UtilisateurValidator.validate(dto);
        if (!errors.isEmpty()) {
            log.error("Utilisateur is not valid {}", dto);
            throw new InvalidEntityException(
                    "L'utilisateur n'est pas valide",
                    ErrorCodes.UTILISATEUR_NOT_VALID,
                    errors
            );
        }

        // Vérification de l'unicité de l'email
        Optional<Utilisateur> utilisateur = utilisateurRepository.findUtilisateurByEmail(dto.getEmail());

        if (utilisateur.isPresent() && !utilisateur.get().getId().equals(dto.getId())) {
            throw new InvalidEntityException(
                    "Un autre utilisateur avec le meme email existe deja",
                    ErrorCodes.UTILISATEUR_ALREADY_EXISTS,
                    Collections.singletonList("Un autre utilisateur avec le meme email existe deja dans la BDD")
            );
        }

        // Encodage du mot de passe seulement si il existe
        if (dto.getMotDePasse() != null && !dto.getMotDePasse().isEmpty()
                && !dto.getMotDePasse().startsWith("$2a$")) {  //Encoder uniquement si le mot de passe n’est pas déjà encodé.

            dto.setMotDePasse(passwordEncoder.encode(dto.getMotDePasse()));
        }

//        if (dto.getMotDePasse() != null && !dto.getMotDePasse().isEmpty()) {
//            dto.setMotDePasse(passwordEncoder.encode(dto.getMotDePasse())); //double encodage et rejet du mot de passe
//        }

        // Sauvegarde
        Utilisateur savedUser = utilisateurRepository.save(UtilisateurDto.toEntity(dto));

        return UtilisateurDto.fromEntity(savedUser);
    }


    private boolean userAlreadyExists(String email) {
        Optional<Utilisateur> user = utilisateurRepository.findUtilisateurByEmail(email);
        return user.isPresent();
    }

    @Override
    public UtilisateurDto findById(Integer id) {
        if (id == null) {
            log.error("Utilisateur ID is null");
            return null;
        }
        return utilisateurRepository.findById(id)
                .map(UtilisateurDto::fromEntity)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Aucun utilisateur avec l'ID = " + id + " n'a ete trouve dans la BDD",
                        ErrorCodes.UTILISATEUR_NOT_FOUND)
                );
    }

    @Override
    public List<UtilisateurDto> findAll() {
        return utilisateurRepository.findAll().stream()
                .map(UtilisateurDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<UtilisateurDto> findAllUtilisateurByIdEntreprise(Integer idEntreprise) {
        if (idEntreprise == null) {
            log.error("Entreprise ID is null");
            return List.of();
        }
        return utilisateurRepository.findAllByEntrepriseId(idEntreprise).stream()
                .map(UtilisateurDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public void delete(Integer id) {
        if (id == null) {
            log.error("Utilisateur ID is null");
            return;
        }
        utilisateurRepository.deleteById(id);
    }


    @Override
    public UtilisateurDto findByNom(String nom) {
        return utilisateurRepository.findByNom(nom)
                .map(UtilisateurDto::fromEntity)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Aucun utilisateur avec l'email = " + nom + " n'a été trouvé dans la BDD",
                        ErrorCodes.UTILISATEUR_NOT_FOUND)
                );
    }

    @Override
    public UtilisateurDto findByStatut(String statut) {
        return utilisateurRepository.findByStatut(statut)
                .map(UtilisateurDto::fromEntity)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Aucun utilisateur avec le statut = " + statut + " n'a été trouvé dans la BDD",
                        ErrorCodes.UTILISATEUR_NOT_FOUND)
                );
    }

    @Override
    public UtilisateurDto findByEmail(String email) {
        return utilisateurRepository.findUtilisateurByEmail(email)
                .map(UtilisateurDto::fromEntity)
                .orElseThrow(() -> new EntityNotFoundException(
                   "Aucun utilisateur avec l'email = " + email + " n'a été trouvé dans la BDD",
                   ErrorCodes.UTILISATEUR_NOT_FOUND)
                );
    }

    @Override
    public UtilisateurDto changerMotDePasse(ChangerMotDePasseUtilisateurDto dto) {
        validate(dto);
        Optional<Utilisateur> utilisateurOptional = utilisateurRepository.findById(dto.getId());
        if (utilisateurOptional.isEmpty()) {
            log.warn("Aucun utilisateur n'a ete trouve avec l'ID " + dto.getId());
            throw new EntityNotFoundException("Aucun utilisateur n'a ete trouve avec l'ID " + dto.getId(),
                    ErrorCodes.UTILISATEUR_NOT_FOUND);
        }

        Utilisateur utilisateur = utilisateurOptional.get();
        utilisateur.setMotDePasse(passwordEncoder.encode(dto.getMotDePasse()));

        return UtilisateurDto.fromEntity(
                utilisateurRepository.save(utilisateur)
        );
    }

    private void validate(ChangerMotDePasseUtilisateurDto dto) {
        if (dto == null) {
            log.warn("Impossible de modifier le mot de passe avec un objet NULL");
            throw new InvalidOperationException("Aucune information n'a ete fourni pour pouvoir changer le mot de passe",
                    ErrorCodes.UTILISATEUR_CHANGE_PASSWORD_OBJECT_NOT_VALID);
        }
        if (dto.getId() == null) {
            log.warn("Impossible de modifier le mot de passe avec un ID NULL");
            throw new InvalidOperationException("ID utilisateur null:: Impossible de modifier le mote de passe",
                    ErrorCodes.UTILISATEUR_CHANGE_PASSWORD_OBJECT_NOT_VALID);
        }
        if (!StringUtils.hasLength(dto.getMotDePasse()) || !StringUtils.hasLength(dto.getConfirmMotDePasse())) {
            log.warn("Impossible de modifier le mot de passe avec un mot de passe NULL");
            throw new InvalidOperationException("Mot de passe utilisateur null:: Impossible de modifier le mote de passe",
                    ErrorCodes.UTILISATEUR_CHANGE_PASSWORD_OBJECT_NOT_VALID);
        }
        if (!dto.getMotDePasse().equals(dto.getConfirmMotDePasse())) {
            log.warn("Impossible de modifier le mot de passe avec deux mots de passe different");
            throw new InvalidOperationException("Mots de passe utilisateur non conformes:: Impossible de modifier le mote de passe",
                    ErrorCodes.UTILISATEUR_CHANGE_PASSWORD_OBJECT_NOT_VALID);
        }
    }
}









//package com.tchindaClovis.gestiondestock.services.impl;
//
//import com.tchindaClovis.gestiondestock.dto.UtilisateurDto;
//import com.tchindaClovis.gestiondestock.exception.EntityNotFoundException;
//import com.tchindaClovis.gestiondestock.exception.ErrorCodes;
//import com.tchindaClovis.gestiondestock.exception.InvalidEntityException;
//import com.tchindaClovis.gestiondestock.model.Utilisateur;
//import com.tchindaClovis.gestiondestock.repository.UtilisateurRepository;
//import com.tchindaClovis.gestiondestock.services.UtilisateurService;
//import com.tchindaClovis.gestiondestock.validator.UtilisateurValidator;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.stereotype.Service;
//import org.springframework.util.StringUtils;
//import java.util.List;
//import java.util.Optional;
//import java.util.stream.Collectors;
//
//@Service
//@Slf4j
//public class UtilisateurServiceImpl implements UtilisateurService {
//    private UtilisateurRepository utilisateurRepository;
//
//    @Autowired
//    public UtilisateurServiceImpl(UtilisateurRepository utilisateurRepository) {
//        this.utilisateurRepository = utilisateurRepository;
//    }
//
//    @Autowired
//    private PasswordEncoder passwordEncoder;
//
//    @Override
//    public UtilisateurDto save(UtilisateurDto dto) {
//        List<String> errors = UtilisateurValidator.validate(dto);
//        if(!errors.isEmpty()){
//            log.error("Utilisateur is not valid{}", dto);
//            throw new InvalidEntityException("Le utilisateur n'est pas valide", ErrorCodes.UTILISATEUR_NOT_VALID, errors);
//        }
//        Utilisateur savedUtilisateur = utilisateurRepository.save(UtilisateurDto.toEntity(dto));
//        return UtilisateurDto.fromEntity(savedUtilisateur);
//    }
//
//    @Override
//    public UtilisateurDto findById(Integer id) {
//        if(id == null){
//            log.error("Utilisateur ID is null");
//            return null;
//        }
//        Optional<Utilisateur> utilisateur = utilisateurRepository.findById(id);
//
//        return Optional.of(UtilisateurDto.fromEntity(utilisateur.get())).orElseThrow(() ->
//                new EntityNotFoundException(
//                        "Aucun fournisseur avec l'ID = " + id + "n'a ete trouve dans la BDD",
//                        ErrorCodes.UTILISATEUR_NOT_FOUND)
//        );
//    }
//
//    @Override
//    public UtilisateurDto findByNom(String nom) {
//        if(!StringUtils.hasLength(nom)){
//            log.error("Utilisateur NOM is null");
//            return null;
//        }
//        Optional<Utilisateur> utilisateur = utilisateurRepository.findByNom(nom);
//        return Optional.of(UtilisateurDto.fromEntity(utilisateur.get())).orElseThrow(() ->
//                new EntityNotFoundException(
//                        "Aucun utilisateur avec le NOM = " + nom + "n'a ete trouve dans la BDD",
//                        ErrorCodes.UTILISATEUR_NOT_FOUND)
//        );
//    }
//
//    @Override
//    public UtilisateurDto findByPrenom(String prenom) {
//        if(!StringUtils.hasLength(prenom)){
//            log.error("Utilisateur PRENOM is null");
//            return null;
//        }
//        Optional<Utilisateur> utilisateur = utilisateurRepository.findByPrenom(prenom);
//        return Optional.of(UtilisateurDto.fromEntity(utilisateur.get())).orElseThrow(() ->
//                new EntityNotFoundException(
//                        "Aucun utilisateur avec le PRENOM = " + prenom + "n'a ete trouve dans la BDD",
//                        ErrorCodes.UTILISATEUR_NOT_FOUND)
//        );
//    }
//
//    @Override
//    public List<UtilisateurDto> findAll() {
//        return utilisateurRepository.findAll().stream()
//                .map(UtilisateurDto::fromEntity)
//                .collect(Collectors.toList());
//    }
//
//    @Override
//    public void delete(Integer id) {
//        if(id == null){
//            log.error("Utilisateur ID is null");
//            return;
//        }
//        utilisateurRepository.deleteById(id);
//    }
//
//
//    @Override
//    public UtilisateurDto findByEmail(String email) {
//        return utilisateurRepository.findUtilisateurByEmail(email)
//                .map(UtilisateurDto::fromEntity)
//                .orElseThrow(() -> new EntityNotFoundException(
//                "Aucun utilisateur avec l'email = " + email + " n'est trouvé dans la BDD",
//                ErrorCodes.UTILISATEUR_NOT_FOUND)
//        );
//
//    }
//
//}





