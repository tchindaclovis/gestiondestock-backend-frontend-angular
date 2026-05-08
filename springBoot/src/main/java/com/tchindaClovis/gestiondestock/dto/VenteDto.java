package com.tchindaClovis.gestiondestock.dto;
import com.tchindaClovis.gestiondestock.model.EPaymentType;
import com.tchindaClovis.gestiondestock.model.Vente;
import lombok.Builder;
import lombok.Data;
import java.time.Instant;
import java.util.List;

@Data
@Builder
public class VenteDto {

    private Integer id;

    private String code;

    private Instant dateVente;

//    private String commentaire;

    private EPaymentType paymentType;

    private ClientDto client;

    private Integer idEntreprise;

    private List<LigneVenteDto> ligneVentes;

    public static VenteDto fromEntity (Vente vente){  //permet de faire un mapping de l'entité vers le DTO
        if(vente == null){
            return null;
        }

        return VenteDto.builder()
                .id(vente.getId())
                .code(vente.getCode())
                .dateVente(vente.getDateVente())
//                .commentaire(vente.getCommentaire())
                .paymentType(vente.getPaymentType())
                .client(ClientDto.fromEntity(vente.getClient()))
                .idEntreprise(vente.getIdEntreprise())
                .build();
    }

    public static Vente toEntity (VenteDto venteDto){    //permet de faire un mapping du DTO vers l'entité
        if(venteDto == null){
            return null;
        }
        Vente vente = new Vente();
        vente.setId(venteDto.getId());
        vente.setCode(venteDto.getCode());
        vente.setDateVente(venteDto.getDateVente());
//        vente.setCommentaire(venteDto.getCommentaire());
        vente.setPaymentType(venteDto.getPaymentType());
        vente.setClient(ClientDto.toEntity(venteDto.getClient()));
        vente.setIdEntreprise(venteDto.getIdEntreprise());

        return vente;
    }

}
