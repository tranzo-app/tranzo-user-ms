package com.tranzo.tranzo_user_ms.chat.socket.payload;

import com.tranzo.tranzo_user_ms.chat.dto.SendMessageRequestDto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class SendMessageSocketPayload {

    @NotNull
    private UUID conversationId;

    @NotBlank
    private SendMessageRequestDto sendMessageRequestDto;
}
