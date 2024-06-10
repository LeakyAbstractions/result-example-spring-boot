package com.example.config;

import com.fasterxml.jackson.databind.Module;
import com.leakyabstractions.result.jackson.ResultModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Registers Jackson datatype module for Result.
 */
@Configuration
public class JacksonConfig {

	@Bean
	public Module registerResultModule() {
		return new ResultModule();
	}
}
