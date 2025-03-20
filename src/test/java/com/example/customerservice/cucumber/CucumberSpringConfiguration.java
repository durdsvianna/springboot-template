package com.example.customerservice.cucumber;

import com.example.customerservice.config.MongoTestContainer;
import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@CucumberContextConfiguration
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(MongoTestContainer.class)
@ActiveProfiles("test")
public class CucumberSpringConfiguration {
}