package com.tchindaClovis.gestiondestock.dto;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.tchindaClovis.gestiondestock.model.LigneVente;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Builder
public class LigneVenteDto {

    private Integer id;

    private BigDecimal quantite;

//    // Cette annotation permet de recevoir "prixUnitaire" OU "prixVenteUnitaireTtc" depuis Angular
//    @JsonProperty("prixUnitaire")
    private BigDecimal prixVenteUnitaireTtc;

    private Integer idEntreprise;

    private ArticleDto article;

    @JsonIgnore  //pour que ceci ne soit pas mappé car on n'a pas besoin de l'objet vente dans l'objet LigneVenteDto
    private VenteDto vente;

    public static LigneVenteDto fromEntity (LigneVente ligneVente){  //permet de faire un mapping de l'entité vers le DTO
        if(ligneVente == null){
            return null;
        }

        return LigneVenteDto.builder()
                .id(ligneVente.getId())
                .quantite(ligneVente.getQuantite())
                .prixVenteUnitaireTtc(ligneVente.getPrixVenteUnitaireTtc())
                .idEntreprise(ligneVente.getIdEntreprise())
                .article(ArticleDto.fromEntity(ligneVente.getArticle()))
                .build();
    }

    public static LigneVente toEntity (LigneVenteDto ligneVenteDto){    //permet de faire un mapping du DTO vers l'entité
        if(ligneVenteDto == null){
            return null;
        }
        LigneVente ligneVente = new LigneVente();
        ligneVente.setId(ligneVenteDto.getId());
        ligneVente.setQuantite(ligneVenteDto.getQuantite());
        ligneVente.setPrixVenteUnitaireTtc(ligneVenteDto.getPrixVenteUnitaireTtc());
        ligneVente.setIdEntreprise(ligneVenteDto.getIdEntreprise());
        ligneVente.setArticle(ArticleDto.toEntity(ligneVenteDto.getArticle()));

        return ligneVente;
    }
}
