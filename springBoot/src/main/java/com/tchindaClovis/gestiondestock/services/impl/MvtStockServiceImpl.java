package com.tchindaClovis.gestiondestock.services.impl;

/*
 * Package services.impl :
 * Contient les implémentations concrètes des interfaces de services.
 * Cette classe implémente la logique métier liée aux mouvements de stock.
 */
import com.tchindaClovis.gestiondestock.dto.MvtStockDto;
import com.tchindaClovis.gestiondestock.exception.EntityNotFoundException;
import com.tchindaClovis.gestiondestock.exception.ErrorCodes;
import com.tchindaClovis.gestiondestock.exception.InvalidEntityException;
import com.tchindaClovis.gestiondestock.model.*;
import com.tchindaClovis.gestiondestock.repository.CommandeClientRepository;
import com.tchindaClovis.gestiondestock.repository.MvtStockRepository;
import com.tchindaClovis.gestiondestock.services.ArticleService;
import com.tchindaClovis.gestiondestock.services.MvtStockService;
import com.tchindaClovis.gestiondestock.validator.MvtStockValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;


/*
 * @Service
 * Indique que cette classe est un service Spring (couche métier).
 * Elle sera automatiquement détectée et injectée.
 */
@Service

/*
 * @Slf4j (Lombok)
 * Génère automatiquement un logger (log).
 * Permet d’écrire :
 * log.info(...)
 * log.warn(...)
 * log.error(...)
 */
@Slf4j
public class MvtStockServiceImpl implements MvtStockService {

    /*
     * mvtStockRepository pour accéder à la base de données
     * concernant les mouvements de stock
     */
    private MvtStockRepository mvtStockRepository;

    /*
     * Service Article :
     * utilisé pour vérifier l'existence d’un article
     */
    private ArticleService articleService;
    private CommandeClientRepository commandeClientRepository;


    /*
     * Injection par constructeur (bonne pratique)
     */
    @Autowired
    public MvtStockServiceImpl(MvtStockRepository mvtStockRepository,
                               ArticleService articleService,
                               CommandeClientRepository commandeClientRepository) {
        this.mvtStockRepository = mvtStockRepository;
        this.articleService = articleService;
        this.commandeClientRepository = commandeClientRepository;
    }


    @Override
    public List<MvtStockDto> findAllMvtStock() {
        return mvtStockRepository.findAll().stream()
                .map(MvtStockDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public MvtStockDto findMvtStockByCodeCommandeClient(String codeCommandeClient) {
        if(!StringUtils.hasLength(codeCommandeClient)){
            log.error("MvtStock CODE is null");
            return null;
        }
        return mvtStockRepository.findByCodeCommandeClient(codeCommandeClient)
                .map(MvtStockDto::fromEntity)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Aucune mvtStock n'a été trouvé avec le CODE " +
                                codeCommandeClient,ErrorCodes.MVT_STOCK_NOT_FOUND
                ));
    }


    /*
     * ================================
     * CALCUL DU STOCK RÉEL
     * ================================
     *
     * Retourne la quantité réelle en stock d’un article.
     */
    @Override
    public BigDecimal stockReelArticle(Integer idArticle) {

        // Vérification si l’ID est null
        if (idArticle == null) {
            log.warn("ID article is NULL");
            return BigDecimal.valueOf(-1);
        }

        // Vérifie si l’article existe (sinon exception)
        articleService.findById(idArticle);

        // Appel du mvtStockRepository pour calculer le stock réel
        return mvtStockRepository.stockReelArticle(idArticle);
    }


    /*
     * ================================
     * LISTE DES MOUVEMENTS DE STOCK
     * ================================
     * Retourne tous les mouvements liés à un article.
     */
    @Override
    public List<MvtStockDto> mvtStockArticle(Integer idArticle) {

        return mvtStockRepository.findAllByArticleId(idArticle)
                .stream()

                // Conversion Entity → DTO
                .map(MvtStockDto::fromEntity)

                // Transformation en liste
                .collect(Collectors.toList());
    }


    @Override
    public List<MvtStockDto> findAllMvtsByEntreprise(Integer idEntreprise) {
        if (idEntreprise == null) {
            log.error("Entreprise ID is null");
            return List.of();
        }
        return mvtStockRepository.findAllByIdEntreprise(idEntreprise)
                .stream()
                .map(MvtStockDto::fromEntity)
                .collect(Collectors.toList());
    }


