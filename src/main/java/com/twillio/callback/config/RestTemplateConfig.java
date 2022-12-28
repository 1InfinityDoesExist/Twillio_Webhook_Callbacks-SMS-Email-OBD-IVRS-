package com.twillio.callback.config;

import java.time.Duration;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.client.RestTemplate;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
public class RestTemplateConfig {

	@Bean("restTemplate")
	@Primary
	public RestTemplate restTemplate(RestTemplateBuilder builder) {
		log.info("----Creating RestTemplate Builder-----");
		return builder.setConnectTimeout(Duration.ofMillis(60000)).setReadTimeout(Duration.ofMillis(60000)).build();
	}
}