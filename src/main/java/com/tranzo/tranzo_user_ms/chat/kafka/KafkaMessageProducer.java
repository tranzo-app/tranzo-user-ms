package com.tranzo.tranzo_user_ms.chat.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tranzo.tranzo_user_ms.chat.model.ConversationEntity;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

@AllArgsConstructor
@Slf4j
@Component
public class KafkaMessageProducer {

    private static final String TOPIC = "trip-group-chat-created";
    private final ObjectMapper objectMapper;
    private final KafkaTemplate<String,String> kafkaTemplate;

    public void publishGroupChatCreated(ConversationEntity conversationEntity, UUID tripId){
           UUID conversationId = conversationEntity.getConversationId();

           TripGroupChatCreatedEventDto tripGroupChatCreatedEventDto = TripGroupChatCreatedEventDto.builder()
                   .tripId(tripId)
                   .eventType("GROUP_CHAT_CREATED")
                   .conversationId(conversationId)
                   .build();

           publishConversationId(tripGroupChatCreatedEventDto);
    }

    public void publishConversationId(TripGroupChatCreatedEventDto payload){
        String conversationId = String.valueOf(payload.getConversationId());
        try{
            String json = objectMapper.writeValueAsString(payload);
            log.info("send Conversation Id id published. ConversationId = {}, json = {}", conversationId,json);
            kafkaTemplate.send(TOPIC,conversationId,json);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

}
