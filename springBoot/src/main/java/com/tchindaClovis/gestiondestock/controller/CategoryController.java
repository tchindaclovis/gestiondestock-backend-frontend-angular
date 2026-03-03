package com.tchindaClovis.gestiondestock.controller;

import com.tchindaClovis.gestiondestock.controller.api.CategoryApi;
import com.tchindaClovis.gestiondestock.dto.CategoryDto;
import com.tchindaClovis.gestiondestock.services.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
public class CategoryController implements CategoryApi {
    private CategoryService categoryService;

    @Autowired
    public CategoryController(CategoryService categoryService) {

        this.categoryService = categoryService;
    }

    @Override
    public CategoryDto save(CategoryDto dto) {

        return categoryService.save(dto);
    }

    @Override
    public CategoryDto findById(Integer id) {

        return categoryService.findById(id);
    }

    @Override
    public CategoryDto findByCode(String code) {

        return categoryService.findByCode(code);
    }

    @Override
    public List<CategoryDto> findAll() {

        return categoryService.findAll();
    }

    @Override
    public void delete(Integer id) {

        categoryService.delete(id);
    }
}
