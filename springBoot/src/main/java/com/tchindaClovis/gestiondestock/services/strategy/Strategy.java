package com.tchindaClovis.gestiondestock.services.strategy;

import io.minio.errors.MinioException;

import java.io.InputStream;

public interface Strategy<T> {
    T savePhoto(Integer id, InputStream photo, String titre) throws MinioException;
}
