package com.tchindaClovis.gestiondestock.services.impl;

import com.tchindaClovis.gestiondestock.dto.CategoryDto;
import com.tchindaClovis.gestiondestock.exception.EntityNotFoundException;
import com.tchindaClovis.gestiondestock.exception.ErrorCodes;
import com.tchindaClovis.gestiondestock.exception.InvalidEntityException;
import com.tchindaClovis.gestiondestock.exception.InvalidOperationException;
import com.tchindaClovis.gestiondestock.model.Utilisateur;
import com.tchindaClovis.gestiondestock.model.Article;
import com.tchindaClovis.gestiondestock.model.Category;
import com.tchindaClovis.gestiondestock.repository.ArticleRepository;
import com.tchindaClovis.gestiondestock.repository.CategoryRepository;
import com.tchindaClovis.gestiondestock.repository.UtilisateurRepository;
import com.tchindaClovis.gestiondestock.services.CategoryService;
import com.tchindaClovis.gestiondestock.validator.CategoryValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class CategoryServiceImpl implements CategoryService {

    private CategoryRepository categoryRepository;
    private ArticleRepository articleRepository;

    private UtilisateurRepository utilisateurRepository;

    @Autowired
    public CategoryServiceImpl(CategoryRepository categoryRepository, ArticleRepository articleRepository,
                               UtilisateurRepository utilisateurRepository) {
        this.categoryRepository = categoryRepository;
        this.articleRepository = articleRepository;
        this.utilisateurRepository = utilisateurRepository;
    }


    @Override
    public CategoryDto save(CategoryDto dto) {

        // 1. Validation de l'objet CategoryDto
        List<String> errors = CategoryValidator.validate(dto);
        if (!errors.isEmpty()) {
            log.error("Category is not valid {}", dto);
            throw new InvalidEntityException("La catégorie n'est pas valide", ErrorCodes.CATEGORY_NOT_VALID, errors);
        }

        // 2. Récupérer l'utilisateur connecté depuis le contexte de sécurité
        String connectedUserEmail = null;
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (principal instanceof org.springframework.security.core.userdetails.UserDetails) {
            connectedUserEmail = ((org.springframework.security.core.userdetails.UserDetails) principal).getUsername();
        } else {
            connectedUserEmail = principal.toString();
        }

        // Récupération de l'utilisateur complet en BDD
        Utilisateur loggedInUser = utilisateurRepository.findUtilisateurByEmail(connectedUserEmail)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Utilisateur connecté introuvable",
                        ErrorCodes.UTILISATEUR_NOT_FOUND));

        // 3. RÈGLE DE GESTION
        if (dto.getId() != null) {
            // Mode Modification : On récupère la catégorie existante en BDD avant mise à jour
            Category existingCategory = categoryRepository.findById(dto.getId())
                    .orElseThrow(() -> new EntityNotFoundException(
                            "Aucune catégorie trouvée avec l'ID = " + dto.getId(),
                            ErrorCodes.CATEGORY_NOT_FOUND));

            // On vérifie si l'utilisateur connecté est le créateur originel
            // (Note : Assurez-vous que votre entité Category possède le champ idUtilisateur ou utilisateur)
            if (existingCategory.getIdUtilisateur() != null && !existingCategory.getIdUtilisateur().equals(loggedInUser.getId())) {
                log.warn("L'utilisateur {} a tenté de modifier la catégorie {} sans en être le créateur", loggedInUser.getId(), dto.getId());
                throw new InvalidOperationException(
                        "Vous n'êtes pas autorisé à modifier cette catégorie car vous n'en êtes pas le créateur originel.",
                        ErrorCodes.UTILISATEUR_CHANGE_CATEGORY_OBJECT_NOT_VALID);
            }
        } else {
            // Mode Création : On injecte l'ID de l'utilisateur connecté comme créateur
            dto.setIdUtilisateur(loggedInUser.getId());

            // Optionnel : Si vos catégories dépendent aussi de l'entreprise de l'utilisateur
            if (loggedInUser.getEntreprise() != null) {
                dto.setIdEntreprise(loggedInUser.getEntreprise().getId());
            }
        }

        // 4. Sauvegarde
        return CategoryDto.fromEntity(categoryRepository.save(CategoryDto.toEntity(dto)));

    }


