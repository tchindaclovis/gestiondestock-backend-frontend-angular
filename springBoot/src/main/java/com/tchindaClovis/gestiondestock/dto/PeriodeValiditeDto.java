package com.tchindaClovis.gestiondestock.dto;

import com.tchindaClovis.gestiondestock.model.PeriodeValidite;
import lombok.Builder;
import lombok.Data;
import java.util.Date;

@Data
@Builder
public class PeriodeValiditeDto {
    private Integer id;
    String intitule;
    Date debut;
    Integer numero;
    Date fin;
    Boolean derniere = Boolean.FALSE;
    private Integer idEntreprise;  //rien à voir avec les règle UML

    //permet de faire un mapping de CategoryDto vers Category
    public static PeriodeValiditeDto fromEntity (PeriodeValidite periodeValidite){
        if(periodeValidite == null){
            return null;
        }
        return PeriodeValiditeDto.builder()
                .id(periodeValidite.getId())
                .intitule(periodeValidite.getIntitule())
                .debut(periodeValidite.getDebut())
                .idEntreprise(periodeValidite.getIdEntreprise())
                .fin(periodeValidite.getFin())
                .numero(periodeValidite.getNumero())
                .derniere(periodeValidite.getDerniere())

                .build();
    }

    public static PeriodeValidite toEntity (PeriodeValiditeDto periodeValiditeDto){
        if(periodeValiditeDto == null){
            return null;
        }
        PeriodeValidite periodeValidite = new PeriodeValidite();
        periodeValidite.setId(periodeValiditeDto.getId());
        periodeValidite.setIntitule(periodeValiditeDto.getIntitule());
        periodeValidite.setDebut(periodeValiditeDto.getDebut());
        periodeValidite.setIdEntreprise(periodeValiditeDto.getIdEntreprise());
        periodeValidite.setFin(periodeValiditeDto.getFin());
        periodeValidite.setNumero(periodeValiditeDto.getNumero());
        periodeValidite.setDerniere(periodeValiditeDto.getDerniere());

        return periodeValidite;
    }
}