    /*
     * ================================
     * ENTRÉE EN STOCK
     * ================================
     * Ajoute une quantité positive au stock.
     */
    @Override
    public MvtStockDto entreeStock(MvtStockDto dto) {

        return saveMvtStockPos(dto, ETypeMvtStock.ENTREE, ESourceMvtStock.COMMANDE_FOURNISSEUR);
    }


    /*
     * ================================
     * SORTIE DE STOCK
     * ================================
     * Retire une quantité du stock.
     */
    @Override
    public MvtStockDto sortieStock(MvtStockDto dto) {

        return saveMvtStockNeg(dto, ETypeMvtStock.SORTIE, ESourceMvtStock.COMMANDE_CLIENT);
    }


    @Override
    public MvtStockDto sortieStockVte(MvtStockDto dto) {

        return saveMvtStockNeg(dto, ETypeMvtStock.SORTIE_VTE, ESourceMvtStock.VENTE);
    }


    @Override
    public MvtStockDto sortieStockVteCMD(MvtStockDto dto) {

        return saveMvtStockNeg(dto, ETypeMvtStock.SORTIE_VTE_CMD, ESourceMvtStock.VENTE_CMD);
    }


    /*
     * ================================
     * CORRECTION POSITIVE
     * ================================
     * Ajustement positif du stock.
     */
    @Override
    public MvtStockDto correctionStockPos(MvtStockDto dto) {

        return saveMvtStockPos(dto, ETypeMvtStock.CORRECTION_POS, ESourceMvtStock.CORRECTION_STOCK);
    }

    @Override
    public MvtStockDto correctionStockPosVenteRed(MvtStockDto dto) {

        return saveMvtStockPos(dto, ETypeMvtStock.CORRECTION_POS_VENTE_RED, ESourceMvtStock.CORRECTION_STOCK);
    }

    @Override
    public MvtStockDto correctionStockPosVenteRed1(MvtStockDto dto) {

        return saveMvtStockPos(dto, ETypeMvtStock.CORRECTION_POS_VENTE_RED, ESourceMvtStock.VENTE);
    }


    @Override
    public MvtStockDto correctionStockPosVenteRed1CMD(MvtStockDto dto) {

        return saveMvtStockPos(dto, ETypeMvtStock.CORRECTION_POS_VENTE_RED, ESourceMvtStock.VENTE_CMD);
    }


    @Override
    public MvtStockDto correctionStockNegRetourFournisseur(MvtStockDto dto) {

        return saveMvtStockNeg(dto, ETypeMvtStock.CORRECTION_NEG_RETOUR_FOURNISSEUR, ESourceMvtStock.COMMANDE_FOURNISSEUR);
    }

    @Override
    public MvtStockDto correctionStockNegRetourFournisseur1(MvtStockDto dto) {

        return saveMvtStockNeg(dto, ETypeMvtStock.CORRECTION_NEG_RETOUR_FOURNISSEUR, ESourceMvtStock.CORRECTION_STOCK);
    }


    @Override
    public MvtStockDto correctionStockPosVenteRed2(MvtStockDto dto) {

        return saveMvtStockPos(dto, ETypeMvtStock.CORRECTION_POS_VENTE_RED, ESourceMvtStock.COMMANDE_CLIENT);
    }


    /*
     * ================================
     * CORRECTION NÉGATIVE
     * ================================
     * Ajustement négatif du stock.
     */
    @Override
    public MvtStockDto correctionStockNeg(MvtStockDto dto) {

        return saveMvtStockNeg(dto, ETypeMvtStock.CORRECTION_NEG, ESourceMvtStock.CORRECTION_STOCK);
    }

    @Override
    public MvtStockDto correctionStockNegVenteAug(MvtStockDto dto) {

        return saveMvtStockNeg(dto, ETypeMvtStock.CORRECTION_NEG_VENTE_AUG, ESourceMvtStock.VENTE);
    }


    @Override
    public MvtStockDto correctionStockNegVenteAugCMD(MvtStockDto dto) {

        return saveMvtStockNeg(dto, ETypeMvtStock.CORRECTION_NEG_VENTE_AUG, ESourceMvtStock.VENTE_CMD);
    }


