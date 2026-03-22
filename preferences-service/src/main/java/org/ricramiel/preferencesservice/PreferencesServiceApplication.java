package org.ricramiel.preferencesservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(
        scanBasePackages = {
                "org.ricramiel.common", "org.ricramiel.preferencesservice"
        })
public class PreferencesServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(PreferencesServiceApplication.class, args);
    }

}
