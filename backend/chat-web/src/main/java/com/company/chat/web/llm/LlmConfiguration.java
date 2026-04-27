package com.company.chat.web.llm;

import com.company.chat.api.llm.LlmClient;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(LlmProperties.class)
public class LlmConfiguration {

    @Bean
    public LlmClient llmClient(LlmProperties properties) {
        return new LlmClientFactory(properties).create();
    }
}
