package com.tranzo.tranzo_user_ms.media.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

/**
 * S3 client configuration. Uses default credential chain (env vars AWS_ACCESS_KEY_ID,
 * AWS_SECRET_ACCESS_KEY, or EC2 instance profile). Bucket and region come from app.s3.* config.
 */
@Configuration
public class S3Config {

    @Value("${app.s3.region:ap-south-1}")
    private String region;

    @Bean
    public S3Client s3Client() {
        return S3Client.builder()
                .region(Region.of(region))
                .build();
    }
}
