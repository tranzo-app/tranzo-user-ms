package com.tranzo.tranzo_user_ms;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.ai.model.openai.autoconfigure.OpenAiChatAutoConfiguration;
import org.springframework.ai.model.openai.autoconfigure.OpenAiAudioSpeechAutoConfiguration;
import org.springframework.ai.model.openai.autoconfigure.OpenAiAudioTranscriptionAutoConfiguration;
import org.springframework.ai.model.openai.autoconfigure.OpenAiEmbeddingAutoConfiguration;
import org.springframework.ai.model.openai.autoconfigure.OpenAiImageAutoConfiguration;
import org.springframework.ai.model.openai.autoconfigure.OpenAiModerationAutoConfiguration;
import org.springframework.ai.model.chat.client.autoconfigure.ChatClientAutoConfiguration;

@SpringBootApplication(exclude = {
        RedisAutoConfiguration.class,
        OpenAiChatAutoConfiguration.class,
        OpenAiAudioSpeechAutoConfiguration.class,
        OpenAiAudioTranscriptionAutoConfiguration.class,
        OpenAiEmbeddingAutoConfiguration.class,
        OpenAiImageAutoConfiguration.class,
        OpenAiModerationAutoConfiguration.class,
        ChatClientAutoConfiguration.class
})
@EnableConfigurationProperties
@EnableScheduling
@EnableCaching
public class TranzoUserMsApplication {

	private static final Logger log = LoggerFactory.getLogger(TranzoUserMsApplication.class);

	public static void main(String[] args) {
		log.info("Application starting | service=tranzo-user-ms | status=STARTING");
		SpringApplication.run(TranzoUserMsApplication.class, args);
		log.info("Application started | service=tranzo-user-ms | status=RUNNING");
	}

}
