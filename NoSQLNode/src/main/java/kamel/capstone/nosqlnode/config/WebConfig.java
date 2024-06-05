package kamel.capstone.nosqlnode.config;

import kamel.capstone.nosqlnode.security.FilterInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new FilterInterceptor()).addPathPatterns("/bootstrap/**", "/nosql/**");
    }
}
