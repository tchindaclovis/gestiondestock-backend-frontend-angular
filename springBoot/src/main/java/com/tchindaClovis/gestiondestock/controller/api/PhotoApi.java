package com.tchindaClovis.gestiondestock.controller.api;

import static com.tchindaClovis.gestiondestock.utils.Constants.APP_ROOT;
import java.io.IOException;
import io.minio.errors.MinioException;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "Photo", description = "API de gestion des photos")
public interface PhotoApi {

    @PostMapping(value=APP_ROOT + "/save/{id}/{title}/{context}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    Object savePhoto(@PathVariable("context") String context,
                     @PathVariable("id") Integer id,
                     @RequestPart("file") MultipartFile photo,
                     @PathVariable("title") String title) throws IOException, MinioException;

    @DeleteMapping(APP_ROOT + "/delete/{photoUrl}")
    public void deletePhoto(@RequestParam String photoUrl);

}
