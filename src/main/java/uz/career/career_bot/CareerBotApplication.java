package uz.career.career_bot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CareerBotApplication {

    public static void main(String[] args) {
        SpringApplication.run(CareerBotApplication.class, args);
    }
}