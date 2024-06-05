package kamel.capstone.emaildemoapp.bean;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class AppBeans {
    @Bean
    public RestTemplate getTemplate() {
        return new RestTemplate();
    }
}
