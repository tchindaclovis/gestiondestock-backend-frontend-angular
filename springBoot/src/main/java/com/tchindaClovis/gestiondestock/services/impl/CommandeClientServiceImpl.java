package com.tchindaClovis.gestiondestock.services.impl;

import com.tchindaClovis.gestiondestock.dto.*;
import com.tchindaClovis.gestiondestock.exception.EntityNotFoundException;
import com.tchindaClovis.gestiondestock.exception.ErrorCodes;
import com.tchindaClovis.gestiondestock.exception.InvalidEntityException;
import com.tchindaClovis.gestiondestock.exception.InvalidOperationException;
import com.tchindaClovis.gestiondestock.model.*;
import com.tchindaClovis.gestiondestock.model.ESourceMvtStock;
import com.tchindaClovis.gestiondestock.repository.ArticleRepository;
import com.tchindaClovis.gestiondestock.repository.ClientRepository;
import com.tchindaClovis.gestiondestock.repository.CommandeClientRepository;
import com.tchindaClovis.gestiondestock.repository.LigneCommandeClientRepository;
import com.tchindaClovis.gestiondestock.services.CommandeClientService;
import com.tchindaClovis.gestiondestock.services.MvtStockService;
import com.tchindaClovis.gestiondestock.validator.ArticleValidator;
import com.tchindaClovis.gestiondestock.validator.CommandeClientValidator;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
@Service
@Slf4j
public class CommandeClientServiceImpl implements CommandeClientService {

    private CommandeClientRepository commandeClientRepository;
    private LigneCommandeClientRepository ligneCommandeClientRepository;
    private ClientRepository clientRepository;
    private ArticleRepository articleRepository;
    private MvtStockService mvtStockService;

    @Autowired
    public CommandeClientServiceImpl(CommandeClientRepository commandeClientRepository,
                                     ClientRepository clientRepository, ArticleRepository articleRepository, LigneCommandeClientRepository ligneCommandeClientRepository,
                                     MvtStockService mvtStockService) {
        this.commandeClientRepository = commandeClientRepository;
        this.ligneCommandeClientRepository = ligneCommandeClientRepository;
        this.clientRepository = clientRepository;
        this.articleRepository = articleRepository;
        this.mvtStockService = mvtStockService;
    }