//    @Override
//    public CategoryDto save(CategoryDto dto) {
//        // 1. Validation de l'objet CategoryDto
//        List<String> errors = CategoryValidator.validate(dto);
//        if (!errors.isEmpty()) {
//            log.error("Category is not valid {}", dto);
//            throw new InvalidEntityException("La catégorie n'est pas valide", ErrorCodes.CATEGORY_NOT_VALID, errors);
//        }
//
//        // 2. Récupérer l'email de l'utilisateur actuellement connecté via Spring Security
//        String connectedUserEmail = null;
//        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
//
//        if (principal instanceof UserDetails) {
//            connectedUserEmail = ((UserDetails) principal).getUsername();
//        } else {
//            connectedUserEmail = principal.toString();
//        }
//
//        // Récupérer l'ID de l'utilisateur connecté depuis la BDD
//        Integer connectedUserId = utilisateurRepository.findUtilisateurByEmail(connectedUserEmail)
//                .orElseThrow(() -> new EntityNotFoundException(
//                        "Utilisateur connecté introuvable en base de données",
//                        ErrorCodes.UTILISATEUR_NOT_FOUND))
//                .getId();
//
//        // 3. CAS D'UNE MODIFICATION : Vérifier si l'utilisateur est bien le créateur originel
//        if (dto.getId() != null) {
//            Category existingCategory = categoryRepository.findById(dto.getId())
//                    .orElseThrow(() -> new EntityNotFoundException(
//                            "Aucune catégorie trouvée avec l'ID = " + dto.getId(),
//                            ErrorCodes.CATEGORY_NOT_FOUND));
//
//            // Comparer l'ID du créateur enregistré avec l'ID de l'utilisateur connecté
//            // /!\ Remplacez "getIdUtilisateur()" par le nom exact de votre champ de traçabilité dans l'entité Category
//            if (existingCategory.getIdUtilisateur() != null && !existingCategory.getIdUtilisateur().equals(connectedUserId)) {
//                log.warn("L'utilisateur ID {} a tenté de modifier la catégorie ID {} sans en être le créateur", connectedUserId, dto.getId());
//                throw new InvalidOperationException(
//                        "Vous n'êtes pas autorisé à modifier cette catégorie car vous n'en êtes pas le créateur originel.",
//                        ErrorCodes.UTILISATEUR_CHANGE_PASSWORD_OBJECT_NOT_VALID // Adaptez ou créez un code d'erreur spécifique comme CATEGORY_MODIFICATION_NOT_ALLOWED
//                );
//            }
//        } else {
//            // CAS D'UNE CRÉATION : On associe automatiquement l'ID de l'utilisateur connecté comme créateur
//            // /!\ Adaptez selon la structure de votre DTO (ex: dto.setIdUtilisateur ou dto.setUtilisateur)
//            dto.setIdUtilisateur(connectedUserId);
//        }
//
//        // 4. Sauvegarde finale
//        Category savedCategory = categoryRepository.save(CategoryDto.toEntity(dto));
//        return CategoryDto.fromEntity(savedCategory);
//
//}