    /*
     * ================================
     * MÉTHODE PRIVÉE : ENTRÉE POSITIVE
     * ================================
     * Rend la quantité toujours positive.
     */
    private MvtStockDto saveMvtStockPos(MvtStockDto dto, ETypeMvtStock typeMvtStock, ESourceMvtStock sourceMvtStock) {
        // 1. Validation (Conservation de votre logique actuelle)
        List<String> errors = MvtStockValidator.validate(dto);
        if (!errors.isEmpty()) {
            log.error("MvtStock is not valid {}", dto);
            throw new InvalidEntityException(
                    "Le mouvement du stock n'est pas valide",
                    ErrorCodes.MVT_STOCK_NOT_VALID,
                    errors
            );
        }

        // 2. Préparation de l'entité (Mapping Manuel Explicite)
        MvtStock entity = MvtStockDto.toEntity(dto);

        // On s'assure que la quantité est positive pour une entrée
        entity.setQuantite(BigDecimal.valueOf(Math.abs(dto.getQuantite().doubleValue())));

        // Définition du Type et de la Source
        entity.setTypeMvt(typeMvtStock);
        entity.setSourceMvt(sourceMvtStock);

        // MAPPAGE EXPLICITE DU CODE SOURCE
        // C'est ici que l'on récupère le code envoyé par le front ou le service de vente
        entity.setCodeSource(dto.getCodeSource());

        // 3. Sauvegarde et retour
        return MvtStockDto.fromEntity(
                mvtStockRepository.save(entity)
        );
    }


    /*
     * ================================
     * MÉTHODE PRIVÉE : SORTIE NÉGATIVE
     * ================================
     * Rend la quantité toujours négative.
     */

    private MvtStockDto saveMvtStockNeg(MvtStockDto dto, ETypeMvtStock typeMvtStock, ESourceMvtStock sourceMvtStock) {
        // 1. Validation (Conservation de votre logique actuelle)
        List<String> errors = MvtStockValidator.validate(dto);
        if (!errors.isEmpty()) {
            log.error("MvtStock is not valid {}", dto);
            throw new InvalidEntityException(
                    "Le mouvement du stock n'est pas valide",
                    ErrorCodes.MVT_STOCK_NOT_VALID,
                    errors
            );
        }

        // 2. Préparation de l'entité (Mapping Manuel Explicite)
        MvtStock entity = MvtStockDto.toEntity(dto);

        // On s'assure que la quantité est négative pour une sortie
        entity.setQuantite(BigDecimal.valueOf(Math.abs(dto.getQuantite().doubleValue()) * -1));

        // Définition du Type et de la Source
        entity.setTypeMvt(typeMvtStock);
        entity.setSourceMvt(sourceMvtStock);

        // MAPPAGE EXPLICITE DU CODE SOURCE
        // C'est ici que l'on récupère le code envoyé par le front ou le service de vente
        entity.setCodeSource(dto.getCodeSource());

        // 3. Sauvegarde et retour
        return MvtStockDto.fromEntity(
                mvtStockRepository.save(entity)
        );
    }


    @Override
    public String getLastCodeCorrection() {
        // On demande le dernier enregistrement avec la source CORRECTION_STOCK
        // PageRequest.of(0, 1) permet de ne récupérer qu'un seul résultat (le top 1)
        List<String> codes = mvtStockRepository.findLastCodeBySource(
                ESourceMvtStock.CORRECTION_STOCK, PageRequest.of(0, 1)
        );

        if (codes.isEmpty()) {
            return "CCS0000"; // Valeur par défaut si la base est vide
        }
        return codes.get(0);
    }


    @Override
    public void delete(Integer id) {
        if (id == null) {
            log.error("Mouvement stock ID is null");
            return;
        }
        mvtStockRepository.deleteById(id);
    }
}








//    @Override
//    public BigDecimal stockReelArticle(Integer idArticle, VenteDto venteDto) {
//
//        // Vérification si l’ID est null
//        if (idArticle == null) {
//            log.warn("ID article is NULL");
//            return BigDecimal.valueOf(-1);
//        }
//
//        // Vérifie si l’article existe
//        articleService.findById(idArticle);
//
//        // Condition basée sur le champ codeCommandeClient de la vente reçue
//        if (venteDto != null && venteDto.getCodeCommandeClient() != null && !venteDto.getCodeCommandeClient().trim().isEmpty()) {
//            log.info("Calcul du stock pour l'article {} (Vente issue d'une commande client)", idArticle);
//            return mvtStockRepository.stockReelArticleVenteIssueDeCommande(idArticle);
//        }
//
//        log.info("Calcul du stock réel standard pour l'article {}", idArticle);
//        return mvtStockRepository.stockReelArticle(idArticle);
//    }





