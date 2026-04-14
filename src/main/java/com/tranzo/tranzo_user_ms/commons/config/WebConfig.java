package com.tranzo.tranzo_user_ms.commons.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
        // Ensures /path and /path/ are treated the same
        configurer.setUseTrailingSlashMatch(true);
    }
}
