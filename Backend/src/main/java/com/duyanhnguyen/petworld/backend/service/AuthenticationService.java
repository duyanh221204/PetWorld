package com.duyanhnguyen.petworld.backend.service;

import com.duyanhnguyen.petworld.backend.dto.request.AuthenticationRequest;
import com.duyanhnguyen.petworld.backend.dto.request.UserActivationRequest;
import com.duyanhnguyen.petworld.backend.dto.response.AuthenticationResponse;

public interface AuthenticationService {

    AuthenticationResponse authenticate(AuthenticationRequest authenticationRequest);

    void activateUser(UserActivationRequest userActivationRequest);

    AuthenticationResponse refreshToken(String refreshToken);

    void logout(String accessToken, String refreshToken);

}
