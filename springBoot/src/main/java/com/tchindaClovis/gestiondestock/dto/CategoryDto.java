package com.tchindaClovis.gestiondestock.dto;

import com.tchindaClovis.gestiondestock.model.Category;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CategoryDto {

    private Integer id;

    private String code;

    private String designation;

    private Integer idEntreprise;

    @JsonIgnore
    private List<ArticleDto> articles;

    public static CategoryDto fromEntity(Category category) {
        if (category == null) {
            return null;
            // TODO throw an exception
        }

        return CategoryDto.builder()
                .id(category.getId())
                .code(category.getCode())
                .designation(category.getDesignation())
                .idEntreprise(category.getIdEntreprise())
                .build();
    }
        //permet de faire un mapping de CategoryDto vers Category
    public static Category toEntity(CategoryDto categoryDto) {
        if (categoryDto == null) {
            return null;
            // TODO throw an exception
        }

        Category category = new Category();
        category.setId(categoryDto.getId());
        category.setCode(categoryDto.getCode());
        category.setDesignation(categoryDto.getDesignation());
        category.setIdEntreprise(categoryDto.getIdEntreprise());

        return category;
    }
}






//package com.tchindaClovis.gestiondestock.dto;
//import com.fasterxml.jackson.annotation.JsonIgnore;
//import com.tchindaClovis.gestiondestock.model.Category;
//import lombok.Builder;
//import lombok.Data;
//import java.util.List;
//
//@Data
//@Builder
//public class CategoryDto {
//
//    private Integer id;
//
//    private String code;
//
//    private String designation;
//
//    @JsonIgnore
//    private List<ArticleDto> articles;
//
//    public static CategoryDto fromEntity (Category category){
//        if(category == null){
//            return null;
//        }
//        //permet de faire un mapping de CategoryDto vers Category
//        return CategoryDto.builder()
//                .id(category.getId())
//                .code(category.getCode())
//                .designation(category.getDesignation())
//                .build();
//    }
//
//    public static Category toEntity (CategoryDto categoryDto){
//        if(categoryDto == null){
//        return null;
//        }
//        Category category = new Category();
//        category.setId(categoryDto.getId());
//        category.setCode(categoryDto.getCode());
//        category.setDesignation(categoryDto.getDesignation());
//
//        return category;
//    }
//}
