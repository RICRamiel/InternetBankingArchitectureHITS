package org.ricramiel.coreapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(
        scanBasePackages = {
                "org.ricramiel.common", "org.ricramiel.coreapi"}
        )
@EnableScheduling
public class CoreApiApplication {
    public static void main(String[] args) {
        SpringApplication.run(CoreApiApplication.class, args);
    }
}
