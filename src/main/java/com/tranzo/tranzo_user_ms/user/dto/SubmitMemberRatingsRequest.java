package com.tranzo.tranzo_user_ms.user.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubmitMemberRatingsRequest {

    @NotEmpty
    @Valid
    private List<SubmitMemberRatingItem> ratings;
}
