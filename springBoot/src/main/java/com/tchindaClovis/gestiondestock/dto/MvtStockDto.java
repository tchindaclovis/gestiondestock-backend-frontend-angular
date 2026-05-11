package com.tchindaClovis.gestiondestock.dto;
import com.tchindaClovis.gestiondestock.model.*;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
public class MvtStockDto {

    private Integer id;

    private Instant dateMvt;

    private BigDecimal quantite;

//    private String codeCorrection;

    private Integer idEntreprise;

    private ArticleDto article;

    private ETypeMvtStock typeMvt;

    private ETypeDocument type;

    private ESourceMvtStock sourceMvt;

    private ESourceDocument source;

    private String codeSource;

    public static MvtStockDto fromEntity (MvtStock mvtStock){
        if(mvtStock == null){
            return null;
        }
        return MvtStockDto.builder()
                .id(mvtStock.getId())
                .dateMvt(mvtStock.getDateMvt())
                .quantite(mvtStock.getQuantite())
//                .codeCorrection(mvtStock.getCodeCorrection())
                .idEntreprise(mvtStock.getIdEntreprise())
                .typeMvt(mvtStock.getTypeMvt())
                .type(mvtStock.getType())
                .sourceMvt(mvtStock.getSourceMvt())
                .source(mvtStock.getSource())
                .codeSource(mvtStock.getCodeSource())
                .article(ArticleDto.fromEntity(mvtStock.getArticle()))
                .build();
    }

    public static MvtStock toEntity (MvtStockDto mvtStockDto){
        if(mvtStockDto == null){
            return null;
        }
        MvtStock mvtStock = new MvtStock();
        mvtStock.setId(mvtStockDto.getId());
        mvtStock.setDateMvt(mvtStockDto.getDateMvt());
        mvtStock.setQuantite(mvtStockDto.getQuantite());
//        mvtStock.setCodeCorrection(mvtStockDto.getCodeCorrection());
        mvtStock.setTypeMvt(mvtStockDto.getTypeMvt());
        mvtStock.setType(mvtStockDto.getType());
        mvtStock.setSourceMvt(mvtStockDto.getSourceMvt());
        mvtStock.setSource(mvtStockDto.getSource());
        mvtStock.setCodeSource(mvtStockDto.getCodeSource());
        mvtStock.setIdEntreprise(mvtStockDto.getIdEntreprise());
        mvtStock.setArticle(ArticleDto.toEntity(mvtStockDto.getArticle()));

        return mvtStock;
    }
}
