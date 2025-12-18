package com.tranzo.tranzo_user_ms.user.dto;

import com.tranzo.tranzo_user_ms.user.enums.Gender;
import com.tranzo.tranzo_user_ms.user.enums.VerificationStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProfileDto {
    @NotBlank(message = "First name is required")
    @Size(max = 50, message = "First name cannot exceed 50 characters")
    private String firstName;

    @Size(max = 50, message = "Middle name cannot exceed 50 characters")
    private String middleName;

    @Pattern(regexp = "[0-9]{7,15}", message = "Mobile number must be 7–15 digits")
    private String mobileNumber;

    @Email(message = "Invalid email format")
    private String emailId;

    @Size(max = 50, message = "Last name cannot exceed 50 characters")
    private String lastName;

    @Size(max = 250, message = "Bio cannot exceed 250 characters")
    private String bio;

    @NotNull(message = "Gender is required")
    private Gender gender;

    @Past(message = "Date of birth must be in the past")
    @NotNull(message = "Date of birth is required")
    private LocalDate dob;

    @Size(max = 100, message = "Location cannot exceed 100 characters")
    private String location;

    private String profilePictureUrl;

    @Valid
    private List<SocialHandleDto> socialHandleDtoList;

    @NotNull
    private VerificationStatus verificationStatus;
}
