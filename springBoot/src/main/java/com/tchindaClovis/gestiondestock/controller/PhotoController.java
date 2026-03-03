package com.tchindaClovis.gestiondestock.controller;

import com.tchindaClovis.gestiondestock.controller.api.PhotoApi;
import com.tchindaClovis.gestiondestock.services.strategy.StrategyPhotoContext;
import com.tchindaClovis.gestiondestock.services.MinioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import io.minio.errors.MinioException;

@RestController
//@RequiredArgsConstructor
public class PhotoController implements PhotoApi {
    private final MinioService minioService;
    private final StrategyPhotoContext strategyPhotoContext;
    @Autowired
    public PhotoController(MinioService minioService, StrategyPhotoContext strategyPhotoContext) {
        this.minioService = minioService;
        this.strategyPhotoContext = strategyPhotoContext;
    }
//    @Override
//    public String uploadPhoto(MultipartFile file, String title) {
//        try {
//            // Utilisation équivalente à l'ancien service Flickr
//            return minioService.savePhoto(file.getInputStream(),
//                    title != null ? title : file.getOriginalFilename());
//        } catch (IOException e) {
//            throw new RuntimeException("Erreur lecture fichier: " + e.getMessage(), e);
//        }
//    }

    @Override
    @PreAuthorize("hasAnyRole('minioadmin','minioadmin')")
    public Object savePhoto(String context, Integer id, MultipartFile photo, String title)
            throws IOException, MinioException {
        return strategyPhotoContext.savePhoto(context, id, photo.getInputStream(), title);
    }


    @Override
    public void deletePhoto(String photoUrl) {
        minioService.deletePhoto(photoUrl);
    }
}




//    private final StrategyPhotoContext strategyPhotoContext;
//
//    @Autowired
//    public PhotoController(StrategyPhotoContext strategyPhotoContext) {
//        this.strategyPhotoContext = strategyPhotoContext;
//    }
//
//    @Override
//    @PreAuthorize("hasAnyRole('USER','ADMIN')")
//    public Object savePhoto(String context, Integer id, MultipartFile photo, String title) throws IOException, FlickrException {
//        return strategyPhotoContext.savePhoto(context, id, photo.getInputStream(), title);
//    }




//package com.tchindaClovis.gestiondestock.controller;
//
//import com.tchindaClovis.gestiondestock.services.strategy.StrategyPhotoContext;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//import org.springframework.web.multipart.MultipartFile;
//import static com.tchindaClovis.gestiondestock.utils.Constants.APP_ROOT;
//
//@RestController
//@RequestMapping(APP_ROOT + "/photos")
//public class PhotoController {
//
//    private final StrategyPhotoContext strategyPhotoContext;
//
//    public PhotoController(StrategyPhotoContext strategyPhotoContext) {
//        this.strategyPhotoContext = strategyPhotoContext;
//    }
//
//    @PostMapping("/{type}")
//    public ResponseEntity<String> uploadPhoto(
//            @PathVariable String type,
//            @RequestParam("photo") MultipartFile photo) {
//
//        try {
//            SavePhoto strategy = strategyPhotoContext.getStrategy(type.toUpperCase());
//            String filePath = strategy.savePhoto(photo);
//
//            return ResponseEntity.ok("Photo enregistrée avec succès: " + filePath);
//        } catch (Exception e) {
//            return ResponseEntity.badRequest().body("Erreur: " + e.getMessage());
//        }
//    }
//}










//package com.tchindaClovis.gestiondestock.controller;
//
//import com.tchindaClovis.gestiondestock.services.MinioService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//import java.io.ByteArrayInputStream;
//import java.io.InputStream;
//
//@RestController
//@RequestMapping("/api/photos")
//public class PhotoController {
//
//    private MinioService minioService;
//    @Autowired
//    public PhotoController(MinioService minioService) {
//        this.minioService = minioService;
//    }
//
//    @PostMapping("/upload")
//    public ResponseEntity<String> uploadPhoto(@RequestBody byte[] photoData,
//                                              @RequestParam String title) {
//        try {
//            InputStream photoStream = new ByteArrayInputStream(photoData);
//            String photoUrl = minioService.savePhoto(photoStream, title);
//            return ResponseEntity.ok(photoUrl);
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                    .body("Erreur d'upload: " + e.getMessage());
//        }
//    }
//}








