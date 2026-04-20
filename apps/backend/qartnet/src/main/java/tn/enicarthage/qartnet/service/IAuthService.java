package tn.enicarthage.qartnet.service;

import tn.enicarthage.qartnet.dto.request.ForgotPasswordRequest;
import tn.enicarthage.qartnet.dto.request.LoginRequest;
import tn.enicarthage.qartnet.dto.request.RegisterRequest;
import tn.enicarthage.qartnet.dto.request.ResetPasswordRequest;
import tn.enicarthage.qartnet.dto.response.AuthResponse;

public interface IAuthService {

    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    void forgotPassword(ForgotPasswordRequest request);

    void resetPassword(ResetPasswordRequest request);
}
