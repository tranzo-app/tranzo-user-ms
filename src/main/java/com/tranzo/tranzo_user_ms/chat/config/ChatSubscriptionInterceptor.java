package com.tranzo.tranzo_user_ms.chat.config;

import com.tranzo.tranzo_user_ms.chat.repository.ConversationParticipantRepository;
import lombok.RequiredArgsConstructor;
import org.apache.tomcat.websocket.AuthenticationException;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;

import java.security.Principal;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ChatSubscriptionInterceptor implements ChannelInterceptor {

    private final ConversationParticipantRepository participantRepository;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {

        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

        // We only care when user SUBSCRIBES to a topic
        if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {

            Principal user = accessor.getUser(); // comes from Spring Security
            String destination = accessor.getDestination();

            if (user == null || destination == null) {
                return null; // block
            }

            // We only protect conversation topics
            if (destination.startsWith("/topic/conversations/")) {

                String conversationIdStr =
                        destination.substring("/topic/conversations/".length());

                UUID conversationId = UUID.fromString(conversationIdStr);
                UUID userId = UUID.fromString(user.getName());

                boolean isParticipant =
                        participantRepository
                                .existsByConversation_ConversationIdAndUserIdAndLeftAtIsNull(
                                        conversationId,
                                        userId
                                );

                if (!isParticipant) {
                    throw new RuntimeException(
                            "You are not allowed to subscribe to this conversation"
                    );
                }
            }
        }

        return message;
    }
}