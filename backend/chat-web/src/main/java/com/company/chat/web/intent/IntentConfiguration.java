package com.company.chat.web.intent;

import com.company.chat.api.intent.IntentRecognizer;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(IntentProperties.class)
public class IntentConfiguration {

    @Bean
    public IntentRecognizer intentRecognizer(IntentProperties properties) {
        return new KeywordIntentRecognizer(properties);
    }
}
