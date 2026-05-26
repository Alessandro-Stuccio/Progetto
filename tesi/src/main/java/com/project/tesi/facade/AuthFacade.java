package com.project.tesi.facade;

import com.project.tesi.dto.request.LoginRequest;
import com.project.tesi.service.AuthResult;

public interface AuthFacade {

    AuthResult login(LoginRequest request);

    void forgotPassword(String email);

    void resetPassword(String token, String newPassword);
}
