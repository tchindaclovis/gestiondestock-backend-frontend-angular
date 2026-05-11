package com.tchindaClovis.gestiondestock.dto;
import com.tchindaClovis.gestiondestock.model.CommandeFournisseur;
import com.tchindaClovis.gestiondestock.model.EEtatCommande;
import com.tchindaClovis.gestiondestock.model.EEtatDocument;
import lombok.Builder;
import lombok.Data;
import java.time.Instant;
import java.util.List;

@Data
@Builder
public class CommandeFournisseurDto {

    private Integer id;

    private String code;

    private Instant dateCommande;

    private EEtatCommande etatCommande;
    private EEtatDocument etat;

    private FournisseurDto fournisseur;

    private Integer idEntreprise;

    private List<LigneCommandeFournisseurDto> ligneCommandeFournisseurs;


    public static CommandeFournisseurDto fromEntity (CommandeFournisseur commandeFournisseur){  //permet de faire un mapping de l'entité vers le DTO
        if(commandeFournisseur == null){
            return null;
        }

        return CommandeFournisseurDto.builder()
                .id(commandeFournisseur.getId())
                .code(commandeFournisseur.getCode())
                .dateCommande(commandeFournisseur.getDateCommande())
                .etatCommande(commandeFournisseur.getEtatCommande())
                .etat(commandeFournisseur.getEtat())
                .idEntreprise(commandeFournisseur.getIdEntreprise())
                .fournisseur(FournisseurDto.fromEntity(commandeFournisseur.getFournisseur()))
                .build();
    }

    public static CommandeFournisseur toEntity (CommandeFournisseurDto commandeFournisseurDto){    //permet de faire un mapping du DTO vers l'entité
        if(commandeFournisseurDto == null){
            return null;
        }
        CommandeFournisseur commandeFournisseur = new CommandeFournisseur();
        commandeFournisseur.setId(commandeFournisseurDto.getId());
        commandeFournisseur.setCode(commandeFournisseurDto.getCode());
        commandeFournisseur.setDateCommande(commandeFournisseurDto.getDateCommande());
        commandeFournisseur.setIdEntreprise(commandeFournisseurDto.getIdEntreprise());
        commandeFournisseur.setEtatCommande(commandeFournisseurDto.getEtatCommande());
        commandeFournisseur.setEtat(commandeFournisseurDto.getEtat());
        commandeFournisseur.setFournisseur(FournisseurDto.toEntity(commandeFournisseurDto.getFournisseur()));

        return commandeFournisseur;
    }
    public boolean isCommandeLivree() {

        return EEtatCommande.LIVREE.equals(this.etatCommande);
    }
}
