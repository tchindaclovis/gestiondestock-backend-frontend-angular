package com.tchindaClovis.gestiondestock.services.impl;

import com.tchindaClovis.gestiondestock.dto.*;
import com.tchindaClovis.gestiondestock.exception.InvalidOperationException;
import com.tchindaClovis.gestiondestock.model.*;
import com.tchindaClovis.gestiondestock.exception.EntityNotFoundException;
import com.tchindaClovis.gestiondestock.exception.ErrorCodes;
import com.tchindaClovis.gestiondestock.exception.InvalidEntityException;
import com.tchindaClovis.gestiondestock.repository.*;
import com.tchindaClovis.gestiondestock.services.VenteService;
import com.tchindaClovis.gestiondestock.services.MvtStockService;
import com.tchindaClovis.gestiondestock.validator.VenteValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class VenteServiceImpl implements VenteService {
    private ArticleRepository articleRepository;
    private VenteRepository venteRepository;
    private LigneVenteRepository ligneVenteRepository;

    private MvtStockService mvtStockService;

    private CommandeClientRepository commandeClientRepository;

    private ClientRepository clientRepository;

    @Autowired
    public VenteServiceImpl(ArticleRepository articleRepository, VenteRepository
            venteRepository, LigneVenteRepository ligneVenteRepository, MvtStockService mvtStockService,
                            CommandeClientRepository commandeClientRepository, ClientRepository clientRepository) {
        this.articleRepository = articleRepository;
        this.venteRepository = venteRepository;
        this.ligneVenteRepository = ligneVenteRepository;
        this.mvtStockService = mvtStockService;
        this.commandeClientRepository = commandeClientRepository;
        this.clientRepository = clientRepository;
    }



    @Override
    @Transactional
    public VenteDto save(VenteDto dto) {
        validateVente(dto);

        // =========================================================================
        // 🛡️ SÉCURITÉ ALIGNÉE : BLOCAGE SI LES QUANTITÉS OU LES ARTICLES ONT CHANGÉ PAR RAPPORT À LA COMMANDE
        // =========================================================================
        if (dto.getCodeCommandeClient() != null && !dto.getCodeCommandeClient().trim().isEmpty()) {

            // Récupérer la commande d'origine avec ses lignes depuis la BDD
            CommandeClient commandeInitiale = commandeClientRepository.findByCode(dto.getCodeCommandeClient())
                    .orElseThrow(() -> new EntityNotFoundException(
                            "Commande client introuvable avec le code : " + dto.getCodeCommandeClient(),
                            ErrorCodes.COMMANDE_CLIENT_NOT_FOUND
                    ));

            // Construire la Map des éléments attendus de la commande : ID Article -> Quantité Commandée
            Map<Integer, BigDecimal> lignesCommandeMap = new HashMap<>();
            if (commandeInitiale.getLigneCommandeClients() != null) {
                commandeInitiale.getLigneCommandeClients().forEach(ligCmd -> {
                    if (ligCmd.getArticle() != null) {
                        lignesCommandeMap.put(ligCmd.getArticle().getId(), ligCmd.getQuantite());
                    }
                });
            }

            int nbrNouvellesLignes = dto.getLigneVentes() != null ? dto.getLigneVentes().size() : 0;

            // Vérification sur le nombre global d'articles
            if (lignesCommandeMap.size() != nbrNouvellesLignes) {
                throw new InvalidOperationException(
                        "Modification impossible : Le nombre d'articles de la vente (" + nbrNouvellesLignes +
                                ") ne correspond pas à la commande initiale (" + lignesCommandeMap.size() + ").",
                        ErrorCodes.VENTE_NON_MODIFIABLE
                );
            }

            // Vérification de la correspondance exacte des articles et des quantités
            if (dto.getLigneVentes() != null) {
                for (LigneVenteDto ligDto : dto.getLigneVentes()) {
                    if (ligDto.getArticle() == null || ligDto.getArticle().getId() == null) {
                        throw new InvalidEntityException("Un article de la vente n'est pas valide ou ne possède pas d'identifiant.");
                    }

                    Integer idArticle = ligDto.getArticle().getId();
                    BigDecimal qteFacturee = ligDto.getQuantite();

                    // L'article fait-il partie de la commande ?
                    if (!lignesCommandeMap.containsKey(idArticle)) {
                        throw new InvalidOperationException(
                                "Modification impossible : L'article ID " + idArticle + " ne figure pas dans la commande d'origine " + dto.getCodeCommandeClient() + ".",
                                ErrorCodes.VENTE_NON_MODIFIABLE
                        );
                    }

                    // La quantité correspond-elle à celle de la commande ?
                    BigDecimal qteCommandee = lignesCommandeMap.get(idArticle);
                    if (qteFacturee.compareTo(qteCommandee) != 0) {
                        throw new InvalidOperationException(
                                "Modification impossible : La quantité facturée pour l'article ID " + idArticle +
                                        " (" + qteFacturee + ") diffère de la quantité commandée (" + qteCommandee + ").",
                                ErrorCodes.VENTE_NON_MODIFIABLE
                        );
                    }
                }
            }
        }
        // =========================================================================

        // On récupère l'ancienne vente si c'est une mise à jour
        Map<Integer, BigDecimal> anciennesLignesMap = new HashMap<>();
        Vente venteToSave;

        if (dto.getId() != null) {
            venteToSave = venteRepository.findByIdWithLignes(dto.getId())
                    .orElseThrow(() -> new EntityNotFoundException("Vente introuvable", ErrorCodes.VENTE_NOT_FOUND));

            // On remplit la Map pour la logique différentielle des stocks
            if (venteToSave.getLigneVentes() != null) {
                venteToSave.getLigneVentes().forEach(lig ->
                        anciennesLignesMap.put(lig.getArticle().getId(), lig.getQuantite())
                );
            }

            // 🛡️ SÉCURITÉ : Conserver le codeCommandeClient d'origine s'il était déjà présent en BDD
            if (venteToSave.getCodeCommandeClient() != null && !venteToSave.getCodeCommandeClient().trim().isEmpty()) {
                // On force le maintien du code d'origine si le DTO l'envoie vide
                if (dto.getCodeCommandeClient() == null || dto.getCodeCommandeClient().trim().isEmpty()) {
                    dto.setCodeCommandeClient(venteToSave.getCodeCommandeClient());
                }
            }

            // On met à jour les infos générales autorisées
            venteToSave.setCode(dto.getCode());
            venteToSave.setCodeCommandeClient(dto.getCodeCommandeClient());
            venteToSave.setPaymentType(dto.getPaymentType());
            venteToSave.setDateVente(dto.getDateVente());

            // 🛡️ MISE À JOUR SÉLECTIVE DU CLIENT : Éviter de vider les autres champs en BDD
            if (dto.getClient() != null && dto.getClient().getId() != null) {
                Client clientExistant = clientRepository.findById(dto.getClient().getId())
                        .orElseThrow(() -> new EntityNotFoundException("Client introuvable", ErrorCodes.CLIENT_NOT_FOUND));

                // On met uniquement à jour les champs gérés par votre formulaire de vente
                if (dto.getClient().getNom() != null) clientExistant.setNom(dto.getClient().getNom());
                if (dto.getClient().getStatut() != null) clientExistant.setStatut(dto.getClient().getStatut());
                if (dto.getClient().getNumTel() != null) clientExistant.setNumTel(dto.getClient().getNumTel());
                if (dto.getClient().getEmail() != null) clientExistant.setEmail(dto.getClient().getEmail());

                if (dto.getClient().getAdresse() != null) {
                    if (clientExistant.getAdresse() == null) {
                        clientExistant.setAdresse(new Adresse()); // ou Adresse.builder().build() selon votre projet
                    }
                    if (dto.getClient().getAdresse().getAdresse1() != null) {
                        clientExistant.getAdresse().setAdresse1(dto.getClient().getAdresse().getAdresse1());
                    }
                }

                // On sauvegarde le client modifié de manière isolée
                clientRepository.save(clientExistant);
                venteToSave.setClient(clientExistant);
            }

            // On nettoie les anciennes lignes pour Hibernate
            venteToSave.getLigneVentes().clear();
            venteRepository.saveAndFlush(venteToSave);

        } else {
            // 1. On intercepte le client AVANT de faire le toEntity global pour éviter de créer un objet transient inutile
            Client clientPersistant = null;
            if (dto.getClient() != null && dto.getClient().getId() != null) {
                clientPersistant = clientRepository.findById(dto.getClient().getId())
                        .orElseThrow(() -> new EntityNotFoundException("Client introuvable", ErrorCodes.CLIENT_NOT_FOUND));
            }

            // 2. On transforme le DTO en entité
            venteToSave = VenteDto.toEntity(dto);

            // 3. On applique explicitement le client persistant managé par Hibernate
            venteToSave.setClient(clientPersistant);
        }


        // Préparer la liste des nouvelles lignes pour la sauvegarde
        if (dto.getLigneVentes() != null) {
            if (venteToSave.getLigneVentes() == null) {
                venteToSave.setLigneVentes(new ArrayList<>());
            }

            for (LigneVenteDto ligDto : dto.getLigneVentes()) {
                LigneVente lig = LigneVenteDto.toEntity(ligDto);
                lig.setId(null);
                lig.setVente(venteToSave);
                lig.setIdEntreprise(dto.getIdEntreprise());
                venteToSave.getLigneVentes().add(lig);

                // LOGIQUE DIFFÉRENTIELLE DES STOCKS
                Integer articleId = ligDto.getArticle().getId();
                BigDecimal nouvelleQte = ligDto.getQuantite();

                if (anciennesLignesMap.containsKey(articleId)) {
                    BigDecimal ancienneQte = anciennesLignesMap.get(articleId);
                    int comparaison = nouvelleQte.compareTo(ancienneQte);

                    if (comparaison > 0) {
                        BigDecimal diff = nouvelleQte.subtract(ancienneQte);
                        modifierStockIndividuel(lig, diff, ETypeMvtStock.CORRECTION_NEG_VENTE_AUG);
                    }
                    else if (comparaison < 0) {
                        BigDecimal diff = ancienneQte.subtract(nouvelleQte);
                        modifierStockIndividuel(lig, diff, ETypeMvtStock.CORRECTION_POS_VENTE_RED);
                    }
                    anciennesLignesMap.remove(articleId);
                } else {
                    modifierStockIndividuel(lig, nouvelleQte, ETypeMvtStock.SORTIE_VTE);
                }
            }
        }

        // GÉRER LES SUPPRESSIONS (Articles restants dans la Map)
        anciennesLignesMap.forEach((idArt, qteInitiale) -> {
            LigneVente ligneSupprimee = new LigneVente();
            ligneSupprimee.setArticle(articleRepository.findById(idArt).orElse(null));
            ligneSupprimee.setQuantite(qteInitiale);
            ligneSupprimee.setIdEntreprise(dto.getIdEntreprise());

            updateMvtStockAnnulation(ligneSupprimee);
        });

        // Sauvegarde de la vente principale
        Vente savedVente = venteRepository.saveAndFlush(venteToSave);

        // TRAITEMENT DE LA COMMANDE CLIENT LIÉE
        if (dto.getCodeCommandeClient() != null && !dto.getCodeCommandeClient().trim().isEmpty()) {
            Optional<CommandeClient> commandeOpt = commandeClientRepository.findByCode(dto.getCodeCommandeClient());

            if (commandeOpt.isPresent()) {
                CommandeClient commandeClient = commandeOpt.get();
                commandeClient.setEtatCommande(EEtatCommande.VENDUE);
                commandeClientRepository.save(commandeClient);
                log.info("✅ La commande client avec le code {} a été passée à l'état VENDUE.", dto.getCodeCommandeClient());
            } else {
                log.warn("⚠️ Code commande client '{}' fourni mais aucune commande correspondante en BDD.", dto.getCodeCommandeClient());
            }
        }

        return VenteDto.fromEntity(savedVente);
    }




    /**
     * Méthode utilitaire pour générer un mouvement de stock spécifique (différentiel)
     */
    private void modifierStockIndividuel(LigneVente lig, BigDecimal qte, ETypeMvtStock type) {
        if (qte.compareTo(BigDecimal.ZERO) <= 0) return;

        MvtStockDto mvt = MvtStockDto.builder()
                .article(ArticleDto.fromEntity(lig.getArticle()))
                .dateMvt(Instant.now())
                .typeMvt(type)
                .sourceMvt(ESourceMvtStock.VENTE)
                .quantite(qte)
                .codeSource(lig.getVente().getCode()) // ON PASSE LE CODE ICI
                .idEntreprise(lig.getIdEntreprise())
                .build();

        if (type == ETypeMvtStock.CORRECTION_NEG_VENTE_AUG) {
            mvtStockService.correctionStockNegVenteAug(mvt);
        } else  if (type == ETypeMvtStock.CORRECTION_POS_VENTE_RED){
            mvtStockService.correctionStockPosVenteRed1(mvt);
        }else{
            mvtStockService.sortieStockVte(mvt);
        }
    }

    /**
     * Méthode pour réintégrer les stocks lors de l'annulation/modification/suppression d'une ligne
     */
    private void updateMvtStockAnnulation(LigneVente ligne) {
        MvtStockDto mvtStockDto = MvtStockDto.builder()
                .article(ArticleDto.fromEntity(ligne.getArticle()))
                .dateMvt(Instant.now())
                .typeMvt(ETypeMvtStock.ENTREE) // Ou CORRECTION_POS selon votre enum
                .sourceMvt(ESourceMvtStock.VENTE)
                .quantite(ligne.getQuantite()) // On remet la quantité initiale en stock
                .codeSource(ligne.getVente().getCode()) // ON PASSE LE CODE ICI
                .idEntreprise(ligne.getIdEntreprise())
                .build();
        mvtStockService.entreeStock(mvtStockDto);
    }


    /**
     * Vérifie si le stock est suffisant pour chaque article de la vente
     */
    private void verifierStockDisponible(VenteDto dto) {
        List<String> stockErrors = new ArrayList<>();

        if (dto.getLigneVentes() != null) {
            for (LigneVenteDto lig : dto.getLigneVentes()) {
                if (lig.getArticle() != null && lig.getArticle().getId() != null) {
                    // On récupère la quantité actuelle en stock pour cet article
                    // Hypothèse : vous avez une méthode dans mvtStockService pour cela
                    BigDecimal stockActuel = mvtStockService.stockReelArticle(lig.getArticle().getId());

                    if (stockActuel.compareTo(lig.getQuantite()) < 0) {
                        stockErrors.add("Stock insuffisant pour l'article " + lig.getArticle().getCodeArticle()
                                + " (Disponible: " + stockActuel + ", Demandé: " + lig.getQuantite() + ")");
                    }
                }
            }
        }

        if (!stockErrors.isEmpty()) {
            throw new InvalidEntityException("Stock insuffisant", ErrorCodes.VENTE_NOT_VALID, stockErrors);
        }
    }


    /**
     * Nettoyage de la logique de validation
     */
    private void validateVente(VenteDto dto) {
        List<String> errors = VenteValidator.validate(dto);

        if (dto.getLigneVentes() != null) {
            dto.getLigneVentes().forEach(lig -> {
                if (lig.getArticle() == null || !articleRepository.existsById(lig.getArticle().getId())) {
                    errors.add("Article ID " + (lig.getArticle() != null ? lig.getArticle().getId() : "null") + " introuvable.");
                }
            });
        }

        if (!errors.isEmpty()) {
            log.error("Échec de validation de la vente : {}", errors);
            throw new InvalidEntityException("Vente invalide", ErrorCodes.VENTE_NOT_VALID, errors);
        }


    }


    private void updateMvtStock(LigneVente lig) {
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

    @Override
    public VenteDto findById(Integer id) {
        if(id == null){
            log.error("Vente ID is NULL");
            return null;
        }
        return venteRepository.findById(id)
                .map(VenteDto::fromEntity)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Aucune vente n'a été trouvé avec l'ID " + id,ErrorCodes.VENTE_NOT_FOUND
                ));
    }


    @Override

    public VenteDto findByCode(String code) {
        if (!StringUtils.hasLength(code)) {
            log.error("Vente CODE is null");
            return null;
        }

        return venteRepository.findVenteByCode(code)
                .map(VenteDto::fromEntity)
                .orElseThrow(() ->
                        new EntityNotFoundException(
                                "Aucune vente avec le CODE = " + code + " n'a été trouve dans la BDD",
                                ErrorCodes.VENTE_NOT_FOUND)
                );
    }

    @Override
    public VenteDto findVenteByCodeCommandeClient(String codeCommandeClient) {
        if(!StringUtils.hasLength(codeCommandeClient)){
            log.error("Vente CODE is null");
            return null;
        }
        return venteRepository.findByCodeCommandeClient(codeCommandeClient)
                .map(VenteDto::fromEntity)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Aucune vente n'a été trouvé avec le CODE " +
                                codeCommandeClient,ErrorCodes.VENTE_NOT_FOUND
                ));
    }

    @Override
    public List<VenteDto> findAll() {
        return venteRepository.findAll().stream()
                .map(VenteDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<VenteDto> findAllVenteByIdEntreprise(Integer idEntreprise) {
        if (idEntreprise == null) {
            log.error("Entreprise ID is null");
            return List.of();
        }
        return venteRepository.findAllByIdEntreprise(idEntreprise).stream()
                .map(VenteDto::fromEntity)
                .collect(Collectors.toList());
    }
    @Override
    public List<LigneVenteDto> findAllLignesVentesByVenteId(Integer idVente) {
        return ligneVenteRepository.findAllByVenteId(idVente).stream()
                .map(LigneVenteDto::fromEntity)
                .collect(Collectors.toList());
    }


    @Override
    @Transactional // CRITIQUE : Assure que tout est validé en base à la fin
    public void delete(Integer id) {
        if (id == null) {
            log.error("Vente ID is NULL");
            return;
        }

        // 1. Récupérer les lignes de vente
        List<LigneVente> ligneVentes = ligneVenteRepository.findAllByVenteId(id);

        // 2. Supprimer les lignes explicitement pour libérer la vente
        if (!ligneVentes.isEmpty()) {
            ligneVenteRepository.deleteAll(ligneVentes);
            log.info("Lignes de vente pour la vente ID {} supprimées", id);
        }

        // 3. Supprimer la vente
        venteRepository.deleteById(id);
        log.info("Vente ID {} supprimée avec succès", id);
    }


    @Override
    public String getLastCodeVente() {
        // CORRECTION : Utilisez l'instance "venteRepository" (minuscule)
        // et extrayez le code de l'objet Vente
        return venteRepository.findTopByOrderByCodeDesc()
                .map(Vente::getCode) // On transforme l'Vente en String (son code)
                .orElse("CVT0000");           // Valeur par défaut si aucun vente n'existe
    }
}





//    @Override
//    @Transactional
//    public VenteDto save(VenteDto dto) {
//        validateVente(dto);
//
//        // =========================================================================
//        // 🛡️ SÉCURITÉ ALIGNÉE : BLOCAGE SI LES QUANTITÉS OU LES ARTICLES ONT CHANGÉ PAR RAPPORT À LA COMMANDE
//        // =========================================================================
//        if (dto.getCodeCommandeClient() != null && !dto.getCodeCommandeClient().trim().isEmpty()) {
//
//            // Récupérer la commande d'origine avec ses lignes depuis la BDD
//            CommandeClient commandeInitiale = commandeClientRepository.findByCode(dto.getCodeCommandeClient())
//                    .orElseThrow(() -> new EntityNotFoundException(
//                            "Commande client introuvable avec le code : " + dto.getCodeCommandeClient(),
//                            ErrorCodes.COMMANDE_CLIENT_NOT_FOUND
//                    ));
//
//            // Construire la Map des éléments attendus de la commande : ID Article -> Quantité Commandée
//            Map<Integer, BigDecimal> lignesCommandeMap = new HashMap<>();
//            if (commandeInitiale.getLigneCommandeClients() != null) {
//                commandeInitiale.getLigneCommandeClients().forEach(ligCmd -> {
//                    if (ligCmd.getArticle() != null) {
//                        lignesCommandeMap.put(ligCmd.getArticle().getId(), ligCmd.getQuantite());
//                    }
//                });
//            }
//
//            int nbrNouvellesLignes = dto.getLigneVentes() != null ? dto.getLigneVentes().size() : 0;
//
//            // Vérification sur le nombre global d'articles
//            if (lignesCommandeMap.size() != nbrNouvellesLignes) {
//                throw new InvalidOperationException(
//                        "Modification impossible : Le nombre d'articles de la vente (" + nbrNouvellesLignes +
//                                ") ne correspond pas à la commande initiale (" + lignesCommandeMap.size() + ").",
//                        ErrorCodes.VENTE_NON_MODIFIABLE
//                );
//            }
//
//            // Vérification de la correspondance exacte des articles et des quantités
//            if (dto.getLigneVentes() != null) {
//                for (LigneVenteDto ligDto : dto.getLigneVentes()) {
//                    if (ligDto.getArticle() == null || ligDto.getArticle().getId() == null) {
//                        throw new InvalidEntityException("Un article de la vente n'est pas valide ou ne possède pas d'identifiant.");
//                    }
//
//                    Integer idArticle = ligDto.getArticle().getId();
//                    BigDecimal qteFacturee = ligDto.getQuantite();
//
//                    // L'article fait-il partie de la commande ?
//                    if (!lignesCommandeMap.containsKey(idArticle)) {
//                        throw new InvalidOperationException(
//                                "Modification impossible : L'article ID " + idArticle + " ne figure pas dans la commande d'origine " + dto.getCodeCommandeClient() + ".",
//                                ErrorCodes.VENTE_NON_MODIFIABLE
//                        );
//                    }
//
//                    // La quantité correspond-elle à celle de la commande ?
//                    BigDecimal qteCommandee = lignesCommandeMap.get(idArticle);
//                    if (qteFacturee.compareTo(qteCommandee) != 0) {
//                        throw new InvalidOperationException(
//                                "Modification impossible : La quantité facturée pour l'article ID " + idArticle +
//                                        " (" + qteFacturee + ") diffère de la quantité commandée (" + qteCommandee + ").",
//                                ErrorCodes.VENTE_NON_MODIFIABLE
//                        );
//                    }
//                }
//            }
//        }
//        // =========================================================================
//
//        // On récupère l'ancienne vente si c'est une mise à jour
//        Map<Integer, BigDecimal> anciennesLignesMap = new HashMap<>();
//        Vente venteToSave;
//
//        if (dto.getId() != null) {
//            venteToSave = venteRepository.findByIdWithLignes(dto.getId())
//                    .orElseThrow(() -> new EntityNotFoundException("Vente introuvable", ErrorCodes.VENTE_NOT_FOUND));
//
//            // On remplit la Map pour la logique différentielle des stocks
//            if (venteToSave.getLigneVentes() != null) {
//                venteToSave.getLigneVentes().forEach(lig ->
//                        anciennesLignesMap.put(lig.getArticle().getId(), lig.getQuantite())
//                );
//            }
//
//            // 🛡️ SÉCURITÉ : Conserver le codeCommandeClient d'origine s'il était déjà présent en BDD
//            if (venteToSave.getCodeCommandeClient() != null && !venteToSave.getCodeCommandeClient().trim().isEmpty()) {
//                // On force le maintien du code d'origine si le DTO l'envoie vide
//                if (dto.getCodeCommandeClient() == null || dto.getCodeCommandeClient().trim().isEmpty()) {
//                    dto.setCodeCommandeClient(venteToSave.getCodeCommandeClient());
//                }
//            }
//
//            // On met à jour les infos générales autorisées
//            venteToSave.setCode(dto.getCode());
//            venteToSave.setCodeCommandeClient(dto.getCodeCommandeClient());
//            venteToSave.setPaymentType(dto.getPaymentType());
//            venteToSave.setDateVente(dto.getDateVente());
//
//            // 🛡️ MISE À JOUR SÉLECTIVE DU CLIENT : Éviter de vider les autres champs en BDD
//            if (dto.getClient() != null && dto.getClient().getId() != null) {
//                Client clientExistant = clientRepository.findById(dto.getClient().getId())
//                        .orElseThrow(() -> new EntityNotFoundException("Client introuvable", ErrorCodes.CLIENT_NOT_FOUND));
//
//                // On met uniquement à jour les champs gérés par votre formulaire de vente
//                if (dto.getClient().getNom() != null) clientExistant.setNom(dto.getClient().getNom());
//                if (dto.getClient().getStatut() != null) clientExistant.setStatut(dto.getClient().getStatut());
//                if (dto.getClient().getNumTel() != null) clientExistant.setNumTel(dto.getClient().getNumTel());
//                if (dto.getClient().getEmail() != null) clientExistant.setEmail(dto.getClient().getEmail());
//
//                if (dto.getClient().getAdresse() != null) {
//                    if (clientExistant.getAdresse() == null) {
//                        clientExistant.setAdresse(new Adresse()); // ou Adresse.builder().build() selon votre projet
//                    }
//                    if (dto.getClient().getAdresse().getAdresse1() != null) {
//                        clientExistant.getAdresse().setAdresse1(dto.getClient().getAdresse().getAdresse1());
//                    }
//                }
//
//                // On sauvegarde le client modifié de manière isolée
//                clientRepository.save(clientExistant);
//                venteToSave.setClient(clientExistant);
//            }
//
//            // On nettoie les anciennes lignes pour Hibernate
//            venteToSave.getLigneVentes().clear();
//            venteRepository.saveAndFlush(venteToSave);
//
//        } else {
//            // 1. On intercepte le client AVANT de faire le toEntity global pour éviter de créer un objet transient inutile
//            Client clientPersistant = null;
//            if (dto.getClient() != null && dto.getClient().getId() != null) {
//                clientPersistant = clientRepository.findById(dto.getClient().getId())
//                        .orElseThrow(() -> new EntityNotFoundException("Client introuvable", ErrorCodes.CLIENT_NOT_FOUND));
//            }
//
//            // 2. On transforme le DTO en entité
//            venteToSave = VenteDto.toEntity(dto);
//
//            // 3. On applique explicitement le client persistant managé par Hibernate
//            venteToSave.setClient(clientPersistant);
//        }
//
//        // -------------------------------------------------------------------------
//        // 📦 INDICATEUR : VÉRIFICATION SI LA VENTE EST ISSUE D'UNE COMMANDE CLIENT
//        // -------------------------------------------------------------------------
//        boolean estIssueDuneCommandeClient = dto.getCodeCommandeClient() != null && !dto.getCodeCommandeClient().trim().isEmpty();
//
//        // Préparer la liste des nouvelles lignes pour la sauvegarde
//        if (dto.getLigneVentes() != null) {
//            if (venteToSave.getLigneVentes() == null) {
//                venteToSave.setLigneVentes(new ArrayList<>());
//            }
//
//            for (LigneVenteDto ligDto : dto.getLigneVentes()) {
//                LigneVente lig = LigneVenteDto.toEntity(ligDto);
//                lig.setId(null);
//                lig.setVente(venteToSave);
//                lig.setIdEntreprise(dto.getIdEntreprise());
//                venteToSave.getLigneVentes().add(lig);
//
//                Integer articleId = ligDto.getArticle().getId();
//                BigDecimal nouvelleQte = ligDto.getQuantite();
//
//                if (estIssueDuneCommandeClient) {
//                    // -----------------------------------------------------------------
//                    // 📝 VENTE ISSUE D'UNE COMMANDE :
//                    // On enregistre la ligne de mouvement de stock pour traçabilité (Audit),
//                    // mais la quantité impactant le stock physique est mise à ZERO.
//                    // -----------------------------------------------------------------
//                    modifierStockIndividuel(lig, BigDecimal.ZERO, ETypeMvtStock.SORTIE_VTE);
//
//                } else {
//                    // -----------------------------------------------------------------
//                    // 🔄 VENTE DIRECTE : LOGIQUE DIFFÉRENTIELLE CLASSIQUE
//                    // -----------------------------------------------------------------
//                    if (anciennesLignesMap.containsKey(articleId)) {
//                        BigDecimal ancienneQte = anciennesLignesMap.get(articleId);
//                        int comparaison = nouvelleQte.compareTo(ancienneQte);
//
//                        if (comparaison > 0) {
//                            BigDecimal diff = nouvelleQte.subtract(ancienneQte);
//                            modifierStockIndividuel(lig, diff, ETypeMvtStock.CORRECTION_NEG_VENTE_AUG);
//                        }
//                        else if (comparaison < 0) {
//                            BigDecimal diff = ancienneQte.subtract(nouvelleQte);
//                            modifierStockIndividuel(lig, diff, ETypeMvtStock.CORRECTION_POS_VENTE_RED);
//                        }
//                        anciennesLignesMap.remove(articleId);
//                    } else {
//                        modifierStockIndividuel(lig, nouvelleQte, ETypeMvtStock.SORTIE_VTE);
//                    }
//                }
//            }
//        }
//
//        // -------------------------------------------------------------------------
//        // GÉRER LES SUPPRESSIONS (Articles restants dans la Map lors d'une MAJ)
//        // S'applique uniquement aux ventes directes (hors commande)
//        // -------------------------------------------------------------------------
//        if (!estIssueDuneCommandeClient) {
//            anciennesLignesMap.forEach((idArt, qteInitiale) -> {
//                LigneVente ligneSupprimee = new LigneVente();
//                ligneSupprimee.setArticle(articleRepository.findById(idArt).orElse(null));
//                ligneSupprimee.setQuantite(qteInitiale);
//                ligneSupprimee.setIdEntreprise(dto.getIdEntreprise());
//
//                updateMvtStockAnnulation(ligneSupprimee);
//            });
//        }
//
//        // Sauvegarde de la vente principale
//        Vente savedVente = venteRepository.saveAndFlush(venteToSave);
//
//        // TRAITEMENT DE LA COMMANDE CLIENT LIÉE
//        if (estIssueDuneCommandeClient) {
//            Optional<CommandeClient> commandeOpt = commandeClientRepository.findByCode(dto.getCodeCommandeClient());
//
//            if (commandeOpt.isPresent()) {
//                CommandeClient commandeClient = commandeOpt.get();
//                commandeClient.setEtatCommande(EEtatCommande.VENDUE);
//                commandeClientRepository.save(commandeClient);
//                log.info("✅ La commande client avec le code {} a été passée à l'état VENDUE.", dto.getCodeCommandeClient());
//            } else {
//                log.warn("⚠️ Code commande client '{}' fourni mais aucune commande correspondante en BDD.", dto.getCodeCommandeClient());
//            }
//        }
//
//        return VenteDto.fromEntity(savedVente);
//    }

















//        if (dto.getId() != null) {
//            venteToSave = venteRepository.findByIdWithLignes(dto.getId())
//            .orElseThrow(() -> new EntityNotFoundException("Vente introuvable", ErrorCodes.VENTE_NOT_FOUND));
//
//            // On remplit la Map : ID Article -> Quantité initiale
//            if (venteToSave.getLigneVentes() != null) {
//            venteToSave.getLigneVentes().forEach(lig ->
//            anciennesLignesMap.put(lig.getArticle().getId(), lig.getQuantite())
//            );
//            }
//
//            // On met à jour les infos générales autorisées (ex: type de paiement)
//            venteToSave.setCode(dto.getCode());
//            venteToSave.setCodeCommandeClient(dto.getCodeCommandeClient());
//            venteToSave.setPaymentType(dto.getPaymentType());
//            venteToSave.setDateVente(dto.getDateVente());
//
//            // On nettoie les anciennes lignes pour Hibernate
//            venteToSave.getLigneVentes().clear();
//            venteRepository.saveAndFlush(venteToSave);
//
//        } else {
//            // 1. On intercepte le client AVANT de faire le toEntity global pour éviter de créer un objet transient inutile
//            Client clientPersistant = null;
//            if (dto.getClient() != null && dto.getClient().getId() != null) {
//            clientPersistant = clientRepository.findById(dto.getClient().getId())
//            .orElseThrow(() -> new EntityNotFoundException("Client introuvable", ErrorCodes.CLIENT_NOT_FOUND));
//            }
//
//            // 2. On transforme le DTO en entité
//            venteToSave = VenteDto.toEntity(dto);
//
//            // 3. On applique explicitement le client persistant managé par Hibernate
//            venteToSave.setClient(clientPersistant);
//        }




//            // =========================================================================
//            // 🛡️ SÉCURITÉ : BLOCAGE SI LES QUANTITÉS OU LES ARTICLES ONT CHANGÉ
//            // =========================================================================
//            int nbrNouvellesLignes = dto.getLigneVentes() != null ? dto.getLigneVentes().size() : 0;
//
//            // 1. Vérification sur le nombre global de lignes/articles
//            if (anciennesLignesMap.size() != nbrNouvellesLignes) {
//                throw new InvalidOperationException(
//                        "Modification impossible : Le nombre d'articles de la vente ne correspond plus à la commande initiale.",
//                        ErrorCodes.VENTE_NON_MODIFIABLE
//                );
//            }
//
//            // 2. Vérification de la correspondance exacte des quantités pour chaque article
//            if (dto.getLigneVentes() != null) {
//                for (LigneVenteDto ligDto : dto.getLigneVentes()) {
//                    Integer idArticle = ligDto.getArticle().getId();
//                    BigDecimal nouvelleQte = ligDto.getQuantite();
//
//                    // Si l'article n'existait pas ou si la quantité a bougé, on lève une exception
//                    if (!anciennesLignesMap.containsKey(idArticle)) {
//                        throw new InvalidOperationException(
//                                "Modification impossible : L'article ID " + idArticle + " ne faisait pas partie de la commande initiale.",
//                                ErrorCodes.VENTE_NON_MODIFIABLE
//                        );
//                    }
//
//                    BigDecimal ancienneQte = anciennesLignesMap.get(idArticle);
//                    if (nouvelleQte.compareTo(ancienneQte) != 0) {
//                        throw new InvalidOperationException(
//                                "Modification impossible : Les quantités facturées ne peuvent pas être modifiées par rapport à la commande d'origine.",
//                                ErrorCodes.VENTE_NON_MODIFIABLE
//                        );
//                    }
//                }
//            }
//            // =========================================================================


//    @Override
//    @Transactional
//    public VenteDto save(VenteDto dto) {
//        validateVente(dto);
//
//        // On récupère l'ancienne vente si c'est une mise à jour
//        Map<Integer, BigDecimal> anciennesLignesMap = new HashMap<>();
//        Vente venteToSave;
//
//        if (dto.getId() != null) {
//            venteToSave = venteRepository.findByIdWithLignes(dto.getId())
//                    .orElseThrow(() -> new EntityNotFoundException("Vente introuvable", ErrorCodes.VENTE_NOT_FOUND));
//
////            // =========================================================================
////            // 🛡️ NOUVELLE SÉCURITÉ : BLOCAGE SI LA VENTE EST ASSOCIÉE À UNE COMMANDE
////            // =========================================================================
////            if (venteToSave.getCodeCommandeClient() != null && !venteToSave.getCodeCommandeClient().trim().isEmpty()) {
////                throw new InvalidOperationException(
////                        "Impossible de modifier cette vente car elle est directement liée à la commande client ["
////                                + venteToSave.getCodeCommandeClient() + "].",
////                        ErrorCodes.COMMANDE_CLIENT_NON_MODIFIABLE
////                );
////            }
////            // =========================================================================
//
//
//            // On remplit la Map : ID Article -> Quantité
//            if (venteToSave.getLigneVentes() != null) {
//                venteToSave.getLigneVentes().forEach(lig ->
//                        anciennesLignesMap.put(lig.getArticle().getId(), lig.getQuantite())
//                );
//            }
//
//            // On met à jour les infos générales
//            venteToSave.setCode(dto.getCode());
//            venteToSave.setCodeCommandeClient(dto.getCodeCommandeClient());
//            venteToSave.setPaymentType(dto.getPaymentType());
//            venteToSave.setDateVente(dto.getDateVente());
//
//            // On nettoie les anciennes lignes pour Hibernate
//            venteToSave.getLigneVentes().clear();
//            venteRepository.saveAndFlush(venteToSave);
//        } else {
//
////            // =========================================================================
////            // 🛡️ SÉCURITÉ : BLOCAGE SI LA COMMANDE ASSOCIÉE EST DÉJÀ CONFIRMÉE
////            // =========================================================================
////            if (dto.getCodeCommandeClient() != null && !dto.getCodeCommandeClient().trim().isEmpty()) {
////                Optional<CommandeClient> commandeOpt = commandeClientRepository.findCommandeClientByCode(dto.getCodeCommandeClient());
////                if (commandeOpt.isPresent()) {
////                    CommandeClient commandeClient = commandeOpt.get();
////
////                    // Si l'état de la commande est CONFIRMEE (ou même déjà VENDUE / LIVREE), on interdit la modification
////                    if (EEtatCommande.CONFIRMEE.equals(commandeClient.getEtatCommande())) {
////                        throw new InvalidOperationException(
////                                "Impossible de modifier cette vente car la commande associée [" + dto.getCodeCommandeClient() + "] est déjà CONFIRMÉE.",
////                                ErrorCodes.COMMANDE_CLIENT_NON_MODIFIABLE
////                        );
////                    }
////                }
////            }
////            // =========================================================================
//
//            venteToSave = VenteDto.toEntity(dto);
//
//            // 🛡️ SÉCURITÉ ANTI-TRANSIENT HIBERNATE :
//            // Si un client est présent dans le DTO lors d'une création de vente directe,
//            // on s'assure d'aller chercher sa version persistante en BDD.
//            if (dto.getClient() != null && dto.getClient().getId() != null) {
//                Client clientPersistant = clientRepository.findById(dto.getClient().getId())
//                        .orElseThrow(() -> new EntityNotFoundException("Client introuvable", ErrorCodes.CLIENT_NOT_FOUND));
//                venteToSave.setClient(clientPersistant);
//            }
//        }
//
//        // Préparer la liste des nouvelles lignes pour la sauvegarde
//        if (dto.getLigneVentes() != null) {
//            if (venteToSave.getLigneVentes() == null) {
//                venteToSave.setLigneVentes(new ArrayList<>());
//            }
//
//            for (LigneVenteDto ligDto : dto.getLigneVentes()) {
//                LigneVente lig = LigneVenteDto.toEntity(ligDto);
//                lig.setId(null);
//                lig.setVente(venteToSave);
//                lig.setIdEntreprise(dto.getIdEntreprise());
//                venteToSave.getLigneVentes().add(lig);
//
//                // LOGIQUE DIFFÉRENTIELLE DES STOCKS
//                Integer articleId = ligDto.getArticle().getId();
//                BigDecimal nouvelleQte = ligDto.getQuantite();
//
//                if (anciennesLignesMap.containsKey(articleId)) {
//                    BigDecimal ancienneQte = anciennesLignesMap.get(articleId);
//                    int comparaison = nouvelleQte.compareTo(ancienneQte);
//
//                    if (comparaison > 0) {
//                        BigDecimal diff = nouvelleQte.subtract(ancienneQte);
//                        modifierStockIndividuel(lig, diff, ETypeMvtStock.CORRECTION_NEG_VENTE_AUG);
//                    }
//                    else if (comparaison < 0) {
//                        BigDecimal diff = ancienneQte.subtract(nouvelleQte);
//                        modifierStockIndividuel(lig, diff, ETypeMvtStock.CORRECTION_POS_VENTE_RED);
//                    }
//                    anciennesLignesMap.remove(articleId);
//                } else {
//                    // Cas : Nouvelle ligne ajoutée
//                    modifierStockIndividuel(lig, nouvelleQte, ETypeMvtStock.SORTIE_VTE);
//                }
//            }
//        }
//
//        // GÉRER LES SUPPRESSIONS (Articles restants dans la Map)
//        anciennesLignesMap.forEach((idArt, qteInitiale) -> {
//            LigneVente ligneSupprimee = new LigneVente();
//            ligneSupprimee.setArticle(articleRepository.findById(idArt).orElse(null));
//            ligneSupprimee.setQuantite(qteInitiale);
//            ligneSupprimee.setIdEntreprise(dto.getIdEntreprise());
//
//            updateMvtStockAnnulation(ligneSupprimee);
//        });
//
//        // Sauvegarde de la vente principale
//        Vente savedVente = venteRepository.saveAndFlush(venteToSave);
//
//        // =========================================================================
//        // 🟢 NOUVELLE LOGIQUE : TRAITEMENT DE LA COMMANDE CLIENT LIÉE
//        // =========================================================================
//        if (dto.getCodeCommandeClient() != null && !dto.getCodeCommandeClient().trim().isEmpty()) {
//            // On cherche la commande par son code unique en base de données
//            Optional<CommandeClient> commandeOpt = commandeClientRepository.findCommandeClientByCode(dto.getCodeCommandeClient());
//
//            if (commandeOpt.isPresent()) {
//                CommandeClient commandeClient = commandeOpt.get();
//                // Mise à jour de l'état
//                commandeClient.setEtatCommande(EEtatCommande.VENDUE);
//                // Sauvegarde de la modification de la commande
//                commandeClientRepository.save(commandeClient);
//                log.info("✅ La commande client avec le code {} a été passée à l'état VENDUE.", dto.getCodeCommandeClient());
//            } else {
//                log.warn("⚠️ Code commande client '{}' fourni mais aucune commande correspondante en BDD.", dto.getCodeCommandeClient());
//            }
//        }
//        // =========================================================================
//
//        return VenteDto.fromEntity(savedVente);
//    }





//    @Override
//    @Transactional
//    public VenteDto save(VenteDto dto) {
//        validateVente(dto);
//
//        // On récupère l'ancienne vente si c'est une mise à jour
//        Map<Integer, BigDecimal> anciennesLignesMap = new HashMap<>();
//        Vente venteToSave;
//
//        if (dto.getId() != null) {
//            venteToSave = venteRepository.findByIdWithLignes(dto.getId())
//                    .orElseThrow(() -> new EntityNotFoundException("Vente introuvable", ErrorCodes.VENTE_NOT_FOUND));
//
//            // On remplit la Map : ID Article -> Quantité
//            if (venteToSave.getLigneVentes() != null) {
//                venteToSave.getLigneVentes().forEach(lig ->
//                        anciennesLignesMap.put(lig.getArticle().getId(), lig.getQuantite())
//                );
//            }
//
//            // On met à jour les infos générales
//            venteToSave.setCode(dto.getCode());
//            venteToSave.setCodeCommandeClient(dto.getCodeCommandeClient());
//            venteToSave.setPaymentType(dto.getPaymentType());
//            venteToSave.setDateVente(dto.getDateVente());
////            venteToSave.setIdEntreprise(dto.getIdEntreprise());
//
//            // Au lieu de supprimer manuellement avec le repository, on vide la collection Java pour éviter les conflits d'état
//            // orphanRemoval = true dans l'entité s'occupera de la suppression en base
//            // On nettoie les anciennes lignes pour Hibernate
//            venteToSave.getLigneVentes().clear();
//            venteRepository.saveAndFlush(venteToSave);
//        } else {
//            venteToSave = VenteDto.toEntity(dto);
//        }
//
//        // Préparer la liste des nouvelles lignes pour la sauvegarde
//        if (dto.getLigneVentes() != null) {
//            if (venteToSave.getLigneVentes() == null) {
//                venteToSave.setLigneVentes(new ArrayList<>());
//            }
//
//            for (LigneVenteDto ligDto : dto.getLigneVentes()) {
//                LigneVente lig = LigneVenteDto.toEntity(ligDto);
//                lig.setId(null);
//                lig.setVente(venteToSave);
//                lig.setIdEntreprise(dto.getIdEntreprise());
//                venteToSave.getLigneVentes().add(lig);
//
//                // LOGIQUE DIFFÉRENTIELLE DES STOCKS
//                Integer articleId = ligDto.getArticle().getId();
//                BigDecimal nouvelleQte = ligDto.getQuantite();
//
//                if (anciennesLignesMap.containsKey(articleId)) {
//                    // Cas : Mise à jour de quantité
//                    BigDecimal ancienneQte = anciennesLignesMap.get(articleId);
//                    int comparaison = nouvelleQte.compareTo(ancienneQte);
//
//                    if (comparaison > 0) {
//                        // Augmentation : on sort la différence
//                        BigDecimal diff = nouvelleQte.subtract(ancienneQte);
//                        modifierStockIndividuel(lig, diff, ETypeMvtStock.CORRECTION_NEG_VENTE_AUG);
//                    }
//                    else if (comparaison < 0) {
//                        // Diminution : on rentre la différence (valeur absolue)
//                        BigDecimal diff = ancienneQte.subtract(nouvelleQte);
//                        modifierStockIndividuel(lig, diff, ETypeMvtStock.CORRECTION_POS_VENTE_RED);
//                    }
//                        // Si inférieur ou égal, on ne fait rien.
//
//                    // On retire de la map pour identifier ce qui reste à supprimer à la fin
//                    anciennesLignesMap.remove(articleId);
//                } else {
//                    // Cas : Nouvelle ligne ajoutée
//                    modifierStockIndividuel(lig, nouvelleQte, ETypeMvtStock.SORTIE_VTE);
//                }
//            }
//        }
//
//        // GÉRER LES SUPPRESSIONS (Articles restants dans la Map)
//        anciennesLignesMap.forEach((idArt, qteInitiale) -> {
//            // On simule une ligne pour la restitution du stock
//            LigneVente ligneSupprimee = new LigneVente();
//            ligneSupprimee.setArticle(articleRepository.findById(idArt).orElse(null));
//            ligneSupprimee.setQuantite(qteInitiale);
//            ligneSupprimee.setIdEntreprise(dto.getIdEntreprise());
//
//            updateMvtStockAnnulation(ligneSupprimee);
//        });
//
//        Vente savedVente = venteRepository.saveAndFlush(venteToSave);
//
//
//        return VenteDto.fromEntity(savedVente);
//    }





//    @Override
//    @Transactional
//    public VenteDto save(VenteDto dto) {
//        validateVente(dto); // Votre validation existante
//
//        // On ne vérifie le stock que pour les nouvelles quantités demandées
//        verifierStockDisponible(dto);
//
//        Vente venteToSave;
//
//        if (dto.getId() != null) {
//            // --- CAS MISE À JOUR ---
//            // On force le chargement des lignes avec FETCH
//            venteToSave = (Vente) venteRepository.findByIdWithLignes(dto.getId())
//                    .orElseThrow(() -> new EntityNotFoundException("Vente introuvable", ErrorCodes.VENTE_NOT_FOUND));
//
//            // 1. COMPENSATION DES STOCKS (Annulation des anciens stocks)
//            if (venteToSave.getLigneVentes() != null) {
//                // Comme on a utilisé JOIN FETCH, cette liste est chargée et accessible
//                venteToSave.getLigneVentes().forEach(this::updateMvtStockAnnulation);
//            }
//
//            // 2. MISE À JOUR DES INFOS GÉNÉRALES
//            venteToSave.setCode(dto.getCode());
//            venteToSave.setPaymentType(dto.getPaymentType());
//            venteToSave.setDateVente(dto.getDateVente());
//            venteToSave.setIdEntreprise(dto.getIdEntreprise());
//
//            // 3. NETTOYAGE DES LIGNES
//            // On supprime physiquement en BDD
////            ligneVenteRepository.deleteByVenteId(dto.getId());
//            // Au lieu de supprimer manuellement avec le repository, on vide la collection
//            // orphanRemoval = true dans l'entité s'occupera de la suppression en base
//            // On vide la collection Java pour éviter les conflits d'état
//            venteToSave.getLigneVentes().clear();
//
//            // On synchronise pour que Hibernate sache que la vente est "propre"
//            venteRepository.saveAndFlush(venteToSave);
//
//        } else {
//            // --- CAS CRÉATION ---
//            venteToSave = VenteDto.toEntity(dto);
//            if (venteToSave.getLigneVentes() == null) {
//                venteToSave.setLigneVentes(new ArrayList<>());
//            }
//        }
//
//        // 4. ATTACHEMENT DES NOUVELLES LIGNES
//        if (dto.getLigneVentes() != null) {
//            // Initialiser la liste si elle est nulle pour éviter un NPE
//            if (venteToSave.getLigneVentes() == null) {
//                venteToSave.setLigneVentes(new ArrayList<>());
//            }
//
//            for (LigneVenteDto ligDto : dto.getLigneVentes()) {
//                LigneVente lig = LigneVenteDto.toEntity(ligDto);
//                lig.setId(null); // <--- FORCE LE NULL : C'est la clé pour éviter l'Optimistic Locking
//                lig.setVente(venteToSave); // Lien vers le parent
//                lig.setIdEntreprise(dto.getIdEntreprise()); // Important pour vos filtres
//
//                // On ajoute à la collection du parent pour que le Cascade/Save fonctionne
//                venteToSave.getLigneVentes().add(lig);
//            }
//        }
//
//        // 5. SAUVEGARDE FINALE ET NOUVEAUX STOCKS
//        // On fait un saveAndFlush pour forcer l'écriture des LigneVente en BDD
//        Vente savedVente = venteRepository.saveAndFlush(venteToSave);
//
//        // 6. MISE À JOUR DES STOCKS
//        if (savedVente.getLigneVentes() != null) {
//            for (LigneVente lig : savedVente.getLigneVentes()) {
//                // Sécurité : on s'assure que l'article est bien présent avant de mouvementer le stock
//                if (lig.getArticle() == null && lig.getArticle().getId() != null) {
//                    // Recharger l'article si nécessaire ou s'assurer que le mapper le fait
//                    lig.setArticle(articleRepository.findById(lig.getArticle().getId()).orElse(null));
//                }
//
//                // On vérifie que la quantité n'est pas nulle
//                if (lig.getQuantite() != null && lig.getQuantite().compareTo(BigDecimal.ZERO) != 0) {
//                    updateMvtStock(lig);
//                } else {
//                    log.warn("Ligne de vente ignorée pour le stock : Quantité nulle pour l'article {}",
//                            lig.getArticle() != null ? lig.getArticle().getId() : "Inconnu");
//                }
//            }
//        }
//
//        return VenteDto.fromEntity(savedVente);
//    }



//    @Override
//    public void delete(Integer id) {
//        if (id == null) {
//            log.error("Vente ID is NULL");
//            return;
//        }
//
//        // 1. Récupérer les lignes de vente pour les traiter (mouvements de stock, etc.)
//        List<LigneVente> ligneVentes = ligneVenteRepository.findAllByVenteId(id);
//
//        // CORRECTION LOGIQUE :
//        // Votre ancien code faisait : if(ligneVentes.isEmpty()) { throw Exception }
//        // Ce qui veut dire : "Je refuse de supprimer s'il n'y a PAS de lignes".
//        // Et votre message disait : "Impossible de supprimer avec lignes".
//        // C'était totalement inversé.
//
//        // 2. Supprimer les lignes de vente manuellement si la cascade n'est pas configurée
//        if (!ligneVentes.isEmpty()) {
//            ligneVenteRepository.deleteAll(ligneVentes);
//        }
//
//        // 3. Enfin, supprimer la vente
//        venteRepository.deleteById(id);
//    }


//    @Override
//    public void delete(Integer id) {
//        if (id == null) {
//            log.error("Vente ID is NULL");
//            return;
//        }
//        List<LigneVente> ligneVentes = ligneVenteRepository.findAllByVenteId(id);
//        if (!ligneVentes.isEmpty()) {
//            throw new InvalidOperationException("Impossible de supprimer une vente avec ligne de vente",
//                    ErrorCodes.VENTE_ALREADY_IN_USE);
//        }
//        venteRepository.deleteById(id);
//    }
