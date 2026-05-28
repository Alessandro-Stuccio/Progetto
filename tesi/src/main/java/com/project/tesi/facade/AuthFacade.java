package com.project.tesi.facade;

import com.project.tesi.dto.request.LoginRequest;
import com.project.tesi.dto.request.RegisterRequest;
import com.project.tesi.dto.response.UserResponse;
import com.project.tesi.dto.response.AuthResult;

public interface AuthFacade {

    UserResponse registerUser(RegisterRequest request);

    AuthResult login(LoginRequest request);

    void forgotPassword(String email);

    void resetPassword(String token, String newPassword);
}
