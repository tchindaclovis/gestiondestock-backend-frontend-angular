package com.tchindaClovis.gestiondestock.validator;

import com.tchindaClovis.gestiondestock.dto.LigneVenteDto;
import java.util.ArrayList;
import java.util.List;

public class LigneVenteValidator {
    public static List<String> validate(LigneVenteDto dto){
        List< String> errors = new ArrayList<>();
        if(dto == null){
            errors.add("Veuillez renseigner le prix unitaire de la la ligne de la Vente");
            errors.add("Veuillez renseigner la quantite de la ligne de la Vente");
            return errors;
        }

        if(dto.getPrixUnitaire() == null){
            errors.add("Veuillez renseigner le prix unitaire de la la ligne de la Vente");
        }
        if(dto.getQuantite() == null){
            errors.add("Veuillez renseigner la quantite de la ligne de la Vente");
        }
        return errors;
    }
}
