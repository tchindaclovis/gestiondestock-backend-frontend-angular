package com.tchindaClovis.gestiondestock.repository;

public interface DocumentRepository{

}








//public interface DocumentRepository<E extends Document> extends JpaRepository<E, Long> {
//
//    E findByProprietaire_MatriculeAndExercice(String matricule, String exercice);
//
//    E findByCode(String code);
//
//    Page<E> findByProprietaire_MatriculeContainingIgnoreCase(String matricule, Pageable pageable);
//
//    Page<E> findByExerciceAndEtat(String exercice, EEtatDocument atat, Pageable pageable);
//
//    Page<E> findByExerciceAndEtatAndProprietaire_Matricule(String exercice, EEtatDocument atat, String matricule,
//                                                           Pageable pageable);
//
//    Set<E> findByExerciceAndProprietaire_Matricule(String exercice, String matricule);
//
//    Page<E> findByExerciceAndEtatIn(String exercice, List<String> etats, Pageable pageable);
//
//    List<E> findByExerciceAndEtatIn(String exercice, List<EEtatDocument> etats);
//
//    Page<E> findByExerciceAndEtatInAndProprietaire_MatriculeContainingIgnoreCase(String exercice, List<String> etats,
//                                                                                 String matricule, Pageable pageable);
//
//    Set<E> findByExerciceAndEtat(String exercice, String etat);
//
//    Set<E> findByExerciceAndEtatAndProprietaire_Matricule(String exercice, String etat, String matricule);
//
//    Page<E> findByExercice(String exercice, Pageable pageable);
//
//    Set<E> findByExercice(String exercice);
//
//    Page<E> findByExerciceAndProprietaire_Matricule(String exercice, String matricule, Pageable pageable);
//
//    Page<E> findByExerciceAndEtatNotIn(String exercice, List<String> etats, Pageable pageable);
//
//    Page<E> findByExerciceAndEtatNotInAndProprietaire_Matricule(String exercice, List<String> etats, String matricule,
//                                                                Pageable pageable);
//
//    E findByExerciceAndProprietaireMatricule(String exercise, String matricule);
//
//    List<E> findByIdIsIn(List<Long> ids);
//
//    List<E> findByExerciceAndEtatAndType(String exercice, EEtatDocument etats, ETypeDocument type);
//
//    Double countByDestinataireAndExerciceAndEtat(String matricule, String exercice, EEtatDocument etat);
//
//    Double countByExerciceAndEtat(String exercice, EEtatDocument etat);
//}

