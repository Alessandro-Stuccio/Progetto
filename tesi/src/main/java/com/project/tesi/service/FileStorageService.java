package com.project.tesi.service;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.multipart.MultipartFile;

@Validated
public interface FileStorageService {

    String store(@NotNull MultipartFile file);

    void delete(@NotBlank String filePath);

    byte[] load(@NotBlank String filePath);
}
