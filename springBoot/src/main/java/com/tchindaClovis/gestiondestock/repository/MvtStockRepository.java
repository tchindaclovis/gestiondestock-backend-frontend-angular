package com.tchindaClovis.gestiondestock.repository;

import com.tchindaClovis.gestiondestock.model.Article;
import com.tchindaClovis.gestiondestock.model.ESourceMvtStock;
import com.tchindaClovis.gestiondestock.model.MvtStock;
import com.tchindaClovis.gestiondestock.model.Vente;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface MvtStockRepository extends JpaRepository<MvtStock, Integer> {

    @Query("select coalesce(sum(m.quantite), 0) from MvtStock m where m.article.id = :idArticle")
    BigDecimal stockReelArticle(@Param("idArticle") Integer idArticle);

//    @Query("select sum(m.quantite) from MvtStock m where m.article.id = :idArticle")
//    BigDecimal stockReelArticle(@Param("idArticle") Integer idArticle);

    List<MvtStock> findAllByArticleId(Integer idArticle);

    List<MvtStock> findAllByIdEntreprise(Integer idEntreprise);

    @Query("SELECT m.codeSource FROM MvtStock m " +
            "WHERE m.sourceMvt = :source " +
            "ORDER BY m.id DESC")
    List<String> findLastCodeBySource(@Param("source") ESourceMvtStock source, Pageable pageable);

    // On ajoute "SourceMvt" dans le nom de la méthode
//    Optional<MvtStock> findTopBySourceMvtOrderByCreationDateDesc(ESourceMvtStock sourceMvt);

//    Optional<MvtStock> findTopByOrderByCodeCorrectionDesc();

}
