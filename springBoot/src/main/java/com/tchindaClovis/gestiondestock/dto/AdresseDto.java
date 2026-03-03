package com.tchindaClovis.gestiondestock.dto;
import com.tchindaClovis.gestiondestock.model.Adresse;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AdresseDto {

    private String adresse1;

    private String adresse2;

    private String ville;

    private String codePostale;

    private String pays;

    //permet de faire un mapping de Adresse vers AdresseDto
    public static AdresseDto fromEntity (Adresse adresse){  //permet de faire un mapping de l'entité vers le DTO
        if(adresse == null){
            return null;
        }
        return AdresseDto.builder()
                .adresse1(adresse.getAdresse1())
                .adresse2(adresse.getAdresse2())
                .ville(adresse.getVille())
                .codePostale(adresse.getCodePostale())
                .pays(adresse.getPays())
                .build();
    }
    //permet de faire un mapping de AdresseDto vers Adresse
    public static Adresse toEntity (AdresseDto adresseDto){  //permet de faire un mapping du DTO vers l'entité
        if(adresseDto == null){
            return null;
        }
        Adresse adresse = new Adresse();
        adresse.setAdresse1(adresseDto.getAdresse1());
        adresse.setAdresse2(adresseDto.getAdresse2());
        adresse.setVille(adresseDto.getVille());
        adresse.setCodePostale(adresseDto.getCodePostale());
        adresse.setPays(adresseDto.getPays());

        return adresse;
    }
}
