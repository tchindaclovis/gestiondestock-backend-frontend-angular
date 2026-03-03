package com.tchindaClovis.gestiondestock.dto;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.tchindaClovis.gestiondestock.model.CommandeClient;
import com.tchindaClovis.gestiondestock.model.LigneCommandeClient;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class LigneCommandeClientDto {

    private Integer id;

    private BigDecimal quantite;

    private BigDecimal prixUnitaire;

    private Integer idEntreprise;

    private ArticleDto article;

    @JsonIgnore  //pour que ceci ne soit pas mappé car on n'a pas besoin de l'objet CC dans l'objet LCC
    private CommandeClientDto commandeClient;

    public static LigneCommandeClientDto fromEntity (LigneCommandeClient ligneCommandeClients){  //permet de faire un mapping de l'entité vers le DTO
        if(ligneCommandeClients == null){
            return null;
        }

        return LigneCommandeClientDto.builder()
                .id(ligneCommandeClients.getId())
                .quantite(ligneCommandeClients.getQuantite())
                .prixUnitaire(ligneCommandeClients.getPrixUnitaire())
                .idEntreprise(ligneCommandeClients.getIdEntreprise())
                .article(ArticleDto.fromEntity(ligneCommandeClients.getArticle()))
//                .commandeClient(CommandeClientDto.fromEntity(ligneCommandeClients.getCommandeClient()))
                .build();
    }

    public static LigneCommandeClient toEntity (LigneCommandeClientDto ligneCommandeClientDto){    //permet de faire un mapping du DTO vers l'entité
        if(ligneCommandeClientDto == null){
            return null;
        }
        LigneCommandeClient ligneCommandeClients = new LigneCommandeClient();
        ligneCommandeClients.setId(ligneCommandeClientDto.getId());
        ligneCommandeClients.setQuantite(ligneCommandeClientDto.getQuantite());
        ligneCommandeClients.setPrixUnitaire(ligneCommandeClientDto.getPrixUnitaire());
        ligneCommandeClients.setIdEntreprise(ligneCommandeClientDto.getIdEntreprise());
        ligneCommandeClients.setArticle(ArticleDto.toEntity(ligneCommandeClientDto.getArticle()));
//        ligneCommandeClients.setCommandeClient(CommandeClientDto.toEntity(ligneCommandeClientDto.getCommandeClient()));
        return ligneCommandeClients;
    }
}
