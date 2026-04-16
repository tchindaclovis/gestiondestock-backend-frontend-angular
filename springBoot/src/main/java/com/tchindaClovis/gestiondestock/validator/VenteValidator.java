package com.tchindaClovis.gestiondestock.validator;

import com.tchindaClovis.gestiondestock.dto.VenteDto;
import org.springframework.util.StringUtils;
import java.util.ArrayList;
import java.util.List;

public class VenteValidator {
    public static List<String> validate(VenteDto dto) {
        List<String> errors = new ArrayList<>();
        if (dto == null) {
            errors.add("Veuillez renseigner le code de la vente");
            errors.add("Veuillez renseigner la date de la vente");
            return errors;
        }

        if (!StringUtils.hasLength(dto.getCode())) {
            errors.add("Veuillez renseigner le code de la vente");
        }
        if (dto.getDateVente() == null) {
            errors.add("Veuillez renseigner la date de la vente");
        }

        return errors;
    }
}
