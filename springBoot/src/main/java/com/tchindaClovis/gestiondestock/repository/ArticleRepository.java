package com.tchindaClovis.gestiondestock.repository;

import com.tchindaClovis.gestiondestock.model.Article;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ArticleRepository extends JpaRepository<Article, Integer> {
    Optional<Article> findArticleByCodeArticle(String codeArticle);
    List<Article> findAllByCategoryId(Integer idCategory);

//    @Query("select a from article where codearticle = :code and designation = :designation") //Requête JPQL
//    List<Article> findByCustomQuery(@Param("code") String c, @Param("designation") String d); //que hybernate transforme en reqête native

//    @Query(value = "select * from article where code = :code", nativeQuery = true) //Requête native
//    List<Article> findByCustomNativeQuery(@Param("code") String c);

//    List<Article> findByCodeArticleAndDesignation(String codeArticle, String designation); //requête hybernate ordinaire
//    List<Article> findByCodeArticleIgnoreCaseAndDesignationIgnoreCase(String codeArticle, String designation); // ignoer la case pour les deux paramètres
    
}
