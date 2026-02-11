package com.invest.execution;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;

@SpringBootApplication
@EnableKafka
public class ExecutionApplication {

	public static void main(String[] args) {
		SpringApplication.run(ExecutionApplication.class, args);
	}

}
