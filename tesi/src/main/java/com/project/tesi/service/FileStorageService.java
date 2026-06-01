package com.project.tesi.service;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.multipart.MultipartFile;

/** Salvataggio fisico dei file sul filesystem. */
@Validated
public interface FileStorageService {

    /** Scrive il file su disco e ritorna il percorso dove è stato archiviato. */
    String store(@NotNull MultipartFile file);

    void delete(@NotBlank String filePath);

    /** Legge dal disco il contenuto binario del file. */
    byte[] load(@NotBlank String filePath);
}
