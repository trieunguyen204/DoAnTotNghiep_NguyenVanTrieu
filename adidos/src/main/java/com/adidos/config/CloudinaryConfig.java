package com.adidos.config;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CloudinaryConfig {

    @Bean
    public Cloudinary cloudinary() {
        return new Cloudinary(ObjectUtils.asMap(
                "Root", "Root",
                "api_key", "718857458432273",
                "api_secret", "ZYgN-Istp_pFwEsdCQK_duI2Okw"
        ));
    }
}