    @Override
    @Transactional // CRITIQUE: Assure la gestion de la session Hibernate et du rollback en cas d'erreur
    public CommandeClientDto save(CommandeClientDto dto) {

        log.info("Saving command with {} lines",
                dto.getLigneCommandeClients() != null ? dto.getLigneCommandeClients().size() : 0);

        // 1. Validation de la structure du DTO
        List<String> errors = CommandeClientValidator.validate(dto);
        if (!errors.isEmpty()) {
            log.error("Commande client n'est pas valide");
            throw new InvalidEntityException(
                    "La commande client n'est pas valide", ErrorCodes.COMMANDE_CLIENT_NOT_VALID, errors);
        }

        // 2. Vérification si modification autorisée (si livrée, on bloque)
        if (dto.getId() != null && dto.isCommandeLivree()) {
            throw new InvalidOperationException(
                    "Impossible de modifier la commande lorsqu'elle est livree", ErrorCodes.COMMANDE_CLIENT_NON_MODIFIABLE);
        }

        // 3. Vérification de l'existence du client
        Optional<Client> client = clientRepository.findById(dto.getClient().getId());
        if (client.isEmpty()) {
            log.warn("Client with ID {} was not found in the DB", dto.getClient().getId());
            throw new EntityNotFoundException(
                    "Aucun client avec l'ID " + dto.getClient().getId() + " n'a ete trouve dans la BDD",
                    ErrorCodes.CLIENT_NOT_FOUND);
        }

        // 4. Vérification de l'existence des articles
        List<String> articleErrors = new ArrayList<>();
        if (dto.getLigneCommandeClients() != null) {
            dto.getLigneCommandeClients().forEach(ligCmdClt -> {
                if (ligCmdClt.getArticle() != null) {
                    Optional<Article> article = articleRepository.findById(ligCmdClt.getArticle().getId());
                    if (article.isEmpty()) {
                        articleErrors.add("L'article avec l'ID " + ligCmdClt.getArticle().getId() + " n'existe pas");
                    }
                } else {
                    articleErrors.add("Impossible d'enregister une commande avec un aticle NULL");
                }
            });
        }

        if (!articleErrors.isEmpty()) {
            throw new InvalidEntityException(
                    "Article n'existe pas dans la BDD", ErrorCodes.ARTICLE_NOT_FOUND, articleErrors);
        }




        CommandeClient commandeClientToSave;

        if (dto.getId() != null) {
            // --- CAS MISE À JOUR ---
            // On force le chargement des lignes avec FETCH
            commandeClientToSave = (CommandeClient) commandeClientRepository.findByIdWithLignes(dto.getId())
                    .orElseThrow(() -> new EntityNotFoundException("CommandeClient introuvable", ErrorCodes.COMMANDE_CLIENT_NOT_FOUND));

            // 1. COMPENSATION DES STOCKS (Annulation des anciens stocks)
            if (commandeClientToSave.getLigneCommandeClients() != null) {
                // Comme on a utilisé JOIN FETCH, cette liste est chargée et accessible
                commandeClientToSave.getLigneCommandeClients().forEach(this::updateMvtStockAnnulation);
            }

            // 2. MISE À JOUR DES INFOS GÉNÉRALES
            commandeClientToSave.setCode(dto.getCode());
            commandeClientToSave.setDateCommande(dto.getDateCommande());
            commandeClientToSave.setIdEntreprise(dto.getIdEntreprise());

            // 3. NETTOYAGE DES LIGNES
            // On supprime physiquement en BDD
//            ligneVenteRepository.deleteByVenteId(dto.getId());
            // Au lieu de supprimer manuellement avec le repository, on vide la collection
            // orphanRemoval = true dans l'entité s'occupera de la suppression en base
            // On vide la collection Java pour éviter les conflits d'état
            commandeClientToSave.getLigneCommandeClients().clear();

            // On synchronise pour que Hibernate sache que la commandeClient est "propre"
            commandeClientRepository.saveAndFlush(commandeClientToSave);

        } else {
            // --- CAS CRÉATION ---
            commandeClientToSave = CommandeClientDto.toEntity(dto);
            if (commandeClientToSave.getLigneCommandeClients() == null) {
                commandeClientToSave.setLigneCommandeClients(new ArrayList<>());
            }
        }

        // 4. ATTACHEMENT DES NOUVELLES LIGNES
        if (dto.getLigneCommandeClients() != null) {
            // Initialiser la liste si elle est nulle pour éviter un NPE
            if (commandeClientToSave.getLigneCommandeClients() == null) {
                commandeClientToSave.setLigneCommandeClients(new ArrayList<>());
            }

            for (LigneCommandeClientDto ligDto : dto.getLigneCommandeClients()) {
                LigneCommandeClient lig = LigneCommandeClientDto.toEntity(ligDto);
                lig.setId(null); // <--- FORCE LE NULL : C'est la clé pour éviter l'Optimistic Locking
                lig.setCommandeClient(commandeClientToSave); // Lien vers le parent
                lig.setIdEntreprise(dto.getIdEntreprise()); // Important pour vos filtres

                // On ajoute à la collection du parent pour que le Cascade/Save fonctionne
                commandeClientToSave.getLigneCommandeClients().add(lig);
            }
        }

        // 5. SAUVEGARDE FINALE ET NOUVEAUX STOCKS
        // On fait un saveAndFlush pour forcer l'écriture des LigneVente en BDD
        CommandeClient savedCommandeClient = commandeClientRepository.saveAndFlush(commandeClientToSave);

        // 6. MISE À JOUR DES STOCKS
        if (savedCommandeClient.getLigneCommandeClients() != null) {
            for (LigneCommandeClient lig : savedCommandeClient.getLigneCommandeClients()) {
                // Sécurité : on s'assure que l'article est bien présent avant de mouvementer le stock
                if (lig.getArticle() == null && lig.getArticle().getId() != null) {
                    // Recharger l'article si nécessaire ou s'assurer que le mapper le fait
                    lig.setArticle(articleRepository.findById(lig.getArticle().getId()).orElse(null));
                }

                // On vérifie que la quantité n'est pas nulle
                if (lig.getQuantite() != null && lig.getQuantite().compareTo(BigDecimal.ZERO) != 0) {
                    updateMvtStock(lig);
                } else {
                    log.warn("Ligne de commandeClient ignorée pour le stock : Quantité nulle pour l'article {}",
                            lig.getArticle() != null ? lig.getArticle().getId() : "Inconnu");
                }
            }
        }
        return CommandeClientDto.fromEntity(savedCommandeClient);
    }


