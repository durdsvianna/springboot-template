package com.example.customerservice.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration
public class MongoTestContainer {

    private static final MongoDBContainer mongoDBContainer;

    static {
        mongoDBContainer = new MongoDBContainer(DockerImageName.parse("mongo:5.0.15"));
        mongoDBContainer.start();
    }

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("embedded.mongodb.host", () -> "localhost");
        registry.add("embedded.mongodb.port", mongoDBContainer::getFirstMappedPort);
    }

    @Bean
    public MongoDBContainer mongoDBContainer() {
        return mongoDBContainer;
    }
}