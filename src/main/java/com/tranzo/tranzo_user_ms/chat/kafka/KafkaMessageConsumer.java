package com.tranzo.tranzo_user_ms.chat.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tranzo.tranzo_user_ms.chat.model.ConversationEntity;
import com.tranzo.tranzo_user_ms.chat.service.CreateAndManageConversationService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.UUID;

@AllArgsConstructor
@Slf4j
@Component
public class KafkaMessageConsumer {

    private final ObjectMapper objectMapper;
    private final CreateAndManageConversationService createAndManageConversationService;
    private final KafkaMessageProducer kafkaMessageProducer;

    @KafkaListener(
            topics = "trip.event",
            groupId = "chat-service",
            containerFactory = "tripEventListenerContainerFactory"
    )
    public void onTripPublishEvent(String messageJson){
        try{
            ChatTripPublishedEventDto event =
                    objectMapper.readValue(messageJson, ChatTripPublishedEventDto.class);

            if ("TRIP_PUBLISHED".equals(event.getEventType())) {
                log.info("Received TripPublishedEvent in chat for tripId={}", event.getTripId());
                UUID hostUserId = event.getHostUserId();
                UUID tripId = event.getTripId();
               ConversationEntity conversationEntity =  createAndManageConversationService.createTripGroupChat(hostUserId);
               kafkaMessageProducer.publishGroupChatCreated(conversationEntity,tripId);
            }
        }catch (Exception e){
            log.error("Failed to process TripPublishedEvent", e);
            throw new RuntimeException(e);
        }
    }

}