    /**
     * Méthode pour réintégrer les stocks lors de l'annulation/modification d'une ligne
     */
    private void updateMvtStockAnnulation(LigneCommandeClient ligne) {
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

    private void updateMvtStock(LigneCommandeClient lig) {
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
//    @Transactional
//    public CommandeClientDto save(CommandeClientDto dto) {
//
//        log.info("Saving command with {} lines",
//                dto.getLigneCommandeClients() != null ? dto.getLigneCommandeClients().size() : 0);
//
//        if (dto.getLigneCommandeClients() != null) {
//            log.info("Ligne details: {}", dto.getLigneCommandeClients());
//        }
//
//
//        List<String> errors = CommandeClientValidator.validate(dto);
//        if (!errors.isEmpty()) {
//            log.error("Commande client n'est pas valide");
//            throw new InvalidEntityException(
//                    "La commande client n'est pas valide", ErrorCodes.COMMANDE_CLIENT_NOT_VALID, errors);
//        }
//
//        if (dto.getId() != null && dto.isCommandeLivree()) {
//            throw new InvalidOperationException(
//                    "Impossible de modifier la commande lorsqu'elle est livree", ErrorCodes.COMMANDE_CLIENT_NON_MODIFIABLE);
//        }
//
//        Optional<Client> client = clientRepository.findById(dto.getClient().getId());
//        if (client.isEmpty()) {
//            log.warn("Client with ID {} was not found in the DB", dto.getClient().getId());
//            throw new EntityNotFoundException(
//                    "Aucun client avec l'ID" + dto.getClient().getId() + " n'a ete trouve dans la BDD",
//                    ErrorCodes.CLIENT_NOT_FOUND);
//        }
//
//        List<String> articleErrors = new ArrayList<>();
//
//        if (dto.getLigneCommandeClients() != null) {
//            dto.getLigneCommandeClients().forEach(ligCmdClt -> {
//                if (ligCmdClt.getArticle() != null) {
//                    Optional<Article> article = articleRepository.findById(ligCmdClt.getArticle().getId());
//                    if (article.isEmpty()) {
//                        articleErrors.add("L'article avec l'ID " + ligCmdClt.getArticle().getId() + " n'existe pas");
//                    }
//                } else {
//                    articleErrors.add("Impossible d'enregister une commande avec un aticle NULL");
//                }
//            });
//        }
//
//        if (!articleErrors.isEmpty()) {
//            log.warn("");
//            throw new InvalidEntityException(
//                    "Article n'existe pas dans la BDD", ErrorCodes.ARTICLE_NOT_FOUND, articleErrors);
//        }
//        dto.setDateCommande(Instant.now());
//
//        CommandeClient savedCmdClt = commandeClientRepository.save(CommandeClientDto.toEntity(dto));
//
//        if (dto.getLigneCommandeClients() != null) {
//            dto.getLigneCommandeClients().forEach(ligCmdClt -> {
//                LigneCommandeClient ligneCommandeClients = LigneCommandeClientDto.toEntity(ligCmdClt);
////                log.info("LIGNES A SAUVER : {}", dto.getLigneCommandeClients());
//                ligneCommandeClients.setCommandeClient(savedCmdClt);
//                ligneCommandeClients.setIdEntreprise(dto.getIdEntreprise());
//                LigneCommandeClient savedLigneCmd = ligneCommandeClientRepository.save(ligneCommandeClients);
//
//                effectuerSortie(savedLigneCmd);
//            });
//        }
//
//        return CommandeClientDto.fromEntity(savedCmdClt);
//    }


    @Override
    public CommandeClientDto findById(Integer id) {
        if (id == null) {
            log.error("Commande client ID is NULL");
            return null;
        }
        return commandeClientRepository.findById(id)
                .map(CommandeClientDto::fromEntity)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Aucune commande client n'a ete trouve avec l'ID " + id, ErrorCodes.COMMANDE_CLIENT_NOT_FOUND
                ));
    }

