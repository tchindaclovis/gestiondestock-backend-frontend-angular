package com.tchindaClovis.gestiondestock.services.impl;

import com.tchindaClovis.gestiondestock.dto.MvtStockDto;
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

@Service
@Slf4j
public class MvtStockServiceImpl implements MvtStockService {
    private MvtStockRepository repository;
    private ArticleService articleService;

    @Autowired
    public MvtStockServiceImpl(MvtStockRepository repository, ArticleService articleService) {
        this.repository = repository;
        this.articleService = articleService;
    }

    @Override
    public BigDecimal stockReelArticle(Integer idArticle) {
        if (idArticle == null) {
            log.warn("ID article is NULL");
            return BigDecimal.valueOf(-1);
        }
        articleService.findById(idArticle);
        return repository.stockReelArticle(idArticle);
    }

    @Override
    public List<MvtStockDto> mvtStockArticle(Integer idArticle) {
        return repository.findAllByArticleId(idArticle).stream()
                .map(MvtStockDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public MvtStockDto entreeStock(MvtStockDto dto) {
        return entreePositive(dto, ETypeMvtStock.ENTREE);
    }

    @Override
    public MvtStockDto sortieStock(MvtStockDto dto) {
        return sortieNegative(dto, ETypeMvtStock.SORTIE);
    }

    @Override
    public MvtStockDto correctionStockPos(MvtStockDto dto) {
        return entreePositive(dto, ETypeMvtStock.CORRECTION_POS);
    }

    @Override
    public MvtStockDto correctionStockNeg(MvtStockDto dto) {
        return sortieNegative(dto, ETypeMvtStock.CORRECTION_NEG);
    }

    private MvtStockDto entreePositive(MvtStockDto dto, ETypeMvtStock typeMvtStock) {
        List<String> errors = MvtStockValidator.validate(dto);
        if (!errors.isEmpty()) {
            log.error("Article is not valid {}", dto);
            throw new InvalidEntityException("Le mouvement du stock n'est pas valide", ErrorCodes.MVT_STOCK_NOT_VALID, errors);
        }
        dto.setQuantite(
                BigDecimal.valueOf(
                        Math.abs(dto.getQuantite().doubleValue())
                )
        );
        dto.setTypeMvt(typeMvtStock);
        return MvtStockDto.fromEntity(
                repository.save(MvtStockDto.toEntity(dto))
        );
    }

    private MvtStockDto sortieNegative(MvtStockDto dto, ETypeMvtStock typeMvtStock) {
        List<String> errors = MvtStockValidator.validate(dto);
        if (!errors.isEmpty()) {
            log.error("Article is not valid {}", dto);
            throw new InvalidEntityException("Le mouvement du stock n'est pas valide",
                    ErrorCodes.MVT_STOCK_NOT_VALID, errors);
        }
        dto.setQuantite(
                BigDecimal.valueOf(
                        Math.abs(dto.getQuantite().doubleValue()) * -1
                )
        );
        dto.setTypeMvt(typeMvtStock);
        return MvtStockDto.fromEntity(
                repository.save(MvtStockDto.toEntity(dto))
        );
    }

}





//package com.tchindaClovis.gestiondestock.services.impl;
//
//import com.tchindaClovis.gestiondestock.dto.MvtStockDto;
//import com.tchindaClovis.gestiondestock.exception.EntityNotFoundException;
//import com.tchindaClovis.gestiondestock.exception.ErrorCodes;
//import com.tchindaClovis.gestiondestock.exception.InvalidEntityException;
//import com.tchindaClovis.gestiondestock.model.ETypeMvtStock;
//import com.tchindaClovis.gestiondestock.model.MvtStock;
//import com.tchindaClovis.gestiondestock.repository.MvtStockRepository;
//import com.tchindaClovis.gestiondestock.services.ArticleService;
//import com.tchindaClovis.gestiondestock.services.MvtStockService;
//import com.tchindaClovis.gestiondestock.validator.MvtStockValidator;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//import java.math.BigDecimal;
//import java.time.Instant;
//import java.util.List;
//import java.util.Optional;
//import java.util.stream.Collectors;
//
//@Service
//@Slf4j
//public class MvtStockServiceImpl implements MvtStockService {
//    private MvtStockRepository mvtStockRepository;
//    private ArticleService articleService;
//    @Autowired
//    public MvtStockServiceImpl(MvtStockRepository mvtStockRepository, ArticleService articleService) {
//        this.mvtStockRepository = mvtStockRepository;
//        this.articleService = articleService;
//    }
//
//    @Override
//    public MvtStockDto save(MvtStockDto dto) {
//        List<String> errors = MvtStockValidator.validate(dto);
//        if(!errors.isEmpty()){
//            log.error("Mouvement de stock is not valid{}", dto);
//            throw new InvalidEntityException("Le mouvement de stock n'est pas valide",
//                    ErrorCodes.MVT_STOCK_NOT_VALID, errors);
//        }
//        MvtStock savedMvtStock = mvtStockRepository.save(MvtStockDto.toEntity(dto));
//        return MvtStockDto.fromEntity(savedMvtStock);
//    }
//
//    @Override
//    public MvtStockDto findById(Integer id) {
//        if(id == null){
//            log.error("Mouvement de stock ID is null");
//            return null;
//        }
//        Optional<MvtStock> mvtStock = mvtStockRepository.findById(id);
//
//        return Optional.of(MvtStockDto.fromEntity(mvtStock.get())).orElseThrow(() ->
//                new EntityNotFoundException(
//                        "Aucun mouvement de stock avec l'ID = " + id + "n'a ete trouve dans la BDD",
//                        ErrorCodes.MVT_STOCK_NOT_FOUND)
//        );
//    }
//
//    @Override
//    public MvtStockDto findByDateMvt(Instant dateMvt) {
//        if (dateMvt == null){
//            log.error("Mouvement de stock DATEMVT is null");
//            return null;
//        }
//        Optional<MvtStock> mvtStock = mvtStockRepository.findMvtStockByDateMvt(dateMvt);
//        return Optional.of(MvtStockDto.fromEntity(mvtStock.get())).orElseThrow(() ->
//                new EntityNotFoundException(
//                        "Aucun mouvement de stock avec le DATEMVT = " + dateMvt + "n'a ete trouve dans la BDD",
//                        ErrorCodes.MVT_STOCK_NOT_FOUND)
//        );
//    }
//
//    @Override
//    public MvtStockDto findByQuantite(BigDecimal quantite) {
//        if (quantite == null){
//            log.error("Mouvement de stock QUANTITE is null");
//            return null;
//        }
//        Optional<MvtStock> mvtStock = mvtStockRepository.findMvtStockByQuantite(quantite);
//        return Optional.of(MvtStockDto.fromEntity(mvtStock.get())).orElseThrow(() ->
//                new EntityNotFoundException(
//                        "Aucun mouvement de stock avec le QUANTITE= " + quantite + "n'a ete trouve dans la BDD",
//                        ErrorCodes.MVT_STOCK_NOT_FOUND)
//        );
//    }
//
//    @Override
//    public List<MvtStockDto> findAll() {
//        return mvtStockRepository.findAll().stream()
//                .map(MvtStockDto::fromEntity)
//                .collect(Collectors.toList());
//    }
//
//
//}
