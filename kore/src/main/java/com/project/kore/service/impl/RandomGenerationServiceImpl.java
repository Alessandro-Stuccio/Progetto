package com.project.kore.service.impl;

import com.project.kore.service.RandomGenerationService;
import org.passay.data.CharacterData;
import org.passay.data.EnglishCharacterData;
import org.passay.generate.PasswordGenerator;
import org.passay.rule.CharacterRule;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RandomGenerationServiceImpl implements RandomGenerationService {

    @Value("${jwt.length}")
    private int length;

    // Se valorizzata (env JWT_SECRET), la chiave di firma è fissa e i token
    // sopravvivono ai riavvii; altrimenti resta la generazione casuale.
    @Value("${jwt.secret:}")
    private String configuredSecret;

    @Override
    public String getTokenKey() {
        if (configuredSecret != null && !configuredSecret.isBlank()) {
            return configuredSecret;
        }
        CharacterData lowerCase= EnglishCharacterData.LowerCase;
        CharacterRule lowerCaseRule = new CharacterRule(lowerCase);
        CharacterData upperCase = EnglishCharacterData.UpperCase;
        CharacterRule upperCaseRule = new CharacterRule(upperCase);
        CharacterData numeric= EnglishCharacterData.Digit;
        CharacterRule numericRule=new CharacterRule(numeric);
        List<CharacterRule> rules = List.of(lowerCaseRule, upperCaseRule, numericRule);
        PasswordGenerator passwordGenerator = new PasswordGenerator(length,rules);
        return passwordGenerator.generate().toString();


    }
}
