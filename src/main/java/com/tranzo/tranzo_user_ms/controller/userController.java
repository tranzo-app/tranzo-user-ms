//package com.tranzo.tranzo_user_ms.controller;
//
//import com.tranzo.tranzo_user_ms.dto.ResponseDto;
//import com.tranzo.tranzo_user_ms.dto.RequestOtpDto;
//import com.tranzo.tranzo_user_ms.dto.VerifyOtpDto;
//import com.tranzo.tranzo_user_ms.service.OtpService;
//import jakarta.validation.Valid;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//@Slf4j
//@RestController
//public class userController {
//
//    @Autowired
//    private OtpService otpService;
//
//    @PostMapping("/auth/request-otp")
//    public ResponseEntity<ResponseDto> requestOtp(@Valid @RequestBody RequestOtpDto requestOtpDto){
//       log.info("requested otp for mobile number : {}", requestOtpDto.getMobileNumber());
//        otpService.generateOtp(requestOtpDto);
//       return ResponseEntity
//               .status(HttpStatus.OK)
//               .body(new ResponseDto(HttpStatus.OK.value(), "OTP Sent successfully."));
//    }
//
//    @PostMapping("/auth/verify-otp")
//    public ResponseEntity<ResponseDto> verifyOtp(@Valid @RequestBody VerifyOtpDto dto) {
//        boolean verified = otpService.verifyOtp(dto);
//        if (verified) {
//            return ResponseEntity
//                    .status(HttpStatus.OK)
//                    .body(new ResponseDto(HttpStatus.OK.value(), "OTP verified successfully."));
//        } else {
//            return ResponseEntity
//                    .status(HttpStatus.BAD_REQUEST)
//                    .body(new ResponseDto(HttpStatus.BAD_REQUEST.value(), "Invalid or expired OTP."));
//        }
//    }
//    // here rt means refresh token
//    //In production make secure = true
//    @PostMapping("/auth/refresh-token")
//    public ResponseEntity<ApiResponse> refreshToken(@CookieValue(value = "rt", required = false) String rt, HttpServletResponse response){
//        if(rt==null || rt.isBlank()){
//            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
//                    new ApiResponse(401,"No refresh token found")
//            );
//        }
//        try{
//            TokenService.IssuedTokens newTokens = TokenService.rotate(rt);
//
//            ResponseCookie accessTokenCookie = ResponseCookie.from("at", newTokens.accessToken())
//                    .httpOnly(true).secure(false).path("/")
//                    .sameSite("Lax").maxAge(Duration.ofMinutes(15)).build();
//            ResponseCookie refreshTokenCookie = ResponseCookie.from("rt", newTokens.refreshToken())
//                    .httpOnly(true).secure(false).path("/")
//                    .sameSite("Lax").maxAge(Duration.ofDays(30)).build();
//
//            response.addHeader("Set-Cookie", accessTokenCookie.toString());
//            response.addHeader("Set-Cookie", refreshTokenCookie.toString());
//            return ResponseEntity.status(HttpStatus.OK).body(
//                    new ApiResponse(200,"Token refreshed successfully"));
//        }catch(Exception e){
//            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
//                    new ApiResponse(401, "Refresh token Expired, please login again")
//            );
//        }
//    }
//}