//package com.tchindaClovis.gestiondestock.controller;
//
//        import com.tchindaClovis.gestiondestock.services.MinioService;
//        import org.springframework.core.io.InputStreamResource;
//        import org.springframework.http.HttpHeaders;
//        import org.springframework.http.MediaType;
//        import org.springframework.http.ResponseEntity;
//        import org.springframework.web.bind.annotation.*;
//        import org.springframework.web.multipart.MultipartFile;
//
//        import java.util.List;
//
//@RestController
//@RequestMapping("/api/files")
//public class MinioController {
//
//    private final MinioService minioService;
//
//    public MinioController(MinioService minioService) {
//        this.minioService = minioService;
//    }
//
//    @PostMapping("/upload")
//    public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file) {
//        try {
//            String filename = file.getOriginalFilename();
//            minioService.uploadFile(file, filename);
//            return ResponseEntity.ok("Fichier uploadé avec succès: " + filename);
//        } catch (Exception e) {
//            return ResponseEntity.internalServerError().body("Échec de l'upload: " + e.getMessage());
//        }
//    }
//
//    @GetMapping("/download/{filename}")
//    public ResponseEntity<InputStreamResource> downloadFile(@PathVariable String filename) {
//        try {
//            InputStream stream = minioService.downloadFile(filename);
//            InputStreamResource resource = new InputStreamResource(stream);
//
//            return ResponseEntity.ok()
//                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
//                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
//                    .body(resource);
//        } catch (Exception e) {
//            return ResponseEntity.notFound().build();
//        }
//    }
//
//    @GetMapping("/list")
//    public ResponseEntity<List<String>> listFiles() {
//        try {
//            List<String> files = minioService.listFiles();
//            return ResponseEntity.ok(files);
//        } catch (Exception e) {
//            return ResponseEntity.internalServerError().build();
//        }
//    }
//
//    @DeleteMapping("/delete/{filename}")
//    public ResponseEntity<String> deleteFile(@PathVariable String filename) {
//        try {
//            minioService.deleteFile(filename);
//            return ResponseEntity.ok("Fichier supprimé: " + filename);
//        } catch (Exception e) {
//            return ResponseEntity.internalServerError().body("Échec de la suppression: " + e.getMessage());
//        }
//    }
//
//    @GetMapping("/presigned-url/{filename}")
//    public ResponseEntity<String> getPresignedUrl(@PathVariable String filename) {
//        try {
//            String url = minioService.getPresignedUrl(filename);
//            return ResponseEntity.ok(url);
//        } catch (Exception e) {
//            return ResponseEntity.internalServerError().body("Erreur: " + e.getMessage());
//        }
//    }
//}






//    Contrôleur pour upload via MultipartFile
//
//@RestController
//@RequestMapping("/api/photos")
//public class PhotoController {
//
//    @Autowired
//    private MinioService minioService;
//
//    @PostMapping("/upload-multipart")
//    public ResponseEntity<String> uploadPhotoMultipart(@RequestParam("file") MultipartFile file,
//                                                       @RequestParam String title) {
//        try {
//            String photoUrl = minioService.savePhoto(file, title);
//            return ResponseEntity.ok(photoUrl);
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                    .body("Erreur d'upload: " + e.getMessage());
//        }
//    }
//
//    @DeleteMapping
//    public ResponseEntity<Void> deletePhoto(@RequestParam String photoUrl) {
//        try {
//            minioService.deletePhoto(photoUrl);
//            return ResponseEntity.ok().build();
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
//        }
//    }
//}