//    @Override
//    public CategoryDto save(CategoryDto dto) {
//
//        // 1. Validation de l'objet CategoryDto
//        List<String> errors = CategoryValidator.validate(dto);
//        if (!errors.isEmpty()) {
//            log.error("Category is not valid {}", dto);
//            throw new InvalidEntityException("La catégorie n'est pas valide", ErrorCodes.CATEGORY_NOT_VALID, errors);
//        }
//
//        return CategoryDto.fromEntity(categoryRepository.save(CategoryDto.toEntity(dto)));
//    }

    @Override
    public CategoryDto findById(Integer id) {
        if(id == null){
            log.error("Category ID is null");
            return null;
        }
        return categoryRepository.findById(id)
                .map(CategoryDto::fromEntity)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Aucune categorie avec l'ID = " + id + " n'a ete trouve dans la BDD",
                        ErrorCodes.CATEGORY_NOT_FOUND)
                );
    }

    @Override
    public CategoryDto findByCode(String code) {
        if(!StringUtils.hasLength(code)){
            log.error("Category CODE is null");
            return null;
        }
        Optional<Category> category = categoryRepository.findCategoryByCode(code);

        return Optional.of(CategoryDto.fromEntity(category.get())).orElseThrow(() ->
                new EntityNotFoundException(
                        "Aucune categorie avec le CODE = " + code + "n'a ete trouve dans la BDD",
                        ErrorCodes.CATEGORY_NOT_FOUND)
        );
    }


    @Override
    public List<CategoryDto> findAll() {
        return categoryRepository.findAll().stream()
                .map(CategoryDto::fromEntity)
                .collect(Collectors.toList());
    }


    @Override
    public void delete(Integer id) {
        // 1. Vérification de la présence de l'ID
        if (id == null) {
            log.error("Category ID is null");
            return;
        }

        // 2. Récupérer la catégorie en base de données pour connaître son créateur
        Category existingCategory = categoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Aucune catégorie trouvée avec l'ID = " + id,
                        ErrorCodes.CATEGORY_NOT_FOUND));

        // 3. Récupérer l'email de l'utilisateur actuellement connecté via Spring Security
        String connectedUserEmail = null;
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (principal instanceof org.springframework.security.core.userdetails.UserDetails) {
            connectedUserEmail = ((UserDetails) principal).getUsername();
        } else {
            connectedUserEmail = principal.toString();
        }

        // 4. Récupérer l'ID de l'utilisateur connecté depuis la BDD
        Utilisateur loggedInUser = utilisateurRepository.findUtilisateurByEmail(connectedUserEmail)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Utilisateur connecté introuvable",
                        ErrorCodes.UTILISATEUR_NOT_FOUND));

        // 5. RÈGLE DE GESTION : Vérifier si l'utilisateur connecté est le créateur originel
        // (Note : Assurez-vous que "existingCategory.getIdUtilisateur()" correspond à votre champ d'audit)
        if (existingCategory.getIdUtilisateur() != null && !existingCategory.getIdUtilisateur().equals(loggedInUser.getId())) {
            log.warn("L'utilisateur ID {} a tenté de supprimer la catégorie ID {} appartenant à l'utilisateur ID {}",
                    loggedInUser.getId(), id, existingCategory.getIdUtilisateur());
            throw new InvalidOperationException(
                    "Vous n'êtes pas autorisé à supprimer cette catégorie car vous n'en êtes pas le créateur originel.",
                    ErrorCodes.UTILISATEUR_CHANGE_CATEGORY_OBJECT_NOT_VALID // Vous pouvez utiliser ou adapter ce code d'erreur
            );
        }

        // 6. Vérification si la catégorie est liée à des articles (votre code d'origine)
        List<Article> articles = articleRepository.findAllByCategoryId(id);
        if (!articles.isEmpty()) {
            throw new InvalidOperationException("Impossible de supprimer cette categorie qui est deja utilise",
                    ErrorCodes.CATEGORY_ALREADY_IN_USE);
        }

        // 7. Suppression effective
        categoryRepository.deleteById(id);
        log.info("La catégorie avec l'ID {} a été supprimée avec succès par son créateur ID {}", id, loggedInUser.getId());
    }


//    @Override
//    public void delete(Integer id) {
//        if (id == null) {
//            log.error("Category ID is null");
//            return;
//        }
//        List<Article> articles = articleRepository.findAllByCategoryId(id);
//        if (!articles.isEmpty()) {
//            throw new InvalidOperationException("Impossible de supprimer cette categorie qui est deja utilise",
//                    ErrorCodes.CATEGORY_ALREADY_IN_USE);
//        }
//        categoryRepository.deleteById(id);
//    }


    @Override
    public String getLastCodeCategory() {
        // CORRECTION : Utilisez l'instance "categoryRepository" (minuscule)
        // et extrayez le code de l'objet Category
        return categoryRepository.findTopByOrderByCodeDesc()
                .map(Category::getCode) // On transforme l'Article en String (son code)
                .orElse("CAT000");           // Valeur par défaut si aucune Category n'existe
    }
}
