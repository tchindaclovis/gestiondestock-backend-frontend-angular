package com.tchindaClovis.gestiondestock.services.impl;

/*
 * Package services.impl :
 * Contient les implémentations concrètes des interfaces de services.
 *
 * Cette classe implémente la logique métier liée aux mouvements de stock.
 */

import com.tchindaClovis.gestiondestock.dto.MvtStockDto;
import com.tchindaClovis.gestiondestock.dto.VenteDto;
import com.tchindaClovis.gestiondestock.exception.ErrorCodes;
import com.tchindaClovis.gestiondestock.exception.InvalidEntityException;
import com.tchindaClovis.gestiondestock.model.ETypeMvtStock;
import com.tchindaClovis.gestiondestock.repository.MvtStockRepository;
import com.tchindaClovis.gestiondestock.services.ArticleService;
import com.tchindaClovis.gestiondestock.services.MvtStockService;
import com.tchindaClovis.gestiondestock.validator.MvtStockValidator;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;


/*
 * @Service
 *
 * Indique que cette classe est un service Spring (couche métier).
 * Elle sera automatiquement détectée et injectée.
 */
@Service

/*
 * @Slf4j (Lombok)
 *
 * Génère automatiquement un logger (log).
 *
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


    /*
     * Injection par constructeur (bonne pratique)
     */
    @Autowired
    public MvtStockServiceImpl(MvtStockRepository mvtStockRepository,
                               ArticleService articleService) {
        this.mvtStockRepository = mvtStockRepository;
        this.articleService = articleService;
    }



    @Override
    public List<MvtStockDto> findAllMvtStock() {
        return mvtStockRepository.findAll().stream()
                .map(MvtStockDto::fromEntity)
                .collect(Collectors.toList());
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
     *
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


    /*
     * ================================
     * ENTRÉE EN STOCK
     * ================================
     *
     * Ajoute une quantité positive au stock.
     */
    @Override
    public MvtStockDto entreeStock(MvtStockDto dto) {

        return entreePositive(dto, ETypeMvtStock.ENTREE);
    }


    /*
     * ================================
     * SORTIE DE STOCK
     * ================================
     *
     * Retire une quantité du stock.
     */
    @Override
    public MvtStockDto sortieStock(MvtStockDto dto) {

        return sortieNegative(dto, ETypeMvtStock.SORTIE);
    }


    /*
     * ================================
     * CORRECTION POSITIVE
     * ================================
     *
     * Ajustement positif du stock.
     */
    @Override
    public MvtStockDto correctionStockPos(MvtStockDto dto) {

        return entreePositive(dto, ETypeMvtStock.CORRECTION_POS);
    }


    /*
     * ================================
     * CORRECTION NÉGATIVE
     * ================================
     *
     * Ajustement négatif du stock.
     */
    @Override
    public MvtStockDto correctionStockNeg(MvtStockDto dto) {

        return sortieNegative(dto, ETypeMvtStock.CORRECTION_NEG);
    }


    /*
     * ================================
     * MÉTHODE PRIVÉE : ENTRÉE POSITIVE
     * ================================
     *
     * Rend la quantité toujours positive.
     */
    private MvtStockDto entreePositive(MvtStockDto dto, ETypeMvtStock typeMvtStock) {
        // Validation des données
        List<String> errors = MvtStockValidator.validate(dto);
        if (!errors.isEmpty()) {
            log.error("Article is not valid {}", dto);
            throw new InvalidEntityException(
                    "Le mouvement du stock n'est pas valide",
                    ErrorCodes.MVT_STOCK_NOT_VALID,
                    errors
            );
        }

//        *Transformation de la quantité en valeur positive
//        *Math.abs() garantit que la valeur est positive
        dto.setQuantite(
                BigDecimal.valueOf(
                        Math.abs(dto.getQuantite().doubleValue())
                )
        );

        // Définition du type de mouvement
        dto.setTypeMvt(typeMvtStock);
        // Sauvegarde en base (DTO → Entity → DB → DTO)
        return MvtStockDto.fromEntity(
                mvtStockRepository.save(MvtStockDto.toEntity(dto))
        );
    }


    /*
     * ================================
     * MÉTHODE PRIVÉE : SORTIE NÉGATIVE
     * ================================
     *
     * Rend la quantité toujours négative.
     */
    private MvtStockDto sortieNegative(MvtStockDto dto, ETypeMvtStock typeMvtStock) {
        // Validation des données
        List<String> errors = MvtStockValidator.validate(dto);
        if (!errors.isEmpty()) {
            log.error("Article is not valid {}", dto);
            throw new InvalidEntityException(
                    "Le mouvement du stock n'est pas valide",
                    ErrorCodes.MVT_STOCK_NOT_VALID,
                    errors
            );
        }

//         * Transformation de la quantité en valeur négative
//         * Exemple : 10 → -10
        dto.setQuantite(
                BigDecimal.valueOf(
                        Math.abs(dto.getQuantite().doubleValue()) * -1
                )
        );
        // Définition du type de mouvement
        dto.setTypeMvt(typeMvtStock);
        // Sauvegarde en base
        return MvtStockDto.fromEntity(
                mvtStockRepository.save(MvtStockDto.toEntity(dto))
        );
    }

}



