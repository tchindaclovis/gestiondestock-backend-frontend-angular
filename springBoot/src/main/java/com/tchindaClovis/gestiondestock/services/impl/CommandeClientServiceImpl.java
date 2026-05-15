package com.tchindaClovis.gestiondestock.services.impl;

import com.tchindaClovis.gestiondestock.dto.*;
import com.tchindaClovis.gestiondestock.exception.EntityNotFoundException;
import com.tchindaClovis.gestiondestock.exception.ErrorCodes;
import com.tchindaClovis.gestiondestock.exception.InvalidEntityException;
import com.tchindaClovis.gestiondestock.exception.InvalidOperationException;
import com.tchindaClovis.gestiondestock.model.*;
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
import java.util.*;
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
    @Transactional
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

    // 2. Vérification si modification autorisée (si livrée, on bloque) || dto.isCommandeConfirmee()
    if (dto.getId() != null && (dto.isCommandeConfirmee())) {
        throw new InvalidOperationException(
                "Impossible de modifier la commande lorsqu'elle est confirmé ", ErrorCodes.COMMANDE_CLIENT_NON_MODIFIABLE);
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
        log.warn("");
        throw new InvalidEntityException("Article n'existe pas dans la BDD", ErrorCodes.ARTICLE_NOT_FOUND, articleErrors);
    }


    // On récupère l'ancienne commandeClient si c'est une mise à jour
    Map<Integer, BigDecimal> anciennesLignesMap = new HashMap<>();
    CommandeClient commandeClientToSave;

    if (dto.getId() != null) {
        commandeClientToSave = commandeClientRepository.findByIdWithLignes(dto.getId())
                .orElseThrow(() -> new EntityNotFoundException("CommandeClient introuvable", ErrorCodes.COMMANDE_CLIENT_NOT_FOUND));

        // On remplit la Map : ID Article -> Quantité
        if (commandeClientToSave.getLigneCommandeClients() != null) {
            commandeClientToSave.getLigneCommandeClients().forEach(lig ->
                    anciennesLignesMap.put(lig.getArticle().getId(), lig.getQuantite())
            );
        }

        // On met à jour les infos générales
        commandeClientToSave.setCode(dto.getCode());
        commandeClientToSave.setDateCommande(dto.getDateCommande());
        commandeClientToSave.setIdEntreprise(dto.getIdEntreprise());
        commandeClientToSave.setEtatCommande(dto.getEtatCommande());
        commandeClientToSave.setClient(ClientDto.toEntity(dto.getClient()));

        // Au lieu de supprimer manuellement avec le repository, on vide la collection Java pour éviter les conflits d'état
        // orphanRemoval = true dans l'entité s'occupera de la suppression en base
        // On nettoie les anciennes lignes pour Hibernate
        commandeClientToSave.getLigneCommandeClients().clear();
        commandeClientRepository.saveAndFlush(commandeClientToSave);
    } else {
        commandeClientToSave = CommandeClientDto.toEntity(dto);
    }

    // Préparer la liste des nouvelles lignes pour la sauvegarde
    if (dto.getLigneCommandeClients() != null) {
        if (commandeClientToSave.getLigneCommandeClients() == null) {
            commandeClientToSave.setLigneCommandeClients(new ArrayList<>());
        }

        for (LigneCommandeClientDto ligDto : dto.getLigneCommandeClients()) {
            LigneCommandeClient lig = LigneCommandeClientDto.toEntity(ligDto);
            lig.setId(null);
            lig.setCommandeClient(commandeClientToSave);
            lig.setIdEntreprise(dto.getIdEntreprise());
            commandeClientToSave.getLigneCommandeClients().add(lig);

        }
    }

        // GÉRER LES SUPPRESSIONS (Articles restants dans la Map)
        anciennesLignesMap.forEach((idArt, qteInitiale) -> {
            // On simule une ligne pour la restitution du stock
            LigneCommandeClient ligneSupprimee = new LigneCommandeClient();
            ligneSupprimee.setArticle(articleRepository.findById(idArt).orElse(null));
            ligneSupprimee.setQuantite(qteInitiale);
            ligneSupprimee.setIdEntreprise(dto.getIdEntreprise());

        });

        CommandeClient savedCommandeClient = commandeClientRepository.saveAndFlush(commandeClientToSave);
        return CommandeClientDto.fromEntity(savedCommandeClient);
    }



    @Override
    @Transactional // CRITIQUE: Assure la gestion de la session Hibernate et du rollback en cas d'erreur
    public CommandeClientDto saveImpact(CommandeClientDto dto) {

        log.info("Saving command with {} lines",
                dto.getLigneCommandeClients() != null ? dto.getLigneCommandeClients().size() : 0);

        // 1. Validation de la structure du DTO
        List<String> errors = CommandeClientValidator.validate(dto);
        if (!errors.isEmpty()) {
            log.error("Commande client n'est pas valide");
            throw new InvalidEntityException(
                    "La commande client n'est pas valide", ErrorCodes.COMMANDE_CLIENT_NOT_VALID, errors);
        }

        // 3. Vérification de l'existence du client
        Optional<Client> client = clientRepository.findById(dto.getClient().getId());
        if (client.isEmpty()) {
            throw new EntityNotFoundException(
                    "Aucun client avec l'ID " + dto.getClient().getId() + " n'a ete trouve", ErrorCodes.CLIENT_NOT_FOUND);
        }

        // 4. Vérification de l'existence des articles
        checkArticlesExistence(dto);

        // --- LOGIQUE DE PRÉPARATION DE L'ENTITÉ ---
        Map<Integer, BigDecimal> anciennesLignesMap = new HashMap<>();
        CommandeClient commandeClientToSave;

//        // Récupération de l'état actuel UNIQUEMENT si l'ID est valide (> 0)
//        CommandeClientDto commandeExistanteDto = null;
//        if (idCommandeClient != null && idCommandeClient > 0) {
//            commandeExistanteDto = checkEtatCommande(idCommandeClient);
//        }

        if (dto.getId() != null) {
            // MODE MODIFICATION
            commandeClientToSave = commandeClientRepository.findByIdWithLignes(dto.getId())
                    .orElseThrow(() -> new EntityNotFoundException("CommandeClient introuvable", ErrorCodes.COMMANDE_CLIENT_NOT_FOUND));

            // On stocke les anciennes quantités pour la logique de stock
            if (commandeClientToSave.getLigneCommandeClients() != null) {
                commandeClientToSave.getLigneCommandeClients().forEach(lig ->
                        anciennesLignesMap.put(lig.getArticle().getId(), lig.getQuantite())
                );
            }

            // Mise à jour des infos générales
            commandeClientToSave.setCode(dto.getCode());
            commandeClientToSave.setDateCommande(dto.getDateCommande());
            commandeClientToSave.setIdEntreprise(dto.getIdEntreprise());
            commandeClientToSave.setClient(ClientDto.toEntity(dto.getClient()));

            // On nettoie les anciennes lignes (orphanRemoval fera le reste)
            commandeClientToSave.getLigneCommandeClients().clear();

            commandeClientRepository.saveAndFlush(commandeClientToSave);
        } else {
            // MODE CRÉATION
            commandeClientToSave = CommandeClientDto.toEntity(dto);
            commandeClientToSave.setId(null); // Sécurité
        }

//        // FIX : On applique l'état sur l'entité qu'on va sauvegarder, pas sur un DTO potentiellement null
//        if (etatCommande != null) {
//            commandeClientToSave.getLigneCommandeClients().clear();
//            commandeClientToSave.setEtatCommande(etatCommande);
//        } else if (commandeClientToSave.getEtatCommande() == null) {
//            commandeClientToSave.setEtatCommande(EEtatCommande.PRO_FORMAT);
//        }

        // --- GESTION DES LIGNES ET DU STOCK ---
        if (dto.getLigneCommandeClients() != null) {
            // Au lieu de tout clearer systématiquement au début,
            // on peut aussi vider ici juste avant de reconstruire
            commandeClientToSave.getLigneCommandeClients().clear();

            if (commandeClientToSave.getLigneCommandeClients() == null) {
                commandeClientToSave.setLigneCommandeClients(new ArrayList<>());
            }

            for (LigneCommandeClientDto ligDto : dto.getLigneCommandeClients()) {
                LigneCommandeClient lig = LigneCommandeClientDto.toEntity(ligDto);
                lig.setId(null);
                lig.setCommandeClient(commandeClientToSave);
                lig.setIdEntreprise(dto.getIdEntreprise());
                commandeClientToSave.getLigneCommandeClients().add(lig);

                // On ne déclenche les mouvements de stock que si la commande est (ou devient) CONFIRMEE
//                if (EEtatCommande.CONFIRMEE.equals(commandeClientToSave.getEtatCommande())) {}

                    // LOGIQUE DIFFÉRENTIELLE DES STOCKS
                    Integer articleId = ligDto.getArticle().getId();
                    BigDecimal nouvelleQte = ligDto.getQuantite();

                    if (anciennesLignesMap.containsKey(articleId)) {
                        BigDecimal ancienneQte = anciennesLignesMap.get(articleId);
                        int comparaison = nouvelleQte.compareTo(ancienneQte);

                        if (comparaison > 0) {
                            modifierStockIndividuel(lig, nouvelleQte.subtract(ancienneQte), ETypeMvtStock.SORTIE);
                        } else if (comparaison < 0) {
                            modifierStockIndividuel(lig, ancienneQte.subtract(nouvelleQte), ETypeMvtStock.CORRECTION_POS_VENTE_RED);
                        }
                        anciennesLignesMap.remove(articleId);
                    } else {
                        // Nouvelle ligne sur une commande déjà confirmée ou en cours de confirmation
                        modifierStockIndividuel(lig, nouvelleQte, ETypeMvtStock.SORTIE);
                    }

            }
        }

        // GÉRER LES SUPPRESSIONS (Restituer le stock pour les articles retirés)
//        if (EEtatCommande.CONFIRMEE.equals(commandeClientToSave.getEtatCommande())) {}
            anciennesLignesMap.forEach((idArt, qteInitiale) -> {
                LigneCommandeClient ligneSupprimee = new LigneCommandeClient();
                ligneSupprimee.setArticle(articleRepository.findById(idArt).orElse(null));
                ligneSupprimee.setQuantite(qteInitiale);
                ligneSupprimee.setIdEntreprise(dto.getIdEntreprise());

                updateMvtStockAnnulation(ligneSupprimee);
            });

        CommandeClient savedCommandeClient = commandeClientRepository.saveAndFlush(commandeClientToSave);
        return CommandeClientDto.fromEntity(savedCommandeClient);
    }

    // Méthode utilitaire pour nettoyer le code principal
    private void checkArticlesExistence(CommandeClientDto dto) {
        List<String> articleErrors = new ArrayList<>();
        if (dto.getLigneCommandeClients() != null) {
            dto.getLigneCommandeClients().forEach(lig -> {
                if (lig.getArticle() == null || articleRepository.findById(lig.getArticle().getId()).isEmpty()) {
                    articleErrors.add("Article introuvable pour une des lignes");
                }
            });
        }
        if (!articleErrors.isEmpty()) {
            throw new InvalidEntityException("Articles non trouvés", ErrorCodes.ARTICLE_NOT_FOUND, articleErrors);
        }
    }

    /**
     * Méthode utilitaire pour générer un mouvement de stock spécifique (différentiel)
     */
    private void modifierStockIndividuel(LigneCommandeClient lig, BigDecimal qte, ETypeMvtStock type) {
        if (qte.compareTo(BigDecimal.ZERO) <= 0) return;

        MvtStockDto mvt = MvtStockDto.builder()
                .article(ArticleDto.fromEntity(lig.getArticle()))
                .dateMvt(Instant.now())
                .typeMvt(type)
                .sourceMvt(ESourceMvtStock.COMMANDE_CLIENT)
                .quantite(qte)
                .codeSource(lig.getCommandeClient().getCode()) // ON PASSE LE CODE ICI
                .idEntreprise(lig.getIdEntreprise())
                .build();

        if (type == ETypeMvtStock.SORTIE) {
            mvtStockService.sortieStock(mvt);
        } else  if (type == ETypeMvtStock.CORRECTION_POS_VENTE_RED){
            mvtStockService.correctionStockPosVenteRed2(mvt);
        }else{
            mvtStockService.sortieStock(mvt);
        }
    }

    /**
     * Méthode pour réintégrer les stocks lors de l'annulation/modification/suppression d'une ligne
     */
    private void updateMvtStockAnnulation(LigneCommandeClient ligne) {

        // Vérification de sécurité pour éviter l'erreur (Error 500) NPE(NullPointerException)
        if (ligne.getCommandeClient() == null) {
            log.warn("Impossible de mettre à jour le mouvement de stock : CommandeClient est nulle pour la ligne ID {}", ligne.getId());
            return;
        }

        MvtStockDto mvtStockDto = MvtStockDto.builder()
                .article(ArticleDto.fromEntity(ligne.getArticle()))
                .dateMvt(Instant.now())
                .typeMvt(ETypeMvtStock.ENTREE) // Ou CORRECTION_POS selon votre enum
                .sourceMvt(ESourceMvtStock.COMMANDE_CLIENT)
                .quantite(ligne.getQuantite()) // On remet la quantité initiale en stock
                .codeSource(ligne.getCommandeClient().getCode()) // ON PASSE LE CODE ICI
                .idEntreprise(ligne.getIdEntreprise())
                .build();
        mvtStockService.entreeStock(mvtStockDto);
    }


    @Override
    @Transactional
    public CommandeClientDto updateEtatCommande(Integer idCommandeClient, EEtatCommande etatCommande, Instant dateConfirmation) {
        checkIdCommande(idCommandeClient);
        checkEtatCommande(idCommandeClient);

        CommandeClient commandeEntity = commandeClientRepository.findByIdWithLignes(idCommandeClient)
                .orElseThrow(() -> new EntityNotFoundException("Commande introuvable", ErrorCodes.COMMANDE_CLIENT_NOT_FOUND));

        log.info("Entité récupérée avec {} lignes", commandeEntity.getLigneCommandeClients().size());

        CommandeClientDto dto = CommandeClientDto.fromEntity(commandeEntity);

        // TEST MANUEL : Si le mapper a échoué, on force le transfert des lignes
        if (dto.getLigneCommandeClients() == null || dto.getLigneCommandeClients().isEmpty()) {
            log.warn("Le mapper fromEntity n'a pas transféré les lignes ! Transfert manuel...");
            dto.setLigneCommandeClients(
                    commandeEntity.getLigneCommandeClients().stream()
                            .map(LigneCommandeClientDto::fromEntity)
                            .collect(Collectors.toList())
            );
        }
        log.info("DTO envoyé au save avec {} lignes", dto.getLigneCommandeClients().size());

//        checkEtatCommande(idCommandeClient);
        log.info("L'état de la commande est {}", dto.getEtatCommande());
        dto.setEtatCommande(etatCommande);
        dto.setDateConfirmation(dateConfirmation);

        CommandeClient savedCmdClt = commandeClientRepository.save(CommandeClientDto.toEntity(dto));

        return this.saveImpact(dto);
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
    public List<LigneCommandeClientDto> findAllLignesCommandesClientByCommandeClientId(Integer idCommandeClient) {
        return ligneCommandeClientRepository.findAllByCommandeClientId(idCommandeClient).stream()
                .map(LigneCommandeClientDto::fromEntity)
                .collect(Collectors.toList());
    }


    @Override
    @Transactional // CRITIQUE : Assure que tout est validé en base à la fin
    public void delete(Integer id) {
        checkEtatCommande(id);
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
    public CommandeClientDto deleteArticle(Integer idCommandeClient, Integer idLigneCommande) {
        checkIdCommande(idCommandeClient);
        checkIdLigneCommande(idLigneCommande);

        CommandeClientDto commandeClient = checkEtatCommande(idCommandeClient);
        // Just to check the LigneCommandeClient and inform the client in case it is absent
        findLigneCommandeClient(idLigneCommande);
        ligneCommandeClientRepository.deleteById(idLigneCommande);

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

    private Optional<LigneCommandeClient> findLigneCommandeClient(Integer idLigneCommande) {
        Optional<LigneCommandeClient> ligneCommandeClientOptional = ligneCommandeClientRepository.findById(idLigneCommande);
        if (ligneCommandeClientOptional.isEmpty()) {
            throw new EntityNotFoundException(
                    "Aucune ligne commande client n'a ete trouve avec l'ID " + idLigneCommande, ErrorCodes.COMMANDE_CLIENT_NOT_FOUND);
        }
        return ligneCommandeClientOptional;
    }


    private CommandeClientDto checkEtatCommande(Integer idCommandeClient) {
        if (idCommandeClient == null || idCommandeClient == 0) {
            return null; // Pas de commande, donc pas d'état à vérifier
        }

        CommandeClientDto commandeClient = findById(idCommandeClient);
        if (commandeClient.isCommandeConfirmee()) {
            throw new InvalidOperationException("Impossible de modifier ou supprimer la commande lorsqu'elle est confirmé",
                    ErrorCodes.COMMANDE_CLIENT_NON_MODIFIABLE);
        }
        return commandeClient;
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

}




//    @Override
//    @Transactional
//    public CommandeClientDto saveImpact(CommandeClientDto dto, Integer idCommandeClient, EEtatCommande etatCommande) {
//
//        log.info("Saving command with {} lines",
//                dto.getLigneCommandeClients() != null ? dto.getLigneCommandeClients().size() : 0);
//
//        // 1. Validation de la structure du DTO
//        List<String> errors = CommandeClientValidator.validate(dto);
//        if (!errors.isEmpty()) {
//            log.error("Commande client n'est pas valide");
//            throw new InvalidEntityException(
//                    "La commande client n'est pas valide", ErrorCodes.COMMANDE_CLIENT_NOT_VALID, errors);
//        }
//
//        // 2. Vérification si modification autorisée
//        if (dto.getId() != null && dto.isCommandeLivree()) {
//            throw new InvalidOperationException(
//                    "Impossible de modifier la commande lorsqu'elle est livree", ErrorCodes.COMMANDE_CLIENT_NON_MODIFIABLE);
//        }
//
//        // 3. Vérification du client
//        Optional<Client> client = clientRepository.findById(dto.getClient().getId());
//        if (client.isEmpty()) {
//            throw new EntityNotFoundException(
//                    "Aucun client avec l'ID " + dto.getClient().getId() + " n'a ete trouve", ErrorCodes.CLIENT_NOT_FOUND);
//        }
//
//        // 4. Vérification de l'existence des articles (Méthode utilitaire)
//        checkArticlesExistence(dto);
//
//        // --- LOGIQUE DE PRÉPARATION DE L'ENTITÉ ---
//        Map<Integer, BigDecimal> anciennesLignesMap = new HashMap<>();
//        CommandeClient commandeClientToSave;
//
//        // Récupération sécurisée du DTO existant (pour check d'état)
//        if (idCommandeClient != null && idCommandeClient > 0) {
//            checkEtatCommande(idCommandeClient);
//        }
//
//        if (dto.getId() != null) {
//            // MODE MODIFICATION
//            commandeClientToSave = commandeClientRepository.findByIdWithLignes(dto.getId())
//                    .orElseThrow(() -> new EntityNotFoundException("CommandeClient introuvable", ErrorCodes.COMMANDE_CLIENT_NOT_FOUND));
//
//            // SÉCURITÉ : Initialisation de la liste si Hibernate renvoie null
//            if (commandeClientToSave.getLigneCommandeClients() == null) {
//                commandeClientToSave.setLigneCommandeClients(new ArrayList<>());
//            } else {
//                // Mapping des anciennes quantités pour le calcul différentiel du stock
//                commandeClientToSave.getLigneCommandeClients().forEach(lig ->
//                        anciennesLignesMap.put(lig.getArticle().getId(), lig.getQuantite())
//                );
//                // On vide pour reconstruire proprement avec les nouvelles lignes du DTO
//                commandeClientToSave.getLigneCommandeClients().clear();
//            }
//
//            // Mise à jour des champs généraux
//            commandeClientToSave.setCode(dto.getCode());
//            commandeClientToSave.setDateCommande(dto.getDateCommande());
//            commandeClientToSave.setIdEntreprise(dto.getIdEntreprise());
//            commandeClientToSave.setClient(ClientDto.toEntity(dto.getClient()));
//
//        } else {
//            // MODE CRÉATION
//            commandeClientToSave = CommandeClientDto.toEntity(dto);
//            commandeClientToSave.setId(null);
//            // SÉCURITÉ : Initialisation de la liste pour éviter le NPE plus bas
//            if (commandeClientToSave.getLigneCommandeClients() == null) {
//                commandeClientToSave.setLigneCommandeClients(new ArrayList<>());
//            }
//        }
//
//        // --- GESTION DE L'ÉTAT ---
//        // On applique l'état passé en paramètre (ex: CONFIRMEE via le bouton valider)
//        if (etatCommande != null) {
//            commandeClientToSave.setEtatCommande(etatCommande);
//        } else if (commandeClientToSave.getEtatCommande() == null) {
//            commandeClientToSave.setEtatCommande(EEtatCommande.PRO_FORMAT);
//        }
//
//        // --- GESTION DES LIGNES ET DU STOCK ---
//        if (dto.getLigneCommandeClients() != null) {
//            // SÉCURITÉ : On s'assure encore que la liste est prête à recevoir les objets
//            if (commandeClientToSave.getLigneCommandeClients() == null) {
//                commandeClientToSave.setLigneCommandeClients(new ArrayList<>());
//            }
//
//            for (LigneCommandeClientDto ligDto : dto.getLigneCommandeClients()) {
//                LigneCommandeClient lig = LigneCommandeClientDto.toEntity(ligDto);
//                lig.setId(null); // On force la création de nouvelles lignes
//                lig.setCommandeClient(commandeClientToSave);
//                lig.setIdEntreprise(dto.getIdEntreprise());
//                commandeClientToSave.getLigneCommandeClients().add(lig);
//
//                // CALCUL DU STOCK : Uniquement si l'état actuel est CONFIRMEE
//                if (EEtatCommande.CONFIRMEE.equals(commandeClientToSave.getEtatCommande())) {
//                    Integer articleId = ligDto.getArticle().getId();
//                    BigDecimal nouvelleQte = ligDto.getQuantite();
//
//                    if (anciennesLignesMap.containsKey(articleId)) {
//                        // Modification de ligne existante
//                        BigDecimal ancienneQte = anciennesLignesMap.get(articleId);
//                        int comparaison = nouvelleQte.compareTo(ancienneQte);
//
//                        if (comparaison > 0) {
//                            modifierStockIndividuel(lig, nouvelleQte.subtract(ancienneQte), ETypeMvtStock.SORTIE);
//                        } else if (comparaison < 0) {
//                            modifierStockIndividuel(lig, ancienneQte.subtract(nouvelleQte), ETypeMvtStock.CORRECTION_POS_VENTE_RED);
//                        }
//                        anciennesLignesMap.remove(articleId); // Retiré pour identifier les suppressions
//                    } else {
//                        // Nouvel article ajouté
//                        modifierStockIndividuel(lig, nouvelleQte, ETypeMvtStock.SORTIE);
//                    }
//                }
//            }
//        }
//
//        // GÉRER LES SUPPRESSIONS (Articles présents avant mais absents du nouveau DTO)
//        if (EEtatCommande.CONFIRMEE.equals(commandeClientToSave.getEtatCommande())) {
//            anciennesLignesMap.forEach((idArt, qteInitiale) -> {
//                LigneCommandeClient ligneSupprimee = new LigneCommandeClient();
//                ligneSupprimee.setArticle(articleRepository.findById(idArt).orElse(null));
//                ligneSupprimee.setQuantite(qteInitiale);
//                ligneSupprimee.setIdEntreprise(dto.getIdEntreprise());
//                updateMvtStockAnnulation(ligneSupprimee);
//            });
//        }
//
//        // Sauvegarde finale
//        CommandeClient savedCommandeClient = commandeClientRepository.saveAndFlush(commandeClientToSave);
//        return CommandeClientDto.fromEntity(savedCommandeClient);
//    }
//
//
//    /**
//     * Vérifie l'existence de tous les articles mentionnés dans les lignes de la commande.
//     * Si un article est introuvable ou si les données sont incomplètes, une exception est levée.
//     *
//     * @param dto Le DTO de la commande client contenant les lignes à vérifier
//     */
//    private void checkArticlesExistence(CommandeClientDto dto) {
//        List<String> articleErrors = new ArrayList<>();
//
//        if (dto.getLigneCommandeClients() != null && !dto.getLigneCommandeClients().isEmpty()) {
//            dto.getLigneCommandeClients().forEach(lig -> {
//                if (lig.getArticle() == null || lig.getArticle().getId() == null) {
//                    articleErrors.add("Impossible d'enregistrer une commande avec un article NULL ou sans ID");
//                } else {
//                    // Vérification en base de données
//                    Optional<Article> article = articleRepository.findById(lig.getArticle().getId());
//                    if (article.isEmpty()) {
//                        articleErrors.add("L'article avec l'ID " + lig.getArticle().getId() + " n'existe pas dans la BDD");
//                    }
//                }
//            });
//        } else {
//            // Optionnel : on peut bloquer si une commande n'a aucune ligne
//            articleErrors.add("La commande doit contenir au moins une ligne (article)");
//        }
//
//        // Si des erreurs ont été collectées, on lève une exception globale
//        if (!articleErrors.isEmpty()) {
//            log.warn("Certains articles de la commande sont introuvables ou invalides : {}", articleErrors);
//            throw new InvalidEntityException(
//                    "Certains articles n'existent pas dans la base de données",
//                    ErrorCodes.ARTICLE_NOT_FOUND,
//                    articleErrors
//            );
//        }
//    }


//    @Override
//    @Transactional // CRITIQUE: Assure la gestion de la session Hibernate et du rollback en cas d'erreur
//    public CommandeClientDto save(CommandeClientDto dto, Integer idCommandeClient, EEtatCommande etatCommande) {
//
//        log.info("Saving command with {} lines",
//                dto.getLigneCommandeClients() != null ? dto.getLigneCommandeClients().size() : 0);
//
//        // 1. Validation de la structure du DTO
//        List<String> errors = CommandeClientValidator.validate(dto);
//        if (!errors.isEmpty()) {
//            log.error("Commande client n'est pas valide");
//            throw new InvalidEntityException(
//                    "La commande client n'est pas valide", ErrorCodes.COMMANDE_CLIENT_NOT_VALID, errors);
//        }
//
//        // 2. Vérification si modification autorisée
//        if (dto.getId() != null && (dto.isCommandeLivree())) {
//            throw new InvalidOperationException(
//                    "Impossible de modifier la commande lorsqu'elle est livree", ErrorCodes.COMMANDE_CLIENT_NON_MODIFIABLE);
//        }
//
//        // 3. Vérification de l'existence du client
//        Optional<Client> client = clientRepository.findById(dto.getClient().getId());
//        if (client.isEmpty()) {
//            throw new EntityNotFoundException(
//                    "Aucun client avec l'ID " + dto.getClient().getId() + " n'a ete trouve", ErrorCodes.CLIENT_NOT_FOUND);
//        }
//
//        // 4. Vérification de l'existence des articles
//        checkArticlesExistence(dto);
//
//        // --- LOGIQUE DE PRÉPARATION DE L'ENTITÉ ---
//        Map<Integer, BigDecimal> anciennesLignesMap = new HashMap<>();
//        CommandeClient commandeClientToSave;
//
//        // Récupération de l'état actuel UNIQUEMENT si l'ID est valide (> 0)
//        CommandeClientDto commandeExistanteDto = null;
//        if (idCommandeClient != null && idCommandeClient > 0) {
//            commandeExistanteDto = checkEtatCommande(idCommandeClient);
//        }
//
//        if (dto.getId() != null) {
//            // MODE MODIFICATION
//            commandeClientToSave = commandeClientRepository.findByIdWithLignes(dto.getId())
//                    .orElseThrow(() -> new EntityNotFoundException("CommandeClient introuvable", ErrorCodes.COMMANDE_CLIENT_NOT_FOUND));
//
//            // On stocke les anciennes quantités pour la logique de stock
//            if (commandeClientToSave.getLigneCommandeClients() != null) {
//                commandeClientToSave.getLigneCommandeClients().forEach(lig ->
//                        anciennesLignesMap.put(lig.getArticle().getId(), lig.getQuantite())
//                );
//            }
//
//            // Mise à jour des infos générales
//            commandeClientToSave.setCode(dto.getCode());
//            commandeClientToSave.setDateCommande(dto.getDateCommande());
//            commandeClientToSave.setIdEntreprise(dto.getIdEntreprise());
//            commandeClientToSave.setClient(ClientDto.toEntity(dto.getClient()));
//
//            // On nettoie les anciennes lignes (orphanRemoval fera le reste)
//            commandeClientToSave.getLigneCommandeClients().clear();
//
//            commandeClientRepository.saveAndFlush(commandeClientToSave);
//        } else {
//            // MODE CRÉATION
//            commandeClientToSave = CommandeClientDto.toEntity(dto);
//            commandeClientToSave.setId(null); // Sécurité
//        }
//
//        // FIX : On applique l'état sur l'entité qu'on va sauvegarder, pas sur un DTO potentiellement null
//        if (etatCommande != null) {
//            commandeClientToSave.getLigneCommandeClients().clear();
//            commandeClientToSave.setEtatCommande(etatCommande);
//        } else if (commandeClientToSave.getEtatCommande() == null) {
//            commandeClientToSave.setEtatCommande(EEtatCommande.PRO_FORMAT);
//        }
//
//        // --- GESTION DES LIGNES ET DU STOCK ---
//        if (dto.getLigneCommandeClients() != null) {
//            // Au lieu de tout clearer systématiquement au début,
//            // on peut aussi vider ici juste avant de reconstruire
//            commandeClientToSave.getLigneCommandeClients().clear();
//
//            if (commandeClientToSave.getLigneCommandeClients() == null) {
//                commandeClientToSave.setLigneCommandeClients(new ArrayList<>());
//            }
//
//            for (LigneCommandeClientDto ligDto : dto.getLigneCommandeClients()) {
//                LigneCommandeClient lig = LigneCommandeClientDto.toEntity(ligDto);
//                lig.setId(null);
//                lig.setCommandeClient(commandeClientToSave);
//                lig.setIdEntreprise(dto.getIdEntreprise());
//                commandeClientToSave.getLigneCommandeClients().add(lig);
//
//                // On ne déclenche les mouvements de stock que si la commande est (ou devient) CONFIRMEE
//                if (EEtatCommande.CONFIRMEE.equals(commandeClientToSave.getEtatCommande())) {
//
//                    Integer articleId = ligDto.getArticle().getId();
//                    BigDecimal nouvelleQte = ligDto.getQuantite();
//
//                    if (anciennesLignesMap.containsKey(articleId)) {
//                        BigDecimal ancienneQte = anciennesLignesMap.get(articleId);
//                        int comparaison = nouvelleQte.compareTo(ancienneQte);
//
//                        if (comparaison > 0) {
//                            modifierStockIndividuel(lig, nouvelleQte.subtract(ancienneQte), ETypeMvtStock.SORTIE);
//                        } else if (comparaison < 0) {
//                            modifierStockIndividuel(lig, ancienneQte.subtract(nouvelleQte), ETypeMvtStock.CORRECTION_POS_VENTE_RED);
//                        }
//                        anciennesLignesMap.remove(articleId);
//                    } else {
//                        // Nouvelle ligne sur une commande déjà confirmée ou en cours de confirmation
//                        modifierStockIndividuel(lig, nouvelleQte, ETypeMvtStock.SORTIE);
//                    }
//                }
//            }
//        }
//
//        // GÉRER LES SUPPRESSIONS (Restituer le stock pour les articles retirés)
//        if (EEtatCommande.CONFIRMEE.equals(commandeClientToSave.getEtatCommande())) {
//            anciennesLignesMap.forEach((idArt, qteInitiale) -> {
//                LigneCommandeClient ligneSupprimee = new LigneCommandeClient();
//                ligneSupprimee.setArticle(articleRepository.findById(idArt).orElse(null));
//                ligneSupprimee.setQuantite(qteInitiale);
//                ligneSupprimee.setIdEntreprise(dto.getIdEntreprise());
//                updateMvtStockAnnulation(ligneSupprimee);
//            });
//        }
//
//        CommandeClient savedCommandeClient = commandeClientRepository.saveAndFlush(commandeClientToSave);
//        return CommandeClientDto.fromEntity(savedCommandeClient);
//    }
//
//    // Méthode utilitaire pour nettoyer le code principal
//    private void checkArticlesExistence(CommandeClientDto dto) {
//        List<String> articleErrors = new ArrayList<>();
//        if (dto.getLigneCommandeClients() != null) {
//            dto.getLigneCommandeClients().forEach(lig -> {
//                if (lig.getArticle() == null || articleRepository.findById(lig.getArticle().getId()).isEmpty()) {
//                    articleErrors.add("Article introuvable pour une des lignes");
//                }
//            });
//        }
//        if (!articleErrors.isEmpty()) {
//            throw new InvalidEntityException("Articles non trouvés", ErrorCodes.ARTICLE_NOT_FOUND, articleErrors);
//        }
//    }




//    @Override
//    @Transactional
//    public CommandeClientDto updateEtatCommande(Integer idCommandeClient, EEtatCommande etatCommande) {
//        checkIdCommande(idCommandeClient);
//
//        // Utilisez findByIdWithLignes et non findById
//        CommandeClient commandeEntity = commandeClientRepository.findByIdWithLignes(idCommandeClient)
//                .orElseThrow(() -> new EntityNotFoundException("Commande introuvable", ErrorCodes.COMMANDE_CLIENT_NOT_FOUND));
//
//        log.info("Commande récupérée avec {} lignes",
//                commandeEntity.getLigneCommandeClients() != null ? commandeEntity.getLigneCommandeClients().size() : 0);
//
//        CommandeClientDto dto = CommandeClientDto.fromEntity(commandeEntity);
//        return this.save(dto, idCommandeClient, etatCommande);
//    }

//    @Override
//    @Transactional
//    public CommandeClientDto updateEtatCommande(Integer idCommandeClient, EEtatCommande etatCommande) {
//        // 1. Vérifications de base
//        checkIdCommande(idCommandeClient);
//        if (etatCommande == null) {
//            log.error("L'etat de la commande client est NULL");
//            throw new InvalidOperationException("Impossible de modifier l'etat de la commande avec un etat null",
//                    ErrorCodes.COMMANDE_CLIENT_NON_MODIFIABLE);
//        }
//
//        // 2. Récupérer la commande complète avec ses lignes depuis la BDD
//        // Il est CRITIQUE d'utiliser findByIdWithLignes pour ne pas perdre les articles
//        CommandeClient commandeEntity = commandeClientRepository.findByIdWithLignes(idCommandeClient)
//                .orElseThrow(() -> new EntityNotFoundException(
//                        "Aucune commande client n'a ete trouvee avec l'ID " + idCommandeClient,
//                        ErrorCodes.COMMANDE_CLIENT_NOT_FOUND));
//
//        // 3. Convertir l'entité complète en DTO
//        CommandeClientDto dto = CommandeClientDto.fromEntity(commandeEntity);
//
//        // 4. Appeler la méthode save() que nous avons corrigée précédemment.
//        // Pourquoi ? Parce que save() va :
//        //    a) Comparer l'ancien état (PRO_FORMAT) avec le nouveau (CONFIRMEE).
//        //    b) Parcourir les lignes pour générer les mouvements de stock (SORTIE).
//        //    c) Préserver l'intégrité des lignes de commande.
//        log.info("Mise à jour de l'etat de la commande ID {} vers {}", idCommandeClient, etatCommande);
//
//        return this.save(dto, idCommandeClient, etatCommande);
//    }




//    @Override
//    @Transactional
//    public CommandeClientDto updateEtatCommande(Integer idCommandeClient, EEtatCommande etatCommande) {
//        // 1. Vérification de base
//        checkIdCommande(idCommandeClient);
//        if (etatCommande == null) {
//            log.error("L'etat de la commande client est NULL");
//            throw new InvalidOperationException("Impossible de modifier l'etat de la commande avec un etat null",
//                    ErrorCodes.COMMANDE_CLIENT_NON_MODIFIABLE);
//        }
//
//        // 2. Récupérer la commande complète avec ses lignes (indispensable pour éviter la suppression par Hibernate)
//        CommandeClient commande = commandeClientRepository.findByIdWithLignes(idCommandeClient)
//                .orElseThrow(() -> new EntityNotFoundException(
//                        "Aucune commande client n'a été trouvée avec l'ID " + idCommandeClient,
//                        ErrorCodes.COMMANDE_CLIENT_NOT_FOUND));
//
//        // 3. Vérification de l'état actuel (si déjà confirmée, on bloque)
//        if (commande.isCommandeConfirmee()) {
//            throw new InvalidOperationException(
//                    "Impossible de modifier l'état d'une commande déjà confirmée",
//                    ErrorCodes.COMMANDE_CLIENT_NON_MODIFIABLE);
//        }
//
//        // 4. Convertir en DTO pour réutiliser la logique métier du save()
//        CommandeClientDto dto = CommandeClientDto.fromEntity(commande);
//
//        // 5. Appeler la méthode save()
//        // Elle gère déjà :
//        // - La mise à jour des quantités dans le stock (via modifierStockIndividuel)
//        // - La préservation des lignes existantes
//        // - La synchronisation de l'état
//        return this.save(dto, idCommandeClient, etatCommande);
//    }

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
//        if (commandeClient.isCommandeConfirmee()) {
//            updateMvtStk(idCommandeClient);
//        }
//        return CommandeClientDto.fromEntity(savedCmdClt);
//    }




//package com.tchindaClovis.gestiondestock.services.impl;

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


