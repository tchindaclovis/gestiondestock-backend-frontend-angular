package com.tchindaClovis.gestiondestock.validator;
import com.tchindaClovis.gestiondestock.dto.ArticleDto;
import org.springframework.util.StringUtils;
import java.util.ArrayList;
import java.util.List;

public class ArticleValidator {
    public static List<String> validate(ArticleDto dto){
        List< String> errors = new ArrayList<>();
        if(dto == null){
            errors.add("Veuillez renseigner le code de l'article");
            errors.add("Veuillez renseigner la désignation de l'article");
            errors.add("Veuillez renseigner le format de l'article");
            errors.add("Veuillez renseigner le prix unitaire HT de l'article");
            errors.add("Veuillez renseigner le taux TVA de l'article");
            errors.add("Veuillez renseigner le prix unitaire TTC de l'article");
            errors.add("Veuillez renseigner une categorie");
            return errors;
        }

        if(!StringUtils.hasLength(dto.getCodeArticle())){
            errors.add("Veuillez renseigner le code de l'article");
        }

        if(!StringUtils.hasLength(dto.getDesignation())){
            errors.add("Veuillez renseigner la désignation de l'article");
        }

        if(!StringUtils.hasLength(dto.getFormat())){
            errors.add("Veuillez renseigner le format de l'article");
        }

        if(dto.getPrixVenteUnitaireHt() == null){
            errors.add("Veuillez renseigner le prix unitaire HT de l'article");
        }

        if(dto.getTauxTva() == null){
            errors.add("Veuillez renseigner le taux TVA de l'article");
        }

        if(dto.getPrixVenteUnitaireTtc() == null){
            errors.add("Veuillez renseigner le prix unitaire TTC de l'article");
        }

//        if(!StringUtils.hasLength((CharSequence) dto.getCategory()))
        if (dto.getCategory() == null || dto.getCategory().getId() == null)
        {
            errors.add("Veuillez renseigner une categorie");
        }

        return errors;
    }
}
