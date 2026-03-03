package com.tchindaClovis.gestiondestock.services.impl;

import com.tchindaClovis.gestiondestock.services.MinioService;
import io.minio.*;
import io.minio.http.Method;
import lombok.extern.slf4j.Slf4j;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.io.InputStream;
import java.util.UUID;

@Service
@Slf4j
//@RequiredArgsConstructor
public class MinioServiceImpl implements MinioService {
    private final MinioClient minioClient;

    @Value("${minio.bucket-name:pictures}")
    private String bucketName;

    @Value("${minio.public-url:false}")
    private boolean usePublicUrl;

    @Value("${minio.presigned-expiry:604800}") // 7 jours par défaut
    private Integer presignedExpiry;

    // Répertoire par défaut pour les photos
    private static final String PHOTOS_DIRECTORY = "photos/";

    @Autowired
    public MinioServiceImpl(MinioClient minioClient) {
        this.minioClient = minioClient;
    }

    // Initialisation après injection des @Value
    @PostConstruct
    public void init() {
        try {
            initializeBucket();
            log.info("✅ Service MinIO initialisé avec succès - Bucket: {}", bucketName);
        } catch (Exception e) {
            log.error("❌ Erreur initialisation MinIO: {}", e.getMessage());
            // Ne pas bloquer le démarrage de l'application
        }
    }

//    private void initializeBucket() {
//        try {
//            boolean found = minioClient.bucketExists(
//                    io.minio.BucketExistsArgs.builder()
//                            .bucket(bucketName)
//                            .build()
//            );
//            if (!found) {
//                minioClient.makeBucket(
//                        io.minio.MakeBucketArgs.builder()
//                                .bucket(bucketName)
//                                .build()
//                );
//                log.info("Bucket MinIO créé: {}", bucketName);
//            }
//        } catch (Exception e) {
//            log.error("Erreur lors de l'initialisation du bucket MinIO: {}", e.getMessage());
//            throw new RuntimeException("Erreur initialisation bucket MinIO: " + e.getMessage(), e);
//        }
//    }


    @Override
    public String savePhoto(InputStream photo, String title) {
        String filename = generateFilename(title);
        return savePhotoToMinio(photo, filename);
    }

    @Override
    public String savePhoto(InputStream photo, String title, String customFilename) {
        String filename = PHOTOS_DIRECTORY + customFilename;
        return savePhotoToMinio(photo, filename);
    }

    /**
     * Initialiser le bucket s'il n'existe pas
     */
    private void initializeBucket() {
        try {
            // ✅ MAINTENANT bucketName est injecté
            boolean bucketExists = minioClient.bucketExists(
                    BucketExistsArgs.builder()
                            .bucket(bucketName) // Plus null!
                            .build()
            );

            if (!bucketExists) {
                minioClient.makeBucket(
                        MakeBucketArgs.builder()
                                .bucket(bucketName)
                                .build()
                );
                log.info("Bucket MinIO créé: {}", bucketName);
            }
        } catch (Exception e) {
            log.warn("Impossible d'initialiser le bucket MinIO: {}", e.getMessage());
        }
    }

    @Override
    public void deletePhoto(String photoUrl) {
        try {
            // Extraire le filename de l'URL
            String filename = extractFilenameFromUrl(photoUrl);

            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(filename)
                            .build()
            );

            log.info("Photo supprimée: {}", filename);
        } catch (Exception e) {
            log.error("Erreur lors de la suppression de la photo {}: {}", photoUrl, e.getMessage());
            throw new RuntimeException("Erreur suppression photo MinIO: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean photoExists(String photoUrl) {
        try {
            String filename = extractFilenameFromUrl(photoUrl);

            minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(bucketName)
                            .object(filename)
                            .build()
            );
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Méthode privée pour uploader la photo vers MinIO
     */
    private String savePhotoToMinio(InputStream photo, String filename) {
        try {
            // S'assurer que le bucket existe
            initializeBucket();

            // Upload vers MinIO
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(filename)
                            .stream(photo, -1, 10485760) // Taille maximale de 10MB
                            .contentType("image/jpeg")
                            .build()
            );

            log.info("Photo sauvegardée dans MinIO: {}", filename);

            // Retourner l'URL de la photo
            return generatePhotoUrl(filename);

        } catch (Exception e) {
            log.error("Erreur lors de la sauvegarde de la photo dans MinIO: {}", e.getMessage());
            throw new RuntimeException("Erreur sauvegarde photo MinIO: " + e.getMessage(), e);
        }
    }

    /**
     * Générer l'URL de la photo (URL publique ou pré-signée)
     */
    private String generatePhotoUrl(String filename) {
        try {
            // TOUJOURS utiliser les URLs pré-signées - plus sécurisé
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET) // ✅ Indiquer la méthode
                            .bucket(bucketName)
                            .object(filename)
                            .expiry(presignedExpiry)
                            .build()
            );
        } catch (Exception e) {
            log.error("Erreur lors de la génération de l'URL: {}", e.getMessage());
            throw new RuntimeException("Erreur génération URL MinIO: " + e.getMessage(), e);
        }
    }

    /**
     * Générer un nom de fichier unique
     */
    private String generateFilename(String title) {
        String safeTitle = title != null ?
                title.replaceAll("[^a-zA-Z0-9.-]", "_") : "photo";
        String uuid = UUID.randomUUID().toString();
        return PHOTOS_DIRECTORY + safeTitle + "_" + uuid + ".jpeg";
    }

    /**
     * Extraire le nom de fichier de l'URL
     */
    private String extractFilenameFromUrl(String photoUrl) {
        if (photoUrl == null) {
            throw new IllegalArgumentException("URL de photo ne peut pas être null");
        }

        // Selon le format d'URL MinIO, extraire le filename après le bucket
        String[] parts = photoUrl.split(bucketName + "/");
        if (parts.length > 1) {
            // Supprimer les paramètres de requête s'ils existent
            String filenameWithParams = parts[1];
            return filenameWithParams.split("\\?")[0];
        }

        // Si on ne peut pas parser, retourner l'URL complète
        log.warn("Impossible d'extraire le filename de l'URL: {}, utilisation complète", photoUrl);
        return photoUrl;
    }

}



//    private String generatePhotoUrl(String filename) {
//        try {
//            if (usePublicUrl) {
//                // URL publique directe (nécessite que le bucket soit public)
//                return minioClient.getObjectUrl(bucketName, filename);
//            } else {
//                // URL pré-signée avec expiration
//                return minioClient.getPresignedObjectUrl(
//                        io.minio.GetPresignedObjectUrlArgs.builder()
//                                .bucket(bucketName)
//                                .object(filename)
//                                .expiry(presignedExpiry)
//                                .build()
//                );
//            }
//        } catch (Exception e) {
//            log.error("Erreur lors de la génération de l'URL: {}", e.getMessage());
//            throw new RuntimeException("Erreur génération URL MinIO: " + e.getMessage(), e);
//        }
//    }
