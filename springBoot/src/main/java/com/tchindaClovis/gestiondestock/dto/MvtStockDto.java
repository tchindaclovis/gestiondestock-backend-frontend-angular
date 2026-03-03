package com.tchindaClovis.gestiondestock.dto;
import com.tchindaClovis.gestiondestock.model.ESourceMvtStock;
import com.tchindaClovis.gestiondestock.model.ETypeMvtStock;
import com.tchindaClovis.gestiondestock.model.MvtStock;
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

    private Integer idEntreprise;

    private ArticleDto article;

    private ETypeMvtStock typeMvt;

    private ESourceMvtStock sourceMvt;

    public static MvtStockDto fromEntity (MvtStock mvtStock){
        if(mvtStock == null){
            return null;
        }
        return MvtStockDto.builder()
                .id(mvtStock.getId())
                .dateMvt(mvtStock.getDateMvt())
                .quantite(mvtStock.getQuantite())
                .idEntreprise(mvtStock.getIdEntreprise())
                .typeMvt(mvtStock.getTypeMvt())
                .sourceMvt(mvtStock.getSourceMvt())
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
        mvtStock.setTypeMvt(mvtStockDto.getTypeMvt());
        mvtStock.setSourceMvt(mvtStockDto.getSourceMvt());
        mvtStock.setIdEntreprise(mvtStockDto.getIdEntreprise());
        mvtStock.setArticle(ArticleDto.toEntity(mvtStockDto.getArticle()));

        return mvtStock;
    }
}
