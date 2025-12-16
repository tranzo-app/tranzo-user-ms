package com.tranzo.tranzo_user_ms.controller;

import com.tranzo.tranzo_user_ms.dto.RequestOtpDto;
import com.tranzo.tranzo_user_ms.dto.ResponseDto;
import com.tranzo.tranzo_user_ms.dto.VerifyOtpDto;
import com.tranzo.tranzo_user_ms.dto.VerifyOtpResponseDto;
import com.tranzo.tranzo_user_ms.service.OtpService;
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
    public ResponseEntity<ResponseDto<VerifyOtpResponseDto>> verifyOtp(@Valid @RequestBody VerifyOtpDto verifyOtpDto)
    {
        VerifyOtpResponseDto response = otpService.verifyOtp(verifyOtpDto);
        return ResponseEntity.ok(ResponseDto.success(200, "OTP has been verified successfully", response));
    }
}
