package hds.server;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

@Profile("web")
@Configuration
@Component
public class DatabaseConfig {
	@Bean
	public static DataSource dataSource() {
		DriverManagerDataSource dataSource = new DriverManagerDataSource();
		dataSource.setDriverClassName(ServerApplication.getDriver());
		dataSource.setUrl(ServerApplication.getUrl());
		dataSource.setUsername(ServerApplication.getUser());
		dataSource.setPassword(ServerApplication.getPassword());
		return dataSource;
	}
}
