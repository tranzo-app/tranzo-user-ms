package com.tranzo.tranzo_user_ms.user.controller;

import com.tranzo.tranzo_user_ms.user.dto.RequestOtpDto;
import com.tranzo.tranzo_user_ms.user.dto.ResponseDto;
import com.tranzo.tranzo_user_ms.user.dto.VerifyOtpDto;
import com.tranzo.tranzo_user_ms.user.dto.VerifyOtpResponseDto;
import com.tranzo.tranzo_user_ms.user.service.OtpService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth/otp")
@RequiredArgsConstructor
public class OtpController {
    private final OtpService otpService;

    @PostMapping("/request")
    public ResponseEntity<ResponseDto<Void>> requestOtp(@Valid @RequestBody RequestOtpDto requestOtpDto)
    {
        otpService.sendOtp(requestOtpDto);
        return ResponseEntity.ok(ResponseDto.success(200, "OTP sent successfully", null));
    }

    @PostMapping("/verify")
    public ResponseEntity<ResponseDto<VerifyOtpResponseDto>> verifyOtp(@Valid @RequestBody VerifyOtpDto verifyOtpDto, HttpServletResponse httpServletResponse)
    {
        VerifyOtpResponseDto response = otpService.verifyOtp(verifyOtpDto, httpServletResponse);
        return ResponseEntity.ok(ResponseDto.success(200, "OTP has been verified successfully", response));
    }
}