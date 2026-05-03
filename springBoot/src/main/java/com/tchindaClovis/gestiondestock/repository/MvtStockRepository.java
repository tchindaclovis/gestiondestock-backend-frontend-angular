package com.tchindaClovis.gestiondestock.repository;

import com.tchindaClovis.gestiondestock.model.Article;
import com.tchindaClovis.gestiondestock.model.MvtStock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;

public interface MvtStockRepository extends JpaRepository<MvtStock, Integer> {

    @Query("select coalesce(sum(m.quantite), 0) from MvtStock m where m.article.id = :idArticle")
    BigDecimal stockReelArticle(@Param("idArticle") Integer idArticle);

//    @Query("select sum(m.quantite) from MvtStock m where m.article.id = :idArticle")
//    BigDecimal stockReelArticle(@Param("idArticle") Integer idArticle);

    List<MvtStock> findAllByArticleId(Integer idArticle);

    List<MvtStock> findAllByIdEntreprise(Integer idEntreprise);

}
