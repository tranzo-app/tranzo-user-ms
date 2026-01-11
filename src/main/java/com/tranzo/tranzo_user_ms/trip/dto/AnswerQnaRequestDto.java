package com.tranzo.tranzo_user_ms.trip.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnswerQnaRequestDto {

    @NotBlank(message = "Question cannot be blank")
    private String answer;


}
