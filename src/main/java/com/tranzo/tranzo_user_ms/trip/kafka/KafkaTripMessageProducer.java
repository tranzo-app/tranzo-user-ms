package com.tranzo.tranzo_user_ms.trip.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class KafkaTripMessageProducer implements TripEventPublisher {

    private static final String Topic = "trip.event";
    private final ObjectMapper objectMapper;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Override
    public void publishTripPublished(TripPublishedEventPayloadDto payload) {
        String hostUserId = String.valueOf(payload.getUserId());
        try {
            String json = objectMapper.writeValueAsString(payload);
            log.info("Publishing Trip Publish Event. Host Id ={},json = {}", hostUserId, json);
            kafkaTemplate.send(Topic, hostUserId, json);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize TripPublishedEventPayloadDto", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void participantJoined(TripPublishedEventPayloadDto payload) {
        String userId = String.valueOf(payload.getUserId());
        try {
            String json = objectMapper.writeValueAsString(payload);
            log.info("Publishing Participant Joined Event. User Id ={},json = {}", userId, json);
            kafkaTemplate.send(Topic, userId, json);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize TripPublishedEventPayloadDto", e);
            throw new RuntimeException(e);
        }
    }
}