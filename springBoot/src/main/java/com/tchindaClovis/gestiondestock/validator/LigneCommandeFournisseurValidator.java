package com.tchindaClovis.gestiondestock.validator;

import com.tchindaClovis.gestiondestock.dto.LigneCommandeFournisseurDto;

import java.util.ArrayList;
import java.util.List;

public class LigneCommandeFournisseurValidator {
    public static List<String> validate(LigneCommandeFournisseurDto dto){
        List< String> errors = new ArrayList<>();
        if(dto == null){
            errors.add("Veuillez renseigner le prix unitaire de la la ligne de commande Fournisseur");
            errors.add("Veuillez renseigner la quantite de la ligne de commande Fournisseur");
            return errors;
        }

        if(dto.getPrixUnitaire() == null){
            errors.add("Veuillez renseigner le prix unitaire de la la ligne de commande Fournisseur");
        }
        if(dto.getQuantite() == null){
            errors.add("Veuillez renseigner la quantite de la ligne de commande Fournisseur");
        }
        return errors;
    }
}
