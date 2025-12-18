package com.tranzo.tranzo_user_ms.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserReportDto {
    @NotBlank(message = "Feedback cannot be empty")
    @Size(max = 1000, message = "Feedback cannot exceed 1000 characters")
    private String feedback;

    @NotBlank(message = "Reporting user UUID is required")
    @Pattern(
            regexp = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$",
            message = "Invalid UUID format"
    )
    private String reportingUserUuid;

    @NotBlank(message = "Reported user UUID is required")
    @Pattern(
            regexp = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$",
            message = "Invalid UUID format"
    )
    private String reportedUserUuid;
}
