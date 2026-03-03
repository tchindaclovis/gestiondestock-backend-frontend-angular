package com.tchindaClovis.gestiondestock.dto;
import com.tchindaClovis.gestiondestock.model.Article;
import com.tchindaClovis.gestiondestock.model.LigneVente;
import com.tchindaClovis.gestiondestock.model.Vente;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Builder
public class LigneVenteDto {

    private Integer id;

    private BigDecimal quantite;

    private BigDecimal prixUnitaire;

    private Integer idEntreprise;

    private ArticleDto article;

    private VenteDto vente;

    public static LigneVenteDto fromEntity (LigneVente ligneVente){  //permet de faire un mapping de l'entité vers le DTO
        if(ligneVente == null){
            return null;
        }

        return LigneVenteDto.builder()
                .id(ligneVente.getId())
                .quantite(ligneVente.getQuantite())
                .prixUnitaire(ligneVente.getPrixUnitaire())
                .idEntreprise(ligneVente.getIdEntreprise())
                .article(ArticleDto.fromEntity(ligneVente.getArticle()))
                .vente(VenteDto.fromEntity(ligneVente.getVente()))
                .build();
    }

    public static LigneVente toEntity (LigneVenteDto ligneVenteDto){    //permet de faire un mapping du DTO vers l'entité
        if(ligneVenteDto == null){
            return null;
        }
        LigneVente ligneVente = new LigneVente();
        ligneVente.setId(ligneVenteDto.getId());
        ligneVente.setQuantite(ligneVenteDto.getQuantite());
        ligneVente.setPrixUnitaire(ligneVenteDto.getPrixUnitaire());
        ligneVente.setIdEntreprise(ligneVenteDto.getIdEntreprise());
        ligneVente.setArticle(ArticleDto.toEntity(ligneVenteDto.getArticle()));
        ligneVente.setVente(VenteDto.toEntity(ligneVenteDto.getVente()));

        return ligneVente;
    }
}
