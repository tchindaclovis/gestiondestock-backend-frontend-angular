package com.tchindaClovis.gestiondestock.validator;
import com.tchindaClovis.gestiondestock.dto.LigneCommandeClientDto;
import java.util.ArrayList;
import java.util.List;

public class LigneCommandeClientValidator {
    public static List<String> validate(LigneCommandeClientDto dto){
        List< String> errors = new ArrayList<>();
        if(dto == null){
            errors.add("Veuillez renseigner le prix unitaire de la la ligne de commande client");
            errors.add("Veuillez renseigner la quantite de la ligne de commande client");
            return errors;
        }

        if(dto.getPrixUnitaire() == null){
            errors.add("Veuillez renseigner le prix unitaire de la la ligne de commande client");
        }
        if(dto.getQuantite() == null){
            errors.add("Veuillez renseigner la quantite de la ligne de commande client");
        }
        return errors;
    }

}
