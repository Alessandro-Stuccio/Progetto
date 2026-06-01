package com.project.tesi.service;

/** Genera valori casuali, ad esempio i token usati per il reset password. */
public interface RandomGenerationService {

    String getTokenKey();
}
