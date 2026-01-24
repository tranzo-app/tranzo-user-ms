package com.tranzo.tranzo_user_ms.chat.dto;


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
public class SendMessageRequestDto {

    @NotBlank(message = "Message content cannot be empty")
    @Size(max = 5000, message = "Message is too long")
    private String content;
}
