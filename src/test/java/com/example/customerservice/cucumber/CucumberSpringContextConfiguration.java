package com.example.customerservice.cucumber;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.example.customerservice.CustomerServiceApplication;

import io.cucumber.java.Before;
import io.cucumber.spring.CucumberContextConfiguration;

/**
 * Class to use spring application context while running cucumber
 */
@CucumberContextConfiguration
@SpringBootTest(classes = CustomerServiceApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class CucumberSpringContextConfiguration {
    
    /**
     * Need this method so the cucumber will recognize this class as glue and load spring context configuration
     */
    @Before
    public void setUp() {
        // Intentionally empty to trigger Spring context loading
    }
}