    @Override
    public CommandeClientDto findByCode(String code) {
        if (!StringUtils.hasLength(code)) {
            log.error("Commande client CODE is NULL");
            return null;
        }
        return commandeClientRepository.findCommandeClientByCode(code)
                .map(CommandeClientDto::fromEntity)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Aucune commande client n'a ete trouve avec le CODE " + code, ErrorCodes.COMMANDE_CLIENT_NOT_FOUND
                ));
    }

    @Override
    public List<CommandeClientDto> findAll() {
        return commandeClientRepository.findAll().stream()
                .map(CommandeClientDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<CommandeClientDto> findAllCommandeClientByIdEntreprise(Integer idEntreprise) {
        if (idEntreprise == null) {
            log.error("Entreprise ID is null");
            return List.of();
        }
        return commandeClientRepository.findAllByIdEntreprise(idEntreprise).stream()
                .map(CommandeClientDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional // CRITIQUE : Assure que tout est validé en base à la fin
    public void delete(Integer id) {
        if (id == null) {
            log.error("Commande client ID is NULL");
            return;
        }
        List<LigneCommandeClient> ligneCommandeClients = ligneCommandeClientRepository.findAllByCommandeClientId(id);
        if (!ligneCommandeClients.isEmpty()) {
            ligneCommandeClientRepository.deleteAll(ligneCommandeClients);
            log.info("Lignes de commandeClient pour la commande ID {} supprimées", id);
//            throw new InvalidOperationException("Impossible de supprimer une commande client deja utilisee",
//                    ErrorCodes.COMMANDE_CLIENT_ALREADY_IN_USE);
        }
        commandeClientRepository.deleteById(id);
        log.info("CommandeClient ID {} supprimée avec succès", id);
    }

    @Override
    public List<LigneCommandeClientDto> findAllLignesCommandesClientByCommandeClientId(Integer idCommandeClient) {
        return ligneCommandeClientRepository.findAllByCommandeClientId(idCommandeClient).stream()
                .map(LigneCommandeClientDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public CommandeClientDto updateEtatCommande(Integer idCommandeClient, EEtatCommande etatCommande) {
        checkIdCommande(idCommandeClient);
        if (!StringUtils.hasLength(String.valueOf(etatCommande))) {
            log.error("L'etat de la commande client is NULL");
            throw new InvalidOperationException("Impossible de modifier l'etat de la commande avec un etat null",
                    ErrorCodes.COMMANDE_CLIENT_NON_MODIFIABLE);
        }
        CommandeClientDto commandeClient = checkEtatCommande(idCommandeClient);
        commandeClient.setEtatCommande(etatCommande);

        CommandeClient savedCmdClt = commandeClientRepository.save(CommandeClientDto.toEntity(commandeClient));
        if (commandeClient.isCommandeLivree()) {
            updateMvtStk(idCommandeClient);
        }

        return CommandeClientDto.fromEntity(savedCmdClt);
    }

    @Override
    public CommandeClientDto updateQuantiteCommande(Integer idCommandeClient, Integer idLigneCommande, BigDecimal quantite) {
        checkIdCommande(idCommandeClient);
        checkIdLigneCommande(idLigneCommande);

        if (quantite == null || quantite.compareTo(BigDecimal.ZERO) == 0) {
            log.error("L'ID de la ligne commande is NULL");
            throw new InvalidOperationException("Impossible de modifier l'etat de la commande avec une quantite null ou ZERO",
                    ErrorCodes.COMMANDE_CLIENT_NON_MODIFIABLE);
        }

        CommandeClientDto commandeClient = checkEtatCommande(idCommandeClient);
        Optional<LigneCommandeClient> ligneCommandeClientOptional = findLigneCommandeClient(idLigneCommande);

        LigneCommandeClient ligneCommandeClients = ligneCommandeClientOptional.get();
        ligneCommandeClients.setQuantite(quantite);
        ligneCommandeClientRepository.save(ligneCommandeClients);

        return commandeClient;
    }

    @Override
    public CommandeClientDto updateClient(Integer idCommandeClient, Integer idClient) {
        checkIdCommande(idCommandeClient);
        if (idClient == null) {
            log.error("L'ID du client is NULL");
            throw new InvalidOperationException("Impossible de modifier l'etat de la commande avec un ID client null",
                    ErrorCodes.COMMANDE_CLIENT_NON_MODIFIABLE);
        }
        CommandeClientDto commandeClient = checkEtatCommande(idCommandeClient);
        Optional<Client> clientOptional = clientRepository.findById(idClient);
        if (clientOptional.isEmpty()) {
            throw new EntityNotFoundException(
                    "Aucun client n'a ete trouve avec l'ID " + idClient, ErrorCodes.CLIENT_NOT_FOUND);
        }
        commandeClient.setClient(ClientDto.fromEntity(clientOptional.get()));

        return CommandeClientDto.fromEntity(
                commandeClientRepository.save(CommandeClientDto.toEntity(commandeClient))
        );
    }

    @Override
    public CommandeClientDto updateArticle(Integer idCommandeClient, Integer idLigneCommande, Integer idArticle) {
        checkIdCommande(idCommandeClient);
        checkIdLigneCommande(idLigneCommande);
        checkIdArticle(idArticle, "nouvel");

        CommandeClientDto commandeClient = checkEtatCommande(idCommandeClient);

        Optional<LigneCommandeClient> ligneCommandeClients = findLigneCommandeClient(idLigneCommande);

        Optional<Article> articleOptional = articleRepository.findById(idArticle);
        if (articleOptional.isEmpty()) {
            throw new EntityNotFoundException(
                    "Aucune article n'a ete trouve avec l'ID " + idArticle, ErrorCodes.ARTICLE_NOT_FOUND);
        }

        List<String> errors = ArticleValidator.validate(ArticleDto.fromEntity(articleOptional.get()));
        if (!errors.isEmpty()) {
            throw new InvalidEntityException("Article invalid", ErrorCodes.ARTICLE_NOT_VALID, errors);
        }

        LigneCommandeClient ligneCommandeClientToSaved = ligneCommandeClients.get();
        ligneCommandeClientToSaved.setArticle(articleOptional.get());
        ligneCommandeClientRepository.save(ligneCommandeClientToSaved);

        return commandeClient;
    }

    @Override
    public CommandeClientDto deleteArticle(Integer idCommandeClient, Integer idLigneCommande) {
        checkIdCommande(idCommandeClient);
        checkIdLigneCommande(idLigneCommande);

        CommandeClientDto commandeClient = checkEtatCommande(idCommandeClient);
        // Just to check the LigneCommandeClient and inform the client in case it is absent
        findLigneCommandeClient(idLigneCommande);
        ligneCommandeClientRepository.deleteById(idLigneCommande);

        return commandeClient;
    }

    private CommandeClientDto checkEtatCommande(Integer idCommandeClient) {
        CommandeClientDto commandeClient = findById(idCommandeClient);
        if (commandeClient.isCommandeLivree()) {
            throw new InvalidOperationException("Impossible de modifier la commande lorsqu'elle est livree", ErrorCodes.COMMANDE_CLIENT_NON_MODIFIABLE);
        }
        return commandeClient;
    }

    private Optional<LigneCommandeClient> findLigneCommandeClient(Integer idLigneCommande) {
        Optional<LigneCommandeClient> ligneCommandeClientOptional = ligneCommandeClientRepository.findById(idLigneCommande);
        if (ligneCommandeClientOptional.isEmpty()) {
            throw new EntityNotFoundException(
                    "Aucune ligne commande client n'a ete trouve avec l'ID " + idLigneCommande, ErrorCodes.COMMANDE_CLIENT_NOT_FOUND);
        }
        return ligneCommandeClientOptional;
    }

    private void checkIdCommande(Integer idCommandeClient) {
        if (idCommandeClient == null) {
            log.error("Commande client ID is NULL");
            throw new InvalidOperationException("Impossible de modifier l'etat de la commande avec un ID null",
                    ErrorCodes.COMMANDE_CLIENT_NON_MODIFIABLE);
        }
    }

    private void checkIdLigneCommande(Integer idLigneCommande) {
        if (idLigneCommande == null) {
            log.error("L'ID de la ligne commande is NULL");
            throw new InvalidOperationException("Impossible de modifier l'etat de la commande avec une ligne de commande null",
                    ErrorCodes.COMMANDE_CLIENT_NON_MODIFIABLE);
        }
    }


    private void checkIdArticle(Integer idArticle, String msg) {
        if (idArticle == null) {
            log.error("L'ID de " + msg + " is NULL");
            throw new InvalidOperationException("Impossible de modifier l'etat de la commande avec un " + msg + " ID article null",
                    ErrorCodes.COMMANDE_CLIENT_NON_MODIFIABLE);
        }
    }

    private void updateMvtStk(Integer idCommandeClient) {
        List<LigneCommandeClient> ligneCommandeClients = ligneCommandeClientRepository.findAllByCommandeClientId(idCommandeClient);
        ligneCommandeClients.forEach(lig -> {
            effectuerSortie(lig);
        });
    }

    private void effectuerSortie(LigneCommandeClient lig) {
        MvtStockDto mvtStockDto = MvtStockDto.builder()
                .article(ArticleDto.fromEntity(lig.getArticle()))
                .dateMvt(Instant.now())
                .typeMvt(ETypeMvtStock.SORTIE)
                .sourceMvt(ESourceMvtStock.COMMANDE_CLIENT)
                .quantite(lig.getQuantite())
                .idEntreprise(lig.getIdEntreprise())
                .build();
        mvtStockService.sortieStock(mvtStockDto);
    }

    public static CommandeClient toEntity(CommandeClientDto dto) {
        if (dto == null) return null;

        CommandeClient commandeClient = new CommandeClient();
        commandeClient.setId(dto.getId());
        commandeClient.setCode(dto.getCode());
        commandeClient.setDateCommande(dto.getDateCommande());
        commandeClient.setEtatCommande(dto.getEtatCommande());
        commandeClient.setIdEntreprise(dto.getIdEntreprise());
        commandeClient.setClient(ClientDto.toEntity(dto.getClient()));

        if (dto.getLigneCommandeClients() != null) {
            List<LigneCommandeClient> lignes = dto.getLigneCommandeClients()
                    .stream()
                    .map(LigneCommandeClientDto::toEntity)
                    .collect(Collectors.toList());

            lignes.forEach(l -> {
                l.setCommandeClient(commandeClient);
                l.setIdEntreprise(commandeClient.getIdEntreprise());
            });
//            lignes.forEach(l -> l.setCommandeClient(commandeClient));
            commandeClient.setLigneCommandeClients(lignes);
        }
        return commandeClient;
    }


    @Override
    public String getLastCodeCommandeClient() {
        // CORRECTION : Utilisez l'instance "commandeClientRepository" (minuscule)
        // et extrayez le code de l'objet CommandeClient
        return commandeClientRepository.findTopByOrderByCodeDesc()
                .map(CommandeClient::getCode) // On transforme la CommandeClient en String (son code)
                .orElse("CMC0000");           // Valeur par défaut si aucun commandeClient n'existe
    }
}





//package com.tchindaClovis.gestiondestock.services.impl;
//
//import com.tchindaClovis.gestiondestock.dto.ArticleDto;
//import com.tchindaClovis.gestiondestock.dto.ClientDto;
//import com.tchindaClovis.gestiondestock.dto.CommandeClientDto;
//import com.tchindaClovis.gestiondestock.dto.LigneCommandeClientDto;
//import com.tchindaClovis.gestiondestock.dto.MvtStockDto;
//import com.tchindaClovis.gestiondestock.exception.EntityNotFoundException;
//import com.tchindaClovis.gestiondestock.exception.ErrorCodes;
//import com.tchindaClovis.gestiondestock.exception.InvalidEntityException;
//import com.tchindaClovis.gestiondestock.exception.InvalidOperationException;
//import com.tchindaClovis.gestiondestock.model.*;
//import com.tchindaClovis.gestiondestock.model.ESourceMvtStock;
//import com.tchindaClovis.gestiondestock.repository.ArticleRepository;
//import com.tchindaClovis.gestiondestock.repository.ClientRepository;
//import com.tchindaClovis.gestiondestock.repository.CommandeClientRepository;
//import com.tchindaClovis.gestiondestock.repository.LigneCommandeClientRepository;
//import com.tchindaClovis.gestiondestock.services.CommandeClientService;
//import com.tchindaClovis.gestiondestock.services.MvtStockService;
//import com.tchindaClovis.gestiondestock.validator.ArticleValidator;
//import com.tchindaClovis.gestiondestock.validator.CommandeClientValidator;
//import java.math.BigDecimal;
//import java.time.Instant;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Optional;
//import java.util.stream.Collectors;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//import org.springframework.util.StringUtils;
//
//@Service
//@Slf4j
//public class CommandeClientServiceImpl implements CommandeClientService {
//
//    private CommandeClientRepository commandeClientRepository;
//    private LigneCommandeClientRepository ligneCommandeClientRepository;
//    private ClientRepository clientRepository;
//    private ArticleRepository articleRepository;
//    private MvtStockService mvtStockService;
//
//    @Autowired
//    public CommandeClientServiceImpl(CommandeClientRepository commandeClientRepository,
//                                     ClientRepository clientRepository, ArticleRepository articleRepository,
//                                     LigneCommandeClientRepository ligneCommandeClientRepository,
//                                     MvtStockService mvtStockService) {
//        this.commandeClientRepository = commandeClientRepository;
//        this.ligneCommandeClientRepository = ligneCommandeClientRepository;
//        this.clientRepository = clientRepository;
//        this.articleRepository = articleRepository;
//        this.mvtStockService = mvtStockService;
//    }
//
//    @Override
//    public CommandeClientDto save(CommandeClientDto dto) {
//
//        List<String> errors = CommandeClientValidator.validate(dto);
//
//        if (!errors.isEmpty()) {
//            log.error("Commande client n'est pas valide");
//            throw new InvalidEntityException("La commande client n'est pas valide",
//                    ErrorCodes.COMMANDE_CLIENT_NOT_VALID, errors);
//        }
//
//        if (dto.getId() != null && dto.isCommandeLivree()) {
//            throw new InvalidOperationException("Impossible de modifier la commande lorsqu'elle est livree",
//                    ErrorCodes.COMMANDE_CLIENT_NON_MODIFIABLE);
//        }
//
//        Optional<Client> client = clientRepository.findById(dto.getClient().getId());
//        if (client.isEmpty()) {
//            log.warn("Client with ID {} was not found in the DB", dto.getClient().getId());
//            throw new EntityNotFoundException("Aucun client avec l'ID" + dto.getClient().getId() +
//                    " n'a ete trouve dans la BDD",
//                    ErrorCodes.CLIENT_NOT_FOUND);
//        }
//
//        List<String> articleErrors = new ArrayList<>();
//
//        if (dto.getLigneCommandeClients() != null) {
//            dto.getLigneCommandeClients().forEach(ligCmdClt -> {
//                if (ligCmdClt.getArticle() != null) {
//                    Optional<Article> article = articleRepository.findById(ligCmdClt.getArticle().getId());
//                    if (article.isEmpty()) {
//                        articleErrors.add("L'article avec l'ID " + ligCmdClt.getArticle().getId() + " n'existe pas");
//                    }
//                } else {
//                    articleErrors.add("Impossible d'enregister une commande avec un aticle NULL");
//                }
//            });
//        }
//
//        if (!articleErrors.isEmpty()) {
//            log.warn("");
//            throw new InvalidEntityException("Article n'existe pas dans la BDD", ErrorCodes.ARTICLE_NOT_FOUND,
//                    articleErrors);
//        }
//        dto.setDateCommande(Instant.now());
//        CommandeClient savedCmdClt = commandeClientRepository.save(CommandeClientDto.toEntity(dto));
//
//        if (dto.getLigneCommandeClients() != null) {
//            dto.getLigneCommandeClients().forEach(ligCmdClt -> {
//                LigneCommandeClient ligneCommandeClient = LigneCommandeClientDto.toEntity(ligCmdClt);
//                ligneCommandeClient.setCommandeClient(savedCmdClt);
//                ligneCommandeClient.setIdEntreprise(dto.getIdEntreprise());
//                LigneCommandeClient savedLigneCmd = ligneCommandeClientRepository.save(ligneCommandeClient);
//
//                effectuerSortie(savedLigneCmd);
//            });
//        }
//
//        return CommandeClientDto.fromEntity(savedCmdClt);
//    }
//
//    @Override
//    public CommandeClientDto findById(Integer id) {
//        if (id == null) {
//            log.error("Commande client ID is NULL");
//            return null;
//        }
//        return commandeClientRepository.findById(id)
//                .map(CommandeClientDto::fromEntity)
//                .orElseThrow(() -> new EntityNotFoundException(
//                        "Aucune commande client n'a ete trouve avec l'ID " + id,
//                        ErrorCodes.COMMANDE_CLIENT_NOT_FOUND
//                ));
//    }
//
//
//    @Override
//    public CommandeClientDto findByCode(String code) {
//        if (!StringUtils.hasLength(code)) {
//            log.error("Commande client CODE is NULL");
//            return null;
//        }
//        return commandeClientRepository.findCommandeClientByCode(code)
//                .map(CommandeClientDto::fromEntity)
//                .orElseThrow(() -> new EntityNotFoundException(
//                        "Aucune commande client n'a ete trouve avec le CODE " + code,
//                        ErrorCodes.COMMANDE_CLIENT_NOT_FOUND
//                ));
//    }
//
//
//    @Override
//    public List<CommandeClientDto> findAll() {
//        return commandeClientRepository.findAll().stream()
//                .map(CommandeClientDto::fromEntity)
//                .collect(Collectors.toList());
//    }
//
//    @Override
//    public void delete(Integer id) {
//        if (id == null) {
//            log.error("Commande client ID is NULL");
//            return;
//        }
//        List<LigneCommandeClient> ligneCommandeClients = ligneCommandeClientRepository.findAllByCommandeClientId(id);
//        if (!ligneCommandeClients.isEmpty()) {
//            throw new InvalidOperationException("Impossible de supprimer une commande client deja utilisee",
//                    ErrorCodes.COMMANDE_CLIENT_ALREADY_IN_USE);
//        }
//        commandeClientRepository.deleteById(id);
//    }
//
//    @Override
//    public List<LigneCommandeClientDto> findAllLignesCommandesClientByCommandeClientId(Integer idCommandeClient) {
//        return ligneCommandeClientRepository.findAllByCommandeClientId(idCommandeClient).stream()
//                .map(LigneCommandeClientDto::fromEntity)
//                .collect(Collectors.toList());
//    }
//
//    @Override
//    public CommandeClientDto updateEtatCommande(Integer idCommandeClient, EEtatCommande etatCommande) {
//        checkIdCommande(idCommandeClient);
//        if (!StringUtils.hasLength(String.valueOf(etatCommande))) {
//            log.error("L'etat de la commande client is NULL");
//            throw new InvalidOperationException("Impossible de modifier l'etat de la commande avec un etat null",
//                    ErrorCodes.COMMANDE_CLIENT_NON_MODIFIABLE);
//        }
//        CommandeClientDto commandeClient = checkEtatCommande(idCommandeClient);
//        commandeClient.setEtatCommande(etatCommande);
//
//        CommandeClient savedCmdClt = commandeClientRepository.save(CommandeClientDto.toEntity(commandeClient));
//        if (commandeClient.isCommandeLivree()) {
//            updateMvtStk(idCommandeClient);
//        }
//
//        return CommandeClientDto.fromEntity(savedCmdClt);
//    }
//
//    @Override
//    public CommandeClientDto updateQuantiteCommande(Integer idCommandeClient, Integer idLigneCommande, BigDecimal quantite) {
//        checkIdCommande(idCommandeClient);
//        checkIdLigneCommande(idLigneCommande);
//
//        if (quantite == null || quantite.compareTo(BigDecimal.ZERO) == 0) {
//            log.error("L'ID de la ligne commande is NULL");
//            throw new InvalidOperationException(
//                    "Impossible de modifier l'etat de la commande avec une quantite null ou ZERO",
//                    ErrorCodes.COMMANDE_CLIENT_NON_MODIFIABLE);
//        }
//
//        CommandeClientDto commandeClient = checkEtatCommande(idCommandeClient);
//        Optional<LigneCommandeClient> ligneCommandeClientOptional = findLigneCommandeClient(idLigneCommande);
//
//        LigneCommandeClient ligneCommandeClient = ligneCommandeClientOptional.get();
//        ligneCommandeClient.setQuantite(quantite);
//        ligneCommandeClientRepository.save(ligneCommandeClient);
//
//        return commandeClient;
//    }
//
//    @Override
//    public CommandeClientDto updateClient(Integer idCommandeClient, Integer idClient) {
//        checkIdCommande(idCommandeClient);
//        if (idClient == null) {
//            log.error("L'ID du client is NULL");
//            throw new InvalidOperationException("Impossible de modifier l'etat de la commande avec un ID client null",
//                    ErrorCodes.COMMANDE_CLIENT_NON_MODIFIABLE);
//        }
//        CommandeClientDto commandeClient = checkEtatCommande(idCommandeClient);
//        Optional<Client> clientOptional = clientRepository.findById(idClient);
//        if (clientOptional.isEmpty()) {
//            throw new EntityNotFoundException(
//                    "Aucun client n'a ete trouve avec l'ID " + idClient, ErrorCodes.CLIENT_NOT_FOUND);
//        }
//        commandeClient.setClient(ClientDto.fromEntity(clientOptional.get()));
//
//        return CommandeClientDto.fromEntity(
//                commandeClientRepository.save(CommandeClientDto.toEntity(commandeClient))
//        );
//    }
//
//    @Override
//    public CommandeClientDto updateArticle(Integer idCommandeClient, Integer idLigneCommande, Integer idArticle) {
//        checkIdCommande(idCommandeClient);            //refactoring du code
//        checkIdLigneCommande(idLigneCommande);  //refactoring du code
//        checkIdArticle(idArticle, "nouvel");  //refactoring du code
//
//        CommandeClientDto commandeClient = checkEtatCommande(idCommandeClient);
//
//        Optional<LigneCommandeClient> ligneCommandeClient = findLigneCommandeClient(idLigneCommande);
//
//        Optional<Article> articleOptional = articleRepository.findById(idArticle);
//        if (articleOptional.isEmpty()) {
//            throw new EntityNotFoundException(
//                    "Aucune article n'a ete trouve avec l'ID " + idArticle,
//                    ErrorCodes.ARTICLE_NOT_FOUND);
//        }
//
//        List<String> errors = ArticleValidator.validate(ArticleDto.fromEntity(articleOptional.get()));
//        if (!errors.isEmpty()) {
//            throw new InvalidEntityException("Article invalid",
//                    ErrorCodes.ARTICLE_NOT_VALID, errors);
//        }
//
//        LigneCommandeClient ligneCommandeClientToSaved = ligneCommandeClient.get();
//        ligneCommandeClientToSaved.setArticle(articleOptional.get());
//        ligneCommandeClientRepository.save(ligneCommandeClientToSaved);
//
//        return commandeClient;
//    }
//
//    @Override
//    public CommandeClientDto deleteArticle(Integer idCommandeClient, Integer idLigneCommande) {
//        checkIdCommande(idCommandeClient);
//        checkIdLigneCommande(idLigneCommande);
//
//        CommandeClientDto commandeClient = checkEtatCommande(idCommandeClient);
//        // Just to check the LigneCommandeClient and inform the client in case it is absent
//        findLigneCommandeClient(idLigneCommande);
//        ligneCommandeClientRepository.deleteById(idLigneCommande);
//
//        return commandeClient;
//    }
//
//    private CommandeClientDto checkEtatCommande(Integer idCommandeClient) {
//        CommandeClientDto commandeClient = findById(idCommandeClient);
//        if (commandeClient.isCommandeLivree()) {
//            throw new InvalidOperationException("Impossible de modifier la commande lorsqu'elle est livree",
//                    ErrorCodes.COMMANDE_CLIENT_NON_MODIFIABLE);
//        }
//        return commandeClient;
//    }
//
//    private Optional<LigneCommandeClient> findLigneCommandeClient(Integer idLigneCommande) {
//        Optional<LigneCommandeClient> ligneCommandeClientOptional = ligneCommandeClientRepository
//                .findById(idLigneCommande);
//        if (ligneCommandeClientOptional.isEmpty()) {
//            throw new EntityNotFoundException(
//                    "Aucune ligne commande client n'a ete trouve avec l'ID " + idLigneCommande,
//                    ErrorCodes.COMMANDE_CLIENT_NOT_FOUND);
//        }
//        return ligneCommandeClientOptional;
//    }
//
//    private void checkIdCommande(Integer idCommandeClient) {
//        if (idCommandeClient == null) {
//            log.error("Commande client ID is NULL");
//            throw new InvalidOperationException(
//                    "Impossible de modifier l'etat de la commande avec un ID null",
//                    ErrorCodes.COMMANDE_CLIENT_NON_MODIFIABLE);
//        }
//    }
//
//    private void checkIdLigneCommande(Integer idLigneCommande) {
//        if (idLigneCommande == null) {
//            log.error("L'ID de la ligne commande is NULL");
//            throw new InvalidOperationException("" +
//                    "Impossible de modifier l'etat de la commande avec une ligne de commande null",
//                    ErrorCodes.COMMANDE_CLIENT_NON_MODIFIABLE);
//        }
//    }
//
//    private void checkIdArticle(Integer idArticle, String msg) {
//        if (idArticle == null) {
//            log.error("L'ID de " + msg + " is NULL");
//            throw new InvalidOperationException("" +
//                    "Impossible de modifier l'etat de la commande avec un " + msg + " ID article null",
//                    ErrorCodes.COMMANDE_CLIENT_NON_MODIFIABLE);
//        }
//    }
//
//    private void updateMvtStk(Integer idCommandeClient) {
//        List<LigneCommandeClient> ligneCommandeClients = ligneCommandeClientRepository
//                .findAllByCommandeClientId(idCommandeClient);
//        ligneCommandeClients.forEach(lig -> {
//            effectuerSortie(lig);
//        });
//    }
//
//    private void effectuerSortie(LigneCommandeClient lig) {
//        MvtStockDto mvtStockDto = MvtStockDto.builder()
//                .article(ArticleDto.fromEntity(lig.getArticle()))
//                .dateMvt(Instant.now())
//                .typeMvt(ETypeMvtStock.SORTIE)
//                .sourceMvt(ESourceMvtStock.COMMANDE_CLIENT)
//                .quantite(lig.getQuantite())
//                .idEntreprise(lig.getIdEntreprise())
//                .build();
//        mvtStockService.sortieStock(mvtStockDto);
//    }
//}


