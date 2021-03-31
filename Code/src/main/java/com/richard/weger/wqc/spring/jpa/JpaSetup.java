package com.richard.weger.wqc.spring.jpa;

import java.util.Optional;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.jdbc.datasource.DriverManagerDataSource;


@Configuration
@EnableConfigurationProperties(DatabaseConfig.class)
public class JpaSetup {
	
	@Autowired private DatabaseConfig databaseConfig;
	
	Logger logger;
	
	public JpaSetup() {
		logger = Logger.getLogger(getClass());
	}
	
	@Bean
	public AuditorAware<String> auditorProvider() {
		
		return () -> Optional.ofNullable("SYSTEM");
	}
	
	@Bean
	public DataSource dataSource() {
		DriverManagerDataSource dataSource = new DriverManagerDataSource();
		
		logger.info("Acquiring datasource");
		
//		boolean isWindows = System.getProperty("os.name").startsWith("Window");
		String username = databaseConfig.getUser(), 
				password = databaseConfig.getPassword(),
				name = databaseConfig.getName();
		
		/*
		if(!isWindows) {
			username = "compassw_wqc";
			password = "wegerit2019_wqc";
			name = "compassw_wqc";
		}
		*/

		dataSource.setUsername(username);
		dataSource.setPassword(password);
		dataSource.setUrl("jdbc:mysql://" + databaseConfig.getServerPath() + ":" + databaseConfig.getServerPort() + "/" + name + "?createDatabaseIfNotExist=true&useTimezone=true&serverTimezone=UTC&useUnicode=true&characterEncoding=utf-8");
		dataSource.setDriverClassName("com.mysql.jdbc.Driver");
		
		logger.info("Datasource acquired");
		
		return dataSource;
	}
}
