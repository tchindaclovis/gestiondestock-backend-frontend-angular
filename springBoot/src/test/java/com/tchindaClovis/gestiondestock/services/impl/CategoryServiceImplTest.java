package com.tchindaClovis.gestiondestock.services.impl;

import com.tchindaClovis.gestiondestock.dto.CategoryDto;
import com.tchindaClovis.gestiondestock.exception.EntityNotFoundException;
import com.tchindaClovis.gestiondestock.exception.ErrorCodes;
import com.tchindaClovis.gestiondestock.exception.InvalidEntityException;
import com.tchindaClovis.gestiondestock.services.CategoryService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.jupiter.api.Assertions.*;
@RunWith(SpringRunner.class)  //pour signifier que c'est avec sprint que je teste
@SpringBootTest  // il va préparer le contexte pour les test
public class CategoryServiceImplTest {
    @Autowired //on peut aussi Mocker c'est à dire sans injecter le springBootTest dans ma classe de test
    private CategoryService service;
    @Test
    public void shouldSaveCategoryWhithSucces(){
       CategoryDto expectedCategory = CategoryDto.builder()
                .code("Cat test")
                .designation("Designation test")
                .idEntreprise(1)
                .build();
       CategoryDto savedCategory = service.save(expectedCategory); //je fais appel à mon service save que je met dans une variable
        assertNotNull(savedCategory);
        assertNotNull(savedCategory.getId());
        assertEquals(expectedCategory.getCode(), savedCategory.getCode());
        assertEquals(expectedCategory.getDesignation(), savedCategory.getDesignation());
        assertEquals(expectedCategory.getIdEntreprise(), savedCategory.getIdEntreprise());
    }


    @Test
    public void shouldUpdateCategoryWhithSucces(){
        CategoryDto expectedCategory = CategoryDto.builder()
                .code("Cat test")
                .designation("Designation test")
                .idEntreprise(1)
                .build();
        CategoryDto savedCategory = service.save(expectedCategory); //je fais appel à mon service save que je met dans une variable
        CategoryDto categoryToUpdate = savedCategory;

        categoryToUpdate.setCode("Cat update");

        savedCategory = service.save(categoryToUpdate);

        assertNotNull(categoryToUpdate);
        assertNotNull(categoryToUpdate.getId());
        assertEquals(categoryToUpdate.getCode(), savedCategory.getCode());
        assertEquals(categoryToUpdate.getDesignation(), savedCategory.getDesignation());
        assertEquals(categoryToUpdate.getIdEntreprise(), savedCategory.getIdEntreprise());
    }


    @Test
    public void shouldThrowInvalidEntityException(){
        CategoryDto expectedCategory = CategoryDto.builder().build();

        InvalidEntityException expectedException = assertThrows(InvalidEntityException.class, () -> service.save(expectedCategory));

        assertEquals(ErrorCodes.CATEGORY_NOT_VALID, expectedException.getErrorCode());
        assertEquals(3, expectedException.getErrors().size());
        assertEquals("Veuillez renseigner le code de la catégorie", expectedException.getErrors().get(0));
    }


    @Test
    public void shouldThrowEntityNotFoundException(){

        EntityNotFoundException expectedException = assertThrows(EntityNotFoundException.class, () -> service.findById(0));

        assertEquals(ErrorCodes.CATEGORY_NOT_FOUND, expectedException.getErrorCode());
        assertEquals( "Aucune categorie avec l'ID = 0 n'a ete trouve dans la BDD", expectedException.getMessage());
    }


    @Test(expected = EntityNotFoundException.class)
    public void shouldThrowEntityNotFoundException1(){

        service.findById(0);
    }
}