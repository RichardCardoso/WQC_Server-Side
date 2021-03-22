package com.richard.weger.wqc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.rest.RepositoryRestMvcAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.socket.config.annotation.EnableWebSocket;

@SpringBootApplication
@ComponentScan//("com.richard.weger.wqc")
@EntityScan//(basePackages = {"com.richard.weger.wqc.domain"})
@EnableJpaAuditing
@EnableAutoConfiguration(exclude = RepositoryRestMvcAutoConfiguration.class)
@EnableJpaRepositories///("com.richard.weger.wqc.repository")
@EnableAsync
@EnableWebSocket
public class WqcApplication extends SpringBootServletInitializer {
	public static void main(String args[]) {
		SpringApplication.run(WqcApplication.class, args);
	}
}
