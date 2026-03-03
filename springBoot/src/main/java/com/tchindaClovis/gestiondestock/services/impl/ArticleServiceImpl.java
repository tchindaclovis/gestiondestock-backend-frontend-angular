package com.tchindaClovis.gestiondestock.services.impl;

import com.tchindaClovis.gestiondestock.dto.ArticleDto;
import com.tchindaClovis.gestiondestock.dto.LigneCommandeClientDto;
import com.tchindaClovis.gestiondestock.dto.LigneCommandeFournisseurDto;
import com.tchindaClovis.gestiondestock.dto.LigneVenteDto;
import com.tchindaClovis.gestiondestock.exception.EntityNotFoundException;
import com.tchindaClovis.gestiondestock.exception.ErrorCodes;
import com.tchindaClovis.gestiondestock.exception.InvalidEntityException;
import com.tchindaClovis.gestiondestock.exception.InvalidOperationException;
import com.tchindaClovis.gestiondestock.model.Article;
import com.tchindaClovis.gestiondestock.model.LigneCommandeClient;
import com.tchindaClovis.gestiondestock.model.LigneCommandeFournisseur;
import com.tchindaClovis.gestiondestock.model.LigneVente;
import com.tchindaClovis.gestiondestock.repository.ArticleRepository;
import com.tchindaClovis.gestiondestock.repository.LigneCommandeClientRepository;
import com.tchindaClovis.gestiondestock.repository.LigneCommandeFournisseurRepository;
import com.tchindaClovis.gestiondestock.repository.LigneVenteRepository;
import com.tchindaClovis.gestiondestock.services.ArticleService;
import com.tchindaClovis.gestiondestock.validator.ArticleValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ArticleServiceImpl implements ArticleService {
    private ArticleRepository articleRepository;
    private LigneVenteRepository venteRepository;
    private LigneCommandeFournisseurRepository commandeFournisseurRepository;
    private LigneCommandeClientRepository commandeClientRepository;


    @Autowired
    public ArticleServiceImpl(ArticleRepository articleRepository,
                              LigneVenteRepository venteRepository,
                              LigneCommandeFournisseurRepository commandeFournisseurRepository,
                              LigneCommandeClientRepository commandeClientRepository) {
        this.articleRepository = articleRepository;
        this.venteRepository = venteRepository;
        this.commandeFournisseurRepository = commandeFournisseurRepository;
        this.commandeClientRepository = commandeClientRepository;
    }

    @Override
    public ArticleDto save(ArticleDto dto){
        List<String> errors = ArticleValidator.validate(dto);
        if(!errors.isEmpty()){
            log.error("Article is not valid{}", dto);
            throw new InvalidEntityException("L'article n'est pas valide", ErrorCodes.ARTICLE_NOT_VALID, errors);
        }
        return ArticleDto.fromEntity(
                articleRepository.save(
                        ArticleDto.toEntity(dto)
                )
        );
       // Article savedArticle = articleRepository.save(ArticleDto.toEntity(dto));
        //return ArticleDto.fromEntity(savedArticle);
    }

    @Override
    public ArticleDto findById(Integer id) {
        if(id == null){
            log.error("Article ID is null");
            return null;
        }
        Optional<Article> article = articleRepository.findById(id);

        return Optional.of(ArticleDto.fromEntity(article.get())).orElseThrow(() ->
                new EntityNotFoundException(
                        "Aucun article avec l'ID = " + id + "n'a ete trouve dans la BDD",
                        ErrorCodes.ARTICLE_NOT_FOUND)
        );
    }

    @Override

    public ArticleDto findByCodeArticle(String codeArticle) {
        if (!StringUtils.hasLength(codeArticle)) {
            log.error("Article CODE is null");
            return null;
        }

        return articleRepository.findArticleByCodeArticle(codeArticle)
                .map(ArticleDto::fromEntity)
                .orElseThrow(() ->
                        new EntityNotFoundException(
                                "Aucun article avec le CODE = " + codeArticle + " n' ete trouve dans la BDD",
                                ErrorCodes.ARTICLE_NOT_FOUND)
                );
    }


//    public ArticleDto findByCodeArticle(String codeArticle) {
//            if(!StringUtils.hasLength(codeArticle)){
//                log.error("Article CODE is null");
//                return null;
//            }
//        Optional<Article> article = articleRepository.findArticleByCodeArticle(codeArticle);
//
//        return Optional.of(ArticleDto.fromEntity(article.get())).orElseThrow(() ->
//                new EntityNotFoundException(
//                        "Aucun article avec le CODE = " + codeArticle + "n'a ete trouve dans la BDD",
//                        ErrorCodes.ARTICLE_NOT_FOUND)
//        );
//    }

    @Override
    public List<ArticleDto> findAll() {
        return articleRepository.findAll().stream()
                .map(ArticleDto::fromEntity)
                .collect(Collectors.toList());
    }


    @Override
    public List<ArticleDto> findAllArticleByIdCategory(Integer idCategory) {
        return articleRepository.findAllByCategoryId(idCategory).stream()
                .map(ArticleDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<LigneVenteDto> findHistoriqueVentes(Integer idArticle) {
        return venteRepository.findAllByArticleId(idArticle).stream()
                .map(LigneVenteDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<LigneCommandeClientDto> findHistoriaueCommandeClient(Integer idArticle) {
        return commandeClientRepository.findAllByArticleId(idArticle).stream()
                .map(LigneCommandeClientDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<LigneCommandeFournisseurDto> findHistoriqueCommandeFournisseur(Integer idArticle) {
        return commandeFournisseurRepository.findAllByArticleId(idArticle).stream()
                .map(LigneCommandeFournisseurDto::fromEntity)
                .collect(Collectors.toList());
    }


    @Override
    public void delete(Integer id) {
        if (id == null) {
            log.error("Article ID is null");
            return;
        }
        List<LigneCommandeClient> ligneCommandeClients = commandeClientRepository.findAllByArticleId(id);
        if (!ligneCommandeClients.isEmpty()) {
            throw new InvalidOperationException("Impossible de supprimer un article deja utilise dans des commandes client",
                    ErrorCodes.ARTICLE_ALREADY_IN_USE);
        }
        List<LigneCommandeFournisseur> ligneCommandeFournisseurs = commandeFournisseurRepository.findAllByArticleId(id);
        if (!ligneCommandeFournisseurs.isEmpty()) {
            throw new InvalidOperationException("Impossible de supprimer un article deja utilise dans des commandes fournisseur",
                    ErrorCodes.ARTICLE_ALREADY_IN_USE);
        }
        List<LigneVente> ligneVentes = venteRepository.findAllByArticleId(id);
        if (!ligneVentes.isEmpty()) {
            throw new InvalidOperationException("Impossible de supprimer un article deja utilise dans des ventes",
                    ErrorCodes.ARTICLE_ALREADY_IN_USE);
        }
        articleRepository.deleteById(id);
    }
}








//package com.tchindaClovis.gestiondestock.services.impl;
//
//import com.tchindaClovis.gestiondestock.dto.ArticleDto;
//import com.tchindaClovis.gestiondestock.exception.EntityNotFoundException;
//import com.tchindaClovis.gestiondestock.exception.ErrorCodes;
//import com.tchindaClovis.gestiondestock.exception.InvalidEntityException;
//import com.tchindaClovis.gestiondestock.model.Article;
//import com.tchindaClovis.gestiondestock.repository.ArticleRepository;
//import com.tchindaClovis.gestiondestock.services.ArticleService;
//import com.tchindaClovis.gestiondestock.validator.ArticleValidator;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//import org.springframework.util.StringUtils;
//import java.util.List;
//import java.util.Optional;
//import java.util.stream.Collectors;
//
//@Service
//@Slf4j
//public class ArticleServiceImpl implements ArticleService {
//    private ArticleRepository articleRepository;
//
//    @Autowired
//    public ArticleServiceImpl(ArticleRepository articleRepository){
//
//      this.articleRepository = articleRepository;
//    }
//
//    @Override
//    public ArticleDto save(ArticleDto dto){
//        List<String> errors = ArticleValidator.validate(dto);
//        if(!errors.isEmpty()){
//            log.error("Article is not valid{}", dto);
//            throw new InvalidEntityException("L'article n'est pas valide", ErrorCodes.ARTICLE_NOT_VALID, errors);
//        }
//        return ArticleDto.fromEntity(
//                articleRepository.save(
//                        ArticleDto.toEntity(dto)
//                )
//        );
//       // Article savedArticle = articleRepository.save(ArticleDto.toEntity(dto));
//        //return ArticleDto.fromEntity(savedArticle);
//    }
//
//    @Override
//    public ArticleDto findById(Integer id) {
//        if(id == null){
//            log.error("Article ID is null");
//            return null;
//        }
//        Optional<Article> article = articleRepository.findById(id);
//
//        return Optional.of(ArticleDto.fromEntity(article.get())).orElseThrow(() ->
//                new EntityNotFoundException(
//                        "Aucun article avec l'ID = " + id + "n'a ete trouve dans la BDD",
//                        ErrorCodes.ARTICLE_NOT_FOUND)
//        );
//    }
//
//    @Override
//    public ArticleDto findByCodeArticle(String codeArticle) {
//            if(!StringUtils.hasLength(codeArticle)){
//                log.error("Article CODE is null");
//                return null;
//            }
//        Optional<Article> article = articleRepository.findArticleByCodeArticle(codeArticle);
//
//        return Optional.of(ArticleDto.fromEntity(article.get())).orElseThrow(() ->
//                new EntityNotFoundException(
//                        "Aucun article avec le CODE = " + codeArticle + "n'a ete trouve dans la BDD",
//                        ErrorCodes.ARTICLE_NOT_FOUND)
//        );
//    }
//
//    @Override
//    public List<ArticleDto> findAll() {
//        return articleRepository.findAll().stream()
//                .map(ArticleDto::fromEntity)
//                .collect(Collectors.toList());
//    }
//
//    @Override
//    public void delete(Integer id) {
//        if(id == null){
//            log.error("Article ID is null");
//            return;
//        }
//        articleRepository.deleteById(id);
//    }
//}
