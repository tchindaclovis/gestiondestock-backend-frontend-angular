package com.tchindaClovis.gestiondestock.dto;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.tchindaClovis.gestiondestock.model.Article;
import com.tchindaClovis.gestiondestock.model.CommandeFournisseur;
import com.tchindaClovis.gestiondestock.model.LigneCommandeFournisseur;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
@Data
@Builder
public class LigneCommandeFournisseurDto {

    private Integer id;

    private BigDecimal quantite;

//    // Cette annotation permet de recevoir "prixUnitaire" OU "prixUnitaireTtc" depuis Angular
//    @JsonProperty("prixUnitaire")
    private BigDecimal prixUnitaireTtc;

    private Integer idEntreprise;

    private ArticleDto article;

    @JsonIgnore  //pour que ceci ne soit pas mappé car on n'a pas besoin de l'objet CF dans l'objet LCF
    private CommandeFournisseurDto commandeFournisseur;

    public static LigneCommandeFournisseurDto fromEntity (LigneCommandeFournisseur ligneCommandeFournisseurs){  //permet de faire un mapping de l'entité vers le DTO
        if(ligneCommandeFournisseurs == null){
            return null;
        }

        return LigneCommandeFournisseurDto.builder()
                .id(ligneCommandeFournisseurs.getId())
                .quantite(ligneCommandeFournisseurs.getQuantite())
                .prixUnitaireTtc(ligneCommandeFournisseurs.getPrixUnitaireTtc())
                .idEntreprise(ligneCommandeFournisseurs.getIdEntreprise())
                .article(ArticleDto.fromEntity(ligneCommandeFournisseurs.getArticle()))
                .build();
    }

    public static LigneCommandeFournisseur toEntity (LigneCommandeFournisseurDto ligneCommandeFournisseurDto){    //permet de faire un mapping du DTO vers l'entité
        if(ligneCommandeFournisseurDto == null){
            return null;
        }
        LigneCommandeFournisseur ligneCommandeFournisseurs = new LigneCommandeFournisseur();
        ligneCommandeFournisseurs.setId(ligneCommandeFournisseurDto.getId());
        ligneCommandeFournisseurs.setQuantite(ligneCommandeFournisseurDto.getQuantite());
        ligneCommandeFournisseurs.setPrixUnitaireTtc(ligneCommandeFournisseurDto.getPrixUnitaireTtc());
        ligneCommandeFournisseurs.setIdEntreprise(ligneCommandeFournisseurDto.getIdEntreprise());
        ligneCommandeFournisseurs.setArticle(ArticleDto.toEntity(ligneCommandeFournisseurDto.getArticle()));
        return ligneCommandeFournisseurs;
    }

}
