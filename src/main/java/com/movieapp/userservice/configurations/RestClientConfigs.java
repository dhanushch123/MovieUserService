package com.movieapp.userservice.configurations;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfigs {
	
	@Value("${user-client.base-url}")
	String user_client_url;
	
	@Bean
	RestClient userRestClient(RestClient.Builder builder) {
		return builder.baseUrl(user_client_url)
		.build();
	}
}
