package com.maestronic.autoimportgtfs;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class AutoimportgtfsApplication {

	public static void main(String[] args) {
		SpringApplication.run(AutoimportgtfsApplication.class, args);
	}

}
