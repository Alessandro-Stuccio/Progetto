package com.project.tesi.service.impl;

import com.project.tesi.service.RandomGenerationService;
import org.passay.data.CharacterData;
import org.passay.data.EnglishCharacterData;
import org.passay.generate.PasswordGenerator;
import org.passay.rule.CharacterRule;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RandomGenerationServiceImpl implements RandomGenerationService {
    @Override
    public String getTokenKey() {
        CharacterData lowerCase= EnglishCharacterData.LowerCase;
        CharacterRule lowerCaseRule = new CharacterRule(lowerCase);
        CharacterData upperCase = EnglishCharacterData.UpperCase;
        CharacterRule upperCaseRule = new CharacterRule(upperCase);
        CharacterData numeric= EnglishCharacterData.Digit;
        CharacterRule numericRule=new CharacterRule(numeric);
        List<CharacterRule> rules = List.of(lowerCaseRule, upperCaseRule, numericRule);
        PasswordGenerator passwordGenerator = new PasswordGenerator(64,rules);
        return passwordGenerator.generate().toString();


    }
}
