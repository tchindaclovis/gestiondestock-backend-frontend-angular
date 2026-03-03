package com.tchindaClovis.gestiondestock.services.impl;

import com.tchindaClovis.gestiondestock.dto.ArticleDto;
import com.tchindaClovis.gestiondestock.dto.ClientDto;
import com.tchindaClovis.gestiondestock.dto.CommandeClientDto;
import com.tchindaClovis.gestiondestock.dto.LigneCommandeClientDto;
import com.tchindaClovis.gestiondestock.dto.MvtStockDto;
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
    public CommandeClientDto save(CommandeClientDto dto) {

        log.info("Saving command with {} lines",
                dto.getLigneCommandeClients() != null ? dto.getLigneCommandeClients().size() : 0);

        if (dto.getLigneCommandeClients() != null) {
            log.info("Ligne details: {}", dto.getLigneCommandeClients());
        }


        List<String> errors = CommandeClientValidator.validate(dto);

        if (!errors.isEmpty()) {
            log.error("Commande client n'est pas valide");
            throw new InvalidEntityException("La commande client n'est pas valide", ErrorCodes.COMMANDE_CLIENT_NOT_VALID, errors);
        }

        if (dto.getId() != null && dto.isCommandeLivree()) {
            throw new InvalidOperationException("Impossible de modifier la commande lorsqu'elle est livree", ErrorCodes.COMMANDE_CLIENT_NON_MODIFIABLE);
        }

        Optional<Client> client = clientRepository.findById(dto.getClient().getId());
        if (client.isEmpty()) {
            log.warn("Client with ID {} was not found in the DB", dto.getClient().getId());
            throw new EntityNotFoundException("Aucun client avec l'ID" + dto.getClient().getId() + " n'a ete trouve dans la BDD",
                    ErrorCodes.CLIENT_NOT_FOUND);
        }

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
        dto.setDateCommande(Instant.now());
        CommandeClient savedCmdClt = commandeClientRepository.save(CommandeClientDto.toEntity(dto));

        if (dto.getLigneCommandeClients() != null) {
            dto.getLigneCommandeClients().forEach(ligCmdClt -> {
                LigneCommandeClient ligneCommandeClients = LigneCommandeClientDto.toEntity(ligCmdClt);

//                log.info("LIGNES A SAUVER : {}", dto.getLigneCommandeClients());

                ligneCommandeClients.setCommandeClient(savedCmdClt);
                ligneCommandeClients.setIdEntreprise(dto.getIdEntreprise());
                LigneCommandeClient savedLigneCmd = ligneCommandeClientRepository.save(ligneCommandeClients);

                effectuerSortie(savedLigneCmd);
            });
        }

        return CommandeClientDto.fromEntity(savedCmdClt);
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
    public void delete(Integer id) {
        if (id == null) {
            log.error("Commande client ID is NULL");
            return;
        }
        List<LigneCommandeClient> ligneCommandeClients = ligneCommandeClientRepository.findAllByCommandeClientId(id);
        if (!ligneCommandeClients.isEmpty()) {
            throw new InvalidOperationException("Impossible de supprimer une commande client deja utilisee",
                    ErrorCodes.COMMANDE_CLIENT_ALREADY_IN_USE);
        }
        commandeClientRepository.deleteById(id);
    }

    @Override
    public List<LigneCommandeClientDto> findAllLignesCommandesClientByCommandeClientId(Integer idCommande) {
        return ligneCommandeClientRepository.findAllByCommandeClientId(idCommande).stream()
                .map(LigneCommandeClientDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public CommandeClientDto updateEtatCommande(Integer idCommande, EEtatCommande etatCommande) {
        checkIdCommande(idCommande);
        if (!StringUtils.hasLength(String.valueOf(etatCommande))) {
            log.error("L'etat de la commande client is NULL");
            throw new InvalidOperationException("Impossible de modifier l'etat de la commande avec un etat null",
                    ErrorCodes.COMMANDE_CLIENT_NON_MODIFIABLE);
        }
        CommandeClientDto commandeClient = checkEtatCommande(idCommande);
        commandeClient.setEtatCommande(etatCommande);

        CommandeClient savedCmdClt = commandeClientRepository.save(CommandeClientDto.toEntity(commandeClient));
        if (commandeClient.isCommandeLivree()) {
            updateMvtStk(idCommande);
        }

        return CommandeClientDto.fromEntity(savedCmdClt);
    }

    @Override
    public CommandeClientDto updateQuantiteCommande(Integer idCommande, Integer idLigneCommande, BigDecimal quantite) {
        checkIdCommande(idCommande);
        checkIdLigneCommande(idLigneCommande);

        if (quantite == null || quantite.compareTo(BigDecimal.ZERO) == 0) {
            log.error("L'ID de la ligne commande is NULL");
            throw new InvalidOperationException("Impossible de modifier l'etat de la commande avec une quantite null ou ZERO",
                    ErrorCodes.COMMANDE_CLIENT_NON_MODIFIABLE);
        }

        CommandeClientDto commandeClient = checkEtatCommande(idCommande);
        Optional<LigneCommandeClient> ligneCommandeClientOptional = findLigneCommandeClient(idLigneCommande);

        LigneCommandeClient ligneCommandeClients = ligneCommandeClientOptional.get();
        ligneCommandeClients.setQuantite(quantite);
        ligneCommandeClientRepository.save(ligneCommandeClients);

        return commandeClient;
    }

    @Override
    public CommandeClientDto updateClient(Integer idCommande, Integer idClient) {
        checkIdCommande(idCommande);
        if (idClient == null) {
            log.error("L'ID du client is NULL");
            throw new InvalidOperationException("Impossible de modifier l'etat de la commande avec un ID client null",
                    ErrorCodes.COMMANDE_CLIENT_NON_MODIFIABLE);
        }
        CommandeClientDto commandeClient = checkEtatCommande(idCommande);
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
    public CommandeClientDto updateArticle(Integer idCommande, Integer idLigneCommande, Integer idArticle) {
        checkIdCommande(idCommande);
        checkIdLigneCommande(idLigneCommande);
        checkIdArticle(idArticle, "nouvel");

        CommandeClientDto commandeClient = checkEtatCommande(idCommande);

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
    public CommandeClientDto deleteArticle(Integer idCommande, Integer idLigneCommande) {
        checkIdCommande(idCommande);
        checkIdLigneCommande(idLigneCommande);

        CommandeClientDto commandeClient = checkEtatCommande(idCommande);
        // Just to check the LigneCommandeClient and inform the client in case it is absent
        findLigneCommandeClient(idLigneCommande);
        ligneCommandeClientRepository.deleteById(idLigneCommande);

        return commandeClient;
    }

    private CommandeClientDto checkEtatCommande(Integer idCommande) {
        CommandeClientDto commandeClient = findById(idCommande);
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

    private void checkIdCommande(Integer idCommande) {
        if (idCommande == null) {
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

    private void updateMvtStk(Integer idCommande) {
        List<LigneCommandeClient> ligneCommandeClients = ligneCommandeClientRepository.findAllByCommandeClientId(idCommande);
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
//    public List<LigneCommandeClientDto> findAllLignesCommandesClientByCommandeClientId(Integer idCommande) {
//        return ligneCommandeClientRepository.findAllByCommandeClientId(idCommande).stream()
//                .map(LigneCommandeClientDto::fromEntity)
//                .collect(Collectors.toList());
//    }
//
//    @Override
//    public CommandeClientDto updateEtatCommande(Integer idCommande, EEtatCommande etatCommande) {
//        checkIdCommande(idCommande);
//        if (!StringUtils.hasLength(String.valueOf(etatCommande))) {
//            log.error("L'etat de la commande client is NULL");
//            throw new InvalidOperationException("Impossible de modifier l'etat de la commande avec un etat null",
//                    ErrorCodes.COMMANDE_CLIENT_NON_MODIFIABLE);
//        }
//        CommandeClientDto commandeClient = checkEtatCommande(idCommande);
//        commandeClient.setEtatCommande(etatCommande);
//
//        CommandeClient savedCmdClt = commandeClientRepository.save(CommandeClientDto.toEntity(commandeClient));
//        if (commandeClient.isCommandeLivree()) {
//            updateMvtStk(idCommande);
//        }
//
//        return CommandeClientDto.fromEntity(savedCmdClt);
//    }
//
//    @Override
//    public CommandeClientDto updateQuantiteCommande(Integer idCommande, Integer idLigneCommande, BigDecimal quantite) {
//        checkIdCommande(idCommande);
//        checkIdLigneCommande(idLigneCommande);
//
//        if (quantite == null || quantite.compareTo(BigDecimal.ZERO) == 0) {
//            log.error("L'ID de la ligne commande is NULL");
//            throw new InvalidOperationException(
//                    "Impossible de modifier l'etat de la commande avec une quantite null ou ZERO",
//                    ErrorCodes.COMMANDE_CLIENT_NON_MODIFIABLE);
//        }
//
//        CommandeClientDto commandeClient = checkEtatCommande(idCommande);
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
//    public CommandeClientDto updateClient(Integer idCommande, Integer idClient) {
//        checkIdCommande(idCommande);
//        if (idClient == null) {
//            log.error("L'ID du client is NULL");
//            throw new InvalidOperationException("Impossible de modifier l'etat de la commande avec un ID client null",
//                    ErrorCodes.COMMANDE_CLIENT_NON_MODIFIABLE);
//        }
//        CommandeClientDto commandeClient = checkEtatCommande(idCommande);
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
//    public CommandeClientDto updateArticle(Integer idCommande, Integer idLigneCommande, Integer idArticle) {
//        checkIdCommande(idCommande);            //refactoring du code
//        checkIdLigneCommande(idLigneCommande);  //refactoring du code
//        checkIdArticle(idArticle, "nouvel");  //refactoring du code
//
//        CommandeClientDto commandeClient = checkEtatCommande(idCommande);
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
//    public CommandeClientDto deleteArticle(Integer idCommande, Integer idLigneCommande) {
//        checkIdCommande(idCommande);
//        checkIdLigneCommande(idLigneCommande);
//
//        CommandeClientDto commandeClient = checkEtatCommande(idCommande);
//        // Just to check the LigneCommandeClient and inform the client in case it is absent
//        findLigneCommandeClient(idLigneCommande);
//        ligneCommandeClientRepository.deleteById(idLigneCommande);
//
//        return commandeClient;
//    }
//
//    private CommandeClientDto checkEtatCommande(Integer idCommande) {
//        CommandeClientDto commandeClient = findById(idCommande);
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
//    private void checkIdCommande(Integer idCommande) {
//        if (idCommande == null) {
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
//    private void updateMvtStk(Integer idCommande) {
//        List<LigneCommandeClient> ligneCommandeClients = ligneCommandeClientRepository
//                .findAllByCommandeClientId(idCommande);
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











//package com.tchindaClovis.gestiondestock.services.impl;
//
//import com.tchindaClovis.gestiondestock.dto.CommandeClientDto;
//import com.tchindaClovis.gestiondestock.dto.LigneCommandeClientDto;
//import com.tchindaClovis.gestiondestock.exception.EntityNotFoundException;
//import com.tchindaClovis.gestiondestock.exception.ErrorCodes;
//import com.tchindaClovis.gestiondestock.exception.InvalidEntityException;
//import com.tchindaClovis.gestiondestock.model.Article;
//import com.tchindaClovis.gestiondestock.model.Client;
//import com.tchindaClovis.gestiondestock.model.CommandeClient;
//import com.tchindaClovis.gestiondestock.model.LigneCommandeClient;
//import com.tchindaClovis.gestiondestock.repository.ArticleRepository;
//import com.tchindaClovis.gestiondestock.repository.ClientRepository;
//import com.tchindaClovis.gestiondestock.repository.CommandeClientRepository;
//import com.tchindaClovis.gestiondestock.repository.LigneCommandeClientRepository;
//import com.tchindaClovis.gestiondestock.services.CommandeClientService;
//import com.tchindaClovis.gestiondestock.validator.CommandeClientValidator;
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
//public class CommandeClientServiceImpl implements CommandeClientService {
//    private CommandeClientRepository commandeClientRepository;
//    private LigneCommandeClientRepository ligneCommandeClientRepository;
//    private ClientRepository clientRepository;
//    private ArticleRepository articleRepository;
//
//    @Autowired
//    public CommandeClientServiceImpl(CommandeClientRepository commandeClientRepository,
//                                     LigneCommandeClientRepository ligneCommandeClientRepository,
//                                     ClientRepository clientRepository, ArticleRepository articleRepository) {
//        this.commandeClientRepository = commandeClientRepository;
//        this.ligneCommandeClientRepository = ligneCommandeClientRepository;
//        this.clientRepository = clientRepository;
//        this.articleRepository = articleRepository;
//    }
//
//    @Override
//    public CommandeClientDto save(CommandeClientDto dto) {
//
//        List<String> errors = CommandeClientValidator.validate(dto);
//
//        if(!errors.isEmpty()){
//            log.error(" Commande client n'est pas valide", dto);
//            throw new InvalidEntityException("La commande client n'est pas valide",
//                    ErrorCodes.COMMANDE_CLIENT_NOT_VALID, errors);
//        }
//        Optional<Client> client = clientRepository.findById(dto.getClient().getId());
//        if(!client.isEmpty()) {
//            log.error(" Client with ID {} was not found in the DB", dto.getClient().getId());
//            throw new EntityNotFoundException("Aucun client avec l'ID" + dto.getClient().getId() +
//                    "n'a été trouvé dans la BDD", ErrorCodes.CLIENT_NOT_FOUND);
//        }
//
//        List<String> articleErrors = new ArrayList<>();
//
//        if(dto.getLigneCommandeClients() != null) {
//            dto.getLigneCommandeClients().forEach(ligCmdClt -> {
//                if (ligCmdClt.getArticle() != null) {
//                    Optional<Article> article = articleRepository.findById(ligCmdClt.getArticle().getId());
//                    if (article.isEmpty()) {
//                        articleErrors.add("L'article avec l'ID " + ligCmdClt.getArticle().getId() +
//                                " n'existe pas");
//                    }
//                }else{
//                    articleErrors.add("Impossible d'enregistrer une commande avec un article NULL");
//                }
//            });
//            }
//
//            if (!articleErrors.isEmpty()){
//              log.warn("");
//              throw new InvalidEntityException("L'article n'existe pas dans la BDD",
//                      ErrorCodes.ARTICLE_NOT_FOUND, articleErrors);
//            }
//
//            CommandeClient savedCmdClt = commandeClientRepository.save(CommandeClientDto.toEntity(dto));
//
//            if(dto.getLigneCommandeClients() != null) {
//                dto.getLigneCommandeClients().forEach(ligCmdClt -> {
//                    LigneCommandeClient ligneCommandeClient = LigneCommandeClientDto.toEntity(ligCmdClt); //transformer l'objet DTO en entité
//                    ligneCommandeClient.setCommandeClient(savedCmdClt); // lier la ligne de commande à la commande enregistrée
//                    ligneCommandeClientRepository.save(ligneCommandeClient);
//                });
//            }
//            return CommandeClientDto.fromEntity(savedCmdClt);
//        }
//
//
//    @Override
//    public CommandeClientDto findById(Integer id) {
//        if(id == null){
//            log.error("Commande client ID is NULL");
//            return null;
//        }
//        return commandeClientRepository.findById(id)
//                .map(CommandeClientDto::fromEntity)
//                .orElseThrow(() -> new EntityNotFoundException(
//                        "Aucune commande client n'a été trouvé avec l'ID " + id,ErrorCodes.COMMANDE_CLIENT_NOT_FOUND
//                ));
//    }
//
//    @Override
//    public CommandeClientDto findByCode(String code) {
//        if(!StringUtils.hasLength(code)){
//            log.error("Commande client CODE is null");
//            return null;
//        }
//        return commandeClientRepository.findCommandeClientByCode(code)
//                .map(CommandeClientDto::fromEntity)
//                .orElseThrow(() -> new EntityNotFoundException(
//                        "Aucune commande client n'a été trouvé avec le CODE " +
//                                code,ErrorCodes.COMMANDE_CLIENT_NOT_FOUND
//                ));
//    }
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
//        if(id == null){
//            log.error("Commande client ID is NULL");
//            return ;
//        }
//        commandeClientRepository.deleteById(id);
//    }
//}
