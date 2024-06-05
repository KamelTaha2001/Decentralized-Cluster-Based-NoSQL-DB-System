package kamel.capstone.bootstrapnode.config;

import kamel.capstone.bootstrapnode.security.NoSQLFilterInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new NoSQLFilterInterceptor()).addPathPatterns("/nosql/**");
    }
}
