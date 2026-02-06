package com.tranzo.tranzo_user_ms.trip.kafka;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.tranzo.tranzo_user_ms.trip.repository.TripRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@AllArgsConstructor
@Component
@Slf4j
public class KafkaMessageConsumer {

    private final TripRepository tripRepository;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "trip-group-chat-created", groupId = "GROUP_CHAT_CREATED",containerFactory = "tripEventListenerContainerFactory")
    @Transactional
    public  void consumerChatCreatedEvent(String eventMessage){
       try{
              TripGroupChatCreatedEventDto event = objectMapper.readValue(eventMessage, TripGroupChatCreatedEventDto.class);
           if("GROUP_CHAT_CREATED".equals(event.getEventType())){
               tripRepository.findById(event.getTripId())
                       .ifPresent(
                               trip -> {
                                   if(trip.getConversationID()==null){
                                       trip.setConversationID(event.getConversationId());
                                   }
                               }
                       );
           }
       }
         catch (Exception e){
             log.error("Failed to process TripPublishedEvent", e);
             throw new RuntimeException(e);
         }
    }

}
