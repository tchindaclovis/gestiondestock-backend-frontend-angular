package com.tchindaClovis.gestiondestock.controller;

import com.tchindaClovis.gestiondestock.controller.api.MvtStockApi;
import com.tchindaClovis.gestiondestock.dto.MvtStockDto;
import com.tchindaClovis.gestiondestock.services.MvtStockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;
import java.math.BigDecimal;
import java.util.List;

@RestController
public class MvtStockController implements MvtStockApi {

    private MvtStockService mvtStockService;

    @Autowired
    public MvtStockController(MvtStockService mvtStockService) {
        this.mvtStockService = mvtStockService;
    }

    @Override
    public BigDecimal stockReelArticle(Integer idArticle) {
            return mvtStockService.stockReelArticle(idArticle);
    }

    @Override
    public List<MvtStockDto> mvtStockArticle(Integer idArticle) {

        return mvtStockService.mvtStockArticle(idArticle);
    }

    @Override
    public List<MvtStockDto> findAllMvtsByEntreprise(Integer idEntreprise) {

        return mvtStockService.findAllMvtsByEntreprise(idEntreprise);
    }

    @Override
    public List<MvtStockDto> findAllMvtStock() {
        return mvtStockService.findAllMvtStock();
    }

    @Override
    public MvtStockDto entreeStock(MvtStockDto dto) {
            return mvtStockService.entreeStock(dto);
    }

    @Override
    public MvtStockDto sortieStock(MvtStockDto dto) {
            return mvtStockService.sortieStock(dto);
    }

    @Override
    public MvtStockDto sortieStockVte(MvtStockDto dto) {
        return mvtStockService.sortieStockVte(dto);
    }

    @Override
    public MvtStockDto correctionStockPos(MvtStockDto dto) {
            return mvtStockService.correctionStockPos(dto);
    }

    @Override
    public MvtStockDto correctionStockPosVenteRed(MvtStockDto dto) {
        return mvtStockService.correctionStockPosVenteRed(dto);
    }

    @Override
    public MvtStockDto correctionStockPosVenteRed1(MvtStockDto dto) {
        return mvtStockService.correctionStockPosVenteRed1(dto);
    }

    @Override
    public MvtStockDto correctionStockPosVenteRed2(MvtStockDto dto) {
        return mvtStockService.correctionStockPosVenteRed2(dto);
    }

    @Override
    public MvtStockDto correctionStockNegRetourFournisseur(MvtStockDto dto) {
        return mvtStockService.correctionStockNegRetourFournisseur(dto);
    }

    @Override
    public MvtStockDto correctionStockNegRetourFournisseur1(MvtStockDto dto) {
        return mvtStockService.correctionStockNegRetourFournisseur1(dto);
    }

    @Override
    public MvtStockDto correctionStockNeg(MvtStockDto dto) {
            return mvtStockService.correctionStockNeg(dto);
    }

    @Override
    public MvtStockDto correctionStockNegVenteAug(MvtStockDto dto) {
        return mvtStockService.correctionStockNegVenteAug(dto);
    }

    @Override
    public String getLastCodeCorrection() {
        return mvtStockService.getLastCodeCorrection();
    }

}



//package com.tchindaClovis.gestiondestock.controller;
//
//import com.tchindaClovis.gestiondestock.controller.api.MvtStockApi;
//import com.tchindaClovis.gestiondestock.dto.MvtStockDto;
//import com.tchindaClovis.gestiondestock.services.MvtStockService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.web.bind.annotation.RestController;
//import java.math.BigDecimal;
//import java.time.Instant;
//import java.util.List;
//
//@RestController
//public class MvtStockController implements MvtStockApi {
//
//    private MvtStockService mvtStockService;
//
//    @Autowired
//    public MvtStockController(MvtStockService mvtStockService) {
//        this.mvtStockService = mvtStockService;
//    }
//
//    @Override
//    public MvtStockDto save(MvtStockDto dto) {
//        return mvtStockService.save(dto);
//    }
//
//    @Override
//    public MvtStockDto findById(Integer id) {
//        return mvtStockService.findById(id);
//    }
//
//    @Override
//    public MvtStockDto findByDateMvt(Instant dateMvt) {
//        return mvtStockService.findByDateMvt(dateMvt);
//    }
//
//    @Override
//    public MvtStockDto findByQuantite(BigDecimal quantite) {
//        return mvtStockService.findByQuantite(quantite);
//    }
//
//    @Override
//    public List<MvtStockDto> findAll() {
//        return mvtStockService.findAll();
//    }
//
//}
