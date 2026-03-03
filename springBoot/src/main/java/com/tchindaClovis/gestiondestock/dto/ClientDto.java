package com.tchindaClovis.gestiondestock.dto;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.tchindaClovis.gestiondestock.model.Adresse;
import com.tchindaClovis.gestiondestock.model.Client;
import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class ClientDto {

    private Integer id;

    private String nom;

    private String prenom;

    private String photo;

    private String email;

    private String numTel;

    private AdresseDto adresse;

    private Integer idEntreprise;

    @JsonIgnore
    private List<CommandeClientDto> commandeClients;

    public static ClientDto fromEntity (Client client){  //permet de faire un mapping de l'entité vers le DTO
        if(client == null){
            return null;
        }

        return ClientDto.builder()
                .id(client.getId())
                .nom(client.getNom())
                .prenom(client.getPrenom())
                .photo(client.getPhoto())
                .email(client.getEmail())
                .numTel(client.getNumTel())
                .adresse(AdresseDto.fromEntity(client.getAdresse()))
                .idEntreprise(client.getIdEntreprise())
                .build();
    }

    public static Client toEntity (ClientDto clientDto){    //permet de faire un mapping du DTO vers l'entité
        if(clientDto == null){
            return null;
        }
        Client client = new Client();
        client.setId(clientDto.getId());
        client.setNom(clientDto.getNom());
        client.setPrenom(clientDto.getPrenom());
        client.setPhoto(clientDto.getPhoto());
        client.setEmail(clientDto.getEmail());
        client.setNumTel(clientDto.getNumTel());
        client.setAdresse(AdresseDto.toEntity(clientDto.getAdresse()));
        client.setIdEntreprise(clientDto.getIdEntreprise());

        return client;
    }
}
