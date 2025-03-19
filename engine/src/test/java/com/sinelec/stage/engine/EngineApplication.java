package com.sinelec.stage.engine;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@SpringBootApplication
@ComponentScan(basePackages = {
    "com.sinelec.stage.controller",
    "com.sinelec.stage.service",
    "com.sinelec.stage.repository",
    "com.sinelec.stage.engine.integration"
})
@EnableMongoRepositories("com.sinelec.stage.repository")
@EntityScan("com.sinelec.stage.domain.engine.model")
public class EngineApplication {
    public static void main(String[] args) {
        SpringApplication.run(EngineApplication.class, args);
    }
} 