package com.tranzo.tranzo_user_ms.user.controller;

import com.tranzo.tranzo_user_ms.commons.dto.ResponseDto;
import com.tranzo.tranzo_user_ms.commons.utility.SecurityUtils;
import com.tranzo.tranzo_user_ms.user.dto.AadharNumberDto;
import com.tranzo.tranzo_user_ms.user.dto.RequestOtpDto;
import com.tranzo.tranzo_user_ms.user.dto.VerifyOtpDto;
import com.tranzo.tranzo_user_ms.user.dto.VerifyOtpResponseDto;
import com.tranzo.tranzo_user_ms.user.service.AadharService;
import jakarta.security.auth.message.AuthException;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/aadhaar/otp")
@RequiredArgsConstructor
public class AadharController {
    private final AadharService aadharService;

    @PostMapping("/request")
    public ResponseEntity<ResponseDto<Void>> requestOtp(@Valid @RequestBody AadharNumberDto aadharNumberDto) throws AuthException {
        UUID requesterId = SecurityUtils.getCurrentUserUuid();
        aadharService.generateOtp(requesterId, aadharNumberDto);
        return ResponseEntity.ok(ResponseDto.success(200, "OTP sent successfully", null));
    }

    // @PostMapping("/verify")
    // public ResponseEntity<ResponseDto<VerifyOtpResponseDto>> verifyOtp(@Valid @RequestBody VerifyOtpDto verifyOtpDto, HttpServletResponse httpServletResponse)
    // {
    //     VerifyOtpResponseDto response = aadharService.verifyOtp(verifyOtpDto, httpServletResponse);
    //     return ResponseEntity.ok(ResponseDto.success(200, "OTP has been verified successfully", response));
    // }
}
