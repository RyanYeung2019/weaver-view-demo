package org.weaver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableAutoConfiguration
public class RunApp {
	
	public static void main(String[] args) {
		SpringApplication.run(RunApp.class, args);
	}

}
