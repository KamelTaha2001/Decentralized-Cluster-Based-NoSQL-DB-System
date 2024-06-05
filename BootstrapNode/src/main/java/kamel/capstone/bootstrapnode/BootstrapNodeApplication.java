package kamel.capstone.bootstrapnode;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
public class BootstrapNodeApplication {

    public static void main(String[] args) {
        SpringApplication.run(BootstrapNodeApplication.class, args);
    }
}


