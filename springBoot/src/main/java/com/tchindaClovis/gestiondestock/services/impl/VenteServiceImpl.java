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
        // -------------------------------------------------------------------------
        // 1. VALIDATION INITIALE DU DTO
        // -------------------------------------------------------------------------
        // Vérifie que les données de base de la vente sont valides (champs obligatoires, structures, etc.)
        validateVente(dto);

        // -------------------------------------------------------------------------
        // 2. BLOCAGE / CONTRÔLE DE SÉCURITÉ SI LA VENTE EST ISSUE D'UNE COMMANDE CLIENT
        // -------------------------------------------------------------------------
        // Si la vente fait référence à une commande client existante, on s'assure qu'aucun
        // article ni aucune quantité n'a été altéré par rapport à la commande d'origine.
        if (dto.getCodeCommandeClient() != null && !dto.getCodeCommandeClient().trim().isEmpty()) {

            // Récupération de la commande initiale en base de données pour comparaison
            CommandeClient commandeInitiale = commandeClientRepository.findByCode(dto.getCodeCommandeClient())
                    .orElseThrow(() -> new EntityNotFoundException(
                            "Commande client introuvable avec le code : " + dto.getCodeCommandeClient(),
                            ErrorCodes.COMMANDE_CLIENT_NOT_FOUND
                    ));

            // Construction d'une Map [ID Article -> Quantité Commandée] pour une recherche rapide en O(1)
            Map<Integer, BigDecimal> lignesCommandeMap = new HashMap<>();
            if (commandeInitiale.getLigneCommandeClients() != null) {
                commandeInitiale.getLigneCommandeClients().forEach(ligCmd -> {
                    if (ligCmd.getArticle() != null) {
                        lignesCommandeMap.put(ligCmd.getArticle().getId(), ligCmd.getQuantite());
                    }
                });
            }

            int nbrNouvellesLignes = dto.getLigneVentes() != null ? dto.getLigneVentes().size() : 0;

            // ÉTAPE 2.1 : Vérification de la cohérence du nombre total d'articles
            if (lignesCommandeMap.size() != nbrNouvellesLignes) {
                throw new InvalidOperationException(
                        "Modification impossible : Le nombre d'articles de la vente (" + nbrNouvellesLignes +
                                ") ne correspond pas à la commande initiale (" + lignesCommandeMap.size() + ").",
                        ErrorCodes.VENTE_NON_MODIFIABLE
                );
            }

            // ÉTAPE 2.2 : Vérification article par article (Présence et quantité exacte)
            if (dto.getLigneVentes() != null) {
                for (LigneVenteDto ligDto : dto.getLigneVentes()) {
                    if (ligDto.getArticle() == null || ligDto.getArticle().getId() == null) {
                        throw new InvalidEntityException("Un article de la vente n'est pas valide ou ne possède pas d'identifiant.");
                    }

                    Integer idArticle = ligDto.getArticle().getId();
                    BigDecimal qteFacturee = ligDto.getQuantite();

                    // L'article fait-il partie de la commande initiale ?
                    if (!lignesCommandeMap.containsKey(idArticle)) {
                        throw new InvalidOperationException(
                                "Modification impossible : L'article ID " + idArticle + " ne figure pas dans la commande d'origine " + dto.getCodeCommandeClient() + ".",
                                ErrorCodes.VENTE_NON_MODIFIABLE
                        );
                    }

                    // La quantité facturée correspond-elle exactement à la quantité commandée ?
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

        // -------------------------------------------------------------------------
        // 3. PRÉPARATION ET GESTION DE L'ENTITÉ VENTE (CRÉATION VS MISE À JOUR)
        // -------------------------------------------------------------------------
        // Map stockant les anciennes lignes de la vente [ID Article -> Quantité]
        // afin de calculer les ajustements différentiels sur les mouvements de stock.
        Map<Integer, BigDecimal> anciennesLignesMap = new HashMap<>();
        Vente venteToSave;

        if (dto.getId() != null) {
            // --- CAS A : MISE À JOUR D'UNE VENTE EXISTANTE ---
            // Charger la vente avec ses lignes de vente associées
            venteToSave = venteRepository.findByIdWithLignes(dto.getId())
                    .orElseThrow(() -> new EntityNotFoundException("Vente introuvable", ErrorCodes.VENTE_NOT_FOUND));

            // Mémoriser l'état initial des lignes de vente pour le calcul de stock ultérieur
            if (venteToSave.getLigneVentes() != null) {
                venteToSave.getLigneVentes().forEach(lig ->
                        anciennesLignesMap.put(lig.getArticle().getId(), lig.getQuantite())
                );
            }

            // Sécurité : Conserver le codeCommandeClient existant en BDD si le DTO l'envoie vide
            if (venteToSave.getCodeCommandeClient() != null && !venteToSave.getCodeCommandeClient().trim().isEmpty()) {
                if (dto.getCodeCommandeClient() == null || dto.getCodeCommandeClient().trim().isEmpty()) {
                    dto.setCodeCommandeClient(venteToSave.getCodeCommandeClient());
                }
            }

            // Mettre à jour les propriétés simples de la vente
            venteToSave.setCode(dto.getCode());

            /*
             * 📌 ENREGISTREMENT SUR L'ENTITÉ VENTE (1/2) :
             * Affectation du codeCommandeClient au POJO Vente lors d'une MISE À JOUR.
             */
            venteToSave.setCodeCommandeClient(dto.getCodeCommandeClient());
            venteToSave.setPaymentType(dto.getPaymentType());
            venteToSave.setDateVente(dto.getDateVente());

            // Mise à jour partielle et ciblée de l'entité Client associée
            if (dto.getClient() != null && dto.getClient().getId() != null) {
                Client clientExistant = clientRepository.findById(dto.getClient().getId())
                        .orElseThrow(() -> new EntityNotFoundException("Client introuvable", ErrorCodes.CLIENT_NOT_FOUND));

                if (dto.getClient().getNom() != null) clientExistant.setNom(dto.getClient().getNom());
                if (dto.getClient().getStatut() != null) clientExistant.setStatut(dto.getClient().getStatut());
                if (dto.getClient().getNumTel() != null) clientExistant.setNumTel(dto.getClient().getNumTel());
                if (dto.getClient().getEmail() != null) clientExistant.setEmail(dto.getClient().getEmail());

                if (dto.getClient().getAdresse() != null) {
                    if (clientExistant.getAdresse() == null) {
                        clientExistant.setAdresse(new Adresse());
                    }
                    if (dto.getClient().getAdresse().getAdresse1() != null) {
                        clientExistant.getAdresse().setAdresse1(dto.getClient().getAdresse().getAdresse1());
                    }
                }

                // Sauvegarder séparément les modifications apportées au client
                clientRepository.save(clientExistant);
                venteToSave.setClient(clientExistant);
            }

            // Vider la collection d'anciennes lignes pour que JPA gère le remplacement proprement
            venteToSave.getLigneVentes().clear();
            venteRepository.saveAndFlush(venteToSave);

        } else {
            // --- CAS B : CRÉATION D'UNE NOUVELLE VENTE ---

            // Charger le client persistant (managé par Hibernate) pour éviter les erreurs d'objets transients
            Client clientPersistant = null;
            if (dto.getClient() != null && dto.getClient().getId() != null) {
                clientPersistant = clientRepository.findById(dto.getClient().getId())
                        .orElseThrow(() -> new EntityNotFoundException("Client introuvable", ErrorCodes.CLIENT_NOT_FOUND));
            }

            /*
             * 📌 ENREGISTREMENT SUR L'ENTITÉ VENTE (2/2) :
             * VenteDto.toEntity(dto) copie automatiquement dto.getCodeCommandeClient()
             * vers l'instance 'venteToSave' de type Vente.
             */
            venteToSave = VenteDto.toEntity(dto);

            // Assigner le client persistant
            venteToSave.setClient(clientPersistant);
        }

        // -------------------------------------------------------------------------
        // 4. TRAITEMENT DES NOUVELLES LIGNES DE VENTE ET IMPACT SUR LE STOCK
        // -------------------------------------------------------------------------
        if (dto.getLigneVentes() != null) {
            if (venteToSave.getLigneVentes() == null) {
                venteToSave.setLigneVentes(new ArrayList<>());
            }

            for (LigneVenteDto ligDto : dto.getLigneVentes()) {
                LigneVente lig = LigneVenteDto.toEntity(ligDto);
                lig.setId(null); // S'assurer que JPA génère un nouvel identifiant
                lig.setVente(venteToSave);
                lig.setIdEntreprise(dto.getIdEntreprise());
                venteToSave.getLigneVentes().add(lig);

                // LOGIQUE DIFFÉRENTIELLE SUR LES STOCKS :
                Integer articleId = ligDto.getArticle().getId();
                BigDecimal nouvelleQte = ligDto.getQuantite();

                // Vérifie si l'article existait déjà dans la Map des anciennes lignes de la vente
                if (anciennesLignesMap.containsKey(articleId)) {
                    // L'article existait déjà : on ajuste la différence (Augmentation ou Réduction)
                    BigDecimal ancienneQte = anciennesLignesMap.get(articleId);
                    int comparaison = nouvelleQte.compareTo(ancienneQte);

                    // La nouvelle quantité est supérieure à l'ancienne quantité
                    if (comparaison > 0) {
                        // La quantité a augmenté -> Déstockage complémentaire
                        BigDecimal diff = nouvelleQte.subtract(ancienneQte);
                        modifierStockIndividuel(lig, diff, ETypeMvtStock.CORRECTION_NEG_VENTE_AUG);
                    }
                    // La nouvelle quantité est inférieure à l'ancienne quantité
                    else if (comparaison < 0) {
                        // La quantité a diminué -> Réintégration en stock
                        BigDecimal diff = ancienneQte.subtract(nouvelleQte);
                        modifierStockIndividuel(lig, diff, ETypeMvtStock.CORRECTION_POS_VENTE_RED);
                    }
                    // Retirer de la Map pour signaler que cet article est traité
                    anciennesLignesMap.remove(articleId);
                } else {
                    // -------------------------------------------------------------------------
                    // NOUVEL ARTICLE AJOUTÉ À LA VENTE : AIGUILLAGE CONDITIONNEL
                    // -------------------------------------------------------------------------
                    // On vérifie si la vente est liée à une commande client
                    boolean estIssueDeCommandeClient = venteToSave.getCodeCommandeClient() != null
                            && !venteToSave.getCodeCommandeClient().trim().isEmpty();

                    if (estIssueDeCommandeClient) {
                        // CAS 1 : Vente issue d'une commande client -> Mouvement spécifique CMD
                        modifierStockIndividuelCMD(lig, nouvelleQte, ETypeMvtStock.SORTIE_VTE_CMD);
                    } else {
                        // CAS 2 : Vente directe / standard -> Sortie de stock classique
                        modifierStockIndividuel(lig, nouvelleQte, ETypeMvtStock.SORTIE_VTE);
                    }
                }
            }
        }

        // -------------------------------------------------------------------------
        // 5. TRAITEMENT DES LIGNES SUPPRIMÉES (RESTANTES DANS LA MAP)
        // -------------------------------------------------------------------------
        // Si des articles étaient présents dans l'ancienne vente mais absents de la nouvelle,
        // on annule les mouvements de stock associés (réintégration du stock).
        anciennesLignesMap.forEach((idArt, qteInitiale) -> {
            LigneVente ligneSupprimee = new LigneVente();
            ligneSupprimee.setArticle(articleRepository.findById(idArt).orElse(null));
            ligneSupprimee.setQuantite(qteInitiale);
            ligneSupprimee.setVente(venteToSave); // Assure la transmission du codeCommandeClient
            ligneSupprimee.setIdEntreprise(dto.getIdEntreprise());

            // NOUVEL ARTICLE AJOUTÉ À LA VENTE : AIGUILLAGE CONDITIONNEL
            // -------------------------------------------------------------------------
            // On vérifie si la vente est liée à une commande client
            boolean estIssueDeCommandeClient = venteToSave.getCodeCommandeClient() != null
                    && !venteToSave.getCodeCommandeClient().trim().isEmpty();

            if (estIssueDeCommandeClient) {
                // CAS 1 : Vente issue d'une commande client -> Mouvement spécifique CMD
                updateMvtStockAnnulationCMD(ligneSupprimee);
            } else {
                // CAS 2 : Vente directe / standard -> Sortie de stock classique
                updateMvtStockAnnulation(ligneSupprimee);
            }
        });

        // -------------------------------------------------------------------------
        // 6. PERSISTANCE FINALE DE LA VENTE ET DES LIGNES EN BDD
        // -------------------------------------------------------------------------
        /*
         * 💾 C'EST ICI QUE LE "codeCommandeClient" EST ÉCRIT EN BASE DE DONNÉES DANS LA TABLE 'VENTE'.
         * 'saveAndFlush' exécute immédiatement le SQL (INSERT ou UPDATE) sur la table Vente.
         */
        Vente savedVente = venteRepository.saveAndFlush(venteToSave);

        // -------------------------------------------------------------------------
        // 7. MISE À JOUR DE L'ÉTAT DE LA COMMANDE CLIENT ASSOCIÉE
        // -------------------------------------------------------------------------
        // Si la vente provient d'une commande client, la commande passe au statut VENDUE.
        if (dto.getCodeCommandeClient() != null && !dto.getCodeCommandeClient().trim().isEmpty()) {
            Optional<CommandeClient> commandeOpt = commandeClientRepository.findByCode(dto.getCodeCommandeClient());

            if (commandeOpt.isPresent()) {
                CommandeClient commandeClient = commandeOpt.get();
                commandeClient.setEtatCommande(EEtatCommande.VENDUE);

                // Persistance de l'état VENDUE sur la commande client
                commandeClientRepository.save(commandeClient);
                log.info("✅ La commande client avec le code {} a été passée à l'état VENDUE.", dto.getCodeCommandeClient());
            } else {
                log.warn("⚠️ Code commande client '{}' fourni mais aucune commande correspondante en BDD.", dto.getCodeCommandeClient());
            }
        }

        // Mapping final de l'entité enregistrée vers le DTO de retour
        return VenteDto.fromEntity(savedVente);
    }



    /**
     * Méthode utilitaire permettant de générer et d'enregistrer un mouvement de stock spécifique
     * (par exemple : ajustement différentiel lors de la modification ou création d'une vente).
     *
     * @param lig  La ligne de vente concernée (contient l'article, la vente parente et l'entreprise)
     * @param qte  La quantité d'impact à appliquer sur le stock (ex: différence de quantité ou quantité totale)
     * @param type Le type exact de mouvement de stock (SORTIE_VTE, CORRECTION_NEG_VENTE_AUG, etc.)
     */
    private void modifierStockIndividuel(LigneVente lig, BigDecimal qte, ETypeMvtStock type) {

        // -------------------------------------------------------------------------
        // 1. VALIDATION DE LA QUANTITÉ
        // -------------------------------------------------------------------------
        // Si la quantité à impacter est nulle ou négative, aucun ajustement de stock n'est nécessaire.
        if (qte.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }

        // -------------------------------------------------------------------------
        // 2. CONSTRUCTION DU DTO DE MOUVEMENT DE STOCK (MvtStockDto)
        // -------------------------------------------------------------------------
        // On prépare l'objet d'audit de stock avec toutes les métadonnées requises.
        MvtStockDto mvt = MvtStockDto.builder()
                // Link vers l'article impacté
                .article(ArticleDto.fromEntity(lig.getArticle()))
                // Horodatage précis du mouvement
                .dateMvt(Instant.now())
                // Qualification de l'opération (Sortie classique, Correction positive/négative)
                .typeMvt(type)
                // Origine du mouvement (dans ce cas, une opération de vente)
                .sourceMvt(ESourceMvtStock.VENTE)
                // Quantité exacte à appliquer
                .quantite(qte)
                // 📌 TRAÇABILITÉ : Référence du document source (ex: N° de ticket/facture de vente)
//                .codeSource(lig.getVente().getCode())
                .codeSource(lig.getVente() != null ? lig.getVente().getCode() : null)
                // 📌 TRANSMISSION DU CODE COMMANDE CLIENT
                .codeCommandeClient(lig.getVente() != null ? lig.getVente().getCodeCommandeClient() : null)
                // Isolation multi-entreprise (Tenant ID)
                .idEntreprise(lig.getIdEntreprise())
                .build();

        // -------------------------------------------------------------------------
        // 3. AIGUILLAGE ET TRAITEMENT SELON LE TYPE DE MOUVEMENT
        // -------------------------------------------------------------------------
        // Selon la nature de l'opération (ajustement à la hausse, à la baisse ou déstockage standard),
        // on fait appel à la méthode métier dédiée dans 'mvtStockService'.

        if (type == ETypeMvtStock.CORRECTION_NEG_VENTE_AUG) {
            // CAS 1 : La quantité vendue a AUGMENTÉ lors de la modification de la vente.
            // On effectue une correction négative (déstockage complémentaire de la différence).
            mvtStockService.correctionStockNegVenteAug(mvt);

        } else if (type == ETypeMvtStock.CORRECTION_POS_VENTE_RED) {
            // CAS 2 : La quantité vendue a DIMINUÉ lors de la modification de la vente.
            // On effectue une correction positive (réintégration du surplus en stock).
            mvtStockService.correctionStockPosVenteRed1(mvt);

        } else {
            // CAS 3 : Création initiale d'une vente ou sortie standard.
            // Déstockage régulier de la quantité vendue.
            mvtStockService.sortieStockVte(mvt);
        }
    }


    //pour les vente issues de commande
    private void modifierStockIndividuelCMD(LigneVente lig, BigDecimal qte, ETypeMvtStock type) {
        if (qte.compareTo(BigDecimal.ZERO) <= 0) return;

        MvtStockDto mvt = MvtStockDto.builder()
                .article(ArticleDto.fromEntity(lig.getArticle()))
                .dateMvt(Instant.now())
                .typeMvt(type)
                .sourceMvt(ESourceMvtStock.VENTE_CMD)
                .quantite(qte)
//                .codeSource(lig.getVente().getCode()) // ON PASSE LE CODE ICI
                // 📌 TRAÇABILITÉ : Référence du document source (ex: N° de ticket/facture de vente)
                .codeSource(lig.getVente() != null ? lig.getVente().getCode() : null)
                // 📌 TRANSMISSION DU CODE COMMANDE CLIENT
                .codeCommandeClient(lig.getVente() != null ? lig.getVente().getCodeCommandeClient() : null)
                .idEntreprise(lig.getIdEntreprise())
                .build();

        if (type == ETypeMvtStock.CORRECTION_NEG_VENTE_AUG) {
            mvtStockService.correctionStockNegVenteAugCMD(mvt);
        } else  if (type == ETypeMvtStock.CORRECTION_POS_VENTE_RED){
            mvtStockService.correctionStockPosVenteRed1CMD(mvt);
        }else{
            mvtStockService.sortieStockVteCMD(mvt);
        }
    }

    /**
     * Méthode utilitaire permettant de traiter la réintégration en stock d'un article
     * lors de la suppression d'une ligne de vente (annulation ou retrait d'un produit).
     *
     * @param ligne La ligne de vente qui a été retirée de la commande/facture
     */
    private void updateMvtStockAnnulation(LigneVente ligne) {

        // -------------------------------------------------------------------------
        // 1. VÉRIFICATION DE SÉCURITÉ
        // -------------------------------------------------------------------------
        // Si la ligne ou la quantité associée est invalide/nulle, on n'exécute aucun mouvement.
        if (ligne == null || ligne.getQuantite() == null || ligne.getQuantite().compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }

        // -------------------------------------------------------------------------
        // 2. CONSTRUCTION DU DTO DE MOUVEMENT DE STOCK (MvtStockDto)
        // -------------------------------------------------------------------------
        // On instancie le DTO d'enregistrement pour réintégrer la quantité en stock.
        MvtStockDto mvtStockDto = MvtStockDto.builder()
                // Article dont la ligne a été supprimée
                .article(ArticleDto.fromEntity(ligne.getArticle()))
                // Horodatage précis de l'opération de réintégration
                .dateMvt(Instant.now())
                // Nature du mouvement : Entrée / Correction positive (pour restocker la quantité retirée)
                .typeMvt(ETypeMvtStock.ENTREE)
                // Origine du mouvement (Annulation liée à une opération de VENTE)
                .sourceMvt(ESourceMvtStock.VENTE)
                // Quantité à créditer de nouveau dans le stock disponible
                .quantite(ligne.getQuantite())
                // 📌 TRAÇABILITÉ : N° de ticket/code de vente de référence (avec garde contre NullPointer)
                .codeSource(ligne.getVente() != null ? ligne.getVente().getCode() : null)
                // 📌 TRANSMISSION DU CODE COMMANDE CLIENT : Utile si la vente dérive d'une commande
                .codeCommandeClient(ligne.getVente() != null ? ligne.getVente().getCodeCommandeClient() : null)
                // Multi-tenant / Référence entreprise
                .idEntreprise(ligne.getIdEntreprise())
                .build();

        // -------------------------------------------------------------------------
        // 3. PERSISTANCE ET RE-CRÉDIT DU STOCK
        // -------------------------------------------------------------------------
        // Appel du service métier pour incrémenter le stock physique et enregistrer le mouvement
        mvtStockService.entreeStock(mvtStockDto);
    }


    //pour les vente issues de commande
    private void updateMvtStockAnnulationCMD(LigneVente ligne) {
        MvtStockDto mvtStockDto = MvtStockDto.builder()
                .article(ArticleDto.fromEntity(ligne.getArticle()))
                .dateMvt(Instant.now())
                .typeMvt(ETypeMvtStock.ENTREE) // Ou CORRECTION_POS selon votre enum
                .sourceMvt(ESourceMvtStock.VENTE_CMD)
                .quantite(ligne.getQuantite()) // On remet la quantité initiale en stock
//                .codeSource(ligne.getVente().getCode()) // ON PASSE LE CODE ICI
                .codeSource(ligne.getVente() != null ? ligne.getVente().getCode() : null)
                .codeCommandeClient(ligne.getVente() != null ? ligne.getVente().getCodeCommandeClient() : null)
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





//
//    @Override
//    @Transactional
//    public VenteDto save(VenteDto dto) {
//        // -------------------------------------------------------------------------
//        // 1. VALIDATION INITIALE DU DTO
//        // -------------------------------------------------------------------------
//        // Vérifie que les données de base de la vente sont valides (champs obligatoires, structures, etc.)
//        validateVente(dto);
//
//        // -------------------------------------------------------------------------
//        // 2. BLOCAGE / CONTRÔLE DE SÉCURITÉ SI LA VENTE EST ISSUE D'UNE COMMANDE CLIENT
//        // -------------------------------------------------------------------------
//        // Si la vente fait référence à une commande client existante, on s'assure qu'aucun
//        // article ni aucune quantité n'a été altéré par rapport à la commande d'origine.
//        if (dto.getCodeCommandeClient() != null && !dto.getCodeCommandeClient().trim().isEmpty()) {
//
//            // Récupération de la commande initiale en base de données pour comparaison
//            CommandeClient commandeInitiale = commandeClientRepository.findByCode(dto.getCodeCommandeClient())
//                    .orElseThrow(() -> new EntityNotFoundException(
//                            "Commande client introuvable avec le code : " + dto.getCodeCommandeClient(),
//                            ErrorCodes.COMMANDE_CLIENT_NOT_FOUND
//                    ));
//
//            // Construction d'une Map [ID Article -> Quantité Commandée] pour une recherche rapide en O(1)
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
//            // ÉTAPE 2.1 : Vérification de la cohérence du nombre total d'articles
//            if (lignesCommandeMap.size() != nbrNouvellesLignes) {
//                throw new InvalidOperationException(
//                        "Modification impossible : Le nombre d'articles de la vente (" + nbrNouvellesLignes +
//                                ") ne correspond pas à la commande initiale (" + lignesCommandeMap.size() + ").",
//                        ErrorCodes.VENTE_NON_MODIFIABLE
//                );
//            }
//
//            // ÉTAPE 2.2 : Vérification article par article (Présence et quantité exacte)
//            if (dto.getLigneVentes() != null) {
//                for (LigneVenteDto ligDto : dto.getLigneVentes()) {
//                    if (ligDto.getArticle() == null || ligDto.getArticle().getId() == null) {
//                        throw new InvalidEntityException("Un article de la vente n'est pas valide ou ne possède pas d'identifiant.");
//                    }
//
//                    Integer idArticle = ligDto.getArticle().getId();
//                    BigDecimal qteFacturee = ligDto.getQuantite();
//
//                    // L'article fait-il partie de la commande initiale ?
//                    if (!lignesCommandeMap.containsKey(idArticle)) {
//                        throw new InvalidOperationException(
//                                "Modification impossible : L'article ID " + idArticle + " ne figure pas dans la commande d'origine " + dto.getCodeCommandeClient() + ".",
//                                ErrorCodes.VENTE_NON_MODIFIABLE
//                        );
//                    }
//
//                    // La quantité facturée correspond-elle exactement à la quantité commandée ?
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
//
//        // -------------------------------------------------------------------------
//        // 3. PRÉPARATION ET GESTION DE L'ENTITÉ VENTE (CRÉATION VS MISE À JOUR)
//        // -------------------------------------------------------------------------
//        // Map stockant les anciennes lignes de la vente [ID Article -> Quantité]
//        // afin de calculer les ajustements différentiels sur les mouvements de stock.
//        Map<Integer, BigDecimal> anciennesLignesMap = new HashMap<>();
//        Vente venteToSave;
//
//        if (dto.getId() != null) {
//            // --- CAS A : MISE À JOUR D'UNE VENTE EXISTANTE ---
//            // Charger la vente avec ses lignes de vente associées
//            venteToSave = venteRepository.findByIdWithLignes(dto.getId())
//                    .orElseThrow(() -> new EntityNotFoundException("Vente introuvable", ErrorCodes.VENTE_NOT_FOUND));
//
//            // Mémoriser l'état initial des lignes de vente pour le calcul de stock ultérieur
//            if (venteToSave.getLigneVentes() != null) {
//                venteToSave.getLigneVentes().forEach(lig ->
//                        anciennesLignesMap.put(lig.getArticle().getId(), lig.getQuantite())
//                );
//            }
//
//            // Sécurité : Conserver le codeCommandeClient existant en BDD si le DTO l'envoie vide
//            if (venteToSave.getCodeCommandeClient() != null && !venteToSave.getCodeCommandeClient().trim().isEmpty()) {
//                if (dto.getCodeCommandeClient() == null || dto.getCodeCommandeClient().trim().isEmpty()) {
//                    dto.setCodeCommandeClient(venteToSave.getCodeCommandeClient());
//                }
//            }
//
//            // Mettre à jour les propriétés simples de la vente
//            venteToSave.setCode(dto.getCode());
//
//            /*
//             * 📌 ENREGISTREMENT SUR L'ENTITÉ VENTE (1/2) :
//             * Affectation du codeCommandeClient au POJO Vente lors d'une MISE À JOUR.
//             */
//            venteToSave.setCodeCommandeClient(dto.getCodeCommandeClient());
//            venteToSave.setPaymentType(dto.getPaymentType());
//            venteToSave.setDateVente(dto.getDateVente());
//
//            // Mise à jour partielle et ciblée de l'entité Client associée
//            if (dto.getClient() != null && dto.getClient().getId() != null) {
//                Client clientExistant = clientRepository.findById(dto.getClient().getId())
//                        .orElseThrow(() -> new EntityNotFoundException("Client introuvable", ErrorCodes.CLIENT_NOT_FOUND));
//
//                if (dto.getClient().getNom() != null) clientExistant.setNom(dto.getClient().getNom());
//                if (dto.getClient().getStatut() != null) clientExistant.setStatut(dto.getClient().getStatut());
//                if (dto.getClient().getNumTel() != null) clientExistant.setNumTel(dto.getClient().getNumTel());
//                if (dto.getClient().getEmail() != null) clientExistant.setEmail(dto.getClient().getEmail());
//
//                if (dto.getClient().getAdresse() != null) {
//                    if (clientExistant.getAdresse() == null) {
//                        clientExistant.setAdresse(new Adresse());
//                    }
//                    if (dto.getClient().getAdresse().getAdresse1() != null) {
//                        clientExistant.getAdresse().setAdresse1(dto.getClient().getAdresse().getAdresse1());
//                    }
//                }
//
//                // Sauvegarder séparément les modifications apportées au client
//                clientRepository.save(clientExistant);
//                venteToSave.setClient(clientExistant);
//            }
//
//            // Vider la collection d'anciennes lignes pour que JPA gère le remplacement proprement
//            venteToSave.getLigneVentes().clear();
//            venteRepository.saveAndFlush(venteToSave);
//
//        } else {
//            // --- CAS B : CRÉATION D'UNE NOUVELLE VENTE ---
//
//            // Charger le client persistant (managé par Hibernate) pour éviter les erreurs d'objets transients
//            Client clientPersistant = null;
//            if (dto.getClient() != null && dto.getClient().getId() != null) {
//                clientPersistant = clientRepository.findById(dto.getClient().getId())
//                        .orElseThrow(() -> new EntityNotFoundException("Client introuvable", ErrorCodes.CLIENT_NOT_FOUND));
//            }
//
//            /*
//             * 📌 ENREGISTREMENT SUR L'ENTITÉ VENTE (2/2) :
//             * VenteDto.toEntity(dto) copie automatiquement dto.getCodeCommandeClient()
//             * vers l'instance 'venteToSave' de type Vente.
//             */
//            venteToSave = VenteDto.toEntity(dto);
//
//            // Assigner le client persistant
//            venteToSave.setClient(clientPersistant);
//        }
//
//        // -------------------------------------------------------------------------
//        // 4. TRAITEMENT DES NOUVELLES LIGNES DE VENTE ET IMPACT SUR LE STOCK
//        // -------------------------------------------------------------------------
//        if (dto.getLigneVentes() != null) {
//            if (venteToSave.getLigneVentes() == null) {
//                venteToSave.setLigneVentes(new ArrayList<>());
//            }
//
//            for (LigneVenteDto ligDto : dto.getLigneVentes()) {
//                LigneVente lig = LigneVenteDto.toEntity(ligDto);
//                lig.setId(null); // S'assurer que JPA génère un nouvel identifiant
//                lig.setVente(venteToSave);
//                lig.setIdEntreprise(dto.getIdEntreprise());
//                venteToSave.getLigneVentes().add(lig);
//
//                // LOGIQUE DIFFÉRENTIELLE SUR LES STOCKS :
//                Integer articleId = ligDto.getArticle().getId();
//                BigDecimal nouvelleQte = ligDto.getQuantite();
//
//                //Vérifie si l'identifiant de l'article (articleId) existait déjà dans la Map des anciennes lignes de la vente
//                //Cette ligne permet au programme de faire la différence entre une modification d'article et un ajout de nouvel
//                // article lors de la mise à jour d'une vente :
//                if (anciennesLignesMap.containsKey(articleId)) {
//                    // L'article existait déjà : on ajuste la différence (Augmentation ou Réduction)
//                    BigDecimal ancienneQte = anciennesLignesMap.get(articleId);
//                    int comparaison = nouvelleQte.compareTo(ancienneQte);
//
//                    //La nouvelle quantité est supérieure à l'anciènne quantité
//                    if (comparaison > 0) {
//                        // La quantité a augmenté -> Déstockage complémentaire
//                        BigDecimal diff = nouvelleQte.subtract(ancienneQte);
//                        modifierStockIndividuel(lig, diff, ETypeMvtStock.CORRECTION_NEG_VENTE_AUG);
//                    }
//
//                    //La nouvelle quantité est inférieure à l'anciènne quantité
//                    else if (comparaison < 0) {
//                        // La quantité a diminué -> Réintégration en stock
//                        BigDecimal diff = ancienneQte.subtract(nouvelleQte);
//                        modifierStockIndividuel(lig, diff, ETypeMvtStock.CORRECTION_POS_VENTE_RED);
//                    }
//                    // Retirer de la Map pour signaler que cet article est traité
//                    anciennesLignesMap.remove(articleId);
//                } else {
//                    // Nouvel article ajouté dans la vente -> Sortie de stock standard
//                    modifierStockIndividuel(lig, nouvelleQte, ETypeMvtStock.SORTIE_VTE);
//
//                    // Mouvement spécifique pour le suivi des ventes issues de commande client
//                    //modifierStockIndividuelCMD(lig, nouvelleQte, ETypeMvtStock.SORTIE_VTE_CMD);
//                }
//            }
//        }
//
//        // -------------------------------------------------------------------------
//        // 5. TRAITEMENT DES LIGNES SUPPRIMÉES (RESTANTES DANS LE MAP)
//        // -------------------------------------------------------------------------
//        // Si des articles étaient présents dans l'ancienne vente mais absents de la nouvelle,
//        // on annule les mouvements de stock associés (réintégration du stock).
//        anciennesLignesMap.forEach((idArt, qteInitiale) -> {
//            LigneVente ligneSupprimee = new LigneVente();
//            ligneSupprimee.setArticle(articleRepository.findById(idArt).orElse(null));
//            ligneSupprimee.setQuantite(qteInitiale);
//            ligneSupprimee.setIdEntreprise(dto.getIdEntreprise());
//
//            updateMvtStockAnnulation(ligneSupprimee);
//        });
//
//        // -------------------------------------------------------------------------
//        // 6. PERSISTANCE FINALE DE LA VENTE ET DES LIGNES EN BDD
//        // -------------------------------------------------------------------------
//        /*
//         * 💾 C'EST ICI QUE LE "codeCommandeClient" EST ÉCRIT EN BASE DE DONNÉES DANS LA TABLE 'VENTE'.
//         * 'saveAndFlush' exécute immédiatement le SQL (INSERT ou UPDATE) sur la table Vente.
//         */
//        Vente savedVente = venteRepository.saveAndFlush(venteToSave);
//
//        // -------------------------------------------------------------------------
//        // 7. MISE À JOUR DE L'ÉTAT DE LA COMMANDE CLIENT ASSOCIÉE
//        // -------------------------------------------------------------------------
//        // Si la vente provient d'une commande client, la commande passe au statut VENDUE.
//        if (dto.getCodeCommandeClient() != null && !dto.getCodeCommandeClient().trim().isEmpty()) {
//            Optional<CommandeClient> commandeOpt = commandeClientRepository.findByCode(dto.getCodeCommandeClient());
//
//            if (commandeOpt.isPresent()) {
//                CommandeClient commandeClient = commandeOpt.get();
//                commandeClient.setEtatCommande(EEtatCommande.VENDUE);
//
//                // Persistance de l'état VENDUE sur la commande client
//                commandeClientRepository.save(commandeClient);
//                log.info("✅ La commande client avec le code {} a été passée à l'état VENDUE.", dto.getCodeCommandeClient());
//            } else {
//                log.warn("⚠️ Code commande client '{}' fourni mais aucune commande correspondante en BDD.", dto.getCodeCommandeClient());
//            }
//        }
//
//        // Mapping final de l'entité enregistrée vers le DTO de retour
//        return VenteDto.fromEntity(savedVente);
//    }







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
