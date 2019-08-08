package com.richard.weger.wqc.spring.jpa;

import java.util.Properties;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;

import com.richard.weger.wqc.domain.DomainEntity;

@Configuration
@EnableConfigurationProperties(DatabaseConfig.class)
public class JpaSetup {
	
	@Autowired private DatabaseConfig databaseConfig;
	
	@Bean
	public DataSource dataSource() {
		DriverManagerDataSource dataSource = new DriverManagerDataSource();
		
		System.out.println("Acquiring datasource");
		
		boolean isWindows = System.getProperty("os.name").startsWith("Window");
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
		
		System.out.println("Datasource acquired");
		
		return dataSource;
	}
}
