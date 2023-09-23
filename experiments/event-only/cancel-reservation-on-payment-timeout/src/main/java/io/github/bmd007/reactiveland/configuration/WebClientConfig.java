package io.github.bmd007.reactiveland.configuration;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Bean
    @Qualifier("notLoadBalancedClient")
    public WebClient.Builder notLoadBalancedWebClientBuilder() {
        return WebClient.builder();
    }

}
