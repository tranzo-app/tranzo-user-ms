package com.tranzo.tranzo_user_ms.chat.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateConversationRequestDto {

    @NotNull(message = "UserId is required")
    private UUID otherUserId;
}
