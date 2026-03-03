package com.tchindaClovis.gestiondestock.services;

import com.tchindaClovis.gestiondestock.dto.MvtStockDto;
import java.math.BigDecimal;
import java.util.List;

public interface MvtStockService {

    BigDecimal stockReelArticle(Integer idArticle);

    List<MvtStockDto> mvtStockArticle(Integer idArticle);

    MvtStockDto entreeStock(MvtStockDto dto);

    MvtStockDto sortieStock(MvtStockDto dto);

    MvtStockDto correctionStockPos(MvtStockDto dto);

    MvtStockDto correctionStockNeg(MvtStockDto dto);

}