//    private MvtStockDto entreePositive(MvtStockDto dto, ETypeMvtStock typeMvtStock, ESourceMvtStock sourceMvtStock) {
//        // Validation des données
//        List<String> errors = MvtStockValidator.validate(dto);
//        if (!errors.isEmpty()) {
//            log.error("Article is not valid {}", dto);
//            throw new InvalidEntityException(
//                    "Le mouvement du stock n'est pas valide",
//                    ErrorCodes.MVT_STOCK_NOT_VALID,
//                    errors
//            );
//        }
//
//        // Transformation de la quantité en valeur positive
//        // Math.abs() garantit que la valeur est positive
//        dto.setQuantite(BigDecimal.valueOf(Math.abs(dto.getQuantite().doubleValue())));
//
//        // Définition du type de mouvement
//        dto.setTypeMvt(typeMvtStock);
//
//        // Définition de la source du mouvement
//        dto.setSourceMvt(sourceMvtStock);
//
//        // Sauvegarde en base (DTO → Entity → DB → DTO)
//        return MvtStockDto.fromEntity(
//                mvtStockRepository.save(MvtStockDto.toEntity(dto))
//        );
//    }



//        private MvtStockDto sortieNegative(MvtStockDto dto, ETypeMvtStock typeMvtStock, ESourceMvtStock sourceMvtStock) {
//        // Validation des données
//        List<String> errors = MvtStockValidator.validate(dto);
//        if (!errors.isEmpty()) {
//            log.error("Article is not valid {}", dto);
//            throw new InvalidEntityException(
//                    "Le mouvement du stock n'est pas valide",
//                    ErrorCodes.MVT_STOCK_NOT_VALID,
//                    errors
//            );
//        }
//
////         * Transformation de la quantité en valeur négative
////         * Exemple : 10 → -10
//        dto.setQuantite(BigDecimal.valueOf(Math.abs(dto.getQuantite().doubleValue()) * -1));
//        // Définition du type de mouvement
//        dto.setTypeMvt(typeMvtStock);
//
//        // Définition de la source du mouvement
//        dto.setSourceMvt(sourceMvtStock);
//
//        // Sauvegarde en base
//        return MvtStockDto.fromEntity(
//                mvtStockRepository.save(MvtStockDto.toEntity(dto))
//        );
//    }




//    @Override
//    public BigDecimal stockReelArticle(Integer idArticle) {
//
//        // 1. Vérification si l’ID est null
//        if (idArticle == null) {
//            log.warn("ID article is NULL");
//            return BigDecimal.valueOf(-1);
//        }
//
//        // 2. Vérifie si l’article existe (sinon déclenche une exception)
//        articleService.findById(idArticle);
//
//        // 3. Vérifie si un mouvement de stock pour cet article contient un codeCommandeClient non nul
//        boolean aCommandeClient = mvtStockRepository.existsByArticleIdAndCodeCommandeClientIsNotNull(idArticle);
//
//        // 4. Aiguillage selon la présence du codeCommandeClient
//        if (aCommandeClient) {
//            log.info("Des mouvements avec codeCommandeClient existent pour l'article ID {}. Utilisation du calcul spécial.", idArticle);
//            return mvtStockRepository.stockReelArticleVenteIssueDeCommande(idArticle);
//        } else {
//            log.info("Aucun codeCommandeClient pour l'article ID {}. Utilisation du calcul classique.", idArticle);
//            return mvtStockRepository.stockReelArticle(idArticle);
//        }
//    }


//    @Override
//    public String getLastCodeCorrection() {
//        // CORRECTION : Utilisez l'instance "venteRepository" (minuscule)
//        // et extrayez le code de l'objet Vente
//        return mvtStockRepository.findTopByOrderByCodeCorrectionDesc()
//                .map(MvtStock::getCodeCorrection) // On transforme l'Vente en String (son code)
//                .orElse("CCS0000");           // Valeur par défaut si aucun vente n'existe
//    }

