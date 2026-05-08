package com.tchindaClovis.gestiondestock.dto;

import com.tchindaClovis.gestiondestock.model.Document;
import com.tchindaClovis.gestiondestock.model.EEtatDocument;
import com.tchindaClovis.gestiondestock.model.ETypeDocument;
import lombok.Builder;
import lombok.Data;

import java.util.Date;

@Data
@Builder
public class DocumentDto {

    private Integer id;
    private String destinataire;
//    private boolean modifiable = true;
    private Integer idEntreprise;  //rien à voir avec les règle UML
    private String exercice;
    private String dernierMotifAnnulation;
    private String dernierRejetteur;
    private Date dateDernierRejet;
    private EEtatDocument etat;
    protected ETypeDocument type;
    private Date date;
    private Date dateButoireConfirmation;
    private Date dateButoireValidation;
    private Date dateButoirePayement;
    private Date dateEnvoi;
    private Date dateValidation;
    private Date dateTransfert;

    //permet de faire un mapping de CategoryDto vers Category
    public static DocumentDto fromEntity (Document document){
        if(document == null){
            return null;
        }
        return DocumentDto.builder()
                .id(document.getId())
                .destinataire(document.getDestinataire())
//                .modifiable(document.getModifiable())
                .idEntreprise(document.getIdEntreprise())
                .exercice(document.getExercice())
                .dernierMotifAnnulation(document.getDernierMotifAnnulation())
                .dernierRejetteur(document.getDernierRejetteur())
                .dateDernierRejet(document.getDateDernierRejet())
                .etat(document.getEtat())
                .type(document.getType())
                .date(document.getDate())
                .dateButoireConfirmation(document.getDateButoireConfirmation())
                .dateButoireValidation(document.getDateButoireValidation())
                .dateButoirePayement(document.getDateButoirePayement())
                .dateEnvoi(document.getDateEnvoi())
                .dateValidation(document.getDateValidation())
                .dateTransfert(document.getDateTransfert())

                .build();
    }

    public static Document toEntity (DocumentDto documentDto){
        if(documentDto == null){
            return null;
        }
        Document document = new Document();
        document.setId(documentDto.getId());
        document.setDestinataire(documentDto.getDestinataire());
//        document.setModifiable(documentDto.getModifiable());
        document.setIdEntreprise(documentDto.getIdEntreprise());
        document.setExercice(documentDto.getExercice());
        document.setDernierMotifAnnulation(documentDto.getDernierMotifAnnulation());
        document.setDernierRejetteur(documentDto.getDernierRejetteur());
        document.setDateDernierRejet(documentDto.getDateDernierRejet());
        document.setEtat(documentDto.getEtat());
        document.setType(documentDto.getType());
        document.setDate(documentDto.getDate());
        document.setDateButoireConfirmation(documentDto.getDateButoireConfirmation());
        document.setDateButoireValidation(documentDto.getDateButoireValidation());
        document.setDateButoirePayement(documentDto.getDateButoirePayement());
        document.setDateEnvoi(documentDto.getDateEnvoi());
        document.setDateValidation(documentDto.getDateValidation());
        document.setDateTransfert(documentDto.getDateTransfert());

        return document;
    }
}
