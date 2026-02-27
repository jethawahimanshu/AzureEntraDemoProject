package com.example.b2cdemo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class B2cDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(B2cDemoApplication.class, args);
    }
}
