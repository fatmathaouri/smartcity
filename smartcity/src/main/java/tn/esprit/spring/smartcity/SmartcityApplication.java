package tn.esprit.spring.smartcity;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SmartcityApplication {

    public static void main(String[] args) {
        SpringApplication.run(SmartcityApplication.class, args);
    }

}
