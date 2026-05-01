package com.ai.factory;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class AiSoftwareFactoryApplication {

    public static void main(String[] args) {
        SpringApplication.run(AiSoftwareFactoryApplication.class, args);
    }
}
