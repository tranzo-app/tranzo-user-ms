package com.tranzo.tranzo_user_ms.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserReportRequestDto {
    @NotBlank(message = "Reported user id is required")
    private String reportedUserId;

     @NotBlank(message = "Report reason is required")
     @Size(max = 500, message = "Report reason cannot exceed 500 characters")
     private String reportReasonMessage;

}
