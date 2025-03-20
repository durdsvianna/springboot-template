package com.example.customerservice.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration
public class MongoTestContainer {
    
    private static final String MONGODB_VERSION = "6.0.6";
    
    @Bean
    public MongoDBContainer mongoDBContainer() {
        MongoDBContainer mongoDBContainer = new MongoDBContainer(DockerImageName.parse("mongo:" + MONGODB_VERSION));
        mongoDBContainer.start();
        
        System.setProperty("spring.data.mongodb.uri", mongoDBContainer.getReplicaSetUrl());
        return mongoDBContainer;
    }
}