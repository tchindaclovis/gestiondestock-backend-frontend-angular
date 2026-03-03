package com.tchindaClovis.gestiondestock.services;

import org.springframework.web.multipart.MultipartFile;
import java.io.InputStream;

public interface MinioService {
    String savePhoto(InputStream photo, String title);

    /**
     * Sauvegarder une photo avec un nom spécifique
     */
    String savePhoto(InputStream photo, String title, String filename);


    /**
     * Sauvegarde une photo à partir d'un MultipartFile
     */
//    String savePhoto(MultipartFile file, String title);

    /**
     * Supprimer une photo
     */
    void deletePhoto(String photoUrl);

    /**
     * Vérifier si une photo existe
     */
    boolean photoExists(String photoUrl);
}







//package com.tchindaClovis.gestiondestock.services;
//
//import java.io.InputStream;
//
//public interface MinioService {
//    String savePhoto(InputStream photo, String title);
//}
