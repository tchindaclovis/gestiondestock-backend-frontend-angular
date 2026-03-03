package com.tchindaClovis.gestiondestock.services;

import com.tchindaClovis.gestiondestock.dto.CategoryDto;
import com.tchindaClovis.gestiondestock.model.Category;

import java.util.List;

public interface CategoryService {
    CategoryDto save(CategoryDto dto);
    CategoryDto findById(Integer id);
    CategoryDto findByCode(String code);
    List<CategoryDto> findAll();
    void delete(Integer id);
}
