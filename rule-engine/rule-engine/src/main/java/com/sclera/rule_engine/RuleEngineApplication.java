package com.sclera.rule_engine;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@OpenAPIDefinition(
		info = @Info(
				title = "Sclera_Location",
				description = "Sclera_Location API",
				version = "3.0"
		)
)
public class RuleEngineApplication {

	public static void main(String[] args) {
		SpringApplication.run(RuleEngineApplication.class, args);
	}

}
