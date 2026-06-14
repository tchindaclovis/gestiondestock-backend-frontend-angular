package com.tchindaClovis.gestiondestock.services.impl;

import com.tchindaClovis.gestiondestock.dto.FournisseurDto;
import com.tchindaClovis.gestiondestock.exception.EntityNotFoundException;
import com.tchindaClovis.gestiondestock.exception.ErrorCodes;
import com.tchindaClovis.gestiondestock.exception.InvalidEntityException;
import com.tchindaClovis.gestiondestock.exception.InvalidOperationException;
import com.tchindaClovis.gestiondestock.model.Category;
import com.tchindaClovis.gestiondestock.model.CommandeFournisseur;
import com.tchindaClovis.gestiondestock.model.Fournisseur;
import com.tchindaClovis.gestiondestock.model.Utilisateur;
import com.tchindaClovis.gestiondestock.repository.CommandeFournisseurRepository;
import com.tchindaClovis.gestiondestock.repository.FournisseurRepository;
import com.tchindaClovis.gestiondestock.repository.UtilisateurRepository;
import com.tchindaClovis.gestiondestock.services.FournisseurService;
import com.tchindaClovis.gestiondestock.validator.FournisseurValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class FournisseurServiceImpl implements FournisseurService {

    private FournisseurRepository fournisseurRepository;
    private CommandeFournisseurRepository commandeFournisseurRepository;

    private UtilisateurRepository utilisateurRepository;

    @Autowired
    public FournisseurServiceImpl(FournisseurRepository fournisseurRepository,
                                  CommandeFournisseurRepository commandeFournisseurRepository,
                                  UtilisateurRepository utilisateurRepository) {
        this.fournisseurRepository = fournisseurRepository;
        this.commandeFournisseurRepository = commandeFournisseurRepository;
        this.utilisateurRepository = utilisateurRepository;
    }

    @Override
    public FournisseurDto save(FournisseurDto dto) {

        // 1. Validation de l'objet FournisseurDto
        List<String> errors = FournisseurValidator.validate(dto);
        if(!errors.isEmpty()){
            log.error("Fournisseur is not valid{}", dto);
            throw new InvalidEntityException("Le fournisseur n'est pas valide", ErrorCodes.FOURNISSEUR_NOT_VALID, errors);
        }

        // 2. Récupérer l'utilisateur connecté depuis le contexte de sécurité
        String connectedUserEmail = null;
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (principal instanceof org.springframework.security.core.userdetails.UserDetails) {
            connectedUserEmail = ((org.springframework.security.core.userdetails.UserDetails) principal).getUsername();
        } else {
            connectedUserEmail = principal.toString();
        }

        // Récupération de l'utilisateur complet en BDD
        Utilisateur loggedInUser = utilisateurRepository.findUtilisateurByEmail(connectedUserEmail)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Utilisateur connecté introuvable",
                        ErrorCodes.UTILISATEUR_NOT_FOUND));

        // 3. RÈGLE DE GESTION
        if (dto.getId() != null) {
            // Mode Modification : On récupère le fournisseur existant en BDD avant mise à jour
            Fournisseur existingFournisseur = fournisseurRepository.findById(dto.getId())
                    .orElseThrow(() -> new EntityNotFoundException(
                            "Aucun fournisseur trouvé avec l'ID = " + dto.getId(),
                            ErrorCodes.FOURNISSEUR_NOT_FOUND));

            // On vérifie si l'utilisateur connecté est le créateur originel
            // (Note : Assurez-vous que votre entité Fournisseur possède le champ idUtilisateur ou utilisateur)
            if (existingFournisseur.getIdUtilisateur() != null && !existingFournisseur.getIdUtilisateur().equals(loggedInUser.getId())) {
                log.warn("L'utilisateur {} a tenté de modifier le fournisseur {} sans en être le créateur", loggedInUser.getId(), dto.getId());
                throw new InvalidOperationException(
                        "Vous n'êtes pas autorisé à modifier ce fournisseur car vous n'en êtes pas le créateur originel.",
                        ErrorCodes.UTILISATEUR_CHANGE_FOURNISSEUR_OBJECT_NOT_VALID);
            }
        } else {
            // Mode Création : On injecte l'ID de l'utilisateur connecté comme créateur
            dto.setIdUtilisateur(loggedInUser.getId());

            // Optionnel : Si vos fournisseurs dépendent aussi de l'entreprise de l'utilisateur
            if (loggedInUser.getEntreprise() != null) {
                dto.setIdEntreprise(loggedInUser.getEntreprise().getId());
            }
        }


        Fournisseur savedFournisseur = fournisseurRepository.save(FournisseurDto.toEntity(dto));
        return FournisseurDto.fromEntity(savedFournisseur);
    }

    @Override
    public FournisseurDto findById(Integer id) {
        if(id == null){
            log.error("Fournisseur ID is null");
            return null;
        }
        Optional<Fournisseur> fournisseur = fournisseurRepository.findById(id);

        return Optional.of(FournisseurDto.fromEntity(fournisseur.get())).orElseThrow(() ->
                new EntityNotFoundException(
                        "Aucun fournisseur avec l'ID = " + id + "n'a ete trouve dans la BDD",
                        ErrorCodes.FOURNISSEUR_NOT_FOUND)
        );
    }

    @Override
    public FournisseurDto findByNom(String nom) {
        if(!StringUtils.hasLength(nom)){
            log.error("Fournisseur NOM is null");
            return null;
        }
        Optional<Fournisseur> fournisseur = fournisseurRepository.findByNom(nom);
        return Optional.of(FournisseurDto.fromEntity(fournisseur.get())).orElseThrow(() ->
                new EntityNotFoundException(
                        "Aucun fournisseur avec le NOM = " + nom + "n'a ete trouve dans la BDD",
                        ErrorCodes.FOURNISSEUR_NOT_FOUND)
        );
    }

    @Override
    public FournisseurDto findByStatut(String statut) {
        if(!StringUtils.hasLength(statut)){
            log.error("Fournisseur STATUT is null");
            return null;
        }
        Optional<Fournisseur> fournisseur = fournisseurRepository.findByStatut(statut);
        return Optional.of(FournisseurDto.fromEntity(fournisseur.get())).orElseThrow(() ->
                new EntityNotFoundException(
                        "Aucun fournisseur avec le STATUT = " + statut + "n'a ete trouve dans la BDD",
                        ErrorCodes.FOURNISSEUR_NOT_FOUND)
        );
    }


    @Override
    public List<FournisseurDto> findAll() {
        return fournisseurRepository.findAll().stream()
                .map(FournisseurDto::fromEntity)
                .collect(Collectors.toList());
    }


    @Override
    public List<FournisseurDto> findAllFournisseurByIdEntreprise(Integer idEntreprise) {
        if (idEntreprise == null) {
            log.error("Entreprise ID is null");
            return List.of();
        }
        return fournisseurRepository.findAllByIdEntreprise(idEntreprise).stream()
                .map(FournisseurDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public void delete(Integer id) {
        if (id == null) {
            log.error("Fournisseur ID is null");
            return;
        }
        List<CommandeFournisseur> commandeFournisseur = commandeFournisseurRepository.findAllByFournisseurId(id);
        if (!commandeFournisseur.isEmpty()) {
            throw new InvalidOperationException("Impossible de supprimer un fournisseur qui a deja des commandes",
                    ErrorCodes.FOURNISSEUR_ALREADY_IN_USE);
        }
        fournisseurRepository.deleteById(id);
    }
}
