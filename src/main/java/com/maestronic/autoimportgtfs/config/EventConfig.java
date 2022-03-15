package com.maestronic.autoimportgtfs.config;

import com.maestronic.autoimportgtfs.bean.EventBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EventConfig {

    @Bean
    public EventBean getReadyEventBean() {
        return new EventBean();
    }
}
