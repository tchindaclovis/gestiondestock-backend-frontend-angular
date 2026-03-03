package com.tchindaClovis.gestiondestock.dto;

import com.tchindaClovis.gestiondestock.model.CommandeClient;
import com.tchindaClovis.gestiondestock.model.EEtatCommande;
import lombok.Builder;
import lombok.Data;
import java.time.Instant;
import java.util.List;

@Data
@Builder
public class  CommandeClientDto {

    private Integer id;

    private String code;

    private Instant dateCommande;

    private Integer idEntreprise;

    private EEtatCommande etatCommande;

    private ClientDto client;

    private List<LigneCommandeClientDto> ligneCommandeClients;

    public static CommandeClientDto fromEntity (CommandeClient commandeClient){  //permet de faire un mapping de l'entité vers le DTO
        if(commandeClient == null){
            return null;
        }

        return CommandeClientDto.builder()
                .id(commandeClient.getId())
                .code(commandeClient.getCode())
                .dateCommande(commandeClient.getDateCommande())
                .idEntreprise(commandeClient.getIdEntreprise())
                .etatCommande(commandeClient.getEtatCommande())
                .client(ClientDto.fromEntity(commandeClient.getClient()))

//                // AJOUTER LE MAPPAGE DES LIGNES
//                .ligneCommandeClients(
//                        commandeClient.getLigneCommandeClients() != null ?
//                                commandeClient.getLigneCommandeClients().stream()
//                                        .map(LigneCommandeClientDto::fromEntity)
//                                        .collect(Collectors.toList()) :
//                                null
//                )

                .build();
    }

    public static CommandeClient toEntity (CommandeClientDto commandeClientDto){    //permet de faire un mapping du DTO vers l'entité
        if(commandeClientDto == null){
            return null;
        }
        CommandeClient commandeClient = new CommandeClient();
        commandeClient.setId(commandeClientDto.getId());
        commandeClient.setCode(commandeClientDto.getCode());
        commandeClient.setDateCommande(commandeClientDto.getDateCommande());
        commandeClient.setEtatCommande(commandeClientDto.getEtatCommande());
        commandeClient.setIdEntreprise(commandeClientDto.getIdEntreprise());
        commandeClient.setClient(ClientDto.toEntity(commandeClientDto.getClient()));

//        // AJOUTER LE MAPPAGE DES LIGNES
//        if (commandeClientDto.getLigneCommandeClients() != null) {
//            List<LigneCommandeClient> lignes = commandeClientDto.getLigneCommandeClients()
//                    .stream()
//                    .map(LigneCommandeClientDto::toEntity)
//                    .collect(Collectors.toList());
//            // Associer chaque ligne à la commande
//            lignes.forEach(ligne -> ligne.setCommandeClient(commandeClient));
//            commandeClient.setLigneCommandeClients(lignes);
//        }

        return commandeClient;
    }

    public boolean isCommandeLivree() {

        return EEtatCommande.LIVREE.equals(this.etatCommande);
    }
}
