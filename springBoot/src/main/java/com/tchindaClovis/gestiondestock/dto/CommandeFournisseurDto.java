package com.tchindaClovis.gestiondestock.dto;
import com.tchindaClovis.gestiondestock.model.*;
import lombok.Builder;
import lombok.Data;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
public class CommandeFournisseurDto {

    private Integer id;

    private String code;

    private Instant dateCommande;

    private Instant creationDate;

    private Instant lastModifiedDate;

    private Instant dateConfirmation;
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
                .creationDate(commandeFournisseur.getCreationDate())
                .lastModifiedDate(commandeFournisseur.getLastModifiedDate())
                .dateConfirmation(commandeFournisseur.getDateConfirmation())
                .etatCommande(commandeFournisseur.getEtatCommande())
                .etat(commandeFournisseur.getEtat())
                .idEntreprise(commandeFournisseur.getIdEntreprise())
                .fournisseur(FournisseurDto.fromEntity(commandeFournisseur.getFournisseur()))

//                // AJOUTER LE MAPPAGE DES LIGNES
//                .ligneCommandeFournisseurs(
//                        commandeFournisseur.getLigneCommandeFournisseurs() != null ?
//                                commandeFournisseur.getLigneCommandeFournisseurs().stream()
//                                        .map(LigneCommandeFournisseurDto::fromEntity)
//                                        .collect(Collectors.toList()) :
//                                null
//                )

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
        commandeFournisseur.setCreationDate(commandeFournisseurDto.getCreationDate());
        commandeFournisseur.setLastModifiedDate(commandeFournisseurDto.getLastModifiedDate());
        commandeFournisseur.setDateConfirmation(commandeFournisseurDto.getDateConfirmation());
        commandeFournisseur.setIdEntreprise(commandeFournisseurDto.getIdEntreprise());
        commandeFournisseur.setEtatCommande(commandeFournisseurDto.getEtatCommande());
        commandeFournisseur.setEtat(commandeFournisseurDto.getEtat());
        commandeFournisseur.setFournisseur(FournisseurDto.toEntity(commandeFournisseurDto.getFournisseur()));

//        // AJOUTER LE MAPPAGE DES LIGNES
//        if (commandeFournisseurDto.getLigneCommandeFournisseurs() != null) {
//            List<LigneCommandeFournisseur> lignes = commandeFournisseurDto.getLigneCommandeFournisseurs()
//                    .stream()
//                    .map(LigneCommandeFournisseurDto::toEntity)
//                    .collect(Collectors.toList());
//            // Associer chaque ligne à la commande
//            lignes.forEach(ligne -> ligne.setCommandeFournisseur(commandeFournisseur));
//            commandeFournisseur.setLigneCommandeFournisseurs(lignes);
//        }

        return commandeFournisseur;
    }

    public boolean isCommandeLivree() {
        return EEtatCommande.LIVREE.equals(this.etatCommande);
    }

    public boolean isCommandeConfirmee() {
        return EEtatCommande.CONFIRMEE.equals(this.etatCommande);
    }
}
