package com.tchindaClovis.gestiondestock.services.strategy;

import com.tchindaClovis.gestiondestock.dto.FournisseurDto;
import com.tchindaClovis.gestiondestock.exception.ErrorCodes;
import com.tchindaClovis.gestiondestock.exception.InvalidOperationException;
import com.tchindaClovis.gestiondestock.services.FournisseurService;
import java.io.InputStream;
import com.tchindaClovis.gestiondestock.services.MinioService;
import io.minio.errors.MinioException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service("fournisseurStrategy")
@Slf4j
public class SaveFournisseurPhoto implements Strategy<FournisseurDto> {

  private MinioService minioService;
  private FournisseurService fournisseurService;

  @Autowired
  public SaveFournisseurPhoto(MinioService minioService, FournisseurService fournisseurService) {
    this.minioService = minioService;
    this.fournisseurService = fournisseurService;
  }

  @Override
  public FournisseurDto savePhoto(Integer id, InputStream photo, String titre) throws MinioException {
    FournisseurDto fournisseur = fournisseurService.findById(id);
    String urlPhoto = minioService.savePhoto(photo, titre);
    if (!StringUtils.hasLength(urlPhoto)) {
      throw new InvalidOperationException("Erreur lors de l'enregistrement de photo du fournisseur", ErrorCodes.UPDATE_PHOTO_EXCEPTION);
    }
    fournisseur.setPhoto(urlPhoto);
    return fournisseurService.save(fournisseur);
  }
}
