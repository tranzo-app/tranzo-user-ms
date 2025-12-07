package com.tranzo.tranzo_user_ms.dto;

import com.tranzo.tranzo_user_ms.enums.Gender;
import jakarta.persistence.Column;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

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

    @NotBlank(message = "Last name is required")
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
}
