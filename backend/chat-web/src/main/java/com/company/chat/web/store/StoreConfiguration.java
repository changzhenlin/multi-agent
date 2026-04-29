package com.company.chat.web.store;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
public class StoreConfiguration {

    @Bean
    Clock clock() {
        return Clock.systemUTC();
    }
}
