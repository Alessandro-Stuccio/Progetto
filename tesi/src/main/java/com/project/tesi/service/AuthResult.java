package com.project.tesi.service;

import com.project.tesi.model.User;

public record AuthResult(String token, User user) {}
