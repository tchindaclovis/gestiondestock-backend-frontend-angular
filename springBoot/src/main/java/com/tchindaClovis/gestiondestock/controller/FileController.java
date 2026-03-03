//package com.tchindaClovis.gestiondestock.controller;
//
//import com.tchindaClovis.gestiondestock.services.MinioService;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//import org.springframework.web.multipart.MultipartFile;
//
//@RestController
//@RequestMapping("/files")
//public class FileController {
//
//    private final MinioService minioService;
//
//    public FileController(MinioService minioService) {
//        this.minioService = minioService;
//    }
//
//    @PostMapping("/upload")
//    public ResponseEntity<String> upload(@RequestParam("file") MultipartFile file) {
//        try {
//            minioService.uploadFile(file.getOriginalFilename(),
//                    file.getInputStream(),
//                    file.getContentType(),
//                    file.getSize());
//            return ResponseEntity.ok("Fichier uploadé avec succès !");
//        } catch (Exception e) {
//            return ResponseEntity.status(500).body("Erreur : " + e.getMessage());
//        }
//    }
//}

