package com.tchindaClovis.gestiondestock.services;

public interface DocumentService{

}




//public interface DocumentService<E extends Document>  {
//
//    Result<E> findEntityByCode(String code, String langCode);
//
//    Result<E> sendToDerogation(User user, E record, String matDestinataire, EActionDerogation action, String langCode)
//            throws Exception;
//
//    Result<E> sendToValidation(User user, E record, String matricule, String destinataire, EActionDocument action,
//                               String langCode) throws Exception;
//
//    boolean isNeedDerogation(E record, EActionDocument action);
//
//    Result<E> transfert(User user, E record, String langCode) throws Exception;
//
//    Result<E> transfertDerogation(User user, E record, String langCode) throws Exception;
//
//    Result<E> rejeter(User user, E record, String motif, String langCode) throws Exception;
//
//    Result<E> rejeterDerogation(User user, E record, String motif, String langCode) throws Exception;
//
//    boolean isNeedDerogation(String codeDocument, EActionDocument action);
//
//    Result<E> reinitialiser(User user, E record, String langCode) throws Exception;
//
//    Result<E> transfert(User user, List<E> record, String langCode) throws Exception;
//
//    void setClazz(Class<E> zz);
//
//    Class<E> getClazz();
//
//    Result<E> findPageByExercice(String exercice, String lang);
//
//    Result<E> findPageByExerciceAndMatricule(String exercice, String matricule, String lang);
//
//    Result<E>  findByExerciceAndProprietaireMatricule(String exercise,String matricule);
//
//
//    Result<E> findByExerciceAndProprietaireMatricule(String exercise, String matricule, String lang);
//
//    Result<E> findPageByExerciceAndMatriculeAndPeriodeNumero(String exercice, String matricule, Integer numero);
//
//    List<SearchCriteria> getElementManagerCriteria(List<SearchCriteria> criterias, User user);
//
//
//    Result<E> validationManager(User user, List<Long> ids, String matricule, EActionDocument action, String langCode)
//            throws Exception;
//
//    Result<E> validationRH(User user, List<Long> ids, String matricule, EActionDocument action, String langCode)
//            throws Exception;
//
//    Result<E> rejetManager(User user, ObjectMappRejetMotif data, String matricule, EActionDocument action, String langCode)
//            throws Exception;
//
//    Result<E> rejetRH(User user, ObjectMappRejetMotif data, String matricule, EActionDocument action, String langCode)
//            throws Exception;
//
//    Result<E> reinitialiser(User user, List<Long> ids, String langCode) throws Exception;
//
//    Result<Document> transferChezAutreManager(List<Long> docs, User user, String matDestinataire) throws Exception;
//}
