package com.tchindaClovis.gestiondestock.repository;

import com.tchindaClovis.gestiondestock.model.Article;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ArticleRepository extends JpaRepository<Article, Integer> {
    Optional<Article> findArticleByCodeArticle(String codeArticle);
    List<Article> findAllByCategoryId(Integer idCategory);
    List<Article> findAllByIdEntreprise(Integer idEntreprise);

    // Cette méthode va trier par codeArticle descendant et prendre le premier (le plus grand)
    Optional<Article> findTopByOrderByCodeArticleDesc();
}
