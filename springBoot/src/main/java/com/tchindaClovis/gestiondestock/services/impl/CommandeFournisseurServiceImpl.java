package com.tchindaClovis.gestiondestock.services.impl;

import com.tchindaClovis.gestiondestock.dto.*;
import com.tchindaClovis.gestiondestock.exception.EntityNotFoundException;
import com.tchindaClovis.gestiondestock.exception.ErrorCodes;
import com.tchindaClovis.gestiondestock.exception.InvalidEntityException;
import com.tchindaClovis.gestiondestock.exception.InvalidOperationException;
import com.tchindaClovis.gestiondestock.model.*;
import com.tchindaClovis.gestiondestock.repository.*;
import com.tchindaClovis.gestiondestock.services.CommandeFournisseurService;
import com.tchindaClovis.gestiondestock.services.MvtStockService;
import com.tchindaClovis.gestiondestock.validator.ArticleValidator;
import com.tchindaClovis.gestiondestock.validator.CommandeFournisseurValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class CommandeFournisseurServiceImpl implements CommandeFournisseurService {

    private CommandeFournisseurRepository commandeFournisseurRepository;
    private LigneCommandeFournisseurRepository ligneCommandeFournisseurRepository;
    private FournisseurRepository fournisseurRepository;
    private ArticleRepository articleRepository;

    private MvtStockService mvtStockService;

    @Autowired
    public CommandeFournisseurServiceImpl(CommandeFournisseurRepository commandeFournisseurRepository,
                                          LigneCommandeFournisseurRepository ligneCommandeFournisseurRepository,
                                          FournisseurRepository fournisseurRepository,
                                          ArticleRepository articleRepository,
                                          MvtStockService mvtStockService) {
        this.commandeFournisseurRepository = commandeFournisseurRepository;
        this.ligneCommandeFournisseurRepository = ligneCommandeFournisseurRepository;
        this.fournisseurRepository = fournisseurRepository;
        this.articleRepository = articleRepository;
        this.mvtStockService = mvtStockService;
    }

    @Override
    @Transactional // CRITIQUE: Assure la gestion de la session Hibernate et du rollback en cas d'erreur
    public CommandeFournisseurDto save(CommandeFournisseurDto dto) {

        log.info("Saving command with {} lines",
                dto.getLigneCommandeFournisseurs() != null ? dto.getLigneCommandeFournisseurs().size() : 0);

        if (dto.getLigneCommandeFournisseurs() != null) {
            log.info("Ligne details: {}", dto.getLigneCommandeFournisseurs());
        }


        List<String> errors = CommandeFournisseurValidator.validate(dto);

        if (!errors.isEmpty()) {
            log.error("Commande fournisseur n'est pas valide");
            throw new InvalidEntityException("La commande fournisseur n'est pas valide", ErrorCodes.COMMANDE_FOURNISSEUR_NOT_VALID, errors);
        }

        if (dto.getId() != null && dto.isCommandeLivree()) {
            throw new InvalidOperationException("Impossible de modifier la commande lorsqu'elle est livree", ErrorCodes.COMMANDE_FOURNISSEUR_NON_MODIFIABLE);
        }

        Optional<Fournisseur> fournisseur = fournisseurRepository.findById(dto.getFournisseur().getId());
        if (fournisseur.isEmpty()) {
            log.warn("Fournisseur with ID {} was not found in the DB", dto.getFournisseur().getId());
            throw new EntityNotFoundException("Aucun fournisseur avec l'ID" + dto.getFournisseur().getId() + " n'a ete trouve dans la BDD",
                    ErrorCodes.FOURNISSEUR_NOT_FOUND);
        }

        List<String> articleErrors = new ArrayList<>();

        if (dto.getLigneCommandeFournisseurs() != null) {
            dto.getLigneCommandeFournisseurs().forEach(ligCmdFrs -> {
                if (ligCmdFrs.getArticle() != null) {
                    Optional<Article> article = articleRepository.findById(ligCmdFrs.getArticle().getId());
                    if (article.isEmpty()) {
                        articleErrors.add("L'article avec l'ID " + ligCmdFrs.getArticle().getId() + " n'existe pas");
                    }
                } else {
                    articleErrors.add("Impossible d'enregister une commande avec un aticle NULL");
                }
            });
        }

        if (!articleErrors.isEmpty()) {
            log.warn("");
            throw new InvalidEntityException("Article n'existe pas dans la BDD", ErrorCodes.ARTICLE_NOT_FOUND, articleErrors);
        }
        CommandeFournisseur commandeFournisseurToSave;

        if (dto.getId() != null) {
            // --- CAS MISE À JOUR ---
            // On force le chargement des lignes avec FETCH
            commandeFournisseurToSave = (CommandeFournisseur) commandeFournisseurRepository.findByIdWithLignes(dto.getId())
                    .orElseThrow(() -> new EntityNotFoundException("CommandeFournisseur introuvable", ErrorCodes.COMMANDE_CLIENT_NOT_FOUND));

            // 1. COMPENSATION DES STOCKS (Annulation des anciens stocks)
            if (commandeFournisseurToSave.getLigneCommandeFournisseurs() != null) {
                // Comme on a utilisé JOIN FETCH, cette liste est chargée et accessible
                commandeFournisseurToSave.getLigneCommandeFournisseurs().forEach(this::updateMvtStockAnnulation);
            }

            // 2. MISE À JOUR DES INFOS GÉNÉRALES
            commandeFournisseurToSave.setCode(dto.getCode());
            commandeFournisseurToSave.setDateCommande(dto.getDateCommande());
            commandeFournisseurToSave.setIdEntreprise(dto.getIdEntreprise());

            // 3. NETTOYAGE DES LIGNES
            // On supprime physiquement en BDD
//            ligneVenteRepository.deleteByVenteId(dto.getId());
            // Au lieu de supprimer manuellement avec le repository, on vide la collection
            // orphanRemoval = true dans l'entité s'occupera de la suppression en base
            // On vide la collection Java pour éviter les conflits d'état
            commandeFournisseurToSave.getLigneCommandeFournisseurs().clear();

            // On synchronise pour que Hibernate sache que la commandeFournisseur est "propre"
            commandeFournisseurRepository.saveAndFlush(commandeFournisseurToSave);

        } else {
            // --- CAS CRÉATION ---
            commandeFournisseurToSave = CommandeFournisseurDto.toEntity(dto);
            if (commandeFournisseurToSave.getLigneCommandeFournisseurs() == null) {
                commandeFournisseurToSave.setLigneCommandeFournisseurs(new ArrayList<>());
            }
        }

        // 4. ATTACHEMENT DES NOUVELLES LIGNES
        if (dto.getLigneCommandeFournisseurs() != null) {
            // Initialiser la liste si elle est nulle pour éviter un NPE
            if (commandeFournisseurToSave.getLigneCommandeFournisseurs() == null) {
                commandeFournisseurToSave.setLigneCommandeFournisseurs(new ArrayList<>());
            }

            for (LigneCommandeFournisseurDto ligDto : dto.getLigneCommandeFournisseurs()) {
                LigneCommandeFournisseur lig = LigneCommandeFournisseurDto.toEntity(ligDto);
                lig.setId(null); // <--- FORCE LE NULL : C'est la clé pour éviter l'Optimistic Locking
                lig.setCommandeFournisseur(commandeFournisseurToSave); // Lien vers le parent
                lig.setIdEntreprise(dto.getIdEntreprise()); // Important pour vos filtres

                // On ajoute à la collection du parent pour que le Cascade/Save fonctionne
                commandeFournisseurToSave.getLigneCommandeFournisseurs().add(lig);
            }
        }

        // 5. SAUVEGARDE FINALE ET NOUVEAUX STOCKS
        // On fait un saveAndFlush pour forcer l'écriture des LigneVente en BDD
        CommandeFournisseur savedCommandeFournisseur = commandeFournisseurRepository.saveAndFlush(commandeFournisseurToSave);

        // 6. MISE À JOUR DES STOCKS
        if (savedCommandeFournisseur.getLigneCommandeFournisseurs() != null) {
            for (LigneCommandeFournisseur lig : savedCommandeFournisseur.getLigneCommandeFournisseurs()) {
                // Sécurité : on s'assure que l'article est bien présent avant de mouvementer le stock
                if (lig.getArticle() == null && lig.getArticle().getId() != null) {
                    // Recharger l'article si nécessaire ou s'assurer que le mapper le fait
                    lig.setArticle(articleRepository.findById(lig.getArticle().getId()).orElse(null));
                }

                // On vérifie que la quantité n'est pas nulle
                if (lig.getQuantite() != null && lig.getQuantite().compareTo(BigDecimal.ZERO) != 0) {
                    updateMvtStock(lig);
                } else {
                    log.warn("Ligne de commandeFournisseur ignorée pour le stock : Quantité nulle pour l'article {}",
                            lig.getArticle() != null ? lig.getArticle().getId() : "Inconnu");
                }
            }
        }

        return CommandeFournisseurDto.fromEntity(savedCommandeFournisseur);
    }


    /**
     * Méthode pour réintégrer les stocks lors de l'annulation/modification d'une ligne
     */
    private void updateMvtStockAnnulation(LigneCommandeFournisseur ligne) {
        MvtStockDto mvtStockDto = MvtStockDto.builder()
                .article(ArticleDto.fromEntity(ligne.getArticle()))
                .dateMvt(Instant.now())
                .typeMvt(ETypeMvtStock.ENTREE) // Ou CORRECTION_POS selon votre enum
                .sourceMvt(ESourceMvtStock.VENTE)
                .quantite(ligne.getQuantite()) // On remet la quantité initiale en stock
                .idEntreprise(ligne.getIdEntreprise())
                .build();
        mvtStockService.entreeStock(mvtStockDto);
    }

    private void updateMvtStock(LigneCommandeFournisseur lig) {
        MvtStockDto mvtStockDto = MvtStockDto.builder()
                .article(ArticleDto.fromEntity(lig.getArticle()))
                .dateMvt(Instant.now())
                .typeMvt(ETypeMvtStock.SORTIE)
                .sourceMvt(ESourceMvtStock.VENTE)
                .quantite(lig.getQuantite())
                .idEntreprise(lig.getIdEntreprise())
                .build();
        mvtStockService.sortieStock(mvtStockDto);
    }



//    @Override
//    public CommandeFournisseurDto save(CommandeFournisseurDto dto) {
//
//        log.info("Saving command with {} lines",
//                dto.getLigneCommandeFournisseurs() != null ? dto.getLigneCommandeFournisseurs().size() : 0);
//
//        if (dto.getLigneCommandeFournisseurs() != null) {
//            log.info("Ligne details: {}", dto.getLigneCommandeFournisseurs());
//        }
//
//
//        List<String> errors = CommandeFournisseurValidator.validate(dto);
//
//        if (!errors.isEmpty()) {
//            log.error("Commande fournisseur n'est pas valide");
//            throw new InvalidEntityException("La commande fournisseur n'est pas valide", ErrorCodes.COMMANDE_FOURNISSEUR_NOT_VALID, errors);
//        }
//
//        if (dto.getId() != null && dto.isCommandeLivree()) {
//            throw new InvalidOperationException("Impossible de modifier la commande lorsqu'elle est livree", ErrorCodes.COMMANDE_FOURNISSEUR_NON_MODIFIABLE);
//        }
//
//        Optional<Fournisseur> fournisseur = fournisseurRepository.findById(dto.getFournisseur().getId());
//        if (fournisseur.isEmpty()) {
//            log.warn("Fournisseur with ID {} was not found in the DB", dto.getFournisseur().getId());
//            throw new EntityNotFoundException("Aucun fournisseur avec l'ID" + dto.getFournisseur().getId() + " n'a ete trouve dans la BDD",
//                    ErrorCodes.FOURNISSEUR_NOT_FOUND);
//        }
//
//        List<String> articleErrors = new ArrayList<>();
//
//        if (dto.getLigneCommandeFournisseurs() != null) {
//            dto.getLigneCommandeFournisseurs().forEach(ligCmdFrs -> {
//                if (ligCmdFrs.getArticle() != null) {
//                    Optional<Article> article = articleRepository.findById(ligCmdFrs.getArticle().getId());
//                    if (article.isEmpty()) {
//                        articleErrors.add("L'article avec l'ID " + ligCmdFrs.getArticle().getId() + " n'existe pas");
//                    }
//                } else {
//                    articleErrors.add("Impossible d'enregister une commande avec un aticle NULL");
//                }
//            });
//        }
//
//        if (!articleErrors.isEmpty()) {
//            log.warn("");
//            throw new InvalidEntityException("Article n'existe pas dans la BDD", ErrorCodes.ARTICLE_NOT_FOUND, articleErrors);
//        }
//        dto.setDateCommande(Instant.now());
//        CommandeFournisseur savedCmdFrs = commandeFournisseurRepository.save(CommandeFournisseurDto.toEntity(dto));
//
//        if (dto.getLigneCommandeFournisseurs() != null) {
//            dto.getLigneCommandeFournisseurs().forEach(ligCmdFrs -> {
//                LigneCommandeFournisseur ligneCommandeFournisseurs = LigneCommandeFournisseurDto.toEntity(ligCmdFrs);
//                ligneCommandeFournisseurs.setCommandeFournisseur(savedCmdFrs);
//                ligneCommandeFournisseurs.setIdEntreprise(savedCmdFrs.getIdEntreprise());
//                LigneCommandeFournisseur saveLigne = ligneCommandeFournisseurRepository.save(ligneCommandeFournisseurs);
//
//                effectuerEntree(saveLigne);
//            });
//        }
//
//        return CommandeFournisseurDto.fromEntity(savedCmdFrs);
//    }

    @Override
    public CommandeFournisseurDto updateEtatCommande(Integer idCommandeFournisseur, EEtatCommande etatCommande) {
        checkIdCommande(idCommandeFournisseur);
        if (!StringUtils.hasLength(String.valueOf(etatCommande))) {
            log.error("L'etat de la commande fournisseur is NULL");
            throw new InvalidOperationException("Impossible de modifier l'etat de la commande avec un etat null",
                    ErrorCodes.COMMANDE_FOURNISSEUR_NON_MODIFIABLE);
        }
        CommandeFournisseurDto commandeFournisseur = checkEtatCommande(idCommandeFournisseur);
        commandeFournisseur.setEtatCommande(etatCommande);

        CommandeFournisseur savedCommande = commandeFournisseurRepository.save(CommandeFournisseurDto.toEntity(commandeFournisseur));
        if (commandeFournisseur.isCommandeLivree()) {
            updateMvtStock(idCommandeFournisseur);
        }
        return CommandeFournisseurDto.fromEntity(savedCommande);
    }

    @Override
    public CommandeFournisseurDto updateQuantiteCommande(Integer idCommandeFournisseur, Integer idLigneCommande, BigDecimal quantite) {
        checkIdCommande(idCommandeFournisseur);
        checkIdLigneCommande(idLigneCommande);

        if (quantite == null || quantite.compareTo(BigDecimal.ZERO) == 0) {
            log.error("L'ID de la ligne commande is NULL");
            throw new InvalidOperationException("Impossible de modifier l'etat de la commande avec une quantite null ou ZERO",
                    ErrorCodes.COMMANDE_FOURNISSEUR_NON_MODIFIABLE);
        }

        CommandeFournisseurDto commandeFournisseur = checkEtatCommande(idCommandeFournisseur);
        Optional<LigneCommandeFournisseur> ligneCommandeFournisseurOptional = findLigneCommandeFournisseur(idLigneCommande);

        LigneCommandeFournisseur ligneCommandeFounisseurs = ligneCommandeFournisseurOptional.get();
        ligneCommandeFounisseurs.setQuantite(quantite);
        ligneCommandeFournisseurRepository.save(ligneCommandeFounisseurs);

        return commandeFournisseur;
    }

    @Override
    public CommandeFournisseurDto updateFournisseur(Integer idCommandeFournisseur, Integer idFournisseur) {
        checkIdCommande(idCommandeFournisseur);
        if (idFournisseur == null) {
            log.error("L'ID du fournisseur is NULL");
            throw new InvalidOperationException("Impossible de modifier l'etat de la commande avec un ID fournisseur null",
                    ErrorCodes.COMMANDE_FOURNISSEUR_NON_MODIFIABLE);
        }
        CommandeFournisseurDto commandeFournisseur = checkEtatCommande(idCommandeFournisseur);
        Optional<Fournisseur> fournisseurOptional = fournisseurRepository.findById(idFournisseur);
        if (fournisseurOptional.isEmpty()) {
            throw new EntityNotFoundException(
                    "Aucun fournisseur n'a ete trouve avec l'ID " + idFournisseur, ErrorCodes.FOURNISSEUR_NOT_FOUND);
        }
        commandeFournisseur.setFournisseur(FournisseurDto.fromEntity(fournisseurOptional.get()));

        return CommandeFournisseurDto.fromEntity(
                commandeFournisseurRepository.save(CommandeFournisseurDto.toEntity(commandeFournisseur))
        );
    }

    @Override
    public CommandeFournisseurDto updateArticle(Integer idCommandeFournisseur, Integer idLigneCommande, Integer idArticle) {
        checkIdCommande(idCommandeFournisseur);
        checkIdLigneCommande(idLigneCommande);
        checkIdArticle(idArticle, "nouvel");

        CommandeFournisseurDto commandeFournisseur = checkEtatCommande(idCommandeFournisseur);

        Optional<LigneCommandeFournisseur> ligneCommandeFournisseurs = findLigneCommandeFournisseur(idLigneCommande);

        Optional<Article> articleOptional = articleRepository.findById(idArticle);
        if (articleOptional.isEmpty()) {
            throw new EntityNotFoundException(
                    "Aucune article n'a ete trouve avec l'ID " + idArticle, ErrorCodes.ARTICLE_NOT_FOUND);
        }

        List<String> errors = ArticleValidator.validate(ArticleDto.fromEntity(articleOptional.get()));
        if (!errors.isEmpty()) {
            throw new InvalidEntityException("Article invalid", ErrorCodes.ARTICLE_NOT_VALID, errors);
        }

        LigneCommandeFournisseur ligneCommandeFournisseurToSaved = ligneCommandeFournisseurs.get();
        ligneCommandeFournisseurToSaved.setArticle(articleOptional.get());
        ligneCommandeFournisseurRepository.save(ligneCommandeFournisseurToSaved);

        return commandeFournisseur;
    }

    private void updateMvtStock(Integer idCommandeFournisseur) {
        List<LigneCommandeFournisseur> ligneCommandeFournisseur = ligneCommandeFournisseurRepository.findAllByCommandeFournisseurId(idCommandeFournisseur);
        ligneCommandeFournisseur.forEach(lig -> {
            effectuerEntree(lig);
        });
    }

    @Override
    public CommandeFournisseurDto findById(Integer id) {
        if (id == null) {
            log.error("Commande fournisseur ID is NULL");
            return null;
        }
        return commandeFournisseurRepository.findById(id)
                .map(CommandeFournisseurDto::fromEntity)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Aucune commande fournisseur n'a ete trouve avec l'ID " + id, ErrorCodes.COMMANDE_FOURNISSEUR_NOT_FOUND
                ));
    }

    @Override
    public CommandeFournisseurDto findByCode(String code) {
        if(!StringUtils.hasLength(code)){
            log.error("Commande fournisseur CODE is null");
            return null;
        }
        return commandeFournisseurRepository.findCommandeFournisseurByCode(code)
                .map(CommandeFournisseurDto::fromEntity)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Aucune commande fournisseur n'a été trouvé avec le CODE " + code,ErrorCodes.COMMANDE_FOURNISSEUR_NOT_FOUND
                ));
    }

    @Override
    public List<CommandeFournisseurDto> findAll() {
        return commandeFournisseurRepository.findAll().stream()
                .map(CommandeFournisseurDto::fromEntity)
                .collect(Collectors.toList());
    }


    @Override
    public List<CommandeFournisseurDto> findAllCommandeFournisseurByIdEntreprise(Integer idEntreprise) {
        if (idEntreprise == null) {
            log.error("Entreprise ID is null");
            return List.of();
        }
        return commandeFournisseurRepository.findAllByIdEntreprise(idEntreprise).stream()
                .map(CommandeFournisseurDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<LigneCommandeFournisseurDto> findAllLignesCommandesFournisseurByCommandeFournisseurId(Integer idCommandeFournisseur) {
        return ligneCommandeFournisseurRepository.findAllByCommandeFournisseurId(idCommandeFournisseur).stream()
                .map(LigneCommandeFournisseurDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public void delete(Integer id) {
        if (id == null) {
            log.error("Commande fournisseur ID is NULL");
            return;
        }
        List<LigneCommandeFournisseur> ligneCommandeFournisseurs = ligneCommandeFournisseurRepository.findAllByCommandeFournisseurId(id);
        if (!ligneCommandeFournisseurs.isEmpty()) {
            ligneCommandeFournisseurRepository.deleteAll(ligneCommandeFournisseurs);
            log.info("Lignes de commandeFournisseur pour l'achat ID {} supprimées", id);
//            throw new InvalidOperationException("Impossible de supprimer une commande fournisseur deja utilisee",
//                    ErrorCodes.COMMANDE_FOURNISSEUR_ALREADY_IN_USE);
        }
        commandeFournisseurRepository.deleteById(id);
        log.info("CommandeFournisseur ID {} supprimée avec succès", id);
    }


    @Override
    public CommandeFournisseurDto deleteArticle(Integer idCommandeFournisseur, Integer idLigneCommande) {
        checkIdCommande(idCommandeFournisseur);
        checkIdLigneCommande(idLigneCommande);

        CommandeFournisseurDto commandeFournisseur = checkEtatCommande(idCommandeFournisseur);
        // Just to check the LigneCommandeFournisseur and inform the fournisseur in case it is absent
        findLigneCommandeFournisseur(idLigneCommande);
        ligneCommandeFournisseurRepository.deleteById(idLigneCommande);

        return commandeFournisseur;
    }


    private Optional<LigneCommandeFournisseur> findLigneCommandeFournisseur(Integer idLigneCommande) {
        Optional<LigneCommandeFournisseur> ligneCommandeFournisseurOptional = ligneCommandeFournisseurRepository.findById(idLigneCommande);
        if (ligneCommandeFournisseurOptional.isEmpty()) {
            throw new EntityNotFoundException(
                    "Aucune ligne commande fournisseur n'a ete trouve avec l'ID " + idLigneCommande,
                    ErrorCodes.COMMANDE_FOURNISSEUR_NOT_FOUND);
        }
        return ligneCommandeFournisseurOptional;
    }

    private CommandeFournisseurDto checkEtatCommande(Integer idCommandeFournisseur) {
        CommandeFournisseurDto commandeFournisseur = findById(idCommandeFournisseur);
        if (commandeFournisseur.isCommandeLivree()) {
            throw new InvalidOperationException("Impossible de modifier la commande lorsqu'elle est livree",
                    ErrorCodes.COMMANDE_FOURNISSEUR_NON_MODIFIABLE);
        }
        return commandeFournisseur;
    }

    private void checkIdCommande(Integer idCommandeFournisseur) {
        if (idCommandeFournisseur == null) {
            log.error("Commande fournisseur ID is NULL");
            throw new InvalidOperationException("Impossible de modifier l'etat de la commande avec un ID null",
                    ErrorCodes.COMMANDE_FOURNISSEUR_NON_MODIFIABLE);
        }
    }

    private void checkIdLigneCommande(Integer idLigneCommande) {
        if (idLigneCommande == null) {
            log.error("L'ID de la ligne commande is NULL");
            throw new InvalidOperationException("Impossible de modifier l'etat de la commande avec une ligne de commande null",
                    ErrorCodes.COMMANDE_FOURNISSEUR_NON_MODIFIABLE);
        }
    }

    private void checkIdArticle(Integer idArticle, String msg) {
        if (idArticle == null) {
            log.error("L'ID de " + msg + " is NULL");
            throw new InvalidOperationException("Impossible de modifier l'etat de la commande avec un " + msg + " ID article null",
                    ErrorCodes.COMMANDE_FOURNISSEUR_NON_MODIFIABLE);
        }
    }

    private void effectuerEntree(LigneCommandeFournisseur lig) {
        MvtStockDto mvtStkDto = MvtStockDto.builder()
                .article(ArticleDto.fromEntity(lig.getArticle()))
                .dateMvt(Instant.now())
                .typeMvt(ETypeMvtStock.ENTREE)
                .sourceMvt(ESourceMvtStock.COMMANDE_FOURNISSEUR)
                .quantite(lig.getQuantite())
                .idEntreprise(lig.getIdEntreprise())
                .build();
        mvtStockService.entreeStock(mvtStkDto);
    }

    @Override
    public String getLastCodeCommandeFournisseur() {
        // CORRECTION : Utilisez l'instance "commandeFournisseurRepository" (minuscule)
        // et extrayez le code de l'objet CommandeFournisseur
        return commandeFournisseurRepository.findTopByOrderByCodeDesc()
                .map(CommandeFournisseur::getCode) // On transforme la CommandeFournisseur en String (son code)
                .orElse("CMF0000");           // Valeur par défaut si aucun commandeFournisseur n'existe
    }
}










//package com.tchindaClovis.gestiondestock.services.impl;
//
//import com.tchindaClovis.gestiondestock.dto.CommandeFournisseurDto;
//import com.tchindaClovis.gestiondestock.dto.LigneCommandeFournisseurDto;
//import com.tchindaClovis.gestiondestock.exception.EntityNotFoundException;
//import com.tchindaClovis.gestiondestock.exception.ErrorCodes;
//import com.tchindaClovis.gestiondestock.exception.InvalidEntityException;
//import com.tchindaClovis.gestiondestock.model.*;
//import com.tchindaClovis.gestiondestock.repository.*;
//import com.tchindaClovis.gestiondestock.services.CommandeFournisseurService;
//import com.tchindaClovis.gestiondestock.services.MvtStockService;
//import com.tchindaClovis.gestiondestock.validator.CommandeFournisseurValidator;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//import org.springframework.util.StringUtils;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Optional;
//import java.util.stream.Collectors;
//
//@Service
//@Slf4j
//public class CommandeFournisseurServiceImpl implements CommandeFournisseurService {
//
//    private CommandeFournisseurRepository commandeFournisseurRepository;
//    private LigneCommandeFournisseurRepository ligneCommandeFournisseurRepository;
//    private FournisseurRepository fournisseurRepository;
//    private ArticleRepository articleRepository;
//
//    private MvtStockService mvtStockService;
//
//    @Autowired
//    public CommandeFournisseurServiceImpl(CommandeFournisseurRepository commandeFournisseurRepository,
//                                          LigneCommandeFournisseurRepository ligneCommandeFournisseurRepository,
//                                          FournisseurRepository fournisseurRepository,
//                                          ArticleRepository articleRepository,
//                                          MvtStockService mvtStockService) {
//        this.commandeFournisseurRepository = commandeFournisseurRepository;
//        this.ligneCommandeFournisseurRepository = ligneCommandeFournisseurRepository;
//        this.fournisseurRepository = fournisseurRepository;
//        this.articleRepository = articleRepository;
//        this.mvtStockService = mvtStockService;
//    }
//
//    @Override
//    public CommandeFournisseurDto save(CommandeFournisseurDto dto) {
//
//        List<String> errors = CommandeFournisseurValidator.validate(dto);
//
//        if(!errors.isEmpty()){
//            log.error(" Commande fournisseur n'est pas valide", dto);
//            throw new InvalidEntityException("La commande fournisseur n'est pas valide",
//                    ErrorCodes.COMMANDE_FOURNISSEUR_NOT_VALID, errors);
//        }
//        Optional<Fournisseur> fournisseur = fournisseurRepository.findById(dto.getFournisseur().getId());
//        if(!fournisseur.isEmpty()) {
//            log.error(" Fournisseur with ID {} was not found in the DB", dto.getFournisseur().getId());
//            throw new EntityNotFoundException("Aucun fournisseur avec l'ID" + dto.getFournisseur().getId() +
//                    "n'a été trouvé dans la BDD", ErrorCodes.FOURNISSEUR_NOT_FOUND);
//        }
//
//        List<String> articleErrors = new ArrayList<>();
//
//        if(dto.getLigneCommandeFournisseurs() != null) {
//            dto.getLigneCommandeFournisseurs().forEach(ligCmdFns -> {
//                if (ligCmdFns.getArticle() != null) {
//                    Optional<Article> article = articleRepository.findById(ligCmdFns.getArticle().getId());
//                    if (article.isEmpty()) {
//                        articleErrors.add("L'article avec l'ID " + ligCmdFns.getArticle().getId() +
//                                " n'existe pas");
//                    }
//                }else{
//                    articleErrors.add("Impossible d'enregistrer une commande avec un article NULL");
//                }
//            });
//        }
//
//        if (!articleErrors.isEmpty()){
//            log.warn("");
//            throw new InvalidEntityException("L'article n'existe pas dans la BDD",
//                    ErrorCodes.ARTICLE_NOT_FOUND, articleErrors);
//        }
//
//        CommandeFournisseur savedCmdFns = commandeFournisseurRepository.save(CommandeFournisseurDto.toEntity(dto));
//
//        if(dto.getLigneCommandeFournisseurs() != null) {
//            dto.getLigneCommandeFournisseurs().forEach(ligCmdFns -> {
//                LigneCommandeFournisseur ligneCommandeFournisseur = LigneCommandeFournisseurDto.toEntity(ligCmdFns); //transformer l'objet DTO en entité
//                ligneCommandeFournisseur.setCommandeFournisseur(savedCmdFns); // lier la ligne de commande à la commande enregistrée
//                ligneCommandeFournisseurRepository.save(ligneCommandeFournisseur);
//            });
//        }
//        return CommandeFournisseurDto.fromEntity(savedCmdFns);
//    }
//
//
//    @Override
//    public CommandeFournisseurDto findById(Integer id) {
//        if(id == null){
//            log.error("Commande fournisseur ID is NULL");
//            return null;
//        }
//        return commandeFournisseurRepository.findById(id)
//                .map(CommandeFournisseurDto::fromEntity)
//                .orElseThrow(() -> new EntityNotFoundException(
//                        "Aucune commande fournisseur n'a été trouvé avec l'ID " + id,ErrorCodes.COMMANDE_FOURNISSEUR_NOT_FOUND
//                ));
//    }
//
//    @Override
//    public CommandeFournisseurDto findByCode(String code) {
//        if(!StringUtils.hasLength(code)){
//            log.error("Commande fournisseur CODE is null");
//            return null;
//        }
//        return commandeFournisseurRepository.findCommandeFournisseurByCode(code)
//                .map(CommandeFournisseurDto::fromEntity)
//                .orElseThrow(() -> new EntityNotFoundException(
//                        "Aucune commande fournisseur n'a été trouvé avec le CODE " + code,ErrorCodes.COMMANDE_FOURNISSEUR_NOT_FOUND
//                ));
//    }
//
//    @Override
//    public List<CommandeFournisseurDto> findAll() {
//        return commandeFournisseurRepository.findAll().stream()
//                .map(CommandeFournisseurDto::fromEntity)
//                .collect(Collectors.toList());
//    }
//
//    @Override
//    public void delete(Integer id) {
//        if(id == null){
//            log.error("Commande fournisseur ID is NULL");
//            return ;
//        }
//        commandeFournisseurRepository.deleteById(id);
//    }
//}
