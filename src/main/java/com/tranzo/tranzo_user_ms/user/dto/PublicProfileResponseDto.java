package com.tranzo.tranzo_user_ms.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PublicProfileResponseDto {

    private String firstName;
    private String middleName;
    private String lastName;
    private String profilePictureUrl;
    private String bio;
    private BigDecimal trustScore;
    private List<ReviewItemDto> reviews;
    private long totalReviewCount;

}
