package com.maestronic.autoimportgtfs.bean;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;

public class EventBean {

    @EventListener(ApplicationReadyEvent.class)
    @Order(1)
    public void appReadyMessage() {
        System.out.println("\nGTFS auto import is running\n");
    }
}
