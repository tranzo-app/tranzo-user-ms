package com.tranzo.tranzo_user_ms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties
public class TranzoUserMsApplication {

	public static void main(String[] args) {
		SpringApplication.run(TranzoUserMsApplication.class, args);
	}

}
