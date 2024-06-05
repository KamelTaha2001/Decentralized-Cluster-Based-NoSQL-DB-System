package kamel.capstone.emaildemoapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class EmailDemoAppApplication {

    public static void main(String[] args) throws InterruptedException {
        Thread.sleep(15000);
        SpringApplication.run(EmailDemoAppApplication.class, args);
    }

}
