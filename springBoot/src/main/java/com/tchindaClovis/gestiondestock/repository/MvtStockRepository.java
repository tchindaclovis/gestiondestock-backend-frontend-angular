package com.tchindaClovis.gestiondestock.repository;

import com.tchindaClovis.gestiondestock.model.ESourceMvtStock;
import com.tchindaClovis.gestiondestock.model.MvtStock;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface MvtStockRepository extends JpaRepository<MvtStock, Integer> {

    // 1. Calcul classique du stock réel
    @Query("SELECT COALESCE(SUM(m.quantite), 0) FROM MvtStock m WHERE m.article.id = :idArticle")
    BigDecimal stockReelArticle(@Param("idArticle") Integer idArticle);

    // 2. Calcul du stock avec ajout de |quantité| pour les mouvements liés à une commande client
    @Query("SELECT COALESCE(SUM(" +
            "  m.quantite + " +
            "  CASE " +
            "    WHEN m.codeCommandeClient IS NOT NULL AND TRIM(m.codeCommandeClient) <> '' " +
            "    THEN ABS(m.quantite) " +
            "    ELSE 0 " +
            "  END" +
            "), 0) " +
            "FROM MvtStock m " +
            "WHERE m.article.id = :idArticle")
    BigDecimal stockReelArticleVenteIssueDeCommande(@Param("idArticle") Integer idArticle);

    // 3. Vérifie si l'article possède au moins un mouvement de stock avec un codeCommandeClient
    @Query("SELECT COUNT(m) > 0 FROM MvtStock m " +
            "WHERE m.article.id = :idArticle " +
            "AND m.codeCommandeClient IS NOT NULL " +
            "AND TRIM(m.codeCommandeClient) <> ''")
    boolean existsByArticleIdAndCodeCommandeClientIsNotNull(@Param("idArticle") Integer idArticle);

    Optional<MvtStock> findByCodeCommandeClient(String codeCommandeClient);


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




//        @Query("SELECT COALESCE(SUM(" +
//                "  m.quantite + " +
//                "  CASE " +
//                "    WHEN m.typeMvt = 'VENTE' AND v.codeCommandeClient IS NOT NULL AND TRIM(v.codeCommandeClient) <> '' " +
//                "    THEN ABS(m.quantite) " +
//                "    ELSE 0 " +
//                "  END" +
//                "), 0) " +
//                "FROM MvtStock m " +
//                "LEFT JOIN Vente v ON m.codeSource = v.code " +
//                "WHERE m.article.id = :idArticle")
//        BigDecimal stockReelArticle(@Param("idArticle") Integer idArticle);



//    @Query("select sum(m.quantite) from MvtStock m where m.article.id = :idArticle")
//    BigDecimal stockReelArticle(@Param("idArticle") Integer idArticle);
