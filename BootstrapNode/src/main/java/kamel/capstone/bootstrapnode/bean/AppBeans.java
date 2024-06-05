package kamel.capstone.bootstrapnode.bean;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.web.client.RestTemplate;

@Configuration
public class AppBeans {
    @Bean
    public RestTemplate getTemplate() {
        return new RestTemplate();
    }
}
