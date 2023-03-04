package ru.otus.geolocation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class GeolocationApplication {

    public static void main(String[] args) {
        SpringApplication.run(GeolocationApplication.class, args);
    }

}
