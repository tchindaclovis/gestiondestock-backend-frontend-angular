package com.tchindaClovis.gestiondestock.dto;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.tchindaClovis.gestiondestock.model.Entreprise;
import com.tchindaClovis.gestiondestock.model.Fournisseur;
import lombok.Builder;
import lombok.Data;
import java.util.List;
@Data
@Builder
public class FournisseurDto {

    private Integer id;

    private String nom;

    private String prenom;

    private String photo;

    private String email;

    private String numTel;

    private Integer idEntreprise;

    private AdresseDto adresse;

    @JsonIgnore
    private List<CommandeFournisseurDto> commandeFournisseurs;

    public static FournisseurDto fromEntity (Fournisseur fournisseur){  //permet de faire un mapping de l'entité vers le DTO
        if(fournisseur == null){
            return null;
        }

        return FournisseurDto.builder()
                .id(fournisseur.getId())
                .nom(fournisseur.getNom())
                .prenom(fournisseur.getPrenom())
                .photo(fournisseur.getPhoto())
                .email(fournisseur.getEmail())
                .numTel(fournisseur.getNumTel())
                .idEntreprise(fournisseur.getIdEntreprise())
                .adresse(AdresseDto.fromEntity(fournisseur.getAdresse()))
                .build();
    }

    public static Fournisseur toEntity (FournisseurDto dto){    //permet de faire un mapping du DTO vers l'entité
        if(dto == null){
            return null;
        }
        Fournisseur fournisseur = new Fournisseur();
        fournisseur.setId(dto.getId());
        fournisseur.setNom(dto.getNom());
        fournisseur.setPrenom(dto.getPrenom());
        fournisseur.setPhoto(dto.getPhoto());
        fournisseur.setEmail(dto.getEmail());
        fournisseur.setNumTel(dto.getNumTel());
        fournisseur.setIdEntreprise(dto.getIdEntreprise());
        fournisseur.setAdresse(AdresseDto.toEntity(dto.getAdresse()));

        return fournisseur;
    }
}
