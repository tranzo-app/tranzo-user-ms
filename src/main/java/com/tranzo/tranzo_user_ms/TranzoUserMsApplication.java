package com.tranzo.tranzo_user_ms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;

@SpringBootApplication(exclude = {RedisAutoConfiguration.class})
@EnableConfigurationProperties
@EnableScheduling
@EnableCaching
public class TranzoUserMsApplication {

	public static void main(String[] args) {
		SpringApplication.run(TranzoUserMsApplication.class, args);
	}

}
