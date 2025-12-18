package com.tranzo.tranzo_user_ms.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RequestOtpDto implements IdentifierAware {
    @Pattern(regexp = "\\+[0-9]{1,4}", message = "Country code must start with + and contain 1–4 digits")
    private String countryCode;

    @Pattern(regexp = "[0-9]{7,15}", message = "Mobile number must be 7–15 digits")
    private String mobileNumber;

    @Email(message = "Invalid email format")
    private String emailId;

    @AssertTrue(message = "Either (country code and mobile number) or email id must be provided, not both")
    public boolean isValidInput() {
        if (countryCode != null && !countryCode.isBlank() && mobileNumber != null && !mobileNumber.isBlank())
        {
            return emailId == null || emailId.isBlank();
        }
        if (emailId != null && !emailId.isBlank())
        {
            return (countryCode == null || countryCode.isBlank()) && (mobileNumber == null || mobileNumber.isBlank());
        }
        return false;
    }
}