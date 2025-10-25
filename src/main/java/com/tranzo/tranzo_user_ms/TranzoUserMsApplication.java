package com.tranzo.tranzo_user_ms;

import com.tranzo.tranzo_user_ms.configuration.TwilioConfig;
import com.twilio.Twilio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties
public class TranzoUserMsApplication {

    @Autowired
    TwilioConfig twilioConfig;

	public static void main(String[] args) {
		SpringApplication.run(TranzoUserMsApplication.class, args);
	}

}
