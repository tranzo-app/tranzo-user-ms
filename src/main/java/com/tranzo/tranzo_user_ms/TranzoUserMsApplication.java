package com.tranzo.tranzo_user_ms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableConfigurationProperties
@EnableScheduling
public class TranzoUserMsApplication {

	public static void main(String[] args) {
		SpringApplication.run(TranzoUserMsApplication.class, args);
	}

}
