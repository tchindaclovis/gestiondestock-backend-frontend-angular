package com.tchindaClovis.gestiondestock.validator;

import com.tchindaClovis.gestiondestock.dto.VenteDto;
import org.springframework.util.StringUtils;
import java.util.ArrayList;
import java.util.List;


public class VenteValidator {

    public static List<String> validate(VenteDto dto) {
        List<String> errors = new ArrayList<>();

        // 1. Si l'objet complet est absent, on retourne toutes les erreurs d'un coup
        if (dto == null) {
            errors.add("Veuillez renseigner le code de la vente");
            errors.add("Veuillez renseigner la date de la vente");
            errors.add("Veuillez renseigner le mode de paiement");
            return errors;
        }

        // 2. Validation du code de vente
        if (!StringUtils.hasLength(dto.getCode())) {
            errors.add("Veuillez renseigner le code de la vente");
        }

        // 3. Validation de la date de vente
        if (dto.getDateVente() == null) {
            errors.add("Veuillez renseigner la date de la vente");
        }

        // 4. CORRECTION SÉCURISÉE : Validation du mode de paiement
        // On vérifie d'abord si l'Enum lui-même est nul avant de faire toute opération dessus
        if (dto.getPaymentType() == null) {
            errors.add("Veuillez renseigner le mode de paiement");
        }

        return errors;
    }
}




//public class VenteValidator {
//    public static List<String> validate(VenteDto dto) {
//        List<String> errors = new ArrayList<>();
//        if (dto == null) {
//            errors.add("Veuillez renseigner le code de la vente");
//            errors.add("Veuillez renseigner la date de la vente");
//            errors.add("Veuillez renseigner le mode de paiement");
//            return errors;
//        }
//
//        if (!StringUtils.hasLength(dto.getCode())) {
//            errors.add("Veuillez renseigner le code de la vente");
//        }
//
//        if (dto.getDateVente() == null) {
//            errors.add("Veuillez renseigner la date de la vente");
//        }
//
//        if (!StringUtils.hasLength(dto.getPaymentType().toString())) {
//            errors.add("Veuillez renseigner le mode de paiement");
//        }
//
//        return errors;
//    }
//}
