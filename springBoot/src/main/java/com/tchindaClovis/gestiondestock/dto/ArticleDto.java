package com.tchindaClovis.gestiondestock.dto;
import com.tchindaClovis.gestiondestock.model.Article;
import com.tchindaClovis.gestiondestock.model.Entreprise;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class ArticleDto {

    private Integer id;

    private String codeArticle;

    private String designation;

    private String description;

    private String format;

    private BigDecimal prixUnitaireHt;

    private BigDecimal prixVenteUnitaireHt;

    private BigDecimal tauxTva;

    private BigDecimal prixUnitaireTtc;

    private BigDecimal prixVenteUnitaireTtc;

    private BigDecimal marge;

    private String photo;

    private CategoryDto category;

    private Integer idEntreprise;

    //permet de faire un mapping de CategoryDto vers Category
    public static ArticleDto fromEntity (Article article){
        if(article == null){
            return null;
        }
        return ArticleDto.builder()
                .id(article.getId())
                .codeArticle(article.getCodeArticle())
                .designation(article.getDesignation())
                .description(article.getDescription())
                .format(article.getFormat())
                .prixUnitaireHt(article.getPrixUnitaireHt())
                .prixVenteUnitaireHt(article.getPrixVenteUnitaireHt())
                .tauxTva(article.getTauxTva())
                .prixUnitaireTtc(article.getPrixUnitaireTtc())
                .prixVenteUnitaireTtc(article.getPrixVenteUnitaireTtc())
                .marge(article.getMarge())
                .photo(article.getPhoto())
                .idEntreprise(article.getIdEntreprise())
                .category(CategoryDto.fromEntity(article.getCategory()))
                .build();
    }

    public static Article toEntity (ArticleDto articleDto){
        if(articleDto == null){
            return null;
        }
        Article article = new Article();
        article.setId(articleDto.getId());
        article.setCodeArticle(articleDto.getCodeArticle());
        article.setDesignation(articleDto.getDesignation());
        article.setDescription(articleDto.getDescription());
        article.setFormat(articleDto.getFormat());
        article.setPrixUnitaireHt(articleDto.getPrixUnitaireHt());
        article.setPrixVenteUnitaireHt(articleDto.getPrixVenteUnitaireHt());
        article.setTauxTva(articleDto.getTauxTva());
        article.setPrixUnitaireTtc(articleDto.getPrixUnitaireTtc());
        article.setPrixVenteUnitaireTtc(articleDto.getPrixVenteUnitaireTtc());
        article.setMarge(articleDto.getMarge());
        article.setPhoto(articleDto.getPhoto());
        article.setIdEntreprise(articleDto.getIdEntreprise());
        article.setCategory(CategoryDto.toEntity(articleDto.getCategory()));

        return article;
    }
}
