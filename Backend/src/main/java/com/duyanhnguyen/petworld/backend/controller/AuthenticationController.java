package com.duyanhnguyen.petworld.backend.controller;

import com.duyanhnguyen.petworld.backend.dto.request.AuthenticationRequest;
import com.duyanhnguyen.petworld.backend.dto.request.UserActivationRequest;
import com.duyanhnguyen.petworld.backend.dto.response.ApiResponse;
import com.duyanhnguyen.petworld.backend.dto.response.AuthenticationResponse;
import com.duyanhnguyen.petworld.backend.enums.ErrorCode;
import com.duyanhnguyen.petworld.backend.exception.AppException;
import com.duyanhnguyen.petworld.backend.service.AuthenticationService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationController {

    AuthenticationService authenticationService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthenticationResponse>> authenticate(@RequestBody AuthenticationRequest request) {
        AuthenticationResponse authenticationResponse = authenticationService.authenticate(request);

        ResponseCookie cookie = ResponseCookie.from("refresh_token", authenticationResponse.getRefreshToken())
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(604800)
                .sameSite("Lax")
                .build();
        authenticationResponse.setRefreshToken(null);

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(
                        ApiResponse.<AuthenticationResponse>builder()
                                .message("Login successfully")
                                .data(authenticationResponse)
                                .build()
                );
    }

    @PostMapping("/activate-user")
    public ApiResponse<Void> activateUser(@RequestBody @Valid UserActivationRequest request) {
        authenticationService.activateUser(request);
        return ApiResponse.<Void>builder()
                .message("User activated successfully")
                .build();
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthenticationResponse>> refreshToken(
            @CookieValue(value = "refresh_token", required = false) String refreshToken
    ) {
        if (refreshToken == null || refreshToken.isBlank())
            throw new AppException(ErrorCode.UNAUTHENTICATED);

        AuthenticationResponse authenticationResponse = authenticationService.refreshToken(refreshToken);

        ResponseCookie cookie = ResponseCookie.from("refresh_token", authenticationResponse.getRefreshToken())
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(604800)
                .sameSite("Lax")
                .build();
        authenticationResponse.setRefreshToken(null);

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(
                        ApiResponse.<AuthenticationResponse>builder()
                                .message("Token refreshed successfully")
                                .data(authenticationResponse)
                                .build()
                );
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @RequestHeader("Authorization") String authHeader,
            @CookieValue(value = "refresh_token", required = false) String refreshToken
    ) {
        if (refreshToken == null || refreshToken.isBlank() ||
                authHeader == null || authHeader.isBlank() || !authHeader.startsWith("Bearer "))
            throw new AppException(ErrorCode.UNAUTHENTICATED);

        authenticationService.logout(authHeader.substring(7), refreshToken);

        ResponseCookie delete = ResponseCookie.from("refresh_token", "")
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(0)
                .sameSite("Lax")
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, delete.toString())
                .body(
                        ApiResponse.<Void>builder()
                                .message("Logout successfully")
                                .build()
                );
    }

}
