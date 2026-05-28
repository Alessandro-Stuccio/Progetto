package com.project.tesi.service;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.multipart.MultipartFile;

/**
 * Servizio per la persistenza fisica dei file sul filesystem.
 */
@Validated
public interface FileStorageService {

    /**
     * Salva il file ricevuto sul filesystem e restituisce il percorso di archiviazione.
     *
     * @param file file multipart da salvare
     * @return percorso assoluto del file salvato
     */
    String store(@NotNull MultipartFile file);

    /**
     * Elimina il file corrispondente al percorso indicato dal filesystem.
     *
     * @param filePath percorso del file da eliminare
     */
    void delete(@NotBlank String filePath);

    /**
     * Carica e restituisce il contenuto binario del file indicato.
     *
     * @param filePath percorso del file da caricare
     * @return array di byte con il contenuto del file
     */
    byte[] load(@NotBlank String filePath);
}
