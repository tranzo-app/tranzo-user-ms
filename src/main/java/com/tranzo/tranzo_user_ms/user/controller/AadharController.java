package com.tranzo.tranzo_user_ms.user.controller;

import com.tranzo.tranzo_user_ms.commons.dto.ResponseDto;
import com.tranzo.tranzo_user_ms.commons.utility.SecurityUtils;
import com.tranzo.tranzo_user_ms.user.dto.PublicProfileResponseDto;
import com.tranzo.tranzo_user_ms.user.dto.SubmitHostRatingRequest;
import com.tranzo.tranzo_user_ms.user.dto.SubmitMemberRatingsRequest;
import com.tranzo.tranzo_user_ms.user.dto.SubmitTripRatingRequest;
import com.tranzo.tranzo_user_ms.user.service.PublicProfileService;
import com.tranzo.tranzo_user_ms.user.service.RatingService;
import com.tranzo.tranzo_user_ms.user.dto.AadharNumberDto;
import com.tranzo.tranzo_user_ms.user.service.AadharService;
import jakarta.security.auth.message.AuthException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
