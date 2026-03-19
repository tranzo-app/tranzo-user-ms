package com.tranzo.tranzo_user_ms.user.dto;

import com.tranzo.tranzo_user_ms.user.enums.Gender;
import com.tranzo.tranzo_user_ms.user.enums.VerificationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

/**
 * Public view of a user profile (no email or phone).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PublicUserProfileDto {
    private String firstName;
    private String middleName;
    private String lastName;
    private String bio;
    private Gender gender;
    private LocalDate dob;
    private String location;
    private String profilePictureUrl;
    private List<SocialHandleDto> socialHandleDtoList;
    private VerificationStatus verificationStatus;
}
